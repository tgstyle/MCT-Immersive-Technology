package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
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
import java.util.List;

public class TileEntityHeatExchangerMaster extends TileEntityHeatExchangerSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver {
    private static final int inputTankSize = Multiblocks.heatExchanger.heatExchanger_input_tankSize;
    private static final int outputTankSize = Multiblocks.heatExchanger.heatExchanger_output_tankSize;
    private static final int energyCapacity = Multiblocks.heatExchanger.heatExchanger_energy_size;
    private static final int energyMaxInput = Multiblocks.heatExchanger.heatExchanger_energy_maxInput;

    public FluxStorageAdvanced energyStorage = new FluxStorageAdvanced(energyCapacity, energyMaxInput, energyMaxInput);

    public FluidTank[] tanks = new FluidTank[] {new ITFluidTank(inputTankSize, this), new ITFluidTank(inputTankSize, this), new ITFluidTank(outputTankSize, this), new ITFluidTank(outputTankSize, this)};

    private PoICache fluidInput0, fluidInput1, fluidOutput0, fluidOutput1;
    private PoICache redstone0;
    PoICache energyInput0;
    private BlockPos sound0;
    private BlockPos outputFront0, outputFront1;

    private float soundVolume;
    private double distanceSqToTE;
    private int playerDimension;
    private boolean isRunning;

    public TileEntityHeatExchangerMaster() { super(); }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        energyStorage.readFromNBT(nbt.getCompoundTag("energy"));
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
        tanks[3].readFromNBT(nbt.getCompoundTag("tank3"));
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("energy", energyStorage.writeToNBT(new NBTTagCompound()));
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
        if (distanceSqToTE > 4096) { ITSoundHandler.StopSound(sound0); soundVolume = 0; return; }
        if (isRunning) { if (soundVolume < 1) soundVolume += 0.01f; }
        else { if (soundVolume > 0) soundVolume -= 0.01f; }
        if (soundVolume == 0) { ITSoundHandler.StopSound(sound0); }
        else {
            float attenuation = Math.max((float) distanceSqToTE / 64f, 1f);
            ITSounds.heatExchanger.PlayRepeating(sound0, soundVolume / (4 * attenuation), 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() { ITSoundHandler.StopSound(sound0); super.onChunkUnload(); }

    @Override public void disassemble() {
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(sound0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), sound0.getX(), sound0.getY(), sound0.getZ(), 0));
        super.disassemble();
    }

