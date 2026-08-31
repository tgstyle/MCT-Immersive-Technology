package mctmods.immersivetechnology.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.util.compat.opencomputers.ManagedEnvironmentIE;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerSolidMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerSolidSlave;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@SuppressWarnings("unused")
public class BoilerSolidDriver extends DriverSidedTileEntity {
	@Override
	public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing facing) {
		TileEntity tile = world.getTileEntity(pos);

		if (tile instanceof TileEntityBoilerSolidSlave) {
			TileEntityBoilerSolidSlave te = (TileEntityBoilerSolidSlave) tile;
			TileEntityBoilerSolidMaster tem = te.master();
			if (tem != null && te.isRedstonePos()) { return new BoilerSolidEnvironment(world, tem.getPos()); }
		}
		return null;
	}

	@Override public Class<?> getTileEntityClass() {
		return TileEntityBoilerSolidSlave.class;
	}

	public static class BoilerSolidEnvironment extends ManagedEnvironmentIE.ManagedEnvMultiblock<TileEntityBoilerSolidMaster> {
		public BoilerSolidEnvironment(World world, BlockPos pos) {
			super(world, pos, TileEntityBoilerSolidMaster.class);
		}

		@Callback(doc = "function():number -- get the heat level of the solid boiler")
		public Object[] getHeat(Context context, Arguments args) {
			return new Object[] {getTileEntity().heatLevel};
		}

		@Callback(doc = "function():boolean -- get whether the pilot light is lit")
		public Object[] isPilotLit(Context context, Arguments args) {
			return new Object[] {getTileEntity().pilotLit};
		}

		@Callback(doc = "function():number -- get the remaining burn time of the current fuel")
		public Object[] getBurnRemaining(Context context, Arguments args) {
			return new Object[] {getTileEntity().burnRemaining};
		}

		@Callback(doc = "function():table -- get the fuel item in the input slot")
		public Object[] getFuelStack(Context context, Arguments args) {
			return new Object[] {getTileEntity().inventory.get(0)};
		}

		@Callback(doc = "function(enabled:bool):nil -- Enables or disables computer control for the attached machine")
		public Object[] enableComputerControl(Context context, Arguments args) {
			return super.enableComputerControl(context, args);
		}

		@Callback(doc = "function(enabled:bool):nil")
		public Object[] setEnabled(Context context, Arguments args) {
			return super.setEnabled(context, args);
		}

		@Override public String preferredName() {
			return "it_boiler_solid";
		}

		@Override public int priority() {
			return 1000;
		}
	}
}
