package mctmods.immersivetechnology.common.multiblocks.gui;

import mctmods.immersivetechnology.common.gui.helper.ContainerMenu;
import mctmods.immersivetechnology.common.gui.helper.GenericContainerData;
import mctmods.immersivetechnology.common.gui.helper.GenericDataSerializers;
import mctmods.immersivetechnology.common.multiblocks.gui.helper.ModSlot;
import mctmods.immersivetechnology.common.multiblocks.helper.SlotwiseItemHandler;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerTankLogic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

public class BoilerTankMenu extends ContainerMenu {
    public final BoilerTankLogic.BoilerTanks tanks;
    public float heatLevel = 0.0f;
    public double workingHeatLevel = BoilerTankLogic.DEFAULT_WORKING_HEAT_LEVEL;

    public static BoilerTankMenu makeServer(MenuType<BoilerTankMenu> type, int id, Inventory invPlayer, ContainerMenu.MultiblockMenuContext<BoilerTankLogic.State> ctx) {
        final BoilerTankLogic.State state = ctx.mbContext().getState();
        return new BoilerTankMenu(ContainerMenu.multiblockCtx(type, id, ctx), invPlayer, state.inventory, state.tanks, state);
    }

    public static BoilerTankMenu makeClient(MenuType<BoilerTankMenu> type, int id, Inventory invPlayer) {
        return new BoilerTankMenu(
                ContainerMenu.clientCtx(type, id),
                invPlayer,
                new SlotwiseItemHandler(
                        List.of(
                                SlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                                SlotwiseItemHandler.IOConstraint.OUTPUT,
                                SlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                                SlotwiseItemHandler.IOConstraint.OUTPUT
                        ),
                        () -> {}
                ),
                BoilerTankLogic.BoilerTanks.makeClient(),
                null
        );
    }

    protected BoilerTankMenu(MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, BoilerTankLogic.BoilerTanks tanks, BoilerTankLogic.State state) {
        super(ctx);
        this.tanks = tanks;
        this.addSlot(new ModSlot.FluidContainer(inv, BoilerTankLogic.INPUT_SLOT_FILLED, 43, 15, 2));
        this.addSlot(new ModSlot.Output(inv, BoilerTankLogic.INPUT_SLOT_EMPTY, 43, 54));
        this.addSlot(new ModSlot.FluidContainer(inv, BoilerTankLogic.OUTPUT_SLOT_EMPTY, 116, 15, 1));
        this.addSlot(new ModSlot.Output(inv, BoilerTankLogic.OUTPUT_SLOT_FILLED, 116, 54));
        addPlayerInventorySlots(inventoryPlayer);
        addGenericData(GenericContainerData.fluid(tanks.input()));
        addGenericData(GenericContainerData.fluid(tanks.output()));
        addGenericData(new GenericContainerData<>(GenericDataSerializers.FLOAT, () -> (state != null ? (float)state.heatLevel : heatLevel), f -> this.heatLevel = f));
        addGenericData(new GenericContainerData<>(GenericDataSerializers.DOUBLE, () -> (state != null ? state.getWorkingHeatLevel() : workingHeatLevel), d -> this.workingHeatLevel = d));
    }

    public float getHeatLevel() { return heatLevel; }
    public double getWorkingHeatLevel() { return workingHeatLevel; }
}
