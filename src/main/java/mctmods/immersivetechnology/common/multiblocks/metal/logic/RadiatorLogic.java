package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.common.multiblocks.helper.ITIDisplayContext;
import mctmods.immersivetechnology.common.multiblocks.helper.ITMultiblockPOIHelper;
import mctmods.immersivetechnology.common.multiblocks.helper.ITIPressurizedFluidOutput;
import mctmods.immersivetechnology.common.multiblocks.metal.process.RadiatorProcess;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.RadiatorRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.RadiatorShape;
import mctmods.immersivetechnology.core.ITServerConfig;
import mctmods.immersivetechnology.core.lib.ITSound;
import mctmods.immersivetechnology.core.registration.ITSounds;
import mctmods.immersivetechnology.core.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.core.util.ITCachedRecipe;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.BiFunction;

public class RadiatorLogic implements IMultiblockLogic<RadiatorLogic.State>, IServerTickableComponent<RadiatorLogic.State>, IClientTickableComponent<RadiatorLogic.State>, ITIPressurizedFluidOutput<RadiatorLogic.State> {
    public static final int INPUT_TANK_CAPACITY = 8 * FluidType.BUCKET_VOLUME;
    public static final int OUTPUT_TANK_CAPACITY = 8 * FluidType.BUCKET_VOLUME;

    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(RadiatorShape.DATA.pointsOfInterest);

    public static final List<BlockPos> INPUT_FLUID_POIS = ITMultiblockPOIHelper.getPosList(RAW_POIS, "fluid_input0");
    public static final List<BlockPos> OUTPUT_FLUID_POIS = ITMultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output0");
    public static final BlockPos REDSTONE_POI = ITMultiblockPOIHelper.getPosList(RAW_POIS, "redstone0").getFirst();
    public static final BlockPos SOUND_POI = ITMultiblockPOIHelper.getPosList(RAW_POIS, "sound0").getFirst();

    private static final RelativeBlockFace INPUT_FLUID_FACING = ITMultiblockPOIHelper.getFacing(RAW_POIS, "fluid_input0");
    private static final RelativeBlockFace OUTPUT_FLUID_FACING = ITMultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output0");

