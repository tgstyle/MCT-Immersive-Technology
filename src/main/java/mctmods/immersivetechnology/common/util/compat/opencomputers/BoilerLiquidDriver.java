package mctmods.immersivetechnology.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.util.compat.opencomputers.ManagedEnvironmentIE;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerLiquidMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerLiquidSlave;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;

@SuppressWarnings("unused")
public class BoilerLiquidDriver extends DriverSidedTileEntity {
	@Override
	public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing facing) {
		TileEntity tile = world.getTileEntity(pos);

		if (tile instanceof TileEntityBoilerLiquidSlave) {
			TileEntityBoilerLiquidSlave te = (TileEntityBoilerLiquidSlave) tile;
			TileEntityBoilerLiquidMaster tem = te.master();
			if (tem != null && te.isRedstonePos()) { return new BoilerLiquidEnvironment(world, tem.getPos()); }
		}
		return null;
	}

	@Override public Class<?> getTileEntityClass() {
		return TileEntityBoilerLiquidSlave.class;
	}

	public static class BoilerLiquidEnvironment extends ManagedEnvironmentIE.ManagedEnvMultiblock<TileEntityBoilerLiquidMaster> {
		public BoilerLiquidEnvironment(World world, BlockPos pos) {
			super(world, pos, TileEntityBoilerLiquidMaster.class);
		}

		@Callback(doc = "function():number -- get the heat level of the liquid boiler")
		public Object[] getHeat(Context context, Arguments args) {
			return new Object[] {getTileEntity().heatLevel};
		}

		@Callback(doc = "function():boolean -- get whether the pilot light is lit")
		public Object[] isPilotLit(Context context, Arguments args) {
			return new Object[] {getTileEntity().pilotLit};
		}

		@Callback(doc = "function():table -- get information about the internal fuel tank")
		public Object[] getFuelTankInfo(Context context, Arguments args) {
			return new Object[] {getTileEntity().tanks[0].getInfo()};
		}

		@Callback(doc = "function():table -- get filled fluid canisters in all slots")
		public Object[] getFullCanisters(Context context, Arguments args) {
			HashMap<String, ItemStack> canisters = new HashMap<>(1);
			canisters.put("fuel", getTileEntity().inventory.get(0));
			return new Object[] {canisters};
		}

		@Callback(doc = "function():table -- get empty fluid canisters in all slots")
		public Object[] getEmptyCanisters(Context context, Arguments args) {
			HashMap<String, ItemStack> canisters = new HashMap<>(1);
			canisters.put("fuel", getTileEntity().inventory.get(1));
			return new Object[] {canisters};
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
			return "it_boiler_liquid";
		}

		@Override public int priority() {
			return 1000;
		}
	}
}
