package mctmods.immersivetechnology.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.util.CachedRecipe;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.client.particles.ColoredSmokeData;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.SteamTurbineRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.SteamTurbineShape;
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.lib.ITMultiblockSound;
import mctmods.immersivetechnology.core.registration.ITSounds;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class ITSteamTurbineLogic implements IMultiblockLogic<ITSteamTurbineLogic.State>, IServerTickableComponent<ITSteamTurbineLogic.State>, IClientTickableComponent<ITSteamTurbineLogic.State> {
    private static final List<BlockPos> FLUID_POS1 = List.of(new BlockPos(2, 1, 9), new BlockPos(1, 1, 0));
    private static final List<BlockPos> FLUID_POS2 = List.of(new BlockPos(1, 0, 1));

    public static final BlockPos REDSTONE_POS = new BlockPos(0, 1, 9);

    public static final int TANK_CAPACITY = 12 * FluidType.BUCKET_VOLUME;

    private static final CapabilityPosition FLUID_OUTPUT_POS = new CapabilityPosition(1, 0, 1, RelativeBlockFace.FRONT);

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        float targetLevel = ITLib.remapRange(0, state.maxSpeed, 0.55f, 1.0f, state.speed);
        if (state.currentLevel == 0f) { state.currentLevel = targetLevel; }
        else state.currentLevel = state.currentLevel * 0.9f + targetLevel * 0.1f;
        float smoothedLevel = state.currentLevel;

        float targetPitch = ITLib.remapRange(0, state.maxSpeed, 0.5f, 1.5f, state.speed);
        if (state.currentPitch == 0f) { state.currentPitch = targetPitch; }
        else state.currentPitch = state.currentPitch * 0.95f + targetPitch * 0.05f;
        if (state.currentPitch < 0.5f) { state.currentPitch = 0.5f; }

        if (state.active || state.animation_fanFadeIn > 0 || state.animation_fanFadeOut > 0) {
            float base = (state.speed / (float)state.maxSpeed) * 72f;
            float step = state.active ? base : 0;
            if (state.animation_fanFadeIn > 0) {
                step -= (state.animation_fanFadeIn / 80f) * base;
                state.animation_fanFadeIn--;
            }
            if (state.animation_fanFadeOut > 0) {
                step += (state.animation_fanFadeOut / 80f) * base;
                state.animation_fanFadeOut--;
            }
            state.animation_fanRotationStep = step;
            state.animation_fanRotation += step;
            state.animation_fanRotation %= 360;
        }

        if (!state.isSoundPlaying.getAsBoolean()) {
            final Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(2.5, 1.5, 1.5));
            state.soundId++;
            int thisId = state.soundId;
            state.isSoundPlaying = ITMultiblockSound.startSound(
                    () -> (state.active || state.animation_fanFadeOut > 0) && state.soundId == thisId,
                    ctx.isValid(),
                    soundPos,
                    ITSounds.steamTurbine,
                    () -> {
                        LocalPlayer player = Minecraft.getInstance().player;
                        if (player == null) return 0f;
                        float attenuation = (float) Math.max(player.distanceToSqr(soundPos) / 32, 1);
                        return (11 * (smoothedLevel - 0.5f)) / attenuation;
                    },
                    () -> state.currentPitch
            );
        }
        if (state.active && ctx.getLevel().shouldTickModulo(2)) {
            Direction facing = ctx.getLevel().getOrientation().front();
            BlockPos outputRel = new BlockPos(1, 0, 1);
            BlockPos outputAbs = ctx.getLevel().toAbsolute(outputRel);
            BlockPos adjacentAbs = outputAbs.relative(facing);
            boolean connected = state.fluidOutput.isPresent();
            if (!connected) {
                Vec3 smokePos = new Vec3(adjacentAbs.getX() + 0.5, adjacentAbs.getY() + 0.5, adjacentAbs.getZ() + 0.5);
                double velX = facing.getStepX() * 0.125 + particleXZSpeed();
                double velY = facing.getStepY() * 0.1 + 0.0625;
                double velZ = facing.getStepZ() * 0.125 + particleXZSpeed();

                FluidStack outFluid = state.tanks.output.getFluid();
                float r = 0.5F, g = 0.5F, b = 0.5F;
                if (!outFluid.isEmpty()) {
                    int tint = IClientFluidTypeExtensions.of(outFluid.getFluid()).getTintColor(outFluid);
                    r = ((tint >> 16) & 0xFF) / 255f;
                    g = ((tint >> 8) & 0xFF) / 255f;
                    b = (tint & 0xFF) / 255f;
                }

                Level level2 = ctx.getLevel().getRawLevel();
                level2.addAlwaysVisibleParticle(
                        new ColoredSmokeData(r, g, b),
                        smokePos.x, smokePos.y, smokePos.z,
                        velX, velY, velZ
                );
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void tickServer(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        boolean previouslyActive = state.active;
        state.active = false;

        if (state.burnRemaining > 0) {
            state.burnRemaining--;
            speedUp(state);
        }
        else if (state.rsState.isEnabled(ctx)) {
            FluidStack fluid = state.tanks.input.getFluid();
            SteamTurbineRecipe recipe = state.recipeGetter.apply(ctx.getLevel().getRawLevel(), fluid);
            if (recipe != null && fluid.getAmount() >= recipe.inputAmount) {
                state.tanks.input.drain(recipe.inputAmount, FluidAction.EXECUTE);
                if (recipe.fluidOutput != null) {
                    int filled = state.tanks.output.fill(recipe.fluidOutput, FluidAction.EXECUTE);
                    if (filled < recipe.fluidOutput.getAmount()) {
                        // Excess discarded, operation continues
                    }
                }
                state.burnRemaining = recipe.getTotalProcessTime() - 1;
                speedUp(state);
            }
            else {
                speedDown(state);
            }
        }
        else {
            speedDown(state);
        }

        boolean changed = false;
        if (state.tanks.output.getFluidAmount() > 0 && state.fluidOutput.isPresent()) {
            IFluidHandler handler = state.fluidOutput.get();
            FluidStack out = state.tanks.output.getFluid();
            if (out.getAmount() > 0) {
                out = out.copy();
                int accepted = handler.fill(out, FluidAction.SIMULATE);
                if (accepted > 0) {
                    int drained = handler.fill(Utils.copyFluidStackWithAmount(out, accepted, false), FluidAction.EXECUTE);
                    state.tanks.output.drain(drained, FluidAction.EXECUTE);
                    if (drained > 0) changed = true;
                }
            }
        }

        if (previouslyActive != state.active || state.speed % 20 == 0 || changed) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        final State state = ctx.getState();
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (position.side() == null || (position.side() == RelativeBlockFace.BACK && FLUID_POS1.contains(position.posInMultiblock()))) { return state.fluidCap.cast(ctx); }
            else if (position.side() == RelativeBlockFace.FRONT && FLUID_POS2.contains(position.posInMultiblock())) { return state.fluidCapExhaust.cast(ctx); }
        }
        return LazyOptional.empty();
    }

    @Override
    public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return SteamTurbineShape.GETTER; }

    public ITSlotwiseItemHandler getInventory() { return null; }

    public SteamTurbineTank getTanks() { return null; }

    private void speedUp(State state) {
        state.speed = Math.min(state.maxSpeed, state.speed + state.speedUpRate);
        state.active = true;
    }

    private void speedDown(State state) {
        state.speed = Math.max(0, state.speed - state.slowDownRate);
    }

    private static double particleXZSpeed() { return ApiUtils.RANDOM.nextDouble(-0.015625, 0.015625); }

    public static class State implements IMultiblockState {
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final SteamTurbineTank tanks;
        public final StoredCapability<IFluidHandler> fluidCap;
        public final StoredCapability<IFluidHandler> fluidCapExhaust;
        public final CapabilityReference<IFluidHandler> fluidOutput;
        private final BiFunction<Level, FluidStack, SteamTurbineRecipe> recipeGetter;
        public int maxSpeed = 1800;
        public int speed = 0;
        public boolean active = false;
        private int burnRemaining = 0;
        public BooleanSupplier isSoundPlaying = () -> false;
        private transient int soundId = 0;
        public float animation_fanRotationStep = 0;
        public float animation_fanRotation = 0;
        private transient int animation_fanFadeIn = 0;
        private transient int animation_fanFadeOut = 0;
        private transient float currentLevel = 0f;
        private transient float currentPitch = 0f;
        private final int slowDownRate = 6;
        private final int speedUpRate = 3;

        public State(IInitialMultiblockContext<State> ctx) {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            final Runnable sync = ctx.getSyncRunnable();
            final Runnable onChanged = () -> { markDirty.run(); sync.run(); };
            this.tanks = new SteamTurbineTank(v -> onChanged.run());
            this.fluidCap = new StoredCapability<>(new ITArrayFluidHandler(tanks.input, false, true, onChanged));
            this.fluidCapExhaust = new StoredCapability<>(new ITArrayFluidHandler(tanks.output, true, false, onChanged));
            this.recipeGetter = CachedRecipe.cached(SteamTurbineRecipe::findFuel);
            CapabilityPosition opposingPos = CapabilityPosition.opposing(new MultiblockFace(FLUID_OUTPUT_POS.side(), FLUID_OUTPUT_POS.posInMultiblock()));
            this.fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, new MultiblockFace(opposingPos.side(), opposingPos.posInMultiblock()));
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt) {
            nbt.putInt("speed", speed);
            nbt.putBoolean("active", active);
            nbt.putInt("burnRemaining", burnRemaining);
            nbt.put("tanks", tanks.toNBT());
        }

        @Override
        public void readSaveNBT(CompoundTag nbt) {
            speed = nbt.getInt("speed");
            active = nbt.getBoolean("active");
            burnRemaining = nbt.getInt("burnRemaining");
            tanks.readNBT(nbt.getCompound("tanks"));
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.putInt("speed", speed);
            nbt.put("tanks", tanks.toNBT());
        }

        @Override
        public void readSyncNBT(CompoundTag nbt) {
            final boolean oldActive = active;
            active = nbt.getBoolean("active");
            speed = nbt.getInt("speed");
            tanks.readNBT(nbt.getCompound("tanks"));
            if (active && !oldActive) { animation_fanFadeIn = 80; }
            else if (!active && oldActive) { animation_fanFadeOut = 80; }
        }

        public boolean isActive() { return active; }
    }

    @SuppressWarnings("unused")
    public record SteamTurbineTank(ITMarkableFluidTank input, ITMarkableFluidTank output) {
        public SteamTurbineTank(Consumer<Void> markDirty) {
            this(new ITMarkableFluidTank(TANK_CAPACITY, markDirty), new ITMarkableFluidTank(TANK_CAPACITY, markDirty));
        }

        public static SteamTurbineTank makeClient() { return new SteamTurbineTank(v -> {}); }

        public Tag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("input", this.input.writeToNBT(new CompoundTag()));
            tag.put("output", this.output.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            this.input.readFromNBT(tag.getCompound("input"));
            this.output.readFromNBT(tag.getCompound("output"));
        }

        public int getCapacity() { return TANK_CAPACITY; }
    }
}
