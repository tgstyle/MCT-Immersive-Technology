package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import com.immersiveconvergence.api.capability.IMechanicalEnergyProvider;
import com.immersiveconvergence.api.client.MechanicalEnergyAnimation;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.client.gui.GuiRotorCreative;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces.IBlockBounds;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Utils;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityRotorCreative extends TileEntityIEBase implements ITickable, IMechanicalEnergyProvider, IDirectionalTile, IPlayerInteraction, IBlockBounds {
    private static int maxSpeed() { return Multiblocks.mechanicalEnergy.mechanicalEnergy_speed_max; }

    public EnumFacing facing = EnumFacing.NORTH;
    public int rpm = maxSpeed();
    private final MechanicalEnergyAnimation animation = new MechanicalEnergyAnimation();

    @Override public void update() {
        if (!world.isRemote) return;
        float step = Math.abs(rpm) / (float)maxSpeed() * 72f * Math.signum(rpm);
        animation.setAnimationMomentum(step);
        animation.setAnimationRotation((animation.getAnimationRotation() + step) % 360f);
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        rpm = nbt.getInteger("rpm");
        facing = EnumFacing.values()[nbt.getInteger("facing")];
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        nbt.setInteger("rpm", rpm);
        nbt.setInteger("facing", facing.ordinal());
    }

    @Override public boolean isValid() { return true; }

    @Override public boolean isMechanicalEnergyTransmitter(EnumFacing side) { return side.getAxis() == facing.getAxis(); }

    @Override public int getSpeed() { return rpm; }

    @Override public float getTorqueMultiplier() { return 1f; }

    @Override public MechanicalEnergyAnimation getAnimation() { return animation; }

    @Override public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        if (!world.isRemote && !Utils.isHammer(heldItem)) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("rpm", rpm);
            ImmersiveTechnology.packetHandler.sendTo(new MessageTileSync(this, tag), (EntityPlayerMP)player);
            return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override public void receiveMessageFromServer(@Nonnull NBTTagCompound message) {
        if (message.hasKey("rpm")) {
            rpm = message.getInteger("rpm");
            Minecraft.getMinecraft().displayGuiScreen(new GuiRotorCreative(this));
        }
    }

    @Override public void receiveMessageFromClient(@Nonnull NBTTagCompound message) {
        if (!message.hasKey("rpm")) return;
        rpm = Math.max(Math.min(message.getInteger("rpm"), maxSpeed()), -maxSpeed());
        markDirty();
        markContainingBlockForUpdate(null);
    }

    @Override public float[] getBlockBounds() {
        if (facing.getAxis() == EnumFacing.Axis.X) { return new float[]{0, .125f, .125f, 1, .875f, .875f}; }
        return new float[]{.125f, .125f, 0, .875f, .875f, 1};
    }

    @Override @Nonnull public EnumFacing getFacing() { return facing; }

    @Override public void setFacing(@Nonnull EnumFacing facing) { this.facing = facing; }

    @Override public int getFacingLimitation() { return 2; }

    @Override public boolean mirrorFacingOnPlacement(@Nonnull EntityLivingBase placer) { return false; }

    @Override public boolean canHammerRotate(@Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull EntityLivingBase entity) { return true; }

    @Override public boolean canRotate(@Nonnull EnumFacing axis) { return true; }

    @Override @Nonnull public EnumFacing getFacingForPlacement(@Nonnull EntityLivingBase placer, @Nonnull BlockPos pos, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) { return placer.getHorizontalFacing(); }
}
