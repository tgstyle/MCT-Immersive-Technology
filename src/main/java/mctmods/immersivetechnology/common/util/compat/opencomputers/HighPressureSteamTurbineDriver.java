package mctmods.immersivetechnology.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.util.compat.opencomputers.ManagedEnvironmentIE;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityHighPressureSteamTurbineMaster;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityHighPressureSteamTurbineSlave;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class HighPressureSteamTurbineDriver extends DriverSidedTileEntity {
	@Override
	public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing facing) {
		TileEntity tile = world.getTileEntity(pos);

		if(tile instanceof TileEntityHighPressureSteamTurbineSlave) {
			TileEntityHighPressureSteamTurbineSlave te = (TileEntityHighPressureSteamTurbineSlave) tile;
			TileEntityHighPressureSteamTurbineMaster tem = te.master();
			if(tem != null && te.isRedstonePos()) {
				return new HighPressureSteamTurbineEnvironment(world, tem.getPos());
			}
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass() {
		return TileEntityHighPressureSteamTurbineSlave.class;
	}

	public class HighPressureSteamTurbineEnvironment extends ManagedEnvironmentIE.ManagedEnvMultiblock<TileEntityHighPressureSteamTurbineMaster> {
		public HighPressureSteamTurbineEnvironment(World world, BlockPos pos) {
			super(world, pos, TileEntityHighPressureSteamTurbineMaster.class);
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
