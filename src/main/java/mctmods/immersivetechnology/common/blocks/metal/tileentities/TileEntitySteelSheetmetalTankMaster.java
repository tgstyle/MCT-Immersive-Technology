package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.SteelTank;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class TileEntitySteelSheetmetalTankMaster extends TileEntitySteelSheetmetalTankSlave implements ITFluidTank.TankListener {

	private static int tankSize = SteelTank.steelTank_tankSize;
	private static int transferSpeed = SteelTank.steelTank_transferSpeed;

	private int[] oldComps = new int[4];
	private int masterCompOld;
	private int sleep = 0;

	public ITFluidTank tank = new ITFluidTank(tankSize, this);

	@Override
	public boolean isDummy() {
		return false;
	}

	@Override
	public TileEntitySteelSheetmetalTankMaster master() {
		master = this;
		return this;
	}

	@Override
	public void update() {
		if(world.isRemote || tank.getFluidAmount() == 0) return;
		if(world.isBlockIndirectlyGettingPowered(getPos()) > 0) {
			for(int index = 0; index < 6; index++) {
				if(index != 1) {
					EnumFacing face = EnumFacing.getFront(index);
					IFluidHandler output = FluidUtil.getFluidHandler(world, getPos().offset(face), face.getOpposite());
					if(output != null) {
						if(sleep == 0) {
							FluidStack accepted = Utils.copyFluidStackWithAmount(tank.getFluid(), Math.min(transferSpeed, tank.getFluidAmount()), false);
							if(accepted == null) {
								sleep = 20;
								return;
							}
							accepted.amount = output.fill(Utils.copyFluidStackWithAmount(accepted, accepted.amount, true), false);
							if(accepted.amount > 0) {
								int drained = output.fill(Utils.copyFluidStackWithAmount(accepted, accepted.amount, false), true);
								tank.drain(drained, true);
								sleep = 0;
							} else {
								sleep = 20;
							}
						} else {
							sleep--;
						}
					}
				}
			}
		}
	}

	@Override
	public void TankContentsChanged() {
		updateComparatorValues();
		this.markContainingBlockForUpdate(null);
	}

	private void updateComparatorValues() {
		int vol = tank.getCapacity() / 6;
		int currentValue = (15 * tank.getFluidAmount()) / tank.getCapacity();
		if(currentValue != masterCompOld) world.notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
		masterCompOld = currentValue;
		for(int i = 0; i < 4; i++) {
			int filled = tank.getFluidAmount()-i * vol;
			int now = Math.min(15, Math.max((15 * filled) / vol, 0));
			if(now != oldComps[i]) {
				for(int x = -1; x <= 1; x++) {
					for(int z = -1; z <= 1; z++) {
						BlockPos pos = getPos().add(-offset[0] + x, -offset[1] + i + 1, -offset[2] + z);
						world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), true);
					}
				}
			}
			oldComps[i] = now;
		}
	}

	@Override
	public int getComparatorInputOverride() {
		return (15 * tank.getFluidAmount()) / tank.getCapacity();
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		tank.readFromNBT(nbt.getCompoundTag("tank"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		nbt.setTag("tank", tankTag);
	}
}