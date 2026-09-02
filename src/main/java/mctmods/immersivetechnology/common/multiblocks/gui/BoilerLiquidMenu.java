package mctmods.immersivetechnology.common.multiblocks.gui;

import com.immersiveconvergence.api.gui.BaseContainerMenu;
import com.immersiveconvergence.api.gui.MenuSyncData;
import com.immersiveconvergence.api.gui.MenuSyncSerializers;
import com.immersiveconvergence.api.gui.ModSlot;
import com.immersiveconvergence.api.util.ConstrainedItemHandler;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerLiquidLogic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class BoilerLiquidMenu extends BaseContainerMenu {
    public final BoilerLiquidLogic.BoilerTank tanks;
    private float heatLevel = 0.0f;
    private double workingHeatLevel = BoilerLiquidLogic.defaultWorkingHeatLevel();

    public static BoilerLiquidMenu makeServer(MenuType<BoilerLiquidMenu> type, int id, Inventory invPlayer, BaseContainerMenu.MultiblockMenuContext<BoilerLiquidLogic.State> ctx) {
        final BoilerLiquidLogic.State state = ctx.mbContext().getState();
        return new BoilerLiquidMenu(BaseContainerMenu.multiblockCtx(type, id, ctx), invPlayer, state.inventory, state.tanks, state);
    }

    public static BoilerLiquidMenu makeClient(MenuType<BoilerLiquidMenu> type, int id, Inventory invPlayer) {
        return new BoilerLiquidMenu(
                BaseContainerMenu.clientCtx(type, id),
                invPlayer,
                new ConstrainedItemHandler(
                        List.of(
                                ConstrainedItemHandler.IOConstraint.FLUID_INPUT,
                                ConstrainedItemHandler.IOConstraint.OUTPUT
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
        addGenericData(MenuSyncData.fluid(tanks.input1()));
        addGenericData(new MenuSyncData<>(MenuSyncSerializers.FLOAT, () -> (state != null ? (float)state.heatLevel : heatLevel), f -> this.heatLevel = f));
        addGenericData(new MenuSyncData<>(MenuSyncSerializers.DOUBLE, () -> (state != null ? state.getWorkingHeatLevel() : workingHeatLevel), d -> this.workingHeatLevel = d));
    }

    public float getHeatLevel() { return heatLevel; }
    public double getWorkingHeatLevel() { return workingHeatLevel; }
}
