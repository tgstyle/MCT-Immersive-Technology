package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.api.crafting.HighPressureSteamTurbineRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.blocks.ITBlockInterfaces.IMechanicalEnergy;
import mctmods.immersivetechnology.common.blocks.multiblocks.TileEntityITMultiblockPartHighPressureSteamTurbine;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.multiblock.PoICache;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.IBinaryMessageReceiver;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
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

public class TileEntityHighPressureSteamTurbineMaster extends TileEntityHighPressureSteamTurbineSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver, IComparatorOverride {
    private static final int inputTankSize = Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_input_tankSize;
    private static final int outputTankSize = Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_output_tankSize;
    private static final int maxSpeed = Multiblocks.mechanicalEnergy.mechanicalEnergy_speed_max;
    private static final int speedGainPerTick = Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_speed_gainPerTick;
    private static final int speedLossPerTick = Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_speed_lossPerTick;
    private static final float maxRotationSpeed = Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_speed_maxRotation;

    public FluidTank[] tanks = new FluidTank[] { new ITFluidTank(inputTankSize, this), new ITFluidTank(outputTankSize, this) };

    public int burnRemaining = 0;
    public int speed;

    private HighPressureSteamTurbineRecipe cachedRecipe;

    MechanicalEnergyAnimation animation = new MechanicalEnergyAnimation();

    IMechanicalEnergy alternator;

    private int clientUpdateCooldown = 1;
    private float targetLevel;
    private float soundVolume;
    private int oldSpeed;

