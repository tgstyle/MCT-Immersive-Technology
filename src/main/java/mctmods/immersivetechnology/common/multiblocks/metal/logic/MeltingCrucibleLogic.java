package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.common.multiblocks.helper.*;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.MeltingCrucibleRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.MeltingCrucibleShape;
import mctmods.immersivetechnology.common.multiblocks.metal.process.MeltingCrucibleProcess;
import mctmods.immersivetechnology.core.ITServerConfig;
import mctmods.immersivetechnology.core.util.multiblock.PoIJSONSchema;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class MeltingCrucibleLogic implements IMultiblockLogic<MeltingCrucibleLogic.State>, IServerTickableComponent<MeltingCrucibleLogic.State>, IClientTickableComponent<MeltingCrucibleLogic.State>, ITPressurizedFluidOutput<MeltingCrucibleLogic.State>
{
    public static final int SLOT_INPUT_FILLED = 1;
    public static final int SLOT_INPUT_EMPTY = 2;
    public static final int SLOT_OUTPUT_EMPTY = 3;
    public static final int SLOT_OUTPUT_FILLED = 4;
    public static final int INPUT_SLOT = 0;

    public static final int TANK_CAPACITY = ITServerConfig.distillerTankCapacity;
    public static final int ENERGY_CAPACITY = ITServerConfig.distillerEnergyCapacity;

    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(MeltingCrucibleShape.DATA.pointsOfInterest);

    public static final BlockPos REDSTONE_POI = getPosList("redstone").get(0);
    public static final CapabilityPosition OUTPUT_FLUID_POI = new CapabilityPosition(getPosList("fluid_output").get(0), getFacing("fluid_output"));
    private static final List<BlockPos> ENERGY_POI_POS = getPosList("energy_input");
    private static final RelativeBlockFace ENERGY_POI_FACING = getFacing("energy_input");
    public static final CapabilityPosition ENERGY_POI = new CapabilityPosition(ENERGY_POI_POS.get(0), ENERGY_POI_FACING);
    private static final List<BlockPos> FLUID_OUTPUT_POIS = getPosList("fluid_output");
    private static final RelativeBlockFace OUTPUT_FACING = getFacing("fluid_output");

    private static List<BlockPos> getPosList(String name) { return RAW_POIS.stream().filter(poi -> poi.name.equals(name)).map(poi -> new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2])).collect(ImmutableList.toImmutableList()); }
    private static RelativeBlockFace getFacing(String name) {
        List<RelativeBlockFace> facings = RAW_POIS.stream().filter(poi -> poi.name.equals(name)).flatMap(poi -> poi.relativeFaces.stream()).distinct().toList();
        if (facings.size() != 1) { throw new RuntimeException("Inconsistent facings for POI: " + name); }
        return facings.get(0);
    }

    private void tryEnqueueProcess(MeltingCrucibleLogic.State state, Level level, MeltingCrucibleRecipe recipe) {

    }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<MeltingCrucibleLogic.State> ctx, CapabilityPosition position, Capability<T> cap) {
        MeltingCrucibleLogic.State state = ctx.getState();
//        if (cap == ForgeCapabilities.ENERGY) {
//            if (position.posInMultiblock().equals(ENERGY_POI.posInMultiblock()) && (position.side() == null || position.side() == ENERGY_POI.side())) { return state.energyCap.cast(ctx); }
//        }
//        else if (cap == ForgeCapabilities.FLUID_HANDLER) {
//            if (position.posInMultiblock().equals(OUTPUT_FLUID_POI.posInMultiblock()) && (position.side() == null || position.side() == OUTPUT_FLUID_POI.side())) { return state.outputCap.cast(ctx); }
//        }
//        else if (cap == ForgeCapabilities.ITEM_HANDLER) {
//            return state.invCap.cast(ctx);
//        }
        return LazyOptional.empty();
    }

    @Override public List<BlockPos> getOutputPositions() { return FLUID_OUTPUT_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<MeltingCrucibleLogic.State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FACING); }

    @Override public List<ITMarkableFluidTank> getOutputTanks(MeltingCrucibleLogic.State state) {
        return ImmutableList.of(state.tank.output);
    }

    @Override
    public void tickClient(IMultiblockContext<State> iMultiblockContext) {

    }

    @Override
    public void tickServer(IMultiblockContext<State> iMultiblockContext) {

    }

    @Override public MeltingCrucibleLogic.State createInitialState(IInitialMultiblockContext<MeltingCrucibleLogic.State> ctx) { return new MeltingCrucibleLogic.State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return MeltingCrucibleShape.GETTER; }

    public static class State implements IMultiblockState//, ITProcessContext.ProcessContextInMachine<MeltingCrucibleRecipe>, ITDisplayContext
    {
//        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final MeltingCrucibleLogic.MeltingCrucibleTank tank;
        private final IFluidTank[] tankArray;

        public State(IInitialMultiblockContext<MeltingCrucibleLogic.State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); };
            this.tank = new MeltingCrucibleLogic.MeltingCrucibleTank(v -> onChanged.run());
            this.tankArray = new IFluidTank[]{tank.output};
