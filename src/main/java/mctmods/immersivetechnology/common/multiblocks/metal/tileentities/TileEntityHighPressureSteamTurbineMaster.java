package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.crafting.HighPressureSteamTurbineRecipe;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces.IMechanicalEnergy;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartHighPressureSteamTurbine;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.util.multiblock.PoICache;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.IBinaryMessageReceiver;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TileEntityHighPressureSteamTurbineMaster extends TileEntityHighPressureSteamTurbineSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver, IComparatorOverride {

    private static final int inputTankSize = Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_input_tankSize;
    private static final int outputTankSize = Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_output_tankSize;
    private static final int maxSpeed = Multiblocks.mechanicalEnergy.mechanicalEnergy_speed_max;
    private static final int speedGainPerTick = Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_speed_gainPerTick;
    private static final int speedLossPerTick = Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_speed_lossPerTick;
    private static final float maxRotationSpeed = Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_speed_maxRotation;

    public FluidTank[] tanks = new FluidTank[] {
            new ITFluidTank(inputTankSize, this),
            new ITFluidTank(outputTankSize, this)
    };

    public int fuelBurnRemaining = 0;
    public int speed;
    public MechanicalEnergyAnimation animation = new MechanicalEnergyAnimation();

    private float targetSoundLevel;
    private float soundVolume = 0f;
    private int soundGracePeriod;
    private int tickCountdown = 5;
    private int oldComparatorOutput;

    public HighPressureSteamTurbineRecipe cachedTurbineRecipe;
    private IMechanicalEnergy alternator;

    private boolean needsPoIInit = false;

    protected PoICache fluidInput0, fluidOutput0, mechanicalOutput0, redstone0;
    private BlockPos outputFront0, mechanicalOutputPos0, sound0;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        speed = nbt.getInteger("speed");
        animation.readFromNBT(nbt);
        fuelBurnRemaining = nbt.getInteger("fuelBurnRemaining");
        oldComparatorOutput = nbt.getInteger("oldComparatorOutput");
        if (!descPacket && formed) { needsPoIInit = true; }
        if (world.isRemote) {
            targetSoundLevel = (float)speed / maxSpeed;
            soundVolume = targetSoundLevel;
            soundGracePeriod = 60;
        }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setInteger("speed", speed);
        animation.writeToNBT(nbt);
        nbt.setInteger("fuelBurnRemaining", fuelBurnRemaining);
        nbt.setInteger("oldComparatorOutput", oldComparatorOutput);
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (sound0 == null) { InitializePoIs(); }
        if (soundVolume == 0) { ITSoundHandler.StopSound(sound0); }
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(sound0.getX(), sound0.getY(), sound0.getZ()) / 8, 1);
            float level = ITUtils.remapRange(0, 1, 0.5f, 1.0f, soundVolume);
            ITSounds.turbine.PlayRepeating(sound0, (11 * (level - 0.5f)) / attenuation, level);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        ITSoundHandler.StopSound(sound0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        super.disassemble();
        if (sound0 == null) { InitializePoIs(); }
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(sound0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), sound0.getX(), sound0.getY(), sound0.getZ(), 0));
    }

    public void notifyNearbyClients() { BinaryMessageTileSync.sendToAllTracking(world, getPos(), Unpooled.copyInt(speed)); }

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    @Override public void receiveMessageFromServer(ByteBuf buf) {
        speed = buf.readInt();
        targetSoundLevel = (float)speed / maxSpeed;
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) { }

    @Override public void update() {
        super.update();
        if (!formed) { return; }
        if (needsPoIInit || fluidInput0 == null) {
            InitializePoIs();
            needsPoIInit = false;
        }
        float rotationSpeed = speed == 0 ? 0f : ((float)speed / (float)maxSpeed) * maxRotationSpeed;
        float oldMomentum = animation.getAnimationMomentum();
        animation.setAnimationRotation(animation.getAnimationRotation() + oldMomentum);
        animation.setAnimationMomentum(rotationSpeed);
        if (world.isRemote) {
            if (soundVolume < targetSoundLevel) { soundVolume = Math.min(targetSoundLevel, soundVolume + 0.01f); soundGracePeriod = 60; }
            else if (soundVolume > targetSoundLevel) {
                if (soundGracePeriod > 0) { soundGracePeriod--; }
                else { soundVolume = Math.max(targetSoundLevel, soundVolume - 0.01f); }
            }
            handleSounds();
            return;
        }
        boolean update = false;
        int prevSpeed = speed;
        int prevBurn = fuelBurnRemaining;
        if (fuelBurnRemaining > 0) {
            fuelBurnRemaining--;
            speedUp();
            if (fuelBurnRemaining != prevBurn) { update = true; }
        } else if (!isRSDisabled() && tanks[0].getFluidAmount() > 0 && isValidAlternator()) {
            if (cachedTurbineRecipe == null || !Objects.requireNonNull(tanks[0].getFluid()).isFluidEqual(cachedTurbineRecipe.fluidInput)) { cachedTurbineRecipe = HighPressureSteamTurbineRecipe.findFuel(tanks[0].getFluid()); }
            HighPressureSteamTurbineRecipe recipe = cachedTurbineRecipe;
            if (recipe != null && recipe.fluidInput.amount <= tanks[0].getFluidAmount()) {
                fuelBurnRemaining = recipe.getTotalProcessTime() - 1;
                tanks[0].drain(recipe.fluidInput.amount, true);
                update = true;
                if (recipe.fluidOutput != null) {
                    tanks[1].fill(recipe.fluidOutput, true);
                }
                speedUp();
            } else { speedDown(); }
        } else { speedDown(); }
        if (prevSpeed != speed) { update = true; }
        if (pumpOutputOut()) { update = true; }
        tickCountdown--;
        if (update) {
            efficientMarkDirty();
            markContainingBlockForUpdate(null);
        }
        if (update && tickCountdown <= 0) {
            notifyNearbyClients();
            tickCountdown = 5;
        }
        int comp = getComparatorInputOverride();
        if (comp != oldComparatorOutput) {
            notifyRedstoneNeighbor();
            oldComparatorOutput = comp;
        }
    }

    private void speedUp() { speed = Math.min(maxSpeed, speed + speedGainPerTick); }

    private void speedDown() { speed = Math.max(0, speed - speedLossPerTick); }

    private boolean pumpOutputOut() {
        if (outputFront0 == null) { InitializePoIs(); }
        if (tanks[1].getFluidAmount() == 0) { return false; }
        IFluidHandler handler = FluidUtil.getFluidHandler(world, outputFront0, fluidOutput0.facing.getOpposite());
        if (handler == null) { return false; }
        FluidStack out = tanks[1].getFluid();
        int accepted = handler.fill(out, false);
        if (accepted == 0) { return false; }
        assert out != null;
        int drained = handler.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        tanks[1].drain(drained, true);
        return drained > 0;
    }

    private boolean isValidAlternator() {
        if (mechanicalOutput0 == null) { InitializePoIs(); }
        if (alternator == null || !alternator.isValid()) {
            TileEntity tile = world.getTileEntity(mechanicalOutputPos0);
            if (tile instanceof IMechanicalEnergy) {
                IMechanicalEnergy possibleAlternator = (IMechanicalEnergy)tile;
                if (possibleAlternator.isValid() && possibleAlternator.isMechanicalEnergyReceiver(mechanicalOutput0.facing.getOpposite())) { alternator = possibleAlternator; }
            }
        }
        return alternator != null && alternator.isValid();
    }

    private void notifyRedstoneNeighbor() {
        BlockPos rsPos = getBlockPosForPos(redstone0.position);
        world.notifyNeighborsOfStateChange(rsPos, world.getBlockState(rsPos).getBlock(), false);
    }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartHighPressureSteamTurbine.instance.pointsOfInterest) {
            switch (poi.name) {
                case "fluid_input0":
                    fluidInput0 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_output0":
                    fluidOutput0 = new PoICache(facing, poi, mirrored);
                    outputFront0 = getBlockPosForPos(fluidOutput0.position).offset(fluidOutput0.facing);
                    break;
                case "redstone0":
                    redstone0 = new PoICache(facing, poi, mirrored);
                    break;
                case "mechanical_output0":
                    mechanicalOutput0 = new PoICache(facing, poi, mirrored);
                    mechanicalOutputPos0 = getBlockPosForPos(mechanicalOutput0.position).offset(mechanicalOutput0.facing);
                    break;
                case "sound0":
                    sound0 = getBlockPosForPos(poi.position);
                    break;
            }
        }
        if (!world.isRemote) { notifyIONeighbors(); }
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(fluidInput0.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput0.position));
        notifyNeighbor(getBlockPosForPos(redstone0.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override public void TankContentsChanged() {
        cachedTurbineRecipe = null;
        efficientMarkDirty();
        markContainingBlockForUpdate(null);
    }

    @Override public int getComparatorInputOverride() { return 15 * speed / maxSpeed; }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityHighPressureSteamTurbineMaster master() { return this; }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) { return new int[0]; }
        if (redstone0 == null) { InitializePoIs(); }
        return new int[] {redstone0.position};
    }

    public boolean isMechanicalEnergyTransmitter(@Nullable EnumFacing facing, int position) {
        if (!formed) { return false; }
        if (mechanicalOutput0 == null) { InitializePoIs(); }
        return facing != null && mechanicalOutput0.isPoI(facing, position);
    }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        if (!formed) { return ITUtils.emptyIFluidTankList; }
        if (fluidInput0 == null) { InitializePoIs(); }
        if (side == null) { return tanks; }
        if (fluidInput0.isPoI(side, position)) { return new IFluidTank[] {tanks[0]}; }
        if (fluidOutput0.isPoI(side, position)) { return new IFluidTank[] {tanks[1]}; }
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (fluidInput0 == null) { InitializePoIs(); }
        if (!fluidInput0.isPoI(side, position) || iTank != 0) { return false; }
        if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) { return false; }
        if (tanks[0].getFluid() == null) { return HighPressureSteamTurbineRecipe.findFuelByFluid(resource.getFluid()) != null; }
        return resource.isFluidEqual(tanks[0].getFluid());
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (fluidOutput0 == null) { InitializePoIs(); }
        return fluidOutput0.isPoI(side, position) && iTank == 1 && tanks[1].getFluidAmount() > 0;
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            if (fluidInput0 == null) { InitializePoIs(); }
            return fluidInput0.isPoI(facing, pos) || fluidOutput0.isPoI(facing, pos);
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            if (fluidInput0.isPoI(facing, pos) || fluidOutput0.isPoI(facing, pos)) {
                return (T) new HighPressureSteamTurbineFluidHandler(getAccessibleFluidTanks(facing, pos), this, facing, pos);
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }

    public static class HighPressureSteamTurbineFluidHandler implements IFluidHandler {
        private final IFluidTank[] accessibleTanks;
        private final TileEntityHighPressureSteamTurbineMaster master;
        private final EnumFacing side;
        private final int position;

        public HighPressureSteamTurbineFluidHandler(IFluidTank[] accessibleTanks, TileEntityHighPressureSteamTurbineMaster master, EnumFacing side, int position) {
            this.accessibleTanks = accessibleTanks;
            this.master = master;
            this.side = side;
            this.position = position;
        }

        private int getTankIndex(IFluidTank tank) {
            for (int i = 0; i < master.tanks.length; i++) {
                if (master.tanks[i] == tank) return i;
            }
            return -1;
        }

        @Override public IFluidTankProperties[] getTankProperties() {
            List<IFluidTankProperties> list = new ArrayList<>();
            for (IFluidTank tank : accessibleTanks) {
                int index = getTankIndex(tank);
                boolean canFill = index == 0;
                boolean canDrain = index == 1;
                list.add(new FluidTankProperties(tank.getFluid(), tank.getCapacity(), canFill, canDrain));
            }
            return list.toArray(new IFluidTankProperties[0]);
        }

        @Override public int fill(FluidStack resource, boolean doFill) {
            if (resource == null) { return 0; }
            resource = resource.copy();
            int filled = 0;
            for (IFluidTank accessible : accessibleTanks) {
                int iTank = getTankIndex(accessible);
                if (iTank != -1 && master.canFillTankFrom(iTank, side, resource, position)) {
                    int f = accessible.fill(resource, doFill);
                    filled += f;
                    resource.amount -= f;
                    if (doFill && f > 0) { master.TankContentsChanged(); }
                    if (resource.amount <= 0) { return filled; }
                }
            }
            return filled;
        }

        @Override public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null) { return null; }
            resource = resource.copy();
            FluidStack drained = null;
            for (IFluidTank accessible : accessibleTanks) {
                int iTank = getTankIndex(accessible);
                if (iTank != -1 && master.canDrainTankFrom(iTank, side, position)) {
                    FluidStack tankFluid = accessible.getFluid();
                    if (tankFluid != null && tankFluid.isFluidEqual(resource)) {
                        int amount = Math.min(resource.amount, tankFluid.amount);
                        FluidStack d = accessible.drain(amount, doDrain);
                        if (d != null) {
                            if (drained == null) { drained = d.copy(); }
                            else { drained.amount += d.amount; }
                            resource.amount -= d.amount;
                            if (doDrain && d.amount > 0) { master.TankContentsChanged(); }
                            if (resource.amount <= 0) { return drained; }
                        }
                    }
                }
            }
            return drained;
        }

        @Override public FluidStack drain(int maxDrain, boolean doDrain) {
            int toDrain = maxDrain;
            FluidStack drained = null;
            for (IFluidTank accessible : accessibleTanks) {
                int iTank = getTankIndex(accessible);
                if (iTank != -1 && master.canDrainTankFrom(iTank, side, position)) {
                    FluidStack d = accessible.drain(toDrain, doDrain);
                    if (d != null) {
                        if (drained == null) { drained = d.copy(); }
                        else { drained.amount += d.amount; }
                        toDrain -= d.amount;
                        if (doDrain && d.amount > 0) { master.TankContentsChanged(); }
                        if (toDrain <= 0) { return drained; }
                    }
                }
            }
            return drained;
        }
    }
}