    private void clientUpdate() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distSq = player.getDistanceSq(sound0.getX() + 0.5, sound0.getY() + 0.5, sound0.getZ() + 0.5);
        if (getWorld().provider.getDimension() == player.dimension && distSq < 400 && (distanceSqToTE > 400 || playerDimension != player.dimension)) requestUpdate();
        distanceSqToTE = distSq;
        playerDimension = player.dimension;
        handleSounds();
    }

    public static class HeatExchangerProcess extends MultiblockProcessInMachine<HeatExchangerRecipe> {
        public HeatExchangerProcess(HeatExchangerRecipe recipe, int... inputSlots) { super(recipe, inputSlots); }

        private int getEnergyPerTick() { return this.recipe.getTotalProcessEnergy() / this.recipe.getTotalProcessTime(); }

        @Override @Nonnull public HeatExchangerProcess setInputTanks(@Nonnull int... tanks) { super.setInputTanks(tanks); return this; }

        @Override public boolean canProcess(TileEntityMultiblockMetal multiblock) {
            int energyPerTick = getEnergyPerTick();
            int simulated = multiblock.getFluxStorage().extractEnergy(energyPerTick, true);
            if (simulated < energyPerTick) return false;
            List<FluidStack> fluidOutputs = recipe.getFluidOutputs();
            if (fluidOutputs != null && !fluidOutputs.isEmpty()) {
                IFluidTank[] tanks = multiblock.getInternalTanks();
                int[] outputTanks = multiblock.getOutputTanks();
                for (FluidStack output : fluidOutputs) {
                    if (output != null && output.amount > 0) {
                        boolean canOutput = false;
                        for (int iOutputTank : outputTanks) {
                            if (iOutputTank >= 0 && iOutputTank < tanks.length && tanks[iOutputTank] != null && tanks[iOutputTank].fill(output, false) == output.amount) {
                                canOutput = true;
                                break;
                            }
                        }
                        if (!canOutput) return false;
                    }
                }
            }
            return true;
        }

        @Override public void doProcessTick(@Nonnull TileEntityMultiblockMetal multiblock) {
            int energyPerTick = getEnergyPerTick();
            multiblock.getFluxStorage().extractEnergy(energyPerTick, false);
            super.doProcessTick(multiblock);
        }
    }

    private void serverUpdate() {
        pumpOutputOut();
        boolean update = false;
        if (energyStorage.getEnergyStored() > 0 && processQueue.size() < this.getProcessQueueMaxLength()) {
            FluidStack fluidInput0 = tanks[0].getFluid();
            FluidStack fluidInput1 = tanks[1].getFluid();
            if ((fluidInput0 != null && fluidInput0.amount > 0) || (fluidInput1 != null && fluidInput1.amount > 0)) {
                HeatExchangerRecipe recipe = HeatExchangerRecipe.findRecipe(fluidInput0, fluidInput1);
                if (recipe != null) {
                    HeatExchangerProcess process = new HeatExchangerProcess(recipe, new int[0]).setInputTanks(0, 1);
                    if (process.canProcess(this) && this.addProcessToQueue(process, true)) {
                        tanks[0].drain(recipe.fluidInput0.amount, true);
                        if (recipe.fluidInput1 != null) tanks[1].drain(recipe.fluidInput1.amount, true);
                        this.addProcessToQueue(process, false);
                        update = true;
                    }
                }
            }
        }
        super.update();
        boolean wasRunning = isRunning;
        isRunning = shouldRenderAsActive() && !processQueue.isEmpty() && processQueue.get(0).canProcess(this);
        if (isRunning != wasRunning) notifyNearbyClients();
        if (update) {
            efficientMarkDirty();
            this.markContainingBlockForUpdate(null);
        }
    }

    @Override public void update() {
        if (formed && redstone0 == null) InitializePoIs();
        if (world.isRemote) { clientUpdate(); return; }
        serverUpdate();
    }

    public void efficientMarkDirty() { world.getChunk(this.getPos()).markDirty(); }

    @Override public void TankContentsChanged() { this.markContainingBlockForUpdate(null); }

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
                case "redstone0":
                    redstone0 = new PoICache(this.facing, poi, this.mirrored);
                    break;
                case "energy_input0":
                    energyInput0 = new PoICache(this.facing, poi, this.mirrored);
                    break;
                case "fluid_input0":
                    fluidInput0 = new PoICache(this.facing, poi, this.mirrored);
                    break;
                case "fluid_input1":
                    fluidInput1 = new PoICache(this.facing, poi, this.mirrored);
                    break;
                case "fluid_output0":
                    fluidOutput0 = new PoICache(this.facing, poi, this.mirrored);
                    outputFront0 = getBlockPosForPos(fluidOutput0.position).offset(fluidOutput0.facing);
                    break;
                case "fluid_output1":
                    fluidOutput1 = new PoICache(this.facing, poi, this.mirrored);
                    outputFront1 = getBlockPosForPos(fluidOutput1.position).offset(fluidOutput1.facing);
                    break;
                case "sound0":
                    sound0 = getBlockPosForPos(poi.position);
                    break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(fluidInput0.position));
        notifyNeighbor(getBlockPosForPos(fluidInput1.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput0.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput1.position));
        notifyNeighbor(getBlockPosForPos(redstone0.position));
        notifyNeighbor(getBlockPosForPos(energyInput0.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override
    @Nonnull
    public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (redstone0 == null) InitializePoIs();
        if (side == null) return tanks;
        if (fluidInput0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        if (fluidInput1.isPoI(side, position)) return new IFluidTank[] {tanks[1]};
        if (fluidOutput0.isPoI(side, position)) return new IFluidTank[] {tanks[2]};
        if (fluidOutput1.isPoI(side, position)) return new IFluidTank[] {tanks[3]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (fluidInput0.isPoI(side, position)) {
            FluidTank tank = tanks[0];
            if (tank.getFluidAmount() >= tank.getCapacity()) return false;
            if (tank.getFluid() == null) return HeatExchangerRecipe.findRecipeByFluid0(resource.getFluid()) != null;
            return resource.getFluid() == tank.getFluid().getFluid();
        }
        if (fluidInput1.isPoI(side, position)) {
            FluidTank tank = tanks[1];
            if (tank.getFluidAmount() >= tank.getCapacity()) return false;
            if (tank.getFluid() == null) return HeatExchangerRecipe.findRecipeByFluid1(resource.getFluid()) != null;
            return resource.getFluid() == tank.getFluid().getFluid();
        }
        return false;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (fluidOutput0.isPoI(side, position)) return tanks[2].getFluidAmount() > 0;
        if (fluidOutput1.isPoI(side, position)) return tanks[3].getFluidAmount() > 0;
        return false;
    }

    private void pumpOutputOut() {
        IFluidHandler output;
        if (tanks[2].getFluidAmount() > 0) {
            output = FluidUtil.getFluidHandler(world, outputFront0, fluidOutput0.facing.getOpposite());
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
            output = FluidUtil.getFluidHandler(world, outputFront1, fluidOutput1.facing.getOpposite());
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

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstone0 == null) InitializePoIs();
        return new int[] {redstone0.position};
    }

    @Override public boolean isRSDisabled() {
        if (computerOn.isPresent()) { boolean on = computerOn.get(); return !on; }
        int[] rsPositions = getRedstonePos();
        if (rsPositions.length < 1) return false;
        for (int rsPos : rsPositions) {
            TileEntityHeatExchangerSlave tile = getTileForPos(rsPos);
            if (tile != null) {
                BlockPos pos = tile.getPos();
                int power = world.getRedstonePowerFromNeighbors(pos);
                boolean b = power > 0;
                return redstoneControlInverted != b;
            }
        }
        return false;
    }

    @Override public @Nonnull int[] getEnergyPos() {
        if (!formed) return new int[0];
        if (energyInput0 == null) InitializePoIs();
        return new int[] {energyInput0.position};
    }

    @Override @Nonnull public FluxStorage getFluxStorage() { return energyStorage; }

    @Override @Nonnull protected MultiblockProcess<HeatExchangerRecipe> loadProcessFromNBT(@Nonnull NBTTagCompound tag) {
        HeatExchangerRecipe recipe = readRecipeFromNBT(tag);
        HeatExchangerProcess process = new HeatExchangerProcess(recipe, tag.getIntArray("process_inputSlots"));
        process.setInputTanks(tag.getIntArray("process_inputTanks"));
        return process;
    }
}
