package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.client.utils.TextUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.interfaces.MBOverlayText;
import blusunrize.immersiveengineering.common.util.LayeredComparatorOutput;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.common.multiblocks.helper.ITDisplayContext;
import mctmods.immersivetechnology.common.multiblocks.helper.ITPressurizedFluidOutput;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.SteelSheetmetalTankShape;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static mctmods.immersivetechnology.common.multiblocks.metal.shapes.SteelSheetmetalTankShape.DATA;

public class SteelSheetmetalTankLogic implements IServerTickableComponent<SteelSheetmetalTankLogic.State>, MBOverlayText<SteelSheetmetalTankLogic.State>, ITPressurizedFluidOutput<SteelSheetmetalTankLogic.State> {
    private static final int CAPACITY = 2048 * FluidType.BUCKET_VOLUME;
    private static final int TRANSFER_SPEED = FluidType.BUCKET_VOLUME;
    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(DATA.pointsOfInterest);
    public static final BlockPos REDSTONE_POI = getPosList().get(0);
    private static final List<CapabilityPosition> INPUT_POIS = getCapabilityPositions("fluid_input");
    private static final List<CapabilityPosition> IO_POIS = getCapabilityPositions("fluid_io");
    private static final BlockPos COMPARATOR_BASE = getPosList("comparator_base").get(0);
    private static final List<BlockPos> COMPARATOR_LAYERS;

