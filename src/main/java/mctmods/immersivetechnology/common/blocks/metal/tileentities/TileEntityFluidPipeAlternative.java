package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.common.Config.ITConfig.Experimental;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.util.IPipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.*;

public class TileEntityFluidPipeAlternative extends blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPipe implements IPipe {

	public int transferRate = Experimental.pipe_transfer_rate;
	public int transferRatePressurized = Experimental.pipe_pressurized_transfer_rate;

	private boolean busy = false;

	PipeFluidHandler[] sidedHandlers = {
			new PipeFluidHandler(EnumFacing.DOWN),
			new PipeFluidHandler(EnumFacing.UP),
			new PipeFluidHandler(EnumFacing.NORTH),
			new PipeFluidHandler(EnumFacing.SOUTH),
			new PipeFluidHandler(EnumFacing.WEST),
			new PipeFluidHandler(EnumFacing.EAST)
	};

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		return (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null && sideConfig[facing.ordinal()] == 0)? (T)sidedHandlers[facing.ordinal()] : null;
	}

	@Override
	public void onNeighborBlockChange(BlockPos otherPos) {
		EnumFacing dir = EnumFacing.getFacingFromVector(otherPos.getX() - pos.getX(), otherPos.getY() - pos.getY(), otherPos.getZ() - pos.getZ());
		if(updateConnectionByte(dir)) ITUtils.improvedMarkBlockForUpdate(world, pos, null, EnumSet.complementOf(EnumSet.of(dir)));
	}

	@Override
	public void onLoad() {
		if(!world.isRemote) {
			boolean changed = false;
			for(EnumFacing f : EnumFacing.VALUES) if(world.isBlockLoaded(pos.offset(f))) changed |= updateConnectionByte(f);
			if(changed) ITUtils.improvedMarkBlockForUpdate(world, pos, null);
		}
	}

	@Override
	public boolean hasCover() {
		return pipeCover.isEmpty();
	}

	class PipeFluidHandler implements IFluidHandler {
		EnumFacing origin;
		ArrayList<EnumFacing> outputs = new ArrayList<>();
		HashMap<EnumFacing, PipeFluidHandler> fastFillOutputs = new HashMap<>();

		public PipeFluidHandler(EnumFacing facing) {
			origin = facing;
			for(EnumFacing destination : EnumSet.complementOf(EnumSet.of(facing))) {
				if(hasOutputConnection(facing)) outputs.add(destination);
			}
		}

		@Override
		public IFluidTankProperties[] getTankProperties() {
			return new IFluidTankProperties[]{new FluidTankProperties(null, transferRatePressurized, true, false)};
		}

		private int fastFill(FluidStack resource, boolean doFill) {
			if(busy) return 0;
			int remaining = resource.amount;

			for(EnumFacing facing : outputs) {
				PipeFluidHandler fastFillOutput = fastFillOutputs.get(facing);
				if(fastFillOutput != null) {
					busy = true;
					remaining -= fastFillOutput.fastFill(new FluidStack(resource, remaining), doFill);
					busy = false;
					if(remaining == 0) {
						if(outputs.indexOf(facing) != 0) Collections.swap(outputs, outputs.indexOf(facing),0); //Add some bias forextra TPS juice
						return resource.amount;
					}
					continue;
				}

				TileEntity adjacentTile = Utils.getExistingTileEntity(world, pos.offset(facing));
				if(adjacentTile != null) {
					IFluidHandler handler = adjacentTile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
					if(handler != null) {
						busy = true;
						remaining -= handler.fill(Utils.copyFluidStackWithAmount(resource, remaining, !(handler instanceof IFluidPipe)), doFill);
						busy = false;
						if(handler instanceof PipeFluidHandler) fastFillOutputs.put(facing, (PipeFluidHandler)handler);
						if(remaining == 0) {
							if(outputs.indexOf(facing) != 0) Collections.swap(outputs, outputs.indexOf(facing),0); //Add some bias forextra TPS juice
							return resource.amount;
						}
					}
				}
			}
			return resource.amount - remaining;
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			if(resource == null || resource.amount == 0) return 0;
			return fastFill(new FluidStack(resource, Math.min(resource.amount, getTranferrableAmount(resource))), doFill);
		}

		private int getTranferrableAmount(FluidStack resource) {
			return (resource.tag != null && resource.tag.hasKey("pressurized") || ITContent.normallyPressurized.contains(resource.getFluid())) ? transferRatePressurized : transferRate;
		}

		public void disableSide(EnumFacing side) {
			if(outputs.contains(side)) outputs.remove(side);
			removeFastFill(side);
		}

		public void enableSide(EnumFacing side) {
			if(!outputs.contains(side) && side != origin) outputs.add(side);
		}

		public void removeFastFill(EnumFacing side) {
			fastFillOutputs.put(side, null);
		}

		@Nullable
		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain) {
			return null;
		}

		@Nullable
		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {
			return null;
		}
	}

	public void neighborPipeRemoved(EnumFacing direction) {
		for(PipeFluidHandler handler : sidedHandlers) handler.removeFastFill(direction);
	}

	public void toggleSide(int side) {
		sideConfig[side]++;
		if(sideConfig[side] > 0) sideConfig[side] = - 1;
		markDirty();

		EnumFacing fd = EnumFacing.getFront(side);
		TileEntity connected = world.getTileEntity(getPos().offset(fd));

		if(sideConfig[side] == 0) for(PipeFluidHandler handler : sidedHandlers) handler.enableSide(fd);
		else for(PipeFluidHandler handler : sidedHandlers) handler.disableSide(fd);

		if(connected instanceof TileEntityFluidPipeAlternative) {
			((TileEntityFluidPipeAlternative)connected).sideConfig[fd.getOpposite().ordinal()] = sideConfig[side];
			if(sideConfig[side] == 0) for(PipeFluidHandler handler : ((TileEntityFluidPipeAlternative)connected).sidedHandlers) handler.enableSide(fd.getOpposite());
			else for(PipeFluidHandler handler : ((TileEntityFluidPipeAlternative)connected).sidedHandlers) handler.disableSide(fd.getOpposite());
			connected.markDirty();
			world.addBlockEvent(getPos().offset(fd), getBlockType(), 0, 0);
		}
		world.addBlockEvent(getPos(), getBlockType(), 0, 0);
	}

	@Override
	public int[] getSideConfig() {
		return sideConfig;
	}

	@Override
	public boolean receiveClientEvent(int id, int arg) {
		if(id == 0) {
			ITUtils.improvedMarkBlockForUpdate(world, pos, null);
			return true;
		}
		return false;
	}

	@Override
	public boolean canOutputPressurized(boolean consumePower) {
		return false;
	}

}