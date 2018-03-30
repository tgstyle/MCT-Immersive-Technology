package ferro2000.immersivetech.common.blocks.metal.tileentities;

import java.util.List;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorRedstone;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAssembler.CrafterPatternInventory;
import ferro2000.immersivetech.api.ITLib;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityTimer extends TileEntityConnectorRedstone implements IGuiTile {

	private int redstoneChannelSending = 0;
	private int lastOutput = 0;
	private int target = 40;
	private int tick = 0;
	private int increment = 0;
	private final int maxTarget = 600;
	private final int minTarget = 10;

	@Override
	public void update() {

		if (!world.isRemote) {

			BlockPos pos = this.getPos().offset(EnumFacing.SOUTH);

			if (increment != 0) {

				setTarget();
				this.increment = 0;
				this.tick = 0;

			}

			if (!stopTimer(pos)) {

				if (tick == target) {

					this.lastOutput = 1;
					this.tick = 0;
					this.rsDirty = true;

				} else {

					this.tick++;

					if (this.lastOutput == 1) {
						this.lastOutput = 0;
					}

				}

			}

		}

		super.update();

	}

	public int getTarget() {
		return this.target;
	}

	private void setTarget() {

		if (increment < 0) {

			if (target != minTarget) {

				if (target < 200 && target > 100) {
					this.target -= 20;
				} else if (target < 100) {
					this.target -= 10;
				} else {
					this.target -= 40;
				}

			}

		} else if (increment > 0) {

			if (target != maxTarget) {

				if (target < 200 && target > 100) {
					this.target += 20;
				} else if (target < 100) {
					this.target += 10;
				} else {
					this.target += 40;
				}

			}

		}

	}

	private boolean stopTimer(BlockPos pos) {

		EnumFacing f = facing == EnumFacing.SOUTH ? EnumFacing.UP
				: facing == EnumFacing.NORTH ? EnumFacing.DOWN
						: facing == EnumFacing.WEST ? EnumFacing.NORTH : EnumFacing.SOUTH;

		if (world.isSidePowered(pos, f)) {
			return true;
		}

		return false;

	}

	@Override
	public boolean isRSInput() {
		return true;
	}

	@Override
	public boolean isRSOutput() {
		return true;
	}

	private EntityItemFrame findItemFrame(World world, final EnumFacing facing, BlockPos pos) {
		List<EntityItemFrame> list = world.getEntitiesWithinAABB(EntityItemFrame.class, new AxisAlignedBB(pos),
				entity -> entity != null && entity.getHorizontalFacing() == facing);
		return list.size() == 1 ? list.get(0) : null;
	}

	@Override
	public void updateInput(byte[] signals) {
		signals[redstoneChannelSending] = (byte) Math.max(lastOutput, signals[redstoneChannelSending]);
		rsDirty = false;
	}

	@Override
	public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ) {
		redstoneChannelSending = (redstoneChannelSending + 1) % 16;
		markDirty();
		wireNetwork.updateValues();
		onChange();
		this.markContainingBlockForUpdate(null);
		world.addBlockEvent(getPos(), this.getBlockType(), 254, 0);
		return true;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("redstoneChannelSending", redstoneChannelSending);
		nbt.setInteger("target", target);
		nbt.setInteger("increment", increment);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		redstoneChannelSending = nbt.getInteger("redstoneChannelSending");
		target = nbt.getInteger("target");
		increment = nbt.getInteger("increment");
	}

	@Override
	public void receiveMessageFromClient(NBTTagCompound message) {

		if (message.hasKey("buttonId")) {

			int id = message.getInteger("buttonId");
			if (id == 0) {
				this.increment = 1;
			} else if (id == 1) {
				this.increment = -1;
			}

		}

	}

	@Override
	public Vec3d getRaytraceOffset(IImmersiveConnectable link) {
		EnumFacing side = facing.getOpposite();
		return new Vec3d(.5 + side.getFrontOffsetX() * .375, .5 + side.getFrontOffsetY() * .375,
				.5 + side.getFrontOffsetZ() * .375);
	}

	@Override
	public Vec3d getConnectionOffset(Connection con) {
		EnumFacing side = facing.getOpposite();
		double conRadius = con.cableType.getRenderDiameter() / 2;
		return new Vec3d(.5 + side.getFrontOffsetX() * (.375 - conRadius),
				.5 + side.getFrontOffsetY() * (.375 - conRadius), .5 + side.getFrontOffsetZ() * (.375 - conRadius));
	}

	@Override
	public float[] getBlockBounds() {

		switch (facing) {

		case NORTH:
			return new float[] { .25f, 0, 0, .75f, .75f, 1 };
		case SOUTH:
			return new float[] { .25f, .25f, 0, .75f, 1, 1 };
		case EAST:
			return new float[] { 0, .25f, .25f, 1, .75f, 1 };
		case WEST:
			return new float[] { 0, .25f, 0, 1, .75f, .75f };
		default:
			return new float[] { .25f, 0, .25f, .75f, 1, 1 };

		}

	}

	@SideOnly(Side.CLIENT)
	@Override
	public TextureAtlasSprite getTextureReplacement(IBlockState object, String material) {
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldRenderGroup(IBlockState object, String group) {
		if (MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.SOLID)
			return false;
		if ("glass".equals(group))
			return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT;
		return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.CUTOUT;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderColour(IBlockState object, String group) {
		if ("colour_out".equals(group))
			return 0xff000000 | EnumDyeColor.byMetadata(this.redstoneChannelSending).getColorValue();
		return 0xffffffff;
	}

	@Override
	public String getCacheKey(IBlockState object) {
		return redstoneChannel + ";" + redstoneChannelSending;
	}

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer) {
		if (!hammer)
			return null;
		float time = (float) this.target / 20;

		System.out.println("time: " + time);
		System.out.println("target: " + target);

		return new String[] {
				I18n.format(Lib.DESC_INFO + "redstoneChannel.send",
						I18n.format("item.fireworksCharge."
								+ EnumDyeColor.byMetadata(redstoneChannelSending).getUnlocalizedName())),
				I18n.format(String.valueOf(time).toString() + " Sec.") };
	}

	@Override
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop) {
		return false;
	}

	@Override
	public boolean canOpenGui() {
		return true;
	}

	@Override
	public int getGuiID() {
		return ITLib.GUIID_Timer;
	}

	@Override
	public TileEntity getGuiMaster() {
		return this;
	}

}
