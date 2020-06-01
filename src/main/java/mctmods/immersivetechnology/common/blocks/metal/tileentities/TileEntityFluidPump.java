package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.common.Config;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityFluidPipe.DirectionalFluidOutput;
import mctmods.immersivetechnology.common.util.IPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.HashMap;

public class TileEntityFluidPump extends blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPump implements ITickable, IBlockBounds, IHasDummyBlocks, IConfigurableSides, IFluidPipe, IIEInternalFluxHandler, IBlockOverlayText {

	boolean checkingArea = false;
	Fluid searchFluid = null;
	ArrayList<BlockPos> openList = new ArrayList<BlockPos>();
	ArrayList<BlockPos> closedList = new ArrayList<BlockPos>();
	ArrayList<BlockPos> checked = new ArrayList<BlockPos>();

	public int getRSPower(BlockPos position) {
		int toReturn = 0;
		for(EnumFacing directions : EnumFacing.values()) toReturn = Math.max(world.getRedstonePower(position.offset(directions, -1), directions), toReturn);
		return toReturn;
	}
		
	@Override
	public void update() {
		ApiUtils.checkForNeedlessTicking(this);
		if(dummy || world.isRemote) return;
		if(tank.getFluidAmount() > 0) {
			int i = outputFluid(tank.getFluid(), false);
			tank.drain(i, true);
		}

		if(getRSPower(getPos()) > 0 || getRSPower(getPos().add(0, 1, 0)) > 0) {
			for(EnumFacing f : EnumFacing.values()) {
				if(sideConfig[f.ordinal()] == 0) {
					BlockPos output = getPos().offset(f);
					TileEntity tile = Utils.getExistingTileEntity(world, output);
					if(tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, f.getOpposite())) {
						IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, f.getOpposite());
						FluidStack drain = handler.drain(Config.ITConfig.Experimental.pipe_pressurized_transfer_rate, false);
						if(drain == null || drain.amount <= 0) continue;
						int out = this.outputFluid(drain, false);
						handler.drain(out, true);
					} else if(world.getTotalWorldTime()%20 == ((getPos().getX()^getPos().getZ())&19) && world.getBlockState(getPos().offset(f)).getBlock() == Blocks.WATER && IEConfig.Machines.pump_infiniteWater && tank.fill(new FluidStack(FluidRegistry.WATER, 1000), false) == 1000 && this.energyStorage.extractEnergy(IEConfig.Machines.pump_consumption, true) >= IEConfig.Machines.pump_consumption) {
						int connectedSources = 0;
						for(EnumFacing f2 : EnumFacing.HORIZONTALS) {
							IBlockState waterState = world.getBlockState(getPos().offset(f).offset(f2));
							if(waterState.getBlock() == Blocks.WATER && Blocks.WATER.getMetaFromState(waterState) == 0) connectedSources++;
						}
						if(connectedSources > 1) {
							this.energyStorage.extractEnergy(IEConfig.Machines.pump_consumption, false);
							this.tank.fill(new FluidStack(FluidRegistry.WATER, 1000), true);
						}
					}
				}
			}
			if(world.getTotalWorldTime()%40 == (((getPos().getX()^getPos().getZ()))%40 + 40)%40) {
				if(closedList.isEmpty()) prepareAreaCheck();
				else {
					int target = closedList.size()-1;
					BlockPos pos = closedList.get(target);
					FluidStack fs = Utils.drainFluidBlock(world, pos, false);
					if(fs == null) closedList.remove(target);
					else if(tank.fill(fs, false) == fs.amount && this.energyStorage.extractEnergy(IEConfig.Machines.pump_consumption, true) >= IEConfig.Machines.pump_consumption) {
						this.energyStorage.extractEnergy(IEConfig.Machines.pump_consumption, false);
						fs = Utils.drainFluidBlock(world, pos, true);
						if(IEConfig.Machines.pump_placeCobble && placeCobble) world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
						this.tank.fill(fs, true);
						closedList.remove(target);
					}
				}
			}
		}
		if(checkingArea) checkAreaTick();
	}

	public void prepareAreaCheck() {
		openList.clear();
		closedList.clear();
		checked.clear();
		for(EnumFacing f : EnumFacing.values()) {
			if(sideConfig[f.ordinal()] == 0) {
				openList.add(getPos().offset(f));
				checkingArea = true;
			}
		}
	}

	public void checkAreaTick() {
		BlockPos next = null;
		final int closedListMax = 2048;
		int timeout = 0;
		while(timeout < 64 && closedList.size() < closedListMax && !openList.isEmpty()) {
			timeout++;
			next = openList.get(0);
			if(!checked.contains(next)) {
				Fluid fluid = Utils.getRelatedFluid(world, next);
				if(fluid != null && (fluid != FluidRegistry.WATER || !IEConfig.Machines.pump_infiniteWater) && (searchFluid == null || fluid == searchFluid)) {
					if(searchFluid == null) searchFluid = fluid;
					if(Utils.drainFluidBlock(world, next, false) != null) closedList.add(next);
					for(EnumFacing f : EnumFacing.values()) {
						BlockPos pos2 = next.offset(f);
						fluid = Utils.getRelatedFluid(world, pos2);
						if(!checked.contains(pos2) && !closedList.contains(pos2) && !openList.contains(pos2) && fluid != null && (fluid != FluidRegistry.WATER || !IEConfig.Machines.pump_infiniteWater) && (searchFluid == null || fluid == searchFluid)) openList.add(pos2);
					}
				}
				checked.add(next);
			}
			openList.remove(0);
		}
		if(closedList.size() >= closedListMax || openList.isEmpty()) checkingArea = false;
	}

	public int outputFluid(FluidStack fs, boolean simulate) {
		if(fs == null) return 0;

		int canAccept = fs.amount;
		if(canAccept <= 0) return 0;

		int accelPower = IEConfig.Machines.pump_consumption_accelerate;
		final int fluidForSort = canAccept;
		int sum = 0;
		HashMap<TileEntityFluidPipe.DirectionalFluidOutput, Integer> sorting = new HashMap<DirectionalFluidOutput, Integer>();
		for(EnumFacing f : EnumFacing.values()) {
			if(sideConfig[f.ordinal()] == 1) {
				TileEntity tile = Utils.getExistingTileEntity(world, getPos().offset(f));
				if(tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, f.getOpposite())) {
					IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, f.getOpposite());
					FluidStack insertResource = Utils.copyFluidStackWithAmount(fs, fs.amount, true);
					if(tile instanceof IPipe && this.energyStorage.extractEnergy(accelPower, true) >= accelPower) {
						insertResource.tag = new NBTTagCompound();
						insertResource.tag.setBoolean("pressurized", true);
					}
					int temp = handler.fill(insertResource, false);
					if(temp > 0) {
						sorting.put(new DirectionalFluidOutput(handler, tile, f), temp);
						sum += temp;
					}
				}
			}
		}
		if(sum > 0) {
			int f = 0;
			int i = 0;
			for(DirectionalFluidOutput output : sorting.keySet()) {
				float prio = sorting.get(output)/(float)sum;
				int amount = (int)(fluidForSort*prio);
				if(i++ == sorting.size()-1) amount = canAccept;
				FluidStack insertResource = Utils.copyFluidStackWithAmount(fs, amount, true);
				if(output.containingTile instanceof IPipe && this.energyStorage.extractEnergy(accelPower, true) >= accelPower) {
					this.energyStorage.extractEnergy(accelPower, false);
					insertResource.tag = new NBTTagCompound();
					insertResource.tag.setBoolean("pressurized", true);
				}
				int r = output.output.fill(insertResource, !simulate);
				f += r;
				canAccept -= r;
				if(canAccept <= 0) break;
			}
			return f;
		}
		return 0;
	}
}