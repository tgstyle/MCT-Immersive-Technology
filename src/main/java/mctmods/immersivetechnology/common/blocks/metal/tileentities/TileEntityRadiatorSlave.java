package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.api.crafting.RadiatorRecipe;
import mctmods.immersivetechnology.common.blocks.metal.TileEntityMultiblockNewSystem;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockRadiator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;

import java.util.ArrayList;
import java.util.List;

public class TileEntityRadiatorSlave extends TileEntityMultiblockNewSystem<TileEntityRadiatorSlave, RadiatorRecipe, TileEntityRadiatorMaster> implements IEBlockInterfaces.IAdvancedSelectionBounds, IEBlockInterfaces.IAdvancedCollisionBounds {
	public TileEntityRadiatorSlave() {
		super(MultiblockRadiator.instance, new int[] { 7, 9, 1}, 0, false);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
	}

	@Override
	public void update() {
		if(isDummy()) ITUtils.RemoveDummyFromTicking(this);
		super.update();
	}

	@Override
	public boolean isDummy() {
		return true;
	}

	TileEntityRadiatorMaster master;

	public TileEntityRadiatorMaster master() {
		if(master != null && !master.tileEntityInvalid) return master;
		BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
		TileEntity te = Utils.getExistingTileEntity(world, masterPos);
		master = te instanceof TileEntityRadiatorMaster?(TileEntityRadiatorMaster)te: null;
		return master;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack) {
		return false;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public void doGraphicalUpdates(int slot) {
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}

	public NonNullList<ItemStack> getInventory() {return null;}

	@Override
	public IFluidTank[] getInternalTanks() {
		return master() == null? new IFluidTank[0] : master.tanks;
	}

	@Override
	protected RadiatorRecipe readRecipeFromNBT(NBTTagCompound tag) {
		return RadiatorRecipe.loadFromNBT(tag);
	}

	@Override
	public RadiatorRecipe findRecipeForInsertion(ItemStack inserting) {
		return null;
	}

	@Override
	public int[] getEnergyPos() {
		return new int[0];
	}

	@Override
	public int[] getRedstonePos() {
		return new int[] { 0 };
	}

	@Override
	public int[] getOutputSlots() {
		return new int[0];
	}

	@Override
	public int[] getOutputTanks() {
		return new int[] { 1 };
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<RadiatorRecipe> process) {
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output) { }

	@Override
	public void doProcessFluidOutput(FluidStack output) { }

	@Override
	public void onProcessFinish(MultiblockProcess<RadiatorRecipe> process) { }

	@Override
	public int getMaxProcessPerTick() {
		return 1;
	}

	@Override
	public int getProcessQueueMaxLength() {
		return 1;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<RadiatorRecipe> process) {
		return 0;
	}

	@Override
	public boolean isInWorldProcessingMachine() {
		return false;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
		if(master() != null) {
			if (side == null) return master.tanks;
			else if((pos == 27 ) && side == facing.getOpposite()) return new FluidTank[] { master.tanks[0] };
			else if(pos == 35 && side == facing) return new FluidTank[] { master.tanks[1] };
		}
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource) {
		if(master() == null) return false;
		if((pos == 27) && side == facing.getOpposite()) {
			if(master.tanks[iTank].getFluidAmount() >= master.tanks[iTank].getCapacity()) return false;
			if(master.tanks[iTank].getFluid() == null) return RadiatorRecipe.findRecipeByFluid(resource.getFluid()) != null;
			else return resource.getFluid() == master.tanks[iTank].getFluid().getFluid();
		}
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side) {
		return pos == 35 && side == facing;
	}

	@Override
	public TileEntityRadiatorSlave getTileForPos(int targetPos) {
		BlockPos target = getBlockPosForPos(targetPos);
		TileEntity tile = world.getTileEntity(target);
		return tile instanceof TileEntityRadiatorSlave ? (TileEntityRadiatorSlave) tile : null;
	}

	@Override
	public float[]getBlockBounds() {
		return new float[] { 0, 0, 0, 1, 1, 1 };
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds() {
		return getAdvancedSelectionBounds();
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds() {
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		if(mirrored) fl = fl.getOpposite();
		if(mirrored) fw = fw.getOpposite();

		List<AxisAlignedBB> list = Lists.newArrayList();
		if(pos == 27 || pos == 35) {
			list.add(new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		} else if (pos > 27 && pos < 35 ) {
			float minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0.25f : 0f;
			float maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0.75f : 1f;
			float minZ = (fw == EnumFacing.NORTH || fw == EnumFacing.SOUTH) ? 0.25f : 0f;
			float maxZ = (fw == EnumFacing.NORTH || fw == EnumFacing.SOUTH) ? 0.75f : 1f;

			list.add(new AxisAlignedBB(minX, 0.25, minZ, maxX, 0.75, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if (pos != 27 && pos != 35) {
			if (!mirrored) {
				float minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0.375f : 0f;
				float maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0.625f : 1f;
				float minZ = (fw == EnumFacing.NORTH || fw == EnumFacing.SOUTH) ? 0.375f : 0f;
				float maxZ = (fw == EnumFacing.NORTH || fw == EnumFacing.SOUTH) ? 0.625f : 1f;

				list.add(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			} else
				list.add(new AxisAlignedBB(0, 0.375, 0, 1, 0.625, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		return list;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list) {
		return false;
	}

	@Override
	public void disassemble() {
		if (this.formed && !this.world.isRemote) {
			BlockPos startPos = this.getOrigin();
			BlockPos masterPos = this.getPos().add(-this.offset[0], -this.offset[1], -this.offset[2]);
			long time = this.world.getTotalWorldTime();

			for(int yy = 0; yy < this.structureDimensions[this.mirrored ? 2 : 0]; ++yy) {
				for(int ll = 0; ll < this.structureDimensions[1]; ++ll) {
					for(int ww = 0; ww < this.structureDimensions[this.mirrored ? 0 : 2]; ++ww) {
						BlockPos pos = startPos.offset(this.facing, ll).offset(this.facing.rotateY(), ww).add(0, yy, 0);
						ItemStack s = ItemStack.EMPTY;
						TileEntity te = this.world.getTileEntity(pos);
						if (te instanceof TileEntityMultiblockPart) {
							TileEntityMultiblockPart part = (TileEntityMultiblockPart)te;
							Vec3i diff = pos.subtract(masterPos);
							if (part.offset[0] != diff.getX() || part.offset[1] != diff.getY() || part.offset[2] != diff.getZ()) {
								continue;
							}

							if (time != part.onlyLocalDissassembly) {
								s = part.getOriginalBlock();
								part.formed = false;
							}
						}

						if (pos.equals(this.getPos())) {
							s = this.getOriginalBlock();
						}

						IBlockState state = Utils.getStateFromItemStack(s);
						if (state != null) {
							if (pos.equals(this.getPos())) {
								this.world.spawnEntity(new EntityItem(this.world, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, s));
							} else {
								this.replaceStructureBlock(pos, state, s, yy, ll, ww);
							}
						}
					}
				}
			}
		}

	}

	public BlockPos getOrigin() {
		return this.getPos().add(-this.offset[0], -this.offset[1], -this.offset[2]).offset(this.mirrored ? this.facing.rotateYCCW() : EnumFacing.DOWN, 3);
	}

}