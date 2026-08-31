package mctmods.immersivetechnology.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.util.compat.opencomputers.ManagedEnvironmentIE;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerTankMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerTankSlave;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;

@SuppressWarnings("unused")
public class BoilerTankDriver extends DriverSidedTileEntity {
	@Override
	public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing facing) {
		TileEntity tile = world.getTileEntity(pos);

		if (tile instanceof TileEntityBoilerTankMaster) {
			TileEntityBoilerTankMaster tem = (TileEntityBoilerTankMaster) tile;
			return new BoilerTankEnvironment(world, tem.getPos());
		}
		return null;
	}

	@Override public Class<?> getTileEntityClass() {
		return TileEntityBoilerTankSlave.class;
	}

	public static class BoilerTankEnvironment extends ManagedEnvironmentIE.ManagedEnvMultiblock<TileEntityBoilerTankMaster> {
		public BoilerTankEnvironment(World world, BlockPos pos) {
			super(world, pos, TileEntityBoilerTankMaster.class);
		}

		@Callback(doc = "function():number -- get the heat level of the boiler tank")
		public Object[] getHeat(Context context, Arguments args) {
			return new Object[] {getTileEntity().heatLevel};
		}

		@Callback(doc = "function():table -- get information about the input tank")
		public Object[] getInputTankInfo(Context context, Arguments args) {
			return new Object[] {getTileEntity().tanks[0].getInfo()};
		}

		@Callback(doc = "function():table -- get information about the output tank")
		public Object[] getOutputTankInfo(Context context, Arguments args) {
			return new Object[] {getTileEntity().tanks[1].getInfo()};
		}

		@Callback(doc = "function():table -- get filled fluid canisters in all slots")
		public Object[] getFullCanisters(Context context, Arguments args) {
			HashMap<String, ItemStack> canisters = new HashMap<>(2);
			canisters.put("input", getTileEntity().inventory.get(0));
			canisters.put("output", getTileEntity().inventory.get(3));
			return new Object[] {canisters};
		}

		@Callback(doc = "function():table -- get empty fluid canisters in all slots")
		public Object[] getEmptyCanisters(Context context, Arguments args) {
			HashMap<String, ItemStack> canisters = new HashMap<>(2);
			canisters.put("input", getTileEntity().inventory.get(1));
			canisters.put("output", getTileEntity().inventory.get(2));
			return new Object[] {canisters};
		}

		@Override public String preferredName() {
			return "it_boiler_tank";
		}

		@Override public int priority() {
			return 1000;
		}
	}
}
