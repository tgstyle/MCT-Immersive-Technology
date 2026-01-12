package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.crafting.DistillerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartDistiller;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.multiblock.PoICache;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.IBinaryMessageReceiver;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TileEntityDistillerMaster extends TileEntityDistillerSlave implements ITFluidTank.TankListener, IIEInventory, IBinaryMessageReceiver, IComparatorOverride {

    private static final int inputTankSize = Multiblocks.distiller.distiller_input_tankSize;
    private static final int outputTankSize = Multiblocks.distiller.distiller_output_tankSize;
    private static final int energyCapacity = Multiblocks.distiller.distiller_energy_size;
    private static final int energyMaxInput = Multiblocks.distiller.distiller_energy_maxInput;

    public FluxStorageAdvanced energyStorage = new FluxStorageAdvanced(energyCapacity, energyMaxInput, energyMaxInput);
    public ITFluidTank[] tanks = new ITFluidTank[] {
            new ITFluidTank(inputTankSize, this),
            new ITFluidTank(outputTankSize, this)
    };

    public static int slotCount = 5;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);

    private boolean running;
    private float soundVolume = 0f;
    private int soundGracePeriod = 60;

    protected PoICache energyInput0, fluidInput0, fluidOutput0, itemOutput0, redstone0;
    private BlockPos fluidOutputFront0, itemOutputFront0, soundPos0;
    public boolean redstoneControlInverted = false;
    private int oldComparatorOutput;

    public DistillerRecipe cachedDistillerRecipe;
    public int processTimeRemaining = 0;

    private boolean needsPoIInit = false;

    public TileEntityDistillerMaster() { super(); }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        energyStorage.readFromNBT(nbt.getCompoundTag("energy"));
        running = nbt.getBoolean("running");
        redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
        oldComparatorOutput = nbt.getInteger("oldComparatorOutput");
        processTimeRemaining = nbt.getInteger("processTimeRemaining");
        if (!descPacket && nbt.hasKey("cachedRecipe")) cachedDistillerRecipe = DistillerRecipe.loadFromNBT(nbt.getCompoundTag("cachedRecipe"));
        if (!descPacket && formed) needsPoIInit = true;
        if (!descPacket) {
            inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
        }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setTag("energy", energyStorage.writeToNBT(new NBTTagCompound()));
        nbt.setBoolean("running", running);
        nbt.setBoolean("redstoneControlInverted", redstoneControlInverted);
        nbt.setInteger("oldComparatorOutput", oldComparatorOutput);
        nbt.setInteger("processTimeRemaining", processTimeRemaining);
        if (!descPacket && cachedDistillerRecipe != null) nbt.setTag("cachedRecipe", cachedDistillerRecipe.writeToNBT(new NBTTagCompound()));
        if (!descPacket) {
            nbt.setTag("inventory", Utils.writeInventory(inventory));
        }
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (soundPos0 == null) InitializePoIs();
        float targetSoundLevel = running ? 1f : 0f;
        if (soundVolume < targetSoundLevel) {
            soundVolume = Math.min(soundVolume + 0.01f, targetSoundLevel);
            soundGracePeriod = 60;
        } else if (soundVolume > targetSoundLevel) {
            if (soundGracePeriod > 0) soundGracePeriod--;
            else soundVolume = Math.max(soundVolume - 0.01f, targetSoundLevel);
        }
        if (soundVolume == 0) ITSoundHandler.StopSound(soundPos0);
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos0.getX(), soundPos0.getY(), soundPos0.getZ()) / 8, 1);
            ITSounds.distiller.PlayRepeating(soundPos0, soundVolume / attenuation, 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        ITSoundHandler.StopSound(soundPos0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        if (soundPos0 != null) {
            ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
        }
        super.disassemble();
    }

    public void notifyNearbyClients() {
        BinaryMessageTileSync.sendToAllTracking(world, getPos(), Unpooled.copyBoolean(running));
    }

    @Override public void receiveMessageFromServer(ByteBuf message) { running = message.readBoolean(); }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {}

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    private void pumpOutputOut() {
        if (tanks[1].getFluidAmount() == 0) return;
        if (fluidOutput0 == null) InitializePoIs();
        if (fluidOutput0 == null) return;
        IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputFront0, fluidOutput0.facing.getOpposite());
        if (output == null) return;
        FluidStack out = tanks[1].getFluid();
        if (out == null) return;
        int accepted = output.fill(out, false);
        if (accepted > 0) {
            int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
            tanks[1].drain(drained, true);
        }
    }

    @Override public void update() {
        if (!formed) return;

        if (needsPoIInit || energyInput0 == null || fluidInput0 == null || fluidOutput0 == null || itemOutput0 == null || redstone0 == null || soundPos0 == null) {
            InitializePoIs();
            needsPoIInit = false;
        }

        super.update();

        if (world.isRemote) {
            handleSounds();
            return;
        }

        boolean update = false;

        boolean shouldRun = !isRSDisabled();

        if (processTimeRemaining == 0 && shouldRun) {
            FluidStack input = tanks[0].getFluid();
            if (input != null && input.amount > 0) {
                cachedDistillerRecipe = DistillerRecipe.findRecipe(input);
                if (cachedDistillerRecipe != null && cachedDistillerRecipe.fluidInput != null && input.amount >= cachedDistillerRecipe.fluidInput.amount) {
                    boolean canOutput = cachedDistillerRecipe.fluidOutput == null || tanks[1].fill(cachedDistillerRecipe.fluidOutput, false) == cachedDistillerRecipe.fluidOutput.amount;
                    if (canOutput) {
                        processTimeRemaining = cachedDistillerRecipe.getTotalProcessTime();
                        tanks[0].drain(cachedDistillerRecipe.fluidInput.amount, true);
                        update = true;
                    }
                }
            }
        }

        if (processTimeRemaining > 0 && shouldRun) {
            if (cachedDistillerRecipe == null) {
                processTimeRemaining = 0;
                update = true;
            } else if (cachedDistillerRecipe.getTotalProcessTime() > 0) {
                int energyPerTick = cachedDistillerRecipe.getTotalProcessEnergy() / cachedDistillerRecipe.getTotalProcessTime();
                int extracted = energyStorage.extractEnergy(energyPerTick, true);
                if (extracted >= energyPerTick) {
                    energyStorage.extractEnergy(energyPerTick, false);
                    processTimeRemaining--;
                    update = true;
                    if (processTimeRemaining <= 0) {
                        if (cachedDistillerRecipe.fluidOutput != null) tanks[1].fill(cachedDistillerRecipe.fluidOutput, true);
                        cachedDistillerRecipe = null;
                    }
                }
            } else {
                processTimeRemaining = 0;
            }
        }

        if (tanks[1].getFluidAmount() > 0) {
            ItemStack filled = Utils.fillFluidContainer(tanks[1], inventory.get(2), inventory.get(3), null);
            if (!filled.isEmpty()) {
                if (!inventory.get(3).isEmpty() && OreDictionary.itemMatches(inventory.get(3), filled, true)) inventory.get(3).grow(filled.getCount());
                else if (inventory.get(3).isEmpty()) inventory.set(3, filled.copy());
                inventory.get(2).shrink(1);
                if (inventory.get(2).getCount() <= 0) inventory.set(2, ItemStack.EMPTY);
                update = true;
            }
        }
        ItemStack empty = Utils.drainFluidContainer(tanks[0], inventory.get(0), inventory.get(1), null);
        if (!empty.isEmpty()) {
            if (!inventory.get(1).isEmpty() && OreDictionary.itemMatches(inventory.get(1), empty, true)) inventory.get(1).grow(empty.getCount());
            else if (inventory.get(1).isEmpty()) inventory.set(1, empty.copy());
            inventory.get(0).shrink(1);
            if (inventory.get(0).getCount() <= 0) inventory.set(0, ItemStack.EMPTY);
            update = true;
        }
        pumpOutputOut();
        if (!inventory.get(4).isEmpty()) {
            if (itemOutput0 == null) InitializePoIs();
            if (itemOutput0 == null) return;
            TileEntity te = world.getTileEntity(itemOutputFront0);
            if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, itemOutput0.facing.getOpposite())) {
                IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, itemOutput0.facing.getOpposite());
                if (handler != null) {
                    ItemStack remaining = ItemHandlerHelper.insertItemStacked(handler, inventory.get(4), false);
                    inventory.set(4, remaining);
                    if (remaining.isEmpty()) update = true;
                }
            }
        }
        boolean currentlyRunning = processTimeRemaining > 0 && shouldRun;
        if (running != currentlyRunning) {
            running = currentlyRunning;
            notifyNearbyClients();
            update = true;
        }
        if (update) {
            efficientMarkDirty();
            markContainingBlockForUpdate(null);
        }
        int comp = getComparatorInputOverride();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            world.updateComparatorOutputLevel(getPos(), getBlockType());
        }
    }

    @Override @Nonnull public int[] getOutputSlots() { return new int[]{4}; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<DistillerRecipe> process) { return true; }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityDistillerMaster master() { return this; }

    @Override public void TankContentsChanged() {
        if (processTimeRemaining == 0) cachedDistillerRecipe = null;
        efficientMarkDirty();
        markContainingBlockForUpdate(null);
    }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, int position) {
        if (!formed) return false;
        if (energyInput0 == null) InitializePoIs();
        return facing != null && energyInput0.isPoI(facing, position);
    }

    @Override @Nonnull public int[] getEnergyPos() {
        if (!formed) return new int[0];
        if (energyInput0 == null) InitializePoIs();
        return new int[] {energyInput0.position};
    }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstone0 == null) InitializePoIs();
        return new int[] {redstone0.position};
    }

    @Override public boolean isRSDisabled() {
        if (redstone0 == null) InitializePoIs();
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

    @Override public int getComparatorInputOverride() { return 15 * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored(); }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartDistiller.instance.pointsOfInterest) {
            switch (poi.name) {
                case "energy_input0":
                    energyInput0 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_input0":
                    fluidInput0 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_output0":
                    fluidOutput0 = new PoICache(facing, poi, mirrored);
                    fluidOutputFront0 = getBlockPosForPos(fluidOutput0.position).offset(fluidOutput0.facing);
                    break;
                case "item_output0":
                    itemOutput0 = new PoICache(facing, poi, mirrored);
                    itemOutputFront0 = getBlockPosForPos(itemOutput0.position).offset(itemOutput0.facing.getOpposite());
                    break;
                case "redstone0":
                    redstone0 = new PoICache(facing, poi, mirrored);
                    break;
                case "sound0":
                    soundPos0 = getBlockPosForPos(poi.position);
                    break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(energyInput0.position));
        notifyNeighbor(getBlockPosForPos(fluidInput0.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput0.position));
        notifyNeighbor(getBlockPosForPos(itemOutput0.position));
        notifyNeighbor(getBlockPosForPos(redstone0.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override @Nonnull public IFluidTank[] getInternalTanks() { return tanks; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if (fluidInput0 == null) InitializePoIs();
        if (fluidInput0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        if (fluidOutput0.isPoI(side, position)) return new IFluidTank[] {tanks[1]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (iTank != 0) return false;
        if (fluidInput0 == null) InitializePoIs();
        if (!fluidInput0.isPoI(side, position)) return false;
        if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
        FluidStack current = tanks[0].getFluid();
        if (current != null) return resource.isFluidEqual(current);
        return DistillerRecipe.findRecipe(resource) != null;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (iTank != 1) return false;
        if (fluidOutput0 == null) InitializePoIs();
        if (!fluidOutput0.isPoI(side, position)) return false;
        return tanks[1].getFluidAmount() > 0;
    }

    @Override @Nonnull public NonNullList<ItemStack> getInventory() { return inventory; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) {
        efficientMarkDirty();
        markContainingBlockForUpdate(null);
    }

    @Override @Nonnull public NonNullList<ItemStack> getDroppedItems() { return inventory; }

    @Override public int getComparatedSize() { return slotCount; }

    public static class DistillerFluidHandler implements IFluidHandler {
        private final IFluidTank[] accessibleTanks;
        private final TileEntityDistillerMaster master;
        private final EnumFacing side;
        private final int position;

        public DistillerFluidHandler(IFluidTank[] accessibleTanks, TileEntityDistillerMaster master, EnumFacing side, int position) {
            this.accessibleTanks = accessibleTanks;
            this.master = master;
            this.side = side;
            this.position = position;
        }

        private int getTankIndex(IFluidTank tank) {
            for (int i = 0; i < master.tanks.length; i++) {
                if (master.tanks[i] == tank) return i;
            }
            return -1;
        }

        @Override public IFluidTankProperties[] getTankProperties() {
            List<IFluidTankProperties> list = new ArrayList<>();
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
                    FluidStack tankFluid = accessible.getFluid();
                    if (tankFluid != null && tankFluid.isFluidEqual(resource)) {
                        int amount = Math.min(resource.amount, tankFluid.amount);
                        FluidStack d = accessible.drain(amount, doDrain);
                        if (d != null) {
                            if (drained == null) drained = d.copy();
                            else drained.amount += d.amount;
                            resource.amount -= d.amount;
                            if (doDrain && d.amount > 0) master.TankContentsChanged();
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
                        else drained.amount += d.amount;
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
