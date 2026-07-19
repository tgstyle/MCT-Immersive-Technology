package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.common.blocks.helper.BlockInterfaces;
import mctmods.immersivetechnology.common.multiblocks.helper.IDisplayContext;
import mctmods.immersivetechnology.common.multiblocks.helper.MultiblockPOIHelper;
import mctmods.immersivetechnology.common.multiblocks.helper.IPressurizedFluidOutput;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.SteelSheetmetalTankShape;
import mctmods.immersivetechnology.common.fluids.helper.MarkableFluidTank;
import mctmods.immersivetechnology.common.fluids.helper.DelegatingFluidTank;
import mctmods.immersivetechnology.core.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.client.util.ClientUtils;
import mctmods.immersivetechnology.core.util.LayeredComparatorOutput;
import mctmods.immersivetechnology.core.util.Utils;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.interfaces.MBOverlayText;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import static mctmods.immersivetechnology.common.multiblocks.metal.shapes.SteelSheetmetalTankShape.DATA;

public class SteelSheetmetalTankLogic implements IMultiblockLogic<SteelSheetmetalTankLogic.State>, IServerTickableComponent<SteelSheetmetalTankLogic.State>, MBOverlayText<SteelSheetmetalTankLogic.State>, IPressurizedFluidOutput<SteelSheetmetalTankLogic.State>, BlockInterfaces.ILadderPositionProvider {

    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(DATA.pointsOfInterest);
    private static final List<CapabilityPosition> INPUT_POIS = MultiblockPOIHelper.getCapabilityPositions(RAW_POIS, "fluid_input0");
    private static final List<CapabilityPosition> IO_POIS = MultiblockPOIHelper.getCapabilityPositions(RAW_POIS, "fluid_io0");
    private static final BlockPos OUTPUT0_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output0").getFirst();
    private static final BlockPos OUTPUT1_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output1").getFirst();
    private static final BlockPos OUTPUT2_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output2").getFirst();
    private static final RelativeBlockFace OUTPUT0_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output0");
    private static final RelativeBlockFace OUTPUT1_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output1");
    private static final RelativeBlockFace OUTPUT2_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output2");
    private static final BlockPos COMPARATOR_BASE = MultiblockPOIHelper.getPosList(RAW_POIS, "comparator_base0").getFirst();
    private static final List<BlockPos> COMPARATOR_LAYERS;

