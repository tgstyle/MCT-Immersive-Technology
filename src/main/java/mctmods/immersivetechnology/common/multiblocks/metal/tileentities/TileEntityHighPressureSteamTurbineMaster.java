package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;


import com.immersiveconvergence.ImmersiveConvergence;
import com.immersiveconvergence.api.capability.IMechanicalEnergyConsumer;
import com.immersiveconvergence.api.capability.RotationInertiaProcess;
import com.immersiveconvergence.api.client.ICSoundHandler;
import com.immersiveconvergence.api.client.MechanicalEnergyAnimation;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.IComparatorOverride;
import com.immersiveconvergence.api.multiblock.PoICache;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.network.BinaryTileSyncMessage;
import com.immersiveconvergence.api.network.IBinaryMessageReceiver;
import com.immersiveconvergence.api.network.MessageStopSound;
import com.immersiveconvergence.api.util.ICFluidTank;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.core.ICCommonConfig;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.api.crafting.HighPressureSteamTurbineRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartHighPressureSteamTurbine;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Objects;

public class TileEntityHighPressureSteamTurbineMaster extends TileEntityHighPressureSteamTurbineSlave implements ICFluidTank.TankListener, IBinaryMessageReceiver, IComparatorOverride {

    private static int inputTankSize() { return Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_input_tankSize; }
    private static int outputTankSize() { return Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_output_tankSize; }
    public static int maxSpeed() { return Math.round(ICCommonConfig.mechanical.maxRpm * Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_speed_maxFactor); }
    private RotationInertiaProcess inertia;
    private double connectedMass = -1;
    private double connectedFriction = -1;
    private int inertiaMax = -1;
    private int effectiveMaxSpeed = maxSpeed();
    private transient int fanFadeIn = 0;
    private transient boolean wasRunningClient = false;
    private boolean speedChanged = false;

    private RotationInertiaProcess inertia(double mass, double friction, int effectiveMax) {
        if (inertia == null || mass != connectedMass || friction != connectedFriction || effectiveMax != inertiaMax) {
            connectedMass = mass;
            connectedFriction = friction;
            inertiaMax = effectiveMax;
            inertia = new RotationInertiaProcess(Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_baseMass + mass, Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_driveTorque, Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_friction + friction, effectiveMax);
        }
        return inertia;
    }
    private static float maxRotationSpeed() { return Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_speed_maxRotation; }

    private static final int FAN_FADE_IN_TICKS = 80;

    public FluidTank[] tanks = new FluidTank[] {new ICFluidTank(inputTankSize(), this), new ICFluidTank(outputTankSize(), this)};
    private float accumConsume = 0f;
    public float currentTorque = Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_torque;
    private float outAccum = 0f;
    public int speed = 0;
    public MechanicalEnergyAnimation animation = new MechanicalEnergyAnimation();

    private float currentLevel = 0f;
    private float currentPitch = 0f;
    private int soundGracePeriod = 0;
    private boolean isRunning = false;
    private int tickCountdown = 5;
    private int oldComparatorOutput = 0;
    private boolean active = false;
    private boolean wasEnabled = false;
    private int pressureReleaseCooldown = 0;
    private float effectiveRatio = 0f;
    private double accumDelta = 0;

    public HighPressureSteamTurbineRecipe cachedTurbineRecipe;
    private IMechanicalEnergyConsumer alternator;

    private boolean needsPoIInit = false;
    private boolean needsNotify = false;

