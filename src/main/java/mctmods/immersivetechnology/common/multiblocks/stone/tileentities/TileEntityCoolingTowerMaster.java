package mctmods.immersivetechnology.common.multiblocks.stone.tileentities;

import com.immersiveconvergence.api.client.ICClientUtils;
import com.immersiveconvergence.api.client.ICSoundHandler;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.IComparatorOverride;
import com.immersiveconvergence.api.multiblock.PoICache;
import com.immersiveconvergence.api.network.BinaryTileSyncMessage;
import com.immersiveconvergence.api.network.IBinaryMessageReceiver;
import com.immersiveconvergence.api.particles.ParticleSmokeCustom;
import com.immersiveconvergence.api.util.ICFluidTank.TankListener;
import com.immersiveconvergence.api.util.ICFluidTank;
import com.immersiveconvergence.api.util.ICUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mctmods.immersivetechnology.api.crafting.CoolingTowerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.Config.ITConfig;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.stone.process.CoolingTowerProcess;
import mctmods.immersivetechnology.conversion.CoolingTowerLegacyConverter;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
import mctmods.immersivetechnology.common.util.compat.advancedrocketry.AdvancedRocketryHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TileEntityCoolingTowerMaster extends TileEntityCoolingTowerSlave implements TankListener, IBinaryMessageReceiver, IComparatorOverride {
    private static int inputTankSize() { return Multiblocks.coolingTower.coolingTower_input_tankSize; }

    private static int outputTankSize() { return Multiblocks.coolingTower.coolingTower_output_tankSize; }

    public FluidTank[] tanks = new FluidTank[] {
            new ICFluidTank(inputTankSize(), this),
            new ICFluidTank(inputTankSize(), this),
            new ICFluidTank(outputTankSize(), this),
            new ICFluidTank(outputTankSize(), this),
            new ICFluidTank(outputTankSize(), this)
    };

    private CoolingTowerRecipe cachedCoolingRecipe;

    public final List<CoolingTowerProcess> processQueue = new ArrayList<>();

    private float soundVolume = 0f;
    private int soundGracePeriod = 0;
    private boolean isRunning = false;

    private int tickCountdown = 20;
    private int oldComparatorOutput;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
        tanks[3].readFromNBT(nbt.getCompoundTag("tank3"));
        tanks[4].readFromNBT(nbt.getCompoundTag("tank4"));
        processQueue.clear();
        NBTTagList queue = nbt.getTagList("processQueue", 10);
        for (int i = 0; i < queue.tagCount(); i++) {
            CoolingTowerProcess process = CoolingTowerProcess.readFromNBT(queue.getCompoundTagAt(i));
            if (process != null) { processQueue.add(process); }
        }
        oldComparatorOutput = nbt.getInteger("oldComparatorOutput");
        soundGracePeriod = nbt.getInteger("soundGracePeriod");
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank2", tanks[2].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank3", tanks[3].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank4", tanks[4].writeToNBT(new NBTTagCompound()));
        NBTTagList queue = new NBTTagList();
        for (CoolingTowerProcess process : processQueue) { queue.appendTag(process.writeToNBT()); }
        nbt.setTag("processQueue", queue);
        nbt.setInteger("oldComparatorOutput", oldComparatorOutput);
        nbt.setInteger("soundGracePeriod", soundGracePeriod);
    }

    @SideOnly(Side.CLIENT)
    public void spawnParticles() {
        BlockPos particlePos = poiWorldPos("particle0");
        if (!isRunning) return;
        Random rand = world.rand;
        int lessParticleSetting = ICClientUtils.mc().gameSettings.particleSetting;
        if (lessParticleSetting == 2 || (lessParticleSetting == 1 && rand.nextInt(3) == 0)) return;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distanceLimit = 64;
        if (particlePos.distanceSq(player.posX, player.posY, player.posZ) > distanceLimit * distanceLimit) return;
        double height = ITConfig.Client.particles.custom_smoke_height;
        for (int i = 0; i < 3; i++) {
            ParticleSmokeCustom cloud = new ParticleSmokeCustom(world,
                    particlePos.getX() + .5 + (rand.nextFloat() * 4f - 2f),
                    particlePos.getY() + .5 + rand.nextFloat() * 2f,
                    particlePos.getZ() + .5 + (rand.nextFloat() * 4f - 2f),
                    (rand.nextFloat() - 0.5) * 0.02, (0.01 + rand.nextFloat() * 0.02) * height, (rand.nextFloat() - 0.5) * 0.02, 7);
            cloud.setRBGColorF(1, 1, 1);
            ICClientUtils.mc().effectRenderer.addEffect(cloud);
        }
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        BlockPos soundPos = poiWorldPos("sound0");
        float targetSoundLevel = isRunning ? 1f : 0f;
        if (soundVolume < targetSoundLevel) { soundVolume = Math.min(soundVolume + 0.01f, targetSoundLevel); }
        else if (soundVolume > targetSoundLevel) { soundVolume = Math.max(soundVolume - 0.01f, targetSoundLevel); }
        if (soundVolume <= 0f) { ICSoundHandler.stopSound(soundPos); }
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            double distance = Math.sqrt(player.getDistanceSq(soundPos.getX() + .5, soundPos.getY() + .5, soundPos.getZ() + .5));
            ITSounds.coolingTower.PlayRepeating(soundPos, soundVolume * (float)Math.max(1 - distance / 16, 0), 1);
        }
    }

    @Override public void disassemble() {
        super.disassemble();
    }

    @Override public void receiveMessageFromServer(ByteBuf message) { isRunning = message.readBoolean(); }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {}

    public void notifyNearbyClients() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeBoolean(isRunning);
        BinaryTileSyncMessage.sendToAllTracking(world, getPos(), buf);
    }

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    @Override public void update() {
        if (!formed) return;
        if (!world.isRemote && world.getBlockState(getPos()).getBlock() == ITContent.blockMetalMultiblock) {
            CoolingTowerLegacyConverter.convert(this);
            return;
        }
        if (world.isRemote) {
            handleSounds();
            spawnParticles();
            return;
        }
        super.update();
        boolean update = pumpOutputOut();
        boolean prevIsRunning = isRunning;
        update |= recipeLogic();
        if (!processQueue.isEmpty()) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;
        isRunning = soundGracePeriod > 0;

        if (prevIsRunning != isRunning) update = true;
        if (update && tickCountdown-- <= 0) {
            notifyNearbyClients();
            tickCountdown = 20;
        }
        if (update) {
            efficientMarkDirty();
            if (isRunning != prevIsRunning) { markContainingBlockForUpdate(null); }
            else { throttledBlockUpdate(); }
        }
        int comp = comparatorValue();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            notifyComparators();
        }
    }

    private boolean pumpOutputOut() {
        boolean changed = false;
        PoICache[] outputs = {poi("fluid_output0"), poi("fluid_output1"), poi("fluid_output2")};
        BlockPos[] fronts = {poiFrontPos("fluid_output0"), poiFrontPos("fluid_output1"), poiFrontPos("fluid_output2")};
        int[] indices = {2, 3, 4};
        for (int i = 0; i < 3; i++) {
            if (tanks[indices[i]].getFluidAmount() > 0) {
                IFluidHandler output = FluidUtil.getFluidHandler(world, fronts[i], outputs[i].facing.getOpposite());
                if (output != null) {
                    FluidStack out = tanks[indices[i]].getFluid();
                    if (out == null) continue;
                    int accepted = output.fill(out, false);
                    if (accepted > 0) {
                        int drained = output.fill(ICUtils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                        tanks[indices[i]].drain(drained, true);
                        if (drained > 0) changed = true;
                    }
                }
            }
        }
        return changed;
    }

    private boolean recipeLogic() {
        boolean update = false;
        double speed = getSpeedMultiplier();
        for (int i = processQueue.size() - 1; i >= 0; i--) {
            CoolingTowerProcess process = processQueue.get(i);
            process.tick(tanks, speed);
            if (process.isComplete()) { processQueue.remove(i); }
            update = true;
        }
        if (speed <= 0 || processQueue.size() >= getProcessQueueMaxLength()) { return update; }
        FluidStack in0 = tanks[0].getFluid();
        FluidStack in1 = tanks[1].getFluid();
        cachedCoolingRecipe = CoolingTowerRecipe.findRecipe(in0, in1);
        boolean swapped = false;
        if (cachedCoolingRecipe == null) {
            cachedCoolingRecipe = CoolingTowerRecipe.findRecipe(in1, in0);
            swapped = true;
        }
        if (cachedCoolingRecipe == null) { return update; }
        boolean canOutput = true;
        if (cachedCoolingRecipe.fluidOutput0 != null) canOutput &= tanks[2].fill(cachedCoolingRecipe.fluidOutput0, false) == cachedCoolingRecipe.fluidOutput0.amount;
        if (cachedCoolingRecipe.fluidOutput1 != null) canOutput &= tanks[3].fill(cachedCoolingRecipe.fluidOutput1, false) == cachedCoolingRecipe.fluidOutput1.amount;
        if (cachedCoolingRecipe.fluidOutput2 != null) canOutput &= tanks[4].fill(cachedCoolingRecipe.fluidOutput2, false) == cachedCoolingRecipe.fluidOutput2.amount;
        if (!canOutput) { return update; }
        processQueue.add(new CoolingTowerProcess(cachedCoolingRecipe, swapped));
        return true;
    }

    private double getSpeedMultiplier() {
        if (ITCompatModule.isAdvancedRocketryLoaded && AdvancedRocketryHelper.isAtmosphereUnsuitableForCooling(world, getPos())) { return 0; }
        double tempFactor = Multiblocks.coolingTower.coolingTower_biome_temp_factor;
        double humidityFactor = Multiblocks.coolingTower.coolingTower_biome_humidity_factor;
        if (tempFactor > 0 && world.provider.isNether()) { return 0; }
        double multiplier = 1.0;
        if (tempFactor > 0 || humidityFactor > 0) {
            if (tempFactor > 0) { multiplier -= (world.getBiome(getPos()).getDefaultTemperature() - 0.8) * tempFactor; }
            if (humidityFactor > 0) { multiplier += 0.075 * humidityFactor * -((world.getBiome(getPos()).getRainfall() - 0.5) / 0.5); }
        }
        if (ITCompatModule.isAdvancedRocketryLoaded) { multiplier *= AdvancedRocketryHelper.getHeatTransferCoefficient(world, getPos()); }
        return Math.max(multiplier, 0);
    }

    @Override public void TankContentsChanged() {
        if (processQueue.isEmpty()) { cachedCoolingRecipe = null; }
        efficientMarkDirty();
        requestClientSync();
    }

    @Override public int getComparatorInputOverride() { return isComparatorPos() ? comparatorValue() : 0; }

    public int comparatorValue() { return 15 * processQueue.size() / getProcessQueueMaxLength(); }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityCoolingTowerMaster master() { return this; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() { return tanks; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(EnumFacing side, BlockPos position) {
        if (isPoI("fluid_input0", side, position)) return tankView(0, tanks[0]);
        if (isPoI("fluid_input1", side, position)) return tankView(1, tanks[1]);
        if (isPoI("fluid_output0", side, position)) return tankView(2, tanks[2]);
        if (isPoI("fluid_output1", side, position)) return tankView(3, tanks[3]);
        if (isPoI("fluid_output2", side, position)) return tankView(4, tanks[4]);
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        if (iTank > 1 || iTank < 0) return false;
        if (iTank == 0 && !isPoI("fluid_input0", side, position)) return false;
        if (iTank == 1 && !isPoI("fluid_input1", side, position)) return false;
        if (tanks[iTank].getFluidAmount() >= tanks[iTank].getCapacity()) return false;
        FluidStack current = tanks[iTank].getFluid();
        if (current != null) { return resource.isFluidEqual(current); }
        return true;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        if (iTank < 2) return false;
        if (iTank == 2 && !isPoI("fluid_output0", side, position)) return false;
        if (iTank == 3 && !isPoI("fluid_output1", side, position)) return false;
        if (iTank == 4 && !isPoI("fluid_output2", side, position)) return false;
        return tanks[iTank].getFluidAmount() > 0;
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return ITUtils.EMPTY_INT_ARRAY; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return ITUtils.EMPTY_INT_ARRAY; }
}
