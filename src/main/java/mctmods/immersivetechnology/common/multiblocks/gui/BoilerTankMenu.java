package mctmods.immersivetechnology.common.multiblocks.gui;

import mctmods.immersivetechnology.common.gui.helper.ITContainerMenu;
import mctmods.immersivetechnology.common.gui.helper.ITGenericContainerData;
import mctmods.immersivetechnology.common.gui.helper.ITGenericDataSerializers;
import mctmods.immersivetechnology.common.multiblocks.gui.helper.ITSlot;
import mctmods.immersivetechnology.common.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerTankLogic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

public class BoilerTankMenu extends ITContainerMenu {
    public final BoilerTankLogic.BoilerTanks tanks;
    public float heatLevel = 0.0f;
    public double workingHeatLevel = BoilerTankLogic.DEFAULT_WORKING_HEAT_LEVEL;

    public static BoilerTankMenu makeServer(MenuType<BoilerTankMenu> type, int id, Inventory invPlayer, ITContainerMenu.MultiblockMenuContext<BoilerTankLogic.State> ctx) {
        final BoilerTankLogic.State state = ctx.mbContext().getState();
        return new BoilerTankMenu(ITContainerMenu.multiblockCtx(type, id, ctx), invPlayer, state.inventory, state.tanks, state);
    }

    public static BoilerTankMenu makeClient(MenuType<BoilerTankMenu> type, int id, Inventory invPlayer) {
        return new BoilerTankMenu(
                ITContainerMenu.clientCtx(type, id),
                invPlayer,
                new ITSlotwiseItemHandler(
                        List.of(
                                ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                                ITSlotwiseItemHandler.IOConstraint.OUTPUT,
                                ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                                ITSlotwiseItemHandler.IOConstraint.OUTPUT
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
        this.addSlot(new ITSlot.FluidContainer(inv, BoilerTankLogic.INPUT_SLOT_FILLED, 43, 15, 2));
        this.addSlot(new ITSlot.Output(inv, BoilerTankLogic.INPUT_SLOT_EMPTY, 43, 54));
        this.addSlot(new ITSlot.FluidContainer(inv, BoilerTankLogic.OUTPUT_SLOT_EMPTY, 116, 15, 1));
        this.addSlot(new ITSlot.Output(inv, BoilerTankLogic.OUTPUT_SLOT_FILLED, 116, 54));
        for (int i = 0; i < 3; i++) for (int j = 0; j < 9; j++) { addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18)); }
        for (int i = 0; i < 9; i++) { addSlot(new Slot(inventoryPlayer, i, 8 + i * 18, 142)); }
        addGenericData(ITGenericContainerData.fluid(tanks.input()));
        addGenericData(ITGenericContainerData.fluid(tanks.output()));
        addGenericData(new ITGenericContainerData<>(ITGenericDataSerializers.FLOAT, () -> (state != null ? (float)state.heatLevel : heatLevel), f -> this.heatLevel = f));
        addGenericData(new ITGenericContainerData<>(ITGenericDataSerializers.DOUBLE, () -> (state != null ? state.getWorkingHeatLevel() : workingHeatLevel), d -> this.workingHeatLevel = d));
    }

    public float getHeatLevel() { return heatLevel; }
    public double getWorkingHeatLevel() { return workingHeatLevel; }
}
