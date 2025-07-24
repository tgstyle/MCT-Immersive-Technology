package mctmods.immersivetechnology.common.blocks.multiblocks.gui;

import blusunrize.immersiveengineering.common.gui.IEContainerMenu;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.ITDistillerLogic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class DistillerMenu extends IEContainerMenu {
    public static DistillerMenu makeServer(MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<ITDistillerLogic.State> ctx) { final ITDistillerLogic.State state = ctx.mbContext().getState(); return new DistillerMenu(multiblockCtx(type, id, ctx), invPlayer, state.getInventory(), state.getTanks()); }
    public static DistillerMenu makeClient(MenuType<?> type, int id, Inventory invPlayer) { return new DistillerMenu(clientCtx(type, id), invPlayer, new ItemStackHandler(4), new ITDistillerLogic.DistillerTank()); }
    public final ITDistillerLogic.DistillerTank tanks;
    protected DistillerMenu(MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, ITDistillerLogic.DistillerTank tanks) {
        super(ctx);
        this.tanks = tanks;
        this.addSlot(new IESlot.NewFluidContainer(inv, ITDistillerLogic.SLOT_WATER_IN, 56, 15, IESlot.NewFluidContainer.Filter.ANY));
        this.addSlot(new IESlot.NewOutput(inv, ITDistillerLogic.SLOT_WATER_EMPTY_OUT, 56, 54));
        this.addSlot(new IESlot.NewFluidContainer(inv, ITDistillerLogic.SLOT_WATER_EMPTY_IN, 120, 15, IESlot.NewFluidContainer.Filter.ANY));
        this.addSlot(new IESlot.NewOutput(inv, ITDistillerLogic.SLOT_WATER_OUT, 120, 54));
        for(int i = 0; i < 3; i++) for(int j = 0; j < 9; j++) addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
        for(int i = 0; i < 9; i++) addSlot(new Slot(inventoryPlayer, i, 8+i*18, 142));
        addGenericData(GenericContainerData.fluid(tanks.waterInput()));
        addGenericData(GenericContainerData.fluid(tanks.output()));
    }
}
