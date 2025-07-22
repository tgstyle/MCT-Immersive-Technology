package mctmods.immersivetechnology.common.blocks.multiblocks.gui;

import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.CokeOvenLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.FurnaceHandler;
import blusunrize.immersiveengineering.common.gui.IEContainerMenu;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.ITAdvancedCokeOvenLogic;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.AdvancedCokeOvenRecipe;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class AdvancedCokeOvenMenu extends IEContainerMenu
{
    public final ContainerData state;
    public final FluidTank tank;

    public static AdvancedCokeOvenMenu makeServer(
            MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<ITAdvancedCokeOvenLogic.State> ctx
    )
    {
        final ITAdvancedCokeOvenLogic.State state = ctx.mbContext().getState();
        return new AdvancedCokeOvenMenu(
                multiblockCtx(type, id, ctx), invPlayer, state.getInventory().getRawHandler(), state, state.getTank()
        );
    }

    public static AdvancedCokeOvenMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
    {
        return new AdvancedCokeOvenMenu(
                clientCtx(type, id),
                invPlayer,
                new ItemStackHandler(CokeOvenLogic.NUM_SLOTS),
                new SimpleContainerData(FurnaceHandler.StateView.NUM_SLOTS),
                new FluidTank(CokeOvenLogic.TANK_CAPACITY)
        );
    }

    private AdvancedCokeOvenMenu(
            MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, ContainerData data, FluidTank tank
    )
    {
        super(ctx);
        this.addSlot(new SlotItemHandler(inv, 0, 30, 35)
        {
            @Override
            public boolean mayPlace(@Nonnull ItemStack itemStack)
            {
                return AdvancedCokeOvenRecipe.findRecipe(inventoryPlayer.player.level(), itemStack)!=null;
            }
        });
        this.addSlot(new IESlot.NewOutput(inv, 1, 85, 35));
        this.addSlot(new IESlot.NewFluidContainer(inv, 2, 152, 17, IESlot.NewFluidContainer.Filter.ANY));
        this.addSlot(new IESlot.NewOutput(inv, 3, 152, 53));
        ownSlotCount = 4;

        for(int i = 0; i < 3; i++)
            for(int j = 0; j < 9; j++)
                addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
        for(int i = 0; i < 9; i++)
            addSlot(new Slot(inventoryPlayer, i, 8+i*18, 142));
        this.state = data;
        this.tank = tank;
        addDataSlots(data);
        addGenericData(GenericContainerData.fluid(tank));
    }
}
