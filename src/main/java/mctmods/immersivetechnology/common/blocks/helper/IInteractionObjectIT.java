package mctmods.immersivetechnology.common.blocks.helper;

import mctmods.immersivetechnology.core.registration.MenuTypes;

import com.google.common.base.Preconditions;
import com.immersiveconvergence.api.block.IMasterMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public interface IInteractionObjectIT<T extends BlockEntity & IInteractionObjectIT<T>> extends IMasterMenuProvider<T> {
    MenuTypes.ArgContainer<? super T, ?> getContainerType();

    @Nonnull default AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory, @NotNull Player playerEntity) {
        T master = getGuiMaster();
        Preconditions.checkNotNull(master);
        MenuTypes.ArgContainer<? super T, ?> type = getContainerType();
        return type.create(id, playerInventory, master);
    }
}
