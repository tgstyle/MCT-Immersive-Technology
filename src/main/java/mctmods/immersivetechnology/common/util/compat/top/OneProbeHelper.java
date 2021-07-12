package mctmods.immersivetechnology.common.util.compat.top;

import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import mcjty.theoneprobe.api.*;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.Boiler;
import mctmods.immersivetechnology.common.Config.ITConfig.MechanicalEnergy;
import mctmods.immersivetechnology.common.blocks.ITBlockInterfaces.IMechanicalEnergy;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityBoilerMaster;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityBoilerSlave;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
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
			return ImmersiveTechnology.MODID + ":" + "MiscInfo";
		}
		@Override
		public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
			TileEntity te = world.getTileEntity(data.getPos());
			if(te instanceof TileEntityBoilerSlave) {
				TileEntityBoilerMaster master = ((TileEntityBoilerSlave)te).master();
				if(master == null) return;
				int current = (int)(master.heatLevel / workingHeatLevel * 100);
				probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
						.text("{*keyword.immersivetech.heat_level*}").progress(current, 100, probeInfo.defaultProgressStyle().numberFormat(NumberFormat.FULL).suffix("%"));
			}
		}
	}

	public static class MechanicalEnergyProvider implements IProbeInfoProvider {
		@Override
		public String getID() {
			return ImmersiveTechnology.MODID + ":" + "MechanicalEnergyInfo";
		}
		@Override
		public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
			TileEntity te = world.getTileEntity(data.getPos());
			if(te instanceof IMechanicalEnergy) {
				TileEntityMultiblockPart<?> master = ((TileEntityMultiblockPart<?>)te).master();
				if(master == null) return;
				int current = ((IMechanicalEnergy)master).getSpeed();
				probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
						.text("{*keyword.immersivetech.speed*}")
						.progress(current, maxSpeed, probeInfo.defaultProgressStyle().numberFormat(NumberFormat.FULL))
						.text("{*keyword.immersivetech.rotations_per_minute*}");
			}
		}
	}
}