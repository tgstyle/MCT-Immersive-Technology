package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.crafting.ElectrolyticCrucibleBatteryRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartElectrolyticCrucibleBattery;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.multiblock.PoICache;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.IBinaryMessageReceiver;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TileEntityElectrolyticCrucibleBatteryMaster extends TileEntityElectrolyticCrucibleBatterySlave implements ITFluidTank.TankListener, IBinaryMessageReceiver, IEBlockInterfaces.IMirrorAble, IEBlockInterfaces.IUsesBooleanProperty, IEBlockInterfaces.IComparatorOverride {

    private static int inputTankSize() { return Multiblocks.electrolyticCrucibleBattery.electrolyticCrucibleBattery_input_tankSize; }
    private static int outputTankSize() { return Multiblocks.electrolyticCrucibleBattery.electrolyticCrucibleBattery_output_tankSize; }
    private static int energyCapacity() { return Multiblocks.electrolyticCrucibleBattery.electrolyticCrucibleBattery_energy_size; }
    private static int energyMaxInput() { return Multiblocks.electrolyticCrucibleBattery.electrolyticCrucibleBattery_energy_maxInput; }

    public FluxStorageAdvanced energyStorage = new FluxStorageAdvanced(energyCapacity(), energyMaxInput(), energyMaxInput());
    public FluidTank[] tanks = new FluidTank[]{
            new ITFluidTank(inputTankSize(), this),
            new ITFluidTank(outputTankSize(), this),
            new ITFluidTank(outputTankSize(), this),
            new ITFluidTank(outputTankSize(), this)
    };

    private float soundVolume = 0f;
    private int soundGracePeriod = 0;
    private boolean isRunning = false;
    private double distanceToTE;
    private int playerDimension;
    public boolean redstoneControlInverted = false;
    private int oldComparatorOutput;

    protected PoICache energyInputPos0, energyInputPos1, energyInputPos2, fluidInputPos0, fluidOutputPos0, fluidOutputPos1, fluidOutputPos2, itemOutputPos0, redstonePos0;
    private BlockPos fluidOutputTEPos0, fluidOutputTEPos1, fluidOutputTEPos2, itemOutputTEPos0, soundPos0;

    private boolean needsPoIInit = true;
    private boolean needsNotify = true;
    private int tickCountdown = 5;
    private int oldEnergy;
    private boolean oldIsRunning;

    public void efficientMarkDirty() {
        world.getChunk(getPos()).markDirty();
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        energyStorage.readFromNBT(nbt.getCompoundTag("energy"));
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
        tanks[3].readFromNBT(nbt.getCompoundTag("tank3"));
        redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
        isRunning = nbt.getBoolean("isRunning");
        soundGracePeriod = nbt.getInteger("soundGracePeriod");
        oldComparatorOutput = nbt.getInteger("oldComparatorOutput");
        if (formed && !descPacket) {
            needsPoIInit = true;
            needsNotify = true;
        }
        if (!descPacket && !formed) processQueue.clear();
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("energy", energyStorage.writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank2", tanks[2].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank3", tanks[3].writeToNBT(new NBTTagCompound()));
        nbt.setBoolean("redstoneControlInverted", redstoneControlInverted);
        nbt.setBoolean("isRunning", isRunning);
        nbt.setInteger("soundGracePeriod", soundGracePeriod);
        nbt.setInteger("oldComparatorOutput", oldComparatorOutput);
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (soundPos0 == null) InitializePoIs();
        float targetSoundLevel = isRunning ? 1f : 0f;
        if (soundVolume < targetSoundLevel) { soundVolume = Math.min(soundVolume + 0.01f, targetSoundLevel); }
        else if (soundVolume > targetSoundLevel) { soundVolume = Math.max(soundVolume - 0.01f, targetSoundLevel); }
        if (soundVolume <= 0f) { ITSoundHandler.StopSound(soundPos0); }
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float) player.getDistanceSq(soundPos0.getX() + .5, soundPos0.getY() + .5, soundPos0.getZ() + .5) / 8, 1);
            ITSounds.electrolyticCrucibleBattery.PlayRepeating(soundPos0, (2 * soundVolume) / attenuation, soundVolume);
        }
    }

    @SideOnly(Side.CLIENT)
    private void clientUpdate() {
        if (soundPos0 == null) InitializePoIs();
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distSq = player.getDistanceSq(soundPos0.getX() + .5, soundPos0.getY() + .5, soundPos0.getZ() + .5);
        if (world.provider.getDimension() == player.dimension && distSq < 400 && (distanceToTE > 400 || playerDimension != player.dimension)) requestUpdate();
        distanceToTE = distSq;
        playerDimension = player.dimension;
        handleSounds();
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        if (soundPos0 != null) ITSoundHandler.StopSound(soundPos0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        if (soundPos0 == null) InitializePoIs();
        if (soundPos0 != null) {
            ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
        }
        super.disassemble();
    }

    public void requestUpdate() {
        BinaryMessageTileSync.sendToServer(getPos(), Unpooled.copyBoolean(true));
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {
        boolean request = message.readBoolean();
        if (request) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeInt(energyStorage.getEnergyStored());
            buf.writeBoolean(isRunning);
            BinaryMessageTileSync.sendToPlayer(player, getPos(), buf);
        }
    }

    @Override public void receiveMessageFromServer(ByteBuf message) {
        int readEnergy = message.readInt();
        energyStorage.modifyEnergyStored(readEnergy - energyStorage.getEnergyStored());
        isRunning = message.readBoolean();
    }

    @Override public void update() {
        if (!formed) return;
        if (needsPoIInit || energyInputPos0 == null) {
            InitializePoIs();
            needsPoIInit = false;
        }
        if (needsNotify) {
            notifyIONeighbors();
            needsNotify = false;
        }
        if (world.isRemote) {
            clientUpdate();
            return;
        }
        super.update();
        boolean update = pumpOutputOut();
        boolean wasRunning = isRunning;
        if (processQueue.size() < getProcessQueueMaxLength()) {
            FluidStack input = tanks[0].getFluid();
            if (input != null && input.amount > 0) {
                ElectrolyticCrucibleBatteryRecipe recipe = ElectrolyticCrucibleBatteryRecipe.findRecipe(input);
                if (recipe != null) {
                    MultiblockProcess<ElectrolyticCrucibleBatteryRecipe> process = new ElectrolyticCrucibleBatteryProcess(recipe).setInputTanks(0);
                    if (addProcessToQueue(process, true)) {
                        addProcessToQueue(process, false);
                        update = true;
                    }
                }
            }
        }
        boolean didWork = tickedProcesses > 0 && !isRSDisabled();
        if (didWork) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;
        isRunning = soundGracePeriod > 0;
        int currentEnergy = energyStorage.getEnergyStored();
        boolean changed = oldEnergy != currentEnergy || oldIsRunning != isRunning;
        if (changed && tickCountdown-- <= 0) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeInt(currentEnergy);
            buf.writeBoolean(isRunning);
            BinaryMessageTileSync.sendToAllTracking(world, getPos(), buf);
            tickCountdown = 5;
            world.markChunkDirty(getPos(), this);
            if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
            else { throttledBlockUpdate(); }
        }
        int comparator = getComparatorInputOverride();
        if (comparator != oldComparatorOutput) {
            oldComparatorOutput = comparator;
            if (redstonePos0 != null) {
                BlockPos rsPos = getBlockPosForPos(redstonePos0.position);
                world.updateComparatorOutputLevel(rsPos, world.getBlockState(rsPos).getBlock());
            }
        }
        oldEnergy = currentEnergy;
        oldIsRunning = isRunning;
        if (update || changed) {
            if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
            else { throttledBlockUpdate(); }
        }
    }

    @Override public TileEntityElectrolyticCrucibleBatteryMaster master() { return this; }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartElectrolyticCrucibleBattery.instance.pointsOfInterest) {
            switch (poi.name) {
                case "redstone0":
                    redstonePos0 = new PoICache(facing, poi, mirrored);
                    break;
                case "sound0":
                    soundPos0 = getBlockPosForPos(poi.position);
                    break;
                case "fluid_input0":
                    fluidInputPos0 = new PoICache(facing, poi, mirrored);
                    break;
                case "energy_input0":
                    energyInputPos0 = new PoICache(facing, poi, mirrored);
                    break;
                case "energy_input1":
                    energyInputPos1 = new PoICache(facing, poi, mirrored);
                    break;
                case "energy_input2":
                    energyInputPos2 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_output0":
                    fluidOutputPos0 = new PoICache(facing, poi, mirrored);
                    fluidOutputTEPos0 = getBlockPosForPos(fluidOutputPos0.position).offset(fluidOutputPos0.facing);
                    break;
                case "fluid_output1":
                    fluidOutputPos1 = new PoICache(facing, poi, mirrored);
                    fluidOutputTEPos1 = getBlockPosForPos(fluidOutputPos1.position).offset(fluidOutputPos1.facing);
                    break;
                case "fluid_output2":
                    fluidOutputPos2 = new PoICache(facing, poi, mirrored);
                    fluidOutputTEPos2 = getBlockPosForPos(fluidOutputPos2.position).offset(fluidOutputPos2.facing);
                    break;
                case "item_output0":
                    itemOutputPos0 = new PoICache(facing, poi, mirrored);
                    itemOutputTEPos0 = getBlockPosForPos(itemOutputPos0.position).offset(itemOutputPos0.facing);
                    break;
            }
        }
    }

    private void notifyIONeighbors() {
        BlockPos p;
        p = getBlockPosForPos(energyInputPos0.position);
        world.notifyNeighborsOfStateChange(p, world.getBlockState(p).getBlock(), true);
        p = getBlockPosForPos(energyInputPos1.position);
        world.notifyNeighborsOfStateChange(p, world.getBlockState(p).getBlock(), true);
        p = getBlockPosForPos(energyInputPos2.position);
        world.notifyNeighborsOfStateChange(p, world.getBlockState(p).getBlock(), true);
        p = getBlockPosForPos(fluidInputPos0.position);
        world.notifyNeighborsOfStateChange(p, world.getBlockState(p).getBlock(), true);
        p = getBlockPosForPos(fluidOutputPos0.position);
        world.notifyNeighborsOfStateChange(p, world.getBlockState(p).getBlock(), true);
        p = getBlockPosForPos(fluidOutputPos1.position);
        world.notifyNeighborsOfStateChange(p, world.getBlockState(p).getBlock(), true);
        p = getBlockPosForPos(fluidOutputPos2.position);
        world.notifyNeighborsOfStateChange(p, world.getBlockState(p).getBlock(), true);
        p = getBlockPosForPos(itemOutputPos0.position);
        world.notifyNeighborsOfStateChange(p, world.getBlockState(p).getBlock(), true);
        p = getBlockPosForPos(redstonePos0.position);
        world.updateComparatorOutputLevel(p, world.getBlockState(p).getBlock());
    }

    private boolean pumpOutputOut() {
        boolean update = false;
        IFluidHandler handler;
        if (tanks[1].getFluidAmount() > 0) {
            handler = FluidUtil.getFluidHandler(world, fluidOutputTEPos0, fluidOutputPos0.facing.getOpposite());
            if (handler != null) {
                FluidStack out = tanks[1].getFluid();
                if (out != null) {
                    int accepted = handler.fill(out, false);
                    if (accepted > 0) {
                        int drained = handler.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                        tanks[1].drain(drained, true);
                        update |= drained > 0;
                    }
                }
            }
        }
        if (tanks[2].getFluidAmount() > 0) {
            handler = FluidUtil.getFluidHandler(world, fluidOutputTEPos1, fluidOutputPos1.facing.getOpposite());
            if (handler != null) {
                FluidStack out = tanks[2].getFluid();
                if (out != null) {
                    int accepted = handler.fill(out, false);
                    if (accepted > 0) {
                        int drained = handler.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                        tanks[2].drain(drained, true);
                        update |= drained > 0;
                    }
                }
            }
        }
        if (tanks[3].getFluidAmount() > 0) {
            handler = FluidUtil.getFluidHandler(world, fluidOutputTEPos2, fluidOutputPos2.facing.getOpposite());
            if (handler != null) {
                FluidStack out = tanks[3].getFluid();
                if (out != null) {
                    int accepted = handler.fill(out, false);
                    if (accepted > 0) {
                        int drained = handler.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                        tanks[3].drain(drained, true);
                        update |= drained > 0;
                    }
                }
            }
        }
        return update;
    }

    @Override public void TankContentsChanged() {
        markContainingBlockForUpdate(null);
    }

    @Override public boolean isRSDisabled() {
        if (computerOn.isPresent()) return !computerOn.get();
        int[] rs = getRedstonePos();
        if (rs.length < 1) return false;
        for (int p : rs) {
            TileEntity te = getTileForPos(p);
            if (te != null) {
                int power = world.getRedstonePowerFromNeighbors(te.getPos());
                return redstoneControlInverted != (power > 0);
            }
        }
        return false;
    }

    @Override public int getComparatorInputOverride() {
        if (!formed) return 0;
        return 15 * tanks[1].getFluidAmount() / tanks[1].getCapacity();
    }

    @Override public boolean isDummy() { return false; }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstonePos0 == null) InitializePoIs();
        return new int[]{redstonePos0.position};
    }

    @Override @Nonnull public int[] getEnergyPos() {
        if (!formed) return new int[0];
        if (energyInputPos0 == null) InitializePoIs();
        return new int[]{energyInputPos0.position, energyInputPos1.position, energyInputPos2.position};
    }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, int position) {
        if (!formed) return false;
        if (energyInputPos0 == null) InitializePoIs();
        if (facing == null) return false;
        return energyInputPos0.isPoI(facing, position) || energyInputPos1.isPoI(facing, position) || energyInputPos2.isPoI(facing, position);
    }

    @Override @Nonnull public FluxStorageAdvanced getFluxStorage() { return energyStorage; }

    @Override public boolean getIsMirrored() { return mirrored; }

    @Override @Nonnull public IEProperties.PropertyBoolInverted getBoolProperty(@Nonnull Class<? extends IEBlockInterfaces.IUsesBooleanProperty> inf) { return IEProperties.BOOLEANS[0]; }

    @Override public int getProcessQueueMaxLength() { return 3; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<ElectrolyticCrucibleBatteryRecipe> process) { return true; }

    @Override public void onProcessFinish(@Nonnull MultiblockProcess<ElectrolyticCrucibleBatteryRecipe> process) {
        if (process.recipe == null) return;
        tanks[1].fillInternal(process.recipe.fluidOutput0, true);
        if (process.recipe.fluidOutput1 != null) tanks[2].fillInternal(process.recipe.fluidOutput1, true);
        if (process.recipe.fluidOutput2 != null) tanks[3].fillInternal(process.recipe.fluidOutput2, true);
        if (process.recipe.itemOutput != null && !process.recipe.itemOutput.isEmpty()) {
            TileEntity inventoryTile = world.getTileEntity(itemOutputTEPos0);
            ItemStack remaining = Utils.insertStackIntoInventory(inventoryTile, process.recipe.itemOutput.copy(), itemOutputPos0.facing.getOpposite());
            if (!remaining.isEmpty()) {
                Utils.dropStackAtPos(world, itemOutputTEPos0, remaining, itemOutputPos0.facing);
            }
        }
    }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (redstonePos0 == null) InitializePoIs();
        if (side == null) return tanks;
        if (fluidInputPos0.isPoI(side, position)) return new IFluidTank[]{tanks[0]};
        if (fluidOutputPos0.isPoI(side, position)) return new IFluidTank[]{tanks[1]};
        if (fluidOutputPos1.isPoI(side, position)) return new IFluidTank[]{tanks[2]};
        if (fluidOutputPos2.isPoI(side, position)) return new IFluidTank[]{tanks[3]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (!formed) return false;
        if (redstonePos0 == null) InitializePoIs();
        if (!fluidInputPos0.isPoI(side, position)) return false;
        if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
        FluidStack current = tanks[0].getFluid();
        if (current == null) return ElectrolyticCrucibleBatteryRecipe.findRecipe(resource) != null;
        return resource.isFluidEqual(current);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (!formed) return false;
        if (redstonePos0 == null) InitializePoIs();
        if (fluidOutputPos0.isPoI(side, position)) return tanks[1].getFluidAmount() > 0;
        if (fluidOutputPos1.isPoI(side, position)) return tanks[2].getFluidAmount() > 0;
        if (fluidOutputPos2.isPoI(side, position)) return tanks[3].getFluidAmount() > 0;
        return false;
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }

    @Override @Nonnull protected MultiblockProcess<ElectrolyticCrucibleBatteryRecipe> loadProcessFromNBT(@Nonnull NBTTagCompound tag) {
        ElectrolyticCrucibleBatteryRecipe recipe = ElectrolyticCrucibleBatteryRecipe.loadFromNBT(tag);
        int[] inputSlots = tag.getIntArray("process_inputSlots");
        int[] inputTanks = tag.getIntArray("process_inputTanks");
        ElectrolyticCrucibleBatteryProcess process = new ElectrolyticCrucibleBatteryProcess(recipe, inputSlots);
        if (inputTanks.length > 0) process.setInputTanks(inputTanks);
        if (tag.hasKey("process_maxTicks")) { process.maxTicks = tag.getInteger("process_maxTicks"); }
        return process;
    }

    @Override @Nonnull protected NBTTagCompound writeProcessToNBT(@Nonnull MultiblockProcess process) {
        NBTTagCompound tag = super.writeProcessToNBT(process);
        tag.setInteger("process_maxTicks", process.maxTicks);
        return tag;
    }

    static class ElectrolyticCrucibleBatteryProcess extends MultiblockProcessInMachine<ElectrolyticCrucibleBatteryRecipe> {
        public ElectrolyticCrucibleBatteryProcess(ElectrolyticCrucibleBatteryRecipe recipe, int... inputSlots) { super(recipe, inputSlots); }

        private int getEnergyPerTick() { return (int) Math.floor((float) recipe.getTotalProcessEnergy() / recipe.getTotalProcessTime()); }

        @Override @Nonnull public ElectrolyticCrucibleBatteryProcess setInputTanks(@Nonnull int... tanks) { super.setInputTanks(tanks); return this; }

        @Override public boolean canProcess(@Nonnull TileEntityMultiblockMetal multiblock) {
            TileEntityElectrolyticCrucibleBatteryMaster master = (TileEntityElectrolyticCrucibleBatteryMaster) multiblock;
            if (recipe == null) return false;
            int energyPerTick = getEnergyPerTick();
            if (energyPerTick > 0 && master.energyStorage.extractEnergy(energyPerTick, true) < energyPerTick) return false;
            if (recipe.fluidOutput0 == null || recipe.fluidOutput0.getFluid() == null || master.tanks[1].fillInternal(recipe.fluidOutput0, false) != recipe.fluidOutput0.amount) return false;
            if (recipe.fluidOutput1 != null && (recipe.fluidOutput1.getFluid() == null || master.tanks[2].fillInternal(recipe.fluidOutput1, false) != recipe.fluidOutput1.amount)) return false;
            return recipe.fluidOutput2 == null || recipe.fluidOutput2.getFluid() == null || master.tanks[3].fillInternal(recipe.fluidOutput2, false) == recipe.fluidOutput2.amount;
        }

        @Override public void doProcessTick(@Nonnull TileEntityMultiblockMetal multiblock) {
            if (recipe == null) return;
            int energyPerTick = getEnergyPerTick();
            if (energyPerTick > 0) ((TileEntityElectrolyticCrucibleBatteryMaster) multiblock).energyStorage.extractEnergy(energyPerTick, false);
            super.doProcessTick(multiblock);
        }
    }

    static class ElectrolyticCrucibleBatteryFluidHandler implements IFluidHandler {
        private final IFluidTank[] tanks;
        private final TileEntityElectrolyticCrucibleBatteryMaster master;
        private final EnumFacing side;
        private final int position;

        ElectrolyticCrucibleBatteryFluidHandler(IFluidTank[] accessibleTanks, TileEntityElectrolyticCrucibleBatteryMaster master, EnumFacing side, int position) {
            this.tanks = accessibleTanks;
            this.master = master;
            this.side = side;
            this.position = position;
        }

        private int getTankIndex(IFluidTank tank) {
            for (int i = 0; i < master.tanks.length; i++) if (master.tanks[i] == tank) return i;
            return -1;
        }

        @Override public IFluidTankProperties[] getTankProperties() {
            List<IFluidTankProperties> list = new ArrayList<>();
            for (IFluidTank tank : tanks) {
                int idx = getTankIndex(tank);
                boolean fill = idx == 0;
                boolean drain = idx > 0;
                list.add(new FluidTankProperties(tank.getFluid(), tank.getCapacity(), fill, drain));
            }
            return list.toArray(new IFluidTankProperties[0]);
        }

        @Override public int fill(FluidStack resource, boolean doFill) {
            if (resource == null) return 0;
            resource = resource.copy();
            int filled = 0;
            for (IFluidTank tank : tanks) {
                int idx = getTankIndex(tank);
                if (idx != -1 && master.canFillTankFrom(idx, side, resource, position)) {
                    int f = tank.fill(resource, doFill);
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
            for (IFluidTank tank : tanks) {
                int idx = getTankIndex(tank);
                if (idx != -1 && master.canDrainTankFrom(idx, side, position)) {
                    FluidStack tf = tank.getFluid();
                    if (tf != null && tf.isFluidEqual(resource)) {
                        int amt = Math.min(resource.amount, tf.amount);
                        FluidStack d = tank.drain(amt, doDrain);
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
            int remaining = maxDrain;
            FluidStack drained = null;
            for (IFluidTank tank : tanks) {
                int idx = getTankIndex(tank);
                if (idx != -1 && master.canDrainTankFrom(idx, side, position)) {
                    FluidStack d = tank.drain(remaining, doDrain);
                    if (d != null) {
                        if (drained == null) drained = d.copy();
                        else if (drained.isFluidEqual(d)) drained.amount += d.amount;
                        remaining -= d.amount;
                        if (doDrain && d.amount > 0) master.TankContentsChanged();
                        if (remaining <= 0) return drained;
                    }
                }
            }
            return drained;
        }
    }
}
