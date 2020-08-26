package mctmods.immersivetechnology.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.util.compat.opencomputers.ManagedEnvironmentIE;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityGasTurbineMaster;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityGasTurbineSlave;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GasTurbineDriver extends DriverSidedTileEntity {
    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing facing) {
        TileEntity tile = world.getTileEntity(pos);

        if(tile instanceof TileEntityGasTurbineSlave) {
            TileEntityGasTurbineSlave te = (TileEntityGasTurbineSlave) tile;
            TileEntityGasTurbineMaster tem = te.master();
            if(tem != null && te.isRedstonePos()) {
                return new GasTurbineDriver.SteamTurbineEnvironment(world, tem.getPos());
            }
        }
        return null;
    }

    @Override
    public Class<?> getTileEntityClass() {
        return TileEntityGasTurbineSlave.class;
    }

    public class SteamTurbineEnvironment extends ManagedEnvironmentIE.ManagedEnvMultiblock<TileEntityGasTurbineMaster> {
        public SteamTurbineEnvironment(World world, BlockPos pos) {
            super(world, pos, TileEntityGasTurbineMaster.class);
        }

        @Callback(doc = "function():number -- get the turbine speed in RPM")
        public Object[] getSpeed(Context context, Arguments args) {
            return new Object[] {getTileEntity().speed};
        }

        @Callback(doc = "function():table -- get information about the turbine fuel level")
        public Object[] getInputTankInfo(Context context, Arguments args) {
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
            return "it_gas_turbine";
        }

        @Override
        public int priority() {
            return 1000;
        }
    }
}