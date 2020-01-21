package ferro2000.immersivetech.common.blocks.metal.tileentities;

import javax.annotation.Nonnull;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;

import ferro2000.immersivetech.common.Config.ITConfig;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

public class TileEntityCokeOvenPreheater extends TileEntityIEBase implements IIEInternalFluxHandler, IDirectionalTile, IHasDummyBlocks {
	public EnumFacing facing = EnumFacing.NORTH;

	public FluxStorage energyStorage = new FluxStorage(8000);

	public boolean dummy = false;
	public boolean active = false;

	private int dummyNum = 0;

	public int doSpeedup() {
		int consumed = ITConfig.Machines.cokeOvenPreheater_consumption;
		if(this.energyStorage.extractEnergy(consumed, true) == consumed) {
			if(!active) {
				active = true;
				this.markContainingBlockForUpdate(null);
			}
			this.energyStorage.extractEnergy(consumed, false);
			return 1;
		} else if(active) {
			active = false;
			this.markContainingBlockForUpdate(null);
		}
		return 0;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		dummy = nbt.getBoolean("dummy");
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		energyStorage.readFromNBT(nbt);
		active = nbt.getBoolean("active");
		if(descPacket) this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		nbt.setBoolean("dummy", dummy);
		nbt.setInteger("facing", facing.ordinal());
		nbt.setBoolean("active", active);
		energyStorage.writeToNBT(nbt);
	}

	@Nonnull
	@Override
	public SideConfig getEnergySideConfig(EnumFacing facing) {
		return !dummy && facing == EnumFacing.UP ? SideConfig.INPUT : SideConfig.NONE;
	}

	IEForgeEnergyWrapper wrapper = new IEForgeEnergyWrapper(this, EnumFacing.UP);

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing) {
		if(!dummy && facing == EnumFacing.UP) {
			return wrapper;
		}
		return null;
	}

	@Override
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ) {
		EnumFacing dummyDir = facing.getAxis() == Axis.X ? EnumFacing.NORTH : EnumFacing.WEST;
		BlockPos[] dummyPos = new BlockPos[2];
		dummyPos[0] = pos.offset(dummyDir);
		dummyPos[1] = pos.offset(dummyDir.getOpposite());
		for(int i = 0; i < 2; i++) {
			world.setBlockState(dummyPos[i], state);
			((TileEntityCokeOvenPreheater) world.getTileEntity(dummyPos[i])).dummy = true;
			((TileEntityCokeOvenPreheater) world.getTileEntity(dummyPos[i])).facing = this.facing;
			((TileEntityCokeOvenPreheater) world.getTileEntity(dummyPos[i])).dummyNum = i + 1;
		}
	}

	@Override
	public void breakDummies(BlockPos pos, IBlockState state) {
		EnumFacing dummyDir = facing.getAxis() == Axis.X ? EnumFacing.NORTH : EnumFacing.WEST;
		BlockPos[] dummyPos = new BlockPos[2];
		dummyPos[0] = pos.offset(dummyDir);
		dummyPos[1] = pos.offset(dummyDir.getOpposite());
		for(int i = 0; i < 2; i++) {
			if(world.getTileEntity(dummyPos[i]) instanceof TileEntityCokeOvenPreheater) {
				world.setBlockToAir(dummyPos[i]);
			}
		}
	}

	@Override
	public boolean isDummy() {
		return dummy;
	}

	@Override
	public EnumFacing getFacing() {
		return facing;
	}

	@Override
	public void setFacing(EnumFacing facing) {
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation() {
		return 2;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer) {
		return false;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity) {
		return false;
	}

	@Override
	public boolean canRotate(EnumFacing axis) {
		return false;
	}

	@Override
	public FluxStorage getFluxStorage() {
		if(dummyNum > 0) {
			EnumFacing dummyDir = facing.getAxis() == Axis.X ? EnumFacing.NORTH : EnumFacing.WEST;
			TileEntity tile;
			if(dummyNum == 1) {
				tile = world.getTileEntity(getPos().offset(dummyDir.getOpposite(), 1));
			} else {
				tile = world.getTileEntity(getPos().offset(dummyDir, 1));
			}
			if(tile instanceof TileEntityCokeOvenPreheater) return ((TileEntityCokeOvenPreheater)tile).getFluxStorage();
		}
		return energyStorage;
	}

}