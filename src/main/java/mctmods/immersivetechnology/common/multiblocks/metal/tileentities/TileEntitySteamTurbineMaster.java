package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.crafting.SteamTurbineRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSteamTurbine;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces.IMechanicalEnergy;
import mctmods.immersivetechnology.common.util.multiblock.PoICache;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.network.IBinaryMessageReceiver;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;

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

public class TileEntitySteamTurbineMaster extends TileEntitySteamTurbineSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver, IComparatorOverride {

    private static final int inputTankSize = Multiblocks.steamTurbine.steamTurbine_input_tankSize;
    private static final int outputTankSize = Multiblocks.steamTurbine.steamTurbine_output_tankSize;
    private static final int maxSpeed = Multiblocks.mechanicalEnergy.mechanicalEnergy_speed_max;
    private static final int speedGainPerTick = Multiblocks.steamTurbine.steamTurbine_speed_gainPerTick;
    private static final int speedLossPerTick = Multiblocks.steamTurbine.steamTurbine_speed_lossPerTick;
    private static final float maxRotationSpeed = Multiblocks.steamTurbine.steamTurbine_speed_maxRotation;

    public FluidTank[] tanks = new FluidTank[] {new ITFluidTank(inputTankSize, this), new ITFluidTank(outputTankSize, this)};
    public int fuelBurnRemaining = 0;
    public int speed = 0;
    public MechanicalEnergyAnimation animation = new MechanicalEnergyAnimation();

    private float targetSoundLevel;
    private float soundVolume = 0f;
    private int soundGracePeriod;
    private int tickCountdown = 5;
    private int oldComparatorOutput = 0;

    public SteamTurbineRecipe cachedTurbineRecipe;
    private IMechanicalEnergy alternator;

    private boolean needsPoIInit = false;
    private boolean needsNotify = false;

