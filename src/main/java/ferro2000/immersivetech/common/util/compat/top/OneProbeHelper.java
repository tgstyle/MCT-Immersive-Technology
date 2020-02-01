package ferro2000.immersivetech.common.util.compat.top;

import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;

import ferro2000.immersivetech.ImmersiveTech;
import ferro2000.immersivetech.common.Config.ITConfig.Machines.Boiler;
import ferro2000.immersivetech.common.Config.ITConfig.MechanicalEnergy;
import ferro2000.immersivetech.common.blocks.ITBlockInterface;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityBoiler;
import ferro2000.immersivetech.common.util.compat.ITCompatModule;
import mcjty.theoneprobe.api.*;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.event.FMLInterModComms;

import javax.annotation.Nullable;
import java.util.function.Function;

	/*
	* Created by Kurtchekov on 2019-01-01.
	*/
public class OneProbeHelper extends ITCompatModule implements Function<ITheOneProbe, Void> {

	private static int maxSpeed = MechanicalEnergy.mechanicalEnergy_speed_max;
	private static double workingHeatLevel = Boiler.boiler_heat_workingLevel;
	

	@Override
	public void preInit() {
		FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", this.getClass().getName());
	}

	@Override
	public void init() {
	}

	@Override
	public void postInit() {
	}

	@Nullable
	@Override
	public Void apply(@Nullable ITheOneProbe input) {
		input.registerProvider(new MechanicalEnergyProvider());
		input.registerProvider(new MiscProvider());
		return null;
	}

	public static class MiscProvider implements IProbeInfoProvider {
		@Override
		public String getID() {
			return ImmersiveTech.MODID + ":" + "MiscInfo";
		}

		@Override
		public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
			TileEntity te = world.getTileEntity(data.getPos());
			if(te instanceof TileEntityBoiler) {
				TileEntityMultiblockPart<?> master = ((TileEntityMultiblockPart<?>)te).master();
				if(master == null) return;
				TileEntityBoiler boiler = (TileEntityBoiler)master;
				int current = (int)(boiler.heatLevel / workingHeatLevel * 100);
				probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2)).text("Heat Level").progress(current, 100, probeInfo.defaultProgressStyle().numberFormat(NumberFormat.FULL).suffix("%"));
			}
		}
	}

	public static class MechanicalEnergyProvider implements IProbeInfoProvider {
		@Override
		public String getID() {
			return ImmersiveTech.MODID + ":" + "MechanicalEnergyInfo";
		}

		@Override
		public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
			TileEntity te = world.getTileEntity(data.getPos());
			if(te instanceof ITBlockInterface.IMechanicalEnergy) {
				TileEntityMultiblockPart<?> master = ((TileEntityMultiblockPart<?>)te).master();
				if(master == null) return;
				int current = ((ITBlockInterface.IMechanicalEnergy)master).getEnergy();
				probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2)).text("Speed").progress(current, maxSpeed, probeInfo.defaultProgressStyle().numberFormat(NumberFormat.FULL).suffix("RPM"));
			}
		}
	}
}