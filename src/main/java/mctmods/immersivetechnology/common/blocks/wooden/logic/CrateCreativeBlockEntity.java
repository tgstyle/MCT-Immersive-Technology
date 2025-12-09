package mctmods.immersivetechnology.common.blocks.wooden.logic;

import mctmods.immersivetechnology.common.blocks.wooden.gui.CrateCreativeMenu;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import mctmods.immersivetechnology.core.registration.ITMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
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
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

public class CrateCreativeBlockEntity extends BlockEntity implements MenuProvider, IItemHandlerModifiable {
    private ItemStack template = ItemStack.EMPTY;
    private final LazyOptional<IItemHandlerModifiable> itemHandler = LazyOptional.of(() -> this);

    public CrateCreativeBlockEntity(BlockPos pos, BlockState state) {
        super(ITBlockEntities.CRATE_CREATIVE.get(), pos, state);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        if (!template.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            template.save(itemTag);
            tag.put("template", itemTag);
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("template")) {
            template = ItemStack.of(tag.getCompound("template"));
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag) {
        super.handleUpdateTag(tag);
        load(tag);
    }

    public void onBEPlaced(ItemStack stack) {
        if (stack.hasTag()) {
            assert stack.getTag() != null;
            if (stack.getTag().contains("template")) {
                template = ItemStack.of(stack.getTag().getCompound("template"));
                setChanged();
            }
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable(TranslationKey.GUI_CRATE_CREATIVE.getLocation());
    }

    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return CrateCreativeMenu.makeServer(ITMenuTypes.CRATE_CREATIVE.getType(), id, inv, this);
    }

    public boolean stillValid(Player player) {
        if (level != null && !level.isClientSide) {
            return !this.isRemoved() && player.distanceToSqr(Vec3.atCenterOf(getBlockPos())) <= 64.0D;
        }
        return false;
    }

    @Override
    public int getSlots() { return 1; }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return slot == 0 ? template.copy() : ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (slot != 0 || stack.isEmpty()) return stack;
        if (simulate) return ItemStack.EMPTY;
        template = stack.copy();
        setChanged();
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != 0 || template.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        ItemStack out = template.copy();
        out.setCount(Math.min(amount, template.getCount()));
        return out;
    }

    @Override
    public int getSlotLimit(int slot) {
        return slot == 0 ? (template.isEmpty() ? 64 : template.getMaxStackSize()) : 0;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return slot == 0 && !stack.isEmpty();
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (slot == 0) {
            template = stack.copy();
            setChanged();
        }
    }
}