//
        }

        public MeltingCrucibleLogic.MeltingCrucibleTank getTanks() { return tank; }

        @Override public void writeSaveNBT(CompoundTag nbt) {
//            nbt.put("energy", energy.serializeNBT());
//            nbt.put("tank", this.tank.toNBT());
//            nbt.put("processor", processor.toNBT());
//            nbt.put("inventory", inventory.serializeNBT());
//            nbt.putBoolean("active", active);
        }

        @Override public void readSaveNBT(CompoundTag nbt) {
//            energy.deserializeNBT(nbt.get("energy"));
//            this.tank.readNBT(nbt.getCompound("tank"));
//            this.processor.fromNBT(nbt.getList("processor", Tag.TAG_COMPOUND), MeltingCrucibleProcess::new);
//            this.inventory.deserializeNBT(nbt.getCompound("inventory"));
//            active = nbt.getBoolean("active");
        }

        @Override public void writeSyncNBT(CompoundTag nbt) {
            CompoundTag display = new CompoundTag();
            //writeDisplaySyncNBT(display);
            nbt.put("display", display);
        }

        @Override public void readSyncNBT(CompoundTag nbt) {
            //if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display")); }
        }

//        @Override public AveragingEnergyStorage getEnergy() { return energy; }
//
//        @Override public IFluidTank[] getInternalTanks() { return tankArray; }
//
//        @Override public int[] getOutputTanks() { return new int[]{1}; }
//
//        @Override public int[] getOutputSlots() { return new int[]{OUTPUT_SLOT}; }
//
//        @Override public boolean isActive() { return active; }
//
//        @Override public void writeDisplaySyncNBT(CompoundTag nbt) {
//            nbt.putBoolean("active", active);
//            nbt.put("tank", tank.toNBT());
//            nbt.put("energy", energy.serializeNBT());
//            nbt.put("inventory", inventory.serializeNBT());
//        }
//
//        @Override public void readDisplaySyncNBT(CompoundTag nbt) {
//            active = nbt.getBoolean("active");
//            tank.readNBT(nbt.getCompound("tank"));
//            if (energy == null) { energy = new MeltingCrucibleLogic.SyncEnergyStorage(ENERGY_CAPACITY, () -> {}); }
//            energy.deserializeNBT(nbt.get("energy"));
//            inventory.deserializeNBT(nbt.getCompound("inventory"));
//        }
    }

    public record MeltingCrucibleTank(ITMarkableFluidTank output) {
        public MeltingCrucibleTank(Consumer<Void> markDirty) {
            this(new ITMarkableFluidTank(TANK_CAPACITY, markDirty));
        }

        public static MeltingCrucibleLogic.MeltingCrucibleTank makeClient() { return new MeltingCrucibleLogic.MeltingCrucibleTank(v -> {}); }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("output", this.output.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            this.output.readFromNBT(tag.getCompound("output"));
        }

        @SuppressWarnings("unused")
        public int getCapacity() { return TANK_CAPACITY; }
    }

    private static class SyncEnergyStorage extends AveragingEnergyStorage {
        private final Runnable onChanged;

        public SyncEnergyStorage(int capacity, Runnable onChanged) {
            super(capacity);
            this.onChanged = onChanged;
        }

        @Override public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (received > 0 && !simulate) { onChanged.run(); }
            return received;
        }

        @Override public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (extracted > 0 && !simulate) { onChanged.run(); }
            return extracted;
        }
    }
}
