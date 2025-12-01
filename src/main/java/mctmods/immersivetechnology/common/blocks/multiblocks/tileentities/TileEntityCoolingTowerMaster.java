package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.CoolingTowerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.blocks.multiblocks.tileentitiesmultiblockpart.TileEntityITMultiblockPartCoolingTower;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.api.particles.ParticleSmokeCustom;
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
import net.minecraft.nbt.NBTTagCompound;
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
import java.util.Random;

public class TileEntityCoolingTowerMaster extends TileEntityCoolingTowerSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver {
    private static final int inputTankSize = Multiblocks.coolingTower.coolingTower_input_tankSize;
    private static final int outputTankSize = Multiblocks.coolingTower.coolingTower_output_tankSize;

    public FluidTank[] tanks = new FluidTank[] {
            new ITFluidTank(inputTankSize, this),
            new ITFluidTank(inputTankSize, this),
            new ITFluidTank(outputTankSize, this),
            new ITFluidTank(outputTankSize, this),
            new ITFluidTank(outputTankSize, this)
    };

    CoolingTowerRecipe recipe;
    private CoolingTowerRecipe cachedRecipe;

    private PoICache input0, input1, output0, output1, output2;
    private BlockPos particleOrigin, soundOrigin, output0Front, output1Front, output2Front;

