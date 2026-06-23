package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.common.multiblocks.helper.ITDisplayContext;
import mctmods.immersivetechnology.common.multiblocks.helper.ITPressurizedFluidOutput;
import mctmods.immersivetechnology.common.multiblocks.metal.process.RadiatorHorizontalProcess;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.RadiatorRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.RadiatorHorizontalShape;
import mctmods.immersivetechnology.core.ITServerConfig;
import mctmods.immersivetechnology.core.lib.ITSound;
import mctmods.immersivetechnology.core.registration.ITSounds;
import mctmods.immersivetechnology.core.util.multiblock.PoIJSONSchema;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class RadiatorHorizontalLogic implements IMultiblockLogic<RadiatorHorizontalLogic.State>, IServerTickableComponent<RadiatorHorizontalLogic.State>, IClientTickableComponent<RadiatorHorizontalLogic.State>, ITPressurizedFluidOutput<RadiatorHorizontalLogic.State> {
    public static final int INPUT_TANK_CAPACITY = 8 * FluidType.BUCKET_VOLUME;
    public static final int OUTPUT_TANK_CAPACITY = 8 * FluidType.BUCKET_VOLUME;

    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(RadiatorHorizontalShape.DATA.pointsOfInterest);

    private static List<BlockPos> getPosList(String name) { return RAW_POIS.stream().filter(poi -> poi.name.equals(name)).map(poi -> new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2])).collect(ImmutableList.toImmutableList()); }

    public static final List<BlockPos> FLUID_INPUT_POIS = getPosList("fluid_input");
    public static final List<BlockPos> FLUID_OUTPUT_POIS = getPosList("fluid_output");
    public static final BlockPos REDSTONE_POI = getPosList("redstone").get(0);
    public static final BlockPos SOUND_POS = getPosList("sound").get(0);

    @Override public List<BlockPos> getOutputPositions() { return FLUID_OUTPUT_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(RelativeBlockFace.FRONT); }

    @Override public List<ITMarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output()); }

    private double getBiomeSpeedMultiplier(IMultiblockContext<State> ctx) {
        if (ITServerConfig.radiatorBiomeTempFactor <= 0.0D) return 1.0D;
        Level level = ctx.getLevel().getRawLevel();
        if (level.dimension() == Level.NETHER) return 0.0D;
        BlockPos worldPos = ctx.getLevel().toAbsolute(BlockPos.ZERO);
        Biome biome = level.getBiome(worldPos).value();
        double temp = biome.getBaseTemperature();
        double deviation = temp - 0.8D;
        return 1.0D + (deviation * ITServerConfig.radiatorBiomeTempFactor);
    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx) {
        pumpOutputs(ctx);
        State state = ctx.getState();
        Level level = ctx.getLevel().getRawLevel();

        boolean enabled = state.rsState.isEnabled(ctx);
        boolean wasActive = state.active;
        boolean progressChanged = false;

        double biomeMult = getBiomeSpeedMultiplier(ctx);

        for (int i = state.processQueue.size() - 1; i >= 0; i--) {
            RadiatorHorizontalProcess process = state.processQueue.get(i);
            process.tick(state, biomeMult);
            if (process.isComplete()) { state.processQueue.remove(i); }
        }

        if (enabled && state.processQueue.size() < 2) {
            FluidStack input = state.tanks.input().getFluid();
            RadiatorRecipe recipe = RadiatorRecipe.findRecipe(level, input);
            if (recipe != null) {
                if (input.getAmount() >= recipe.input.getAmount() &&
                        state.tanks.output().fill(recipe.fluidOutput, FluidAction.SIMULATE) >= recipe.fluidOutput.getAmount()) {
                    state.tanks.input().drain(recipe.input.getAmount(), FluidAction.EXECUTE);
                    state.processQueue.add(new RadiatorHorizontalProcess(recipe));
                }
            }
        }

        state.active = enabled && !state.processQueue.isEmpty();

        if (!state.processQueue.isEmpty()) {
            RadiatorHorizontalProcess current = state.processQueue.get(0);
            int newProg = current.getTicksProcessed();
            int newTotal = current.getRecipe().totalProcessTime;
            if (newProg != state.processProgress || newTotal != state.totalProcessTime) { state.processProgress = newProg; state.totalProcessTime = newTotal; progressChanged = true; }
        } else if (state.processProgress > 0 || state.totalProcessTime > 0) { state.processProgress = 0; state.totalProcessTime = 0; progressChanged = true; }

        boolean activeChanged = wasActive != state.active;
        boolean update = activeChanged || progressChanged;
        if (update) { ctx.markMasterDirty(); ctx.requestMasterBESync(); }
    }

    @Override public void tickClient(IMultiblockContext<RadiatorHorizontalLogic.State> ctx) {
        RadiatorHorizontalLogic.State state = ctx.getState();
        if (state.active) { state.soundCooldown = 40; } else if (state.soundCooldown > 0) { state.soundCooldown--; }
        handleSounds(ctx, state);
    }

    private void handleSounds(IMultiblockContext<RadiatorHorizontalLogic.State> ctx, RadiatorHorizontalLogic.State state) {
        if (state.isSoundPlaying.getAsBoolean()) { return; }
        Vec3 soundVec = ctx.getLevel().toAbsolute(new Vec3(SOUND_POS.getX() + 0.5, SOUND_POS.getY() + 0.5, SOUND_POS.getZ() + 0.5));
        state.isSoundPlaying = ITSound.startSound(() -> state.soundCooldown > 0, ctx.isValid(), soundVec, ITSounds.solarTower, () -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) { return 0f; }
            return (float) Math.max(1 - Math.sqrt(player.distanceToSqr(soundVec)) / 16, 0);
        }, () -> 1f);
    }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        State state = ctx.getState();
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            BlockPos localPos = position.posInMultiblock();
            if (FLUID_INPUT_POIS.contains(localPos)) { return state.inputCap.cast(ctx); }
            if (FLUID_OUTPUT_POIS.contains(localPos)) { return state.outputCap.cast(ctx); }
        }
        return LazyOptional.empty();
    }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return RadiatorHorizontalShape.GETTER; }

    public static class State implements IMultiblockState, ITDisplayContext {
        public final RadiatorTanks tanks;
        public final StoredCapability<IFluidHandler> inputCap;
        public final StoredCapability<IFluidHandler> outputCap;
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public boolean active;
        public int soundCooldown = 0;
        public List<RadiatorHorizontalProcess> processQueue = new ArrayList<>();
        public BooleanSupplier isSoundPlaying = () -> false;
        public int processProgress = 0;
        public int totalProcessTime = 0;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Consumer<Void> onChanged = v -> { markDirty.run(); sync.run(); };

            this.tanks = new RadiatorTanks(onChanged);
            this.inputCap = new StoredCapability<>(ITArrayFluidHandler.fillOnly(tanks.input(), () -> onChanged.accept(null)));
            this.outputCap = new StoredCapability<>(ITArrayFluidHandler.drainOnly(tanks.output(), () -> onChanged.accept(null)));
        }

        @Override public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("tanks", tanks.toNBT());
            nbt.putBoolean("active", active);
        }

        @Override public void readSaveNBT(CompoundTag nbt) {
            tanks.readNBT(nbt.getCompound("tanks"));
            active = nbt.getBoolean("active");
        }

        @Override public void writeSyncNBT(CompoundTag nbt) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display);
            nbt.put("display", display);
        }

        @Override public void readSyncNBT(CompoundTag nbt) {
            if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display")); }
        }

        @Override public boolean isActive() { return active; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input(), tanks.output()}; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT());
            nbt.putInt("processProgress", processProgress);
            nbt.putInt("totalProcessTime", totalProcessTime);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"));
            processProgress = nbt.getInt("processProgress");
            totalProcessTime = nbt.getInt("totalProcessTime");
        }
    }

    public record RadiatorTanks(ITMarkableFluidTank input, ITMarkableFluidTank output) {

        public RadiatorTanks(Consumer<Void> markDirty) {
            this(
                    new ITMarkableFluidTank(INPUT_TANK_CAPACITY, markDirty),
                    new ITMarkableFluidTank(OUTPUT_TANK_CAPACITY, markDirty)
            );
        }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("input", input.writeToNBT(new CompoundTag()));
            tag.put("output", output.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            input.readFromNBT(tag.getCompound("input"));
            output.readFromNBT(tag.getCompound("output"));
        }
    }
}
