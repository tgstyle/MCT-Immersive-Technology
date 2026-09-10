package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.capability.IMechanicalEnergyConsumer;
import com.immersiveconvergence.api.capability.RotationInertiaProcess;
import com.immersiveconvergence.api.client.ICClientUtils;
import com.immersiveconvergence.api.client.ICSoundHandler;
import com.immersiveconvergence.api.client.MechanicalEnergyAnimation;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.IComparatorOverride;
import com.immersiveconvergence.api.multiblock.TemplateMultiblock;
import com.immersiveconvergence.api.network.BinaryTileSyncMessage;
import com.immersiveconvergence.api.network.IBinaryMessageReceiver;
import com.immersiveconvergence.api.particles.ParticleColoredSmoke;
import com.immersiveconvergence.api.util.ICFluidTank;
import com.immersiveconvergence.api.util.ICFluxStorage;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.core.ICCommonConfig;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.api.crafting.GasTurbineRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
import mctmods.immersivetechnology.common.util.compat.advancedrocketry.AdvancedRocketryHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleSmokeNormal;
import net.minecraft.entity.player.EntityPlayerMP;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;

public class TileEntityGasTurbineMaster extends TileEntityGasTurbineSlave implements ICFluidTank.TankListener, IBinaryMessageReceiver, IComparatorOverride {
    public static int maxSpeed() { return Math.round(ICCommonConfig.mechanical.maxRpm * Multiblocks.gasTurbine.gasTurbine_speed_maxFactor); }

    private static float maxRotationSpeed() { return Multiblocks.gasTurbine.gasTurbine_speed_maxRotation; }

    private RotationInertiaProcess inertia;
    private RotationInertiaProcess inertia() {
        if (inertia == null) {
            inertia = new RotationInertiaProcess(Multiblocks.gasTurbine.gasTurbine_baseMass, Multiblocks.gasTurbine.gasTurbine_driveTorque, Multiblocks.gasTurbine.gasTurbine_friction, maxSpeed());
        }
        return inertia;
    }

    private int speedGainPerTick() { return inertia().getSpeedUpRate(); }

    private int speedLossPerTick() { return inertia().getSpeedDownRate(); }

    private int effectiveMax() { return isValidAlternator() ? Math.min(maxSpeed(), alternator.getMaxSpeed()) : maxSpeed(); }

    private static int inputTankSize() { return Multiblocks.gasTurbine.gasTurbine_input_tankSize; }

    private static int outputTankSize() { return Multiblocks.gasTurbine.gasTurbine_output_tankSize; }

    public static int electricStarterConsumption() { return Multiblocks.gasTurbine.gasTurbine_electric_starter_consumption; }

    public static int sparkplugConsumption() { return Multiblocks.gasTurbine.gasTurbine_sparkplug_consumption; }

    private static int electricStarterSize() { return Multiblocks.gasTurbine.gasTurbine_electric_starter_size; }

    private static int sparkplugSize() { return Multiblocks.gasTurbine.gasTurbine_sparkplug_size; }

    public ICFluxStorage starterStorage = new ICFluxStorage(electricStarterSize(), false, true);
    public ICFluxStorage sparkplugStorage = new ICFluxStorage(sparkplugSize(), false, true);
    public FluidTank[] tanks = new FluidTank[] {
            new ICFluidTank(inputTankSize(), this),
            new ICFluidTank(outputTankSize(), this)
    };
    public MechanicalEnergyAnimation animation = new MechanicalEnergyAnimation();

    public int fuelBurnRemaining = 0;
    private boolean isShutdown = false;
    public int speed;
    public int ignitionGracePeriod = 0;
    public boolean ignited;
    public boolean starterRunning = false;
    public float currentTorque = Multiblocks.gasTurbine.gasTurbine_torque;
    private boolean stall = false;
    private boolean everIgnited = false;

    private int effectiveMaxSpeed = maxSpeed();
    private float currentLevel = 0f;
    private float currentPitch = 0f;
    private int soundGracePeriod = 0;
    private int igniteSoundDelay = 0;
    private int tickCountdown = 5;
    public boolean redstoneControlInverted = false;
    private int oldComparatorOutput;
    private boolean isRunning = false;

