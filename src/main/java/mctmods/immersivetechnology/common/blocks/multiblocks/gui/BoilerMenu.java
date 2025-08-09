package mctmods.immersivetechnology.common.blocks.multiblocks.gui;

import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import mctmods.immersivetechnology.common.blocks.gui.helper.ITContainerMenu;
import mctmods.immersivetechnology.common.blocks.multiblocks.gui.helper.ITSlot;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.BoilerLogic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.items.IItemHandler;
import java.util.List;

public class BoilerMenu extends ITContainerMenu {
    public final BoilerLogic.BoilerTank tanks;
    private float heatLevel = 0.0f;

    public static BoilerMenu makeServer(MenuType<BoilerMenu> type, int id, Inventory invPlayer, ITContainerMenu.MultiblockMenuContext<BoilerLogic.State> ctx) {
        final BoilerLogic.State state = ctx.mbContext().getState();
        return new BoilerMenu(ITContainerMenu.multiblockCtx(type, id, ctx), invPlayer, state.getInventory(), state.getTanks(), state);
    }

    public static BoilerMenu makeClient(MenuType<BoilerMenu> type, int id, Inventory invPlayer) {
        return new BoilerMenu(
                ITContainerMenu.clientCtx(type, id),
                invPlayer,
                new ITSlotwiseItemHandler(
                        List.of(
                                ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                                ITSlotwiseItemHandler.IOConstraint.OUTPUT,
                                ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                                ITSlotwiseItemHandler.IOConstraint.OUTPUT,
                                ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                                ITSlotwiseItemHandler.IOConstraint.OUTPUT
                        ),
                        () -> {}
                ),
                BoilerLogic.BoilerTank.makeClient(),
                null
        );
    }

    protected BoilerMenu(MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, BoilerLogic.BoilerTank tanks, BoilerLogic.State state) {
        super(ctx);
        this.tanks = tanks;
        this.addSlot(new ITSlot.FluidContainer(inv, BoilerLogic.INPUT_FUEL_SLOT_FILLED, 37, 15, 2));
        this.addSlot(new ITSlot.Output(inv, BoilerLogic.INPUT_FUEL_SLOT_EMPTY, 37, 54));
        this.addSlot(new ITSlot.FluidContainer(inv, BoilerLogic.INPUT_SLOT_FILLED, 76, 15, 2));
        this.addSlot(new ITSlot.Output(inv, BoilerLogic.INPUT_SLOT_EMPTY, 76, 54));
        this.addSlot(new ITSlot.FluidContainer(inv, BoilerLogic.OUTPUT_SLOT_EMPTY, 149, 15, 1));
        this.addSlot(new ITSlot.Output(inv, BoilerLogic.OUTPUT_SLOT_FILLED, 149, 54));
        for(int i = 0; i < 3; i++) { for(int j = 0; j < 9; j++) { addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18)); } }
        for(int i = 0; i < 9; i++) { addSlot(new Slot(inventoryPlayer, i, 8+i*18, 142)); }
        addGenericData(GenericContainerData.fluid(tanks.input1()));
        addGenericData(GenericContainerData.fluid(tanks.input2()));
        addGenericData(GenericContainerData.fluid(tanks.output()));
        addGenericData(new GenericContainerData<>(GenericDataSerializers.FLOAT, () -> (state != null ? (float)state.heatLevel : heatLevel), f -> this.heatLevel = f));
    }

    public float getHeatLevel() { return heatLevel; }
}
