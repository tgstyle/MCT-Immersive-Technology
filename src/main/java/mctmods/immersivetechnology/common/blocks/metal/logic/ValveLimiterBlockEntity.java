package mctmods.immersivetechnology.common.blocks.metal.logic;

import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import mctmods.immersivetechnology.common.blocks.helper.ITIServerTickableBE;
import mctmods.immersivetechnology.common.blocks.metal.gui.ValveLimiterMenu;
import mctmods.immersivetechnology.core.util.TranslationKey;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static mctmods.immersivetechnology.common.blocks.metal.ValveLimiterBlock.ROTATION;

public class ValveLimiterBlockEntity extends ValveCommonBlockEntity implements ITIServerTickableBE, IItemHandler {
    public record OutputItemHandler(ValveLimiterBlockEntity be) implements IItemHandler {
        @Override public int getSlots() { return be.getSource() != null ? be.getSource().getSlots() : 0; }

        @Override @NotNull public ItemStack getStackInSlot(int slot) { return be.getSource() != null ? be.getSource().getStackInSlot(slot) : ItemStack.EMPTY; }

        @Override @NotNull public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) { return stack; }

        @Override @NotNull public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (be.level == null || be.level.isClientSide) return ItemStack.EMPTY;
            if (be.busy) return ItemStack.EMPTY;
            BlockState state = be.getBlockState();
            if (!state.getValue(ValveCommonBlockEntity.OPEN)) return ItemStack.EMPTY;
            IItemHandler src = be.getSource();
            if (src == null) return ItemStack.EMPTY;
            int canAccept = amount;
            canAccept = be.timeLimit > 0 ? Math.clamp(be.timeLimit - ValveCommonBlockEntity.longToInt(be.acceptedAmount), 0, canAccept) : canAccept;
            canAccept = be.packetLimit > 0 ? Math.min(canAccept, be.packetLimit) : canAccept;
            if (be.redstoneMode > 0) canAccept = (int) (canAccept * ((be.redstoneMode == 1 ? 15 - be.getRSPower() : be.getRSPower()) / 15.0));
            if (canAccept == 0) return ItemStack.EMPTY;
            be.busy = true;
            ItemStack extracted = src.extractItem(slot, canAccept, simulate);
            be.busy = false;
            if (!simulate && !extracted.isEmpty()) {
                be.acceptedAmount += extracted.getCount();
                be.packets++;
            }
            return extracted;
        }

        @Override public int getSlotLimit(int slot) { return be.getSource() != null ? be.getSource().getSlotLimit(slot) : 0; }

        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return false; }
    }

    public int rotation = 0;

    public ValveLimiterBlockEntity(BlockPos pos, BlockState state) { super(ITBlockEntities.VALVE_LIMITER.get(), pos, state, TranslationKey.OVERLAY_OSD_VALVE_LIMITER_NORMAL_FIRST_LINE, TranslationKey.OVERLAY_OSD_VALVE_LIMITER_SNEAKING_FIRST_LINE, TranslationKey.OVERLAY_OSD_VALVE_LIMITER_SNEAKING_SECOND_LINE, 2); }

    @Override public void tickServer() { updateBase(); }

    @Override public void onLoad() {
        super.onLoad();
        if (level == null || level.isClientSide) return;
        efficientSetChanged();
        for (Direction d : Direction.values()) { level.neighborChanged(worldPosition.relative(d), getBlockState().getBlock(), worldPosition); }
        markContainingBlockForUpdate(null);
        updateRedstoneState();
        rotation = getBlockState().getValue(ROTATION);
    }

    @Override public void onNeighborBlockChange(BlockPos otherPos) {
        super.onNeighborBlockChange(otherPos);
        updateRedstoneState();
    }

    @SuppressWarnings("unused")
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) return null;
        BlockState state = getBlockState();
        Direction blockFacing = state.getValue(ITProperties.FACING_ALL);
        if (side.getAxis() == blockFacing.getAxis()) {
            if (side == blockFacing) {
                return this;
            } else if (side == blockFacing.getOpposite()) {
                return new OutputItemHandler(this);
            }
        }
        return null;
    }

    @Override public void setFacing(@NotNull Direction facing) {
        super.setFacing(facing);
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

    @Override @NotNull public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (level == null || level.isClientSide || busy || stack.isEmpty()) return stack;
        BlockState state = getBlockState();
        if (!state.getValue(ValveCommonBlockEntity.OPEN)) return stack;
        IItemHandler dest = getDestination();
        if (dest == null) return stack;
        int canAccept = stack.getCount();
        canAccept = timeLimit > 0 ? Math.clamp(timeLimit - ValveCommonBlockEntity.longToInt(acceptedAmount), 0, canAccept) : canAccept;
        canAccept = keepSize > 0 ? Math.clamp(keepSize - getInventoryFill(dest, stack), 0, canAccept) : canAccept;
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

    @Override @NotNull public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }

    @Override public int getSlotLimit(int slot) {
        IItemHandler dest = getDestination();
        return dest != null ? dest.getSlotLimit(slot) : 0;
    }

    @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        IItemHandler dest = getDestination();
        return dest != null && dest.isItemValid(slot, stack);
    }

    public IItemHandler getDestination() {
        if (level == null) return null;
        BlockState state = getBlockState();
        Direction blockFacing = state.getValue(ITProperties.FACING_ALL);
        BlockPos dstPos = worldPosition.relative(blockFacing.getOpposite());
        return level.getCapability(Capabilities.ItemHandler.BLOCK, dstPos, blockFacing);
    }

    public IItemHandler getSource() {
        if (level == null) return null;
        BlockState state = getBlockState();
        Direction blockFacing = state.getValue(ITProperties.FACING_ALL);
        BlockPos srcPos = worldPosition.relative(blockFacing);
        return level.getCapability(Capabilities.ItemHandler.BLOCK, srcPos, blockFacing.getOpposite());
    }

    @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) { return ValveLimiterMenu.makeServer(ITMenuTypes.VALVE_LIMITER.getType(), id, inv, this); }

    @Override public @NotNull Component getDisplayName() { return Component.translatable(TranslationKey.GUI_VALVE_LIMITER.location); }

    @Override public void receiveMessageFromServer(CompoundTag nbt) {
        packetLimit = nbt.getInt("packetLimit");
        timeLimit = nbt.getInt("timeLimit");
        keepSize = nbt.getInt("keepSize");
    }

    @Override public void receiveMessageFromClient(CompoundTag nbt) {
        packetLimit = nbt.getInt("packetLimit");
        timeLimit = nbt.getInt("timeLimit");
        keepSize = nbt.getInt("keepSize");
        efficientSetChanged();
    }

    @Override public boolean stillValid(Player player) { return !isRemoved() && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D; }

    @Override public boolean hammerUseSide(@NotNull Direction side, @NotNull Player player, @NotNull InteractionHand hand, @NotNull Vec3 hit) {
        if (level == null || level.isClientSide) return false;
        boolean counter = player.isShiftKeyDown() != (side == Direction.DOWN);
        Direction oldFacing = facing;
        Direction newFacing = counter ? oldFacing.getCounterClockWise(side.getAxis()) : oldFacing.getClockWise(side.getAxis());
        setFacing(newFacing);
        return true;
    }

    @Override public void readCustomNBT(CompoundTag nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        rotation = nbt.getInt("rotation");
    }

    @Override public void writeCustomNBT(CompoundTag nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.putInt("rotation", rotation);
    }
}
