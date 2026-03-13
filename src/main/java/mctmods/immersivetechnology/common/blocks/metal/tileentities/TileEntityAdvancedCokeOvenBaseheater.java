package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.PropertyBoolInverted;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IActiveState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IUsesBooleanProperty;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityAdvancedCokeOvenSlave;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class TileEntityAdvancedCokeOvenBaseheater extends TileEntityIEBase implements IIEInternalFluxHandler, IDirectionalTile, IHasDummyBlocks, IActiveState, ITickable {
    private static final int cokeOvenConsumption = Multiblocks.advancedCokeOvenBaseheater.advancedCokeOvenBaseheater_energy_consumption;
    public EnumFacing facing = EnumFacing.NORTH;
    public FluxStorage energyStorage = new FluxStorage(8000);
    public boolean dummy = false;
    public boolean active = false;
    public BlockPos masterPos;

    private float soundVolume = 0f;
    private int soundGracePeriod = 0;
    private boolean isRunning = false;

    @SideOnly(Side.CLIENT) private float fanRotation;
    @SideOnly(Side.CLIENT) private float prevFanRotation;

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    @Override public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        dummy = nbt.getBoolean("dummy");
        facing = EnumFacing.values()[nbt.getInteger("facing")];
        energyStorage.readFromNBT(nbt);
        active = nbt.getBoolean("active");
        isRunning = nbt.getBoolean("isRunning");
        soundGracePeriod = nbt.getInteger("soundGracePeriod");
        if (descPacket) { this.markContainingBlockForUpdate(null); }
    }

    @Override public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        nbt.setBoolean("dummy", dummy);
        nbt.setInteger("facing", facing.ordinal());
        nbt.setBoolean("active", active);
        nbt.setBoolean("isRunning", isRunning);
        nbt.setInteger("soundGracePeriod", soundGracePeriod);
        energyStorage.writeToNBT(nbt);
    }

    public boolean doSpeedup() {
        if (dummy) { return false; }
        int consumed = cokeOvenConsumption;
        boolean didWork = false;
        if (this.energyStorage.extractEnergy(consumed, true) == consumed) {
            this.energyStorage.extractEnergy(consumed, false);
            didWork = true;
            if (!active) {
                active = true;
                this.markContainingBlockForUpdate(null);
                updateDummies();
            }
        }
        if (didWork) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;
        if (!didWork && soundGracePeriod == 0 && active) {
            active = false;
            this.markContainingBlockForUpdate(null);
            updateDummies();
        }
        boolean wasRunning = isRunning;
        isRunning = soundGracePeriod > 0;
        if (isRunning != wasRunning) {
            markContainingBlockForUpdate(null);
        }
        return didWork;
    }

    public void updateDummies() {
        if (world.isRemote) { return; }
        BlockPos dummyPos = getPos().offset(facing.rotateY());
        IBlockState dummyState = world.getBlockState(dummyPos);
        world.notifyBlockUpdate(dummyPos, dummyState, dummyState, 3);
        dummyPos = getPos().offset(facing.rotateYCCW());
        dummyState = world.getBlockState(dummyPos);
        world.notifyBlockUpdate(dummyPos, dummyState, dummyState, 3);
    }

    @Override @Nonnull public SideConfig getEnergySideConfig(EnumFacing facing) {
        return !dummy && facing == EnumFacing.UP ? SideConfig.INPUT : SideConfig.NONE;
    }

    IEForgeEnergyWrapper wrapper = new IEForgeEnergyWrapper(this, EnumFacing.UP);

    @Override public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing) {
        if (!dummy && facing == EnumFacing.UP) { return wrapper; }
        return null;
    }

    @Override public void placeDummies(BlockPos pos, @Nonnull IBlockState state, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) {
        BlockPos dummyPos = pos.offset(facing.rotateY());
        world.setBlockState(dummyPos, state);
        TileEntityAdvancedCokeOvenBaseheater dummyTE = (TileEntityAdvancedCokeOvenBaseheater)world.getTileEntity(dummyPos);
        assert dummyTE != null;
        dummyTE.dummy = true;
        dummyTE.facing = facing.rotateY();
        dummyTE.markContainingBlockForUpdate(null);
        dummyPos = pos.offset(facing.rotateYCCW());
        world.setBlockState(dummyPos, state);
        dummyTE = (TileEntityAdvancedCokeOvenBaseheater)world.getTileEntity(dummyPos);
        assert dummyTE != null;
        dummyTE.dummy = true;
        dummyTE.facing = facing.rotateYCCW();
        dummyTE.markContainingBlockForUpdate(null);
    }

    @Override public void breakDummies(@Nonnull BlockPos pos, @Nonnull IBlockState state) {
        if (dummy) {
            if (masterPos == null) { findMaster(); }
            TileEntity tile = world.getTileEntity(masterPos);
            if (tile instanceof TileEntityAdvancedCokeOvenBaseheater) {
                ((TileEntityAdvancedCokeOvenBaseheater)tile).breakDummies(masterPos, world.getBlockState(masterPos));
            }
        } else {
            ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(getPos()), new NetworkRegistry.TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 0));
            BlockPos dummyPos0 = getPos().offset(facing.rotateY());
            TileEntityAdvancedCokeOvenBaseheater dummy0 = (TileEntityAdvancedCokeOvenBaseheater)world.getTileEntity(dummyPos0);
            BlockPos dummyPos1 = getPos().offset(facing.rotateYCCW());
            TileEntityAdvancedCokeOvenBaseheater dummy1 = (TileEntityAdvancedCokeOvenBaseheater)world.getTileEntity(dummyPos1);
            if (dummy0 != null) { world.setBlockToAir(dummyPos0); }
            if (dummy1 != null) { world.setBlockToAir(dummyPos1); }
            world.setBlockToAir(getPos());
        }
    }

    @Override public boolean isDummy() { return dummy; }

    @Override @Nonnull public EnumFacing getFacing() { return facing; }

    @Override public void setFacing(@Nonnull EnumFacing facing) { this.facing = facing; }

    @Override public int getFacingLimitation() { return 2; }

    @Override public boolean mirrorFacingOnPlacement(@Nonnull EntityLivingBase placer) { return false; }

    @Override public boolean canHammerRotate(@Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull EntityLivingBase entity) { return false; }

    @Override public boolean canRotate(@Nonnull EnumFacing axis) { return false; }

    @Override @Nonnull public EnumFacing getFacingForPlacement(@Nonnull EntityLivingBase placer, @Nonnull BlockPos pos, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) { return placer.getHorizontalFacing(); }

    public void findMaster() {
        if (!dummy) {
            masterPos = getPos();
            return;
        }
        TileEntity tile = world.getTileEntity(getPos().offset(facing, -1));
        if (tile instanceof TileEntityAdvancedCokeOvenBaseheater && ((TileEntityAdvancedCokeOvenBaseheater)tile).isMaster(this)) {
            masterPos = getPos().offset(facing, -1);
            return;
        }
        tile = world.getTileEntity(getPos().offset(facing.rotateY()));
        if (tile instanceof TileEntityAdvancedCokeOvenBaseheater && ((TileEntityAdvancedCokeOvenBaseheater)tile).isMaster(this)) {
            masterPos = getPos().offset(facing.rotateY());
            facing = facing.rotateYCCW();
            this.markContainingBlockForUpdate(null);
            return;
        }
        tile = world.getTileEntity(getPos().offset(facing.rotateYCCW()));
        if (tile instanceof TileEntityAdvancedCokeOvenBaseheater && ((TileEntityAdvancedCokeOvenBaseheater)tile).isMaster(this)) {
            masterPos = getPos().offset(facing.rotateYCCW());
            facing = facing.rotateY();
            this.markContainingBlockForUpdate(null);
            return;
        }
        masterPos = getPos();
    }

    private boolean isMaster(TileEntityAdvancedCokeOvenBaseheater requester) {
        if (dummy || requester == null) { return false; }
        BlockPos dummyPos = getPos().offset(facing.rotateY());
        if (requester.getPos().equals(dummyPos)) { return true; }
        dummyPos = getPos().offset(facing.rotateYCCW());
        return requester.getPos().equals(dummyPos);
    }

    @Override @Nonnull public FluxStorage getFluxStorage() {
        if (dummy) {
            if (masterPos == null) { findMaster(); }
            TileEntity tile = world.getTileEntity(masterPos);
            if (tile instanceof TileEntityAdvancedCokeOvenBaseheater) { return ((TileEntityAdvancedCokeOvenBaseheater)tile).getFluxStorage(); }
        }
        return energyStorage;
    }

    @Override public boolean getIsActive() { return active; }

    @Override @Nonnull public PropertyBoolInverted getBoolProperty(@Nonnull Class<? extends IUsesBooleanProperty> inf) { return IEProperties.BOOLEANS[0]; }

    @Override public void update() {
        if (!world.isRemote && !dummy && active) {
            BlockPos attachedPos = getPos().offset(facing);
            TileEntity te = world.getTileEntity(attachedPos);
            if (!(te instanceof TileEntityAdvancedCokeOvenSlave) || !((TileEntityAdvancedCokeOvenSlave)te).formed) {
                active = false;
                soundGracePeriod = 0;
                isRunning = false;
                markContainingBlockForUpdate(null);
                updateDummies();
            }
        }
        if (world.isRemote && !dummy) {
            prevFanRotation = fanRotation;
            if (active) {
                fanRotation += 35f;
                fanRotation %= 360;
            }
            handleSounds();
        }
    }

    @SideOnly(Side.CLIENT)
    public float getFanRotation(float partialTicks) {
        return prevFanRotation + (fanRotation - prevFanRotation) * partialTicks;
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        float targetSoundLevel = isRunning ? 1f : 0f;
        if (soundVolume < targetSoundLevel) { soundVolume = Math.min(soundVolume + 0.01f, targetSoundLevel); }
        else if (soundVolume > targetSoundLevel) { soundVolume = Math.max(soundVolume - 0.01f, targetSoundLevel); }
        if (soundVolume <= 0f) { ITSoundHandler.StopSound(getPos()); }
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(getPos().getX() + .5, getPos().getY() + .5, getPos().getZ() + .5) / 8, 1);
            float level = ITUtils.remapRange(0, 1, 0.5f, 1.0f, soundVolume);
            ITSounds.advancedCokeOvenFan.PlayRepeating(getPos(), (5 * soundVolume) / attenuation, level);
        }
    }

    @Override public void onChunkUnload() {
        if (world.isRemote) { ITSoundHandler.StopSound(getPos()); }
        super.onChunkUnload();
    }
}