    public GasTurbineRecipe lastRecipe;
    private GasTurbineRecipe cachedFuelRecipe;
    private IMechanicalEnergyConsumer alternator;

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        speed = nbt.getInteger("speed");
        starterRunning = nbt.getBoolean("starter");
        stall = nbt.getBoolean("stall");
        everIgnited = nbt.getBoolean("everIgnited");
        ignitionGracePeriod = nbt.getInteger("ignitionGracePeriod");
        animation.readFromNBT(nbt);
        fuelBurnRemaining = nbt.getInteger("fuelBurnRemaining");
        isShutdown = nbt.getBoolean("isShutdown");
        starterStorage.readFromNBT(nbt.getCompoundTag("starterStorage"));
        sparkplugStorage.readFromNBT(nbt.getCompoundTag("sparkplugStorage"));
        redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
        oldComparatorOutput = nbt.getInteger("oldComparatorOutput");
        soundGracePeriod = nbt.getInteger("soundGracePeriod");
        isRunning = nbt.getBoolean("isRunning");
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setInteger("speed", speed);
        nbt.setBoolean("starter", starterRunning);
        nbt.setBoolean("stall", stall);
        nbt.setBoolean("everIgnited", everIgnited);
        nbt.setInteger("ignitionGracePeriod", ignitionGracePeriod);
        animation.writeToNBT(nbt);
        nbt.setInteger("fuelBurnRemaining", fuelBurnRemaining);
        nbt.setBoolean("isShutdown", isShutdown);
        nbt.setTag("starterStorage", starterStorage.writeToNBT(new NBTTagCompound()));
        nbt.setTag("sparkplugStorage", sparkplugStorage.writeToNBT(new NBTTagCompound()));
        nbt.setBoolean("redstoneControlInverted", redstoneControlInverted);
        nbt.setInteger("oldComparatorOutput", oldComparatorOutput);
        nbt.setInteger("soundGracePeriod", soundGracePeriod);
        nbt.setBoolean("isRunning", isRunning);
    }

    @SideOnly(Side.CLIENT)
    private void spawnParticles() {
        BlockPos particlePos = poiWorldPos("particle0");
        if (!starterRunning || speed < effectiveMaxSpeed / 4) return;
        Random rand = world.rand;
        if (rand.nextInt(40) == 0) return;
        int lessParticleSetting = ICClientUtils.mc().gameSettings.particleSetting;
        if (lessParticleSetting == 2 || (lessParticleSetting == 1 && rand.nextInt(3) == 0)) return;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (particlePos.distanceSq(player.posX, player.posY, player.posZ) > 4096) return;
        Particle particle = new ParticleSmokeNormal.Factory().createParticle(0, world,
                particlePos.getX() + 2 - rand.nextFloat() * 3,
                particlePos.getY() + 0.5f,
                particlePos.getZ() + 2 - rand.nextFloat() * 3,
                0, 0.02f, 0);
        ICClientUtils.mc().effectRenderer.addEffect(particle);
    }

    @SideOnly(Side.CLIENT)
    private void spawnVentSmoke() {
        BlockPos smokePos = poiWorldPos("smoke1");
        if (!isRunning || world.getTotalWorldTime() % 2 != 0) return;
        if (FluidUtil.getFluidHandler(world, poiFrontPos("fluid_output0"), poi("fluid_output0").facing.getOpposite()) != null) return;
        Random rand = world.rand;
        int lessParticleSetting = ICClientUtils.mc().gameSettings.particleSetting;
        if (lessParticleSetting == 2 || (lessParticleSetting == 1 && rand.nextInt(3) == 0)) return;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (smokePos.distanceSq(player.posX, player.posY, player.posZ) > 4096) return;
        float normSpeed = Math.max(0f, ITUtils.remapRange(100f, effectiveMaxSpeed, 0f, 1f, speed));
        double dirVelHoriz = 0.125 * normSpeed;
        double dirVelVert = 0.1 * normSpeed;
        double baseUp = 0.0625 + 0.1 * (1 - normSpeed);
        double velX = facing.getXOffset() * dirVelHoriz + (rand.nextDouble() - 0.5) * 0.03125;
        double velY = facing.getYOffset() * dirVelVert + baseUp;
        double velZ = facing.getZOffset() * dirVelHoriz + (rand.nextDouble() - 0.5) * 0.03125;
        FluidStack outFluid = tanks[1].getFluid();
        float r = 0.5F, g = 0.5F, b = 0.5F;
        if (outFluid != null) {
            int tint = outFluid.getFluid().getColor(outFluid);
            r = ((tint >> 16) & 0xFF) / 255f;
            g = ((tint >> 8) & 0xFF) / 255f;
            b = (tint & 0xFF) / 255f;
        }
        ParticleColoredSmoke cloud = new ParticleColoredSmoke(world,
                smokePos.getX() + 0.5, smokePos.getY() + 0.5, smokePos.getZ() + 0.5, velX, velY, velZ, ITConfig.Client.particles.colored_smoke_height);
        cloud.setRBGColorF(r, g, b);
        ICClientUtils.mc().effectRenderer.addEffect(cloud);
    }

    @SideOnly(Side.CLIENT)
    private static float soundAttenuation(EntityPlayerSP player, BlockPos pos, float divisor) { return Math.max((float)player.getDistanceSq(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) / divisor, 1); }

    @SideOnly(Side.CLIENT)
    private void handleSounds() {
        BlockPos soundPos = poiWorldPos("sound0");
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        float targetLevel = ITUtils.remapRange(0, effectiveMaxSpeed, 0.2f, 1.0f, speed);
        if (currentLevel == 0f) { currentLevel = targetLevel; }
        else { currentLevel = currentLevel * 0.9f + targetLevel * 0.1f; }
        float targetPitch = ITUtils.remapRange(0, effectiveMaxSpeed, 0.5f, 1.5f, speed);
        if (currentPitch == 0f) { currentPitch = targetPitch; }
        else { currentPitch = currentPitch * 0.95f + targetPitch * 0.05f; }
        if (currentPitch < 0.5f) { currentPitch = 0.5f; }
        boolean runningAudible = speed > 0 && ((everIgnited && !starterRunning) || (stall && ignited));
        if (!runningAudible) { ICSoundHandler.stopSound(soundPos); }
        else { ITSounds.gasTurbineRunning.PlayRepeating(soundPos, (8 * (currentLevel - 0.2f)) / soundAttenuation(player, soundPos, 32f), currentPitch); }
        if (starterRunning) {
            ITSounds.gasTurbineStarter.PlayRepeating(poiWorldPos("sound3"), Math.min(currentLevel / soundAttenuation(player, poiWorldPos("sound3"), 64f), 0.4f), 1);
            if (speed >= effectiveMaxSpeed / 4) { ITSounds.gasTurbineArc.PlayRepeating(poiWorldPos("sound1"), Math.min(currentLevel / soundAttenuation(player, poiWorldPos("sound1"), 64f), 0.4f), 1); }
        } else {
            ICSoundHandler.stopSound(poiWorldPos("sound3"));
            ICSoundHandler.stopSound(poiWorldPos("sound1"));
        }
    }

    @Override public void receiveMessageFromServer(ByteBuf buf) {
        BlockPos soundPos = poiWorldPos("sound2");
        if (buf.readableBytes() == 0) {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos.getX(), soundPos.getY(), soundPos.getZ()) / 8, 1);
            ITSounds.gasTurbineSpark.PlayOnce(soundPos, 1 / attenuation, 1);
        }
        else if (buf.readableBytes() == 1 && buf.readByte() == 1) {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(poiWorldPos("sound3").getX(), poiWorldPos("sound3").getY(), poiWorldPos("sound3").getZ()) / 8, 1);
            ITSounds.gasIgnite.PlayOnce(poiWorldPos("sound3"), 1 / attenuation, 1);
        }
        else {
            speed = buf.readInt();
            effectiveMaxSpeed = buf.readInt();
            starterRunning = buf.readBoolean();
            isRunning = buf.readBoolean();
            stall = buf.readBoolean();
            everIgnited = buf.readBoolean();
            ignited = buf.readBoolean();
        }
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {}

    private void notifyNearbyClients() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(speed);
        buf.writeInt(effectiveMax());
        buf.writeBoolean(starterRunning);
        buf.writeBoolean(isRunning);
        buf.writeBoolean(stall);
        buf.writeBoolean(everIgnited);
        buf.writeBoolean(ignited);
        BinaryTileSyncMessage.sendToAllTracking(world, getPos(), buf);
    }

    @Override public void update() {
        super.update();
        if (!formed || world.isRemote) {
            if (world.isRemote) {
                float rotationSpeed = speed == 0 || effectiveMaxSpeed <= 0 ? 0f : ((float)speed / (float)effectiveMaxSpeed) * maxRotationSpeed();
                animation.setAnimationRotation(animation.getAnimationRotation() + animation.getAnimationMomentum());
                animation.setAnimationMomentum(rotationSpeed);
                handleSounds();
                spawnParticles();
                spawnVentSmoke();
            }
            return;
        }

        boolean update = false;
        ignited = ignitionGracePeriod > 0;
        boolean prevStarterRunning = starterRunning;
        boolean canRun = !isRSDisabled() && isValidAlternator();
        if (canRun && electricStarterConsumption() <= starterStorage.getEnergyStored()) {
            starterRunning = true;
            starterStorage.modifyEnergyStored(-electricStarterConsumption());
        } else starterRunning = false;
        int prevSpeed = speed;
        boolean wasRunning = isRunning;

        boolean wasStall = stall;
        if (speed <= 0) {
            speed = 0;
            isShutdown = false;
            stall = false;
            everIgnited = false;
            currentTorque = Multiblocks.gasTurbine.gasTurbine_torque;
        }
        if (!canRun) {
            isShutdown = true;
            ignitionGracePeriod = 0;
            fuelBurnRemaining = 0;
            stall = false;
        }

        if (speed < effectiveMax() / 4) {
            if (canRun && !isShutdown) {
                if (ignitionGracePeriod > 0) ignitionGracePeriod--;
                speedUp();
            } else speedDown();
        } else if (!isShutdown && starterRunning) {
                if (canIgnite()) {
                stall = true;
                if (!wasStall) ignite();
                else ignitionGracePeriod = 60;
                speed = effectiveMax() / 4;
                if (ignitionGracePeriod > 0) ignitionGracePeriod--;
            } else {
                stall = false;
                speedDown();
            }
        } else {
            stall = false;
            if (isShutdown) speedDown();
            else if (fuelBurnRemaining > 0 && (ignited || canIgnite())) {
                fuelBurnRemaining--;
                if (!ignited) ignite();
                speedUp();
            } else if (canRun && tanks[0].getFluidAmount() > 0 && (ignited || canIgnite())) {
                if (lastRecipe == null || !Objects.requireNonNull(tanks[0].getFluid()).isFluidEqual(lastRecipe.fluidInput)) cachedFuelRecipe = GasTurbineRecipe.findFuel(tanks[0].getFluid());
                GasTurbineRecipe recipe = lastRecipe = cachedFuelRecipe;
                if (recipe != null && recipe.fluidInput.amount <= tanks[0].getFluidAmount()) {
                    currentTorque = recipe.torque;
                    fuelBurnRemaining = recipe.getTotalProcessTime() - 1;
                    tanks[0].drain(recipe.fluidInput.amount, true);
                    if (recipe.fluidOutput != null) tanks[1].fill(recipe.fluidOutput, true);
                    if (!ignited) ignite();
                    speedUp();
                    update = true;
                } else speedDown();
            } else speedDown();
        }

        if (pumpOutputOut()) update = true;

        if (igniteSoundDelay > 0) {
            igniteSoundDelay--;
            if (igniteSoundDelay == 0 && starterRunning) { BinaryTileSyncMessage.sendToAllTracking(world, getPos(), Unpooled.buffer(1).writeByte(1)); }
        }

        boolean didWork = speed > 0;
        if (didWork) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;
        isRunning = soundGracePeriod > 0;

        float rotationSpeed = speed == 0 ? 0f : ((float)speed / (float)maxSpeed()) * maxRotationSpeed();
        float oldMomentum = animation.getAnimationMomentum();
        animation.setAnimationMomentum(rotationSpeed);
        animation.setAnimationRotation(animation.getAnimationRotation() + oldMomentum);

        boolean changed = animation.getAnimationMomentum() != oldMomentum || starterRunning != prevStarterRunning || prevSpeed != speed || isRunning != (speed > 0) || stall != wasStall;
        if (changed && tickCountdown-- <= 0) {
            notifyNearbyClients();
            tickCountdown = 5;
        }
        if (update || changed) {
            efficientMarkDirty();
            if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
            else { throttledBlockUpdate(); }
        }

        int comp = comparatorValue();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            notifyComparators();
        }
    }

    private void speedUp() {
        if (starterRunning) speed = Math.min(effectiveMax() / 4, speed + speedGainPerTick());
        else if (speed >= effectiveMax() / 4) speed = Math.min(effectiveMax(), speed + speedGainPerTick());
        else speedDown();
    }

    private void speedDown() {
        if (ignitionGracePeriod > 0) ignitionGracePeriod--;
        speed = Math.max(0, speed - speedLossPerTick());
    }

    private boolean isValidAlternator() {
        if (alternator == null || !alternator.isValid()) {
            TileEntity tile = world.getTileEntity(poiFrontPos("mechanical_output0"));
            if (tile instanceof IMechanicalEnergyConsumer) {
                IMechanicalEnergyConsumer possible = (IMechanicalEnergyConsumer)tile;
                if (possible.isValid() && possible.isMechanicalEnergyReceiver(poi("mechanical_output0").facing.getOpposite())) alternator = possible;
            }
        }
        return alternator != null && alternator.isValid();
    }

    private void ignite() {
        sparkplugStorage.modifyEnergyStored(-sparkplugConsumption());
        ignited = true;
        everIgnited = true;
        ignitionGracePeriod = 60;
        if (speed < effectiveMax() / 2) {
            BinaryTileSyncMessage.sendToAllTracking(world, getPos(), Unpooled.buffer());
            igniteSoundDelay = 3;
        }
    }

    private boolean canIgnite() {
        boolean canFuelCombust = true;
        if (ITCompatModule.isAdvancedRocketryLoaded) canFuelCombust = AdvancedRocketryHelper.isAtmosphereSuitableForCombustion(world, TemplateMultiblock.localToWorld(getPos(), 0, 0, -1, facing, mirrored));
        return sparkplugConsumption() <= sparkplugStorage.getEnergyStored() && canFuelCombust;
    }

    private boolean pumpOutputOut() {
        if (tanks[1].getFluidAmount() == 0) return false;
        IFluidHandler output = FluidUtil.getFluidHandler(world, poiFrontPos("fluid_output0"), poi("fluid_output0").facing.getOpposite());
        if (output == null) return false;
        FluidStack out = tanks[1].getFluid();
        if (out == null) return false;
        int accepted = output.fill(out, false);
        if (accepted == 0) return false;
        int drained = output.fill(ICUtils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        tanks[1].drain(drained, true);
        return drained > 0;
    }

    @Override public void TankContentsChanged() {
        lastRecipe = null;
        requestClientSync();
        tickCountdown = 0;
    }

    @Override public int getComparatorInputOverride() { return isComparatorPos() ? comparatorValue() : 0; }

    public int comparatorValue() { return maxSpeed() <= 0 ? 0 : 15 * speed / maxSpeed(); }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityGasTurbineMaster master() { return this; }

    public boolean isMechanicalEnergyTransmitter(@Nullable EnumFacing facing, BlockPos position) {
        if (!formed) return false;
        return facing != null && isPoI("mechanical_output0", facing, position);
    }

    public IEnergyStorage getEnergyAtPosition(@Nullable EnumFacing facing, BlockPos position) {
        if (!formed || facing == null) return null;
        if (isPoI("energy_input0", facing, position)) return starterStorage;
        if (isPoI("energy_input1", facing, position)) return sparkplugStorage;
        return null;
    }

    public ICFluxStorage getFluxStorageAtPosition(BlockPos position) {
        return poi("energy_input1").position.equals(position) ? sparkplugStorage : starterStorage;
    }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, BlockPos position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (side == null) return tanks;
        if (isPoI("fluid_input0", side, position)) return tankView(0, tanks[0]);
        if (isPoI("fluid_output0", side, position)) return tankView(1, tanks[1]);
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        if (!isPoI("fluid_input0", side, position)) return false;
        if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
        if (tanks[0].getFluid() == null) { return true; }
        return resource.getFluid() == tanks[0].getFluid().getFluid();
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        return isPoI("fluid_output0", side, position) && tanks[1].getFluidAmount() > 0;
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return ITUtils.EMPTY_INT_ARRAY; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return ITUtils.EMPTY_INT_ARRAY; }
}
