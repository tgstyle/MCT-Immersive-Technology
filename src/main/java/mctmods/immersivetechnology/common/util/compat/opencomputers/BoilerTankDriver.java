package mctmods.immersivetechnology.common.util.compat.opencomputers;

import com.immersiveconvergence.api.compat.opencomputers.ManagedEnvironmentIC;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerTankMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerTankSlave;
import mctmods.immersivetechnology.common.util.compat.computers.ITComputerTables;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@SuppressWarnings("unused")
public class BoilerTankDriver extends DriverSidedTileEntity {
	@Override
	public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing facing) {
		TileEntity tile = world.getTileEntity(pos);

		if (tile instanceof TileEntityBoilerTankSlave) {
			TileEntityBoilerTankSlave te = (TileEntityBoilerTankSlave) tile;
			TileEntityBoilerTankMaster tem = te.master();
			if (tem != null && te.isRedstonePos()) {
				return new BoilerTankEnvironment(world, tem.getPos());
			}
		}
		return null;
	}

	@Override public Class<?> getTileEntityClass() {
		return TileEntityBoilerTankSlave.class;
	}

	public static class BoilerTankEnvironment extends ManagedEnvironmentIC.ManagedEnvMultiblock<TileEntityBoilerTankMaster> {
		public BoilerTankEnvironment(World world, BlockPos pos) {
			super(world, pos, TileEntityBoilerTankMaster.class);
		}

		@Callback(doc = "function():number -- get the heat level of the boiler tank")
		public Object[] getHeat(Context context, Arguments args) {
			return call(ITComputerTables.BOILER_TANK, "getHeat");
		}

		@Callback(doc = "function():table -- get information about the input tank")
		public Object[] getInputTankInfo(Context context, Arguments args) {
			return call(ITComputerTables.BOILER_TANK, "getInputTankInfo");
		}

		@Callback(doc = "function():table -- get information about the output tank")
		public Object[] getOutputTankInfo(Context context, Arguments args) {
			return call(ITComputerTables.BOILER_TANK, "getOutputTankInfo");
		}

		@Callback(doc = "function():table -- get filled fluid canisters in all slots")
		public Object[] getFullCanisters(Context context, Arguments args) {
			return call(ITComputerTables.BOILER_TANK, "getFullCanisters");
		}

		@Callback(doc = "function():table -- get empty fluid canisters in all slots")
		public Object[] getEmptyCanisters(Context context, Arguments args) {
			return call(ITComputerTables.BOILER_TANK, "getEmptyCanisters");
		}

		@Override public String preferredName() {
			return "it_boiler_tank";
		}

		@Override public int priority() {
			return 1000;
		}
	}
}
