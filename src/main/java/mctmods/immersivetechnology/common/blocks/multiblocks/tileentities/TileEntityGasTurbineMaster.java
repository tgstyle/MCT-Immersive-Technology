package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.api.crafting.GasTurbineRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.blocks.ITBlockInterfaces.IMechanicalEnergy;
import mctmods.immersivetechnology.common.blocks.multiblocks.TileEntityITMultiblockPartGasTurbine;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITFluxStorage;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
import mctmods.immersivetechnology.common.util.compat.advancedrocketry.AdvancedRocketryHelper;
import mctmods.immersivetechnology.common.util.multiblock.PoICache;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.IBinaryMessageReceiver;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleSmokeNormal;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.energy.IEnergyStorage;
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
import java.util.Random;

public class TileEntityGasTurbineMaster extends TileEntityGasTurbineSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver {
    private static final int maxSpeed = Multiblocks.mechanicalEnergy.mechanicalEnergy_speed_max;
    private static final float maxRotationSpeed = Multiblocks.gasTurbine.gasTurbine_speed_maxRotation;
    private static final int speedGainPerTick = Multiblocks.gasTurbine.gasTurbine_speed_gainPerTick;
    private static final int speedLossPerTick = Multiblocks.gasTurbine.gasTurbine_speed_lossPerTick;
    private static final int inputTankSize = Multiblocks.gasTurbine.gasTurbine_input_tankSize;
    private static final int outputTankSize = Multiblocks.gasTurbine.gasTurbine_output_tankSize;
    public static final int electricStarterConsumption = Multiblocks.gasTurbine.gasTurbine_electric_starter_consumption;
    public static final int sparkplugConsumption = Multiblocks.gasTurbine.gasTurbine_sparkplug_consumption;
    private static final int electricStarterSize = Multiblocks.gasTurbine.gasTurbine_electric_starter_size;
    private static final int sparkplugSize = Multiblocks.gasTurbine.gasTurbine_sparkplug_size;

    public FluidTank[] tanks = new FluidTank[] { new ITFluidTank(inputTankSize, this), new ITFluidTank(outputTankSize, this) };
    public ITFluxStorage starterStorage = new ITFluxStorage(electricStarterSize, false, true);
    public ITFluxStorage sparkplugStorage = new ITFluxStorage(sparkplugSize, false, true);
    public int burnRemaining = 0;
    public int speed;
    public int ignitionGracePeriod = 0;
    public boolean ignited;
    public boolean starterRunning = false;
    public GasTurbineRecipe lastRecipe;
    private GasTurbineRecipe cachedRecipe;
    MechanicalEnergyAnimation animation = new MechanicalEnergyAnimation();
    IMechanicalEnergy alternator;
    private PoICache input, output, energy0, energy1, redstone, mechanicalOutput;
    private BlockPos particleOrigin, runningSoundOrigin, arcSoundOrigin, ignitionSoundOrigin, starterSoundOrigin, outputFront, mechanicalOutputFront;
    private boolean lastStarterRunning = false;
    private int clientUpdateCooldown = 1;

