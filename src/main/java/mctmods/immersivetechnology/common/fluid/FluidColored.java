package mctmods.immersivetechnology.common.fluid;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.ITLogger;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class FluidColored extends Fluid {
	static final ResourceLocation ICON_MetalStill = new ResourceLocation("tconstruct:blocks/fluids/molten_metal");
	static final ResourceLocation ICON_MetalFlowing = new ResourceLocation("tconstruct:blocks/fluids/molten_metal_flow");
	static final ResourceLocation ICON_Still = new ResourceLocation(ImmersiveTechnology.MODID + ":blocks/fluids/fluid_still");
	static final ResourceLocation ICON_Flowing = new ResourceLocation(ImmersiveTechnology.MODID + ":blocks/fluids/fluid_flowing");

	int color;

	public FluidColored(String name, int color, int temp, int density, int viscosity) {
		super(name, ICON_MetalStill, ICON_MetalFlowing);
		if(FluidRegistry.isFluidRegistered(name) == false) {
			this.color = color;
			this.setTemperature(temp);
			this.setDensity(density);
			this.setViscosity(viscosity);
			FluidRegistry.registerFluid(this);
			FluidRegistry.addBucketForFluid(this);
			ITLogger.info("Added molten fluid: " + ImmersiveTechnology.MODID + ":" + name);
		}
	}

	public FluidColored(String name, int color, int density, int viscosity, boolean gaseous) {
		super(name, ICON_Still, ICON_Flowing);
		if(FluidRegistry.isFluidRegistered(name) == false) {
			this.color = color;
			this.setDensity(density);
			this.setViscosity(viscosity);
			this.setGaseous(gaseous);
			FluidRegistry.registerFluid(this);
			FluidRegistry.addBucketForFluid(this);
			ITLogger.info("Added fluid: " + ImmersiveTechnology.MODID + ":" + name);
		}
	}

	@Override
	public int getColor() {
		return color|0xff000000;
	}

}