package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.api.crafting.CoolingTowerRecipe;
import mctmods.immersivetechnology.common.Config;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockCoolingTower;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ParticleSmokeCustomSize;
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

import java.util.Random;

public class TileEntityCoolingTowerMaster extends TileEntityCoolingTowerSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver {

    private static int inputTankSize = Config.ITConfig.Machines.CoolingTower.coolingTower_input_tankSize;
    private static int outputTankSize = Config.ITConfig.Machines.CoolingTower.coolingTower_output_tankSize;

    public FluidTank[] tanks = new FluidTank[] {
            new ITFluidTank(inputTankSize, this),
            new ITFluidTank(inputTankSize, this),
            new ITFluidTank(outputTankSize, this),
            new ITFluidTank(outputTankSize, this)
    };

    CoolingTowerRecipe recipe;

    @Override
    public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
        tanks[3].readFromNBT(nbt.getCompoundTag("tank3"));
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank2", tanks[2].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank3", tanks[3].writeToNBT(new NBTTagCompound()));
    }

    @SideOnly(Side.CLIENT)
    public void spawnParticles() {
        if(!isRunning) return;
        Random rand = new Random();
        if(rand.nextInt(40) == 0) return;
        int lessParticleSetting = ClientUtils.mc().gameSettings.particleSetting;
        if(lessParticleSetting == 2 || lessParticleSetting == 1 && rand.nextInt(3) == 0) return;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distanceLimit = 64;
        if(particleOrigin.distanceSq(player.posX, player.posY, player.posZ) > distanceLimit * distanceLimit) return;
        ParticleSmokeCustomSize cloud = new ParticleSmokeCustomSize(world,
                particleOrigin.getX() + 2 - rand.nextFloat() * 3,
                particleOrigin.getY(),
                particleOrigin.getZ() + 2 - rand.nextFloat() * 3, 0, 0.02f, 0, 7);
        cloud.setRBGColorF(1,1,1);
        ClientUtils.mc().effectRenderer.addEffect(cloud);
    }

    public void notifyNearbyClients() {
        ByteBuf buffer = Unpooled.copyBoolean(isRunning);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendToAllAround(new BinaryMessageTileSync(center, buffer), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 40));
    }

    @Override
    public void receiveMessageFromServer(ByteBuf message) {
        isRunning = message.readBoolean();
    }

    private float soundVolume;
    private int gracePeriod = 60;
    private int clientUpdateCooldown = 1;
    private boolean isRunning;

    public void handleSounds() {
        if(isRunning) {
            if(soundVolume < 1) soundVolume += 0.01f;
        } else {
            if(soundVolume > 0) soundVolume -= 0.01f;
        }
        if(soundVolume == 0) ITSoundHandler.StopSound(soundOrigin);
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float) player.getDistanceSq(soundOrigin.getX(), soundOrigin.getY(), soundOrigin.getZ()) / 8, 1);
            ITSounds.coolingTower.PlayRepeating(soundOrigin, (10 * soundVolume) / attenuation, 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onChunkUnload() {
        ITSoundHandler.StopSound(soundOrigin);
        super.onChunkUnload();
    }

    @Override
    public void disassemble() {
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundOrigin), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundOrigin.getX(), soundOrigin.getY(), soundOrigin.getZ(), 0));
        super.disassemble();
    }

    @Override
    public void update() {
        super.update();
        if(world.isRemote) {
            if(input0 == null) InitializePoIs();
            handleSounds();
            spawnParticles();
            return;
        }
        pumpOutputOut();
        boolean update = false;
        if(processQueue.size() < this.getProcessQueueMaxLength()) {
            if(tanks[0].getFluidAmount() > 0 || tanks[1].getFluidAmount() > 0) {
                recipe = CoolingTowerRecipe.findRecipe(tanks[0].getFluid(), tanks[1].getFluid());
                if(recipe != null) {
                    @SuppressWarnings("unchecked")
					MultiblockProcessInMachine<CoolingTowerRecipe> process =
                            new MultiblockProcessInMachine<>(recipe).setInputTanks(new int[]{0, 1});
                    if(this.addProcessToQueue(process, true)) {
                        this.addProcessToQueue(process, false);
                        update = true;
                    }
                }
            }
        }

        if (tickedProcesses > 0) {
            isRunning = true;
            gracePeriod = 60;
        } else {
            if (gracePeriod == 0) isRunning = false;
            else gracePeriod--;
        }

        if(clientUpdateCooldown > 1) clientUpdateCooldown--;
        else {
            notifyNearbyClients();
            clientUpdateCooldown = 20;
        }
        if(update) {
            efficientMarkDirty();
            this.markContainingBlockForUpdate(null);
        }
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
    public TileEntityCoolingTowerMaster master() {
        master = this;
        return this;
    }

    private PoICache input0, input1, output0, output1;
    private BlockPos particleOrigin, soundOrigin, output0Front, output1Front;

    private void InitializePoIs() {
        for(PoIJSONSchema poi : MultiblockCoolingTower.instance.pointsOfInterest) {
            if(poi.name.equals("input0")) input0 = new PoICache(facing, poi, mirrored);
            else if(poi.name.equals("input1")) input1 = new PoICache(facing, poi, mirrored);
            else if(poi.name.equals("output0")) {
                output0 = new PoICache(facing, poi, mirrored);
                output0Front = getBlockPosForPos(output0.position).offset(output0.facing);
            } else if(poi.name.equals("output1")) {
                output1 = new PoICache(facing, poi, mirrored);
                output1Front = getBlockPosForPos(output1.position).offset(output1.facing);
            } else if(poi.name.equals("particle")) particleOrigin = getBlockPosForPos(poi.position);
            else if(poi.name.equals("sound")) soundOrigin = getBlockPosForPos(poi.position);
        }
    }

    public IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if(input0 == null) InitializePoIs();
        if(side == null) return tanks;
        if(input0.isPoI(side, position)) return new FluidTank[] {tanks[0]};
        else if(input1.isPoI(side, position)) return new FluidTank[] {tanks[1]};
        else if(output0.isPoI(side, position)) return new FluidTank[] {tanks[2]};
        else if(output1.isPoI(side, position)) return new FluidTank[] {tanks[3]};
        return ITUtils.emptyIFluidTankList;
    }

    public boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource, int position) {
        if(input0 == null) InitializePoIs();
        if(input0.isPoI(side, position)) {
            if(tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
            if(tanks[0].getFluid() == null) return CoolingTowerRecipe.findRecipeByFluid0(resource.getFluid()) != null;
            else return resource.getFluid() == tanks[0].getFluid().getFluid();
        } else if(input1.isPoI(side, position)) {
            if(tanks[1].getFluidAmount() >= tanks[1].getCapacity()) return false;
            if(tanks[1].getFluid() == null) return CoolingTowerRecipe.findRecipeByFluid1(resource.getFluid()) != null;
            else return resource.getFluid() == tanks[1].getFluid().getFluid();
        }
        return false;
    }

    public boolean canDrainTankFrom(int iTank, EnumFacing side, int position) {
        if(input0 == null) InitializePoIs();
        if(output0.isPoI(side, position)) {
            return tanks[2].getFluidAmount() > 0;
        } else if(output1.isPoI(side, position)) {
            return tanks[3].getFluidAmount() > 0;
        }
        return false;
    }

    private void pumpOutputOut() {
        if(recipe == null) return;
        if(input0 == null) InitializePoIs();
        IFluidHandler output;
        if(tanks[2].getFluidAmount() >= recipe.fluidOutput0.amount && (output = FluidUtil.getFluidHandler(world, output0Front, output0.facing.getOpposite())) != null) {
            FluidStack out = tanks[2].getFluid();
            int accepted = output.fill(out, false);
            if(accepted == 0) return;
            int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
            this.tanks[2].drain(drained, true);
        }
        if(tanks[3].getFluidAmount() >= recipe.fluidOutput1.amount && (output = FluidUtil.getFluidHandler(world, output1Front, output1.facing.getOpposite())) != null) {
            FluidStack out = tanks[3].getFluid();
            int accepted = output.fill(out, false);
            if(accepted == 0) return;
            int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
            this.tanks[3].drain(drained, true);
        }
    }
}