    private PoICache input, output, redstone, mechanicalOutput;
    private BlockPos soundOrigin, mechanicalOutputFront;
    private BlockPos fluidOutputPos;

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        speed = nbt.getInteger("speed");
        animation.readFromNBT(nbt);
        burnRemaining = nbt.getInteger("burnRemaining");
    }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setInteger("speed", speed);
        animation.writeToNBT(nbt);
        nbt.setInteger("burnRemaining", burnRemaining);
    }

    private void speedUp() { speed = Math.min(maxSpeed, speed + speedGainPerTick); }

    private void speedDown() { speed = Math.max(0, speed - speedLossPerTick); }

    private boolean pumpOutputOut() {
        if (tanks[1].getFluidAmount() == 0) return false;
        if (output == null) InitializePoIs();
        IFluidHandler handler = FluidUtil.getFluidHandler(world, fluidOutputPos, output.facing.getOpposite());
        if (handler == null) return false;
        FluidStack out = tanks[1].getFluid();
        int accepted = handler.fill(out, false);
        if (accepted == 0) return false;
        assert out != null;
        int drained = handler.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        this.tanks[1].drain(drained, true);
        return drained > 0;
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (soundOrigin == null) { InitializePoIs(); if (soundOrigin == null) return; }
        if (soundVolume == 0) { ITSoundHandler.StopSound(soundOrigin); }
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundOrigin.getX(), soundOrigin.getY(), soundOrigin.getZ()) / 8, 1);
            float level = ITUtils.remapRange(0, 1, 0.5f, 1.0f, soundVolume);
            ITSounds.turbine.PlayRepeating(soundOrigin, (11 * (level - 0.5f)) / attenuation, level);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onChunkUnload() {
        if (soundOrigin == null) { InitializePoIs(); if (soundOrigin == null) return; }
        ITSoundHandler.StopSound(soundOrigin);
        super.onChunkUnload();
    }

    @Override
    public void disassemble() {
        super.disassemble();
        if (soundOrigin == null) { InitializePoIs(); if (soundOrigin == null) return; }
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundOrigin), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundOrigin.getX(), soundOrigin.getY(), soundOrigin.getZ(), 0));
    }

    public void efficientMarkDirty() { world.getChunk(this.getPos()).markDirty(); }

    public boolean isValidAlternator() {
        if (mechanicalOutput == null) InitializePoIs();
        if (alternator == null || !alternator.isValid()) {
            TileEntity tile = world.getTileEntity(mechanicalOutputFront);
            if (tile instanceof IMechanicalEnergy) {
                IMechanicalEnergy possibleAlternator = (IMechanicalEnergy) tile;
                if (possibleAlternator.isValid() && possibleAlternator.isMechanicalEnergyReceiver(facing.getOpposite())) alternator = possibleAlternator;
            }
        }
        return alternator != null && alternator.isValid();
    }

    @Override
    public void update() {
        super.update();
        if (!formed) return;
        if (world.isRemote) {
            float rotationSpeed = speed == 0 ? 0f : ((float) speed / (float) maxSpeed) * maxRotationSpeed;
            float oldMomentum = animation.getAnimationMomentum();
            animation.setAnimationMomentum(rotationSpeed);
            animation.setAnimationRotation(animation.getAnimationRotation() + oldMomentum);
            if (soundVolume < targetLevel) { soundVolume = Math.min(soundVolume + 0.01f, targetLevel); }
            else if (soundVolume > targetLevel) { soundVolume = Math.max(soundVolume - 0.01f, targetLevel); }
            handleSounds();
            return;
        }
        boolean update = false;
        int prevSpeed = speed;
        int prevBurn = burnRemaining;
        if (burnRemaining > 0) {
            burnRemaining--;
            speedUp();
            if (burnRemaining != prevBurn) update = true;
        } else if (!isRSDisabled() && tanks[0].getFluid() != null && tanks[0].getFluid().getFluid() != null && isValidAlternator()) {
            if (cachedRecipe == null || !tanks[0].getFluid().isFluidEqual(cachedRecipe.fluidInput)) cachedRecipe = HighPressureSteamTurbineRecipe.findFuel(tanks[0].getFluid());
            HighPressureSteamTurbineRecipe recipe = cachedRecipe;
            if (recipe != null && recipe.fluidInput.amount <= tanks[0].getFluidAmount()) {
                burnRemaining = recipe.getTotalProcessTime() - 1;
                tanks[0].drain(recipe.fluidInput.amount, true);
                if (recipe.fluidOutput != null) tanks[1].fill(recipe.fluidOutput, true);
                this.markContainingBlockForUpdate(null);
                speedUp();
                update = true;
            } else {
                speedDown();
            }
        } else {
            speedDown();
        }
        if (prevSpeed != speed) update = true;
        if (pumpOutputOut()) update = true;
        clientUpdateCooldown--;
        if (update && clientUpdateCooldown <= 0) {
            efficientMarkDirty();
            this.markContainingBlockForUpdate(null);
            clientUpdateCooldown = 5;
        }
        if (oldSpeed != speed) notifyNearbyClients();
        oldSpeed = speed;
    }

    @Override
    public void TankContentsChanged() {
        cachedRecipe = null;
        this.markContainingBlockForUpdate(null);
    }

    @Override
    public boolean isDummy() { return false; }

    @Override
    public TileEntityHighPressureSteamTurbineMaster master() {
        master = this;
        return this;
    }

    @Override
    public int getComparatorInputOverride() { return 15 * speed / maxSpeed; }

    public boolean isMechanicalEnergyTransmitter(@Nullable EnumFacing facing, int position) {
        if (!formed) return false;
        if (mechanicalOutput == null) InitializePoIs();
        return facing != null && mechanicalOutput.isPoI(facing, position);
    }

    public void notifyNearbyClients() { BinaryMessageTileSync.sendToAllTracking(world, getPos(), Unpooled.copyInt(speed)); }

    @Override
    public void receiveMessageFromServer(ByteBuf buf) {
        speed = buf.readInt();
        targetLevel = (float)speed / maxSpeed;
    }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartHighPressureSteamTurbine.instance.pointsOfInterest) {
            switch (poi.name) {
                case "fluid_input":
                    input = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_output":
                    output = new PoICache(facing, poi, mirrored);
                    fluidOutputPos = getBlockPosForPos(output.position).offset(output.facing);
                    break;
                case "redstone":
                    redstone = new PoICache(facing, poi, mirrored);
                    break;
                case "mechanical_output":
                    mechanicalOutput = new PoICache(facing, poi, mirrored);
                    mechanicalOutputFront = getBlockPosForPos(mechanicalOutput.position).offset(mechanicalOutput.facing);
                    break;
                case "running_sound":
                    soundOrigin = getBlockPosForPos(poi.position);
                    break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        if (input != null) notifyNeighbor(getBlockPosForPos(input.position));
        if (output != null) notifyNeighbor(getBlockPosForPos(output.position));
        if (redstone != null) notifyNeighbor(getBlockPosForPos(redstone.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override
    public @Nonnull int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstone == null) InitializePoIs();
        return new int[] {redstone.position};
    }

    @Override
    protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (input == null) InitializePoIs();
        if (input.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        if (output.isPoI(side, position)) return new IFluidTank[] {tanks[1]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override
    protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (input == null) InitializePoIs();
        if (input.isPoI(side, position) && iTank == 0) {
            if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
            FluidStack current = tanks[0].getFluid();
            if (current == null) return HighPressureSteamTurbineRecipe.findFuelByFluid(resource.getFluid()) != null;
            return resource.isFluidEqual(current);
        }
        return false;
    }

    @Override
    protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (output == null) InitializePoIs();
        return output.isPoI(side, position) && iTank == 1;
    }
}
