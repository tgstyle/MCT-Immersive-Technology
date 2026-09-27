package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.util.IDisplaySyncState;
import mctmods.immersivetechnology.core.util.RedstoneInput;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.interfaces.MBOverlayText;
import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.multiblock.IDataReloadAware;
import com.immersiveconvergence.api.multiblock.IFluidOutputPump;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.util.ICFluidUtils;
import com.immersiveconvergence.api.util.LayeredComparator;
import com.immersiveconvergence.api.util.MarkableFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SteelSheetmetalTankLogic implements IMultiblockLogic<SteelSheetmetalTankLogic.State>, IServerTickableComponent<SteelSheetmetalTankLogic.State>, MBOverlayText<SteelSheetmetalTankLogic.State>, IFluidOutputPump<SteelSheetmetalTankLogic.State> {
    public static BlockPos REDSTONE_POI;
    private static List<CapabilityPosition> INPUT_POIS;
    private static List<CapabilityPosition> IO_POIS;
    private static List<BlockPos> COMPARATOR_BASE;
    private static List<List<BlockPos>> COMPARATOR_LAYERS;
    public static int COMPARATOR_LAYER_COUNT;

    static { ITShapes.readPois("steel_sheetmetal_tank", SteelSheetmetalTankLogic::loadPois); }

    @Override public List<BlockPos> getOutputPositions() { return IO_POIS.stream().map(CapabilityPosition::posInMultiblock).collect(ImmutableList.toImmutableList()); }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return null; }

    @Override public List<RelativeBlockFace> getOutputFacings() { return IO_POIS.stream().map(CapabilityPosition::side).collect(ImmutableList.toImmutableList()); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return Stream.generate(() -> state.tank).limit(IO_POIS.size()).collect(ImmutableList.toImmutableList()); }

    @Override public int getTransferSpeed() { return ServerConfig.steelSheetmetalTankTransferSpeed; }

    @Override public boolean shouldPumpOutputs(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        return !RedstoneInput.unpowered(ctx, REDSTONE_POI) && !state.tank.isEmpty();
    }

    private record ConditionalFluidHandler(MarkableFluidTank tank, boolean canFill, boolean canDrain, Runnable onChange, Supplier<Boolean> allowDrain) implements IFluidHandler {
        @Override public int getTanks() { return 1; }

        @Override @Nonnull public FluidStack getFluidInTank(int tank) { return this.tank.getFluid(); }

        @Override public int getTankCapacity(int tank) { return this.tank.getCapacity(); }

        @Override public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return this.tank.isFluidValid(stack); }

        @Override public int fill(FluidStack resource, FluidAction action) {
            if (!canFill || resource.isEmpty()) { return 0; }
            if (action == FluidAction.SIMULATE) { return this.tank.fill(resource, FluidAction.SIMULATE); }
            int filled = this.tank.fill(resource, FluidAction.EXECUTE);
            if (filled > 0) { onChange.run(); }
            return filled;
        }

        @Override @Nonnull public FluidStack drain(FluidStack resource, FluidAction action) {
            if (!canDrain || !allowDrain.get() || resource.isEmpty()) { return FluidStack.EMPTY; }
            return drained(this.tank.drain(resource, action), action);
        }

        @Override @Nonnull public FluidStack drain(int maxDrain, FluidAction action) {
            if (!canDrain || !allowDrain.get() || maxDrain <= 0) { return FluidStack.EMPTY; }
            return drained(this.tank.drain(maxDrain, action), action);
        }

        private FluidStack drained(FluidStack drained, FluidAction action) {
            if (!drained.isEmpty() && action == FluidAction.EXECUTE) { onChange.run(); }
            return drained;
        }
    }

    public static class State implements IDisplaySyncState, IDataReloadAware {
        public final MarkableFluidTank tank;
        private LayeredComparator<IMultiblockContext<State>> comparatorHelper;
        private final StoredCapability<IFluidHandler> inputHandler;
        private final StoredCapability<IFluidHandler> ioHandler;
        public boolean active = false;

        public State(IInitialMultiblockContext<State> capabilitySource) {
            Runnable changedAndSync = () -> {
                capabilitySource.getSyncRunnable().run();
                capabilitySource.getMarkDirtyRunnable().run();
            };
            this.tank = new MarkableFluidTank(ServerConfig.steelSheetmetalTankCapacity, v -> changedAndSync.run());
            this.inputHandler = new StoredCapability<>(new ConditionalFluidHandler(tank, true, false, changedAndSync, () -> false));
            this.ioHandler = new StoredCapability<>(new ConditionalFluidHandler(tank, true, true, changedAndSync, () -> true));
            this.comparatorHelper = newComparator();
        }

        private LayeredComparator<IMultiblockContext<State>> newComparator() { return new LayeredComparator<>(tank.getCapacity(), COMPARATOR_LAYERS.size(), (ctx, value) -> { for (BlockPos pos : COMPARATOR_BASE) { ctx.setComparatorOutputFor(pos, value); } }, (ctx, layer, value) -> { for (BlockPos pos : COMPARATOR_LAYERS.get(layer)) { ctx.setComparatorOutputFor(pos, value); } }); }

        @Override public void onDataReload(IInitialMultiblockContext<?> context) { comparatorHelper = newComparator(); }

        @Override public boolean isActive() { return active; }

        @Override public List<AveragingEnergyStorage> getEnergies() { return ImmutableList.of(); }

        @Override public IItemHandlerModifiable getInventory() { return null; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tank}; }

        @Override public void writeSaveNBT(CompoundTag nbt) { nbt.put("tank", tank.writeToNBT(new CompoundTag())); }

        @Override public void readSaveNBT(CompoundTag nbt) { tank.readFromNBT(nbt.getCompound("tank")); }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.put("tank", tank.writeToNBT(new CompoundTag()));
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            tank.readFromNBT(nbt.getCompound("tank"));
        }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        state.comparatorHelper.update(ctx, state.tank.getFluidAmount());
        boolean isActive = shouldPumpOutputs(ctx);
        if (state.active != isActive) {
            state.active = isActive;
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
        pumpOutputs(ctx);
    }

    @Override public State createInitialState(IInitialMultiblockContext<State> capabilitySource) { return new State(capabilitySource); }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        State state = ctx.getState();
        BlockPos posIn = position.posInMultiblock();
        RelativeBlockFace side = position.side();
        if (cap != ForgeCapabilities.FLUID_HANDLER || side == null) { return LazyOptional.empty(); }
        if (INPUT_POIS.stream().anyMatch(p -> p.posInMultiblock().equals(posIn) && p.side() == side)) { return state.inputHandler.cast(ctx); }
        if (IO_POIS.stream().anyMatch(p -> p.posInMultiblock().equals(posIn) && p.side() == side)) { return state.ioHandler.cast(ctx); }
        return LazyOptional.empty();
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { }

    @Override @Nullable public List<Component> getOverlayText(State state, Player player, boolean hammer) { return ICFluidUtils.isFluidRelatedItemStack(player.getItemInHand(InteractionHand.MAIN_HAND)) ? List.of(ICFluidUtils.formatFluidStack(state.tank.getFluid())) : null; }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType) { return ITShapes.get("steel_sheetmetal_tank").getter; }

    @Override public InteractionResult click(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) {
        if (!FluidUtils.interactWithFluidHandler(player, hand, ctx.getState().tank)) { return InteractionResult.PASS; }
        ctx.markMasterDirty();
        ctx.requestMasterBESync();
        return InteractionResult.SUCCESS;
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        REDSTONE_POI = MultiblockPOIHelper.getPosList(pois, "redstone0").get(0);
        INPUT_POIS = MultiblockPOIHelper.getCapabilityPositions(pois, "fluid_input0");
        IO_POIS = MultiblockPOIHelper.getCapabilityPositions(pois, "fluid_io0");
        COMPARATOR_BASE = MultiblockPOIHelper.getPosList(pois, "comparator_base0");
        COMPARATOR_LAYERS = MultiblockPOIHelper.getPosList(pois, "comparator_layer0").stream().collect(Collectors.groupingBy(BlockPos::getY, TreeMap::new, ImmutableList.toImmutableList())).values().stream().collect(ImmutableList.toImmutableList());
        COMPARATOR_LAYER_COUNT = COMPARATOR_LAYERS.size();
    }
}
