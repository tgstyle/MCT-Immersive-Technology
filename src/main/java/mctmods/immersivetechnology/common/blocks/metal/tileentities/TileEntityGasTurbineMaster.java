package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.api.crafting.GasTurbineRecipe;
import mctmods.immersivetechnology.common.Config;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.GasTurbine;
import mctmods.immersivetechnology.common.blocks.ITBlockInterfaces.IMechanicalEnergy;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockGasTurbine;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITFluxStorage;
import mctmods.immersivetechnology.common.util.ITSounds;
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

import javax.annotation.Nullable;
import java.util.Random;

public class TileEntityGasTurbineMaster extends TileEntityGasTurbineSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver {

    private static final int maxSpeed = Config.ITConfig.MechanicalEnergy.mechanicalEnergy_speed_max;
    private static final float maxRotationSpeed = GasTurbine.gasTurbine_speed_maxRotation;
    private static final int speedGainPerTick = GasTurbine.gasTurbine_speed_gainPerTick;
    private static final int speedLossPerTick = GasTurbine.gasTurbine_speed_lossPerTick;
    private static final int inputTankSize = GasTurbine.gasTurbine_input_tankSize;
    private static final int outputTankSize = GasTurbine.gasTurbine_input_tankSize;
    public static final int electricStarterConsumption = GasTurbine.gasTurbine_electric_starter_consumption;
    public static final int sparkplugConsumption = GasTurbine.gasTurbine_sparkplug_consumption;
    public static final int electricStarterSize = GasTurbine.gasTurbine_electric_starter_size;
    public static final int sparkplugSize = GasTurbine.gasTurbine_sparkplug_size;

    public FluidTank[] tanks = new FluidTank[] {
            new ITFluidTank(inputTankSize, this),
            new ITFluidTank(outputTankSize, this)
    };

    public ITFluxStorage starterStorage = new ITFluxStorage(electricStarterSize,false,true);
    public ITFluxStorage sparkplugStorage = new ITFluxStorage(sparkplugSize,false,true);

    public int burnRemaining = 0;
    public int speed;
    public int ignitionGracePeriod = 0;
    public boolean ignited;
    public boolean starterRunning = false;

    public GasTurbineRecipe lastRecipe;

    MechanicalEnergyAnimation animation = new MechanicalEnergyAnimation();

    IMechanicalEnergy alternator;

    @SideOnly(Side.CLIENT)
    public void spawnParticles() {
        if (!starterRunning || speed < maxSpeed / 4) return;
        if (input == null) InitializePoIs();
        Random rand = new Random();
        if(rand.nextInt(40) == 0) return;
        int lessParticleSetting = ClientUtils.mc().gameSettings.particleSetting;
        if(lessParticleSetting == 2 || lessParticleSetting == 1 && rand.nextInt(3) == 0) return;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distanceLimit = 64;
        if(particleOrigin.distanceSq(player.posX, player.posY, player.posZ) > distanceLimit * distanceLimit) return;
        Particle particle = new ParticleSmokeNormal.Factory().createParticle(0, world,
                particleOrigin.getX() + 2 - rand.nextFloat() * 3,
                particleOrigin.getY() + 0.5f,
                particleOrigin.getZ() + 2 - rand.nextFloat() * 3, 0, 0.02f, 0, 7);
        ClientUtils.mc().effectRenderer.addEffect(particle);
    }

