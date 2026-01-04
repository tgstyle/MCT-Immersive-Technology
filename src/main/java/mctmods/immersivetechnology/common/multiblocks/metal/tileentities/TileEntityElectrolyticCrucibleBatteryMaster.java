package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.ElectrolyticCrucibleBatteryRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartElectrolyticCrucibleBattery;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityElectrolyticCrucibleBatteryMaster extends TileEntityElectrolyticCrucibleBatterySlave implements ITFluidTank.TankListener, IBinaryMessageReceiver, IEBlockInterfaces.IMirrorAble, IEBlockInterfaces.IUsesBooleanProperty {

    private static final int inputTankSize = Multiblocks.electrolyticCrucibleBattery.electrolyticCrucibleBattery_input_tankSize;
    private static final int outputTankSize = Multiblocks.electrolyticCrucibleBattery.electrolyticCrucibleBattery_output_tankSize;
    private static final int energyCapacity = Multiblocks.electrolyticCrucibleBattery.electrolyticCrucibleBattery_energy_size;
    private static final int energyMaxInput = Multiblocks.electrolyticCrucibleBattery.electrolyticCrucibleBattery_energy_maxInput;

    public TileEntityElectrolyticCrucibleBatteryMaster() { super(); }

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
    private double distanceToTE = 0;
    private int playerDimension;
    private boolean isRunning;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        energyStorage.readFromNBT(nbt.getCompoundTag("energy"));
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
        tanks[3].readFromNBT(nbt.getCompoundTag("tank3"));
        isRunning = nbt.getBoolean("running");
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("energy", energyStorage.writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank2", tanks[2].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank3", tanks[3].writeToNBT(new NBTTagCompound()));
        nbt.setBoolean("running", isRunning);
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

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (isRunning) { if (soundVolume < 1) { soundVolume += 0.02f; } }
        else { if (soundVolume > 0) { soundVolume -= 0.02f; } }
        if (soundVolume == 0) { ITSoundHandler.StopSound(sound0); }
        else {
            float attenuation = Math.max((float)distanceToTE / 16f, 1);
            ITSounds.gasTurbineArc.PlayRepeating(sound0, soundVolume / attenuation, 1);
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
        double currentDistance = player.getDistanceSq(sound0.getX(), sound0.getY(), sound0.getZ());
        if (world.provider.getDimension() == player.dimension && currentDistance < 400 &&
                (distanceToTE > 400 || playerDimension != player.dimension)) { requestUpdate(); }
        distanceToTE = currentDistance;
        playerDimension = player.dimension;
        handleSounds();
    }

    public static class ElectrolyticCrucibleBatteryProcess extends MultiblockProcessInMachine<ElectrolyticCrucibleBatteryRecipe> {
        public ElectrolyticCrucibleBatteryProcess(ElectrolyticCrucibleBatteryRecipe recipe, int... inputSlots) { super(recipe, inputSlots); }

        private int getEnergyPerTick() { return recipe.getTotalProcessEnergy() / recipe.getTotalProcessTime(); }

        @Override @Nonnull public ElectrolyticCrucibleBatteryProcess setInputTanks(@Nonnull int... tanks) { super.setInputTanks(tanks); return this; }

        @Override public boolean canProcess(@Nonnull TileEntityMultiblockMetal multiblock) {
            int energyPerTick = getEnergyPerTick();
            if (energyPerTick <= 0) return true;
            int simulated = multiblock.getFluxStorage().extractEnergy(energyPerTick, true);
            if (simulated < energyPerTick) return false;
            TileEntityElectrolyticCrucibleBatteryMaster master = (TileEntityElectrolyticCrucibleBatteryMaster)multiblock;
            int fill0 = master.tanks[1].fill(recipe.fluidOutput0, false);
            if (fill0 != recipe.fluidOutput0.amount) return false;
            if (recipe.fluidOutput1 != null) {
                int fill1 = master.tanks[2].fill(recipe.fluidOutput1, false);
                if (fill1 != recipe.fluidOutput1.amount) return false;
            }
            if (recipe.fluidOutput2 != null) {
                int fill2 = master.tanks[3].fill(recipe.fluidOutput2, false);
                return fill2 == recipe.fluidOutput2.amount;
            }
            return true;
        }

        @Override public void doProcessTick(@Nonnull TileEntityMultiblockMetal multiblock) {
            int energyPerTick = getEnergyPerTick();
            if (energyPerTick > 0) { multiblock.getFluxStorage().extractEnergy(energyPerTick, false); }
            super.doProcessTick(multiblock);
        }
    }

    @Override @Nonnull protected MultiblockProcess<ElectrolyticCrucibleBatteryRecipe> loadProcessFromNBT(@Nonnull NBTTagCompound tag) {
        ElectrolyticCrucibleBatteryRecipe recipe = ElectrolyticCrucibleBatteryRecipe.loadFromNBT(tag);
        int[] inputSlots = tag.getIntArray("process_inputSlots");
        int[] inputTanks = tag.getIntArray("process_inputTanks");
        ElectrolyticCrucibleBatteryProcess process = new ElectrolyticCrucibleBatteryProcess(recipe, inputSlots);
        if (inputTanks.length > 0) { process.setInputTanks(inputTanks); }
        return process;
    }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<ElectrolyticCrucibleBatteryRecipe> process) { return true; }

    @Override public void update() {
        if (formed && redstone0 == null) { InitializePoIs(); }
        if (world.isRemote) { clientUpdate(); return; }
        super.update();
        pumpOutputOut();
        boolean update = false;
        if (processQueue.size() < getProcessQueueMaxLength()) {
            FluidStack input = tanks[0].getFluid();
            if (input != null && input.amount > 0) {
                ElectrolyticCrucibleBatteryRecipe recipe = ElectrolyticCrucibleBatteryRecipe.findRecipe(input);
                if (recipe != null) {
                    ElectrolyticCrucibleBatteryProcess process = new ElectrolyticCrucibleBatteryProcess(recipe).setInputTanks(0);
                    if (process.canProcess(this) && addProcessToQueue(process, true)) {
                        tanks[0].drain(recipe.fluidInput0.amount, true);
                        addProcessToQueue(process, false);
                        update = true;
                    }
                }
            }
        }
        boolean shouldRun = tickedProcesses > 0 && !isRSDisabled();
        if (isRunning != shouldRun) {
            isRunning = shouldRun;
            notifyNearbyClients();
        }
        if (update) {
            efficientMarkDirty();
            markContainingBlockForUpdate(null);
        }
    }

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    @Override public void TankContentsChanged() { markContainingBlockForUpdate(null); }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityElectrolyticCrucibleBatteryMaster master() { master = this; return this; }

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
        if (!world.isRemote) { notifyIONeighbors(); }
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

    private void notifyNeighbor(BlockPos pos) {
        world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false);
    }

    @Override public boolean isRSDisabled() {
        if (computerOn.isPresent()) { boolean on = computerOn.get(); return !on; }
        int[] rsPositions = getRedstonePos();
        if (rsPositions.length < 1) return false;
        for (int rsPos : rsPositions) {
            TileEntity tile = world.getTileEntity(getBlockPosForPos(rsPos));
            if (tile != null) {
                int power = world.getRedstonePowerFromNeighbors(tile.getPos());
                boolean b = power > 0;
                return redstoneControlInverted != b;
            }
        }
        return false;
    }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstone0 == null) { InitializePoIs(); }
        return new int[] {redstone0.position};
    }

    @Override public @Nonnull int[] getEnergyPos() {
        if (!formed) return new int[0];
        return new int[] {energyInput0.position, energyInput1.position, energyInput2.position};
    }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, int position) {
        if (!formed) return false;
        if (redstone0 == null) { InitializePoIs(); }
        if (facing == null) return false;
        if (energyInput0.isPoI(facing, position)) return true;
        if (energyInput1.isPoI(facing, position)) return true;
        return energyInput2.isPoI(facing, position);
    }

    @Override public void onProcessFinish(@Nonnull MultiblockProcess<ElectrolyticCrucibleBatteryRecipe> process) {
        tanks[1].fill(process.recipe.fluidOutput0, true);
        if (process.recipe.fluidOutput1 != null) { tanks[2].fill(process.recipe.fluidOutput1, true); }
        if (process.recipe.fluidOutput2 != null) { tanks[3].fill(process.recipe.fluidOutput2, true); }
        if (process.recipe.itemOutput != null && !process.recipe.itemOutput.isEmpty()) {
            TileEntity inventoryTile = world.getTileEntity(itemOutputPos0);
            ItemStack output = Utils.insertStackIntoInventory(inventoryTile, process.recipe.itemOutput.copy(), itemOutput0.facing.getOpposite());
            if (output != null && !output.isEmpty()) { Utils.dropStackAtPos(world, itemOutputPos0, output, itemOutput0.facing); }
        }
    }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (redstone0 == null) { InitializePoIs(); }
        if (side == null) return tanks;
        if (fluidInput0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        if (fluidOutput0.isPoI(side, position)) return new IFluidTank[] {tanks[1]};
        if (fluidOutput1.isPoI(side, position)) return new IFluidTank[] {tanks[2]};
        if (fluidOutput2.isPoI(side, position)) return new IFluidTank[] {tanks[3]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (!fluidInput0.isPoI(side, position)) return false;
        if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
        if (tanks[0].getFluid() == null) return ElectrolyticCrucibleBatteryRecipe.findRecipeFluid(resource.getFluid()) != null;
        return resource.getFluid() == tanks[0].getFluid().getFluid();
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (fluidOutput0.isPoI(side, position)) return tanks[1].getFluidAmount() > 0;
        if (fluidOutput1.isPoI(side, position)) return tanks[2].getFluidAmount() > 0;
        if (fluidOutput2.isPoI(side, position)) return tanks[3].getFluidAmount() > 0;
        return false;
    }

    private void pumpOutputOut() {
        IFluidHandler output;
        if (tanks[1].getFluidAmount() > 0) {
            output = FluidUtil.getFluidHandler(world, fluidOutputPos0, fluidOutput0.facing.getOpposite());
            if (output != null) {
                FluidStack out = tanks[1].getFluid();
                int accepted = output.fill(out, false);
                if (accepted > 0) {
                    assert out != null;
                    int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                    tanks[1].drain(drained, true);
                }
            }
        }
        if (tanks[2].getFluidAmount() > 0) {
            output = FluidUtil.getFluidHandler(world, fluidOutputPos1, fluidOutput1.facing.getOpposite());
            if (output != null) {
                FluidStack out = tanks[2].getFluid();
                int accepted = output.fill(out, false);
                if (accepted > 0) {
                    assert out != null;
                    int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                    tanks[2].drain(drained, true);
                }
            }
        }
        if (tanks[3].getFluidAmount() > 0) {
            output = FluidUtil.getFluidHandler(world, fluidOutputPos2, fluidOutput2.facing.getOpposite());
            if (output != null) {
                FluidStack out = tanks[3].getFluid();
                int accepted = output.fill(out, false);
                if (accepted > 0) {
                    assert out != null;
                    int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                    tanks[3].drain(drained, true);
                }
            }
        }
    }

    @Override @Nonnull public FluxStorage getFluxStorage() { return energyStorage; }

    @Override public boolean getIsMirrored() { return mirrored; }

    @Override public @Nonnull IEProperties.PropertyBoolInverted getBoolProperty(@Nonnull Class<? extends IEBlockInterfaces.IUsesBooleanProperty> inf) { return IEProperties.BOOLEANS[0]; }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }

    @SuppressWarnings("unused")
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
            if (master == null) return -1;
            for (int i = 0; i < master.tanks.length; i++) if (master.tanks[i] == tank) return i;
            return -1;
        }

        @Override public IFluidTankProperties[] getTankProperties() {
            IFluidTankProperties[] info = new IFluidTankProperties[tanks.length];
            for (int i = 0; i < tanks.length; i++) {
                FluidStack fs = tanks[i].getFluid();
                int idx = getTankIndex(tanks[i]);
                boolean canFill = idx == 0;
                boolean canDrain = idx > 0;
                info[i] = new FluidTankProperties(fs != null ? Utils.copyFluidStackWithAmount(fs, fs.amount, false) : null, tanks[i].getCapacity(), canFill, canDrain);
            }
            return info;
        }

        @Override public int fill(FluidStack resource, boolean doFill) {
            if (resource == null || resource.amount <= 0 || tanks.length == 0 || master == null) return 0;
            int filled = 0;
            for (IFluidTank tank : tanks) {
                int idx = getTankIndex(tank);
                if (idx == -1) continue;
                if (master.canFillTankFrom(idx, side, resource, position)) {
                    FluidStack copy = Utils.copyFluidStackWithAmount(resource, resource.amount - filled, false);
                    if (copy.amount <= 0) break;
                    int f = tank.fill(copy, doFill);
                    filled += f;
                    if (doFill && f > 0) master.TankContentsChanged();
                }
            }
            return filled;
        }

        @Override public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.amount <= 0 || tanks.length == 0 || master == null) return null;
            for (IFluidTank tank : tanks) {
                int idx = getTankIndex(tank);
                if (idx == -1) continue;
                if (master.canDrainTankFrom(idx, side, position)) {
                    FluidStack tankFluid = tank.getFluid();
                    if (tankFluid != null && tankFluid.isFluidEqual(resource)) {
                        int drainAmt = Math.min(resource.amount, tankFluid.amount);
                        FluidStack drained = tank.drain(drainAmt, doDrain);
                        if (drained != null && drained.amount > 0 && doDrain) master.TankContentsChanged();
                        return drained;
                    }
                }
            }
            return null;
        }

        @Override public FluidStack drain(int maxDrain, boolean doDrain) {
            if (maxDrain <= 0 || tanks.length == 0 || master == null) return null;
            for (IFluidTank tank : tanks) {
                int idx = getTankIndex(tank);
                if (idx == -1) continue;
                if (master.canDrainTankFrom(idx, side, position)) {
                    FluidStack drained = tank.drain(maxDrain, doDrain);
                    if (drained != null && drained.amount > 0 && doDrain) master.TankContentsChanged();
                    return drained;
                }
            }
            return null;
        }
    }
}
