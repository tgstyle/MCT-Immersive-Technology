package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.multiblock.IDisplayContext;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.multiblock.IFluidOutputPump;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.util.TranslationKey;
import com.immersiveconvergence.api.util.MarkableFluidTank;
import mctmods.immersivetechnology.core.ServerConfig;
import com.immersiveconvergence.api.util.LayeredComparator;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.interfaces.MBOverlayText;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.immersiveconvergence.api.util.ICFluidUtils;
import com.immersiveconvergence.api.multiblock.ShapeData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SteelSheetmetalTankLogic implements IMultiblockLogic<SteelSheetmetalTankLogic.State>, IServerTickableComponent<SteelSheetmetalTankLogic.State>, MBOverlayText<SteelSheetmetalTankLogic.State>, IFluidOutputPump<SteelSheetmetalTankLogic.State> {
    private static final ShapeData SHAPE = ITShapes.get("steel_sheetmetal_tank");

    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(ITShapes.data("steel_sheetmetal_tank").pointsOfInterest);
    public static final BlockPos REDSTONE_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "redstone0").get(0);
    private static final List<CapabilityPosition> INPUT_POIS = MultiblockPOIHelper.getCapabilityPositions(RAW_POIS, "fluid_input0");
    private static final List<CapabilityPosition> IO_POIS = MultiblockPOIHelper.getCapabilityPositions(RAW_POIS, "fluid_io0");
    private static final List<BlockPos> COMPARATOR_BASE = MultiblockPOIHelper.getPosList(RAW_POIS, "comparator_base0");
    private static final List<List<BlockPos>> COMPARATOR_LAYERS;

    static {
        COMPARATOR_LAYERS = MultiblockPOIHelper.getPosList(RAW_POIS, "comparator_layer0").stream().collect(Collectors.groupingBy(BlockPos::getY, TreeMap::new, ImmutableList.toImmutableList())).values().stream().collect(ImmutableList.toImmutableList());
    }
    public static final int COMPARATOR_LAYER_COUNT = COMPARATOR_LAYERS.size();

    @Override public List<BlockPos> getOutputPositions() { return IO_POIS.stream().map(CapabilityPosition::posInMultiblock).collect(ImmutableList.toImmutableList()); }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return null; }

    @Override public List<RelativeBlockFace> getOutputFacings() { return IO_POIS.stream().map(CapabilityPosition::side).collect(ImmutableList.toImmutableList()); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return Stream.generate(() -> state.tank).limit(IO_POIS.size()).collect(ImmutableList.toImmutableList()); }

    @Override public int getTransferSpeed() { return ServerConfig.steelSheetmetalTankTransferSpeed; }

    @Override public boolean shouldPumpOutputs(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        return state.rsState.isEnabled(ctx) && !state.tank.isEmpty();
    }

    private record ConditionalFluidHandler(MarkableFluidTank tank, boolean canFill, boolean canDrain, Runnable onChange, Supplier<Boolean> allowDrain) implements IFluidHandler {
        @Override public int getTanks() { return 1; }

        @Override @Nonnull public FluidStack getFluidInTank(int tank) { return this.tank.getFluid(); }

        @Override public int getTankCapacity(int tank) { return this.tank.getCapacity(); }

        @Override public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return this.tank.isFluidValid(stack); }

        @Override public int fill(FluidStack resource, FluidAction action) {
            if (!canFill || resource.isEmpty()) return 0;
            if (action == FluidAction.SIMULATE) return this.tank.fill(resource, FluidAction.SIMULATE);
            int filled = this.tank.fill(resource, FluidAction.EXECUTE);
            if (filled > 0) onChange.run();
            return filled;
        }

        @Override @Nonnull public FluidStack drain(FluidStack resource, FluidAction action) {
            if (!canDrain || !allowDrain.get() || resource.isEmpty()) return FluidStack.EMPTY;
            FluidStack drained = this.tank.drain(resource, action);
            if (!drained.isEmpty() && action == FluidAction.EXECUTE) onChange.run();
            return drained;
        }

        @Override @Nonnull public FluidStack drain(int maxDrain, FluidAction action) {
            if (!canDrain || !allowDrain.get() || maxDrain <= 0) return FluidStack.EMPTY;
            FluidStack drained = this.tank.drain(maxDrain, action);
            if (!drained.isEmpty() && action == FluidAction.EXECUTE) onChange.run();
            return drained;
        }
    }

    public static class State implements IMultiblockState, IDisplayContext {
        public final MarkableFluidTank tank;
        private final LayeredComparator<IMultiblockContext<State>> comparatorHelper;
        private final StoredCapability<IFluidHandler> inputHandler;
        private final StoredCapability<IFluidHandler> ioHandler;
        public RSState rsState = RSState.disabledByDefault();
        public boolean active = false;

        public State(IInitialMultiblockContext<State> capabilitySource) {
            Runnable changedAndSync = () -> { capabilitySource.getSyncRunnable().run(); capabilitySource.getMarkDirtyRunnable().run(); };
            this.tank = new MarkableFluidTank(ServerConfig.steelSheetmetalTankCapacity, v -> changedAndSync.run());
            this.inputHandler = new StoredCapability<>(new ConditionalFluidHandler(tank, true, false, changedAndSync, () -> false));
            this.ioHandler = new StoredCapability<>(new ConditionalFluidHandler(tank, true, true, changedAndSync, () -> true));
            try {
                Field positionsField = RSState.class.getDeclaredField("positions");
                positionsField.setAccessible(true);
                positionsField.set(rsState, ImmutableList.of(REDSTONE_POI));
            } catch (Exception e) {
                throw new RuntimeException("Failed to set RSState positions", e);
            }
            this.comparatorHelper = new LayeredComparator<>(tank.getCapacity(), COMPARATOR_LAYERS.size(),
                    (ctx, value) -> { for (BlockPos pos : COMPARATOR_BASE) { ctx.setComparatorOutputFor(pos, value); } },
                    (ctx, layer, value) -> { for (BlockPos pos : COMPARATOR_LAYERS.get(layer)) { ctx.setComparatorOutputFor(pos, value); } }
            );
        }

        @Override public boolean isActive() { return active; }

        @Override public List<AveragingEnergyStorage> getEnergies() { return ImmutableList.of(); }

        @Override public IItemHandlerModifiable getInventory() { return null; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tank}; }

        @Override public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("tank", tank.writeToNBT(new CompoundTag()));
            CompoundTag rsTag = new CompoundTag();
            rsState.writeSaveNBT(rsTag);
            nbt.put("rsState", rsTag);
        }

        @Override public void readSaveNBT(CompoundTag nbt) {
            tank.readFromNBT(nbt.getCompound("tank"));
            rsState.readSaveNBT(nbt.getCompound("rsState"));
        }

        @Override public void writeSyncNBT(CompoundTag nbt) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display);
            nbt.put("display", display);
        }

        @Override public void readSyncNBT(CompoundTag nbt) {
            if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display")); }
        }

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
        final State state = ctx.getState();
        state.comparatorHelper.update(ctx, state.tank.getFluidAmount());
        boolean enabled = state.rsState.isEnabled(ctx);
        boolean isActive = enabled && !state.tank.isEmpty();
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
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            BlockPos posIn = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (side != null) {
                if (INPUT_POIS.stream().anyMatch(p -> p.posInMultiblock().equals(posIn) && p.side() == side)) return state.inputHandler.cast(ctx);
                if (IO_POIS.stream().anyMatch(p -> p.posInMultiblock().equals(posIn) && p.side() == side)) return state.ioHandler.cast(ctx);
            }
        }
        return LazyOptional.empty();
    }

    @Override public void dropExtraItems(State state, java.util.function.Consumer<net.minecraft.world.item.ItemStack> drop) { }

    @Override @Nullable public List<Component> getOverlayText(State state, Player player, boolean hammer) {
        if (ICFluidUtils.isFluidRelatedItemStack(player.getItemInHand(InteractionHand.MAIN_HAND))) return List.of(ICFluidUtils.formatFluidStack(state.tank.getFluid()));
        return null;
    }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType) { return SHAPE.getter; }

    @Override public InteractionResult click(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) {
        if (posInMultiblock.equals(REDSTONE_POI) && player.getItemInHand(hand).is(IETags.screwdrivers)) {
            if (!isClient) {
                State state = ctx.getState();
                try {
                    Field f = RSState.class.getDeclaredField("rsEnablesMachine");
                    f.setAccessible(true);
                    boolean current = (boolean) f.get(state.rsState);
                    boolean inverted = !current;
                    f.set(state.rsState, inverted);
                    player.displayClientMessage(Component.translatable(inverted ? TranslationKey.CHAT_RS_CONTROL_INVERTED_OFF.getLocation() : TranslationKey.CHAT_RS_CONTROL_INVERTED_ON.getLocation()), true);
                    ctx.markMasterDirty();
                    ctx.requestMasterBESync();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to invert RSState", e);
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (FluidUtils.interactWithFluidHandler(player, hand, ctx.getState().tank)) { ctx.markMasterDirty(); ctx.requestMasterBESync(); return InteractionResult.SUCCESS; } else return InteractionResult.PASS;
    }
}
