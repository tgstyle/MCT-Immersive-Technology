package mctmods.immersivetechnology.common.multiblocks.gui;

import blusunrize.immersiveengineering.api.energy.IMutableEnergyStorage;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import mctmods.immersivetechnology.common.gui.helper.ContainerMenu;
import mctmods.immersivetechnology.common.gui.helper.GenericContainerData;
import mctmods.immersivetechnology.common.multiblocks.gui.helper.ModSlot;
import mctmods.immersivetechnology.common.multiblocks.helper.SlotwiseItemHandler;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.DistillerLogic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class DistillerMenu extends ContainerMenu {
    public final DistillerLogic.DistillerTank tanks;
    public final IMutableEnergyStorage energy;

    public static DistillerMenu makeServer(MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<DistillerLogic.State> ctx) {
        final DistillerLogic.State state = ctx.mbContext().getState();
        return new DistillerMenu(multiblockCtx(type, id, ctx), invPlayer, state.getInventory(), state.getTanks(), state.getEnergy());
    }

    public static DistillerMenu makeClient(MenuType<?> type, int id, Inventory invPlayer) {
        return new DistillerMenu(
                clientCtx(type, id),
                invPlayer,
                new SlotwiseItemHandler(
                        List.of(
                                SlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                                SlotwiseItemHandler.IOConstraint.OUTPUT,
                                SlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                                SlotwiseItemHandler.IOConstraint.OUTPUT,
                                SlotwiseItemHandler.IOConstraint.OUTPUT
                        ),
                        () -> {}
                ),
                DistillerLogic.DistillerTank.makeClient(),
                new MutableEnergyStorage(32000)
        );
    }

    protected DistillerMenu(MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, DistillerLogic.DistillerTank tanks, IMutableEnergyStorage energy) {
        super(ctx);
        this.tanks = tanks;
        this.energy = energy;
        this.addSlot(new ModSlot.FluidContainer(inv, DistillerLogic.SLOT_INPUT_FILLED, 26, 17, 2));
        this.addSlot(new ModSlot.Output(inv, DistillerLogic.SLOT_INPUT_EMPTY, 26, 53));
        this.addSlot(new ModSlot.FluidContainer(inv, DistillerLogic.SLOT_OUTPUT_EMPTY, 134, 17, 1));
        this.addSlot(new ModSlot.Output(inv, DistillerLogic.SLOT_OUTPUT_FILLED, 134, 53));
        this.addSlot(new ModSlot.Output(inv, DistillerLogic.OUTPUT_SLOT, 80, 35));
        addPlayerInventorySlots(inventoryPlayer);
        addGenericData(GenericContainerData.energy(energy));
        addGenericData(GenericContainerData.fluid(tanks.input()));
        addGenericData(GenericContainerData.fluid(tanks.output()));
    }
}
