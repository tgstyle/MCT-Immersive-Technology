package mctmods.immersivetechnology.common.blocks.metal.gui;

import com.mojang.datafixers.util.Pair;
import mctmods.immersivetechnology.common.gui.helper.ITContainerMenu;
import mctmods.immersivetechnology.common.gui.helper.ITGenericDataSerializers;
import mctmods.immersivetechnology.common.blocks.metal.logic.TrashItemBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TrashItemMenu extends ITContainerMenu {
    private final IItemHandlerModifiable handler;
    private final TrashItemBlockEntity tile;

    public TrashItemMenu(MenuType<TrashItemMenu> type, int id, Inventory inv, TrashItemBlockEntity tile) {
        super(ITContainerMenu.blockCtx(type, id, tile));
        this.handler = tile;
        this.tile = tile;
        addOwnSlots();
        addPlayerSlots(inv);
    }

    public TrashItemMenu(MenuType<TrashItemMenu> type, int id, Inventory inv) {
        super(ITContainerMenu.clientCtx(type, id));
        this.handler = new DummyHandler();
        this.tile = null;
        addOwnSlots();
        addPlayerSlots(inv);
    }

    public static TrashItemMenu makeServer(MenuType<TrashItemMenu> type, int id, Inventory inv, TrashItemBlockEntity tile) { return new TrashItemMenu(type, id, inv, tile); }

    public static TrashItemMenu makeClient(MenuType<TrashItemMenu> type, int id, Inventory inv) { return new TrashItemMenu(type, id, inv); }

    private void addOwnSlots() { addSlot(new SlotItemHandler(handler, 0, 79, 36)); ownSlotCount=1; }

    private void addPlayerSlots(Inventory inv) {
        for (int y = 0; y < 3; y++) for (int x = 0; x < 9; x++) addSlot(new Slot(inv, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
        for (int x = 0; x < 9; x++) addSlot(new Slot(inv, x, 8 + x * 18, 142));
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
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

    @Override
    public boolean stillValid(@NotNull Player player) { return tile == null || tile.stillValid(player); }

    @Override
    public void receiveSync(List<Pair<Integer, ITGenericDataSerializers.DataPair<?>>> synced) {}

    private static class DummyHandler implements IItemHandlerModifiable {
        @Override
        public int getSlots() { return 1; }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) { return ItemStack.EMPTY; }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }

        @Override
        public int getSlotLimit(int slot) { return Integer.MAX_VALUE; }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) { return true; }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {}
    }
}
