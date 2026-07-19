package mctmods.immersivetechnology.common.multiblocks.helper;

import mctmods.immersivetechnology.core.util.ITUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class ITSlotwiseItemHandler implements IItemHandlerModifiable, Iterable<ItemStack> {
    private final ItemStackHandler rawHandler;
    private final List<IOConstraint> slotConstraints;

    public ITSlotwiseItemHandler(List<IOConstraint> slotConstraints, Runnable onChanged) {
        this.rawHandler = new ItemStackHandler(slotConstraints.size()) {
            @Override protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                onChanged.run();
            }
        };
        this.slotConstraints = slotConstraints;
    }

    @Override public int getSlots() { return rawHandler.getSlots(); }

    @Override @NotNull public ItemStack getStackInSlot(int slot) { return rawHandler.getStackInSlot(slot); }

    @Override @NotNull public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (slot >= this.slotConstraints.size()) return stack;
        boolean allowInsert = this.slotConstraints.get(slot).allowInsert.test(stack);
        if (!allowInsert) return stack;
        ItemStack current = getStackInSlot(slot);
        if (!current.isEmpty()) {
            if (!ItemStack.isSameItemSameTags(current, stack)) return stack;
        }
        ItemStack result = rawHandler.insertItem(slot, stack, simulate);
        if (!simulate) {
            ItemStack after = getStackInSlot(slot);
            if (after.getCount() > getSlotLimit(slot)) {
                after.setCount(getSlotLimit(slot));
                rawHandler.setStackInSlot(slot, after);
            }
        }
        return result;
    }

    @Override @NotNull public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot >= this.slotConstraints.size() || !this.slotConstraints.get(slot).allowExtract()) return ItemStack.EMPTY;
        return rawHandler.extractItem(slot, amount, simulate);
    }

    @Override public int getSlotLimit(int slot) { return rawHandler.getSlotLimit(slot); }

    @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (slot >= this.slotConstraints.size()) return false;
        return this.slotConstraints.get(slot).allowInsert.test(stack);
    }

    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        ItemStack toSet = stack.copy();
        toSet.setCount(Math.min(toSet.getCount(), getSlotLimit(slot)));
        rawHandler.setStackInSlot(slot, toSet);
    }

    public Tag serializeNBT() { return rawHandler.serializeNBT(); }

    public void deserializeNBT(CompoundTag nbt) { rawHandler.deserializeNBT(nbt); }

    public ItemStackHandler getRawHandler() { return rawHandler; }

    @Nonnull @Override public Iterator<ItemStack> iterator() {
        return new Iterator<>() {
            private int slot = 0;

            @Override public boolean hasNext() { return slot < getSlots(); }

            @Override public ItemStack next() {
                final ItemStack next = getStackInSlot(slot);
                ++slot;
                return next;
            }
        };
    }

    public record IOConstraint(boolean allowExtract, Predicate<ItemStack> allowInsert) {
        public static final IOConstraint INPUT = input($ -> true);
        public static final IOConstraint OUTPUT = new IOConstraint(true, $ -> false);
        public static final IOConstraint FLUID_INPUT = IOConstraint.input(ITUtils::isFluidRelatedItemStack);

        public static IOConstraint input(Predicate<ItemStack> allow) { return new IOConstraint(true, allow); }

        public boolean allowExtract() { return allowExtract; }
    }
}
