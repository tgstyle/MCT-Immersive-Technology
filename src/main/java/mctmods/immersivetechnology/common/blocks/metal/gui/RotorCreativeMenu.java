package mctmods.immersivetechnology.common.blocks.metal.gui;

import mctmods.immersivetechnology.common.blocks.metal.logic.RotorCreativeBlockEntity;
import mctmods.immersivetechnology.common.gui.helper.ContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class RotorCreativeMenu extends ContainerMenu {
    public RotorCreativeBlockEntity tile;
    private int rpm;

    public RotorCreativeMenu(MenuType<?> type, int id, Inventory inv, RotorCreativeBlockEntity tile) {
        super(ContainerMenu.blockCtx(type, id, tile));
        this.tile = tile;
        addDataSlot(new DataSlot() { public int get() { return tile.rpm; } public void set(int v) { rpm = v; } });
    }

    public RotorCreativeMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf buffer) {
        super(ContainerMenu.clientCtx(type, id));
        BlockPos pos = buffer.readBlockPos();
        this.tile = (RotorCreativeBlockEntity) inv.player.level().getBlockEntity(pos);
        addDataSlot(new DataSlot() { public int get() { return 0; } public void set(int v) { rpm = v; } });
    }

    public static RotorCreativeMenu makeServer(MenuType<RotorCreativeMenu> type, int id, Inventory inv, RotorCreativeBlockEntity tile) { return new RotorCreativeMenu(type, id, inv, tile); }

    public static RotorCreativeMenu makeClient(MenuType<RotorCreativeMenu> type, int id, Inventory inv, FriendlyByteBuf buffer) { return new RotorCreativeMenu(type, id, inv, buffer); }

    @Override public boolean stillValid(@NotNull Player player) { return tile.stillValid(player); }

    @Override @NotNull public ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        if (pIndex < 0 || pIndex >= slots.size()) { return ItemStack.EMPTY; }
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot.hasItem()) {
            ItemStack itemStack1 = slot.getItem();
            itemstack = itemStack1.copy();
            if (pIndex < 1) {
                if (!this.moveItemStackTo(itemStack1, 1, 37, true)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(itemStack1, 0, 1, false)) return ItemStack.EMPTY;
            }
            if (itemStack1.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }

    public int getRpm() { return rpm; }
}
