package mctmods.immersivetechnology.common.multiblocks.gui;

import com.immersiveconvergence.api.gui.BaseContainerMenu;
import com.immersiveconvergence.api.gui.MenuSyncData;
import com.immersiveconvergence.api.gui.ModSlot;
import com.immersiveconvergence.api.util.ConstrainedItemHandler;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarTowerLogic;
import com.immersiveconvergence.api.util.TankPair;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.ISolarMultiblockState;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class SolarMenu extends BaseContainerMenu {
    public final SimpleContainerData state;
    public final FluidTank inputTank;
    public FluidTank outputTank;
    private final Supplier<ISolarMultiblockState> mbStateSupplier;

    public static SolarMenu makeServer(MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<? extends ISolarMultiblockState> ctx) {
        final ISolarMultiblockState state = ctx.mbContext().getState();
        return new SolarMenu(multiblockCtx(type, id, ctx), invPlayer, state.getInventory(), state.getTanks().input(), state.getTanks().output(), () -> state);
    }

    public static SolarMenu makeClient(MenuType<?> type, int id, Inventory invPlayer) {
        return new SolarMenu(clientCtx(type, id), invPlayer, new ConstrainedItemHandler(List.of(ConstrainedItemHandler.IOConstraint.FLUID_INPUT, ConstrainedItemHandler.IOConstraint.OUTPUT, ConstrainedItemHandler.IOConstraint.FLUID_INPUT, ConstrainedItemHandler.IOConstraint.OUTPUT), () -> {}), new FluidTank(TankPair.TANK_CAPACITY), new FluidTank(TankPair.TANK_CAPACITY), () -> null);
    }

    private SolarMenu(MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, FluidTank input, FluidTank output, Supplier<ISolarMultiblockState> mbStateSupplier) {
        super(ctx);
        this.inputTank = input;
        this.outputTank = output;
        this.mbStateSupplier = mbStateSupplier;
        this.state = new SimpleContainerData(8);
        this.addSlot(new ModSlot.FluidContainer(inv, 0, 80, 17, 1) { @Override public boolean mayPlace(@Nonnull ItemStack itemStack) { FluidStack fs = FluidUtil.getFluidContained(itemStack).orElse(null); if (fs == null) return false;return inputTank.getFluidAmount() <= 0 || fs.isFluidEqual(inputTank.getFluid()); }});
        this.addSlot(new ModSlot.Output(inv, 1, 80, 53));
        this.addSlot(new ModSlot.FluidContainer(inv, 2, 148, 17, 0) { @Override public boolean mayPlace(@Nonnull ItemStack itemStack) {
                return itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
            }});
        this.addSlot(new ModSlot.Output(inv, 3, 148, 53));
        ownSlotCount = 4;
        addPlayerInventorySlots(inventoryPlayer);
        addDataSlots(state);
        addGenericData(MenuSyncData.fluid(inputTank));
        addGenericData(MenuSyncData.fluid(outputTank));
    }

    @Override public void broadcastChanges() {
        if (mbStateSupplier != null) {
            ISolarMultiblockState s = mbStateSupplier.get();
            state.set(0, (int) s.getHeatLevel());
            if (!this.usingPlayers.isEmpty()) { state.set(1, SolarTowerLogic.getSolarIncidenceAngleSection(this.usingPlayers.get(0).level())); }
            state.set(2, s.getDirCounts()[0]);
            state.set(3, s.getDirCounts()[1]);
            state.set(4, s.getDirCounts()[2]);
            state.set(5, s.getDirCounts()[3]);
            state.set(6, s.getProcessProgress());
            state.set(7, s.isSunVisible() ? 1 : 0);
        }
        super.broadcastChanges();
    }
}
