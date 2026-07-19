package mctmods.immersivetechnology.common.multiblocks.gui;

import mctmods.immersivetechnology.common.gui.helper.ITContainerMenu;
import mctmods.immersivetechnology.common.gui.helper.ITGenericContainerData;
import mctmods.immersivetechnology.common.multiblocks.gui.helper.ITSlot;
import mctmods.immersivetechnology.common.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.multiblocks.stone.logic.AdvancedCokeOvenLogic;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.AdvancedCokeOvenRecipe;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;

public class AdvancedCokeOvenMenu extends ITContainerMenu {
    public final AdvancedCokeOvenLogic.AdvancedCokeOvenTank tanks;
    private int maxProcessTime = 0;
    private int remainingProcessTime = 0;

    public static AdvancedCokeOvenMenu makeServer(MenuType<AdvancedCokeOvenMenu> type, int id, Inventory invPlayer, ITContainerMenu.MultiblockMenuContext<AdvancedCokeOvenLogic.State> ctx) {
        final AdvancedCokeOvenLogic.State state = ctx.mbContext().getState();
        return new AdvancedCokeOvenMenu(ITContainerMenu.multiblockCtx(type, id, ctx), invPlayer, state.inventory, state.tanks, state);
    }

    public static AdvancedCokeOvenMenu makeClient(MenuType<AdvancedCokeOvenMenu> type, int id, Inventory invPlayer) {
        ITSlotwiseItemHandler dummy = new ITSlotwiseItemHandler(
                List.of(
                        ITSlotwiseItemHandler.IOConstraint.input(i -> AdvancedCokeOvenRecipe.findRecipe(invPlayer.player.level(), i, null) != null),
                        ITSlotwiseItemHandler.IOConstraint.OUTPUT,
                        ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                        ITSlotwiseItemHandler.IOConstraint.OUTPUT
                ),
                () -> {}
        );
        return new AdvancedCokeOvenMenu(ITContainerMenu.clientCtx(type, id), invPlayer, dummy, AdvancedCokeOvenLogic.AdvancedCokeOvenTank.makeClient(), null);
    }

    protected AdvancedCokeOvenMenu(ITContainerMenu.MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, AdvancedCokeOvenLogic.AdvancedCokeOvenTank tanks, @Nullable AdvancedCokeOvenLogic.State state) {
        super(ctx);
        this.tanks = tanks;
        this.addSlot(new ITSlot.Input(inv, AdvancedCokeOvenLogic.SLOT_INPUT, 30, 35));
        this.addSlot(new ITSlot.Output(inv, AdvancedCokeOvenLogic.SLOT_OUTPUT, 85, 35));
        this.addSlot(new ITSlot.FluidContainer(inv, AdvancedCokeOvenLogic.SLOT_EMPTY_CONTAINER, 152, 17, 0));
        this.addSlot(new ITSlot.Output(inv, AdvancedCokeOvenLogic.SLOT_FILLED_CONTAINER, 152, 53));
        this.ownSlotCount = 4;
        addPlayerInventorySlots(inventoryPlayer);
        addGenericData(ITGenericContainerData.fluid(tanks.output()));
        addGenericData(ITGenericContainerData.int32(() -> state != null ? state.get(AdvancedCokeOvenLogic.State.MAX_PROCESS_TIME) : maxProcessTime, i -> this.maxProcessTime = i));
        addGenericData(ITGenericContainerData.int32(() -> state != null ? state.get(AdvancedCokeOvenLogic.State.REMAINING_PROCESS_TIME) : remainingProcessTime, i -> this.remainingProcessTime = i));
    }

    public int getMaxProcessTime() { return maxProcessTime; }
    public int getRemainingProcessTime() { return remainingProcessTime; }
}
