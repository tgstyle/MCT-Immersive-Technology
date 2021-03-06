package mctmods.immersivetechnology.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.util.compat.opencomputers.ManagedEnvironmentIE;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityHeatExchangerMaster;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityHeatExchangerSlave;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HeatExchangerDriver extends DriverSidedTileEntity {
    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing facing) {
        TileEntity tile = world.getTileEntity(pos);

        if(tile instanceof TileEntityHeatExchangerSlave) {
            TileEntityHeatExchangerSlave te = (TileEntityHeatExchangerSlave) tile;
            TileEntityHeatExchangerMaster tem = te.master();
            if(tem != null && te.isRedstonePos()) {
                return new HeatExchangerDriver.HeatExchangerEnvironment(world, tem.getPos());
            }
        }
        return null;
    }

    @Override
    public Class<?> getTileEntityClass() {
        return TileEntityHeatExchangerSlave.class;
    }

    public class HeatExchangerEnvironment extends ManagedEnvironmentIE.ManagedEnvMultiblock<TileEntityHeatExchangerMaster> {
        public HeatExchangerEnvironment(World world, BlockPos pos) {
            super(world, pos, TileEntityHeatExchangerMaster.class);
        }

        @Callback(doc = "function():table -- get information about the first input tank")
        public Object[] getFirstInputTankInfo(Context context, Arguments args) {
            return new Object[] {getTileEntity().tanks[0].getInfo()};
        }

        @Callback(doc = "function():table -- get information about the second input tank")
        public Object[] getSecondInputTankInfo(Context context, Arguments args) {
            return new Object[] {getTileEntity().tanks[1].getInfo()};
        }

        @Callback(doc = "function():table -- get information about the first output tank")
        public Object[] getFirstOutputTankInfo(Context context, Arguments args) {
            return new Object[] {getTileEntity().tanks[2].getInfo()};
        }

        @Callback(doc = "function():table -- get information about the second output tank")
        public Object[] getSecondOutputTankInfo(Context context, Arguments args) {
            return new Object[] {getTileEntity().tanks[3].getInfo()};
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
            return "it_heat_exchanger";
        }

        @Override
        public int priority() {
            return 1000;
        }
    }
}