    private float soundVolume;
    private int gracePeriod = 60;
    private int clientUpdateCooldown = 1;
    private boolean isRunning;

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); tanks[0].readFromNBT(nbt.getCompoundTag("tank0")); tanks[1].readFromNBT(nbt.getCompoundTag("tank1")); tanks[2].readFromNBT(nbt.getCompoundTag("tank2")); tanks[3].readFromNBT(nbt.getCompoundTag("tank3")); tanks[4].readFromNBT(nbt.getCompoundTag("tank4")); }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound())); nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound())); nbt.setTag("tank2", tanks[2].writeToNBT(new NBTTagCompound())); nbt.setTag("tank3", tanks[3].writeToNBT(new NBTTagCompound())); nbt.setTag("tank4", tanks[4].writeToNBT(new NBTTagCompound())); }

    @SideOnly(Side.CLIENT)
    public void spawnParticles() {
        if(!isRunning) return;
        if(particleOrigin == null) InitializePoIs();
        Random rand = new Random();
        if(rand.nextInt(40) == 0) return;
        int lessParticleSetting = ClientUtils.mc().gameSettings.particleSetting;
        if(lessParticleSetting == 2 || lessParticleSetting == 1 && rand.nextInt(3) == 0) return;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distanceLimit = 64;
        if(particleOrigin.distanceSq(player.posX, player.posY, player.posZ) > distanceLimit * distanceLimit) return;
        ParticleSmokeCustom cloud = new ParticleSmokeCustom(world,
                particleOrigin.getX() + 2 - rand.nextFloat() * 3,
                particleOrigin.getY(),
                particleOrigin.getZ() + 2 - rand.nextFloat() * 3, 0, 0.02f, 0, 7);
        cloud.setRBGColorF(1,1,1);
        ClientUtils.mc().effectRenderer.addEffect(cloud);
    }

    @Override
    public void receiveMessageFromServer(ByteBuf message) { isRunning = message.readBoolean(); }

    public void handleSounds() {
        if(soundOrigin == null) InitializePoIs();
        if(isRunning) { if(soundVolume < 1) soundVolume += 0.01f; }
        else { if(soundVolume > 0) soundVolume -= 0.01f; }
        if(soundVolume == 0) { ITSoundHandler.StopSound(soundOrigin); }
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float) player.getDistanceSq(soundOrigin.getX(), soundOrigin.getY(), soundOrigin.getZ()) / 8, 1);
            ITSounds.coolingTower.PlayRepeating(soundOrigin, (10 * soundVolume) / attenuation, 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onChunkUnload() { ITSoundHandler.StopSound(soundOrigin); super.onChunkUnload(); }

    @Override
    public void disassemble() {
        if(soundOrigin == null) InitializePoIs();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundOrigin), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundOrigin.getX(), soundOrigin.getY(), soundOrigin.getZ(), 0));
        super.disassemble();
    }

    @Override
    public void update() {
        super.update();
        if(world.isRemote) { handleSounds(); spawnParticles(); return; }
        if(ITCompatModule.isAdvancedRocketryLoaded && AdvancedRocketryHelper.isAtmosphereUnsuitableForCooling(world, getPos())) return;
        boolean update = false;
        pumpOutputOut();
        if(processQueue.size() < this.getProcessQueueMaxLength()) {
            if(tanks[0].getFluidAmount() > 0 || tanks[1].getFluidAmount() > 0) {
                FluidStack in0 = tanks[0].getFluid();
                FluidStack in1 = tanks[1].getFluid();
                cachedRecipe = CoolingTowerRecipe.findRecipe(in0, in1);
                boolean swapped = false;
                if(cachedRecipe == null) {
                    cachedRecipe = CoolingTowerRecipe.findRecipe(in1, in0);
                    swapped = true;
                }
                recipe = cachedRecipe;
                if(recipe != null) {
                    boolean canOutput = true;
                    if(recipe.fluidOutput0 != null) canOutput &= tanks[2].fill(recipe.fluidOutput0, false) == recipe.fluidOutput0.amount;
                    if(recipe.fluidOutput1 != null) canOutput &= tanks[3].fill(recipe.fluidOutput1, false) == recipe.fluidOutput1.amount;
                    if(recipe.fluidOutput2 != null) canOutput &= tanks[4].fill(recipe.fluidOutput2, false) == recipe.fluidOutput2.amount;
                    if(canOutput) {
                        @SuppressWarnings("unchecked")
                        MultiblockProcessInMachine<CoolingTowerRecipe> process = new MultiblockProcessInMachine<>(recipe).setInputTanks(swapped ? 1 : 0, swapped ? 0 : 1);
                        if(ITCompatModule.isAdvancedRocketryLoaded) process.maxTicks *= (int) (1 / AdvancedRocketryHelper.getHeatTransferCoefficient(world, getPos()));
                        boolean canAdd = this.addProcessToQueue(process, true);
                        if(canAdd) {
                            this.addProcessToQueue(process, false);
                            update = true;
                        }
                    }
                }
            }
        }
        if(tickedProcesses > 0) { isRunning = true; gracePeriod = 60; }
        else { if(gracePeriod == 0) isRunning = false; else gracePeriod--; }
        if(clientUpdateCooldown > 1) clientUpdateCooldown--;
        else { notifyNearbyClients(); clientUpdateCooldown = 20; }
        if(update) { efficientMarkDirty(); this.markContainingBlockForUpdate(null); }
    }

    public void efficientMarkDirty() { world.getChunk(this.getPos()).markDirty(); }

    @Override
    public void TankContentsChanged() { cachedRecipe = null; this.markContainingBlockForUpdate(null); }

    @Override
    public boolean isDummy() { return false; }

    @Override
    public TileEntityCoolingTowerMaster master() { master = this; return this; }

    private void InitializePoIs() {
        for(PoIJSONSchema poi : TileEntityITMultiblockPartCoolingTower.instance.pointsOfInterest) {
            switch(poi.name) {
                case "input0": input0 = new PoICache(facing, poi, mirrored); break;
                case "input1": input1 = new PoICache(facing, poi, mirrored); break;
                case "output0": output0 = new PoICache(facing, poi, mirrored); break;
                case "output1": output1 = new PoICache(facing, poi, mirrored); break;
                case "output2": output2 = new PoICache(facing, poi, mirrored); break;
                case "particle": particleOrigin = getBlockPosForPos(poi.position); break;
                case "sound": soundOrigin = getBlockPosForPos(poi.position); break;
            }
        }
        output0Front = getBlockPosForPos(output0.position).offset(output0.facing);
        output1Front = getBlockPosForPos(output1.position).offset(output1.facing);
        output2Front = getBlockPosForPos(output2.position).offset(output2.facing);
        if(!world.isRemote) notifyIONeighbors();
    }

    public @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if(input0 == null) InitializePoIs();
        if(side == null) return tanks;
        if(input0.isPoI(side, position)) return new FluidTank[] {tanks[0]};
        else if(input1.isPoI(side, position)) return new FluidTank[] {tanks[1]};
        else if(output0.isPoI(side, position)) return new FluidTank[] {tanks[2]};
        else if(output1.isPoI(side, position)) return new FluidTank[] {tanks[3]};
        else if(output2.isPoI(side, position)) return new FluidTank[] {tanks[4]};
        return ITUtils.emptyIFluidTankList;
    }

    public boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if(input0.isPoI(side, position)) {
            if(tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
            if(tanks[0].getFluid() == null) return CoolingTowerRecipe.findRecipeByFluid0(resource.getFluid()) != null;
            else return resource.getFluid().getName().equals(tanks[0].getFluid().getFluid().getName());
        } else if(input1.isPoI(side, position)) {
            if(tanks[1].getFluidAmount() >= tanks[1].getCapacity()) return false;
            if(tanks[1].getFluid() == null) return CoolingTowerRecipe.findRecipeByFluid1(resource.getFluid()) != null;
            else return resource.getFluid().getName().equals(tanks[1].getFluid().getFluid().getName());
        }
        return false;
    }

    public boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if(output0.isPoI(side, position)) return tanks[2].getFluidAmount() > 0;
        else if(output1.isPoI(side, position)) return tanks[3].getFluidAmount() > 0;
        else if(output2.isPoI(side, position)) return tanks[4].getFluidAmount() > 0;
        return false;
    }

    private void pumpOutputOut() {
        IFluidHandler output;
        if(tanks[2].getFluidAmount() > 0 && (output = FluidUtil.getFluidHandler(world, output0Front, output0.facing.getOpposite())) != null) {
            FluidStack out = tanks[2].getFluid();
            int accepted = output.fill(out, false);
            if(accepted == 0) return;
            assert out != null;
            int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
            this.tanks[2].drain(drained, true);
        }
        if(tanks[3].getFluidAmount() > 0 && (output = FluidUtil.getFluidHandler(world, output1Front, output1.facing.getOpposite())) != null) {
            FluidStack out = tanks[3].getFluid();
            int accepted = output.fill(out, false);
            if(accepted == 0) return;
            assert out != null;
            int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
            this.tanks[3].drain(drained, true);
        }
        if(tanks[4].getFluidAmount() > 0 && (output = FluidUtil.getFluidHandler(world, output2Front, output2.facing.getOpposite())) != null) {
            FluidStack out = tanks[4].getFluid();
            int accepted = output.fill(out, false);
            if(accepted == 0) return;
            assert out != null;
            int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
            this.tanks[4].drain(drained, true);
        }
    }

    public void notifyNearbyClients() {
        ByteBuf buffer = Unpooled.copyBoolean(isRunning);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendToAllAround(new BinaryMessageTileSync(center, buffer), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 40));
    }

    private void notifyIONeighbors() {
        if(world.isRemote) return;
        notifyNeighbor(getBlockPosForPos(input0.position));
        notifyNeighbor(getBlockPosForPos(input1.position));
        notifyNeighbor(getBlockPosForPos(output0.position));
        notifyNeighbor(getBlockPosForPos(output1.position));
        notifyNeighbor(getBlockPosForPos(output2.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override
    public void onProcessFinish(@Nonnull MultiblockProcess<CoolingTowerRecipe> process) {
        if(process.recipe.fluidOutput0 != null) {
            tanks[2].fill(process.recipe.fluidOutput0.copy(), true);
        }
        if(process.recipe.fluidOutput1 != null) {
            tanks[3].fill(process.recipe.fluidOutput1.copy(), true);
        }
        if(process.recipe.fluidOutput2 != null) {
            tanks[4].fill(process.recipe.fluidOutput2.copy(), true);
        }
    }
}