    @Override public List<BlockPos> getOutputPositions() { return OUTPUT_FLUID_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FLUID_FACING); }

    @Override public List<ITMarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output()); }

    private double getBiomeSpeedMultiplier(IMultiblockContext<State> ctx) {
        if (ITServerConfig.radiatorBiomeTempFactor <= 0.0D) { return 1.0D; }
        Level level = ctx.getLevel().getRawLevel();
        if (level.dimension() == Level.NETHER) { return 0.0D; }
        BlockPos worldPos = ctx.getLevel().toAbsolute(BlockPos.ZERO);
        Biome biome = level.getBiome(worldPos).value();
        double temp = biome.getBaseTemperature();
        double deviation = temp - 0.8D;
        return 1.0D + (deviation * ITServerConfig.radiatorBiomeTempFactor);
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        pumpOutputs(ctx);
        State state = ctx.getState();
        Level level = ctx.getLevel().getRawLevel();

        boolean enabled = state.rsState.isEnabled(ctx);
        boolean wasActive = state.active;
        boolean progressChanged = false;

        double biomeMult = getBiomeSpeedMultiplier(ctx);

        for (int i = state.processQueue.size() - 1; i >= 0; i--) {
            RadiatorProcess process = state.processQueue.get(i);
            process.tick(state.tanks.output(), biomeMult);
            if (process.isComplete()) { state.processQueue.remove(i); }
        }

        if (enabled && state.processQueue.size() < 2) {
            FluidStack input = state.tanks.input().getFluid();
            RadiatorRecipe recipe = state.recipeGetter.apply(level, input);
            if (recipe != null) {
                int req = recipe.getInputAmount();
                FluidStack outF = recipe.fluidOutput();
                boolean canOutput = (outF == null || outF.isEmpty() || state.tanks.output().fill(outF, FluidAction.SIMULATE) >= outF.getAmount());
                if (input.getAmount() >= req && canOutput) {
                    state.tanks.input().drain(req, FluidAction.EXECUTE);
                    state.processQueue.add(new RadiatorProcess(recipe));
                }
            }
        }

        state.active = enabled && !state.processQueue.isEmpty();

        if (!state.processQueue.isEmpty()) {
            RadiatorProcess current = state.processQueue.getFirst();
            int newProg = current.getTicksProcessed();
            int newTotal = current.getRecipe().getTotalProcessTime();
            if (newProg != state.processProgress || newTotal != state.totalProcessTime) { state.processProgress = newProg; state.totalProcessTime = newTotal; progressChanged = true; }
        } else if (state.processProgress > 0 || state.totalProcessTime > 0) { state.processProgress = 0; state.totalProcessTime = 0; progressChanged = true; }

        boolean activeChanged = wasActive != state.active;
        boolean update = activeChanged || progressChanged;
        if (update) { ctx.markMasterDirty(); ctx.requestMasterBESync(); }
    }

    @Override public void tickClient(IMultiblockContext<RadiatorLogic.State> ctx) {
        RadiatorLogic.State state = ctx.getState();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { return; }
        if (state.active) {
            Vec3 soundVec = ctx.getLevel().toAbsolute(new Vec3(SOUND_POI.getX() + 0.5, SOUND_POI.getY() + 0.5, SOUND_POI.getZ() + 0.5));
            float att = (float) Math.max(player.distanceToSqr(soundVec) / 16, 1);
            float vol = 1f / att;
            if (vol > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
                state.isSoundPlaying = ITSound.startSound(
                        () -> state.active,
                        ctx.isValid(),
                        soundVec,
                        ITSounds.solarTower,
                        () -> {
                            LocalPlayer p = Minecraft.getInstance().player;
                            if (p == null) { return 0f; }
                            return (float) Math.max(1 - Math.sqrt(p.distanceToSqr(soundVec)) / 16, 0);
                        },
                        () -> 1f
                );
            }
        } else {
            state.isSoundPlaying = () -> false;
        }
    }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.FluidHandler.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (INPUT_FLUID_POIS.contains(localPos) && (side == null || side == INPUT_FLUID_FACING)) {
                return state.inputCap;
            }
            if (OUTPUT_FLUID_POIS.contains(localPos) && (side == null || side == OUTPUT_FLUID_FACING)) {
                return state.outputCap;
            }
            return null;
        });
    }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return RadiatorShape.GETTER; }

    public static class State implements IMultiblockState, ITIDisplayContext {
        public final BiFunction<Level, FluidStack, RadiatorRecipe> recipeGetter = ITCachedRecipe.cached(RadiatorRecipe::findRecipe);
        public final RadiatorTanks tanks;
        public IFluidHandler inputCap;
        public IFluidHandler outputCap;
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public boolean active;
        public List<RadiatorProcess> processQueue = new ArrayList<>();
        public BooleanSupplier isSoundPlaying = () -> false;
        public int processProgress = 0;
        public int totalProcessTime = 0;
        public boolean tanksDirty = false;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); this.tanksDirty = true; };

            this.tanks = new RadiatorTanks(v -> { onChanged.run(); this.tanksDirty = true; });
            this.inputCap = new ITArrayFluidHandler(tanks.input(), false, true, () -> { onChanged.run(); this.tanksDirty = true; });
            this.outputCap = new ITArrayFluidHandler(tanks.output(), true, false, () -> { onChanged.run(); this.tanksDirty = true; });
        }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.putBoolean("active", active);
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            active = nbt.getBoolean("active");
            tanksDirty = false;
        }

        @Override public void writeSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display, provider);
            nbt.put("display", display);
        }

        @Override public void readSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            if (nbt.contains("display", Tag.TAG_COMPOUND)) {
                readDisplaySyncNBT(nbt.getCompound("display"), provider);
            }
        }

        @Override public boolean isActive() { return active; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input(), tanks.output()}; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.putInt("processProgress", processProgress);
            nbt.putInt("totalProcessTime", totalProcessTime);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            processProgress = nbt.getInt("processProgress");
            totalProcessTime = nbt.getInt("totalProcessTime");
            tanksDirty = false;
        }
    }

    public record RadiatorTanks(ITMarkableFluidTank input, ITMarkableFluidTank output) {

        public RadiatorTanks(Consumer<Void> markDirty) {
            this(
                    new ITMarkableFluidTank(INPUT_TANK_CAPACITY, markDirty),
                    new ITMarkableFluidTank(OUTPUT_TANK_CAPACITY, markDirty)
            );
        }

        public static RadiatorTanks makeClient() { return new RadiatorTanks(v -> {}); }

        public CompoundTag toNBT(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.put("input", this.input.writeToNBT(provider, new CompoundTag()));
            tag.put("output", this.output.writeToNBT(provider, new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag, HolderLookup.Provider provider) {
            this.input.readFromNBT(provider, tag.getCompound("input"));
            this.output.readFromNBT(provider, tag.getCompound("output"));
        }
    }
}
