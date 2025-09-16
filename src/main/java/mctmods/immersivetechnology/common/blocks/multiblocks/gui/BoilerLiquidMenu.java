package mctmods.immersivetechnology.common.blocks.multiblocks.gui;

import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import mctmods.immersivetechnology.common.blocks.gui.helper.ITContainerMenu;
import mctmods.immersivetechnology.common.blocks.multiblocks.gui.helper.ITSlot;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.BoilerLiquidLogic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class BoilerLiquidMenu extends ITContainerMenu {
    public final BoilerLiquidLogic.BoilerTank tanks;
    private float heatLevel = 0.0f;

    public static BoilerLiquidMenu makeServer(MenuType<BoilerLiquidMenu> type, int id, Inventory invPlayer, ITContainerMenu.MultiblockMenuContext<BoilerLiquidLogic.State> ctx) {
        final BoilerLiquidLogic.State state = ctx.mbContext().getState();
        return new BoilerLiquidMenu(ITContainerMenu.multiblockCtx(type, id, ctx), invPlayer, state.inventory, state.tanks, state);
    }

    public static BoilerLiquidMenu makeClient(MenuType<BoilerLiquidMenu> type, int id, Inventory invPlayer) {
        return new BoilerLiquidMenu(
                ITContainerMenu.clientCtx(type, id),
                invPlayer,
                new ITSlotwiseItemHandler(
                        List.of(
                                ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                                ITSlotwiseItemHandler.IOConstraint.OUTPUT
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
        this.addSlot(new ITSlot.FluidContainer(inv, BoilerLiquidLogic.INPUT_FUEL_SLOT_FILLED, 92, 15, 2));
        this.addSlot(new ITSlot.Output(inv, BoilerLiquidLogic.INPUT_FUEL_SLOT_EMPTY, 92, 54));
        for (int i = 0; i < 3; i++) for (int j = 0; j < 9; j++) { addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18)); }
        for (int i = 0; i < 9; i++) { addSlot(new Slot(inventoryPlayer, i, 8 + i * 18, 142)); }
        addGenericData(GenericContainerData.fluid(tanks.input1()));
        addGenericData(new GenericContainerData<>(GenericDataSerializers.FLOAT, () -> (state != null ? (float)state.heatLevel : heatLevel), f -> this.heatLevel = f));
    }

    public float getHeatLevel() { return heatLevel; }
}
