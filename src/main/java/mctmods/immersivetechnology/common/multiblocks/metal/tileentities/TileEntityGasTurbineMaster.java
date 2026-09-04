package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.Utils;

import com.immersiveconvergence.ImmersiveConvergence;
import com.immersiveconvergence.api.capability.IMechanicalEnergyConsumer;
import com.immersiveconvergence.api.capability.RotationInertiaProcess;
import com.immersiveconvergence.api.client.ICSoundHandler;
import com.immersiveconvergence.api.client.MechanicalEnergyAnimation;
import com.immersiveconvergence.api.multiblock.PoICache;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.multiblock.TemplateMultiblock;
import com.immersiveconvergence.api.network.BinaryTileSyncMessage;
import com.immersiveconvergence.api.network.IBinaryMessageReceiver;
import com.immersiveconvergence.api.network.MessageStopSound;
import com.immersiveconvergence.api.particles.ParticleColoredSmoke;
import com.immersiveconvergence.api.util.ICFluidTank;
import com.immersiveconvergence.api.util.ICFluxStorage;
import com.immersiveconvergence.core.ICCommonConfig;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.api.crafting.GasTurbineRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartGasTurbine;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.IEnergyStorage;
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
    public int speed;
    public int ignitionGracePeriod = 0;
    public boolean ignited;
    public boolean starterRunning = false;

    private float targetSoundLevel;
    private float soundVolume = 0f;
    private int soundGracePeriod = 0;
    private int igniteSoundDelay = 0;
    private int tickCountdown = 5;
    public boolean redstoneControlInverted = false;
    private int oldComparatorOutput;
    private boolean isRunning = false;
    private boolean needsPoIInit = false;

    public GasTurbineRecipe lastRecipe;
    private GasTurbineRecipe cachedFuelRecipe;
    private IMechanicalEnergyConsumer alternator;

    protected PoICache energyInputPos0, energyInputPos1, fluidInputPos0, fluidOutputPos0, mechanicalOutputPos0, redstonePos0;
    private BlockPos outputFront0, mechanicalOutputTEPos0, particle0, soundPos0, soundPos1, soundPos2, soundPos3, smokePos1;

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        speed = nbt.getInteger("speed");
        starterRunning = nbt.getBoolean("starter");
        ignitionGracePeriod = nbt.getInteger("ignitionGracePeriod");
        animation.readFromNBT(nbt);
        fuelBurnRemaining = nbt.getInteger("fuelBurnRemaining");
        starterStorage.readFromNBT(nbt.getCompoundTag("starterStorage"));
        sparkplugStorage.readFromNBT(nbt.getCompoundTag("sparkplugStorage"));
        redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
        oldComparatorOutput = nbt.getInteger("oldComparatorOutput");
        soundGracePeriod = nbt.getInteger("soundGracePeriod");
        isRunning = nbt.getBoolean("isRunning");
        if (formed && !descPacket) needsPoIInit = true;
        if (world.isRemote) {
            targetSoundLevel = (float)speed / maxSpeed();
            soundVolume = targetSoundLevel;
        }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setInteger("speed", speed);
        nbt.setBoolean("starter", starterRunning);
        nbt.setInteger("ignitionGracePeriod", ignitionGracePeriod);
        animation.writeToNBT(nbt);
        nbt.setInteger("fuelBurnRemaining", fuelBurnRemaining);
        nbt.setTag("starterStorage", starterStorage.writeToNBT(new NBTTagCompound()));
        nbt.setTag("sparkplugStorage", sparkplugStorage.writeToNBT(new NBTTagCompound()));
        nbt.setBoolean("redstoneControlInverted", redstoneControlInverted);
        nbt.setInteger("oldComparatorOutput", oldComparatorOutput);
        nbt.setInteger("soundGracePeriod", soundGracePeriod);
        nbt.setBoolean("isRunning", isRunning);
    }

    @SideOnly(Side.CLIENT)
    private void spawnParticles() {
        if (particle0 == null) InitializePoIs();
        if (!starterRunning || speed < maxSpeed() / 4) return;
        Random rand = new Random();
        if (rand.nextInt(40) != 0) return;
        int lessParticleSetting = ClientUtils.mc().gameSettings.particleSetting;
        if (lessParticleSetting == 2 || (lessParticleSetting == 1 && rand.nextInt(3) == 0)) return;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (particle0.distanceSq(player.posX, player.posY, player.posZ) > 4096) return;
        Particle particle = new ParticleSmokeNormal.Factory().createParticle(0, world,
                particle0.getX() + 2 - rand.nextFloat() * 3,
                particle0.getY() + 0.5f,
                particle0.getZ() + 2 - rand.nextFloat() * 3,
                0, 0.02f, 0);
        ClientUtils.mc().effectRenderer.addEffect(particle);
    }

    @SideOnly(Side.CLIENT)
    private void spawnVentSmoke() {
        if (smokePos1 == null || fluidOutputPos0 == null) InitializePoIs();
        if (smokePos1 == null || !isRunning || world.getTotalWorldTime() % 2 != 0) return;
        if (FluidUtil.getFluidHandler(world, outputFront0, fluidOutputPos0.facing.getOpposite()) != null) return;
        Random rand = new Random();
        int lessParticleSetting = ClientUtils.mc().gameSettings.particleSetting;
        if (lessParticleSetting == 2 || (lessParticleSetting == 1 && rand.nextInt(3) == 0)) return;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (smokePos1.distanceSq(player.posX, player.posY, player.posZ) > 4096) return;
        float normSpeed = Math.max(0f, ITUtils.remapRange(100f, maxSpeed(), 0f, 1f, speed));
        double dirVelHoriz = 0.125 * normSpeed;
        double baseUp = 0.0625 + 0.1 * (1 - normSpeed);
        double velX = facing.getXOffset() * dirVelHoriz + (rand.nextDouble() - 0.5) * 0.03125;
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
                smokePos1.getX() + 0.5, smokePos1.getY() + 0.5, smokePos1.getZ() + 0.5, velX, baseUp, velZ, ITConfig.Client.particles.colored_smoke_height);
        cloud.setRBGColorF(r, g, b);
        ClientUtils.mc().effectRenderer.addEffect(cloud);
    }

    @SideOnly(Side.CLIENT)
    private void handleSounds() {
        if (soundPos0 == null) InitializePoIs();
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        float att = Math.max((float)player.getDistanceSq(soundPos0.getX(), soundPos0.getY(), soundPos0.getZ()) / 64, 1);
        float level = ITUtils.remapRange(0, 1, 0.5f, 1.5f, soundVolume);
        if (speed == 0) ICSoundHandler.stopSound(soundPos0);
        else ITSounds.gasTurbineRunning.PlayRepeating(soundPos0, (level - 0.5f) / att, level);
        if (starterRunning) {
            ITSounds.gasTurbineStarter.PlayRepeating(soundPos3, Math.min((level - .5f) / att, .2f), 1);
            if (speed >= maxSpeed() / 4) ITSounds.gasTurbineArc.PlayRepeating(soundPos1, Math.min((level - .5f) / att, .2f), 1);
        } else {
            ICSoundHandler.stopSound(soundPos3);
            ICSoundHandler.stopSound(soundPos1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        ICSoundHandler.stopSound(soundPos0);
        ICSoundHandler.stopSound(soundPos1);
        ICSoundHandler.stopSound(soundPos2);
        ICSoundHandler.stopSound(soundPos3);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        super.disassemble();
        if (soundPos0 != null) ImmersiveConvergence.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
        if (soundPos1 != null) ImmersiveConvergence.packetHandler.sendToAllTracking(new MessageStopSound(soundPos1), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos1.getX(), soundPos1.getY(), soundPos1.getZ(), 0));
        if (soundPos2 != null) ImmersiveConvergence.packetHandler.sendToAllTracking(new MessageStopSound(soundPos2), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos2.getX(), soundPos2.getY(), soundPos2.getZ(), 0));
        if (soundPos3 != null) ImmersiveConvergence.packetHandler.sendToAllTracking(new MessageStopSound(soundPos3), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos3.getX(), soundPos3.getY(), soundPos3.getZ(), 0));
    }

    @Override public void receiveMessageFromServer(ByteBuf buf) {
        if (buf.readableBytes() == 0) {
            if (soundPos2 == null) InitializePoIs();
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos2.getX(), soundPos2.getY(), soundPos2.getZ()) / 8, 1);
            ITSounds.gasTurbineSpark.PlayOnce(soundPos2, 1 / attenuation, 1);
        }
        else if (buf.readableBytes() == 1 && buf.readByte() == 1) {
            if (soundPos3 == null) InitializePoIs();
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos3.getX(), soundPos3.getY(), soundPos3.getZ()) / 8, 1);
            ITSounds.gasIgnite.PlayOnce(soundPos3, 1 / attenuation, 1);
        }
        else {
            speed = buf.readInt();
            starterRunning = buf.readBoolean();
            targetSoundLevel = (float)speed / maxSpeed();
            isRunning = buf.readBoolean();
        }
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {}

    private void notifyNearbyClients() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(speed);
        buf.writeBoolean(starterRunning);
        buf.writeBoolean(isRunning);
        BinaryTileSyncMessage.sendToAllTracking(world, getPos(), buf);
    }

    @Override public void update() {
        if (formed && (needsPoIInit || energyInputPos0 == null)) {
            InitializePoIs();
            needsPoIInit = false;
        }
        super.update();
        if (!formed || world.isRemote) {
            if (world.isRemote) {
                float rotationSpeed = speed == 0 ? 0f : ((float)speed / (float)maxSpeed()) * maxRotationSpeed();
                animation.setAnimationRotation(animation.getAnimationRotation() + animation.getAnimationMomentum());
                animation.setAnimationMomentum(rotationSpeed);
                if (soundVolume < targetSoundLevel) { soundVolume = Math.min(targetSoundLevel, soundVolume + 0.01f); }
                else if (soundVolume > targetSoundLevel) { soundVolume = Math.max(targetSoundLevel, soundVolume - 0.01f); }
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

        if (speed < maxSpeed() / 4) {
            if (canRun) {
                if (ignitionGracePeriod > 0) ignitionGracePeriod--;
                speedUp();
            } else speedDown();
        } else {
            if (fuelBurnRemaining > 0 && (ignited || canIgnite())) {
                fuelBurnRemaining--;
                if (!ignited) ignite();
                speedUp();
            } else if (canRun && tanks[0].getFluidAmount() > 0 && (ignited || canIgnite())) {
                if (lastRecipe == null || !Objects.requireNonNull(tanks[0].getFluid()).isFluidEqual(lastRecipe.fluidInput)) cachedFuelRecipe = GasTurbineRecipe.findFuel(tanks[0].getFluid());
                GasTurbineRecipe recipe = lastRecipe = cachedFuelRecipe;
                if (recipe != null && recipe.fluidInput.amount <= tanks[0].getFluidAmount()) {
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

        boolean changed = animation.getAnimationMomentum() != oldMomentum || starterRunning != prevStarterRunning || prevSpeed != speed || isRunning != (speed > 0);
        if (changed && tickCountdown-- <= 0) {
            notifyNearbyClients();
            tickCountdown = 5;
        }
        if (update || changed) {
            efficientMarkDirty();
            if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
            else { throttledBlockUpdate(); }
        }

        int comp = getComparatorInputOverride();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            if (redstonePos0 != null) {
                BlockPos rsPos = getBlockPosForPos(redstonePos0.position);
                world.updateComparatorOutputLevel(rsPos, getBlockType());
            }
        }
    }

    private void speedUp() {
        if (starterRunning) {
            if (speed >= maxSpeed() / 4) speed = Math.max(Math.min(effectiveMax(), speed + speedGainPerTick() - speedLossPerTick()), maxSpeed() / 4);
            else speed = Math.min(maxSpeed() / 4, speed + speedGainPerTick());
        } else {
            if (speed >= maxSpeed() / 4) speed = Math.min(effectiveMax(), speed + speedGainPerTick());
            else speedDown();
        }
    }

    private void speedDown() {
        if (ignitionGracePeriod > 0) ignitionGracePeriod--;
        speed = Math.max(0, speed - speedLossPerTick());
    }

    private boolean isValidAlternator() {
        if (mechanicalOutputPos0 == null) InitializePoIs();
        if (alternator == null || !alternator.isValid()) {
            TileEntity tile = world.getTileEntity(mechanicalOutputTEPos0);
            if (tile instanceof IMechanicalEnergyConsumer) {
                IMechanicalEnergyConsumer possible = (IMechanicalEnergyConsumer)tile;
                if (possible.isValid() && possible.isMechanicalEnergyReceiver(mechanicalOutputPos0.facing.getOpposite())) alternator = possible;
            }
        }
        return alternator != null && alternator.isValid();
    }

    private void ignite() {
        sparkplugStorage.modifyEnergyStored(-sparkplugConsumption());
        ignited = true;
        ignitionGracePeriod = 60;
        BinaryTileSyncMessage.sendToAllTracking(world, getPos(), Unpooled.buffer());
        igniteSoundDelay = 3;
    }

    private boolean canIgnite() {
        boolean canFuelCombust = true;
        if (ITCompatModule.isAdvancedRocketryLoaded) canFuelCombust = AdvancedRocketryHelper.isAtmosphereSuitableForCombustion(world, TemplateMultiblock.localToWorld(getPos(), 0, 0, -1, facing, mirrored));
        return sparkplugConsumption() <= sparkplugStorage.getEnergyStored() && canFuelCombust;
    }

    private boolean pumpOutputOut() {
        if (tanks[1].getFluidAmount() == 0) return false;
        IFluidHandler output = FluidUtil.getFluidHandler(world, outputFront0, fluidOutputPos0.facing.getOpposite());
        if (output == null) return false;
        FluidStack out = tanks[1].getFluid();
        if (out == null) return false;
        int accepted = output.fill(out, false);
        if (accepted == 0) return false;
        int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        tanks[1].drain(drained, true);
        return drained > 0;
    }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartGasTurbine.instance.pointsOfInterest) {
            switch (poi.name) {
                case "fluid_input0":
                    fluidInputPos0 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_output0":
                    fluidOutputPos0 = new PoICache(facing, poi, mirrored);
                    outputFront0 = getBlockPosForPos(fluidOutputPos0.position).offset(fluidOutputPos0.facing);
                    break;
                case "particle0":
                    particle0 = getBlockPosForPos(poi.position);
                    break;
                case "smoke1":
                    smokePos1 = getBlockPosForPos(poi.position);
                    break;
                case "sound0":
                    soundPos0 = getBlockPosForPos(poi.position);
                    break;
                case "sound1":
                    soundPos1 = getBlockPosForPos(poi.position);
                    break;
                case "sound2":
                    soundPos2 = getBlockPosForPos(poi.position);
                    break;
                case "sound3":
                    soundPos3 = getBlockPosForPos(poi.position);
                    break;
                case "energy_input0":
                    energyInputPos0 = new PoICache(facing, poi, mirrored);
                    break;
                case "energy_input1":
                    energyInputPos1 = new PoICache(facing, poi, mirrored);
                    break;
                case "mechanical_output0":
                    mechanicalOutputPos0 = new PoICache(facing, poi, mirrored);
                    mechanicalOutputTEPos0 = getBlockPosForPos(mechanicalOutputPos0.position).offset(mechanicalOutputPos0.facing);
                    break;
                case "redstone0":
                    redstonePos0 = new PoICache(facing, poi, mirrored);
                    break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(fluidInputPos0.position));
        notifyNeighbor(getBlockPosForPos(fluidOutputPos0.position));
        notifyNeighbor(getBlockPosForPos(energyInputPos0.position));
        notifyNeighbor(getBlockPosForPos(energyInputPos1.position));
        notifyNeighbor(getBlockPosForPos(redstonePos0.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override public void TankContentsChanged() {
        lastRecipe = null;
        markContainingBlockForUpdate(null);
        tickCountdown = 0;
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

    @Override public int getComparatorInputOverride() { return maxSpeed() <= 0 ? 0 : 15 * speed / maxSpeed(); }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityGasTurbineMaster master() { return this; }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstonePos0 == null) InitializePoIs();
        return new int[]{toFlatIndex(redstonePos0.position)};
    }

    @Override @Nonnull public int[] getEnergyPos() {
        if (!formed) return new int[0];
        if (energyInputPos0 == null) InitializePoIs();
        return new int[]{toFlatIndex(energyInputPos0.position), toFlatIndex(energyInputPos1.position)};
    }

    public boolean isMechanicalEnergyTransmitter(@Nullable EnumFacing facing, BlockPos position) {
        if (!formed) return false;
        if (mechanicalOutputPos0 == null) InitializePoIs();
        return facing != null && mechanicalOutputPos0.isPoI(facing, position);
    }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, BlockPos position) {
        if (!formed) return false;
        if (facing == null) return false;
        if (energyInputPos0 == null) InitializePoIs();
        return energyInputPos0.isPoI(facing, position) || energyInputPos1.isPoI(facing, position);
    }

    public IEnergyStorage getEnergyAtPosition(@Nullable EnumFacing facing, BlockPos position) {
        if (!formed || facing == null) return null;
        if (energyInputPos0 == null) InitializePoIs();
        if (energyInputPos0.isPoI(facing, position)) return starterStorage;
        if (energyInputPos1.isPoI(facing, position)) return sparkplugStorage;
        return null;
    }

    public FluxStorage getFluxStorageAtPosition(BlockPos position) {
        if (energyInputPos0 == null) InitializePoIs();
        return energyInputPos1.position.equals(position) ? sparkplugStorage : starterStorage;
    }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, BlockPos position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (fluidInputPos0 == null) InitializePoIs();
        if (side == null) return tanks;
        if (fluidInputPos0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        if (fluidOutputPos0.isPoI(side, position)) return new IFluidTank[] {tanks[1]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        if (!formed || fluidInputPos0 == null) InitializePoIs();
        if (!fluidInputPos0.isPoI(side, position)) return false;
        if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
        if (tanks[0].getFluid() == null) { return true; }
        return resource.getFluid() == tanks[0].getFluid().getFluid();
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
        if (!formed || fluidOutputPos0 == null) InitializePoIs();
        return fluidOutputPos0.isPoI(side, position) && tanks[1].getFluidAmount() > 0;
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            if (fluidInputPos0 == null) InitializePoIs();
            return fluidInputPos0.isPoI(facing, posInMultiblock()) || fluidOutputPos0.isPoI(facing, posInMultiblock());
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            if (fluidInputPos0 == null) InitializePoIs();
            if (fluidInputPos0.isPoI(facing, posInMultiblock()) || fluidOutputPos0.isPoI(facing, posInMultiblock())) {
                return (T)new GasTurbineFluidHandler(getAccessibleFluidTanks(facing, posInMultiblock()), this, facing, posInMultiblock());
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }

    public static class GasTurbineFluidHandler implements IFluidHandler {
        private final IFluidTank[] accessibleTanks;
        private final TileEntityGasTurbineMaster master;
        private final EnumFacing side;
        private final BlockPos position;

        public GasTurbineFluidHandler(IFluidTank[] accessibleTanks, TileEntityGasTurbineMaster master, EnumFacing side, BlockPos position) {
            this.accessibleTanks = accessibleTanks;
            this.master = master;
            this.side = side;
            this.position = position;
        }

        private int getTankIndex(IFluidTank tank) {
            for (int i = 0; i < master.tanks.length; i++) if (master.tanks[i] == tank) return i;
            return -1;
        }

        @Override public IFluidTankProperties[] getTankProperties() {
            java.util.List<IFluidTankProperties> list = new java.util.ArrayList<>();
            for (IFluidTank tank : accessibleTanks) {
                int index = getTankIndex(tank);
                boolean canFill = index == 0;
                boolean canDrain = index == 1;
                list.add(new FluidTankProperties(tank.getFluid(), tank.getCapacity(), canFill, canDrain));
            }
            return list.toArray(new IFluidTankProperties[0]);
        }

        @Override public int fill(FluidStack resource, boolean doFill) {
            if (resource == null) return 0;
            resource = resource.copy();
            int filled = 0;
            for (IFluidTank accessible : accessibleTanks) {
                int iTank = getTankIndex(accessible);
                if (iTank != -1 && master.canFillTankFrom(iTank, side, resource, position)) {
                    int f = accessible.fill(resource, doFill);
                    filled += f;
                    resource.amount -= f;
                    if (doFill && f > 0) master.TankContentsChanged();
                    if (resource.amount <= 0) return filled;
                }
            }
            return filled;
        }

        @Override public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null) return null;
            resource = resource.copy();
            FluidStack drained = null;
            for (IFluidTank accessible : accessibleTanks) {
                int iTank = getTankIndex(accessible);
                if (iTank != -1 && master.canDrainTankFrom(iTank, side, position)) {
                    FluidStack tf = accessible.getFluid();
                    if (tf != null && tf.isFluidEqual(resource)) {
                        int amount = Math.min(resource.amount, tf.amount);
                        FluidStack d = accessible.drain(amount, doDrain);
                        if (d != null) {
                            if (drained == null) drained = d.copy();
                            else drained.amount += d.amount;
                            if (doDrain && d.amount > 0) master.TankContentsChanged();
                            resource.amount -= d.amount;
                            if (resource.amount <= 0) return drained;
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
                        if (drained == null) drained = d.copy();
                        else if (drained.isFluidEqual(d)) drained.amount += d.amount;
                        toDrain -= d.amount;
                        if (doDrain && d.amount > 0) master.TankContentsChanged();
                        if (toDrain <= 0) return drained;
                    }
                }
            }
            return drained;
        }
    }
}
