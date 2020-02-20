package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDecoration;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.common.Config.ITConfig.SteelTank;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntitySteelSheetmetalTank extends TileEntityMultiblockPart<TileEntitySteelSheetmetalTank> implements IBlockOverlayText, IPlayerInteraction, IComparatorOverride {

	private static int tankSize = SteelTank.steelTank_tankSize;
	
	public FluidTank tank = new FluidTank(tankSize);

	private int[] oldComps = new int[4];
	private int masterCompOld;

	private static final int[] size = {5, 3, 3};

	public TileEntitySteelSheetmetalTank() {
		super(size);
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

	private void updateComparatorValues() {
		int vol = tank.getCapacity() / 6;
		if((15 * tank.getFluidAmount()) / tank.getCapacity() != masterCompOld) world.notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
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
		}
	}

	@Override
	public void update() {
		ApiUtils.checkForNeedlessTicking(this);
		if(world.isRemote || tank.getFluidAmount() == 0) return;
		boolean update = false;
		if(pos == 4 && world.isBlockIndirectlyGettingPowered(getPos()) > 0) {
			for(int index = 0; index < 6; index++) {
				if(index != 1) {
					EnumFacing face = EnumFacing.getFront(index);
					IFluidHandler output = FluidUtil.getFluidHandler(world, getPos().offset(face), face.getOpposite());
					if(output != null) {
						FluidStack accepted = Utils.copyFluidStackWithAmount(tank.getFluid(), Math.min(1000, tank.getFluidAmount()), false);
						accepted.amount = output.fill(Utils.copyFluidStackWithAmount(accepted, accepted.amount, true), false);
						if(accepted.amount > 0) {
							int drained = output.fill(Utils.copyFluidStackWithAmount(accepted, accepted.amount, false), true);
							this.tank.drain(drained, true);
							update=true;
						}
					}
				}
			}
		}
		if(update) {
			updateComparatorValues();
			this.markContainingBlockForUpdate(null);
		}
	}

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer) {
		if(Utils.isFluidRelatedItemStack(player.getHeldItem(EnumHand.MAIN_HAND))) {
			TileEntitySteelSheetmetalTank master = master();
			FluidStack fs = master != null ? master.tank.getFluid() : this.tank.getFluid();
			String s = null;
			if(fs != null) {
				s = fs.getLocalizedName() + " : " + fs.amount + "mB";
			} else {
				s = I18n.format(Lib.GUI + "empty");
			}
			return new String[]{s};
		}
		return null;
	}

	@Override
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop) {
		return false;
	}

	@Override
	public int getComparatorInputOverride() {
		if(pos == 4) return (15 * tank.getFluidAmount()) / tank.getCapacity();
		TileEntitySteelSheetmetalTank master = master();
		if(offset[1] >= 1 && offset[1] <= 4 && master != null) {
			FluidTank t = master.tank;
			int layer = offset[1]-1;
			int vol = t.getCapacity() / 4;
			int filled = t.getFluidAmount()-layer * vol;
			int ret = Math.min(15, Math.max(0, (15 * filled) / vol));
			return ret;
		}
		return 0;
	}

	@Override
	public float[] getBlockBounds() {
		if(pos == 9) return new float[]{.375f, 0, .375f, .625f, 1, .625f};
		if(pos == 0 || pos == 2 || pos == 6 || pos == 8) return new float[]{.375f, 0, .375f, .625f, 1, .625f};
		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@Override
	public ItemStack getOriginalBlock() {
		return pos == 0 || pos == 2 || pos == 6 || pos == 8 ? new ItemStack(IEContent.blockWoodenDecoration, 1, BlockTypes_WoodenDecoration.FENCE.getMeta()) : new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.STEEL.getMeta());
	}

	@Override
	public BlockPos getOrigin() {
		return getPos().add(-offset[0], -offset[1], -offset[2]).offset(facing.rotateYCCW()).offset(facing.getOpposite());
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
		TileEntitySteelSheetmetalTank master = master();
		if(master != null && (pos == 4 || pos == 40)) {
			return new FluidTank[]{master.tank};
		}
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource) {
		return pos == 4 || pos == 40;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side) {
		return pos == 4;
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
		TileEntitySteelSheetmetalTank master = this.master();
		if(master != null) {
			if(FluidUtil.interactWithFluidHandler(player, hand, master.tank)) {
				this.updateMasterBlock(null, true);
				return true;
			}
		}
		return false;
	}

	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderAABB;

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		if(renderAABB == null)
			if(pos == 4) {
				renderAABB = new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(2, 5, 2));
			} else {
				renderAABB = new AxisAlignedBB(getPos(), getPos());
			}
		return renderAABB;
	}

}