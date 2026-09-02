package mctmods.immersivetechnology.common.blocks.connectors.gui;

import mctmods.immersivetechnology.common.blocks.connectors.logic.ConnectorTimerBlockEntity;
import com.immersiveconvergence.api.gui.BaseContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ConnectorTimerMenu extends BaseContainerMenu {
    public ConnectorTimerBlockEntity tile;

    public ConnectorTimerMenu(MenuType<ConnectorTimerMenu> type, int id, Inventory _inv, ConnectorTimerBlockEntity tile) {
        super(blockCtx(type, id, tile));
        Objects.requireNonNull(_inv);
        this.tile = tile;
    }

    @SuppressWarnings("resource")
    public ConnectorTimerMenu(MenuType<ConnectorTimerMenu> type, int id, Inventory inv, FriendlyByteBuf buffer) {
        super(clientCtx(type, id));
        BlockPos pos = buffer.readBlockPos();
        Level level = inv.player.level();
        this.tile = (ConnectorTimerBlockEntity) level.getBlockEntity(pos);
    }

    public static ConnectorTimerMenu makeServer(MenuType<ConnectorTimerMenu> type, int id, Inventory inv, ConnectorTimerBlockEntity tile) { return new ConnectorTimerMenu(type, id, inv, tile); }

    public static ConnectorTimerMenu makeClient(MenuType<ConnectorTimerMenu> type, int id, Inventory inv, FriendlyByteBuf buffer) { return new ConnectorTimerMenu(type, id, inv, buffer); }

    public int getTarget() { return tile != null ? tile.getTarget() : 40; }

    @Override public boolean stillValid(@Nonnull Player player) { return tile == null || tile.stillValid(player); }

    @Override @Nonnull public ItemStack quickMoveStack(@Nonnull Player player, int index) { return ItemStack.EMPTY; }
}
