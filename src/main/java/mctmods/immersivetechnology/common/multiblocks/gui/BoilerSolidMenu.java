package mctmods.immersivetechnology.common.multiblocks.gui;

import mctmods.immersivetechnology.common.gui.helper.ITContainerMenu;
import mctmods.immersivetechnology.common.gui.helper.ITGenericContainerData;
import mctmods.immersivetechnology.common.gui.helper.ITGenericDataSerializers;
import mctmods.immersivetechnology.common.multiblocks.gui.helper.ITSlot;
import mctmods.immersivetechnology.common.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerSolidLogic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class BoilerSolidMenu extends ITContainerMenu {
    private float heatLevel = 0.0f;
    private int burnRemaining = 0;
    private int totalBurnTime = 0;

    public static BoilerSolidMenu makeServer(MenuType<BoilerSolidMenu> type, int id, Inventory invPlayer, ITContainerMenu.MultiblockMenuContext<BoilerSolidLogic.State> ctx) {
        final BoilerSolidLogic.State state = ctx.mbContext().getState();
        return new BoilerSolidMenu(ITContainerMenu.multiblockCtx(type, id, ctx), invPlayer, state.inventory, state);
    }

    public static BoilerSolidMenu makeClient(MenuType<BoilerSolidMenu> type, int id, Inventory invPlayer) {
        ITSlotwiseItemHandler dummy = new ITSlotwiseItemHandler(List.of(ITSlotwiseItemHandler.IOConstraint.INPUT), () -> {});
        return new BoilerSolidMenu(ITContainerMenu.clientCtx(type, id), invPlayer, dummy, null);
    }

    protected BoilerSolidMenu(ITContainerMenu.MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, BoilerSolidLogic.State state) {
        super(ctx);
        this.addSlot(new ITSlot.Fuel(inv, BoilerSolidLogic.INPUT_FUEL_SLOT, 44, 34));
        ownSlotCount=1;
        for (int i = 0; i < 3; i++) for (int j = 0; j < 9; j++) { addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18)); }
        for (int i = 0; i < 9; i++) { addSlot(new Slot(inventoryPlayer, i, 8 + i * 18, 142)); }
        addGenericData(new ITGenericContainerData<>(ITGenericDataSerializers.FLOAT, () -> (state != null ? (float)state.heatLevel : heatLevel), f -> this.heatLevel = f));
        addGenericData(ITGenericContainerData.int32(() -> state != null ? state.burnRemaining : burnRemaining, i -> this.burnRemaining = i));
        addGenericData(ITGenericContainerData.int32(() -> state != null ? state.totalBurnTime : totalBurnTime, i -> this.totalBurnTime = i));
    }

    public float getHeatLevel() { return heatLevel; }
    public int getBurnRemaining() { return burnRemaining; }
    public int getTotalBurnTime() { return totalBurnTime; }
}
