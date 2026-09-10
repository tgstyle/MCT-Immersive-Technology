package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.client.ICSoundHandler;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.IComparatorOverride;
import com.immersiveconvergence.api.network.BinaryTileSyncMessage;
import com.immersiveconvergence.api.network.IBinaryMessageReceiver;
import com.immersiveconvergence.api.util.ICFluidTank;
import com.immersiveconvergence.api.util.ICFluxStorageAdvanced;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.util.IICInventory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.api.crafting.DistillerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.ITSounds;

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
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;

public class TileEntityDistillerMaster extends TileEntityDistillerSlave implements ICFluidTank.TankListener, IICInventory, IBinaryMessageReceiver, IComparatorOverride {
    private static int inputTankSize() { return Multiblocks.distiller.distiller_input_tankSize; }

    private static int outputTankSize() { return Multiblocks.distiller.distiller_output_tankSize; }

    private static int energyCapacity() { return Multiblocks.distiller.distiller_energy_size; }

    private static int energyMaxInput() { return Multiblocks.distiller.distiller_energy_maxInput; }

    public ICFluxStorageAdvanced energyStorage = new ICFluxStorageAdvanced(energyCapacity(), energyMaxInput(), energyMaxInput());
    public ICFluidTank[] tanks = new ICFluidTank[] {
            new ICFluidTank(inputTankSize(), this),
            new ICFluidTank(outputTankSize(), this)
    };

