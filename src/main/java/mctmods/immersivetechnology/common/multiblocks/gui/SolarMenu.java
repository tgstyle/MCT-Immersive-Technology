package mctmods.immersivetechnology.common.multiblocks.gui;

import mctmods.immersivetechnology.common.gui.helper.ITContainerMenu;
import mctmods.immersivetechnology.common.gui.helper.ITGenericContainerData;
import mctmods.immersivetechnology.common.multiblocks.gui.helper.ITSlot;
import mctmods.immersivetechnology.common.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarTowerLogic;
import mctmods.immersivetechnology.common.fluids.helper.ITSolarTank;
import mctmods.immersivetechnology.common.multiblocks.metal.interfaces.ITISolarMultiblockState;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class SolarMenu extends ITContainerMenu {
    public final SimpleContainerData state;
    public final FluidTank inputTank;
    public FluidTank outputTank;
    private final Supplier<ITISolarMultiblockState> mbStateSupplier;

    public static SolarMenu makeServer(MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<? extends ITISolarMultiblockState> ctx) {
        final ITISolarMultiblockState state = ctx.mbContext().getState();
        return new SolarMenu(multiblockCtx(type, id, ctx), invPlayer, state.getInventory(), state.getTanks().input(), state.getTanks().output(), () -> state);
    }

    public static SolarMenu makeClient(MenuType<?> type, int id, Inventory invPlayer) {
        return new SolarMenu(clientCtx(type, id), invPlayer, new ITSlotwiseItemHandler(List.of(ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT, ITSlotwiseItemHandler.IOConstraint.OUTPUT, ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT, ITSlotwiseItemHandler.IOConstraint.OUTPUT), () -> {}), new FluidTank(ITSolarTank.TANK_CAPACITY), new FluidTank(ITSolarTank.TANK_CAPACITY), () -> null);
    }

    private SolarMenu(MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, FluidTank input, FluidTank output, Supplier<ITISolarMultiblockState> mbStateSupplier) {
        super(ctx);
        this.inputTank = input;
        this.outputTank = output;
        this.mbStateSupplier = mbStateSupplier;
        this.state = new SimpleContainerData(8);
        this.addSlot(new ITSlot.FluidContainer(inv, 0, 80, 17, 1) {
            @Override
            public boolean mayPlace(@Nonnull ItemStack itemStack) {
                FluidStack fs = FluidUtil.getFluidContained(itemStack).orElse(null);
                if (fs == null) return false;
                return inputTank.getFluidAmount() <= 0 || fs.isFluidEqual(inputTank.getFluid());
            }
        });
        this.addSlot(new ITSlot.Output(inv, 1, 80, 53));
        this.addSlot(new ITSlot.FluidContainer(inv, 2, 148, 17, 0) {
            @Override
            public boolean mayPlace(@Nonnull ItemStack itemStack) {
                return itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
            }
        });
        this.addSlot(new ITSlot.Output(inv, 3, 148, 53));
        ownSlotCount = 4;
        for (int i = 0; i < 3; i++) for (int j = 0; j < 9; j++) { addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18)); }
        for (int i = 0; i < 9; i++) { addSlot(new Slot(inventoryPlayer, i, 8 + i * 18, 142)); }
        addDataSlots(state);
        addGenericData(ITGenericContainerData.fluid(inputTank));
        addGenericData(ITGenericContainerData.fluid(outputTank));
    }

    @Override
    public void broadcastChanges() {
        if (mbStateSupplier != null) {
            ITISolarMultiblockState s = mbStateSupplier.get();
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
