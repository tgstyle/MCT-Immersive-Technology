package mctmods.immersivetechnology.common.blocks.wooden.logic;

import com.immersiveconvergence.api.block.BlockInterfaces;
import mctmods.immersivetechnology.common.blocks.metal.logic.OSDCommonBlockEntity;
import mctmods.immersivetechnology.common.blocks.wooden.gui.CrateCreativeMenu;
import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.registration.BlockEntities;
import mctmods.immersivetechnology.core.registration.MenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
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
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CrateCreativeBlockEntity extends OSDCommonBlockEntity implements MenuProvider, IItemHandlerModifiable, BlockInterfaces.IBlockEntityDrop, BlockInterfaces.IPlayerInteraction {

    private ItemStack template = ItemStack.EMPTY;

    public CrateCreativeBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.CRATE_CREATIVE.get(), pos, state);
    }

    private HolderLookup.Provider registries() { return level != null ? level.registryAccess() : HolderLookup.Provider.create(Stream.empty()); }

    @Override public void writeCustomNBT(@Nonnull CompoundTag tag, boolean descPacket) {
        super.writeCustomNBT(tag, descPacket);
        if (!template.isEmpty()) { tag.put("template", template.save(registries())); }
    }

    @Override protected void loadAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("template")) { template = ItemStack.parse(registries, tag.getCompound("template")).orElse(ItemStack.EMPTY); }
    }

    @Override public void readCustomNBT(@Nonnull CompoundTag tag, boolean descPacket) {
        super.readCustomNBT(tag, descPacket);
        if (tag.contains("template")) { template = ItemStack.parse(registries(), tag.getCompound("template")).orElse(ItemStack.EMPTY); }
    }

    private void setTemplate(ItemStack stack) {
        template = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        setChanged();
        markContainingBlockForUpdate(null);
        if (level != null) { level.invalidateCapabilities(worldPosition); }
    }

    @Override public TranslationKey text() { return TranslationKey.OVERLAY_OSD_CREATIVE_CRATE_NORMAL_FIRST_LINE; }

    @Override public Component[] getOverlayText(@Nonnull Player player, @Nonnull HitResult rtr, boolean hammer) {
        if (rtr.getType() == HitResult.Type.MISS) { return null; }
        if (template.isEmpty()) { return new Component[]{Component.translatable(TranslationKey.GUI_EMPTY.getLocation())}; }
        return new Component[]{Component.translatable(text().getLocation(), template.getHoverName(), formattedAmount())};
    }

    @Override public void getBlockEntityDrop(@Nonnull LootContext context, @Nonnull Consumer<ItemStack> drop) {
        ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
        CompoundTag tag = new CompoundTag();
        writeCustomNBT(tag, false);
        if (!tag.isEmpty()) { stack.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag)); }
        drop.accept(stack);
    }

    @Override public void onBEPlaced(BlockPlaceContext ctx) { onBEPlaced(ctx.getItemInHand()); }

    @Override public boolean interact(@Nonnull Direction side, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        if (player.isShiftKeyDown()) {
            setTemplate(ItemStack.EMPTY);
            return true;
        }
        if (heldItem.isEmpty()) { return false; }
        setTemplate(heldItem);
        return true;
    }

    public void onBEPlaced(ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_DATA)) {
            CompoundTag tag = Objects.requireNonNull(stack.get(DataComponents.CUSTOM_DATA)).copyTag();
            if (tag.contains("template")) {
                CompoundTag templateTag = tag.getCompound("template");
                if (!templateTag.isEmpty()) {
                    setTemplate(ItemStack.parse(registries(), templateTag).orElse(ItemStack.EMPTY));
                    if (level != null) {
                        level.invalidateCapabilities(worldPosition);
                    }
                }
            }
        }
    }

    @Override
    @Nonnull
    public Component getDisplayName() {
        return Component.translatable(TranslationKey.GUI_CRATE_CREATIVE.getLocation());
    }

    @Override
    public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inv, @Nonnull Player player) {
        return CrateCreativeMenu.makeServer(MenuTypes.CRATE_CREATIVE.getType(), id, inv, this);
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
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        return slot == 0 ? template.copy() : ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (slot != 0 || stack.isEmpty()) { return stack; }
        if (simulate) { return ItemStack.EMPTY; }
        setTemplate(stack);
        return ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != 0 || template.isEmpty() || amount <= 0) { return ItemStack.EMPTY; }
        ItemStack out = template.copy();
        out.setCount(Math.min(amount, template.getMaxStackSize()));
        return out;
    }

    @Override
    public int getSlotLimit(int slot) {
        return slot == 0 ? (template.isEmpty() ? 64 : template.getMaxStackSize()) : 0;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return slot == 0 && !stack.isEmpty();
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (slot == 0) { setTemplate(stack); }
    }
}
