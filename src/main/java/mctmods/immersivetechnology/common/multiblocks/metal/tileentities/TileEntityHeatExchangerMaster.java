package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.HeatExchangerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartHeatExchanger;
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
import net.minecraft.entity.player.EntityPlayerMP;
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
import javax.annotation.Nullable;

public class TileEntityHeatExchangerMaster extends TileEntityHeatExchangerSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver {
    private static final int inputTankSize = Multiblocks.heatExchanger.heatExchanger_input_tankSize;
    private static final int outputTankSize = Multiblocks.heatExchanger.heatExchanger_output_tankSize;

    public FluidTank[] tanks = new FluidTank[] {new ITFluidTank(inputTankSize, this), new ITFluidTank(inputTankSize, this), new ITFluidTank(outputTankSize, this), new ITFluidTank(outputTankSize, this)};

    HeatExchangerRecipe recipe;
    private HeatExchangerRecipe cachedRecipe;

    private PoICache input0, input1, output0, output1;
    private PoICache redstone0, energyInput0;
    private BlockPos soundPos0;
    private BlockPos outputFront0, outputFront1;

    private float soundVolume;
    private int clientUpdateCooldown = 1;
    private double distanceSqToTE;
    private int playerDimension;
    private boolean isRunning;
    private int gracePeriod = 0;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
        tanks[3].readFromNBT(nbt.getCompoundTag("tank3"));
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank2", tanks[2].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank3", tanks[3].writeToNBT(new NBTTagCompound()));
    }

    public void requestUpdate() {
        ByteBuf buffer = Unpooled.copyBoolean(true);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendToServer(new BinaryMessageTileSync(center, buffer));
    }

    public void notifyNearbyClients() {
        ByteBuf buffer = Unpooled.copyBoolean(isRunning);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendToAllAround(new BinaryMessageTileSync(center, buffer), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 40));
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {
        ByteBuf buffer = Unpooled.copyBoolean(isRunning);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendTo(new BinaryMessageTileSync(center, buffer), player);
    }

    @Override public void receiveMessageFromServer(ByteBuf message) { isRunning = message.readBoolean(); }

    public void handleSounds() {
        if (isRunning) { if (soundVolume < 1) soundVolume += 0.01f; }
        else { if (soundVolume > 0) soundVolume -= 0.01f; }
        if (soundVolume == 0) { ITSoundHandler.StopSound(soundPos0); }
        else {
            float attenuation = Math.max((float) Math.sqrt(distanceSqToTE) / 8, 1);
            ITSounds.heatExchanger.PlayRepeating(soundPos0, (10 * soundVolume) / attenuation, 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() { ITSoundHandler.StopSound(soundPos0); super.onChunkUnload(); }

    @Override public void disassemble() {
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
        super.disassemble();
    }

    private void clientUpdate() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distSq = player.getDistanceSq(soundPos0.getX() + 0.5, soundPos0.getY() + 0.5, soundPos0.getZ() + 0.5);
        if (getWorld().provider.getDimension() == player.dimension && distSq < 400 && (distanceSqToTE > 400 || playerDimension != player.dimension)) requestUpdate();
        distanceSqToTE = distSq;
        playerDimension = player.dimension;
        handleSounds();
    }

    private void serverUpdate() {
        pumpOutputOut();
        boolean update = false;
        if (processQueue.size() < this.getProcessQueueMaxLength()) {
            if (tanks[0].getFluidAmount() > 0 || tanks[1].getFluidAmount() > 0) {
                if (cachedRecipe == null) cachedRecipe = HeatExchangerRecipe.findRecipe(tanks[0].getFluid(), tanks[1].getFluid());
                recipe = cachedRecipe;
                if (recipe != null && tanks[2].fill(recipe.fluidOutput0, false) == recipe.fluidOutput0.amount && (recipe.fluidOutput1 == null || tanks[3].fill(recipe.fluidOutput1, false) == recipe.fluidOutput1.amount)) {
                    @SuppressWarnings("unchecked")
                    MultiblockProcessInMachine<HeatExchangerRecipe> process = new MultiblockProcessInMachine<>(recipe).setInputTanks(0, 1);
                    if (this.addProcessToQueue(process, true)) {
                        this.addProcessToQueue(process, false);
                        update = true;
                    }
                }
            }
        }
        boolean wasRunning = isRunning;
        if (tickedProcesses > 0) gracePeriod = 60;
        else if (gracePeriod > 0) gracePeriod--;
        isRunning = gracePeriod > 0;
        if (isRunning != wasRunning) notifyNearbyClients();
        if (clientUpdateCooldown > 1) clientUpdateCooldown--;
        else {
            notifyNearbyClients();
            clientUpdateCooldown = 20;
        }
        if (update) {
            efficientMarkDirty();
            this.markContainingBlockForUpdate(null);
        }
    }

    @Override public void update() {
        if (formed && redstone0 == null) InitializePoIs();
        super.update();
        if (world.isRemote) { clientUpdate(); return; }
        serverUpdate();
    }

    public void efficientMarkDirty() { world.getChunk(this.getPos()).markDirty(); }

    @Override public void TankContentsChanged() {
        cachedRecipe = null;
        this.markContainingBlockForUpdate(null);
    }

    @Override public void onProcessFinish(@Nonnull MultiblockProcess<HeatExchangerRecipe> process) {
        tanks[2].fill(process.recipe.fluidOutput0, true);
        if (process.recipe.fluidOutput1 != null) tanks[3].fill(process.recipe.fluidOutput1, true);
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityHeatExchangerMaster master() {
        master = this;
        return this;
    }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartHeatExchanger.instance.pointsOfInterest) {
            switch (poi.name) {
                case "redstone0": redstone0 = new PoICache(this.facing, poi, this.mirrored); break;
                case "energy_input0": energyInput0 = new PoICache(this.facing, poi, this.mirrored); break;
                case "input0": input0 = new PoICache(this.facing, poi, this.mirrored); break;
                case "input1": input1 = new PoICache(this.facing, poi, this.mirrored); break;
                case "output0":
                    output0 = new PoICache(this.facing, poi, this.mirrored);
                    outputFront0 = getBlockPosForPos(output0.position).offset(output0.facing);
                    break;
                case "output1":
                    output1 = new PoICache(this.facing, poi, this.mirrored);
                    outputFront1 = getBlockPosForPos(output1.position).offset(output1.facing);
                    break;
                case "sound0": soundPos0 = getBlockPosForPos(poi.position); break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(input0.position));
        notifyNeighbor(getBlockPosForPos(input1.position));
        notifyNeighbor(getBlockPosForPos(output0.position));
        notifyNeighbor(getBlockPosForPos(output1.position));
        notifyNeighbor(getBlockPosForPos(redstone0.position));
        notifyNeighbor(getBlockPosForPos(energyInput0.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override
    @Nonnull
    public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (side == null) return tanks;
        if (input0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        if (input1.isPoI(side, position)) return new IFluidTank[] {tanks[1]};
        if (output0.isPoI(side, position)) return new IFluidTank[] {tanks[2]};
        if (output1.isPoI(side, position)) return new IFluidTank[] {tanks[3]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (input0.isPoI(side, position)) {
            FluidTank tank = tanks[0];
            if (tank.getFluidAmount() >= tank.getCapacity()) return false;
            if (tank.getFluid() == null) return HeatExchangerRecipe.findRecipeByFluid0(resource.getFluid()) != null;
            return resource.getFluid() == tank.getFluid().getFluid();
        }
        if (input1.isPoI(side, position)) {
            FluidTank tank = tanks[1];
            if (tank.getFluidAmount() >= tank.getCapacity()) return false;
            if (tank.getFluid() == null) return HeatExchangerRecipe.findRecipeByFluid1(resource.getFluid()) != null;
            return resource.getFluid() == tank.getFluid().getFluid();
        }
        return false;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (output0.isPoI(side, position)) return tanks[2].getFluidAmount() > 0;
        if (output1.isPoI(side, position)) return tanks[3].getFluidAmount() > 0;
        return false;
    }

    private void pumpOutputOut() {
        IFluidHandler output;
        if (tanks[2].getFluidAmount() > 0) {
            output = FluidUtil.getFluidHandler(world, outputFront0, output0.facing.getOpposite());
            if (output != null) {
                FluidStack out = tanks[2].getFluid();
                int accepted = output.fill(out, false);
                if (accepted > 0) {
                    assert out != null;
                    int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                    this.tanks[2].drain(drained, true);
                }
            }
        }
        if (tanks[3].getFluidAmount() > 0) {
            output = FluidUtil.getFluidHandler(world, outputFront1, output1.facing.getOpposite());
            if (output != null) {
                FluidStack out = tanks[3].getFluid();
                int accepted = output.fill(out, false);
                if (accepted > 0) {
                    assert out != null;
                    int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                    this.tanks[3].drain(drained, true);
                }
            }
        }
    }

    @Override public @Nonnull int[] getRedstonePos() {
        if (!formed) return new int[0];
        return new int[] {redstone0.position};
    }

    @Override public @Nonnull int[] getEnergyPos() {
        if (!formed) return new int[0];
        return new int[] {energyInput0.position};
    }
}