    static {
        COMPARATOR_LAYERS = RAW_POIS.stream().filter(poi -> poi.name.equals("comparator_layer")).map(poi -> new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2])).sorted(Comparator.comparingInt(BlockPos::getY)).collect(ImmutableList.toImmutableList());
    }

    private static List<BlockPos> getPosList() { return getPosList("redstone"); }

    private static List<BlockPos> getPosList(String name) { return RAW_POIS.stream().filter(poi -> poi.name.equals(name)).map(poi -> new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2])).collect(ImmutableList.toImmutableList()); }

    private static List<CapabilityPosition> getCapabilityPositions(String name) {
        List<CapabilityPosition> result = new ArrayList<>();
        for (PoIJSONSchema poi : RAW_POIS) {
            if (poi.name.equals(name)) {
                BlockPos connPos = new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2]);
                for (RelativeBlockFace face : poi.relativeFaces) {
                    result.add(new CapabilityPosition(connPos, face));
                }
            }
        }
        if (result.isEmpty()) throw new RuntimeException("No POI found for " + name);
        return ImmutableList.copyOf(result);
    }

    @Override public List<BlockPos> getOutputPositions() { return IO_POIS.stream().map(CapabilityPosition::posInMultiblock).collect(ImmutableList.toImmutableList()); }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return null; }

    @Override public List<RelativeBlockFace> getOutputFacings() { return IO_POIS.stream().map(CapabilityPosition::side).collect(ImmutableList.toImmutableList()); }

    @Override public List<ITMarkableFluidTank> getOutputTanks(State state) { return Stream.generate(() -> state.tank).limit(IO_POIS.size()).collect(ImmutableList.toImmutableList()); }

    @Override public List<CapabilityReference<IFluidHandler>> getFluidOutputs(State state) { return state.outputs; }

    @Override public int getTransferSpeed() { return TRANSFER_SPEED; }

    @Override public boolean shouldPumpOutputs(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        return state.rsState.isEnabled(ctx) && !state.tank.isEmpty();
    }

    private record ConditionalFluidHandler(ITMarkableFluidTank tank, boolean canFill, boolean canDrain, Runnable onChange, Supplier<Boolean> allowDrain) implements IFluidHandler {
        @Override public int getTanks() { return 1; }

        @Override @NotNull public FluidStack getFluidInTank(int tank) { return this.tank.getFluid(); }

        @Override public int getTankCapacity(int tank) { return this.tank.getCapacity(); }

        @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return this.tank.isFluidValid(stack); }

        @Override public int fill(FluidStack resource, FluidAction action) {
            if (!canFill || resource.isEmpty()) return 0;
            if (action == FluidAction.SIMULATE) return this.tank.fill(resource, FluidAction.SIMULATE);
            int filled = this.tank.fill(resource, FluidAction.EXECUTE);
            if (filled > 0) onChange.run();
            return filled;
        }

        @Override @NotNull public FluidStack drain(FluidStack resource, FluidAction action) {
            if (!canDrain || !allowDrain.get() || resource.isEmpty()) return FluidStack.EMPTY;
            FluidStack drained = this.tank.drain(resource, action);
            if (!drained.isEmpty() && action == FluidAction.EXECUTE) onChange.run();
            return drained;
        }

        @Override @NotNull public FluidStack drain(int maxDrain, FluidAction action) {
            if (!canDrain || !allowDrain.get() || maxDrain <= 0) return FluidStack.EMPTY;
            FluidStack drained = this.tank.drain(maxDrain, action);
            if (!drained.isEmpty() && action == FluidAction.EXECUTE) onChange.run();
            return drained;
        }
    }

    public static class State implements IMultiblockState, ITDisplayContext {
        public final ITMarkableFluidTank tank;
        private final LayeredComparatorOutput<IMultiblockContext<State>> comparatorHelper;
        private final List<CapabilityReference<IFluidHandler>> outputs;
        private final StoredCapability<IFluidHandler> inputHandler;
        private final StoredCapability<IFluidHandler> ioHandler;
        public RSState rsState = RSState.disabledByDefault();
        public boolean active = false;

        public State(IInitialMultiblockContext<State> capabilitySource) {
            ImmutableList.Builder<CapabilityReference<IFluidHandler>> outputBuilder = ImmutableList.builder();
            for (CapabilityPosition p : IO_POIS) {
                MultiblockFace mbf = new MultiblockFace(p.side(), p.posInMultiblock());
                CapabilityPosition opp = CapabilityPosition.opposing(mbf);
                outputBuilder.add(capabilitySource.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, opp.posInMultiblock(), opp.side()));
            }
            this.outputs = outputBuilder.build();
            Runnable changedAndSync = () -> { capabilitySource.getSyncRunnable().run(); capabilitySource.getMarkDirtyRunnable().run(); };
            this.tank = new ITMarkableFluidTank(CAPACITY, v -> changedAndSync.run());
            this.inputHandler = new StoredCapability<>(new ConditionalFluidHandler(tank, true, false, changedAndSync, () -> false));
            this.ioHandler = new StoredCapability<>(new ConditionalFluidHandler(tank, true, true, changedAndSync, () -> true));
            try {
                Field positionsField = RSState.class.getDeclaredField("positions");
                positionsField.setAccessible(true);
                positionsField.set(rsState, ImmutableList.of(REDSTONE_POI));
            } catch (Exception e) {
                throw new RuntimeException("Failed to set RSState positions", e);
            }
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

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.put("tank", tank.writeToNBT(new CompoundTag()));
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            tank.readFromNBT(nbt.getCompound("tank"));
        }

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

        @Override public void writeSyncNBT(CompoundTag nbt) { writeSaveNBT(nbt); nbt.putBoolean("active", active); }

        @Override public void readSyncNBT(CompoundTag nbt) { readSaveNBT(nbt); active = nbt.getBoolean("active"); }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        state.comparatorHelper.update(ctx, state.tank.getFluidAmount());
        boolean enabled = state.rsState.isEnabled(ctx);
        boolean isActive = enabled && !state.tank.isEmpty();
        if (state.active != isActive) { state.active = isActive; ctx.markDirtyAndSync(); }
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

    @Override @Nullable public List<Component> getOverlayText(State state, Player player, boolean hammer) {
        if (Utils.isFluidRelatedItemStack(player.getItemInHand(InteractionHand.MAIN_HAND))) return List.of(TextUtils.formatFluidStack(state.tank.getFluid()));
        return null;
    }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType) { return SteelSheetmetalTankShape.GETTER; }

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
                    ctx.markDirtyAndSync();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to invert RSState", e);
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (FluidUtils.interactWithFluidHandler(player, hand, ctx.getState().tank)) { ctx.markDirtyAndSync(); return InteractionResult.SUCCESS; } else return InteractionResult.PASS;
    }
}
