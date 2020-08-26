package mctmods.immersivetechnology.common.util.compat.opencomputers;

import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntitySteamTurbineMaster;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntitySteamTurbineSlave;

// Largely based on BluSunrize's drivers for the IE machines

import blusunrize.immersiveengineering.common.util.compat.opencomputers.ManagedEnvironmentIE;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class SteamTurbineDriver extends DriverSidedTileEntity {
	@Override
	public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing facing) {
		TileEntity tile = world.getTileEntity(pos);

		if(tile instanceof TileEntitySteamTurbineSlave) {
			TileEntitySteamTurbineSlave te = (TileEntitySteamTurbineSlave) tile;
			TileEntitySteamTurbineMaster tem = te.master();
			if(tem != null && te.isRedstonePos()) {
				return new SteamTurbineEnvironment(world, tem.getPos());
			}
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass() {
		return TileEntitySteamTurbineSlave.class;
	}

	public class SteamTurbineEnvironment extends ManagedEnvironmentIE.ManagedEnvMultiblock<TileEntitySteamTurbineMaster> {
		public SteamTurbineEnvironment(World world, BlockPos pos) {
			super(world, pos, TileEntitySteamTurbineMaster.class);
		}

		@Callback(doc = "function():number -- get the turbine speed in RPM")
		public Object[] getSpeed(Context context, Arguments args) {
			return new Object[] {getTileEntity().speed};
		}

		@Callback(doc = "function():table -- get information about the turbine steam level")
		public Object[] getTankInfo(Context context, Arguments args) {
			return new Object[] {getTileEntity().tanks[0].getInfo()};
		}

		@Callback(doc = "function():table -- get information about the turbine output tank level")
		public Object[] getOutputTankInfo(Context context, Arguments args) {
			return new Object[] {getTileEntity().tanks[1].getInfo()};
		}

		@Callback(doc = "function(enabled:bool):nil -- Enables or disables computer control for the attached machine")
		public Object[] enableComputerControl(Context context, Arguments args) {
			return super.enableComputerControl(context, args);
		}

		@Callback(doc = "function(enabled:bool):nil")
		public Object[] setEnabled(Context context, Arguments args) {
			return super.setEnabled(context, args);
		}

		@Override
		public String preferredName() {
			return "it_steam_turbine";
		}

		@Override
		public int priority() {
			return 1000;
		}
	}
}
