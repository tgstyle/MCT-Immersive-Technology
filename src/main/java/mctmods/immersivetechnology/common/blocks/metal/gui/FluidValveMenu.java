package mctmods.immersivetechnology.common.blocks.metal.gui;

import mctmods.immersivetechnology.common.blocks.gui.helper.ITContainerMenu;
import mctmods.immersivetechnology.common.blocks.metal.FluidValveBlockEntity;
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
public class FluidValveMenu extends ITContainerMenu {
    public FluidValveBlockEntity tile;

    private int packetLimit;

    private int timeLimit;

    private int keepSize;

    public FluidValveMenu(MenuType<FluidValveMenu> type, int id, Inventory inv, FluidValveBlockEntity tile) {
        super(ITContainerMenu.blockCtx(type, id, tile));
        this.tile = tile;
        addDataSlot(new DataSlot() { public int get() { return tile.packetLimit; } public void set(int v) { packetLimit = v; } });
        addDataSlot(new DataSlot() { public int get() { return tile.timeLimit; } public void set(int v) { timeLimit = v; } });
        addDataSlot(new DataSlot() { public int get() { return tile.keepSize; } public void set(int v) { keepSize = v; } });
    }

    public FluidValveMenu(MenuType<FluidValveMenu> type, int id, Inventory inv, FriendlyByteBuf buffer) {
        super(ITContainerMenu.clientCtx(type, id));
        BlockPos pos = buffer.readBlockPos();
        this.tile = (FluidValveBlockEntity) inv.player.level().getBlockEntity(pos);
        addDataSlot(new DataSlot() { public int get() { return 0; } public void set(int v) { packetLimit = v; } });
        addDataSlot(new DataSlot() { public int get() { return 0; } public void set(int v) { timeLimit = v; } });
        addDataSlot(new DataSlot() { public int get() { return 0; } public void set(int v) { keepSize = v; } });
    }

    public static FluidValveMenu makeServer(MenuType<FluidValveMenu> type, int id, Inventory inv, FluidValveBlockEntity tile) { return new FluidValveMenu(type, id, inv, tile); }

    public static FluidValveMenu makeClient(MenuType<FluidValveMenu> type, int id, Inventory inv, FriendlyByteBuf buffer) { return new FluidValveMenu(type, id, inv, buffer); }

    @Override
    public boolean stillValid(@NotNull Player player) { return tile == null || tile.stillValid(player); }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        if (pIndex < 0 || pIndex >= slots.size()) { return ItemStack.EMPTY; }
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot.hasItem()) {
            ItemStack itemStack1 = slot.getItem();
            itemstack = itemStack1.copy();
            if (pIndex < 1) {
                if (!this.moveItemStackTo(itemStack1, 1, 37, true)) return ItemStack.EMPTY;
            }
            else {
                if (!this.moveItemStackTo(itemStack1, 0, 1, false)) return ItemStack.EMPTY;
            }
            if (itemStack1.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }

    public int getPacketLimit() { return packetLimit; }

    public int getTimeLimit() { return timeLimit; }

    public int getKeepSize() { return keepSize; }
}
