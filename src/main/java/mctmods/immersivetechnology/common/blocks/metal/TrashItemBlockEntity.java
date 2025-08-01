package mctmods.immersivetechnology.common.blocks.metal;

import mctmods.immersivetechnology.common.blocks.helper.ITBlockInterfaces;
import mctmods.immersivetechnology.common.blocks.metal.shape.TrashCanShape;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.core.ITClientConfig;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import mctmods.immersivetechnology.core.registration.ITMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TrashItemBlockEntity extends TrashCommonBlockEntity implements IItemHandlerModifiable, ITBlockInterfaces.IInteractionObjectIE<TrashItemBlockEntity>, TrashCanShape, Container {
    public TrashItemBlockEntity(BlockPos pos, BlockState state) { super(ITBlockEntities.TRASH_ITEM.get(), pos, state); }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) { return LazyOptional.of(() -> (T) this); }
        return super.getCapability(capability, facing);
    }

    @Override
    public int getSlots() { return 1; }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (!simulate) { acceptedAmount += stack.getCount(); }
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }

    @Override
    public int getSlotLimit(int slot) { return Integer.MAX_VALUE; }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) { return true; }

    @Override
    public TrashItemBlockEntity getGuiMaster() { return this; }

    @Override
    public ITMenuTypes.ArgContainer<? super TrashItemBlockEntity, ?> getContainerType() { return ITMenuTypes.TRASH_ITEM; }

    @Override
    public boolean canUseGui(Player player) { return true; }

    @Override
    public TranslationKey text() { return ITClientConfig.perTickTrashCans.get() ? TranslationKey.OVERLAY_OSD_TRASH_ITEM_NORMAL_ALTERNATIVE : TranslationKey.OVERLAY_OSD_TRASH_ITEM_NORMAL_FIRST_LINE; }

    @Override
    public int getContainerSize() { return 1; }

    @Override
    public boolean isEmpty() { return true; }

    @Override
    public @NotNull ItemStack getItem(int slot) { return ItemStack.EMPTY; }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) { return ItemStack.EMPTY; }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) { return ItemStack.EMPTY; }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!stack.isEmpty()) { insertItem(slot, stack, false); }
    }

    @Override
    public boolean stillValid(@NotNull Player player) { return true; }

    @Override
    public void clearContent() {}

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) { insertItem(slot, stack, false); }
}
