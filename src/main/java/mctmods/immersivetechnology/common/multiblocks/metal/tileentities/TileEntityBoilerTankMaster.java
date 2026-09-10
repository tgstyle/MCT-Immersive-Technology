package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.capability.IHeatProvider;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.IComparatorOverride;
import com.immersiveconvergence.api.network.BinaryTileSyncMessage;
import com.immersiveconvergence.api.network.IBinaryMessageReceiver;
import com.immersiveconvergence.api.util.ICFluidTank;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.util.IICInventory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.api.crafting.BoilerTankRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.conversion.BoilerLegacyConverter;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Objects;

public class TileEntityBoilerTankMaster extends TileEntityBoilerTankSlave implements ICFluidTank.TankListener, IComparatorOverride, IICInventory, IBinaryMessageReceiver {
    private static int tankSize() { return Multiblocks.boilerTank.boilerTank_tankSize; }

    private static int progressLossPerTick() { return Multiblocks.boilerTank.boilerTank_progress_lossInTicks; }

    private static double defaultWorkingHeatLevel() { return Multiblocks.boilerHeat.workingLevel(); }

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

    private NBTTagCompound legacyNbt = null;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        if (!descPacket && nbt.hasKey("tank2")) {
            legacyNbt = nbt.copy();
            tanks[0].readFromNBT(nbt.getCompoundTag("tank1"));
            tanks[1].readFromNBT(nbt.getCompoundTag("tank2"));
            heatLevel = 0;
            processTimeRemaining = nbt.getInteger("processTimeRemaining");
            processTimeMax = nbt.getInteger("processTimeMax");
            NonNullList<ItemStack> legacyInventory = ICUtils.readInventory(nbt.getTagList("inventory", 10), 6);
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
                inventory = ICUtils.readInventory(nbt.getTagList("inventory", 10), slotCount);
                if (nbt.hasKey("cachedRecipe")) cachedRecipe = BoilerTankRecipe.loadFromNBT(nbt.getCompoundTag("cachedRecipe"));
                if (processTimeRemaining > 0 && cachedRecipe == null) processTimeRemaining = 0;
            }
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
            nbt.setTag("inventory", ICUtils.writeInventory(inventory));
            if (cachedRecipe != null) nbt.setTag("cachedRecipe", cachedRecipe.writeToNBT(new NBTTagCompound()));
        }
    }

    public boolean isHeatInputPoI(BlockPos position) {
        return poi("heat_input0").position.equals(position);
    }

    private void notifyNearbyClients() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeDouble(heatLevel);
        buf.writeDouble(workingHeatLevel);
        buf.writeInt(processTimeRemaining);
        buf.writeInt(processTimeMax);
        buf.writeBoolean(isRunning);
        BinaryTileSyncMessage.sendToAllTracking(world, getPos(), buf);
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
        int comp = comparatorValue();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            notifyComparators();
        }
        if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
        else if (changed) { throttledBlockUpdate(); }
    }

    private boolean heatLogic() {
        double previousHeat = heatLevel;
        double previousWorking = workingHeatLevel;
        heatLevel = 0;
        TileEntity te = world.getTileEntity(poiFrontPos("heat_input0"));
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
                BoilerTankRecipe recipe = (cachedRecipe != null && Objects.requireNonNull(tanks[0].getFluid()).isFluidEqual(cachedRecipe.fluidInput)) ? cachedRecipe : BoilerTankRecipe.findRecipe(tanks[0].getFluid());
                cachedRecipe = recipe;
                if (recipe != null && heatLevel >= recipe.requiredHeat && recipe.fluidInput.amount <= tanks[0].getFluidAmount() && recipe.fluidOutput.amount == tanks[1].fillInternal(recipe.fluidOutput, false)) {
                    FluidStack drained = tanks[0].drain(recipe.fluidInput.amount, true);
                    if (drained != null && drained.amount == recipe.fluidInput.amount && drained.isFluidEqual(recipe.fluidInput)) {
                        cachedRecipe = recipe;
                        processTimeRemaining = recipe.getTotalProcessTime();
                        processTimeMax = processTimeRemaining;
                        if (gainProgress()) update = true;
                    }
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
            ItemStack filled = ICUtils.fillFluidContainer(tanks[1], inventory.get(2), inventory.get(3), null);
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
        ItemStack empty = ICUtils.drainFluidContainer(tanks[0], inventory.get(0), inventory.get(1), null);
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
            tanks[1].fillInternal(completingRecipe.fluidOutput, true);
            return true;
        }
        return false;
    }

    private boolean pumpOutputOut() {
        if (tanks[1].getFluidAmount() == 0) return false;
        IFluidHandler output = FluidUtil.getFluidHandler(world, poiFrontPos("fluid_output0"), poi("fluid_output0").facing.getOpposite());
        if (output == null) return false;
        FluidStack out = tanks[1].getFluid();
        int accepted = output.fill(out, false);
        if (accepted <= 0) return false;
        assert out != null;
        int drained = output.fill(ICUtils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        tanks[1].drain(drained, true);
        return drained > 0;
    }

    @Override public void disassemble() {
        if (!world.isRemote) {
            for (ItemStack stack : inventory) if (!stack.isEmpty()) ICUtils.dropStackAtPos(world, getPos(), stack);
            inventory.clear();
        }
        super.disassemble();
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityBoilerTankMaster master() { return this; }

    @Override public TileEntity getGuiMaster() { return this; }

    @Override public void TankContentsChanged() {
        if (processTimeRemaining == 0) { cachedRecipe = null; }
        requestClientSync();
    }

    @Override public int getComparatorInputOverride() { return isComparatorPos() ? comparatorValue() : 0; }

    public int comparatorValue() { return workingHeatLevel > 0 ? (int)Math.min(15, 15 * (heatLevel / workingHeatLevel)) : 0; }

    @Override @Nonnull public NonNullList<ItemStack> getInventory() { return inventory; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) { markDirty(); markContainingBlockForUpdate(null); }

    @Override @Nonnull public NonNullList<ItemStack> getDroppedItems() { return inventory; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, BlockPos position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (side == null) return tanks;
        if (isPoI("fluid_input0", side, position)) return tankView(0, tanks[0]);
        if (isPoI("fluid_output0", side, position)) return tankView(1, tanks[1]);
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        if (iTank == 0 && isPoI("fluid_input0", side, position)) {
            if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
            if (tanks[0].getFluid() == null) { return true; }
            return resource.isFluidEqual(tanks[0].getFluid());
        }
        return false;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        return iTank == 1 && isPoI("fluid_output0", side, position);
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return ITUtils.EMPTY_INT_ARRAY; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return ITUtils.EMPTY_INT_ARRAY; }
}
