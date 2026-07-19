package mctmods.immersivetechnology.common.multiblocks.gui;

import mctmods.immersivetechnology.common.gui.helper.ContainerMenu;
import mctmods.immersivetechnology.common.gui.helper.GenericContainerData;
import mctmods.immersivetechnology.common.multiblocks.gui.helper.ModSlot;
import mctmods.immersivetechnology.common.multiblocks.helper.SlotwiseItemHandler;
import mctmods.immersivetechnology.common.multiblocks.stone.logic.AdvancedCokeOvenLogic;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.AdvancedCokeOvenRecipe;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;

public class AdvancedCokeOvenMenu extends ContainerMenu {
    public final AdvancedCokeOvenLogic.AdvancedCokeOvenTank tanks;
    private int maxProcessTime = 0;
    private int remainingProcessTime = 0;

    public static AdvancedCokeOvenMenu makeServer(MenuType<AdvancedCokeOvenMenu> type, int id, Inventory invPlayer, ContainerMenu.MultiblockMenuContext<AdvancedCokeOvenLogic.State> ctx) {
        final AdvancedCokeOvenLogic.State state = ctx.mbContext().getState();
        return new AdvancedCokeOvenMenu(ContainerMenu.multiblockCtx(type, id, ctx), invPlayer, state.inventory, state.tanks, state);
    }

    public static AdvancedCokeOvenMenu makeClient(MenuType<AdvancedCokeOvenMenu> type, int id, Inventory invPlayer) {
        SlotwiseItemHandler dummy = new SlotwiseItemHandler(
                List.of(
                        SlotwiseItemHandler.IOConstraint.input(i -> AdvancedCokeOvenRecipe.findRecipe(invPlayer.player.level(), i, null) != null),
                        SlotwiseItemHandler.IOConstraint.OUTPUT,
                        SlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                        SlotwiseItemHandler.IOConstraint.OUTPUT
                ),
                () -> {}
        );
        return new AdvancedCokeOvenMenu(ContainerMenu.clientCtx(type, id), invPlayer, dummy, AdvancedCokeOvenLogic.AdvancedCokeOvenTank.makeClient(), null);
    }

    protected AdvancedCokeOvenMenu(ContainerMenu.MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, AdvancedCokeOvenLogic.AdvancedCokeOvenTank tanks, @Nullable AdvancedCokeOvenLogic.State state) {
        super(ctx);
        this.tanks = tanks;
        this.addSlot(new ModSlot.Input(inv, AdvancedCokeOvenLogic.SLOT_INPUT, 30, 35));
        this.addSlot(new ModSlot.Output(inv, AdvancedCokeOvenLogic.SLOT_OUTPUT, 85, 35));
        this.addSlot(new ModSlot.FluidContainer(inv, AdvancedCokeOvenLogic.SLOT_EMPTY_CONTAINER, 152, 17, 0));
        this.addSlot(new ModSlot.Output(inv, AdvancedCokeOvenLogic.SLOT_FILLED_CONTAINER, 152, 53));
        this.ownSlotCount = 4;
        addPlayerInventorySlots(inventoryPlayer);
        addGenericData(GenericContainerData.fluid(tanks.output()));
        addGenericData(GenericContainerData.int32(() -> state != null ? state.get(AdvancedCokeOvenLogic.State.MAX_PROCESS_TIME) : maxProcessTime, i -> this.maxProcessTime = i));
        addGenericData(GenericContainerData.int32(() -> state != null ? state.get(AdvancedCokeOvenLogic.State.REMAINING_PROCESS_TIME) : remainingProcessTime, i -> this.remainingProcessTime = i));
    }

    public int getMaxProcessTime() { return maxProcessTime; }
    public int getRemainingProcessTime() { return remainingProcessTime; }
}
