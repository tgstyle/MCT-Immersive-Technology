package mctmods.immersivetechnology.common.blocks.multiblocks.gui;

import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import mctmods.immersivetechnology.common.blocks.gui.helper.ITContainerMenu;
import mctmods.immersivetechnology.common.blocks.multiblocks.gui.helper.ITSlot;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.SolarTowerLogic;
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

public class SolarTowerMenu extends ITContainerMenu {
    public final SimpleContainerData state;
    public final FluidTank inputTank;
    public final FluidTank outputTank;
    private final Supplier<SolarTowerLogic.State> mbStateSupplier;

    public static SolarTowerMenu makeServer(MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<SolarTowerLogic.State> ctx) {
        final SolarTowerLogic.State state = ctx.mbContext().getState();
        return new SolarTowerMenu(multiblockCtx(type, id, ctx), invPlayer, state.getInventory(), state.getTanks().input(), state.getTanks().output(), () -> state);
    }

    public static SolarTowerMenu makeClient(MenuType<?> type, int id, Inventory invPlayer) {
        return new SolarTowerMenu(clientCtx(type, id), invPlayer, new ITSlotwiseItemHandler(List.of(ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT, ITSlotwiseItemHandler.IOConstraint.OUTPUT, ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT, ITSlotwiseItemHandler.IOConstraint.OUTPUT), () -> {}), new FluidTank(SolarTowerLogic.TANK_CAPACITY), new FluidTank(SolarTowerLogic.TANK_CAPACITY), () -> null);
    }

    private SolarTowerMenu(MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, FluidTank input, FluidTank output, Supplier<SolarTowerLogic.State> mbStateSupplier) {
        super(ctx);
        this.inputTank = input;
        this.outputTank = output;
        this.mbStateSupplier = mbStateSupplier;
        this.state = new SimpleContainerData(2);
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
        addGenericData(GenericContainerData.fluid(inputTank));
        addGenericData(GenericContainerData.fluid(outputTank));
    }

    @Override
    public void broadcastChanges() {
        if (mbStateSupplier != null) {
            SolarTowerLogic.State s = mbStateSupplier.get();
            state.set(0, (int) s.heatLevel);
            if (!this.usingPlayers.isEmpty()) { state.set(1, SolarTowerLogic.getSolarIncidenceAngleSection(this.usingPlayers.get(0).level())); }
        }
        super.broadcastChanges();
    }
}
