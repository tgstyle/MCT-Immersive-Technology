package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.CoolingTowerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartCoolingTower;
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

    private PoICache fluidInput0, fluidInput1, fluidOutput0, fluidOutput1, fluidOutput2;
    private BlockPos particlePos0, soundPos0;
    private BlockPos outputFront0, outputFront1, outputFront2;

    private float soundVolume;
    private int gracePeriod = 60;
    private int clientUpdateCooldown = 1;
    private boolean isRunning;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
        tanks[3].readFromNBT(nbt.getCompoundTag("tank3"));
        tanks[4].readFromNBT(nbt.getCompoundTag("tank4"));
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank2", tanks[2].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank3", tanks[3].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank4", tanks[4].writeToNBT(new NBTTagCompound()));
    }

    @SideOnly(Side.CLIENT)
    public void spawnParticles() {
        if(!isRunning) return;
        if(particlePos0 == null) InitializePoIs();
        Random rand = new Random();
        if(rand.nextInt(40) == 0) return;
        int lessParticleSetting = ClientUtils.mc().gameSettings.particleSetting;
        if(lessParticleSetting == 2 || lessParticleSetting == 1 && rand.nextInt(3) == 0) return;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distanceLimit = 64;
        if(particlePos0.distanceSq(player.posX, player.posY, player.posZ) > distanceLimit * distanceLimit) return;
        ParticleSmokeCustom cloud = new ParticleSmokeCustom(world,
                particlePos0.getX() + 2 - rand.nextFloat() * 3,
                particlePos0.getY(),
                particlePos0.getZ() + 2 - rand.nextFloat() * 3, 0, 0.02f, 0, 7);
        cloud.setRBGColorF(1,1,1);
        ClientUtils.mc().effectRenderer.addEffect(cloud);
    }

    @Override public void receiveMessageFromServer(ByteBuf message) { isRunning = message.readBoolean(); }

    public void handleSounds() {
        if(soundPos0 == null) InitializePoIs();
        if(isRunning) { if(soundVolume < 1) soundVolume += 0.01f; }
        else { if(soundVolume > 0) soundVolume -= 0.01f; }
        if(soundVolume == 0) { ITSoundHandler.StopSound(soundPos0); }
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float) player.getDistanceSq(soundPos0.getX(), soundPos0.getY(), soundPos0.getZ()) / 8, 1);
            ITSounds.coolingTower.PlayRepeating(soundPos0, (10 * soundVolume) / attenuation, 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() { ITSoundHandler.StopSound(soundPos0); super.onChunkUnload(); }

    @Override public void disassemble() {
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
        super.disassemble();
    }

    @Override public void update() {
        if (formed && fluidInput0 == null) InitializePoIs();
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

    @Override public void TankContentsChanged() { cachedRecipe = null; this.markContainingBlockForUpdate(null); }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityCoolingTowerMaster master() { master = this; return this; }

    private void InitializePoIs() {
        for(PoIJSONSchema poi : TileEntityITMultiblockPartCoolingTower.instance.pointsOfInterest) {
            switch(poi.name) {
                case "fluid_input0":
                    fluidInput0 = new PoICache(this.facing, poi, this.mirrored);
                    break;
                case "fluid_input1":
                    fluidInput1 = new PoICache(this.facing, poi, this.mirrored);
                    break;
                case "fluid_output0":
                    fluidOutput0 = new PoICache(this.facing, poi, this.mirrored); outputFront0 = getBlockPosForPos(fluidOutput0.position).offset(fluidOutput0.facing);
                    break;
                case "fluid_output1":
                    fluidOutput1 = new PoICache(this.facing, poi, this.mirrored); outputFront1 = getBlockPosForPos(fluidOutput1.position).offset(fluidOutput1.facing);
                    break;
                case "fluid_output2":
                    fluidOutput2 = new PoICache(this.facing, poi, this.mirrored); outputFront2 = getBlockPosForPos(fluidOutput2.position).offset(fluidOutput2.facing);
                    break;
                case "particle0":
                    particlePos0 = getBlockPosForPos(poi.position);
                    break;
                case "sound0":
                    soundPos0 = getBlockPosForPos(poi.position);
                    break;
            }
        }
        if(!world.isRemote) notifyIONeighbors();
    }

    public @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if(fluidInput0 == null) InitializePoIs();
        if(side == null) return tanks;
        if(fluidInput0.isPoI(side, position)) return new IFluidTank[]{tanks[0]};
        if(fluidInput1.isPoI(side, position)) return new IFluidTank[]{tanks[1]};
        if(fluidOutput0.isPoI(side, position)) return new IFluidTank[]{tanks[2]};
        if(fluidOutput1.isPoI(side, position)) return new IFluidTank[]{tanks[3]};
        if(fluidOutput2.isPoI(side, position)) return new IFluidTank[]{tanks[4]};
        return ITUtils.emptyIFluidTankList;
    }

    public boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if(fluidInput0 == null) InitializePoIs();
        if(fluidInput0.isPoI(side, position)) {
            FluidTank tank = tanks[0];
            if(tank.getFluidAmount() >= tank.getCapacity()) return false;
            if(tank.getFluid() == null) return CoolingTowerRecipe.findRecipeByFluid0(resource.getFluid()) != null;
            else return resource.getFluid().getName().equals(tank.getFluid().getFluid().getName());
        }
        if(fluidInput1.isPoI(side, position)) {
            FluidTank tank = tanks[1];
            if(tank.getFluidAmount() >= tank.getCapacity()) return false;
            if(tank.getFluid() == null) return CoolingTowerRecipe.findRecipeByFluid1(resource.getFluid()) != null;
            else return resource.getFluid().getName().equals(tank.getFluid().getFluid().getName());
        }
        return false;
    }

    public boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if(fluidInput0 == null) InitializePoIs();
        if(fluidOutput0.isPoI(side, position)) return tanks[2].getFluidAmount() > 0;
        if(fluidOutput1.isPoI(side, position)) return tanks[3].getFluidAmount() > 0;
        if(fluidOutput2.isPoI(side, position)) return tanks[4].getFluidAmount() > 0;
        return false;
    }

    private void pumpOutputOut() {
        IFluidHandler output;
        if(tanks[2].getFluidAmount() > 0) {
            output = FluidUtil.getFluidHandler(world, outputFront0, fluidOutput0.facing.getOpposite());
            if(output != null) {
                FluidStack out = tanks[2].getFluid();
                int accepted = output.fill(out, false);
                if(accepted > 0) {
                    assert out != null;
                    int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                    tanks[2].drain(drained, true);
                }
            }
        }
        if(tanks[3].getFluidAmount() > 0) {
            output = FluidUtil.getFluidHandler(world, outputFront1, fluidOutput1.facing.getOpposite());
            if(output != null) {
                FluidStack out = tanks[3].getFluid();
                int accepted = output.fill(out, false);
                if(accepted > 0) {
                    assert out != null;
                    int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                    tanks[3].drain(drained, true);
                }
            }
        }
        if(tanks[4].getFluidAmount() > 0) {
            output = FluidUtil.getFluidHandler(world, outputFront2, fluidOutput2.facing.getOpposite());
            if(output != null) {
                FluidStack out = tanks[4].getFluid();
                int accepted = output.fill(out, false);
                if(accepted > 0) {
                    assert out != null;
                    int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                    tanks[4].drain(drained, true);
                }
            }
        }
    }

    public void notifyNearbyClients() {
        ByteBuf buffer = Unpooled.copyBoolean(isRunning);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendToAllAround(new BinaryMessageTileSync(center, buffer), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 40));
    }

    private void notifyIONeighbors() {
        if(world.isRemote) return;
        notifyNeighbor(getBlockPosForPos(fluidInput0.position));
        notifyNeighbor(getBlockPosForPos(fluidInput1.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput0.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput1.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput2.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override public void onProcessFinish(@Nonnull MultiblockProcess<CoolingTowerRecipe> process) {
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

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }
}
