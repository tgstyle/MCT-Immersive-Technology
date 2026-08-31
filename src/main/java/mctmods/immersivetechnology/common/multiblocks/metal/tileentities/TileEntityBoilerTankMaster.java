package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import com.immersiveconvergence.api.capability.IHeatProvider;
import com.immersiveconvergence.api.multiblock.PoICache;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.network.BinaryMessageTileSync;
import com.immersiveconvergence.api.network.IBinaryMessageReceiver;
import com.immersiveconvergence.api.util.ICFluidTank;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.api.crafting.BoilerTankRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartBoilerTank;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.conversion.BoilerLegacyConverter;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TileEntityBoilerTankMaster extends TileEntityBoilerTankSlave implements ICFluidTank.TankListener, IComparatorOverride, IIEInventory, IBinaryMessageReceiver {

    private static int tankSize() { return Multiblocks.boilerTank.boilerTank_tankSize; }
    private static int progressLossPerTick() { return Multiblocks.boilerTank.boilerTank_progress_lossInTicks; }
    private static double defaultWorkingHeatLevel() { return Multiblocks.boilerHeat.boiler_heat_workingLevel; }

    public FluidTank[] tanks = new FluidTank[] {
            new ICFluidTank(tankSize(), this),
            new ICFluidTank(tankSize(), this)
    };

    public static int slotCount = 4;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);

    public int processTimeRemaining = 0;
    public int processTimeMax = 0;
    public double heatLevel = 0;
    public double workingHeatLevel = defaultWorkingHeatLevel();

    private boolean isRunning = false;
    public BoilerTankRecipe cachedRecipe;
    private int oldComparatorOutput = 0;
    private int tickCountdown = 5;

    private boolean needsPoIInit = false;
    private boolean needsNotify = false;
    private NBTTagCompound legacyNbt = null;

    protected PoICache fluidInputPos0, fluidOutputPos0, heatInputPos0;
    private BlockPos fluidOutputTEPos0, heatSourceTEPos0;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        if (!descPacket && nbt.hasKey("tank2")) {
            legacyNbt = nbt.copy();
            tanks[0].readFromNBT(nbt.getCompoundTag("tank1"));
            tanks[1].readFromNBT(nbt.getCompoundTag("tank2"));
            heatLevel = 0;
            processTimeRemaining = nbt.getInteger("processTimeRemaining");
            processTimeMax = nbt.getInteger("processTimeMax");
            NonNullList<ItemStack> legacyInventory = Utils.readInventory(nbt.getTagList("inventory", 10), 6);
            for (int slot = 0; slot < slotCount; slot++) { inventory.set(slot, legacyInventory.get(slot + 2)); }
            if (processTimeRemaining > 0) { cachedRecipe = BoilerTankRecipe.findRecipe(tanks[0].getFluid()); }
            if (processTimeRemaining > 0 && cachedRecipe == null) processTimeRemaining = 0;
        }
        else {
            tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
            tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
            heatLevel = nbt.getDouble("heatLevel");
            if (nbt.hasKey("workingHeatLevel")) { workingHeatLevel = nbt.getDouble("workingHeatLevel"); }
            processTimeRemaining = nbt.getInteger("processTimeRemaining");
            processTimeMax = nbt.getInteger("processTimeMax");
            oldComparatorOutput = nbt.getInteger("oldComparatorOutput");
            if (!descPacket) {
                inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
                if (nbt.hasKey("cachedRecipe")) cachedRecipe = BoilerTankRecipe.loadFromNBT(nbt.getCompoundTag("cachedRecipe"));
                if (processTimeRemaining > 0 && cachedRecipe == null) processTimeRemaining = 0;
            }
        }
        if (formed && !descPacket) {
            needsPoIInit = true;
            needsNotify = true;
        }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setDouble("heatLevel", heatLevel);
        nbt.setDouble("workingHeatLevel", workingHeatLevel);
        nbt.setInteger("processTimeRemaining", processTimeRemaining);
        nbt.setInteger("processTimeMax", processTimeMax);
        nbt.setInteger("oldComparatorOutput", oldComparatorOutput);
        if (!descPacket) {
            nbt.setTag("inventory", Utils.writeInventory(inventory));
            if (cachedRecipe != null) nbt.setTag("cachedRecipe", cachedRecipe.writeToNBT(new NBTTagCompound()));
        }
    }

    void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartBoilerTank.instance.pointsOfInterest) {
            switch (poi.name) {
                case "fluid_input0":
                    fluidInputPos0 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_output0":
                    fluidOutputPos0 = new PoICache(facing, poi, mirrored);
                    fluidOutputTEPos0 = getBlockPosForPos(fluidOutputPos0.position).offset(fluidOutputPos0.facing);
                    break;
                case "heat_input0":
                    heatInputPos0 = new PoICache(facing, poi, mirrored);
                    heatSourceTEPos0 = getBlockPosForPos(heatInputPos0.position).offset(heatInputPos0.facing);
                    break;
            }
        }
    }

    public boolean isHeatInputPoI(BlockPos position) {
        if (heatInputPos0 == null) InitializePoIs();
        return heatInputPos0.position.equals(position);
    }

    private void notifyIONeighbors() {
        if (fluidInputPos0 != null) world.notifyNeighborsOfStateChange(getBlockPosForPos(fluidInputPos0.position), getBlockType(), true);
        if (fluidOutputPos0 != null) world.notifyNeighborsOfStateChange(getBlockPosForPos(fluidOutputPos0.position), getBlockType(), true);
    }

    private void notifyNearbyClients() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeDouble(heatLevel);
        buf.writeDouble(workingHeatLevel);
        buf.writeInt(processTimeRemaining);
        buf.writeInt(processTimeMax);
        buf.writeBoolean(isRunning);
        BinaryMessageTileSync.sendToAllTracking(world, getPos(), buf);
    }

    @Override public void receiveMessageFromServer(ByteBuf message) {
        heatLevel = message.readDouble();
        workingHeatLevel = message.readDouble();
        processTimeRemaining = message.readInt();
        processTimeMax = message.readInt();
        isRunning = message.readBoolean();
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {}

    @Override public void update() {
        super.update();
        if (!formed) return;
        if (!world.isRemote && legacyNbt != null) {
            NBTTagCompound nbt = legacyNbt;
            legacyNbt = null;
            BoilerLegacyConverter.convert(this, nbt);
        }
        if (needsPoIInit || fluidInputPos0 == null || fluidOutputPos0 == null || heatInputPos0 == null) {
            InitializePoIs();
            needsPoIInit = false;
        }
        if (needsNotify) {
            notifyIONeighbors();
            needsNotify = false;
        }
        if (world.isRemote) return;

        boolean changed = heatLogic();
        if (recipeLogic()) changed = true;
        if (outputTankLogic()) changed = true;
        if (inputTankLogic()) changed = true;

        boolean wasRunning = isRunning;
        isRunning = heatLevel >= workingHeatLevel && processTimeRemaining > 0;

        if (changed || isRunning != wasRunning) {
            if (tickCountdown-- <= 0) {
                notifyNearbyClients();
                tickCountdown = 5;
            }
            world.markChunkDirty(getPos(), this);
        }
        int comp = getComparatorInputOverride();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            world.updateComparatorOutputLevel(getPos(), getBlockType());
        }
        if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
        else if (changed) { throttledBlockUpdate(); }
    }

    private boolean heatLogic() {
        double previousHeat = heatLevel;
        double previousWorking = workingHeatLevel;
        heatLevel = 0;
        TileEntity te = world.getTileEntity(heatSourceTEPos0);
        if (te instanceof IHeatProvider) { heatLevel = ((IHeatProvider)te).getHeatLevel(); }
        double displayMax = defaultWorkingHeatLevel();
        BoilerTankRecipe recipe = cachedRecipe;
        if (recipe == null && tanks[0].getFluidAmount() > 0) { recipe = BoilerTankRecipe.findRecipe(tanks[0].getFluid()); }
        if (recipe != null) { displayMax = Math.max(displayMax, recipe.requiredHeat); }
        workingHeatLevel = Math.max(displayMax, heatLevel);
        return previousHeat != heatLevel || previousWorking != workingHeatLevel;
    }

    private boolean recipeLogic() {
        boolean update = false;
        double required = cachedRecipe != null ? Math.max(defaultWorkingHeatLevel(), cachedRecipe.requiredHeat) : defaultWorkingHeatLevel();
        if (heatLevel >= required) {
            if (processTimeRemaining > 0) {
                if (gainProgress()) update = true;
            }
            else if (tanks[0].getFluidAmount() > 0) {
                cachedRecipe = (cachedRecipe != null && Objects.requireNonNull(tanks[0].getFluid()).isFluidEqual(cachedRecipe.fluidInput)) ? cachedRecipe : BoilerTankRecipe.findRecipe(tanks[0].getFluid());
                if (cachedRecipe != null && heatLevel >= cachedRecipe.requiredHeat && cachedRecipe.fluidInput.amount <= tanks[0].getFluidAmount() && cachedRecipe.fluidOutput.amount == tanks[1].fillInternal(cachedRecipe.fluidOutput, false)) {
                    processTimeRemaining = cachedRecipe.getTotalProcessTime();
                    processTimeMax = processTimeRemaining;
                    if (gainProgress()) update = true;
                }
            }
        }
        else if (processTimeRemaining > 0) {
            if (loseProgress()) update = true;
        }
        return update;
    }

    private boolean outputTankLogic() {
        boolean update = false;
        if (tanks[1].getFluidAmount() > 0) {
            ItemStack filled = Utils.fillFluidContainer(tanks[1], inventory.get(2), inventory.get(3), null);
            if (!filled.isEmpty()) {
                if (!inventory.get(3).isEmpty() && OreDictionary.itemMatches(inventory.get(3), filled, true)) inventory.get(3).grow(filled.getCount());
                else if (inventory.get(3).isEmpty()) inventory.set(3, filled.copy());
                inventory.get(2).shrink(1);
                if (inventory.get(2).getCount() <= 0) inventory.set(2, ItemStack.EMPTY);
                update = true;
            }
            if (pumpOutputOut()) update = true;
        }
        return update;
    }

    private boolean inputTankLogic() {
        int prev = tanks[0].getFluidAmount();
        ItemStack empty = Utils.drainFluidContainer(tanks[0], inventory.get(0), inventory.get(1), null);
        if (prev != tanks[0].getFluidAmount()) {
            if (!inventory.get(1).isEmpty() && OreDictionary.itemMatches(inventory.get(1), empty, true)) inventory.get(1).grow(empty.getCount());
            else if (inventory.get(1).isEmpty()) inventory.set(1, empty.copy());
            inventory.get(0).shrink(1);
            if (inventory.get(0).getCount() <= 0) inventory.set(0, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    private boolean loseProgress() {
        if (cachedRecipe == null) { processTimeRemaining = 0; return true; }
        int previous = processTimeRemaining;
        processTimeRemaining = Math.min(processTimeRemaining + progressLossPerTick(), cachedRecipe.getTotalProcessTime());
        return previous != processTimeRemaining;
    }

    private boolean gainProgress() {
        if (cachedRecipe == null) { processTimeRemaining = 0; return true; }
        processTimeRemaining--;
        if (processTimeRemaining == 0) {
            BoilerTankRecipe completingRecipe = cachedRecipe;
            cachedRecipe = null;
            processTimeMax = 0;
            tanks[0].drain(completingRecipe.fluidInput.amount, true);
            tanks[1].fillInternal(completingRecipe.fluidOutput, true);
            return true;
        }
        return false;
    }

    private boolean pumpOutputOut() {
        if (tanks[1].getFluidAmount() == 0) return false;
        if (fluidOutputPos0 == null) InitializePoIs();
        IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputTEPos0, fluidOutputPos0.facing.getOpposite());
        if (output == null) return false;
        FluidStack out = tanks[1].getFluid();
        int accepted = output.fill(out, false);
        if (accepted <= 0) return false;
        assert out != null;
        int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        tanks[1].drain(drained, true);
        return drained > 0;
    }

    @Override public void disassemble() {
        if (!world.isRemote) {
            for (ItemStack stack : inventory) if (!stack.isEmpty()) Utils.dropStackAtPos(world, getPos(), stack);
            inventory.clear();
        }
        super.disassemble();
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            if (fluidInputPos0 == null) InitializePoIs();
            if (fluidInputPos0.isPoI(facing, posInMultiblock()) || fluidOutputPos0.isPoI(facing, posInMultiblock())) return true;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            if (fluidInputPos0.isPoI(facing, posInMultiblock()) || fluidOutputPos0.isPoI(facing, posInMultiblock())) {
                return (T)new BoilerTankFluidHandler(getAccessibleFluidTanks(facing, posInMultiblock()), this, facing, posInMultiblock());
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityBoilerTankMaster master() { return this; }

    @Override public TileEntity getGuiMaster() { return this; }

    @Override public void TankContentsChanged() {
        if (processTimeRemaining == 0) { cachedRecipe = null; }
        markContainingBlockForUpdate(null);
    }

    @Override public int getComparatorInputOverride() { return workingHeatLevel > 0 ? (int)Math.min(15, 15 * (heatLevel / workingHeatLevel)) : 0; }

    @Override @Nonnull public NonNullList<ItemStack> getInventory() { return inventory; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) { markDirty(); markContainingBlockForUpdate(null); }

    @Override @Nonnull public NonNullList<ItemStack> getDroppedItems() { return inventory; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, BlockPos position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (fluidInputPos0 == null) InitializePoIs();
        if (side == null) return tanks;
        if (fluidInputPos0.isPoI(side, position)) return new IFluidTank[]{tanks[0]};
        if (fluidOutputPos0.isPoI(side, position)) return new IFluidTank[]{tanks[1]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        if (fluidInputPos0 == null) InitializePoIs();
        if (iTank == 0 && fluidInputPos0.isPoI(side, position)) {
            if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
            if (tanks[0].getFluid() == null) { return true; }
            return resource.isFluidEqual(tanks[0].getFluid());
        }
        return false;
    }

    @Override protected boolean isInputFluidPoI(BlockPos position) {
        if (fluidInputPos0 == null) { InitializePoIs(); }
        return fluidInputPos0.position.equals(position);
    }

    @Override protected int clearInputTanks() {
        tanks[0].drain(Integer.MAX_VALUE, true);
        TankContentsChanged();
        return 1;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        if (fluidOutputPos0 == null) InitializePoIs();
        return iTank == 1 && fluidOutputPos0.isPoI(side, position);
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }

    public static class BoilerTankFluidHandler implements IFluidHandler {
        private final IFluidTank[] accessibleTanks;
        private final TileEntityBoilerTankMaster master;
        private final EnumFacing side;
        private final BlockPos position;

        public BoilerTankFluidHandler(IFluidTank[] accessibleTanks, TileEntityBoilerTankMaster master, EnumFacing side, BlockPos position) {
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
                list.add(new FluidTankProperties(tank.getFluid(), tank.getCapacity(), index == 0, index == 1));
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
                        else if (drained.isFluidEqual(d)) drained.amount += d.amount;
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
