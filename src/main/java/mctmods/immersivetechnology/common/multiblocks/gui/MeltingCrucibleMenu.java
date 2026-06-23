package mctmods.immersivetechnology.common.multiblocks.gui;

import blusunrize.immersiveengineering.api.energy.IMutableEnergyStorage;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import mctmods.immersivetechnology.common.gui.helper.ITContainerMenu;
import mctmods.immersivetechnology.common.gui.helper.ITGenericContainerData;
import mctmods.immersivetechnology.common.multiblocks.gui.helper.ITSlot;
import mctmods.immersivetechnology.common.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.MeltingCrucibleLogic;
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

public class MeltingCrucibleMenu extends ITContainerMenu {
    public final SimpleContainerData state;
    public final FluidTank inputTank;
    public final FluidTank outputTank;
    public final IMutableEnergyStorage energy;
    private final Supplier<MeltingCrucibleLogic.State> mbStateSupplier;

    public static MeltingCrucibleMenu makeServer(MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<? extends MeltingCrucibleLogic.State> ctx) {
        final MeltingCrucibleLogic.State state = ctx.mbContext().getState();
        return new MeltingCrucibleMenu(multiblockCtx(type, id, ctx), invPlayer, state.getInventory(), state.getTanks().input(), state.getTanks().output(), state.energy, () -> state);
    }

    public static MeltingCrucibleMenu makeClient(MenuType<?> type, int id, Inventory invPlayer) {
        return new MeltingCrucibleMenu(clientCtx(type, id), invPlayer, new ITSlotwiseItemHandler(List.of(ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT, ITSlotwiseItemHandler.IOConstraint.OUTPUT, ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT, ITSlotwiseItemHandler.IOConstraint.OUTPUT), () -> {}), new FluidTank(MeltingCrucibleLogic.TANK_CAPACITY), new FluidTank(MeltingCrucibleLogic.TANK_CAPACITY), new MutableEnergyStorage(MeltingCrucibleLogic.ENERGY_CAPACITY), () -> null);
    }

    private MeltingCrucibleMenu(MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, FluidTank input, FluidTank output, IMutableEnergyStorage energy, Supplier<MeltingCrucibleLogic.State> mbStateSupplier) {
        super(ctx);
        this.inputTank = input;
        this.outputTank = output;
        this.energy = energy;
        this.mbStateSupplier = mbStateSupplier;
        this.state = new SimpleContainerData(4);
        this.addSlot(new ITSlot.FluidContainer(inv, 0, 80, 17, 1) {
            @Override public boolean mayPlace(@Nonnull ItemStack itemStack) {
                FluidStack fs = FluidUtil.getFluidContained(itemStack).orElse(FluidStack.EMPTY);
                if (fs.isEmpty()) return false;
                return inputTank.getFluidAmount() <= 0 || fs.isFluidEqual(inputTank.getFluid());
            }
        });
        this.addSlot(new ITSlot.Output(inv, 1, 80, 53));
        this.addSlot(new ITSlot.FluidContainer(inv, 2, 148, 17, 0) {
            @Override public boolean mayPlace(@Nonnull ItemStack itemStack) {
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
        addGenericData(ITGenericContainerData.energy(energy));
    }

    @Override public void broadcastChanges() {
        if (mbStateSupplier != null) {
            MeltingCrucibleLogic.State s = mbStateSupplier.get();
            state.set(0, s.active ? 1 : 0);
            state.set(1, s.energy.getEnergyStored());
            state.set(2, s.energy.getMaxEnergyStored());
            state.set(3, (int) s.heatLevel);
        }
        super.broadcastChanges();
    }
}
