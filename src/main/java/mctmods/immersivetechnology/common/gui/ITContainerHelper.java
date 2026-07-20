package mctmods.immersivetechnology.common.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public final class ITContainerHelper {
    private ITContainerHelper() { }

    public static List<Slot> playerInventorySlots(InventoryPlayer inventoryPlayer, int yOffset) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < 3; i++) { for (int j = 0; j < 9; j++) { slots.add(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, yOffset + i * 18)); } }
        for (int i = 0; i < 9; i++) { slots.add(new Slot(inventoryPlayer, i, 8 + i * 18, yOffset + 58)); }
        return slots;
    }

    public static boolean acceptsMatchingFluid(ItemStack itemStack, IFluidTank tank) {
        IFluidHandler h = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (h == null || h.getTankProperties().length == 0) { return false; }
        FluidStack fs = h.getTankProperties()[0].getContents();
        if (fs == null) { return false; }
        return tank.getFluidAmount() <= 0 || fs.isFluidEqual(tank.getFluid());
    }
}
