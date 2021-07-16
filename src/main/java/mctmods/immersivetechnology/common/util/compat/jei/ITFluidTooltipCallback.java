package mctmods.immersivetechnology.common.util.compat.jei;

import mezz.jei.api.gui.ITooltipCallback;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class ITFluidTooltipCallback implements ITooltipCallback<FluidStack> {
	@Override
	public void onTooltip(int slotIndex, boolean input, FluidStack ingredient, List<String> tooltip) {
		if(ingredient!=null && ingredient.getFluid() instanceof Fluid) {
			ArrayList<String> fluidInfo = new ArrayList<>();
			if(tooltip.size() >1) tooltip.addAll(1, fluidInfo);	else tooltip.addAll(fluidInfo);
		}
	}

}