    @SideOnly(Side.CLIENT)
    public void spawnParticles() {
        if (input == null) InitializePoIs();
        if (!starterRunning || speed < maxSpeed / 4) return;
        Random rand = new Random();
        if (rand.nextInt(40) == 0) return;
        int lessParticleSetting = ClientUtils.mc().gameSettings.particleSetting;
        if (lessParticleSetting == 2 || lessParticleSetting == 1 && rand.nextInt(3) == 0) return;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distanceLimit = 64;
        if (particleOrigin.distanceSq(player.posX, player.posY, player.posZ) > distanceLimit * distanceLimit) return;
        Particle particle = new ParticleSmokeNormal.Factory().createParticle(0, world, particleOrigin.getX() + 2 - rand.nextFloat() * 3, particleOrigin.getY() + 0.5f, particleOrigin.getZ() + 2 - rand.nextFloat() * 3, 0, 0.02f, 0);
        ClientUtils.mc().effectRenderer.addEffect(particle);
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (input == null) InitializePoIs();
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        float att = Math.max((float) player.getDistanceSq(runningSoundOrigin.getX(), runningSoundOrigin.getY(), runningSoundOrigin.getZ()) / 64, 1);
        float level = ITUtils.remapRange(0, maxSpeed, 0.5f, 1.5f, speed);
        if (speed == 0) ITSoundHandler.StopSound(runningSoundOrigin);
        else ITSounds.gasTurbineRunning.PlayRepeating(runningSoundOrigin, (level - 0.5f) / (4 * att), level);
        if (starterRunning) {
            ITSounds.gasTurbineStarter.PlayRepeating(starterSoundOrigin, Math.min((level - .5f) / att, .2f), 1);
            if (speed >= maxSpeed / 4) ITSounds.gasTurbineArc.PlayRepeating(arcSoundOrigin, Math.min((level - .5f) / att, .2f), 1);
        } else {
            ITSoundHandler.StopSound(starterSoundOrigin);
            ITSoundHandler.StopSound(arcSoundOrigin);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onChunkUnload() {
        if (input == null) InitializePoIs();
        ITSoundHandler.StopSound(runningSoundOrigin);
        ITSoundHandler.StopSound(arcSoundOrigin);
        ITSoundHandler.StopSound(ignitionSoundOrigin);
        ITSoundHandler.StopSound(starterSoundOrigin);
        super.onChunkUnload();
    }

    @Override
    public void disassemble() {
        super.disassemble();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(runningSoundOrigin), new NetworkRegistry.TargetPoint(world.provider.getDimension(), runningSoundOrigin.getX(), runningSoundOrigin.getY(), runningSoundOrigin.getZ(), 0));
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(arcSoundOrigin), new NetworkRegistry.TargetPoint(world.provider.getDimension(), arcSoundOrigin.getX(), arcSoundOrigin.getY(), arcSoundOrigin.getZ(), 0));
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(ignitionSoundOrigin), new NetworkRegistry.TargetPoint(world.provider.getDimension(), ignitionSoundOrigin.getX(), ignitionSoundOrigin.getY(), ignitionSoundOrigin.getZ(), 0));
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(starterSoundOrigin), new NetworkRegistry.TargetPoint(world.provider.getDimension(), starterSoundOrigin.getX(), starterSoundOrigin.getY(), starterSoundOrigin.getZ(), 0));
        input = output = energy0 = energy1 = redstone = mechanicalOutput = null;
        particleOrigin = runningSoundOrigin = arcSoundOrigin = ignitionSoundOrigin = starterSoundOrigin = outputFront = mechanicalOutputFront = null;
    }

    private void speedUp() {
        if (starterRunning) {
            if (speed >= maxSpeed / 4) speed = Math.max(Math.min(maxSpeed, speed + speedGainPerTick - speedLossPerTick), maxSpeed / 4);
            else speed = Math.min(maxSpeed / 4, speed + speedGainPerTick);
        } else {
            if (speed >= maxSpeed / 4) speed = Math.min(maxSpeed, speed + speedGainPerTick);
            else speedDown();
        }
    }

    private void speedDown() {
        if (ignitionGracePeriod > 0) ignitionGracePeriod--;
        speed = Math.max(0, speed - speedLossPerTick);
    }

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

    public void ignite() {
        sparkplugStorage.modifyEnergyStored(-sparkplugConsumption);
        ignited = true;
        ignitionGracePeriod = 60;
        BinaryMessageTileSync.sendToAllTracking(world, getPos(), Unpooled.buffer());
    }

    public boolean canIgnite() {
        boolean canFuelCombust = true;
        if (ITCompatModule.isAdvancedRocketryLoaded) canFuelCombust = AdvancedRocketryHelper.isAtmosphereSuitableForCombustion(world, ITUtils.LocalOffsetToWorldBlockPos(this.getPos(), 0, 0, -1, facing, mirrored));
        return sparkplugConsumption <= sparkplugStorage.getEnergyStored() && canFuelCombust;
    }

    @Override
    public void receiveMessageFromServer(ByteBuf buf) {
        if (input == null) InitializePoIs();
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        float attenuation = Math.max((float) player.getDistanceSq(runningSoundOrigin.getX(), runningSoundOrigin.getY(), runningSoundOrigin.getZ()) / 8, 1);
        ITSounds.gasTurbineSpark.PlayOnce(ignitionSoundOrigin, 1 / attenuation, 1);
    }

    @Override
    public void update() {
        super.update();
        if (!formed || world.isRemote) {
            if (world.isRemote) {
                animation.setAnimationRotation(animation.getAnimationRotation() + animation.getAnimationMomentum());
                handleSounds();
                spawnParticles();
            }
            return;
        }
        float rotationSpeed = speed == 0 ? 0f : ((float) speed / (float) maxSpeed) * maxRotationSpeed;
        float oldMomentum = animation.getAnimationMomentum();
        animation.setAnimationMomentum(rotationSpeed);
        animation.setAnimationRotation(animation.getAnimationRotation() + oldMomentum);
        ignited = ignitionGracePeriod > 0;
        boolean canRun = !isRSDisabled() && isValidAlternator();
        if (canRun && electricStarterConsumption <= starterStorage.getEnergyStored()) {
            starterRunning = true;
            starterStorage.modifyEnergyStored(-electricStarterConsumption);
        } else starterRunning = false;
        if (speed < maxSpeed / 4) {
            if (canRun) {
                if (ignitionGracePeriod > 0) ignitionGracePeriod--;
                speedUp();
            } else speedDown();
        } else {
            if (burnRemaining > 0 && (ignited || canIgnite())) {
                burnRemaining--;
                if (!ignited) ignite();
                speedUp();
            } else if (canRun && tanks[0].getFluid() != null && tanks[0].getFluid().getFluid() != null && (ignited || canIgnite())) {
                if (lastRecipe == null || !tanks[0].getFluid().isFluidEqual(lastRecipe.fluidInput)) cachedRecipe = GasTurbineRecipe.findFuel(tanks[0].getFluid());
                GasTurbineRecipe recipe = lastRecipe = cachedRecipe;
                if (recipe != null && recipe.fluidInput.amount <= tanks[0].getFluidAmount()) {
                    burnRemaining = recipe.getTotalProcessTime() - 1;
                    tanks[0].drain(recipe.fluidInput.amount, true);
                    if (recipe.fluidOutput != null) tanks[1].fill(recipe.fluidOutput, true);
                    if (!ignited) ignite();
                    speedUp();
                } else speedDown();
            } else speedDown();
        }
        pumpOutputOut();
        boolean changed = animation.getAnimationMomentum() != oldMomentum;
        if (starterRunning != lastStarterRunning) {
            lastStarterRunning = starterRunning;
            changed = true;
        }
        clientUpdateCooldown--;
        if (changed && clientUpdateCooldown <= 0) {
            efficientMarkDirty();
            this.markContainingBlockForUpdate(null);
            clientUpdateCooldown = 5;
        }
    }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        speed = nbt.getInteger("speed");
        starterRunning = nbt.getBoolean("starter");
        ignitionGracePeriod = nbt.getInteger("ignitionGracePeriod");
        animation.readFromNBT(nbt);
        burnRemaining = nbt.getInteger("burnRemaining");
        starterStorage.readFromNBT(nbt.getCompoundTag("starterStorage"));
        sparkplugStorage.readFromNBT(nbt.getCompoundTag("sparkplugStorage"));
        lastStarterRunning = starterRunning;
    }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setInteger("speed", speed);
        nbt.setBoolean("starter", starterRunning);
        nbt.setInteger("ignitionGracePeriod", ignitionGracePeriod);
        animation.writeToNBT(nbt);
        nbt.setInteger("burnRemaining", burnRemaining);
        nbt.setTag("starterStorage", starterStorage.writeToNBT(new NBTTagCompound()));
        nbt.setTag("sparkplugStorage", sparkplugStorage.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public @Nonnull NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        nbt.setFloat("animationRotation", getAnimation().getAnimationRotation());
        return nbt;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        getAnimation().setAnimationRotation(nbt.getFloat("animationRotation"));
    }

    public void efficientMarkDirty() { world.getChunk(this.getPos()).markDirty(); }

    @Override
    public void TankContentsChanged() { this.markContainingBlockForUpdate(null); clientUpdateCooldown = 0; }

    @Override
    public boolean isDummy() { return false; }

    @Override
    public TileEntityGasTurbineMaster master() {
        master = this;
        return this;
    }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartGasTurbine.instance.pointsOfInterest) {
            switch (poi.name) {
                case "input": input = new PoICache(facing, poi, mirrored); break;
                case "output":
                    output = new PoICache(facing, poi, mirrored);
                    outputFront = getBlockPosForPos(output.position).offset(output.facing);
                    break;
                case "particle": particleOrigin = getBlockPosForPos(poi.position); break;
                case "running_sound": runningSoundOrigin = getBlockPosForPos(poi.position); break;
                case "arc_sound": arcSoundOrigin = getBlockPosForPos(poi.position); break;
                case "ignition_sound": ignitionSoundOrigin = getBlockPosForPos(poi.position); break;
                case "starter_sound": starterSoundOrigin = getBlockPosForPos(poi.position); break;
                case "energy0": energy0 = new PoICache(facing, poi, mirrored); break;
                case "energy1": energy1 = new PoICache(facing, poi, mirrored); break;
                case "mechanical_output":
                    mechanicalOutput = new PoICache(facing, poi, mirrored);
                    mechanicalOutputFront = getBlockPosForPos(mechanicalOutput.position).offset(mechanicalOutput.facing);
                    break;
                case "redstone": redstone = new PoICache(facing, poi, mirrored); break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(input.position));
        notifyNeighbor(getBlockPosForPos(output.position));
        notifyNeighbor(getBlockPosForPos(energy0.position));
        notifyNeighbor(getBlockPosForPos(energy1.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override
    public @Nonnull int[] getRedstonePos() {
        if (redstone == null) InitializePoIs();
        return new int[] {redstone.position};
    }

    public @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if (input == null) InitializePoIs();
        if (side == null) return tanks;
        if (input.isPoI(side, position)) return new FluidTank[] {tanks[0]};
        else if (output.isPoI(side, position)) return new FluidTank[] {tanks[1]};
        return ITUtils.emptyIFluidTankList;
    }

    public boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (input == null) InitializePoIs();
        if (!input.isPoI(side, position)) return false;
        if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
        if (tanks[0].getFluid() == null) return GasTurbineRecipe.findFuelByFluid(resource.getFluid()) != null;
        else return resource.getFluid() == tanks[0].getFluid().getFluid();
    }

    public boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (output == null) InitializePoIs();
        if (output.isPoI(side, position)) return tanks[1].getFluidAmount() > 0;
        return false;
    }

    private void pumpOutputOut() {
        if (output == null) InitializePoIs();
        if (tanks[1].getFluidAmount() == 0) return;
        IFluidHandler output;
        if ((output = FluidUtil.getFluidHandler(world, outputFront, this.output.facing.getOpposite())) != null) {
            FluidStack out = tanks[1].getFluid();
            int accepted = output.fill(out, false);
            if (accepted == 0) return;
            assert out != null;
            int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
            this.tanks[1].drain(drained, true);
        }
    }

    public boolean isMechanicalEnergyTransmitter(EnumFacing facing, int position) {
        if (mechanicalOutput == null) InitializePoIs();
        return mechanicalOutput.isPoI(facing, position);
    }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, int position) {
        if (energy0 == null) InitializePoIs();
        return energy0.isPoI(facing, position) || energy1.isPoI(facing, position);
    }

    public IEnergyStorage getEnergyAtPosition(@Nullable EnumFacing facing, int position) {
        if (energy0 == null) InitializePoIs();
        if (facing == null) return null;
        if (energy1.isPoI(facing, position)) return sparkplugStorage;
        else if (energy0.isPoI(facing, position)) return starterStorage;
        else return null;
    }

    public boolean isFluidInputPosition(@Nullable EnumFacing facing, int position) {
        if (input == null) InitializePoIs();
        return input.isPoI(facing, position);
    }

    public boolean isFluidOutputPosition(@Nullable EnumFacing facing, int position) {
        if (output == null) InitializePoIs();
        return output.isPoI(facing, position);
    }

    public boolean isStarterPosition(@Nullable EnumFacing facing, int position) {
        if (energy0 == null) InitializePoIs();
        return energy0.isPoI(facing, position);
    }

    public boolean isSparkplugPosition(@Nullable EnumFacing facing, int position) {
        if (energy1 == null) InitializePoIs();
        return energy1.isPoI(facing, position);
    }
}
