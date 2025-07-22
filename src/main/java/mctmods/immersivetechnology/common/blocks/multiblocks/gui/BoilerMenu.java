package mctmods.immersivetechnology.common.blocks.multiblocks.gui;

import blusunrize.immersiveengineering.common.gui.IEContainerMenu;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.ITBoilerLogic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class BoilerMenu extends IEContainerMenu
{
    public static BoilerMenu makeServer(
            MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<ITBoilerLogic.State> ctx
    )
    {
        final ITBoilerLogic.State state = ctx.mbContext().getState();
        return new BoilerMenu(
                multiblockCtx(type, id, ctx), invPlayer, state.getInventory().getRawHandler(), state.getTanks()
        );
    }

    public static BoilerMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
    {
        return new BoilerMenu(
                clientCtx(type, id),
                invPlayer,
                new ItemStackHandler(ITBoilerLogic.NUM_SLOTS),
                new ITBoilerLogic.BoilerTank());
    }

    public final ITBoilerLogic.BoilerTank tanks;

    protected BoilerMenu(MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, ITBoilerLogic.BoilerTank tanks) {
        super(ctx);
        this.tanks = tanks;
        this.addSlot(new IESlot.NewFluidContainer(inv, ownSlotCount++, 37, 15, IESlot.NewFluidContainer.Filter.ANY));
        this.addSlot(new IESlot.NewOutput(inv, ownSlotCount++, 37, 54));
        this.addSlot(new IESlot.NewFluidContainer(inv, ownSlotCount++, 76, 15, IESlot.NewFluidContainer.Filter.ANY));
        this.addSlot(new IESlot.NewOutput(inv, ownSlotCount++, 76, 54));
        this.addSlot(new IESlot.NewFluidContainer(inv, ownSlotCount++, 149, 15, IESlot.NewFluidContainer.Filter.ANY));
        this.addSlot(new IESlot.NewOutput(inv, ownSlotCount++, 149, 54));

        for(int i = 0; i < 3; i++)
            for(int j = 0; j < 9; j++)
                addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
        for(int i = 0; i < 9; i++)
            addSlot(new Slot(inventoryPlayer, i, 8+i*18, 142));
        addGenericData(GenericContainerData.fluid(tanks.fuelInput()));
        addGenericData(GenericContainerData.fluid(tanks.waterInput()));
        addGenericData(GenericContainerData.fluid(tanks.output()));
    }
}
