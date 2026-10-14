package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.util.IDisplaySyncState;

import com.immersiveconvergence.api.multiblock.blocks.component.IMultiblockComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.IServerTickableComponent;
import com.immersiveconvergence.api.multiblock.blocks.env.IInitialMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.logic.IMultiblockLogic;
import com.immersiveconvergence.api.multiblock.blocks.util.CapabilityPosition;
import com.immersiveconvergence.api.multiblock.blocks.util.RelativeBlockFace;
import com.immersiveconvergence.api.multiblock.blocks.util.ShapeType;
import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.block.BlockInterfaces;
import com.immersiveconvergence.api.block.IMBOverlayText;
import com.immersiveconvergence.api.multiblock.IDataReloadAware;
import com.immersiveconvergence.api.multiblock.IFluidOutputPump;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.util.DelegatingFluidTank;
import com.immersiveconvergence.api.util.ICFluidUtils;
import com.immersiveconvergence.api.util.LayeredComparator;
import com.immersiveconvergence.api.util.MarkableFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SteelSheetmetalTankLogic implements IMultiblockLogic<SteelSheetmetalTankLogic.State>, IServerTickableComponent<SteelSheetmetalTankLogic.State>, IMBOverlayText<SteelSheetmetalTankLogic.State>, IFluidOutputPump<SteelSheetmetalTankLogic.State>, BlockInterfaces.ILadderPositionProvider {
    private static List<CapabilityPosition> INPUT_POIS;
    private static List<CapabilityPosition> IO_POIS;
    private static BlockPos OUTPUT0_POI;
    private static BlockPos OUTPUT1_POI;
    private static BlockPos OUTPUT2_POI;
    private static RelativeBlockFace OUTPUT0_FACING;
    private static RelativeBlockFace OUTPUT1_FACING;
    private static RelativeBlockFace OUTPUT2_FACING;
    private static List<BlockPos> COMPARATOR_BASE;
    private static List<List<BlockPos>> COMPARATOR_LAYERS;
    public static int COMPARATOR_HEIGHT;
    private static Set<BlockPos> LADDER_POSITIONS;

    static { ITShapes.readPois("steel_sheetmetal_tank", SteelSheetmetalTankLogic::loadPois); }

    @Override public boolean isLadderPos(BlockPos posInMB) { return LADDER_POSITIONS.contains(posInMB); }

    @Override public List<BlockPos> getOutputPositions() { return Stream.concat(IO_POIS.stream().map(CapabilityPosition::posInMultiblock), Stream.of(OUTPUT0_POI, OUTPUT1_POI, OUTPUT2_POI)).collect(ImmutableList.toImmutableList()); }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return null; }

    @Override public List<RelativeBlockFace> getOutputFacings() { return Stream.concat(IO_POIS.stream().map(CapabilityPosition::side), Stream.of(OUTPUT0_FACING, OUTPUT1_FACING, OUTPUT2_FACING)).collect(ImmutableList.toImmutableList()); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return Stream.concat(Stream.generate(() -> state.tank).limit(IO_POIS.size()), Stream.of(state.output0Tank, state.output1Tank, state.output2Tank)).collect(ImmutableList.toImmutableList()); }

    @Override public int getTransferSpeed() { return ServerConfig.steelSheetmetalTankTransferSpeed; }

    @Override public boolean shouldPumpOutputs(IMultiblockContext<State> ctx) { return !ctx.getState().tank.isEmpty(); }

    private record ConditionalFluidHandler(MarkableFluidTank tank, boolean canFill, boolean canDrain, Runnable onChange, Supplier<Boolean> allowDrain) implements IFluidHandler {
        @Override public int getTanks() { return 1; }

        @Override @Nonnull public FluidStack getFluidInTank(int tank) { return this.tank.getFluid(); }

        @Override public int getTankCapacity(int tank) { return this.tank.getCapacity(); }

        @Override public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return this.tank.isFluidValid(stack); }

        @Override public int fill(@Nonnull FluidStack resource, @Nonnull FluidAction action) {
            if (!canFill || resource.isEmpty()) { return 0; }
            if (action == FluidAction.SIMULATE) { return this.tank.fill(resource, FluidAction.SIMULATE); }
            int filled = this.tank.fill(resource, FluidAction.EXECUTE);
            if (filled > 0) { onChange.run(); }
            return filled;
        }

        @Override @Nonnull public FluidStack drain(@Nonnull FluidStack resource, @Nonnull FluidAction action) {
            if (!canDrain || !allowDrain.get() || resource.isEmpty()) { return FluidStack.EMPTY; }
            return drained(this.tank.drain(resource, action), action);
        }

        @Override @Nonnull public FluidStack drain(int maxDrain, @Nonnull FluidAction action) {
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
        public final IFluidHandler inputHandler;
        public final IFluidHandler ioHandler;
        public final IFluidHandler outputHandler;
        public final MarkableFluidTank output0Tank;
        public final MarkableFluidTank output1Tank;
        public final MarkableFluidTank output2Tank;
        public boolean active = false;

        public State(IInitialMultiblockContext<State> capabilitySource) {
            Runnable changedAndSync = () -> {
                capabilitySource.getSyncRunnable().run();
                capabilitySource.getMarkDirtyRunnable().run();
            };
            this.tank = new MarkableFluidTank(ServerConfig.steelSheetmetalTankCapacity, v -> changedAndSync.run());
            this.inputHandler = new ConditionalFluidHandler(tank, true, false, changedAndSync, () -> false);
            this.ioHandler = new ConditionalFluidHandler(tank, true, true, changedAndSync, () -> true);
            this.outputHandler = new ConditionalFluidHandler(tank, false, true, changedAndSync, () -> true);
            this.output0Tank = new DelegatingFluidTank(tank);
            this.output1Tank = new DelegatingFluidTank(tank);
            this.output2Tank = new DelegatingFluidTank(tank);
            this.comparatorHelper = newComparator();
        }

        private LayeredComparator<IMultiblockContext<State>> newComparator() {
            return new LayeredComparator<>(tank.getCapacity(), COMPARATOR_LAYERS.size(), (ctx, value) -> { for (BlockPos pos : COMPARATOR_BASE) { ctx.setComparatorOutputFor(pos, value); } }, (ctx, layer, value) -> { for (BlockPos pos : COMPARATOR_LAYERS.get(layer)) { ctx.setComparatorOutputFor(pos, value); } });
        }

        @Override public void onDataReload(IInitialMultiblockContext<?> context) { comparatorHelper = newComparator(); }

        @Override public boolean isActive() { return active; }

        @Override public IItemHandlerModifiable getInventory() { return null; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tank}; }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) { nbt.put("tank", tank.writeToNBT(provider, new CompoundTag())); }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) { tank.readFromNBT(provider, nbt.getCompound("tank")); }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.put("tank", tank.writeToNBT(provider, new CompoundTag()));
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            active = nbt.getBoolean("active");
            tank.readFromNBT(provider, nbt.getCompound("tank"));
        }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        state.comparatorHelper.update(ctx, state.tank.getFluidAmount());
        boolean isActive = !state.tank.isEmpty();
        if (state.active != isActive) {
            state.active = isActive;
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
        pumpOutputs(ctx);
    }

    @Override public State createInitialState(IInitialMultiblockContext<State> capabilitySource) { return new State(capabilitySource); }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.FluidHandler.BLOCK, (state, position) -> {
            BlockPos posIn = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (side == null) { return null; }
            if (INPUT_POIS.stream().anyMatch(p -> p.posInMultiblock().equals(posIn) && p.side() == side)) { return state.inputHandler; }
            if (IO_POIS.stream().anyMatch(p -> p.posInMultiblock().equals(posIn) && p.side() == side)) { return state.ioHandler; }
            if ((posIn.equals(OUTPUT0_POI) && side == OUTPUT0_FACING) || (posIn.equals(OUTPUT1_POI) && side == OUTPUT1_FACING) || (posIn.equals(OUTPUT2_POI) && side == OUTPUT2_FACING)) { return state.outputHandler; }
            return null;
        });
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { }

    @Override @Nullable public List<Component> getOverlayText(State state, BlockPos pos, BlockHitResult hit, Player player, boolean hammer) { return ICFluidUtils.isFluidRelatedItemStack(player.getItemInHand(InteractionHand.MAIN_HAND)) ? List.of(ICFluidUtils.formatFluidStack(state.tank.getFluid())) : null; }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType) { return ITShapes.get("steel_sheetmetal_tank").getter; }

    @Override public ItemInteractionResult click(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) {
        if (!ICFluidUtils.interactWithFluidHandler(player, hand, ctx.getState().tank)) { return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; }
        ctx.markMasterDirty();
        ctx.requestMasterBESync();
        return ItemInteractionResult.SUCCESS;
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        INPUT_POIS = MultiblockPOIHelper.getCapabilityPositions(pois, "fluid_input0");
        IO_POIS = MultiblockPOIHelper.getCapabilityPositions(pois, "fluid_io0");
        OUTPUT0_POI = MultiblockPOIHelper.getPosList(pois, "fluid_output0").getFirst();
        OUTPUT1_POI = MultiblockPOIHelper.getPosList(pois, "fluid_output1").getFirst();
        OUTPUT2_POI = MultiblockPOIHelper.getPosList(pois, "fluid_output2").getFirst();
        OUTPUT0_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_output0");
        OUTPUT1_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_output1");
        OUTPUT2_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_output2");
        COMPARATOR_BASE = MultiblockPOIHelper.getPosList(pois, "comparator_base0");
        COMPARATOR_LAYERS = MultiblockPOIHelper.getPosList(pois, "comparator_layer0").stream().collect(Collectors.groupingBy(BlockPos::getY, TreeMap::new, ImmutableList.toImmutableList())).values().stream().collect(ImmutableList.toImmutableList());
        COMPARATOR_HEIGHT = COMPARATOR_LAYERS.size() + 1;
        LADDER_POSITIONS = Set.copyOf(MultiblockPOIHelper.getPosList(pois, "ladder"));
    }
}
