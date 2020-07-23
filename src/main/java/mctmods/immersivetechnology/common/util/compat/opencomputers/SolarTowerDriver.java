package mctmods.immersivetechnology.common.util.compat.opencomputers;

import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntitySolarTowerSlave;

// Largely based on BluSunrize's drivers for the IE machines

import blusunrize.immersiveengineering.common.util.compat.opencomputers.ManagedEnvironmentIE;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Optional;


public class SolarTowerDriver extends DriverSidedTileEntity {
	@Override
	public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing facing) {
		TileEntity tile = world.getTileEntity(pos);

		if (tile instanceof TileEntitySolarTowerSlave) {
			TileEntitySolarTowerSlave tower = (TileEntitySolarTowerSlave) tile;
			if (tower.master() != null && tower.isRedstonePos()) {
				return new SolarTowerEnvironment(world, tower.getPos());
			}
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass() {
		return TileEntitySolarTowerSlave.class;
	}

	public class SolarTowerEnvironment extends ManagedEnvironmentIE.ManagedEnvMultiblock<TileEntitySolarTowerSlave> {
		public SolarTowerEnvironment(World world, BlockPos pos) {
			super(world, pos, TileEntitySolarTowerSlave.class);
		}

		@Callback(doc = "function():number -- get the number of reflectors pointing at the tower")
		public Object[] getReflectors(Context context, Arguments args) {
			return new Object[] {getTileEntity().master().reflectors};
		}

		@Callback(doc = "function():table -- get information about the input tank")
		public Object[] getInputTankInfo(Context context, Arguments args) {
			return new Object[] {getTileEntity().master().tanks[0].getInfo()};
		}

		@Callback(doc = "function():table -- get information about the output tank")
		public Object[] getOutputTankInfo(Context context, Arguments args) {
			return new Object[] {getTileEntity().master().tanks[1].getInfo()};
		}

		@Callback(doc = "function():table -- get filled fluid canisters in all slots")
		public Object[] getFullCanisters(Context context, Arguments args) {
			HashMap<String, ItemStack> canisters = new HashMap<>(2);
			canisters.put("input", getTileEntity().master().inventory.get(1));
			canisters.put("output", getTileEntity().master().inventory.get(3));
			return new Object[] {canisters};
		}

		@Callback(doc = "function():table -- get empty fluid canisters in all slots")
		public Object[] getEmptyCanisters(Context context, Arguments args) {
			HashMap<String, ItemStack> canisters = new HashMap<>(2);
			canisters.put("input", getTileEntity().master().inventory.get(0));
			canisters.put("output", getTileEntity().master().inventory.get(2));
			return new Object[] {canisters};
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
			return "it_solar_tower";
		}

		@Override
		public int priority() {
			return 1000;
		}
	}
}