    public void handleSounds() {
        float level = ITUtils.remapRange(0, maxSpeed, 0.5f, 1.5f, speed);
        if (input == null) InitializePoIs();
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        float attenuation = Math.max((float) player.getDistanceSq(runningSoundOrigin.getX(), runningSoundOrigin.getY(), runningSoundOrigin.getZ()) / 64, 1);
        if(speed == 0) ITSoundHandler.StopSound(runningSoundOrigin);
        else ITSounds.gasTurbineRunning.PlayRepeating(runningSoundOrigin, (level - 0.5f) / (4 * attenuation), level);
        if (starterRunning) {
            ITSounds.gasTurbineStarter.PlayRepeating(starterSoundOrigin, Math.min((level - .5f) / attenuation, .2f), 1);
            if (speed >= maxSpeed / 4) ITSounds.gasTurbineArc.PlayRepeating(arcSoundOrigin, Math.min((level - .5f) / attenuation, .2f), 1);
        } else {
            ITSoundHandler.StopSound(starterSoundOrigin);
            ITSoundHandler.StopSound(arcSoundOrigin);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onChunkUnload() {
        if(input == null) InitializePoIs();
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
        if (alternator == null || !alternator.isValid()) {
            if(input == null) InitializePoIs();
            TileEntity tile = world.getTileEntity(mechanicalOutputFront);
            if (tile instanceof IMechanicalEnergy) {
                IMechanicalEnergy possibleAlternator = (IMechanicalEnergy) tile;
                if (possibleAlternator.isValid() && possibleAlternator.isMechanicalEnergyReceiver(facing.getOpposite())) {
                    alternator = possibleAlternator;
                }
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
        return sparkplugConsumption <= sparkplugStorage.getEnergyStored();
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
        if(!formed) return;
        if(world.isRemote) {
            handleSounds();
            spawnParticles();
            return;
        }

        float rotationSpeed = speed == 0 ? 0f : ((float) speed / (float) maxSpeed) * maxRotationSpeed;
        if(ITUtils.setRotationAngle(animation, rotationSpeed)) {
            efficientMarkDirty();
            this.markContainingBlockForUpdate(null);
        }

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
            if(burnRemaining > 0 && (ignited || canIgnite())) {
                burnRemaining--;
                if (!ignited) ignite();
                speedUp();
            } else if(canRun && tanks[0].getFluid() != null && tanks[0].getFluid().getFluid() != null && (ignited || canIgnite())) {
                GasTurbineRecipe recipe = (lastRecipe != null && tanks[0].getFluid().isFluidEqual(lastRecipe.fluidInput)) ? lastRecipe : GasTurbineRecipe.findFuel(tanks[0].getFluid());
                if(recipe != null && recipe.fluidInput.amount <= tanks[0].getFluidAmount()) {
                    lastRecipe = recipe;
                    burnRemaining = recipe.getTotalProcessTime() - 1;
                    tanks[0].drain(recipe.fluidInput.amount, true);
                    if(recipe.fluidOutput != null) tanks[1].fill(recipe.fluidOutput, true);
                    if (!ignited) ignite();
                    this.markContainingBlockForUpdate(null);
                    speedUp();
                } else speedDown();
            } else speedDown();
        }
        pumpOutputOut();
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
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
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
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

    public void efficientMarkDirty() { // !!!!!!! only use it within update() function !!!!!!!
        world.getChunkFromBlockCoords(this.getPos()).markDirty();
    }

    @Override
    public void TankContentsChanged() {
        this.markContainingBlockForUpdate(null);
    }

    @Override
    public boolean isDummy() {
        return false;
    }

    @Override
    public TileEntityGasTurbineMaster master() {
        master = this;
        return this;
    }

    private PoICache input, output, power0, power1, redstone, mechanicalOutput;
    private BlockPos particleOrigin, runningSoundOrigin, arcSoundOrigin, ignitionSoundOrigin, starterSoundOrigin, outputFront, mechanicalOutputFront;

    private void InitializePoIs() {
        for(PoIJSONSchema poi : MultiblockGasTurbine.instance.pointsOfInterest) {
            if(poi.name.equals("input")) input = new PoICache(facing, poi, mirrored);
            else if(poi.name.equals("output")) {
                output = new PoICache(facing, poi, mirrored);
                outputFront = getBlockPosForPos(output.position).offset(output.facing);
            } else if(poi.name.equals("particle")) particleOrigin = getBlockPosForPos(poi.position);
            else if(poi.name.equals("running_sound")) runningSoundOrigin = getBlockPosForPos(poi.position);
            else if(poi.name.equals("arc_sound")) arcSoundOrigin = getBlockPosForPos(poi.position);
            else if(poi.name.equals("ignition_sound")) ignitionSoundOrigin = getBlockPosForPos(poi.position);
            else if(poi.name.equals("starter_sound")) starterSoundOrigin = getBlockPosForPos(poi.position);
            else if(poi.name.equals("power0")) power0 = new PoICache(facing, poi, mirrored);
            else if(poi.name.equals("power1")) power1 = new PoICache(facing, poi, mirrored);
            else if(poi.name.equals("mechanical_output")) {
                mechanicalOutput = new PoICache(facing, poi, mirrored);
                mechanicalOutputFront = getBlockPosForPos(mechanicalOutput.position).offset(mechanicalOutput.facing);
            } else if(poi.name.equals("redstone")) redstone = new PoICache(facing, poi, mirrored);
        }
    }

    @Override
    public int[] getRedstonePos() {
        if(input == null) InitializePoIs();
        return new int[] { redstone.position };
    }

    public IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if(input == null) InitializePoIs();
        if(side == null) return tanks;
        if(input.isPoI(side, position)) return new FluidTank[] {tanks[0]};
        else if(output.isPoI(side, position)) return new FluidTank[] {tanks[1]};
        return ITUtils.emptyIFluidTankList;
    }

    public boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource, int position) {
        if(input == null) InitializePoIs();
        if(input.isPoI(side, position)) {
            if(tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
            if(tanks[0].getFluid() == null) return GasTurbineRecipe.findFuelByFluid(resource.getFluid()) != null;
            else return resource.getFluid() == tanks[0].getFluid().getFluid();
        }
        return false;
    }

    public boolean canDrainTankFrom(int iTank, EnumFacing side, int position) {
        if(input == null) InitializePoIs();
        if(output.isPoI(side, position)) return tanks[1].getFluidAmount() > 0;
        return false;
    }

    private void pumpOutputOut() {
        if(input == null) InitializePoIs();
        IFluidHandler output;
        if((output = FluidUtil.getFluidHandler(world, outputFront, this.output.facing.getOpposite())) != null) {
            FluidStack out = tanks[1].getFluid();
            int accepted = output.fill(out, false);
            if(accepted == 0) return;
            int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
            this.tanks[1].drain(drained, true);
        }
    }

    public boolean isMechanicalEnergyTransmitter(EnumFacing facing, int position) {
        if(input == null) InitializePoIs();
        return mechanicalOutput.isPoI(facing, position);
    }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, int position) {
        if(input == null) InitializePoIs();
        return power0.isPoI(facing, position) || power1.isPoI(facing, position);
    }

    public IEnergyStorage getEnergyAtPosition(@Nullable EnumFacing facing, int position) {
        if(input == null) InitializePoIs();
        if (facing == null) return null;
        if (power1.isPoI(facing, position)) return sparkplugStorage;
        else if (power0.isPoI(facing, position)) return starterStorage;
        else return null;
    }
}
