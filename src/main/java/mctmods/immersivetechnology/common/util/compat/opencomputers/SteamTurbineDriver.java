package mctmods.immersivetechnology.common.util.compat.opencomputers;

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

import java.util.Optional;


public class SteamTurbineDriver extends DriverSidedTileEntity {
	@Override
	public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing facing) {
		TileEntity tile = world.getTileEntity(pos);

		if (tile instanceof TileEntitySteamTurbineSlave) {
			TileEntitySteamTurbineSlave te = (TileEntitySteamTurbineSlave) tile;
			if (te.master() != null && te.isRedstonePos()) {
				return new SteamTurbineEnvironment(world, te.getPos());
			}
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass() {
		return TileEntitySteamTurbineSlave.class;
	}

	public class SteamTurbineEnvironment extends ManagedEnvironmentIE.ManagedEnvMultiblock<TileEntitySteamTurbineSlave> {
		public SteamTurbineEnvironment(World world, BlockPos pos) {
			super(world, pos, TileEntitySteamTurbineSlave.class);
		}

		@Callback(doc = "function():number -- get the turbine speed in RPM")
		public Object[] getSpeed(Context context, Arguments args) {
			return new Object[] {getTileEntity().master().speed};
		}

		@Callback(doc = "function():table -- get information about the turbine steam level")
		public Object[] getTankInfo(Context context, Arguments args) {
			return new Object[] {getTileEntity().master().tanks[0].getInfo()};
		}

		/*
		 enableComputerControl and setEnable almost directly from IE
		 Reimplemented here since they need to act on the master, which I (Sigma-One) couldn't figure out how to do without reimplementing them
		 */
		@Callback(doc = "function(enabled:bool):nil -- Enables or disables computer control for the attached machine")
		public Object[] enableComputerControl(Context context, Arguments args) {
			boolean state = args.checkBoolean(0);
			if (state) {
				getTileEntity().master().computerOn = Optional.of(state);
			}
			else {
				getTileEntity().master().computerOn = Optional.empty();
			}
			return null;
		}

		@Callback(doc = "function(enabled:bool):nil")
		public Object[] setEnabled(Context context, Arguments args) {
			boolean state = args.checkBoolean(0);
			if (!getTileEntity().master().computerOn.isPresent()) {
				throw new IllegalStateException("Computer control must be enabled to enable or disable the machine");
			}
			getTileEntity().master().computerOn = Optional.of(state);
			return null;
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
