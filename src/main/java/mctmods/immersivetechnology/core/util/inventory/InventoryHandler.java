package mctmods.immersivetechnology.core.util.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.Objects;

public class InventoryHandler implements IItemHandlerModifiable {
    int slots;
    IInventory inv;
    int slotOffset;
    boolean[] canInsert;
    boolean[] canExtract;

    public InventoryHandler(int slots, IInventory inventory, int slotOffset, boolean[] canInsert, boolean[] canExtract) { this.slots = slots; this.inv = inventory; this.slotOffset = slotOffset; this.canInsert = canInsert; this.canExtract = canExtract; }

    public int getSlots() { return this.slots; }

    @Nonnull public ItemStack getStackInSlot(int slot) { return Objects.requireNonNull(this.inv.getInventory()).get(this.slotOffset + slot); }

    @Nonnull public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (this.canInsert[slot] && !stack.isEmpty()) {
            if (!this.inv.isStackValid(this.slotOffset + slot, stack)) { return stack; }
            else {
                int offsetSlot = this.slotOffset + slot;
                ItemStack currentStack = Objects.requireNonNull(this.inv.getInventory()).get(offsetSlot);
                if (currentStack.isEmpty()) {
                    int accepted = Math.min(stack.getMaxStackSize(), this.inv.getSlotLimit(offsetSlot));
                    if (accepted < stack.getCount()) {
                        stack = stack.copy();
                        if (!simulate) { this.inv.getInventory().set(offsetSlot, stack.split(accepted)); this.inv.doGraphicalUpdates(); } else { stack.shrink(accepted); }
                        return stack;
                    } else {
                        if (!simulate) { this.inv.getInventory().set(offsetSlot, stack.copy()); this.inv.doGraphicalUpdates(); }
                        return ItemStack.EMPTY;
                    }
                } else if (!ItemHandlerHelper.canItemStacksStack(stack, currentStack)) { return stack; }
                else {
                    int accepted = Math.min(stack.getMaxStackSize(), this.inv.getSlotLimit(offsetSlot)) - currentStack.getCount();
                    if (accepted < stack.getCount()) {
                        stack = stack.copy();
                        if (!simulate) { ItemStack newStack = stack.split(accepted); newStack.grow(currentStack.getCount()); this.inv.getInventory().set(offsetSlot, newStack); this.inv.doGraphicalUpdates(); } else { stack.shrink(accepted); }
                        return stack;
                    } else {
                        if (!simulate) { ItemStack newStack = stack.copy(); newStack.grow(currentStack.getCount()); this.inv.getInventory().set(offsetSlot, newStack); this.inv.doGraphicalUpdates(); }
                        return ItemStack.EMPTY;
                    }
                }
            }
        } else { return stack; }
    }

    @Nonnull public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (this.canExtract[slot] && amount != 0) {
            int offsetSlot = this.slotOffset + slot;
            ItemStack currentStack = Objects.requireNonNull(this.inv.getInventory()).get(offsetSlot);
            if (currentStack.isEmpty()) { return ItemStack.EMPTY; }
            else {
                int extracted = Math.min(currentStack.getCount(), amount);
                ItemStack copy = currentStack.copy();
                copy.setCount(extracted);
                if (!simulate) {
                    if (extracted < currentStack.getCount()) { currentStack.shrink(extracted); } else { currentStack = ItemStack.EMPTY; }
                    this.inv.getInventory().set(offsetSlot, currentStack);
                    this.inv.doGraphicalUpdates();
                }
                return copy;
            }
        } else { return ItemStack.EMPTY; }
    }

    public int getSlotLimit(int slot) { return 64; }

    public boolean isItemValid(int slot, @Nonnull ItemStack stack) { return this.canInsert[slot] && this.inv.isStackValid(slot, stack); }

    public void setStackInSlot(int slot, @Nonnull ItemStack stack) { Objects.requireNonNull(this.inv.getInventory()).set(this.slotOffset + slot, stack); this.inv.doGraphicalUpdates(); }
}
