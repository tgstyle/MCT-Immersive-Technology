package mctmods.immersivetechnology.common.multiblocks.gui;

import com.immersiveconvergence.api.gui.BaseContainerMenu;
import com.immersiveconvergence.api.gui.MenuSyncData;
import com.immersiveconvergence.api.gui.MenuSyncSerializers;
import com.immersiveconvergence.api.gui.ModSlot;
import com.immersiveconvergence.api.util.ConstrainedItemHandler;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerSolidLogic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class BoilerSolidMenu extends BaseContainerMenu {
    private float heatLevel = 0.0f;
    private int burnRemaining = 0;
    private int totalBurnTime = 0;
    private double workingHeatLevel = BoilerSolidLogic.defaultWorkingHeatLevel();

    public static BoilerSolidMenu makeServer(MenuType<BoilerSolidMenu> type, int id, Inventory invPlayer, BaseContainerMenu.MultiblockMenuContext<BoilerSolidLogic.State> ctx) {
        final BoilerSolidLogic.State state = ctx.mbContext().getState();
        return new BoilerSolidMenu(BaseContainerMenu.multiblockCtx(type, id, ctx), invPlayer, state.inventory, state);
    }

    public static BoilerSolidMenu makeClient(MenuType<BoilerSolidMenu> type, int id, Inventory invPlayer) {
        ConstrainedItemHandler dummy = new ConstrainedItemHandler(List.of(ConstrainedItemHandler.IOConstraint.INPUT), () -> {});
        return new BoilerSolidMenu(BaseContainerMenu.clientCtx(type, id), invPlayer, dummy, null);
    }

    protected BoilerSolidMenu(BaseContainerMenu.MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, BoilerSolidLogic.State state) {
        super(ctx);
        this.addSlot(new ModSlot.Fuel(inv, BoilerSolidLogic.INPUT_FUEL_SLOT, 44, 34));
        ownSlotCount=1;
        addPlayerInventorySlots(inventoryPlayer);
        addGenericData(new MenuSyncData<>(MenuSyncSerializers.FLOAT, () -> (state != null ? (float)state.heatLevel : heatLevel), f -> this.heatLevel = f));
        addGenericData(MenuSyncData.int32(() -> state != null ? state.burnRemaining : burnRemaining, i -> this.burnRemaining = i));
        addGenericData(MenuSyncData.int32(() -> state != null ? state.totalBurnTime : totalBurnTime, i -> this.totalBurnTime = i));
        addGenericData(new MenuSyncData<>(MenuSyncSerializers.DOUBLE, () -> (state != null ? state.getWorkingHeatLevel() : workingHeatLevel), d -> this.workingHeatLevel = d));
    }

    public float getHeatLevel() { return heatLevel; }
    public int getBurnRemaining() { return burnRemaining; }
    public int getTotalBurnTime() { return totalBurnTime; }
    public double getWorkingHeatLevel() { return workingHeatLevel; }
}
