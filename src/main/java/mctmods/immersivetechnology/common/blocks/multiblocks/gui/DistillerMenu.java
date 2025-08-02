package mctmods.immersivetechnology.common.blocks.multiblocks.gui;

import blusunrize.immersiveengineering.api.energy.IMutableEnergyStorage;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import mctmods.immersivetechnology.common.blocks.gui.helper.ITContainerMenu;
import mctmods.immersivetechnology.common.blocks.multiblocks.gui.helper.ITSlot;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.ITDistillerLogic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class DistillerMenu extends ITContainerMenu {
    public final ITDistillerLogic.DistillerTank tanks;
    public final IMutableEnergyStorage energy;

    public static DistillerMenu makeServer(MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<ITDistillerLogic.State> ctx) {
        final ITDistillerLogic.State state = ctx.mbContext().getState();
        return new DistillerMenu(multiblockCtx(type, id, ctx), invPlayer, state.getInventory(), state.getTanks(), state.getEnergy());
    }

    public static DistillerMenu makeClient(MenuType<?> type, int id, Inventory invPlayer) {
        return new DistillerMenu(
                clientCtx(type, id),
                invPlayer,
                new ITSlotwiseItemHandler(
                        List.of(
                                ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                                ITSlotwiseItemHandler.IOConstraint.OUTPUT,
                                ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                                ITSlotwiseItemHandler.IOConstraint.OUTPUT,
                                ITSlotwiseItemHandler.IOConstraint.OUTPUT
                        ),
                        () -> {}
                ),
                ITDistillerLogic.DistillerTank.makeClient(),
                new MutableEnergyStorage(32000)
        );
    }

    protected DistillerMenu(MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, ITDistillerLogic.DistillerTank tanks, IMutableEnergyStorage energy) {
        super(ctx);
        this.tanks = tanks;
        this.energy = energy;
        this.addSlot(new ITSlot.FluidContainer(inv, ITDistillerLogic.SLOT_INPUT_FILLED, 26, 17, 2));
        this.addSlot(new ITSlot.Output(inv, ITDistillerLogic.SLOT_INPUT_EMPTY, 26, 53));
        this.addSlot(new ITSlot.FluidContainer(inv, ITDistillerLogic.SLOT_OUTPUT_EMPTY, 134, 17, 1));
        this.addSlot(new ITSlot.Output(inv, ITDistillerLogic.SLOT_OUTPUT_FILLED, 134, 53));
        this.addSlot(new ITSlot.Output(inv, ITDistillerLogic.OUTPUT_SLOT, 80, 35));
        for (int i = 0; i < 3; i++) { for (int j = 0; j < 9; j++) { addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18)); } }
        for (int i = 0; i < 9; i++) { addSlot(new Slot(inventoryPlayer, i, 8 + i * 18, 142)); }
        addGenericData(GenericContainerData.energy(energy));
        addGenericData(GenericContainerData.fluid(tanks.input()));
        addGenericData(GenericContainerData.fluid(tanks.output()));
    }
}
