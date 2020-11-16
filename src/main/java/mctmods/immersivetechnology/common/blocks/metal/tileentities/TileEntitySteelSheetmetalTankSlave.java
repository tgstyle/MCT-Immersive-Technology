package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDecoration;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntitySteelSheetmetalTankSlave extends TileEntityMultiblockPart<TileEntitySteelSheetmetalTankSlave> implements IEBlockInterfaces.IBlockOverlayText, IEBlockInterfaces.IPlayerInteraction, IEBlockInterfaces.IComparatorOverride {

	private static final int[] size = {5, 3, 3};

	public TileEntitySteelSheetmetalTankSlave() {
		super(size);
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
		ITUtils.RemoveDummyFromTicking(this);
	}

	@Override
	public boolean isDummy() {
		return true;
	}

	TileEntitySteelSheetmetalTankMaster master;

	public TileEntitySteelSheetmetalTankMaster master() {
		if(master != null && !master.tileEntityInvalid) return master;
		BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
		TileEntity te = Utils.getExistingTileEntity(world, masterPos);
		master = te instanceof TileEntitySteelSheetmetalTankMaster?(TileEntitySteelSheetmetalTankMaster)te: null;
		return master;
	}

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer) {
		if(Utils.isFluidRelatedItemStack(player.getHeldItem(EnumHand.MAIN_HAND))) {
			FluidStack fs = master() != null ? master.tank.getFluid() : null;
			return (fs != null)?
				new String[]{TranslationKey.OVERLAY_STEEL_TANK_NORMAL_FIRST_LINE.format(fs.getLocalizedName(), fs.amount)}:
				new String[]{TranslationKey.GUI_EMPTY.text()};
		}
		return null;
	}

	@Override
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop) {
		return false;
	}

	@Override
	public int getComparatorInputOverride() {
		if(offset[1] >= 1 && offset[1] <= 4 && master() != null) {
			int layer = offset[1]-1;
			int vol = master.tank.getCapacity() / 4;
			int filled = master.tank.getFluidAmount()-layer * vol;
			return Math.min(15, Math.max(0, (15 * filled) / vol));
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
		if(master() != null && (side == null || pos == 4 || pos == 40)) return new FluidTank[]{master.tank};
		return new FluidTank[0];
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if(capability== CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&this.getAccessibleFluidTanks(facing).length > 0)
			return true;
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&this.getAccessibleFluidTanks(facing).length > 0)
			return (T)new MultiblockFluidWrapper(this, facing);
		return super.getCapability(capability, facing);
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
		if(master() != null) {
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