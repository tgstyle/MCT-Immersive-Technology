package mctmods.immersivetechnology.common.multiblocks.helper;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ITWrappingItemHandler(IItemHandler wrapped, boolean allowInsert, boolean allowExtract, List<ITWrappingItemHandler.IntRange> allowedRanges) implements IItemHandler {

    @Override
    public int getSlots() { return wrapped.getSlots(); }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) { return wrapped.getStackInSlot(slot); }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!allowInsert || !isAccessible(slot)) return stack;
        return wrapped.insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!allowExtract || !isAccessible(slot)) return ItemStack.EMPTY;
        return wrapped.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) { return wrapped.getSlotLimit(slot); }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) { return isAccessible(slot) && wrapped.isItemValid(slot, stack); }

    private boolean isAccessible(int slot) {
        for (final ITWrappingItemHandler.IntRange range : allowedRanges) if (range.contains(slot)) return true;
        return false;
    }

    public record IntRange(int first, int firstAfter) {
        private boolean contains(int slotId) { return slotId >= first && slotId < firstAfter; }
    }
}
