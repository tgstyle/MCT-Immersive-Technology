package mctmods.immersivetechnology.common.gui;

import mctmods.immersivetechnology.common.blocks.metal.logic.ValveLoadBlockEntity;

import com.immersiveconvergence.api.gui.BaseContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nonnull;

@SuppressWarnings("unused") public class LoadControllerMenu extends BaseContainerMenu {
    public ValveLoadBlockEntity tile;
    private int packetLimit;
    private int timeLimit;
    private int keepSize;

    public LoadControllerMenu(MenuType<LoadControllerMenu> type, int id, Inventory inv, ValveLoadBlockEntity tile) {
        super(BaseContainerMenu.blockCtx(type, id, tile));
        this.tile = tile;
        addDataSlot(new DataSlot() { public int get() { return tile.packetLimit; } public void set(int v) { packetLimit = v; } });
        addDataSlot(new DataSlot() { public int get() { return tile.timeLimit; } public void set(int v) { timeLimit = v; } });
        addDataSlot(new DataSlot() { public int get() { return tile.keepSize; } public void set(int v) { keepSize = v; } });
    }

    public LoadControllerMenu(MenuType<LoadControllerMenu> type, int id, Inventory inv, FriendlyByteBuf buffer) {
        super(BaseContainerMenu.clientCtx(type, id));
        BlockPos pos = buffer.readBlockPos();
        this.tile = (ValveLoadBlockEntity) inv.player.level().getBlockEntity(pos);
        addDataSlot(new DataSlot() { public int get() { return 0; } public void set(int v) { packetLimit = v; } });
        addDataSlot(new DataSlot() { public int get() { return 0; } public void set(int v) { timeLimit = v; } });
        addDataSlot(new DataSlot() { public int get() { return 0; } public void set(int v) { keepSize = v; } });
    }

    public static LoadControllerMenu makeServer(MenuType<LoadControllerMenu> type, int id, Inventory inv, ValveLoadBlockEntity tile) { return new LoadControllerMenu(type, id, inv, tile); }

    public static LoadControllerMenu makeClient(MenuType<LoadControllerMenu> type, int id, Inventory inv, FriendlyByteBuf buffer) { return new LoadControllerMenu(type, id, inv, buffer); }

    @Override public boolean stillValid(@Nonnull Player player) { return tile == null || tile.stillValid(player); }

    @Override @Nonnull public ItemStack quickMoveStack(@Nonnull Player pPlayer, int pIndex) { return ItemStack.EMPTY; }

    public int getPacketLimit() { return packetLimit; }

    public int getTimeLimit() { return timeLimit; }

    public int getKeepSize() { return keepSize; }
}
