package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
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
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class TileEntityElectrolyticCrucibleBatteryMaster extends TileEntityElectrolyticCrucibleBatterySlave implements ITFluidTank.TankListener, IBinaryMessageReceiver, IEBlockInterfaces.IMirrorAble, IEBlockInterfaces.IUsesBooleanProperty, IComparatorOverride {

    private static final int inputTankSize = Multiblocks.electrolyticCrucibleBattery.electrolyticCrucibleBattery_input_tankSize;
    private static final int outputTankSize = Multiblocks.electrolyticCrucibleBattery.electrolyticCrucibleBattery_output_tankSize;
    private static final int energyCapacity = Multiblocks.electrolyticCrucibleBattery.electrolyticCrucibleBattery_energy_size;
    private static final int energyMaxInput = Multiblocks.electrolyticCrucibleBattery.electrolyticCrucibleBattery_energy_maxInput;

    public FluxStorageAdvanced energyStorage = new FluxStorageAdvanced(energyCapacity, energyMaxInput, energyMaxInput);
    public FluidTank[] tanks = new FluidTank[] {
            new ITFluidTank(inputTankSize, this),
            new ITFluidTank(outputTankSize, this),
            new ITFluidTank(outputTankSize, this),
            new ITFluidTank(outputTankSize, this)
    };

    protected PoICache energyInput0, energyInput1, energyInput2, fluidInput0, fluidOutput0, fluidOutput1, fluidOutput2, itemOutput0, redstone0;
    private BlockPos fluidOutputPos0, fluidOutputPos1, fluidOutputPos2, itemOutputPos0, sound0;

    private float soundVolume;
    private int soundGracePeriod = 60;
    private double distanceToTE;
    private int playerDimension;
    private boolean isRunning;
    public boolean redstoneControlInverted = false;
    public Optional<Boolean> computerOn = Optional.empty();
    private int oldComparatorOutput;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        energyStorage.readFromNBT(nbt.getCompoundTag("energy"));
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
        tanks[3].readFromNBT(nbt.getCompoundTag("tank3"));
        redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
        isRunning = nbt.getBoolean("running");
        oldComparatorOutput = nbt.getInteger("oldComparatorOutput");
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
        nbt.setBoolean("running", isRunning);
        nbt.setInteger("oldComparatorOutput", oldComparatorOutput);
    }

    @Override @Nonnull protected MultiblockProcess<ElectrolyticCrucibleBatteryRecipe> loadProcessFromNBT(@Nonnull NBTTagCompound tag) {
        ElectrolyticCrucibleBatteryRecipe recipe = ElectrolyticCrucibleBatteryRecipe.loadFromNBT(tag);
        int[] inputSlots = tag.getIntArray("process_inputSlots");
        int[] inputTanks = tag.getIntArray("process_inputTanks");
        ElectrolyticCrucibleBatteryProcess process = new ElectrolyticCrucibleBatteryProcess(recipe, inputSlots);
        if (inputTanks.length > 0) process.setInputTanks(inputTanks);
        return process;
    }

    public void requestUpdate() { BinaryMessageTileSync.sendToServer(getPos(), Unpooled.copyBoolean(true)); }

    public void notifyNearbyClients() { BinaryMessageTileSync.sendToAllTracking(world, getPos(), Unpooled.copyBoolean(isRunning)); }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) { BinaryMessageTileSync.sendToPlayer(player, getPos(), Unpooled.copyBoolean(isRunning)); }

    @Override public void receiveMessageFromServer(ByteBuf message) { isRunning = message.readBoolean(); }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (sound0 == null) InitializePoIs();
        if (soundVolume == 0) ITSoundHandler.StopSound(sound0);
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(sound0.getX() + .5, sound0.getY() + .5, sound0.getZ() + .5) / 8, 1);
            ITSounds.gasTurbineArc.PlayRepeating(sound0, (2 * soundVolume) / attenuation, soundVolume);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        if (sound0 != null) ITSoundHandler.StopSound(sound0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        if (sound0 == null) InitializePoIs();
        if (sound0 != null) {
            ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(sound0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), sound0.getX(), sound0.getY(), sound0.getZ(), 0));
        }
        super.disassemble();
    }

    @SideOnly(Side.CLIENT)
    private void clientUpdate() {
        if (sound0 == null) InitializePoIs();
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distSq = player.getDistanceSq(sound0.getX() + .5, sound0.getY() + .5, sound0.getZ() + .5);
        if (world.provider.getDimension() == player.dimension && distSq < 400 && (distanceToTE > 400 || playerDimension != player.dimension)) requestUpdate();
        distanceToTE = distSq;
        playerDimension = player.dimension;
        float targetSoundLevel = isRunning ? 1f : 0f;
        if (soundVolume < targetSoundLevel) {
            soundVolume = Math.min(soundVolume + 0.01f, targetSoundLevel);
            soundGracePeriod = 60;
        } else if (soundVolume > targetSoundLevel) {
            if (soundGracePeriod > 0) soundGracePeriod--;
            else soundVolume = Math.max(soundVolume - 0.01f, targetSoundLevel);
        }
        handleSounds();
    }

    @Override public void update() {
        if (world.isRemote) {
            clientUpdate();
            return;
        }
        if (formed && (redstone0 == null)) {
            InitializePoIs();
        }
        super.update();
        if (!formed) return;

        boolean update = pumpOutputOut();

        if (processQueue.size() < getProcessQueueMaxLength()) {
            FluidStack input = tanks[0].getFluid();
            if (input != null && input.amount > 0) {
                ElectrolyticCrucibleBatteryRecipe recipe = ElectrolyticCrucibleBatteryRecipe.findRecipe(input);
                if (recipe != null) {
                    MultiblockProcess<ElectrolyticCrucibleBatteryRecipe> process = new ElectrolyticCrucibleBatteryProcess(recipe).setInputTanks(0);
                    if (process.canProcess(this) && addProcessToQueue(process, true)) {
                        addProcessToQueue(process, false);
                        update = true;
                    }
                }
            }
        }

        boolean active = tickedProcesses > 0 && !isRSDisabled();
        if (active) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;
        boolean wasRunning = isRunning;
        isRunning = soundGracePeriod > 0;

        if (isRunning != wasRunning) notifyNearbyClients();

        if (update || isRunning != wasRunning) {
            efficientMarkDirty();
            markContainingBlockForUpdate(null);
        }

        int comp = getComparatorInputOverride();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            world.notifyNeighborsOfStateChange(getBlockPosForPos(redstone0.position), world.getBlockState(getBlockPosForPos(redstone0.position)).getBlock(), false);
        }
    }

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    @Override public void TankContentsChanged() { markContainingBlockForUpdate(null); }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityElectrolyticCrucibleBatteryMaster master() { return this; }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartElectrolyticCrucibleBattery.instance.pointsOfInterest) {
            switch (poi.name) {
                case "redstone0":
                    redstone0 = new PoICache(facing, poi, mirrored);
                    break;
                case "sound0":
                    sound0 = getBlockPosForPos(poi.position);
                    break;
                case "fluid_input0":
                    fluidInput0 = new PoICache(facing, poi, mirrored);
                    break;
                case "energy_input0":
                    energyInput0 = new PoICache(facing, poi, mirrored);
                    break;
                case "energy_input1":
                    energyInput1 = new PoICache(facing, poi, mirrored);
                    break;
                case "energy_input2":
                    energyInput2 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_output0":
                    fluidOutput0 = new PoICache(facing, poi, mirrored);
                    fluidOutputPos0 = getBlockPosForPos(fluidOutput0.position).offset(fluidOutput0.facing);
                    break;
                case "fluid_output1":
                    fluidOutput1 = new PoICache(facing, poi, mirrored);
                    fluidOutputPos1 = getBlockPosForPos(fluidOutput1.position).offset(fluidOutput1.facing);
                    break;
                case "fluid_output2":
                    fluidOutput2 = new PoICache(facing, poi, mirrored);
                    fluidOutputPos2 = getBlockPosForPos(fluidOutput2.position).offset(fluidOutput2.facing);
                    break;
                case "item_output0":
                    itemOutput0 = new PoICache(facing, poi, mirrored);
                    itemOutputPos0 = getBlockPosForPos(itemOutput0.position).offset(itemOutput0.facing);
                    break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(redstone0.position));
        notifyNeighbor(getBlockPosForPos(fluidInput0.position));
        notifyNeighbor(getBlockPosForPos(energyInput0.position));
        notifyNeighbor(getBlockPosForPos(energyInput1.position));
        notifyNeighbor(getBlockPosForPos(energyInput2.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput0.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput1.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput2.position));
        notifyNeighbor(getBlockPosForPos(itemOutput0.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

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

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstone0 == null) InitializePoIs();
        return new int[] {redstone0.position};
    }

    @Override @Nonnull public int[] getEnergyPos() {
        if (!formed) return new int[0];
        if (redstone0 == null) InitializePoIs();
        return new int[] {energyInput0.position, energyInput1.position, energyInput2.position};
    }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, int position) {
        if (!formed) return false;
        if (redstone0 == null) InitializePoIs();
        if (facing == null) return false;
        if (energyInput0.isPoI(facing, position)) return true;
        if (energyInput1.isPoI(facing, position)) return true;
        return energyInput2.isPoI(facing, position);
    }

    @Override public int getProcessQueueMaxLength() { return 3; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<ElectrolyticCrucibleBatteryRecipe> process) { return true; }

    @Override public void onProcessFinish(@Nonnull MultiblockProcess<ElectrolyticCrucibleBatteryRecipe> process) {
        if (process.recipe == null) return;
        tanks[1].fillInternal(process.recipe.fluidOutput0, true);
        if (process.recipe.fluidOutput1 != null) tanks[2].fillInternal(process.recipe.fluidOutput1, true);
        if (process.recipe.fluidOutput2 != null) tanks[3].fillInternal(process.recipe.fluidOutput2, true);
        if (process.recipe.itemOutput != null && !process.recipe.itemOutput.isEmpty()) {
            TileEntity inventoryTile = world.getTileEntity(itemOutputPos0);
            ItemStack remaining = Utils.insertStackIntoInventory(inventoryTile, process.recipe.itemOutput.copy(), itemOutput0.facing.getOpposite());
            if (!remaining.isEmpty()) {
                Utils.dropStackAtPos(world, itemOutputPos0, remaining, itemOutput0.facing);
            }
        }
    }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (redstone0 == null) InitializePoIs();
        if (side == null) return tanks;
        if (fluidInput0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        if (fluidOutput0.isPoI(side, position)) return new IFluidTank[] {tanks[1]};
        if (fluidOutput1.isPoI(side, position)) return new IFluidTank[] {tanks[2]};
        if (fluidOutput2.isPoI(side, position)) return new IFluidTank[] {tanks[3]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (!formed || redstone0 == null) InitializePoIs();
        if (!fluidInput0.isPoI(side, position)) return false;
        if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
        FluidStack current = tanks[0].getFluid();
        if (current == null) return ElectrolyticCrucibleBatteryRecipe.findRecipe(resource) != null;
        return resource.isFluidEqual(current);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (!formed || redstone0 == null) InitializePoIs();
        if (fluidOutput0.isPoI(side, position)) return tanks[1].getFluidAmount() > 0;
        if (fluidOutput1.isPoI(side, position)) return tanks[2].getFluidAmount() > 0;
        if (fluidOutput2.isPoI(side, position)) return tanks[3].getFluidAmount() > 0;
        return false;
    }

    private boolean pumpOutputOut() {
        boolean update = false;
        IFluidHandler handler;
        if (tanks[1].getFluidAmount() > 0) {
            handler = FluidUtil.getFluidHandler(world, fluidOutputPos0, fluidOutput0.facing.getOpposite());
            if (handler != null) {
                FluidStack out = tanks[1].getFluid();
                if (out == null) return false;
                int accepted = handler.fill(out, false);
                if (accepted > 0) {
                    int drained = handler.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                    tanks[1].drain(drained, true);
                    update = drained > 0;
                }
            }
        }
        if (tanks[2].getFluidAmount() > 0) {
            handler = FluidUtil.getFluidHandler(world, fluidOutputPos1, fluidOutput1.facing.getOpposite());
            if (handler != null) {
                FluidStack out = tanks[2].getFluid();
                if (out == null) return false;
                int accepted = handler.fill(out, false);
                if (accepted > 0) {
                    int drained = handler.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                    tanks[2].drain(drained, true);
                    update |= drained > 0;
                }
            }
        }
        if (tanks[3].getFluidAmount() > 0) {
            handler = FluidUtil.getFluidHandler(world, fluidOutputPos2, fluidOutput2.facing.getOpposite());
            if (handler != null) {
                FluidStack out = tanks[3].getFluid();
                if (out == null) return false;
                int accepted = handler.fill(out, false);
                if (accepted > 0) {
                    int drained = handler.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                    tanks[3].drain(drained, true);
                    update |= drained > 0;
                }
            }
        }
        return update;
    }

    @Override @Nonnull public FluxStorageAdvanced getFluxStorage() { return energyStorage; }

    @Override public boolean getIsMirrored() { return mirrored; }

    @Override public @Nonnull IEProperties.PropertyBoolInverted getBoolProperty(@Nonnull Class<? extends IEBlockInterfaces.IUsesBooleanProperty> inf) { return IEProperties.BOOLEANS[0]; }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }

    @Override public int getComparatorInputOverride() {
        if (!formed) return 0;
        return 15 * tanks[1].getFluidAmount() / tanks[1].getCapacity();
    }

    static class ElectrolyticCrucibleBatteryProcess extends MultiblockProcessInMachine<ElectrolyticCrucibleBatteryRecipe> {
        public ElectrolyticCrucibleBatteryProcess(ElectrolyticCrucibleBatteryRecipe recipe, int... inputSlots) { super(recipe, inputSlots); }

        private int getEnergyPerTick() { return (int)Math.floor((float)recipe.getTotalProcessEnergy() / recipe.getTotalProcessTime()); }

        @Override @Nonnull public ElectrolyticCrucibleBatteryProcess setInputTanks(@Nonnull int... tanks) { super.setInputTanks(tanks); return this; }

        @Override public boolean canProcess(@Nonnull TileEntityMultiblockMetal multiblock) {
            TileEntityElectrolyticCrucibleBatteryMaster master = (TileEntityElectrolyticCrucibleBatteryMaster)multiblock;
            if (recipe == null) return false;
            int energyPerTick = getEnergyPerTick();
            if (energyPerTick > 0 && master.energyStorage.extractEnergy(energyPerTick, true) < energyPerTick) return false;
            if (recipe.fluidOutput0 == null || recipe.fluidOutput0.getFluid() == null || master.tanks[1].fillInternal(recipe.fluidOutput0, false) != recipe.fluidOutput0.amount) return false;
            if (recipe.fluidOutput1 != null && (recipe.fluidOutput1.getFluid() == null || master.tanks[2].fillInternal(recipe.fluidOutput1, false) != recipe.fluidOutput1.amount)) return false;
            return recipe.fluidOutput2 == null || (recipe.fluidOutput2.getFluid() != null && master.tanks[3].fillInternal(recipe.fluidOutput2, false) == recipe.fluidOutput2.amount);
        }

        @Override public void doProcessTick(@Nonnull TileEntityMultiblockMetal multiblock) {
            if (recipe == null) return;
            int energyPerTick = getEnergyPerTick();
            if (energyPerTick > 0) ((TileEntityElectrolyticCrucibleBatteryMaster)multiblock).energyStorage.extractEnergy(energyPerTick, false);
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
                            if (resource.amount <= d.amount) return drained;
                            resource.amount -= d.amount;
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
