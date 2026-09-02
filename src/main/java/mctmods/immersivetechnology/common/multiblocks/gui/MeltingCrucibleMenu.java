package mctmods.immersivetechnology.common.multiblocks.gui;

import blusunrize.immersiveengineering.api.energy.IMutableEnergyStorage;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import com.immersiveconvergence.api.gui.BaseContainerMenu;
import com.immersiveconvergence.api.gui.MenuSyncData;
import com.immersiveconvergence.api.gui.ModSlot;
import com.immersiveconvergence.api.util.ConstrainedItemHandler;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.MeltingCrucibleLogic;
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

public class MeltingCrucibleMenu extends BaseContainerMenu {
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
        return new MeltingCrucibleMenu(clientCtx(type, id), invPlayer, new ConstrainedItemHandler(List.of(ConstrainedItemHandler.IOConstraint.FLUID_INPUT, ConstrainedItemHandler.IOConstraint.OUTPUT, ConstrainedItemHandler.IOConstraint.FLUID_INPUT, ConstrainedItemHandler.IOConstraint.OUTPUT), () -> {}), new FluidTank(MeltingCrucibleLogic.inputTankCapacity()), new FluidTank(MeltingCrucibleLogic.outputTankCapacity()), new MutableEnergyStorage(MeltingCrucibleLogic.energyCapacity()), () -> null);
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
                return inputTank.getFluidAmount() <= 0 || fs.isFluidEqual(inputTank.getFluid());
            }
        });
        this.addSlot(new ModSlot.Output(inv, 1, 80, 53));
        this.addSlot(new ModSlot.FluidContainer(inv, 2, 148, 17, 0) {
            @Override public boolean mayPlace(@Nonnull ItemStack itemStack) {
                return itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
            }
        });
        this.addSlot(new ModSlot.Output(inv, 3, 148, 53));
        ownSlotCount = 4;
        addPlayerInventorySlots(inventoryPlayer);
        addDataSlots(state);
        addGenericData(MenuSyncData.fluid(inputTank));
        addGenericData(MenuSyncData.fluid(outputTank));
        addGenericData(MenuSyncData.energy(energy));
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
