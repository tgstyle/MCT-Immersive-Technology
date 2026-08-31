package mctmods.immersivetechnology.common.blocks.connectors.tileentities;

import com.immersiveconvergence.ImmersiveConvergence;
import com.immersiveconvergence.api.network.MessageTileSync;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAttachedIntegerProperies;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorRedstone;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.client.ITGUI;
import mctmods.immersivetechnology.common.blocks.connectors.BlockConnectors;

import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Optional;

public class TileEntityTimer extends TileEntityConnectorRedstone implements IGuiTile, IHammerInteraction, IAttachedIntegerProperies {
    private int lastOutput = 0;
    private int target = 40;
    private int rotation = 0;
    private EnumFacing inputSide;
    private int ioMode = 0;
    private transient int outputClient = 0;

    @Override public void onLoad() {
        super.onLoad();
        if (world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
        inputSide = computeInputSide();
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setInteger("target", target);
        nbt.setInteger("rotation", rotation);
        nbt.setInteger("ioMode", ioMode);
        nbt.setInteger("output", isRSOutput() ? lastOutput : 0);
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        target = nbt.getInteger("target");
        rotation = nbt.getInteger("rotation");
        ioMode = nbt.getInteger("ioMode");
        outputClient = nbt.getInteger("output");
        inputSide = computeInputSide();
    }

    public int getTarget() { return this.target; }

    private void setTarget(int increment) {
        if (increment < 0) {
            int minTarget = 10;
            if (target != minTarget) {
                if (target < 200 && target > 100) { this.target -= 20; }
                else if (target < 100) { this.target -= 10; }
                else { this.target -= 40; }
            }
        } else if (increment > 0) {
            int maxTarget = 600;
            if (target != maxTarget) {
                if (target < 200 && target > 100) { this.target += 20; }
                else if (target < 100) { this.target += 10; }
                else { this.target += 40; }
            }
        }
    }

    private EnumFacing computeInputSide() {
        return facing.getAxis().isVertical() ? EnumFacing.byHorizontalIndex(rotation) : facing.getOpposite();
    }

    public EnumFacing getInputSide() { return inputSide; }

    @Override public void update() {
        if (!world.isRemote) {
            int currentInput;
            if (ioMode == 0) {
                BlockPos neighborPos = pos.offset(inputSide);
                IBlockState neighborState = world.getBlockState(neighborPos);
                currentInput = Math.max(
                        neighborState.getWeakPower(world, neighborPos, inputSide.getOpposite()),
                        neighborState.getStrongPower(world, neighborPos, inputSide.getOpposite())
                );
                if (neighborState.getBlock() == Blocks.REDSTONE_WIRE) {
                    currentInput = Math.max(currentInput, neighborState.getValue(BlockRedstoneWire.POWER));
                }
            } else {
                currentInput = wireNetwork.getPowerOutput(redstoneChannel);
            }

            int desiredOutput = currentInput > 0 && (world.getTotalWorldTime() % (long) target == 0) ? 15 : 0;

            if (desiredOutput != lastOutput) {
                lastOutput = desiredOutput;
                rsDirty = true;
                if (isRSOutput()) onChange();
            }
        }
        super.update();
    }

    @Override public boolean isRSInput() { return ioMode == 0; }

    @Override public boolean isRSOutput() { return ioMode == 1; }

    @Override public void updateInput(@Nonnull byte[] signals) {
        if (isRSInput()) {
            signals[redstoneChannel] = (byte) Math.max(lastOutput, signals[redstoneChannel]);
        }
        rsDirty = false;
    }

    @Override public boolean hammerUseSide(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            redstoneChannel = (redstoneChannel + 1) % 16;
        } else {
            ioMode = ioMode == 0 ? 1 : 0;
        }
        markDirty();
        wireNetwork.updateValues();
        onChange();
        this.markContainingBlockForUpdate(null);
        world.addBlockEvent(getPos(), this.getBlockType(), 254, 0);
        return true;
    }

    @Override public void receiveMessageFromClient(@Nonnull NBTTagCompound message) {
        if (!message.hasKey("buttonId")) return;
        int id = message.getInteger("buttonId");
        setTarget(id == 0 ? 1 : -1);
        markDirty();
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("target", target);
        ImmersiveConvergence.packetHandler.sendToAllTracking(new MessageTileSync(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64));
    }

    @Override public void receiveMessageFromServer(@Nonnull NBTTagCompound message) {
        if (message.hasKey("target")) target = message.getInteger("target");
    }

    @SuppressWarnings("deprecation")
    @Override public Vec3d getRaytraceOffset(IImmersiveConnectable link) {
        EnumFacing side = facing.getOpposite();
        return new Vec3d(.5 + side.getXOffset() * .375, .5 + side.getYOffset() * .375, .5 + side.getZOffset() * .375);
    }