    protected PoICache fluidInputPos0, fluidOutputPos0, mechanicalOutputPos0, redstonePos0;
    private BlockPos fluidOutputTEPos0, mechanicalOutputTEPos0, soundPos0;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        speed = nbt.getInteger("speed");
        if (!descPacket) animation.readFromNBT(nbt);
        accumConsume = nbt.getFloat("accumConsume");
        outAccum = nbt.getFloat("outAccum");
        oldComparatorOutput = nbt.getInteger("oldComparatorOutput");
        soundGracePeriod = nbt.getInteger("soundGracePeriod");
        effectiveRatio = nbt.getFloat("effectiveRatio");
        if (!descPacket && formed) {
            needsPoIInit = true;
            needsNotify = true;
        }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setInteger("speed", speed);
        if (!descPacket) animation.writeToNBT(nbt);
        nbt.setFloat("accumConsume", accumConsume);
        nbt.setFloat("outAccum", outAccum);
        nbt.setInteger("oldComparatorOutput", oldComparatorOutput);
        nbt.setInteger("soundGracePeriod", soundGracePeriod);
        nbt.setFloat("effectiveRatio", effectiveRatio);
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (soundPos0 == null) InitializePoIs();
        float targetLevel = ITUtils.remapRange(0, effectiveMaxSpeed, 0.5f, 1.0f, speed);
        if (currentLevel == 0f) { currentLevel = targetLevel; }
        else { currentLevel = currentLevel * 0.9f + targetLevel * 0.1f; }
        float targetPitch = ITUtils.remapRange(0, effectiveMaxSpeed, 0.5f, 1.5f, speed);
        if (currentPitch == 0f) { currentPitch = targetPitch; }
        else { currentPitch = currentPitch * 0.95f + targetPitch * 0.05f; }
        if (currentPitch < 0.5f) { currentPitch = 0.5f; }
        if (!isRunning) { ICSoundHandler.stopSound(soundPos0); }
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos0.getX() + .5, soundPos0.getY() + .5, soundPos0.getZ() + .5) / 32f, 1f);
            ITSounds.steamTurbine.PlayRepeating(soundPos0, (11f * (currentLevel - 0.5f)) / attenuation, currentPitch);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        if (soundPos0 != null) ICSoundHandler.stopSound(soundPos0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        super.disassemble();
        if (soundPos0 == null) InitializePoIs();
        if (!world.isRemote) {
            ImmersiveConvergence.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0),
                    new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
        }
    }

    private void notifyNearbyClients() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(speed);
        buf.writeInt(effectiveMaxSpeed);
        buf.writeBoolean(isRunning);
        BinaryTileSyncMessage.sendToAllTracking(world, getPos(), buf);
    }

    @Override public void receiveMessageFromServer(ByteBuf buf) {
        if (buf.readableBytes() == 1 && buf.readByte() == 1) {
            if (soundPos0 == null) InitializePoIs();
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos0.getX() + .5, soundPos0.getY() + .5, soundPos0.getZ() + .5) / 8f, 1f);
            ITSounds.pressureRelease.PlayOnce(soundPos0, 1 / attenuation, 1);
        }
        else {
            speed = buf.readInt();
            effectiveMaxSpeed = buf.readInt();
            isRunning = buf.readBoolean();
        }
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {}

    @Override public void update() {
        super.update();
        if (!formed) return;

        if (needsPoIInit || fluidInputPos0 == null || mechanicalOutputPos0 == null || redstonePos0 == null || soundPos0 == null) {
            InitializePoIs();
            needsPoIInit = false;
        }
        if (needsNotify) {
            notifyIONeighbors();
            needsNotify = false;
        }

        if (world.isRemote) {
            if (isRunning && !wasRunningClient) fanFadeIn = FAN_FADE_IN_TICKS;
            wasRunningClient = isRunning;
            float rotationSpeed = speed == 0 || effectiveMaxSpeed <= 0 ? 0f : ((float)speed / effectiveMaxSpeed) * maxRotationSpeed();
            if (fanFadeIn > 0) {
                rotationSpeed -= ((float)fanFadeIn / FAN_FADE_IN_TICKS) * rotationSpeed;
                fanFadeIn--;
            }
            float oldMomentum = animation.getAnimationMomentum();
            animation.setAnimationRotation(animation.getAnimationRotation() + oldMomentum);
            animation.setAnimationMomentum(rotationSpeed);
            handleSounds();
            return;
        }

        boolean changed = false;
        speedChanged = false;
        int prevSpeed = speed;
        boolean wasActive = active;
        active = false;
        boolean currentlyEnabled = !isRSDisabled();

        boolean canRun = currentlyEnabled && isValidAlternator();
        float ratio = 0f;
        if (canRun) { currentTorque = Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_torque; }
        if (canRun && tanks[0].getFluidAmount() > 0) {
            FluidStack fluid = tanks[0].getFluid();

            HighPressureSteamTurbineRecipe recipe;
            if (cachedTurbineRecipe != null && Objects.requireNonNull(fluid).isFluidEqual(cachedTurbineRecipe.fluidInput)) {
                recipe = cachedTurbineRecipe;
            } else {
                recipe = HighPressureSteamTurbineRecipe.findFuel(fluid);
                cachedTurbineRecipe = recipe;
            }

            if (recipe != null && recipe.getTotalProcessTime() > 0) {
                currentTorque = recipe.torque;
                float fluidPerTick = (float)recipe.fluidInput.amount / recipe.getTotalProcessTime();
                accumConsume += fluidPerTick;
                int toDrain = (int)accumConsume;
                if (toDrain > 0) {
                    FluidStack drainedStack = tanks[0].drain(toDrain, true);
                    int drained = drainedStack == null ? 0 : drainedStack.amount;
                    accumConsume -= drained;
                    ratio = drained / fluidPerTick;
                    if (drained > 0) { changed = true; }
                    if (recipe.fluidOutput != null && drained > 0) {
                        outAccum += ratio * ((float)recipe.fluidOutput.amount / recipe.getTotalProcessTime());
                        if (outAccum >= 1) {
                            FluidStack out = recipe.fluidOutput.copy();
                            out.amount = (int)outAccum;
                            outAccum -= tanks[1].fill(out, true);
                        }
                    }
                }
            }
        }

        applyInertia(canRun, ratio);
        active = effectiveRatio > 0.001f;
        if (active != wasActive) { changed = true; }

        if (pressureReleaseCooldown > 0) { pressureReleaseCooldown--; }
        boolean triggerRelease = (!wasActive && active) || (!wasEnabled && currentlyEnabled);
        if (triggerRelease && pressureReleaseCooldown <= 0) {
            BinaryTileSyncMessage.sendToAllTracking(world, getPos(), Unpooled.buffer(1).writeByte(1));
            pressureReleaseCooldown = 200;
        }
        wasEnabled = currentlyEnabled;

        if (speed != prevSpeed || speedChanged) changed = true;
        if (pumpOutputOut()) changed = true;

        boolean didWork = speed > 0;
        if (didWork) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;
        boolean wasRunning = isRunning;
        isRunning = soundGracePeriod > 0;

        if (changed) {
            this.markDirty();
            if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
            else { throttledBlockUpdate(); }
        }
        if ((changed || isRunning != wasRunning) && tickCountdown-- <= 0) {
            notifyNearbyClients();
            tickCountdown = 5;
            this.markDirty();
        }

        int comparator = comparatorValue();
        if (comparator != oldComparatorOutput) {
            oldComparatorOutput = comparator;
            notifyComparators();
        }
    }

    private boolean pumpOutputOut() {
        if (fluidOutputTEPos0 == null) InitializePoIs();
        if (tanks[1].getFluidAmount() == 0) return false;
        IFluidHandler handler = FluidUtil.getFluidHandler(world, fluidOutputTEPos0, fluidOutputPos0.facing.getOpposite());
        if (handler == null) return false;
        FluidStack out = tanks[1].getFluid();
        if (out == null) return false;
        int accepted = handler.fill(out, false);
        if (accepted <= 0) return false;
        int drained = handler.fill(ICUtils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        tanks[1].drain(drained, true);
        return drained > 0;
    }

    private void applyInertia(boolean canRun, float ratio) {
        boolean connected = isValidAlternator();
        double mass = connected ? alternator.getMass() : 0;
        double friction = connected ? alternator.getFriction() : 0;
        int effectiveMax = connected ? Math.min(maxSpeed(), alternator.getMaxSpeed()) : maxSpeed();
        if (effectiveMaxSpeed != effectiveMax) { effectiveMaxSpeed = effectiveMax; speedChanged = true; }
        effectiveRatio = effectiveRatio * 0.9f + ratio * 0.1f;
        accumDelta += inertia(mass, friction, effectiveMax).getAlpha(canRun ? effectiveRatio : 0f, speed);
        int delta = (int)Math.round(accumDelta);
        accumDelta -= delta;
        speed = Math.max(0, Math.min(effectiveMax, speed + delta));
    }

    private boolean isValidAlternator() {
        if (mechanicalOutputPos0 == null) InitializePoIs();
        if (alternator == null || !alternator.isValid()) {
            TileEntity te = world.getTileEntity(mechanicalOutputTEPos0);
            if (te instanceof IMechanicalEnergyConsumer) {
                IMechanicalEnergyConsumer possible = (IMechanicalEnergyConsumer)te;
                if (possible.isValid() && possible.isMechanicalEnergyReceiver(mechanicalOutputPos0.facing.getOpposite())) alternator = possible;
                else alternator = null;
            } else alternator = null;
        }
        return alternator != null && alternator.isValid();
    }

    void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartHighPressureSteamTurbine.instance.pointsOfInterest) {
            switch (poi.name) {
                case "fluid_input0":
                    fluidInputPos0 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_output0":
                    fluidOutputPos0 = new PoICache(facing, poi, mirrored);
                    fluidOutputTEPos0 = getBlockPosForPos(fluidOutputPos0.position).offset(fluidOutputPos0.facing);
                    break;
                case "redstone0":
                    redstonePos0 = new PoICache(facing, poi, mirrored);
                    break;
                case "mechanical_output0":
                    mechanicalOutputPos0 = new PoICache(facing, poi, mirrored);
                    mechanicalOutputTEPos0 = getBlockPosForPos(mechanicalOutputPos0.position).offset(mechanicalOutputPos0.facing);
                    break;
                case "sound0":
                    soundPos0 = getBlockPosForPos(poi.position);
                    break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        if (fluidInputPos0 != null) world.notifyNeighborsOfStateChange(getBlockPosForPos(fluidInputPos0.position), getBlockType(), true);
        if (fluidOutputPos0 != null) world.notifyNeighborsOfStateChange(getBlockPosForPos(fluidOutputPos0.position), getBlockType(), true);
        notifyComparators();
    }

    @Override public void TankContentsChanged() {
        cachedTurbineRecipe = null;
        this.markDirty();
        requestClientSync();
    }

    @Override public boolean isRSDisabled() {
        int[] rsPositions = getRedstonePos();
        if (rsPositions.length < 1) return false;
        for (int rsPos : rsPositions) {
            TileEntity tile = world.getTileEntity(getBlockPosForPos(rsPos));
            if (tile != null) {
                int power = world.getRedstonePowerFromNeighbors(tile.getPos());
                return redstoneControlInverted != (power > 0);
            }
        }
        return false;
    }

    @Override public int getComparatorInputOverride() { return isComparatorPos() ? comparatorValue() : 0; }

    public int comparatorValue() { return effectiveMaxSpeed <= 0 ? 0 : 15 * speed / effectiveMaxSpeed; }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityHighPressureSteamTurbineMaster master() { return this; }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return ITUtils.EMPTY_INT_ARRAY;
        if (redstonePos0 == null) InitializePoIs();
        return new int[]{toFlatIndex(redstonePos0.position)};
    }

    public boolean isMechanicalEnergyTransmitter(@Nullable EnumFacing facing, BlockPos position) {
        if (!formed) return false;
        if (mechanicalOutputPos0 == null) InitializePoIs();
        return facing != null && mechanicalOutputPos0.isPoI(facing, position);
    }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, BlockPos position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (fluidInputPos0 == null) InitializePoIs();
        if (side == null) return tanks;
        if (fluidInputPos0.isPoI(side, position)) return tankView(0, tanks[0]);
        if (fluidOutputPos0.isPoI(side, position)) return tankView(1, tanks[1]);
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        if (fluidInputPos0 == null) InitializePoIs();
        if (!fluidInputPos0.isPoI(side, position) || iTank != 0) return false;
        if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
        if (tanks[0].getFluid() == null) { return true; }
        return resource.isFluidEqual(tanks[0].getFluid());
    }

    @Override protected boolean isInputFluidPoI(BlockPos position) {
        if (fluidInputPos0 == null) { InitializePoIs(); }
        return fluidInputPos0.position.equals(position);
    }

    @Override protected int clearInputTanks() {
        tanks[0].drain(Integer.MAX_VALUE, true);
        TankContentsChanged();
        return 1;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        if (fluidOutputPos0 == null) InitializePoIs();
        return fluidOutputPos0.isPoI(side, position) && iTank == 1 && tanks[1].getFluidAmount() > 0;
    }
}
