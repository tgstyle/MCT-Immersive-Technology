package mctmods.immersivetechnology.common.multiblocks.gui;

import mctmods.immersivetechnology.common.gui.helper.ContainerMenu;
import mctmods.immersivetechnology.common.gui.helper.GenericContainerData;
import mctmods.immersivetechnology.common.multiblocks.gui.helper.ModSlot;
import com.immersiveconvergence.api.util.ConstrainedItemHandler;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.StyleNOXLogic;

import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class StyleNOXMenu extends ContainerMenu {
    public final StyleNOXLogic.StyleNOXTank tanks;
    public final MutableEnergyStorage energy;
    @Nullable private final IMultiblockContext<StyleNOXLogic.State> mbContext;
    public int rodLevel = 0;
    public int reactorState = 0;
    public int startupProgress = 0;
    public int burnProgress = 0;
    public int burnTotal = 0;

    public static StyleNOXMenu makeServer(MenuType<StyleNOXMenu> type, int id, Inventory invPlayer, ContainerMenu.MultiblockMenuContext<StyleNOXLogic.State> ctx) {
        final StyleNOXLogic.State state = ctx.mbContext().getState();
        return new StyleNOXMenu(ContainerMenu.multiblockCtx(type, id, ctx), invPlayer, state.inventory, state.tanks, state, ctx.mbContext());
    }

    public static StyleNOXMenu makeClient(MenuType<StyleNOXMenu> type, int id, Inventory invPlayer) {
        return new StyleNOXMenu(
                ContainerMenu.clientCtx(type, id),
                invPlayer,
                new ConstrainedItemHandler(List.of(ConstrainedItemHandler.IOConstraint.INPUT, ConstrainedItemHandler.IOConstraint.OUTPUT), () -> {}),
                StyleNOXLogic.StyleNOXTank.makeClient(),
                null,
                null
        );
    }

    protected StyleNOXMenu(MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, StyleNOXLogic.StyleNOXTank tanks, @Nullable StyleNOXLogic.State state, @Nullable IMultiblockContext<StyleNOXLogic.State> mbContext) {
        super(ctx);
        this.tanks = tanks;
        this.mbContext = mbContext;
        this.energy = new MutableEnergyStorage(StyleNOXLogic.energyCapacity());
        this.addSlot(new ModSlot.Input(inv, StyleNOXLogic.SLOT_FUEL, 71, 17));
        this.addSlot(new ModSlot.Output(inv, StyleNOXLogic.SLOT_SPENT, 99, 17));
        ownSlotCount = 2;
        addPlayerInventorySlots(inventoryPlayer);
        addGenericData(GenericContainerData.fluid(tanks.water()));
        addGenericData(GenericContainerData.fluid(tanks.steam()));
        addGenericData(GenericContainerData.fluid(tanks.co2()));
        addGenericData(GenericContainerData.int32(() -> state != null ? state.getEnergy().getEnergyStored() : energy.getEnergyStored(), energy::setStoredEnergy));
        addGenericData(GenericContainerData.int32(() -> state != null ? state.rodLevel : rodLevel, v -> this.rodLevel = v));
        addGenericData(GenericContainerData.int32(() -> state != null ? state.reactorState.ordinal() : reactorState, v -> this.reactorState = v));
        addGenericData(GenericContainerData.int32(() -> state != null ? state.startupProgress : startupProgress, v -> this.startupProgress = v));
        addGenericData(GenericContainerData.int32(() -> state != null ? state.burnProgress : burnProgress, v -> this.burnProgress = v));
        addGenericData(GenericContainerData.int32(() -> state != null ? state.burnTotal : burnTotal, v -> this.burnTotal = v));
    }

    @Override public boolean clickMenuButton(@NotNull Player player, int id) {
        if (mbContext == null || id < 0 || id > StyleNOXLogic.MAX_ROD_LEVEL) { return false; }
        StyleNOXLogic.State state = mbContext.getState();
        state.rodLevel = id;
        if (id == 0) {
            state.reactorState = StyleNOXLogic.ReactorState.IDLE;
            state.startupProgress = 0;
        }
        mbContext.markMasterDirty();
        mbContext.requestMasterBESync();
        return true;
    }
}
