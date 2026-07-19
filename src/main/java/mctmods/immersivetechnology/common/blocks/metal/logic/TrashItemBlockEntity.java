package mctmods.immersivetechnology.common.blocks.metal.logic;

import mctmods.immersivetechnology.common.blocks.helper.BlockInterfaces;
import mctmods.immersivetechnology.common.blocks.metal.shapes.ITrashCanShape;
import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.ClientConfig;
import mctmods.immersivetechnology.core.registration.BlockEntities;
import mctmods.immersivetechnology.core.registration.MenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TrashItemBlockEntity extends OSDCommonBlockEntity implements IItemHandlerModifiable, BlockInterfaces.IInteractionObjectIT<TrashItemBlockEntity>, ITrashCanShape, Container {
    public TrashItemBlockEntity(BlockPos pos, BlockState state) { super(BlockEntities.TRASH_ITEM.get(), pos, state); }

    @SuppressWarnings("unused")
    public IItemHandlerModifiable getItemHandler(@Nullable Direction side) {
        return this;
    }

    @Override public int getSlots() { return 1; }

    @Override @NotNull public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }

    @Override @NotNull public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!simulate) { acceptedAmount += stack.getCount(); }
        return ItemStack.EMPTY;
    }

    @Override @NotNull public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }

    @Override public int getSlotLimit(int slot) { return Integer.MAX_VALUE; }

    @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return true; }

    @Override public TrashItemBlockEntity getGuiMaster() { return this; }

    @Override public MenuTypes.ArgContainer<? super TrashItemBlockEntity, ?> getContainerType() { return MenuTypes.TRASH_ITEM; }

    @Override public boolean canUseGui(Player player) { return true; }

    @Override public TranslationKey text() { return ClientConfig.perTickTrashCans ? TranslationKey.OVERLAY_OSD_TRASH_ITEM_NORMAL_ALTERNATIVE : TranslationKey.OVERLAY_OSD_TRASH_ITEM_NORMAL_FIRST_LINE; }

    @Override public int getContainerSize() { return 1; }

    @Override public boolean isEmpty() { return true; }

    @Override @NotNull public ItemStack getItem(int slot) { return ItemStack.EMPTY; }

    @Override @NotNull public ItemStack removeItem(int slot, int amount) { return ItemStack.EMPTY; }

    @Override @NotNull public ItemStack removeItemNoUpdate(int slot) { return ItemStack.EMPTY; }

    @Override public void setItem(int slot, ItemStack stack) {
        if (!stack.isEmpty()) { insertItem(slot, stack, false); }
    }

    @Override public boolean stillValid(@NotNull Player player) { return true; }

    @Override public void clearContent() {}

    @Override public void setStackInSlot(int slot, @NotNull ItemStack stack) { insertItem(slot, stack, false); }
}
