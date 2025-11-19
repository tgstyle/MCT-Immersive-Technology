package mctmods.immersivetechnology.common.blocks.connectors.tileentities;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorRedstone;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.ITGUI;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
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

public class TileEntityTimer extends TileEntityConnectorRedstone implements IGuiTile {
    private int lastOutput = 0;
    private int target = 40;
    private int tick = 0;
    private int lastInput = 0;

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setInteger("target", target);
        nbt.setInteger("tick", tick);
        nbt.setInteger("lastInput", lastInput);
    }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        target = nbt.getInteger("target");
        tick = nbt.getInteger("tick");
        lastInput = nbt.getInteger("lastInput");
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
        this.tick = 0;
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            int currentInput = world.getRedstonePower(getPos(), facing.getOpposite());
            if (currentInput > 0) {
                if (tick == target) {
                    this.lastOutput = 15;
                    this.tick = 0;
                    this.rsDirty = true;
                } else {
                    this.tick++;
                    if (this.lastOutput == 15) {
                        this.lastOutput = 0;
                        this.rsDirty = true;
                    }
                }
            } else {
                if (this.lastOutput == 15) {
                    this.lastOutput = 0;
                    this.rsDirty = true;
                }
                this.tick = 0;
            }
        }
        super.update();
    }

    @Override
    public boolean isRSInput() { return true; }

    @Override
    public boolean isRSOutput() { return false; }

    @Override
    public void updateInput(@Nonnull byte[] signals) {
        signals[redstoneChannel] = (byte) Math.max(lastOutput, signals[redstoneChannel]);
        rsDirty = false;
    }

    @Override
    public boolean hammerUseSide(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, float hitX, float hitY, float hitZ) {
        redstoneChannel = (redstoneChannel + 1) % 16;
        markDirty();
        wireNetwork.updateValues();
        onChange();
        this.markContainingBlockForUpdate(null);
        world.addBlockEvent(getPos(), this.getBlockType(), 254, 0);
        return true;
    }

    @Override
    public void receiveMessageFromClient(@Nonnull NBTTagCompound message) {
        if (!message.hasKey("buttonId")) return;
        int id = message.getInteger("buttonId");
        setTarget(id == 0 ? 1 : -1);
        markDirty();
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("target", target);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageTileSync(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
    }

    @Override
    public void receiveMessageFromServer(@Nonnull NBTTagCompound message) {
        if (!message.hasKey("target")) return;
        target = message.getInteger("target");
    }

    @Override
    @SuppressWarnings("deprecation")
    public Vec3d getRaytraceOffset(IImmersiveConnectable link) {
        EnumFacing side = facing.getOpposite();
        return new Vec3d(.5 + side.getXOffset() * .375, .5 + side.getYOffset() * .375, .5 + side.getZOffset() * .375);
    }

    @Override
    public @Nonnull Vec3d getConnectionOffset(@Nonnull Connection con) {
        EnumFacing side = facing.getOpposite();
        double conRadius = con.cableType.getRenderDiameter() / 2;
        return new Vec3d(.5 + side.getXOffset() * (.375 - conRadius), .5 + side.getYOffset() * (.375 - conRadius), .5 + side.getZOffset() * (.375 - conRadius));
    }

    @Override
    public @Nonnull String getCacheKey(@Nonnull IBlockState object) { return redstoneChannel + ""; }

    @Override
    public @Nonnull String[] getOverlayText(@Nonnull EntityPlayer player, @Nonnull RayTraceResult mop, boolean hammer) {
        if (!hammer) return new String[0];
        float time = (float) this.target / 20;
        return new String[] {I18n.format(Lib.DESC_INFO + "redstoneChannel.send", I18n.format("item.fireworksCharge." + EnumDyeColor.byMetadata(redstoneChannel).getTranslationKey())), I18n.format(time + " Sec.")};
    }

    @Override
    public boolean useNixieFont(@Nonnull EntityPlayer player, @Nonnull RayTraceResult mop) { return false; }

    @Override
    public boolean canOpenGui() { return true; }

    @Override
    public int getGuiID() { return ITGUI.GUIID_Timer; }

    @Override
    public TileEntity getGuiMaster() { return this; }

    @Override
    public @Nonnull float[] getBlockBounds() {
        switch(facing) {
            case UP:
            case DOWN:
                return new float[] {.25f, 0, .25f, .75f, 1, .75f};
            case NORTH:
                return new float[] {.25f, 0, 0, .75f, .75f, 1};
            case SOUTH:
                return new float[] {.25f, .25f, 0, .75f, 1, 1};
            case EAST:
                return new float[] {0, .25f, .25f, 1, .75f, .75f};
            case WEST:
                return new float[] {0, .25f, .25f, 1, .75f, .75f};
        }
        return new float[] {0, 0, 0, 1, 1, 1};
    }

    @SideOnly(Side.CLIENT)
    @Override
    public TextureAtlasSprite getTextureReplacement(IBlockState object, String material) { return null; }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldRenderGroup(@Nonnull IBlockState object, @Nonnull String group) {
        if (MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.SOLID) return false;
        if ("glass".equals(group)) return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT;
        return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.CUTOUT;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderColour(@Nonnull IBlockState object, @Nonnull String group) {
        if ("colour_out".equals(group)) return 0xff000000 | EnumDyeColor.byMetadata(this.redstoneChannel).getColorValue();
        return 0xffffffff;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Optional<TRSRTransformation> applyTransformations(@Nonnull IBlockState object, String group, Optional<TRSRTransformation> transform) {
        TRSRTransformation rotation;
        EnumFacing f = facing;
        int angleX;
        int angleY = 0;
        if (f.getAxis() == EnumFacing.Axis.Y) {
            angleX = f == EnumFacing.UP ? 180 : 0;
        } else {
            angleX = -90;
            angleY = (int) f.getHorizontalAngle() - 180;
        }
        rotation = TRSRTransformation.from(ModelRotation.getModelRotation(angleX, angleY));
        return transform.map(trsrTransformation -> Optional.of(rotation.compose(trsrTransformation))).orElseGet(() -> Optional.of(rotation));
    }
}