    static {
        COMPARATOR_LAYERS = RAW_POIS.stream().filter(poi -> poi.name.equals("comparator_layer0")).map(poi -> new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2])).sorted(Comparator.comparingInt(BlockPos::getY)).collect(ImmutableList.toImmutableList());
    }
    private static final Set<BlockPos> LADDER_POSITIONS = Set.copyOf(MultiblockPOIHelper.getPosList(RAW_POIS, "ladder"));

    @Override public boolean isLadderPos(BlockPos posInMB) { return LADDER_POSITIONS.contains(posInMB); }

    @Override public List<BlockPos> getOutputPositions() { return Stream.concat(IO_POIS.stream().map(CapabilityPosition::posInMultiblock), Stream.of(OUTPUT0_POI, OUTPUT1_POI, OUTPUT2_POI)).collect(ImmutableList.toImmutableList()); }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return null; }

    @Override public List<RelativeBlockFace> getOutputFacings() { return Stream.concat(IO_POIS.stream().map(CapabilityPosition::side), Stream.of(OUTPUT0_FACING, OUTPUT1_FACING, OUTPUT2_FACING)).collect(ImmutableList.toImmutableList()); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return Stream.concat(Stream.generate(() -> state.tank).limit(IO_POIS.size()), Stream.of(state.output0Tank, state.output1Tank, state.output2Tank)).collect(ImmutableList.toImmutableList()); }

    @Override public int getTransferSpeed() { return ServerConfig.steelSheetmetalTankTransferSpeed; }

    @Override public boolean shouldPumpOutputs(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        return !state.tank.isEmpty();
    }

    private record ConditionalFluidHandler(MarkableFluidTank tank, boolean canFill, boolean canDrain, Runnable onChange, Supplier<Boolean> allowDrain) implements IFluidHandler {
        @Override public int getTanks() { return 1; }

        @Override @NotNull public FluidStack getFluidInTank(int tank) { return this.tank.getFluid(); }

        @Override public int getTankCapacity(int tank) { return this.tank.getCapacity(); }

        @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return this.tank.isFluidValid(stack); }

        @Override public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
            if (!canFill || resource.isEmpty()) return 0;
            if (action == FluidAction.SIMULATE) return this.tank.fill(resource, FluidAction.SIMULATE);
            int filled = this.tank.fill(resource, FluidAction.EXECUTE);
            if (filled > 0) onChange.run();
            return filled;
        }

        @Override @NotNull public FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) {
            if (!canDrain || !allowDrain.get() || resource.isEmpty()) return FluidStack.EMPTY;
            FluidStack drained = this.tank.drain(resource, action);
            if (!drained.isEmpty() && action == FluidAction.EXECUTE) onChange.run();
            return drained;
        }

        @Override @NotNull public FluidStack drain(int maxDrain, @NotNull FluidAction action) {
            if (!canDrain || !allowDrain.get() || maxDrain <= 0) return FluidStack.EMPTY;
            FluidStack drained = this.tank.drain(maxDrain, action);
            if (!drained.isEmpty() && action == FluidAction.EXECUTE) onChange.run();
            return drained;
        }
    }

    public static class State implements IMultiblockState, IDisplayContext {
        public final MarkableFluidTank tank;
        private final LayeredComparatorOutput<IMultiblockContext<State>> comparatorHelper;
        public IFluidHandler inputHandler;
        public IFluidHandler ioHandler;
        public IFluidHandler output0Handler;
        public IFluidHandler output1Handler;
        public IFluidHandler output2Handler;
        public final MarkableFluidTank output0Tank;
        public final MarkableFluidTank output1Tank;
        public final MarkableFluidTank output2Tank;
        public boolean active = false;

        public State(IInitialMultiblockContext<State> capabilitySource) {
            Runnable changedAndSync = () -> { capabilitySource.getSyncRunnable().run(); capabilitySource.getMarkDirtyRunnable().run(); };
            this.tank = new MarkableFluidTank(ServerConfig.steelSheetmetalTankCapacity, v -> changedAndSync.run());
            this.inputHandler = new ConditionalFluidHandler(tank, true, false, changedAndSync, () -> false);
            this.ioHandler = new ConditionalFluidHandler(tank, true, true, changedAndSync, () -> true);
            this.output0Handler = new ConditionalFluidHandler(tank, false, true, changedAndSync, () -> true);
            this.output1Handler = new ConditionalFluidHandler(tank, false, true, changedAndSync, () -> true);
            this.output2Handler = new ConditionalFluidHandler(tank, false, true, changedAndSync, () -> true);
            this.output0Tank = new DelegatingFluidTank(tank);
            this.output1Tank = new DelegatingFluidTank(tank);
            this.output2Tank = new DelegatingFluidTank(tank);
            this.comparatorHelper = new LayeredComparatorOutput<>(tank.getCapacity(), COMPARATOR_LAYERS.size(),
                    (ctx, value) -> {
                        BlockPos pos = COMPARATOR_BASE;
                        IMultiblockLevel level = ctx.getLevel();
                        ctx.setComparatorOutputFor(pos, value);
                        BlockPos absPos = level.toAbsolute(pos);
                        BlockState stateAt = level.getBlockState(pos);
                        level.getRawLevel().updateNeighborsAt(absPos, stateAt.getBlock());
                    },
                    (ctx, layer, value) -> {
                        BlockPos pos = COMPARATOR_LAYERS.get(layer);
                        IMultiblockLevel level = ctx.getLevel();
                        ctx.setComparatorOutputFor(pos, value);
                        BlockPos absPos = level.toAbsolute(pos);
                        BlockState stateAt = level.getBlockState(pos);
                        level.getRawLevel().updateNeighborsAt(absPos, stateAt.getBlock());
                    }
            );
        }

        @Override public boolean isActive() { return active; }

        @Override public List<AveragingEnergyStorage> getEnergies() { return ImmutableList.of(); }

        @Override public IItemHandlerModifiable getInventory() { return null; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tank}; }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.put("tank", tank.writeToNBT(provider, new CompoundTag()));
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            tank.readFromNBT(provider, nbt.getCompound("tank"));
        }

        @Override public void writeSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display, provider);
            nbt.put("display", display);
        }

        @Override public void readSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display"), provider); }
        }

        public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.put("tank", tank.writeToNBT(provider, new CompoundTag()));
        }

        public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            active = nbt.getBoolean("active");
            tank.readFromNBT(provider, nbt.getCompound("tank"));
        }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
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
            if (side == null) return null;
            if (INPUT_POIS.stream().anyMatch(p -> p.posInMultiblock().equals(posIn) && p.side() == side)) return state.inputHandler;
            if (IO_POIS.stream().anyMatch(p -> p.posInMultiblock().equals(posIn) && p.side() == side)) return state.ioHandler;
            if (posIn.equals(OUTPUT0_POI) && side == OUTPUT0_FACING) return state.output0Handler;
            if (posIn.equals(OUTPUT1_POI) && side == OUTPUT1_FACING) return state.output1Handler;
            if (posIn.equals(OUTPUT2_POI) && side == OUTPUT2_FACING) return state.output2Handler;
            return null;
        });
    }

    @Override public void dropExtraItems(State state, java.util.function.Consumer<net.minecraft.world.item.ItemStack> drop) { }

    @Override @Nullable public List<Component> getOverlayText(State state, BlockPos pos, BlockHitResult hit, Player player, boolean hammer) {
        if (Utils.isFluidRelatedItemStack(player.getItemInHand(InteractionHand.MAIN_HAND))) return List.of(ClientUtils.formatFluidStack(state.tank.getFluid()));
        return null;
    }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType) { return SteelSheetmetalTankShape.GETTER; }

    public ItemInteractionResult click(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) {
        if (FluidUtils.interactWithFluidHandler(player, hand, ctx.getState().tank)) { ctx.markMasterDirty(); ctx.requestMasterBESync(); return ItemInteractionResult.SUCCESS; } else return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
