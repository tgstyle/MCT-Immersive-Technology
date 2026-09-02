package mctmods.immersivetechnology.common.multiblocks.gui;

import com.immersiveconvergence.api.gui.BaseContainerMenu;
import com.immersiveconvergence.api.gui.MenuSyncData;
import com.immersiveconvergence.api.gui.MenuSyncSerializers;
import com.immersiveconvergence.api.gui.ModSlot;
import com.immersiveconvergence.api.util.ConstrainedItemHandler;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerTankLogic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.items.IItemHandler;
import java.util.List;

public class BoilerTankMenu extends BaseContainerMenu {
    public final BoilerTankLogic.BoilerTanks tanks;
    public float heatLevel = 0.0f;
    public double workingHeatLevel = BoilerTankLogic.defaultWorkingHeatLevel();

    public static BoilerTankMenu makeServer(MenuType<BoilerTankMenu> type, int id, Inventory invPlayer, BaseContainerMenu.MultiblockMenuContext<BoilerTankLogic.State> ctx) {
        final BoilerTankLogic.State state = ctx.mbContext().getState();
        return new BoilerTankMenu(BaseContainerMenu.multiblockCtx(type, id, ctx), invPlayer, state.inventory, state.tanks, state);
    }

    public static BoilerTankMenu makeClient(MenuType<BoilerTankMenu> type, int id, Inventory invPlayer) {
        return new BoilerTankMenu(
                BaseContainerMenu.clientCtx(type, id),
                invPlayer,
                new ConstrainedItemHandler(
                        List.of(
                                ConstrainedItemHandler.IOConstraint.FLUID_INPUT,
                                ConstrainedItemHandler.IOConstraint.OUTPUT,
                                ConstrainedItemHandler.IOConstraint.FLUID_INPUT,
                                ConstrainedItemHandler.IOConstraint.OUTPUT
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
        addGenericData(MenuSyncData.fluid(tanks.input()));
        addGenericData(MenuSyncData.fluid(tanks.output()));
        addGenericData(new MenuSyncData<>(MenuSyncSerializers.FLOAT, () -> (state != null ? (float)state.heatLevel : heatLevel), f -> this.heatLevel = f));
        addGenericData(new MenuSyncData<>(MenuSyncSerializers.DOUBLE, () -> (state != null ? state.getWorkingHeatLevel() : workingHeatLevel), d -> this.workingHeatLevel = d));
    }

    public float getHeatLevel() { return heatLevel; }
    public double getWorkingHeatLevel() { return workingHeatLevel; }
}
