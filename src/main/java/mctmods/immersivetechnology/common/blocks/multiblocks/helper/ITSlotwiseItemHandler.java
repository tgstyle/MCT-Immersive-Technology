package mctmods.immersivetechnology.common.blocks.multiblocks.helper;

import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class ITSlotwiseItemHandler implements IItemHandlerModifiable, Iterable<ItemStack> {
    private final ItemStackHandler rawHandler;
    private final List<mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraint> slotConstraints;

    public static mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler makeWithGroups(Runnable onChanged, mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraintGroup... constraintGroups) {
        return makeWithGroups(Arrays.asList(constraintGroups), onChanged);
    }

    public static mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler onSlotRange(mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraint constraint, int min, int count, Runnable onChanged) {
        return mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.makeWithGroups(onChanged, new mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraintGroup(mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraint.BLOCKED, min), new mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraintGroup(mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraint.ANY_INPUT, count));
    }

    public static mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler makeWithGroups(List<mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraintGroup> constraintGroups, Runnable onChanged) {
        List<mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraint> slotConstraints = new ArrayList<>();
        for (final mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraintGroup group : constraintGroups)
            for (int i = 0; i < group.slotCount; ++i)
                slotConstraints.add(group.constraint);
        return new mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler(slotConstraints, onChanged);
    }

    public ITSlotwiseItemHandler(List<mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraint> slotConstraints, Runnable onChanged) {
        this.rawHandler = new ItemStackHandler(slotConstraints.size()) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                onChanged.run();
            }
        };
        this.slotConstraints = slotConstraints;
    }

    @Override
    public int getSlots() {
        return rawHandler.getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return rawHandler.getStackInSlot(slot);
    }

    @Override
    @NotNull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (slot >= this.slotConstraints.size() || !this.slotConstraints.get(slot).allowInsert.test(stack)) return stack;
        return rawHandler.insertItem(slot, stack, simulate);
    }

    @Override
    @NotNull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot >= this.slotConstraints.size() || !this.slotConstraints.get(slot).allowExtract()) return ItemStack.EMPTY;
        return rawHandler.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return rawHandler.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        // TODO may not be entirely correct
        return rawHandler.isItemValid(slot, stack);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        rawHandler.setStackInSlot(slot, stack);
    }

    public Tag serializeNBT() {
        return rawHandler.serializeNBT();
    }

    public void deserializeNBT(CompoundTag nbt) {
        rawHandler.deserializeNBT(nbt);
    }

    public ItemStackHandler getRawHandler() {
        return rawHandler;
    }

    @Nonnull
    @Override
    public Iterator<ItemStack> iterator() {
        return new Iterator<>() {
            private int slot = 0;

            @Override
            public boolean hasNext() {
                return slot < getSlots();
            }

            @Override
            public ItemStack next() {
                final ItemStack next = getStackInSlot(slot);
                ++slot;
                return next;
            }
        };
    }

    public record IOConstraint(boolean allowExtract, Predicate<ItemStack> allowInsert) {
        public static final mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraint OUTPUT = new mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraint(true, $ -> false);
        public static final mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraint ANY_INPUT = new mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraint(false, $ -> true);
        public static final mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraint FLUID_INPUT = mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraint.input(Utils::isFluidRelatedItemStack);
        public static final mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraint NO_CONSTRAINT = new mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraint(true, $ -> true);
        public static final mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraint BLOCKED = new mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraint(false, $ -> false);

        public static mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraint input(Predicate<ItemStack> allow) {
            return new mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraint(false, allow);
        }
    }

    public record IOConstraintGroup(mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler.IOConstraint constraint, int slotCount) {}
}
