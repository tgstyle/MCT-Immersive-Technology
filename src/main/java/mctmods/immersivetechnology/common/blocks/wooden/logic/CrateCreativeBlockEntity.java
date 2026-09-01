package mctmods.immersivetechnology.common.blocks.wooden.logic;

import com.immersiveconvergence.api.block.BlockInterfaces;
import mctmods.immersivetechnology.common.blocks.metal.logic.OSDCommonBlockEntity;
import mctmods.immersivetechnology.common.blocks.wooden.gui.CrateCreativeMenu;
import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.registration.BlockEntities;
import mctmods.immersivetechnology.core.registration.MenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.function.Consumer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

public class CrateCreativeBlockEntity extends OSDCommonBlockEntity implements MenuProvider, IItemHandlerModifiable, BlockInterfaces.IBlockEntityDrop, BlockInterfaces.IPlayerInteraction {

    private ItemStack template = ItemStack.EMPTY;
    private final LazyOptional<IItemHandlerModifiable> itemHandler = LazyOptional.of(() -> this);

    public CrateCreativeBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.CRATE_CREATIVE.get(), pos, state);
    }

    @Override public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) { return itemHandler.cast(); }
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override public void writeCustomNBT(@NotNull CompoundTag tag, boolean descPacket) {
        super.writeCustomNBT(tag, descPacket);
        if (!template.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            template.save(itemTag);
            tag.put("template", itemTag);
        }
    }

    @Override public void readCustomNBT(@NotNull CompoundTag tag, boolean descPacket) {
        super.readCustomNBT(tag, descPacket);
        if (tag.contains("template")) { template = ItemStack.of(tag.getCompound("template")); }
    }

    private void setTemplate(ItemStack stack) {
        template = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        setChanged();
        markContainingBlockForUpdate(null);
    }

    @Override public TranslationKey text() { return TranslationKey.OVERLAY_OSD_CREATIVE_CRATE_NORMAL_FIRST_LINE; }

    @Override public Component[] getOverlayText(@NotNull Player player, @NotNull HitResult rtr, boolean hammer) {
        if (rtr.getType() == HitResult.Type.MISS) { return null; }
        requestOverlaySync();
        if (template.isEmpty()) { return new Component[]{Component.translatable(TranslationKey.GUI_EMPTY.getLocation())}; }
        return new Component[]{Component.translatable(text().getLocation(), template.getHoverName(), formattedAmount())};
    }

    @Override public void getBlockEntityDrop(@NotNull LootContext context, @NotNull Consumer<ItemStack> drop) {
        ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
        CompoundTag tag = new CompoundTag();
        writeCustomNBT(tag, false);
        if (!tag.isEmpty()) { stack.setTag(tag); }
        drop.accept(stack);
    }

    @Override public void onBEPlaced(BlockPlaceContext ctx) { onBEPlaced(ctx.getItemInHand()); }

    @Override public boolean interact(@NotNull Direction side, @NotNull Player player, @NotNull InteractionHand hand, @NotNull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        if (player.isShiftKeyDown()) {
            setTemplate(ItemStack.EMPTY);
            return true;
        }
        if (heldItem.isEmpty()) { return false; }
        setTemplate(heldItem);
        return true;
    }

    public void onBEPlaced(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("template")) {
                CompoundTag templateTag = tag.getCompound("template");
                if (!templateTag.isEmpty()) {
                    setTemplate(ItemStack.of(templateTag));
                }
            }
        }
    }

    @Override @NotNull public Component getDisplayName() { return Component.translatable(TranslationKey.GUI_CRATE_CREATIVE.getLocation()); }

    @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) { return CrateCreativeMenu.makeServer(MenuTypes.CRATE_CREATIVE.getType(), id, inv, this); }

    public boolean stillValid(Player player) {
        if (level != null && !level.isClientSide) {
            return !this.isRemoved() && player.distanceToSqr(Vec3.atCenterOf(getBlockPos())) <= 64.0D;
        }
        return false;
    }

    @Override public int getSlots() { return 1; }

    @Override @NotNull public ItemStack getStackInSlot(int slot) { return slot == 0 ? template.copy() : ItemStack.EMPTY; }

    @Override @NotNull public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (slot != 0 || stack.isEmpty()) { return stack; }
        if (simulate) { return ItemStack.EMPTY; }
        setTemplate(stack);
        return ItemStack.EMPTY;
    }

    @Override @NotNull public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != 0 || template.isEmpty() || amount <= 0) { return ItemStack.EMPTY; }
        ItemStack out = template.copy();
        out.setCount(Math.min(amount, template.getMaxStackSize()));
        if (!simulate) { acceptedAmount += out.getCount(); }
        return out;
    }

    @Override public int getSlotLimit(int slot) { return slot == 0 ? (template.isEmpty() ? 64 : template.getMaxStackSize()) : 0; }

    @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return slot == 0 && !stack.isEmpty(); }

    @Override public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (slot == 0) { setTemplate(stack); }
    }
}
