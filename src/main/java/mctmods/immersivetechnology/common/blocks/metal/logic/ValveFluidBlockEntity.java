package mctmods.immersivetechnology.common.blocks.metal.logic;

import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeBlockEntity;
import mctmods.immersivetechnology.common.blocks.helper.ITServerTickableBE;
import mctmods.immersivetechnology.common.blocks.metal.gui.ValveFluidMenu;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import mctmods.immersivetechnology.core.registration.ITMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static mctmods.immersivetechnology.common.blocks.metal.ValveFluidBlock.OPEN;

public class ValveFluidBlockEntity extends ValveCommonBlockEntity implements IFluidHandler, IFluidPipe, ITServerTickableBE {
    public static class DummyTank implements IFluidHandler {
        @Override public int getTanks() { return 1; }
        @Override public @NotNull FluidStack getFluidInTank(int tank) { return FluidStack.EMPTY; }
        @Override public int getTankCapacity(int tank) { return 0; }
        @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return false; }
        @Override public int fill(FluidStack fluidStack, FluidAction b) { return 0; }
        @Override public @NotNull FluidStack drain(FluidStack fluidStack, FluidAction b) { return FluidStack.EMPTY; }
        @Override public @NotNull FluidStack drain(int i, FluidAction b) { return FluidStack.EMPTY; }
    }

    public ValveFluidBlockEntity(BlockPos pos, BlockState state) { super(ITBlockEntities.VALVE_FLUID.get(), pos, state, TranslationKey.OVERLAY_OSD_VALVE_FLUID_NORMAL_FIRST_LINE, TranslationKey.OVERLAY_OSD_VALVE_FLUID_SNEAKING_FIRST_LINE, TranslationKey.OVERLAY_OSD_VALVE_FLUID_SNEAKING_SECOND_LINE, 0); }

    @Override
    public void tickServer() { updateBase(); }

    @Override
    public boolean canTickAny() { return true; }

    @Override
    public void onLoad() {
        super.onLoad();
        assert level != null;
        if (!level.isClientSide) {
            efficientSetChanged();
            for (Direction d : Direction.values()) { level.neighborChanged(worldPosition.relative(d), getBlockState().getBlock(), worldPosition); }
            markContainingBlockForUpdate(null);
            updateRedstoneState();
        }
    }

    @Override
    public void onNeighborBlockChange(BlockPos otherPos) {
        super.onNeighborBlockChange(otherPos);
        updateRedstoneState();
    }

    @Override public boolean canOutputPressurized(boolean consumePower) { return true; }

    @SuppressWarnings("unused")
    public boolean hasOutputConnection(Direction side) { return side == facing.getOpposite(); }

    private final LazyOptional<IFluidHandler> myCapability = LazyOptional.of(() -> this);

    private final LazyOptional<IFluidHandler> dummyCapability = LazyOptional.of(DummyTank::new);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, Direction facing) {
        if (facing == null) return super.getCapability(capability, null);
        if (capability == ForgeCapabilities.FLUID_HANDLER && facing.getAxis() == this.facing.getAxis()) {
            if (facing == this.facing) return myCapability.cast();
            else if (facing == this.facing.getOpposite()) return dummyCapability.cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); myCapability.invalidate(); dummyCapability.invalidate(); }

    @Override public int getTanks() { return 1; }

    @Override public @NotNull FluidStack getFluidInTank(int tank) { return FluidStack.EMPTY; }

    @Override public int getTankCapacity(int tank) { return Integer.MAX_VALUE; }

    @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return true; }

    boolean busy = false;

    @Override
    public int fill(FluidStack fluidStack, FluidAction doFill) {
        assert level != null;
        if (level.isClientSide) return 0;
        if (busy) return 0;
        BlockState state = getBlockState();
        if (!state.getValue(OPEN)) return 0;
        IFluidHandler destination = getDestination();
        if (destination == null) return 0;
        int canAccept = fluidStack.getAmount();
        canAccept = timeLimit > 0 ? Math.min(Math.max(timeLimit - ValveCommonBlockEntity.longToInt(acceptedAmount), 0), canAccept) : canAccept;
        canAccept = keepSize > 0 ? Math.min(Math.max(keepSize - getTankFill(destination, fluidStack), 0), canAccept) : canAccept;
        canAccept = packetLimit > 0 ? Math.min(canAccept, packetLimit) : canAccept;
        if (redstoneMode > 0) canAccept = (int) (canAccept * ((redstoneMode == 1 ? 15 - getRSPower() : getRSPower()) / 15.0));
        if (canAccept == 0) return 0;
        assert level != null;
        BlockEntity dst = level.getBlockEntity(worldPosition.relative(facing.getOpposite()));
        boolean isPipe = dst instanceof FluidPipeBlockEntity;
        FluidStack fillStack = new FluidStack(fluidStack.getFluid(), canAccept, fluidStack.getTag());
        boolean hadTag = fillStack.hasTag() && fillStack.getTag().contains(IFluidPipe.NBT_PRESSURIZED);
        if (isPipe && !hadTag) { fillStack.getOrCreateTag().putBoolean(IFluidPipe.NBT_PRESSURIZED, true); }
        busy = true;
        int toReturn = destination.fill(fillStack, doFill);
        busy = false;
        if (!hadTag && fillStack.hasTag()) { fillStack.getTag().remove(IFluidPipe.NBT_PRESSURIZED); }
        if (doFill == FluidAction.EXECUTE) { acceptedAmount += toReturn; packets++; }
        return toReturn;
    }

    public static int getTankFill(IFluidHandler handler, FluidStack toFill) {
        int toReturn = 0;
        for (int i = 0; i < handler.getTanks(); i++) {
            FluidStack stored = handler.getFluidInTank(i);
            if (!stored.isEmpty() && stored.isFluidEqual(toFill)) toReturn += stored.getAmount();
        }
        return toReturn;
    }

    @Override public @NotNull FluidStack drain(FluidStack fluidStack, FluidAction b) { return FluidStack.EMPTY; }

    @Override public @NotNull FluidStack drain(int i, FluidAction b) { return FluidStack.EMPTY; }

    public IFluidHandler getDestination() {
        assert level != null;
        BlockPos dstPos = worldPosition.relative(facing.getOpposite());
        BlockEntity dst = level.getBlockEntity(dstPos);
        if (dst != null) {
            LazyOptional<IFluidHandler> cap = dst.getCapability(ForgeCapabilities.FLUID_HANDLER, facing);
            Optional<IFluidHandler> resolved = cap.resolve();
            return resolved.orElse(null);
        }
        return null;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) { return ValveFluidMenu.makeServer(ITMenuTypes.VALVE_FLUID.getType(), id, inv, this); }

    @Override public @NotNull Component getDisplayName() { return Component.translatable(TranslationKey.GUI_VALVE_FLUID.location); }

    @Override
    public void receiveMessageFromServer(CompoundTag nbt) {
        packetLimit = nbt.getInt("packetLimit");
        timeLimit = nbt.getInt("timeLimit");
        keepSize = nbt.getInt("keepSize");
    }

    @Override
    public void receiveMessageFromClient(CompoundTag nbt) {
        packetLimit = nbt.getInt("packetLimit");
        timeLimit = nbt.getInt("timeLimit");
        keepSize = nbt.getInt("keepSize");
        efficientSetChanged();
    }

    @Override public boolean stillValid(Player player) { return !isRemoved() && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D; }
}
