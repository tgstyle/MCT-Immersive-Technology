package mctmods.immersivetechnology.common.multiblocks.gui;

import mctmods.immersivetechnology.common.gui.helper.ContainerMenu;
import mctmods.immersivetechnology.common.gui.helper.GenericContainerData;
import mctmods.immersivetechnology.common.gui.helper.GenericDataSerializers;
import mctmods.immersivetechnology.common.multiblocks.gui.helper.ModSlot;
import mctmods.immersivetechnology.common.multiblocks.helper.SlotwiseItemHandler;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerSolidLogic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

public class BoilerSolidMenu extends ContainerMenu {
    private float heatLevel = 0.0f;
    private int burnRemaining = 0;
    private int totalBurnTime = 0;
    private double workingHeatLevel = BoilerSolidLogic.DEFAULT_WORKING_HEAT_LEVEL;

    public static BoilerSolidMenu makeServer(MenuType<BoilerSolidMenu> type, int id, Inventory invPlayer, ContainerMenu.MultiblockMenuContext<BoilerSolidLogic.State> ctx) {
        final BoilerSolidLogic.State state = ctx.mbContext().getState();
        return new BoilerSolidMenu(ContainerMenu.multiblockCtx(type, id, ctx), invPlayer, state.inventory, state);
    }

    public static BoilerSolidMenu makeClient(MenuType<BoilerSolidMenu> type, int id, Inventory invPlayer) {
        SlotwiseItemHandler dummy = new SlotwiseItemHandler(List.of(SlotwiseItemHandler.IOConstraint.INPUT), () -> {});
        return new BoilerSolidMenu(ContainerMenu.clientCtx(type, id), invPlayer, dummy, null);
    }

    protected BoilerSolidMenu(ContainerMenu.MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, BoilerSolidLogic.State state) {
        super(ctx);
        this.addSlot(new ModSlot.Fuel(inv, BoilerSolidLogic.INPUT_FUEL_SLOT, 44, 34));
        ownSlotCount=1;
        addPlayerInventorySlots(inventoryPlayer);
        addGenericData(new GenericContainerData<>(GenericDataSerializers.FLOAT, () -> (state != null ? (float)state.heatLevel : heatLevel), f -> this.heatLevel = f));
        addGenericData(GenericContainerData.int32(() -> state != null ? state.burnRemaining : burnRemaining, i -> this.burnRemaining = i));
        addGenericData(GenericContainerData.int32(() -> state != null ? state.totalBurnTime : totalBurnTime, i -> this.totalBurnTime = i));
        addGenericData(new GenericContainerData<>(GenericDataSerializers.DOUBLE, () -> (state != null ? state.getWorkingHeatLevel() : workingHeatLevel), d -> this.workingHeatLevel = d));
    }

    public float getHeatLevel() { return heatLevel; }
    public int getBurnRemaining() { return burnRemaining; }
    public int getTotalBurnTime() { return totalBurnTime; }
    public double getWorkingHeatLevel() { return workingHeatLevel; }
}
