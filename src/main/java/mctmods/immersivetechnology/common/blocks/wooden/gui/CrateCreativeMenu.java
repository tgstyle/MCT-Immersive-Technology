package mctmods.immersivetechnology.common.blocks.wooden.gui;

import com.mojang.datafixers.util.Pair;
import mctmods.immersivetechnology.common.gui.helper.ContainerMenu;
import mctmods.immersivetechnology.common.gui.helper.GenericContainerData;
import mctmods.immersivetechnology.common.blocks.wooden.logic.CrateCreativeBlockEntity;
import mctmods.immersivetechnology.common.gui.helper.GenericDataSerializers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class CrateCreativeMenu extends ContainerMenu {
    private final IItemHandlerModifiable handler;
    private final CrateCreativeBlockEntity tile;

    public CrateCreativeMenu(MenuType<CrateCreativeMenu> type, int id, Inventory inv, CrateCreativeBlockEntity tile) {
        super(ContainerMenu.blockCtx(type, id, tile));
        this.handler = tile;
        this.tile = tile;
        addGenericData(GenericContainerData.itemStack(() -> handler.getStackInSlot(0), stack -> handler.setStackInSlot(0, stack)));
        addOwnSlots();
        addPlayerSlots(inv);
    }

    public CrateCreativeMenu(MenuType<CrateCreativeMenu> type, int id, Inventory inv) {
        super(ContainerMenu.clientCtx(type, id));
        this.handler = new DummyHandler();
        this.tile = null;
        addGenericData(GenericContainerData.itemStack(() -> handler.getStackInSlot(0), stack -> handler.setStackInSlot(0, stack)));
        addOwnSlots();
        addPlayerSlots(inv);
    }

    public static CrateCreativeMenu makeServer(MenuType<CrateCreativeMenu> type, int id, Inventory inv, CrateCreativeBlockEntity tile) { return new CrateCreativeMenu(type, id, inv, tile); }

    public static CrateCreativeMenu makeClient(MenuType<CrateCreativeMenu> type, int id, Inventory inv) { return new CrateCreativeMenu(type, id, inv); }

    private void addOwnSlots() { addSlot(new SlotItemHandler(handler, 0, 79, 36)); ownSlotCount=1; }

    private void addPlayerSlots(Inventory inv) {
        for (int y = 0; y < 3; y++) for (int x = 0; x < 9; x++) { addSlot(new Slot(inv, x + y * 9 + 9, 8 + x * 18, 84 + y * 18)); }
        for (int x = 0; x < 9; x++) { addSlot(new Slot(inv, x, 8 + x * 18, 142)); }
    }

    @Override @Nonnull public ItemStack quickMoveStack(@Nonnull Player pPlayer, int pIndex) {
        Slot slot = this.slots.get(pIndex);
        if (!slot.hasItem()) { return ItemStack.EMPTY; }
        ItemStack itemStack1 = slot.getItem();
        if (pIndex != 0) { handler.setStackInSlot(0, itemStack1.copy()); slot.set(ItemStack.EMPTY); return itemStack1; }
        return ItemStack.EMPTY;
    }

    @Override public boolean stillValid(@Nonnull Player player) { return tile == null || tile.stillValid(player); }

    @Override public void receiveSync(List<Pair<Integer, GenericDataSerializers.DataPair<?>>> synced) { super.receiveSync(synced); }

    @Override public void clicked(int slotId, int button, @Nonnull ClickType clickType, @Nonnull Player player) {
        if (slotId == 0) {
            ItemStack carried = getCarried();
            if (clickType == ClickType.PICKUP) {
                if (!carried.isEmpty()) { super.clicked(slotId, button, clickType, player); }
                else if (!handler.getStackInSlot(0).isEmpty()) {
                    int amount = (button == 0) ? handler.getStackInSlot(0).getMaxStackSize() : 1;
                    ItemStack extracted = handler.extractItem(0, amount, false);
                    setCarried(extracted);
                }
            }
            else if (clickType == ClickType.QUICK_MOVE) {
                if (carried.isEmpty()) {
                    ItemStack temp = handler.getStackInSlot(0);
                    if (!temp.isEmpty()) {
                        int amount = temp.getMaxStackSize();
                        ItemStack extracted = handler.extractItem(0, amount, false);
                        moveItemStackTo(extracted, 1, slots.size(), true);
                        if (!extracted.isEmpty()) { player.drop(extracted, false); }
                    }
                }
            }
            else if (clickType == ClickType.CLONE) {
                if (player.getAbilities().instabuild && carried.isEmpty()) {
                    ItemStack temp = handler.getStackInSlot(0);
                    if (!temp.isEmpty()) {
                        ItemStack cloned = temp.copy();
                        cloned.setCount(cloned.getMaxStackSize());
                        setCarried(cloned);
                    }
                }
            }
            else { super.clicked(slotId, button, clickType, player); }
        }
        else { super.clicked(slotId, button, clickType, player); }
    }

    private static class DummyHandler implements IItemHandlerModifiable {
        private ItemStack template = ItemStack.EMPTY;

        @Override public int getSlots() { return 1; }

        @Override public @Nonnull ItemStack getStackInSlot(int slot) { return slot == 0 ? template.copy() : ItemStack.EMPTY; }

        @Override public @Nonnull ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (slot != 0 || stack.isEmpty()) { return stack; }
            if (simulate) { return ItemStack.EMPTY; }
            template = stack.copy();
            return ItemStack.EMPTY;
        }

        @Override public @Nonnull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 0 || template.isEmpty() || amount <= 0) { return ItemStack.EMPTY; }
            ItemStack out = template.copy();
            out.setCount(Math.min(amount, template.getMaxStackSize()));
            return out;
        }

        @Override public int getSlotLimit(int slot) { return slot == 0 ? (template.isEmpty() ? 64 : template.getMaxStackSize()) : 0; }

        @Override public boolean isItemValid(int slot, @Nonnull ItemStack stack) { return slot == 0 && !stack.isEmpty(); }

        @Override public void setStackInSlot(int slot, @Nonnull ItemStack stack) { if (slot == 0) { template = stack.copy(); } }
    }
}
