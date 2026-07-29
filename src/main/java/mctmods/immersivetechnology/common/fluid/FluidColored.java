package mctmods.immersivetechnology.common.fluid;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.ITLogger;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class FluidColored extends Fluid {
	static final ResourceLocation ICON_MetalStill = new ResourceLocation("immersivetech:blocks/fluids/molten_metal");
	static final ResourceLocation ICON_MetalFlowing = new ResourceLocation("immersivetech:blocks/fluids/molten_metal_flow");
	static final ResourceLocation ICON_Still = new ResourceLocation(ImmersiveTechnology.MODID + ":blocks/fluids/fluid_still");
	static final ResourceLocation ICON_Flowing = new ResourceLocation(ImmersiveTechnology.MODID + ":blocks/fluids/fluid_flowing");

	int color;

	public FluidColored(String name, int color, int temp, int density, int viscosity) {
		super(name, ICON_MetalStill, ICON_MetalFlowing);
		this.color = color;
		setTemperature(temp);
		setDensity(density);
		setViscosity(viscosity);
	}

	public FluidColored(String name, int color, int density, int viscosity, boolean gaseous) {
		super(name, ICON_Still, ICON_Flowing);
		this.color = color;
		setDensity(density);
		setViscosity(viscosity);
		setGaseous(gaseous);
	}

	public FluidColored(String name, int color, int temp, int density, int viscosity, boolean gaseous) {
		super(name, ICON_Still, ICON_Flowing);
		this.color = color;
		setTemperature(temp);
		setDensity(density);
		setViscosity(viscosity);
		setGaseous(gaseous);
	}

	public static Fluid register(FluidColored fluid) {
		if (FluidRegistry.registerFluid(fluid)) {
			FluidRegistry.addBucketForFluid(fluid);
			return fluid;
		}
		ITLogger.logger.info("A fluid named {} is already registered, using the existing one", fluid.getName());
		return FluidRegistry.getFluid(fluid.getName());
	}

	@Override public int getColor() {
		return color|0xff000000;
	}
}