    protected PoICache fluidInput0, fluidOutput0, mechanicalOutput0, redstone0;
    private BlockPos outputFront0, mechanicalOutputPos0, sound0;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        speed = nbt.getInteger("speed");
        if (!descPacket) animation.readFromNBT(nbt);
        fuelBurnRemaining = nbt.getInteger("fuelBurnRemaining");
        oldComparatorOutput = nbt.getInteger("oldComparatorOutput");
        if (!descPacket && formed) {
            needsPoIInit = true;
            needsNotify = true;
        }
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
        if (!descPacket) animation.writeToNBT(nbt);
        nbt.setInteger("fuelBurnRemaining", fuelBurnRemaining);
        nbt.setInteger("oldComparatorOutput", oldComparatorOutput);
    }

    @SideOnly(Side.CLIENT)
    private void handleSounds() {
        if (sound0 == null) InitializePoIs();
        if (soundVolume == 0f) ITSoundHandler.StopSound(sound0);
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(sound0.getX() + .5, sound0.getY() + .5, sound0.getZ() + .5) / 8f, 1f);
            float level = ITUtils.remapRange(0f, 1f, 0.5f, 1.0f, soundVolume);
            ITSounds.turbine.PlayRepeating(sound0, (11f * (level - 0.5f)) / attenuation, level);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        if (sound0 != null) ITSoundHandler.StopSound(sound0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        super.disassemble();
        if (sound0 == null) InitializePoIs();
        if (!world.isRemote) {
            ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(sound0),
                    new NetworkRegistry.TargetPoint(world.provider.getDimension(), sound0.getX(), sound0.getY(), sound0.getZ(), 0));
        }
    }

    private void notifyNearbyClients() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(speed);
        BinaryMessageTileSync.sendToAllTracking(world, getPos(), buf);
    }

    @Override public void receiveMessageFromServer(ByteBuf buf) {
        speed = buf.readInt();
        targetSoundLevel = (float)speed / maxSpeed;
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {}

    @Override public void update() {
        super.update();
        if (!formed) return;

        if (needsPoIInit || fluidInput0 == null || mechanicalOutput0 == null || redstone0 == null || sound0 == null) {
            InitializePoIs();
            needsPoIInit = false;
        }
        if (needsNotify) {
            notifyIONeighbors();
            needsNotify = false;
        }

        if (world.isRemote) {
            float rotationSpeed = speed == 0 ? 0f : ((float)speed / maxSpeed) * maxRotationSpeed;
            float oldMomentum = animation.getAnimationMomentum();
            animation.setAnimationRotation(animation.getAnimationRotation() + oldMomentum);
            animation.setAnimationMomentum(rotationSpeed);

            if (soundVolume < targetSoundLevel) {
                soundVolume = Math.min(targetSoundLevel, soundVolume + 0.01f);
                soundGracePeriod = 60;
            } else if (soundVolume > targetSoundLevel) {
                if (soundGracePeriod > 0) soundGracePeriod--;
                else soundVolume = Math.max(targetSoundLevel, soundVolume - 0.01f);
            }
            handleSounds();
            return;
        }

        boolean changed = false;
        int prevSpeed = speed;

        if (fuelBurnRemaining > 0) {
            fuelBurnRemaining--;
            speed = Math.min(maxSpeed, speed + speedGainPerTick);
            changed = true;
        } else if (!isRSDisabled() && tanks[0].getFluidAmount() > 0 && isValidAlternator()) {
            FluidStack fluid = tanks[0].getFluid();

            SteamTurbineRecipe recipe;
            if (cachedTurbineRecipe != null && Objects.requireNonNull(fluid).isFluidEqual(cachedTurbineRecipe.fluidInput)) {
                recipe = cachedTurbineRecipe;
            } else {
                recipe = SteamTurbineRecipe.findFuel(fluid);
                cachedTurbineRecipe = recipe;
            }

            if (recipe != null && recipe.fluidInput.amount <= tanks[0].getFluidAmount()) {
                fuelBurnRemaining = recipe.getTotalProcessTime() - 1;
                tanks[0].drain(recipe.fluidInput.amount, true);
                if (recipe.fluidOutput != null) tanks[1].fill(recipe.fluidOutput, true);
                speed = Math.min(maxSpeed, speed + speedGainPerTick);
                changed = true;
            } else {
                speed = Math.max(0, speed - speedLossPerTick);
            }
        } else {
            speed = Math.max(0, speed - speedLossPerTick);
        }

        if (speed != prevSpeed) changed = true;
        if (pumpOutputOut()) changed = true;

        if (changed) {
            this.markDirty();
            markContainingBlockForUpdate(null);
        }
        if (changed && tickCountdown-- <= 0) {
            notifyNearbyClients();
            tickCountdown = 5;
            this.markDirty();
        }

        int comparator = getComparatorInputOverride();
        if (comparator != oldComparatorOutput) {
            oldComparatorOutput = comparator;
            if (redstone0 != null) {
                BlockPos rsPos = getBlockPosForPos(redstone0.position);
                world.updateComparatorOutputLevel(rsPos, getBlockType());
            }
        }
    }

    private boolean pumpOutputOut() {
        if (outputFront0 == null) InitializePoIs();
        if (tanks[1].getFluidAmount() == 0) return false;
        IFluidHandler handler = FluidUtil.getFluidHandler(world, outputFront0, fluidOutput0.facing.getOpposite());
        if (handler == null) return false;
        FluidStack out = tanks[1].getFluid();
        if (out == null) return false;
        int accepted = handler.fill(out, false);
        if (accepted <= 0) return false;
        int drained = handler.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        tanks[1].drain(drained, true);
        return drained > 0;
    }

    private boolean isValidAlternator() {
        if (mechanicalOutput0 == null) InitializePoIs();
        if (alternator == null || !alternator.isValid()) {
            TileEntity te = world.getTileEntity(mechanicalOutputPos0);
            if (te instanceof IMechanicalEnergy) {
                IMechanicalEnergy possible = (IMechanicalEnergy)te;
                if (possible.isValid() && possible.isMechanicalEnergyReceiver(mechanicalOutput0.facing.getOpposite())) alternator = possible;
                else alternator = null;
            } else alternator = null;
        }
        return alternator != null && alternator.isValid();
    }

    void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartSteamTurbine.instance.pointsOfInterest) {
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
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        if (fluidInput0 != null) world.notifyNeighborsOfStateChange(getBlockPosForPos(fluidInput0.position), getBlockType(), true);
        if (fluidOutput0 != null) world.notifyNeighborsOfStateChange(getBlockPosForPos(fluidOutput0.position), getBlockType(), true);
        if (redstone0 != null) world.updateComparatorOutputLevel(getBlockPosForPos(redstone0.position), getBlockType());
    }

    @Override public void TankContentsChanged() {
        cachedTurbineRecipe = null;
        this.markDirty();
        markContainingBlockForUpdate(null);
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

    @Override public int getComparatorInputOverride() { return 15 * speed / maxSpeed; }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntitySteamTurbineMaster master() { return this; }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstone0 == null) InitializePoIs();
        return new int[]{redstone0.position};
    }

    public boolean isMechanicalEnergyTransmitter(@Nullable EnumFacing facing, int position) {
        if (!formed) return false;
        if (mechanicalOutput0 == null) InitializePoIs();
        return facing != null && mechanicalOutput0.isPoI(facing, position);
    }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (fluidInput0 == null) InitializePoIs();
        if (side == null) return tanks;
        if (fluidInput0.isPoI(side, position)) return new IFluidTank[]{tanks[0]};
        if (fluidOutput0.isPoI(side, position)) return new IFluidTank[]{tanks[1]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (fluidInput0 == null) InitializePoIs();
        if (!fluidInput0.isPoI(side, position) || iTank != 0) return false;
        if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
        if (tanks[0].getFluid() == null) return SteamTurbineRecipe.findFuelByFluid(resource.getFluid()) != null;
        return resource.isFluidEqual(tanks[0].getFluid());
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (fluidOutput0 == null) InitializePoIs();
        return fluidOutput0.isPoI(side, position) && iTank == 1 && tanks[1].getFluidAmount() > 0;
    }
}
