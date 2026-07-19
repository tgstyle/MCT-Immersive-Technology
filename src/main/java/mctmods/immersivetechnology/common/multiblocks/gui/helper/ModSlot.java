package mctmods.immersivetechnology.common.multiblocks.gui.helper;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class ModSlot extends Slot {
    final AbstractContainerMenu containerMenu;

    public ModSlot(AbstractContainerMenu containerMenu, Container inv, int id, int x, int y) {
        super(inv, id, x, y);
        this.containerMenu = containerMenu;
    }

    public static class Input extends SlotItemHandlerIT {
        public Input(IItemHandler inv, int id, int x, int y) { super(inv, id, x, y); }
    }

    public static class Output extends SlotItemHandlerIT {
        public Output(IItemHandler inv, int id, int x, int y) { super(inv, id, x, y); }

        @Override public boolean mayPlace(@NotNull ItemStack itemStack) { return false; }
    }

    public static class FluidContainer extends SlotItemHandlerIT {
        int filter;

        public FluidContainer(IItemHandler inv, int id, int x, int y, int filter) {
            super(inv, id, x, y);
            this.filter = filter;
        }

        @Override public boolean mayPlace(@NotNull ItemStack itemStack) {
            Optional<IFluidHandlerItem> handlerCap = FluidUtil.getFluidHandler(itemStack);
            return handlerCap.map(handler -> {
                if (handler.getTanks() <= 0) return false;

                if (filter == 1) return handler.getFluidInTank(0).isEmpty();
                else if (filter == 2) return !handler.getFluidInTank(0).isEmpty();
                return true;
            }).orElse(false);
        }
    }

    public static class Fuel extends SlotItemHandlerIT {
        public Fuel(IItemHandler inv, int id, int x, int y) { super(inv, id, x, y); }

        @Override public boolean mayPlace(@NotNull ItemStack stack) { return getItemHandler().isItemValid(getSlotIndex(), stack); }
    }

    public static class ItemHandlerGhost extends ModSlot.SlotItemHandlerIT {

        public ItemHandlerGhost(IItemHandler itemHandler, int index, int xPosition, int yPosition) { super(itemHandler, index, xPosition, yPosition); }

        @Override public boolean mayPickup(@NotNull Player playerIn) { return false; }
    }

    private static class SlotItemHandlerIT extends SlotItemHandler {
        public SlotItemHandlerIT(IItemHandler itemHandler, int index, int xPosition, int yPosition) { super(itemHandler, index, xPosition, yPosition); }

        @Override public int getMaxStackSize(@NotNull ItemStack stack) { return Math.min(this.getMaxStackSize(), stack.getMaxStackSize()); }
    }
}