    public static int slotCount = 5;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);

    public int processTimeRemaining = 0;
    public int processTimeMax = 0;

    private boolean isRunning = false;
    private float soundVolume = 0f;
    private int soundGracePeriod = 0;

    public DistillerRecipe cachedDistillerRecipe;
    public boolean redstoneControlInverted = false;
    private int oldComparatorOutput;

    private int tickCountdown = 5;

    private static final int[] ITEM_OUTPUT_SLOTS = {1, 3, 4};

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        energyStorage.readFromNBT(nbt.getCompoundTag("energy"));
        isRunning = nbt.getBoolean("isRunning");
        redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
        oldComparatorOutput = nbt.getInteger("oldComparatorOutput");
        processTimeRemaining = nbt.getInteger("processTimeRemaining");
        processTimeMax = nbt.getInteger("processTimeMax");
        soundGracePeriod = nbt.getInteger("soundGracePeriod");
        if (!descPacket) {
            if (nbt.hasKey("cachedRecipe")) cachedDistillerRecipe = DistillerRecipe.loadFromNBT(nbt.getCompoundTag("cachedRecipe"));
            inventory = ICUtils.readInventory(nbt.getTagList("inventory", 10), slotCount);
            if (processTimeRemaining > 0 && cachedDistillerRecipe == null) processTimeRemaining = 0;
        }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setTag("energy", energyStorage.writeToNBT(new NBTTagCompound()));
        nbt.setBoolean("isRunning", isRunning);
        nbt.setBoolean("redstoneControlInverted", redstoneControlInverted);
        nbt.setInteger("oldComparatorOutput", oldComparatorOutput);
        nbt.setInteger("processTimeRemaining", processTimeRemaining);
        nbt.setInteger("processTimeMax", processTimeMax);
        nbt.setInteger("soundGracePeriod", soundGracePeriod);
        if (!descPacket) {
            if (cachedDistillerRecipe != null) nbt.setTag("cachedRecipe", cachedDistillerRecipe.writeToNBT(new NBTTagCompound()));
            nbt.setTag("inventory", ICUtils.writeInventory(inventory));
        }
    }

    @SideOnly(Side.CLIENT)
    private void handleSounds() {
        BlockPos soundPos = poiWorldPos("sound0");
        float target = isRunning ? 1f : 0f;
        if (soundVolume < target) { soundVolume = Math.min(soundVolume + 0.01f, target); }
        else if (soundVolume > target) { soundVolume = Math.max(soundVolume - 0.01f, target); }
        if (soundVolume <= 0f) {
            ICSoundHandler.stopSound(soundPos);
            soundVolume = 0f;
        } else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos.getX(), soundPos.getY(), soundPos.getZ()) / 32f, 1f);
            ITSounds.distiller.PlayRepeating(soundPos, soundVolume / attenuation, 1f);
        }
    }

    @Override public void disassemble() {
        if (!world.isRemote) {
            for (ItemStack stack : inventory) if (!stack.isEmpty()) ICUtils.dropStackAtPos(world, getPos(), stack);
            inventory.clear();
        }
        super.disassemble();
    }

    void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    @Override public void update() {
        if (!formed) return;
        super.update();
        if (world.isRemote) {
            handleSounds();
            return;
        }
        int oldEnergy = energyStorage.getEnergyStored();
        int oldProcess = processTimeRemaining;
        boolean wasRunning = isRunning;
        boolean update = false;
        boolean shouldRun = !isRSDisabled();
        if (processTimeRemaining == 0 && shouldRun) {
            FluidStack input = tanks[0].getFluid();
            if (input != null && input.amount > 0) {
                DistillerRecipe recipe = cachedDistillerRecipe;
                if (recipe == null || recipe.fluidInput == null || !input.isFluidEqual(recipe.fluidInput)) { recipe = DistillerRecipe.findRecipe(input); }
                if (recipe != null && recipe.fluidInput != null && input.amount >= recipe.fluidInput.amount) {
                    boolean canOutput = recipe.fluidOutput == null || tanks[1].fill(recipe.fluidOutput, false) == recipe.fluidOutput.amount;
                    if (canOutput) {
                        cachedDistillerRecipe = recipe;
                        processTimeRemaining = recipe.getTotalProcessTime();
                        processTimeMax = processTimeRemaining;
                        tanks[0].drain(recipe.fluidInput.amount, true);
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
                        DistillerRecipe completingRecipe = cachedDistillerRecipe;
                        cachedDistillerRecipe = null;

                        if (completingRecipe != null) {
                            if (completingRecipe.fluidOutput != null) {
                                tanks[1].fill(completingRecipe.fluidOutput.copy(), true);
                            }
                            if (completingRecipe.itemOutput != null && !completingRecipe.itemOutput.isEmpty()
                                    && world.rand.nextFloat() < completingRecipe.chance) {
                                ItemStack output = completingRecipe.itemOutput.copy();
                                ItemStack slot = inventory.get(4);
                                boolean inserted = false;
                                if (slot.isEmpty()) {
                                    inventory.set(4, output);
                                    inserted = true;
                                } else if (ItemHandlerHelper.canItemStacksStack(slot, output)) {
                                    int space = getSlotLimit(4) - slot.getCount();
                                    int add = Math.min(space, output.getCount());
                                    if (add > 0) {
                                        slot.grow(add);
                                        inserted = true;
                                    }
                                }
                                if (inserted) {
                                    doGraphicalUpdates(4);
                                }
                            }
                        }
                    }
                }
            } else {
                processTimeRemaining = 0;
            }
        }
        if (tanks[1].getFluidAmount() > 0) {
            ItemStack filled = ICUtils.fillFluidContainer(tanks[1], inventory.get(2), inventory.get(3), null);
            if (!filled.isEmpty()) {
                if (!inventory.get(3).isEmpty() && OreDictionary.itemMatches(inventory.get(3), filled, true)) inventory.get(3).grow(filled.getCount());
                else if (inventory.get(3).isEmpty()) inventory.set(3, filled.copy());
                inventory.get(2).shrink(1);
                if (inventory.get(2).getCount() <= 0) inventory.set(2, ItemStack.EMPTY);
                update = true;
            }
        }
        ItemStack empty = ICUtils.drainFluidContainer(tanks[0], inventory.get(0), inventory.get(1), null);
        if (!empty.isEmpty()) {
            if (!inventory.get(1).isEmpty() && OreDictionary.itemMatches(inventory.get(1), empty, true)) inventory.get(1).grow(empty.getCount());
            else if (inventory.get(1).isEmpty()) inventory.set(1, empty.copy());
            inventory.get(0).shrink(1);
            if (inventory.get(0).getCount() <= 0) inventory.set(0, ItemStack.EMPTY);
            update = true;
        }
        pumpOutputOut();
        TileEntity outputTile = world.getTileEntity(poiWorldPos("item_output0").offset(poi("item_output0").facing.getOpposite()));
        if (outputTile != null && outputTile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, poi("item_output0").facing.getOpposite())) {
            IItemHandler handler = outputTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, poi("item_output0").facing.getOpposite());
            if (handler != null) {
                for (int slot : ITEM_OUTPUT_SLOTS) {
                    if (inventory.get(slot).isEmpty()) { continue; }
                    ItemStack current = inventory.get(slot).copy();
                    ItemStack remaining = ItemHandlerHelper.insertItemStacked(handler, current, false);
                    inventory.set(slot, remaining);
                    if (remaining.getCount() < current.getCount()) { update = true; }
                }
            }
        }
        boolean didWork = processTimeRemaining > 0 && shouldRun;
        if (didWork) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;
        boolean currentlyRunning = soundGracePeriod > 0;
        if (currentlyRunning != isRunning) {
            isRunning = currentlyRunning;
            update = true;
        }
        if (update) {
            efficientMarkDirty();
            if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
            else { throttledBlockUpdate(); }
        }
        boolean changed = oldEnergy != energyStorage.getEnergyStored() || oldProcess != processTimeRemaining || wasRunning != isRunning;
        if (changed && tickCountdown-- <= 0) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeInt(energyStorage.getEnergyStored() - oldEnergy);
            buf.writeInt(processTimeRemaining);
            buf.writeInt(processTimeMax);
            buf.writeBoolean(isRunning);
            BinaryTileSyncMessage.sendToAllTracking(world, getPos(), buf);
            tickCountdown = 5;
        }
        int comp = comparatorValue();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            notifyComparators();
        }
    }

    private void pumpOutputOut() {
        if (tanks[1].getFluidAmount() == 0) return;
        IFluidHandler output = FluidUtil.getFluidHandler(world, poiFrontPos("fluid_output0"), poi("fluid_output0").facing.getOpposite());
        if (output == null) return;
        FluidStack out = tanks[1].getFluid();
        if (out == null) return;
        int accepted = output.fill(out, false);
        if (accepted > 0) {
            int drained = output.fill(ICUtils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
            tanks[1].drain(drained, true);
        }
    }

    @Override public void TankContentsChanged() {
        if (processTimeRemaining == 0) { cachedDistillerRecipe = null; }
        efficientMarkDirty();
        requestClientSync();
    }

    @Override public int getComparatorInputOverride() { return isComparatorPos() ? comparatorValue() : 0; }

    public int comparatorValue() {
        if (!formed) return 0;
        return 15 * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored();
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityDistillerMaster master() { return this; }

    @Override @Nonnull public int[] getOutputSlots() { return new int[]{4}; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<DistillerRecipe> process) { return true; }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return ITUtils.EMPTY_INT_ARRAY; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return ITUtils.EMPTY_INT_ARRAY; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() { return tanks; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(EnumFacing side, BlockPos position) {
        if (isPoI("fluid_input0", side, position)) return tankView(0, tanks[0]);
        if (isPoI("fluid_output0", side, position)) return tankView(1, tanks[1]);
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        if (iTank != 0) return false;
        if (!isPoI("fluid_input0", side, position)) return false;
        if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
        FluidStack current = tanks[0].getFluid();
        if (current != null) return resource.isFluidEqual(current);
        return true;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        if (iTank != 1) return false;
        if (!isPoI("fluid_output0", side, position)) return false;
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

    @Override public void receiveMessageFromServer(ByteBuf message) {
        int delta = message.readInt();
        energyStorage.modifyEnergyStored(delta);
        processTimeRemaining = message.readInt();
        processTimeMax = message.readInt();
        isRunning = message.readBoolean();
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {}
}
