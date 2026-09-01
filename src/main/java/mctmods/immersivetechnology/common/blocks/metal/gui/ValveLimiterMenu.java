package mctmods.immersivetechnology.common.blocks.metal.gui;

import mctmods.immersivetechnology.common.gui.helper.ContainerMenu;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveLimiterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class ValveLimiterMenu extends ContainerMenu {
    public ValveLimiterBlockEntity tile;

    private int packetLimit;

    private int timeLimit;

    private int keepSize;

    public ValveLimiterMenu(MenuType<ValveLimiterMenu> type, int id, Inventory inv, ValveLimiterBlockEntity tile) {
        super(ContainerMenu.blockCtx(type, id, tile));
        this.tile = tile;
        addDataSlot(new DataSlot() { public int get() { return tile.packetLimit; } public void set(int v) { packetLimit = v; } });
        addDataSlot(new DataSlot() { public int get() { return tile.timeLimit; } public void set(int v) { timeLimit = v; } });
        addDataSlot(new DataSlot() { public int get() { return tile.keepSize; } public void set(int v) { keepSize = v; } });
    }

    public ValveLimiterMenu(MenuType<ValveLimiterMenu> type, int id, Inventory inv, FriendlyByteBuf buffer) {
        super(ContainerMenu.clientCtx(type, id));
        BlockPos pos = buffer.readBlockPos();
        this.tile = (ValveLimiterBlockEntity) inv.player.level().getBlockEntity(pos);
        addDataSlot(new DataSlot() { public int get() { return 0; } public void set(int v) { packetLimit = v; } });
        addDataSlot(new DataSlot() { public int get() { return 0; } public void set(int v) { timeLimit = v; } });
        addDataSlot(new DataSlot() { public int get() { return 0; } public void set(int v) { keepSize = v; } });
    }

    public static ValveLimiterMenu makeServer(MenuType<ValveLimiterMenu> type, int id, Inventory inv, ValveLimiterBlockEntity tile) { return new ValveLimiterMenu(type, id, inv, tile); }

    public static ValveLimiterMenu makeClient(MenuType<ValveLimiterMenu> type, int id, Inventory inv, FriendlyByteBuf buffer) { return new ValveLimiterMenu(type, id, inv, buffer); }

    @Override public boolean stillValid(@Nonnull Player player) { return tile == null || tile.stillValid(player); }

    @Override @Nonnull public ItemStack quickMoveStack(@Nonnull Player pPlayer, int pIndex) { return ItemStack.EMPTY; }

    public int getPacketLimit() { return packetLimit; }

    public int getTimeLimit() { return timeLimit; }

    public int getKeepSize() { return keepSize; }
}