    @Override @Nonnull public Vec3d getConnectionOffset(@Nonnull Connection con) {
        EnumFacing side = facing.getOpposite();
        double conRadius = con.cableType.getRenderDiameter() / 2;
        return new Vec3d(.5 + side.getXOffset() * (.375 - conRadius), .5 + side.getYOffset() * (.375 - conRadius), .5 + side.getZOffset() * (.375 - conRadius));
    }

    @Override @Nonnull public String getCacheKey(@Nonnull IBlockState object) { return redstoneChannel + ";" + ioMode; }

    @Override @Nonnull public String[] getOverlayText(@Nonnull EntityPlayer player, @Nonnull RayTraceResult mop, boolean hammer) {
        if (!hammer) return new String[0];
        float time = (float) this.target / 20;
        EnumDyeColor color = EnumDyeColor.byMetadata(redstoneChannel);
        String channelInfo = I18n.format(Lib.DESC_INFO + "redstoneChannel.send", I18n.format("item.fireworksCharge." + color.getTranslationKey()));
        String modeInfo = I18n.format(Lib.DESC_INFO + "blockSide.io." + this.ioMode);
        String delayInfo = String.format("%.1f Sec.", time);
        return new String[]{channelInfo, modeInfo, delayInfo};
    }

    @Override public boolean useNixieFont(@Nonnull EntityPlayer player, @Nonnull RayTraceResult mop) { return false; }

    @Override public boolean canOpenGui() { return true; }

    @Override public int getGuiID() { return ITGUI.GUIID_Timer; }

    @Override public TileEntity getGuiMaster() { return this; }

    @Override @Nonnull public float[] getBlockBounds() {
        switch (facing) {
            case UP:
            case DOWN:
                return new float[]{.25f, 0, .25f, .75f, 1, .75f};
            case NORTH:
            case SOUTH:
                return new float[]{.25f, 0, 0, .75f, .75f, 1};
            case EAST:
            case WEST:
                return new float[]{0, .25f, .25f, 1, .75f, .75f};
        }
        return new float[]{0, 0, 0, 1, 1, 1};
    }

    @SideOnly(Side.CLIENT)
    @Override public TextureAtlasSprite getTextureReplacement(IBlockState object, String material) { return null; }

    @SideOnly(Side.CLIENT)
    @Override public boolean shouldRenderGroup(@Nonnull IBlockState object, @Nonnull String group) {
        if (MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.SOLID) return false;
        if ("glass".equals(group)) return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT;
        return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.CUTOUT;
    }

    @SideOnly(Side.CLIENT)
    @Override public int getRenderColour(@Nonnull IBlockState object, @Nonnull String group) {
        if ("colour_out".equals(group)) return 0xff000000 | EnumDyeColor.byMetadata(this.redstoneChannel).getColorValue();
        return 0xffffffff;
    }

    @SideOnly(Side.CLIENT)
    @Override public Optional<TRSRTransformation> applyTransformations(@Nonnull IBlockState object, String group, Optional<TRSRTransformation> transform) {
        EnumFacing facing = object.getValue(IEProperties.FACING_ALL);
        int rot = object.getValue(BlockConnectors.ROTATION);
        int angleX = facing.getAxis() == EnumFacing.Axis.Y ? (facing == EnumFacing.DOWN ? 0 : 180) : -90;
        int angleY = rot * 90;

        TRSRTransformation rotation = TRSRTransformation.from(ModelRotation.getModelRotation(angleX, angleY));
        return transform.map(t -> Optional.of(rotation.compose(t))).orElseGet(() -> Optional.of(rotation));
    }

    @Override @Nonnull public String[] getIntPropertyNames() { return new String[]{"rotation"}; }

    @Override @Nonnull public PropertyInteger getIntProperty(@Nonnull String name) { return BlockConnectors.ROTATION; }

    @Override public int getIntPropertyValue(@Nonnull String name) { return rotation; }

    @Override public void setValue(@Nonnull String name, int value) { this.rotation = value; inputSide = computeInputSide(); }

    @Override public int getStrongRSOutput(@Nonnull IBlockState state, @Nonnull EnumFacing side) {
        if (!isRSOutput() || side != inputSide) return 0;
        if (world.isRemote) return outputClient;
        return lastOutput;
    }

    @Override public int getWeakRSOutput(@Nonnull IBlockState state, @Nonnull EnumFacing side) {
        if (!isRSOutput()) return 0;
        if (world.isRemote) return outputClient;
        return lastOutput;
    }
}
