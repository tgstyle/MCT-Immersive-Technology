package mctmods.immersivetechnology.common.multiblocks.gui;

import mctmods.immersivetechnology.common.gui.helper.ContainerMenu;
import mctmods.immersivetechnology.common.gui.helper.GenericContainerData;
import mctmods.immersivetechnology.common.gui.helper.GenericDataSerializers;
import mctmods.immersivetechnology.common.multiblocks.gui.helper.ModSlot;
import mctmods.immersivetechnology.common.multiblocks.helper.SlotwiseItemHandler;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerLiquidLogic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

public class BoilerLiquidMenu extends ContainerMenu {
    public final BoilerLiquidLogic.BoilerTank tanks;
    private float heatLevel = 0.0f;
    private double workingHeatLevel = BoilerLiquidLogic.DEFAULT_WORKING_HEAT_LEVEL;

    public static BoilerLiquidMenu makeServer(MenuType<BoilerLiquidMenu> type, int id, Inventory invPlayer, ContainerMenu.MultiblockMenuContext<BoilerLiquidLogic.State> ctx) {
        final BoilerLiquidLogic.State state = ctx.mbContext().getState();
        return new BoilerLiquidMenu(ContainerMenu.multiblockCtx(type, id, ctx), invPlayer, state.inventory, state.tanks, state);
    }

    public static BoilerLiquidMenu makeClient(MenuType<BoilerLiquidMenu> type, int id, Inventory invPlayer) {
        return new BoilerLiquidMenu(
                ContainerMenu.clientCtx(type, id),
                invPlayer,
                new SlotwiseItemHandler(
                        List.of(
                                SlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                                SlotwiseItemHandler.IOConstraint.OUTPUT
                        ),
                        () -> {}
                ),
                BoilerLiquidLogic.BoilerTank.makeClient(),
                null
        );
    }

    protected BoilerLiquidMenu(MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, BoilerLiquidLogic.BoilerTank tanks, BoilerLiquidLogic.State state) {
        super(ctx);
        this.tanks = tanks;
        this.addSlot(new ModSlot.FluidContainer(inv, BoilerLiquidLogic.INPUT_FUEL_SLOT_FILLED, 26, 17, 2));
        this.addSlot(new ModSlot.Output(inv, BoilerLiquidLogic.INPUT_FUEL_SLOT_EMPTY, 26, 53));
        addPlayerInventorySlots(inventoryPlayer);
        addGenericData(GenericContainerData.fluid(tanks.input1()));
        addGenericData(new GenericContainerData<>(GenericDataSerializers.FLOAT, () -> (state != null ? (float)state.heatLevel : heatLevel), f -> this.heatLevel = f));
        addGenericData(new GenericContainerData<>(GenericDataSerializers.DOUBLE, () -> (state != null ? state.getWorkingHeatLevel() : workingHeatLevel), d -> this.workingHeatLevel = d));
    }

    public float getHeatLevel() { return heatLevel; }
    public double getWorkingHeatLevel() { return workingHeatLevel; }
}
