package mctmods.immersivetechnology.common.multiblocks.gui;

import blusunrize.immersiveengineering.api.energy.IMutableEnergyStorage;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import mctmods.immersivetechnology.common.gui.helper.ContainerMenu;
import mctmods.immersivetechnology.common.gui.helper.GenericContainerData;
import mctmods.immersivetechnology.common.multiblocks.gui.helper.ModSlot;
import mctmods.immersivetechnology.common.multiblocks.helper.SlotwiseItemHandler;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.MeltingCrucibleLogic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class MeltingCrucibleMenu extends ContainerMenu {
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
        return new MeltingCrucibleMenu(clientCtx(type, id), invPlayer, new SlotwiseItemHandler(List.of(SlotwiseItemHandler.IOConstraint.FLUID_INPUT, SlotwiseItemHandler.IOConstraint.OUTPUT, SlotwiseItemHandler.IOConstraint.FLUID_INPUT, SlotwiseItemHandler.IOConstraint.OUTPUT), () -> {}), new FluidTank(MeltingCrucibleLogic.INPUT_TANK_CAPACITY), new FluidTank(MeltingCrucibleLogic.OUTPUT_TANK_CAPACITY), new MutableEnergyStorage(MeltingCrucibleLogic.ENERGY_CAPACITY), () -> null);
    }

    private MeltingCrucibleMenu(MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, FluidTank input, FluidTank output, IMutableEnergyStorage energy, Supplier<MeltingCrucibleLogic.State> mbStateSupplier) {
        super(ctx);
        this.inputTank = input;
        this.outputTank = output;
        this.energy = energy;
        this.mbStateSupplier = mbStateSupplier;
        this.state = new SimpleContainerData(4);
        this.addSlot(new ModSlot.FluidContainer(inv, 0, 80, 17, 1) {
            @Override public boolean mayPlace(@Nonnull ItemStack itemStack) {
                FluidStack fs = FluidUtil.getFluidContained(itemStack).orElse(FluidStack.EMPTY);
                if (fs.isEmpty()) return false;
                return inputTank.getFluidAmount() <= 0 || FluidStack.isSameFluid(fs, inputTank.getFluid());
            }
        });
        this.addSlot(new ModSlot.Output(inv, 1, 80, 53));
        this.addSlot(new ModSlot.FluidContainer(inv, 2, 148, 17, 0) {
            @Override public boolean mayPlace(@Nonnull ItemStack itemStack) {
                return itemStack.getCapability(Capabilities.FluidHandler.ITEM) != null;
            }
        });
        this.addSlot(new ModSlot.Output(inv, 3, 148, 53));
        ownSlotCount = 4;
        addPlayerInventorySlots(inventoryPlayer);
        addDataSlots(state);
        addGenericData(GenericContainerData.fluid(inputTank));
        addGenericData(GenericContainerData.fluid(outputTank));
        addGenericData(GenericContainerData.energy(energy));
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
