package mctmods.immersivetechnology.common.blocks.metal.logic;

import mctmods.immersivetechnology.common.blocks.helper.ITServerTickableBE;
import mctmods.immersivetechnology.common.blocks.metal.gui.ValveLimiterMenu;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import mctmods.immersivetechnology.core.registration.ITMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import static mctmods.immersivetechnology.common.blocks.metal.ValveLimiterBlock.OPEN;
import static mctmods.immersivetechnology.common.blocks.metal.ValveLimiterBlock.ROTATION;

public class ValveLimiterBlockEntity extends ValveCommonBlockEntity implements ITServerTickableBE, IItemHandler {
    public record OutputItemHandler(ValveLimiterBlockEntity te) implements IItemHandler {
        @Override public int getSlots() { return te.getSource() != null ? te.getSource().getSlots() : 0; }

        @Override public @NotNull ItemStack getStackInSlot(int slot) { return te.getSource() != null ? te.getSource().getStackInSlot(slot) : ItemStack.EMPTY; }

        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) { return stack; }

        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            assert te.level != null;
            if (te.level.isClientSide) return ItemStack.EMPTY;
            if (te.busy) return ItemStack.EMPTY;
            BlockState state = te.getBlockState();
            if (!state.getValue(OPEN)) return ItemStack.EMPTY;
            IItemHandler src = te.getSource();
            if (src == null) return ItemStack.EMPTY;
            int canAccept = amount;
            canAccept = te.timeLimit > 0 ? Math.min(Math.max(te.timeLimit - ValveCommonBlockEntity.longToInt(te.acceptedAmount), 0), canAccept) : canAccept;
            canAccept = te.packetLimit > 0 ? Math.min(canAccept, te.packetLimit) : canAccept;
            if (te.redstoneMode > 0) canAccept = (int) (canAccept * ((te.redstoneMode == 1 ? 15 - te.getRSPower() : te.getRSPower()) / 15.0));
            if (canAccept == 0) return ItemStack.EMPTY;
            te.busy = true;
            ItemStack extracted = src.extractItem(slot, canAccept, simulate);
            te.busy = false;
            if (!simulate && !extracted.isEmpty()) {
                te.acceptedAmount += extracted.getCount();
                te.packets++;
            }
            return extracted;
        }

        @Override public int getSlotLimit(int slot) { return te.getSource() != null ? te.getSource().getSlotLimit(slot) : 0; }

        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return false; }
    }

    public int rotation = 0;

    public ValveLimiterBlockEntity(BlockPos pos, BlockState state) { super(ITBlockEntities.VALVE_LIMITER.get(), pos, state, TranslationKey.OVERLAY_OSD_VALVE_LIMITER_NORMAL_FIRST_LINE, TranslationKey.OVERLAY_OSD_VALVE_LIMITER_SNEAKING_FIRST_LINE, TranslationKey.OVERLAY_OSD_VALVE_LIMITER_SNEAKING_SECOND_LINE, 2); }

    @Override
    public void tickServer() { updateBase(); }

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
        rotation = getBlockState().getValue(ROTATION);
    }

    @Override
    public void onNeighborBlockChange(BlockPos otherPos) {
        super.onNeighborBlockChange(otherPos);
        updateRedstoneState();
    }

    private LazyOptional<IItemHandler> myCapability = null;

    private LazyOptional<IItemHandler> dummyCapability = null;

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, Direction facing) {
        if (facing == null) return super.getCapability(capability, null);
        if (capability == ForgeCapabilities.ITEM_HANDLER && facing.getAxis() == this.facing.getAxis()) {
            if (facing == this.facing) {
                if (myCapability == null || !myCapability.isPresent()) myCapability = LazyOptional.of(() -> this);
                return myCapability.cast();
            } else if (facing == this.facing.getOpposite()) {
                if (dummyCapability == null || !dummyCapability.isPresent()) dummyCapability = LazyOptional.of(() -> new OutputItemHandler(this));
                return dummyCapability.cast();
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override public void invalidateCaps() {
        super.invalidateCaps();
        if (myCapability != null) { myCapability.invalidate(); myCapability = null; }
        if (dummyCapability != null) { dummyCapability.invalidate(); dummyCapability = null; }
    }

    @Override public void setFacing(@NotNull Direction facing) {
        super.setFacing(facing);
        invalidateCaps();
    }

    @Override public int getSlots() {
        IItemHandler dest = getDestination();
        return dest != null ? dest.getSlots() : 0;
    }

    @Override public @NotNull ItemStack getStackInSlot(int slot) {
        IItemHandler dest = getDestination();
        return dest != null ? dest.getStackInSlot(slot) : ItemStack.EMPTY;
    }

    boolean busy = false;

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        assert level != null;
        if (level.isClientSide) return stack;
        if (busy) return stack;
        BlockState state = getBlockState();
        if (!state.getValue(OPEN)) return stack;
        IItemHandler dest = getDestination();
        if (dest == null) return stack;
        int canAccept = stack.getCount();
        canAccept = timeLimit > 0 ? Math.min(Math.max(timeLimit - ValveCommonBlockEntity.longToInt(acceptedAmount), 0), canAccept) : canAccept;
        canAccept = keepSize > 0 ? Math.min(Math.max(keepSize - getInventoryFill(dest, stack), 0), canAccept) : canAccept;
        canAccept = packetLimit > 0 ? Math.min(canAccept, packetLimit) : canAccept;
        if (redstoneMode > 0) canAccept = (int) (canAccept * ((redstoneMode == 1 ? 15 - getRSPower() : getRSPower()) / 15.0));
        if (canAccept == 0) return stack;
        ItemStack insertStack = stack.copy();
        insertStack.setCount(canAccept);
        busy = true;
        ItemStack remaining = dest.insertItem(slot, insertStack, simulate);
        busy = false;
        int inserted = canAccept - remaining.getCount();
        if (!simulate) { acceptedAmount += inserted; packets++; }
        ItemStack toReturn = stack.copy();
        toReturn.setCount(stack.getCount() - inserted);
        return toReturn;
    }

    public static int getInventoryFill(IItemHandler handler, ItemStack toInsert) {
        int toReturn = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stored = handler.getStackInSlot(i);
            if (!stored.isEmpty() && stored.is(toInsert.getItem())) toReturn += stored.getCount();
        }
        return toReturn;
    }

    @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }

    @Override public int getSlotLimit(int slot) {
        IItemHandler dest = getDestination();
        return dest != null ? dest.getSlotLimit(slot) : 0;
    }

    @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        IItemHandler dest = getDestination();
        return dest != null && dest.isItemValid(slot, stack);
    }

    public IItemHandler getDestination() {
        assert level != null;
        BlockPos dstPos = worldPosition.relative(facing.getOpposite());
        BlockEntity dst = level.getBlockEntity(dstPos);
        if (dst != null) {
            LazyOptional<IItemHandler> cap = dst.getCapability(ForgeCapabilities.ITEM_HANDLER, facing);
            return cap.resolve().orElse(null);
        }
        return null;
    }

    public IItemHandler getSource() {
        assert level != null;
        BlockPos srcPos = worldPosition.relative(facing);
        BlockEntity src = level.getBlockEntity(srcPos);
        if (src != null) {
            LazyOptional<IItemHandler> cap = src.getCapability(ForgeCapabilities.ITEM_HANDLER, facing.getOpposite());
            return cap.resolve().orElse(null);
        }
        return null;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) { return ValveLimiterMenu.makeServer(ITMenuTypes.VALVE_LIMITER.getType(), id, inv, this); }

    @Override public @NotNull Component getDisplayName() { return Component.translatable(TranslationKey.GUI_VALVE_LIMITER.location); }

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

    @Override
    public boolean hammerUseSide(@NotNull Direction side, @NotNull Player player, @NotNull InteractionHand hand, @NotNull Vec3 hit) {
        assert level != null;
        if (level.isClientSide) return false;
        boolean counter = player.isShiftKeyDown() != (side == Direction.DOWN);
        Direction oldFacing = facing;
        Direction newFacing = counter ? oldFacing.getCounterClockWise(side.getAxis()) : oldFacing.getClockWise(side.getAxis());
        setFacing(newFacing);
        return true;
    }

    @Override
    public void readCustomNBT(CompoundTag nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        rotation = nbt.getInt("rotation");
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.putInt("rotation", rotation);
    }
}
