package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.crafting.MeltingCrucibleRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartMeltingCrucible;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.multiblock.PoICache;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.network.IBinaryMessageReceiver;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityMeltingCrucibleMaster extends TileEntityMeltingCrucibleSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver, IIEInventory, IComparatorOverride {

    private static final int outputTankSize = Multiblocks.meltingCrucible.meltingCrucible_output_tankSize;
    private static final int energyCapacity = Multiblocks.meltingCrucible.meltingCrucible_energy_size;
    private static final int energyMaxInput = Multiblocks.meltingCrucible.meltingCrucible_energy_maxInput;
    private static final double workingHeatLevel = Multiblocks.meltingCrucible.meltingCrucible_heat_workingLevel;
    private static final double heatLossMultiplier = Multiblocks.meltingCrucible.meltingCrucible_heat_loss_multiplier;
    private static final double heatGainBase = Multiblocks.meltingCrucible.meltingCrucible_heat_gain_base;
    private static final int energyPerTickToHeat = Multiblocks.meltingCrucible.meltingCrucible_energy_per_tick_heating;
    private static final int energyPerTickToMaintain = Multiblocks.meltingCrucible.meltingCrucible_energy_per_tick_maintain;
    private static final int progressResolution = 64;
    public static final int slotCount = 3;

    public FluxStorageAdvanced energyStorage = new FluxStorageAdvanced(energyCapacity, energyMaxInput, energyMaxInput);
    public ITFluidTank[] tanks = new ITFluidTank[1];
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    public IItemHandler insertionHandler;

    public int processTimeRemaining = 0;
    public int processTimeMax = 0;
    public double heatLevel = 0;
    public MeltingCrucibleRecipe cachedMeltingRecipe;

    private float soundVolume = 0f;
    private int soundGracePeriod = 0;
    private boolean isRunning = false;
    public boolean redstoneControlInverted = false;
    private int oldComparatorOutput = 0;
    private int tickCountdown = 5;

    private boolean needsPoIInit = true;
    private boolean needsNotify = true;

    protected PoICache energyInputPos0, itemInputPos0, fluidOutputPos0, redstonePos0;
    private BlockPos soundPos0, fluidOutputTEPos0;

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    public TileEntityMeltingCrucibleMaster() {
        tanks[0] = new ITFluidTank(outputTankSize, this);
        insertionHandler = new IEInventoryHandler(1, this, 0, new boolean[]{true}, new boolean[]{false});
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        energyStorage.readFromNBT(nbt.getCompoundTag("energy"));
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        if (nbt.hasKey("inventory")) inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
        processTimeRemaining = nbt.getInteger("processTimeRemaining");
        processTimeMax = nbt.getInteger("processTimeMax");
        heatLevel = nbt.getDouble("heatLevel");
        redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
        isRunning = nbt.getBoolean("isRunning");
        soundGracePeriod = nbt.getInteger("soundGracePeriod");
        if (!descPacket) {
            if (nbt.hasKey("cachedRecipe")) cachedMeltingRecipe = MeltingCrucibleRecipe.loadFromNBT(nbt.getCompoundTag("cachedRecipe"));
            if (processTimeRemaining > 0 && cachedMeltingRecipe == null) processTimeRemaining = 0;
        }
        if (formed) {
            needsPoIInit = true;
            needsNotify = true;
        }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("energy", energyStorage.writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("inventory", Utils.writeInventory(inventory));
        nbt.setInteger("processTimeRemaining", processTimeRemaining);
        nbt.setInteger("processTimeMax", processTimeMax);
        nbt.setDouble("heatLevel", heatLevel);
        nbt.setBoolean("redstoneControlInverted", redstoneControlInverted);
        nbt.setBoolean("isRunning", isRunning);
        nbt.setInteger("soundGracePeriod", soundGracePeriod);
        if (!descPacket && cachedMeltingRecipe != null) nbt.setTag("cachedRecipe", cachedMeltingRecipe.writeToNBT(new NBTTagCompound()));
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (soundPos0 == null) InitializePoIs();
        if (soundPos0 == null) return;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double dSq = player.getDistanceSq(soundPos0.getX() + 0.5, soundPos0.getY() + 0.5, soundPos0.getZ() + 0.5);
        if (dSq > 4096) {
            ITSoundHandler.StopSound(soundPos0);
            soundVolume = 0f;
            return;
        }
        float target = isRunning ? 1f : 0f;
        if (soundVolume < target) { soundVolume = Math.min(soundVolume + 0.02f, target); }
        else if (soundVolume > target) { soundVolume = Math.max(soundVolume - 0.02f, target); }
        if (soundVolume <= 0f) ITSoundHandler.StopSound(soundPos0);
        else {
            float distance = (float)Math.sqrt(dSq);
            float attenuation = Math.max(distance / 16f, 1f);
            ITSounds.meltingCrucible.PlayRepeating(soundPos0, soundVolume / attenuation, 1f);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        if (soundPos0 != null) ITSoundHandler.StopSound(soundPos0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        if (soundPos0 == null) InitializePoIs();
        if (!world.isRemote && soundPos0 != null) {
            NetworkRegistry.TargetPoint tp = new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0);
            ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), tp);
        }
        if (!world.isRemote) {
            for (ItemStack stack : inventory) if (!stack.isEmpty()) Utils.dropStackAtPos(world, getPos(), stack);
            inventory.clear();
        }
        super.disassemble();
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {}

    @Override public void update() {
        super.update();
        if (!formed) {
            if (world.isRemote && soundPos0 != null) ITSoundHandler.StopSound(soundPos0);
            return;
        }
        if (needsPoIInit || energyInputPos0 == null) {
            InitializePoIs();
            needsPoIInit = false;
        }
        if (needsNotify) {
            notifyIONeighbors();
            needsNotify = false;
        }
        if (world.isRemote) {
            handleSounds();
            return;
        }
        int oldEnergy = energyStorage.getEnergyStored();
        double oldHeat = heatLevel;
        int oldProcess = processTimeRemaining;
        boolean wasRunning = isRunning;

        boolean update = false;
        boolean shouldRun = !isRSDisabled();
        int energyThisTick = heatLevel >= workingHeatLevel ? energyPerTickToMaintain : energyPerTickToHeat;
        boolean heating = shouldRun && energyStorage.extractEnergy(energyThisTick, true) >= energyThisTick;
        if (heating) energyStorage.extractEnergy(energyThisTick, false);
        update |= heatLogic(heating, heating ? energyThisTick : 0);
        update |= recipeLogic(shouldRun);
        update |= outputTankLogic();
        boolean active = processTimeRemaining > 0 && shouldRun;
        if (active) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;
        isRunning = soundGracePeriod > 0;
        if (isRunning != wasRunning) update = true;

        boolean changed = oldEnergy != energyStorage.getEnergyStored() || oldHeat != heatLevel || oldProcess != processTimeRemaining || wasRunning != isRunning;
        if (changed && tickCountdown-- <= 0) {
            tickCountdown = 5;
            ByteBuf buf = Unpooled.buffer();
            buf.writeInt(energyStorage.getEnergyStored());
            buf.writeDouble(heatLevel);
            buf.writeInt(processTimeRemaining);
            buf.writeInt(processTimeMax);
            buf.writeBoolean(isRunning);
            BinaryMessageTileSync.sendToAllTracking(world, getPos(), buf);
            if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
            else { throttledBlockUpdate(); }
        }
        int comparator = getComparatorInputOverride();
        if (comparator != oldComparatorOutput) {
            oldComparatorOutput = comparator;
            if (redstonePos0 != null) world.updateComparatorOutputLevel(getBlockPosForPos(redstonePos0.position), getBlockType());
            update = true;
        }
        if (update) {
            efficientMarkDirty();
            if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
            else { throttledBlockUpdate(); }
        }
    }

    private boolean heatLogic(boolean heating, int energyUsed) {
        boolean changed = false;
        double prev = heatLevel;
        heatLevel -= getCooldownAmount();
        heatLevel = Math.max(heatLevel, 0);
        if (heating) heatLevel += getTemperatureIncrease(energyUsed);
        heatLevel = Math.min(heatLevel, workingHeatLevel);
        if (prev != heatLevel) changed = true;
        return changed;
    }

    private double getTemperatureIncrease(int energyUsed) { return (energyUsed / (double)energyPerTickToHeat) * heatGainBase; }

    private double getCooldownAmount() {
        double heatLost = world.getBiome(getPos()).getTemperature(getPos());
        if (heatLost <= 0) heatLost = 0.1;
        return (1 / heatLost) * heatLossMultiplier;
    }

    private boolean recipeLogic(boolean shouldRun) {
        boolean update = false;
        if (processTimeRemaining == 0 && shouldRun && heatLevel >= workingHeatLevel) {
            ItemStack input = inventory.get(0);
            if (!input.isEmpty()) {
                MeltingCrucibleRecipe recipe = MeltingCrucibleRecipe.findRecipe(input);
                if (recipe != null && input.getCount() >= recipe.itemInput.inputSize && tanks[0].fill(recipe.fluidOutput, false) == recipe.fluidOutput.amount) {
                    cachedMeltingRecipe = recipe;
                    input.shrink(recipe.itemInput.inputSize);
                    if (input.getCount() <= 0) inventory.set(0, ItemStack.EMPTY);
                    doGraphicalUpdates(0);
                    processTimeRemaining = recipe.getTotalProcessTime() * progressResolution;
                    processTimeMax = processTimeRemaining;
                    efficientMarkDirty();
                    update = true;
                }
            }
        }
        if (processTimeRemaining > 0 && shouldRun && heatLevel >= workingHeatLevel) {
            int prev = processTimeRemaining;
            processTimeRemaining -= progressResolution;
            if (prev != processTimeRemaining) update = true;
            if (processTimeRemaining <= 0) {
                processTimeRemaining = 0;
                if (cachedMeltingRecipe != null) {
                    tanks[0].fill(cachedMeltingRecipe.fluidOutput.copy(), true);
                    cachedMeltingRecipe = null;
                    efficientMarkDirty();
                }
            }
        }
        return update;
    }

    private boolean outputTankLogic() {
        boolean update = false;
        ItemStack filled = Utils.fillFluidContainer(tanks[0], inventory.get(1), inventory.get(2), null);
        if (!filled.isEmpty()) {
            if (!inventory.get(2).isEmpty() && OreDictionary.itemMatches(inventory.get(2), filled, true)) inventory.get(2).grow(filled.getCount());
            else if (inventory.get(2).isEmpty()) inventory.set(2, filled);
            inventory.get(1).shrink(1);
            if (inventory.get(1).getCount() <= 0) inventory.set(1, ItemStack.EMPTY);
            doGraphicalUpdates(1);
            doGraphicalUpdates(2);
            efficientMarkDirty();
            update = true;
        }
        ItemStack empty = Utils.drainFluidContainer(tanks[0], inventory.get(1), inventory.get(2), null);
        if (!empty.isEmpty()) {
            if (!inventory.get(2).isEmpty() && OreDictionary.itemMatches(inventory.get(2), empty, true)) inventory.get(2).grow(empty.getCount());
            else if (inventory.get(2).isEmpty()) inventory.set(2, empty);
            inventory.get(1).shrink(1);
            if (inventory.get(1).getCount() <= 0) inventory.set(1, ItemStack.EMPTY);
            doGraphicalUpdates(1);
            doGraphicalUpdates(2);
            efficientMarkDirty();
            update = true;
        }
        if (pumpOutputOut()) update = true;
        return update;
    }

    private boolean pumpOutputOut() {
        if (tanks[0].getFluidAmount() == 0) return false;
        if (fluidOutputPos0 == null) InitializePoIs();
        IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputTEPos0, fluidOutputPos0.facing.getOpposite());
        if (output == null) return false;
        FluidStack out = tanks[0].getFluid();
        if (out == null) return false;
        int accepted = output.fill(out.copy(), false);
        if (accepted <= 0) return false;
        int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        if (drained > 0) tanks[0].drain(drained, true);
        return drained > 0;
    }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartMeltingCrucible.instance.pointsOfInterest) {
            PoICache cache = new PoICache(facing, poi, mirrored);
            switch (poi.name) {
                case "energy_input0":
                    energyInputPos0 = cache;
                    break;
                case "item_input0":
                    itemInputPos0 = cache;
                    break;
                case "fluid_output0":
                    fluidOutputPos0 = cache;
                    fluidOutputTEPos0 = getBlockPosForPos(cache.position).offset(cache.facing);
                    break;
                case "redstone0":
                    redstonePos0 = cache;
                    break;
                case "sound0":
                    soundPos0 = getBlockPosForPos(poi.position);
                    break;
            }
        }
    }

    private void notifyIONeighbors() {
        if (energyInputPos0 != null) notifyPort(energyInputPos0);
        if (itemInputPos0 != null) notifyPort(itemInputPos0);
        if (fluidOutputPos0 != null) notifyPort(fluidOutputPos0);
        if (redstonePos0 != null) world.updateComparatorOutputLevel(getBlockPosForPos(redstonePos0.position), getBlockType());
    }

    private void notifyPort(PoICache cache) { world.notifyNeighborsOfStateChange(getBlockPosForPos(cache.position), getBlockType(), true); }

    @Override public void TankContentsChanged() {
        efficientMarkDirty();
        markContainingBlockForUpdate(null);
    }

    @Override public boolean isRSDisabled() {
        int[] rsPos = getRedstonePos();
        if (rsPos.length == 0) return false;
        int power = world.getRedstonePowerFromNeighbors(getBlockPosForPos(rsPos[0]));
        return redstoneControlInverted != (power > 0);
    }

    @Override public int getComparatorInputOverride() { return (int)(15 * heatLevel / workingHeatLevel); }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityMeltingCrucibleMaster master() { return this; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (fluidOutputPos0 == null) InitializePoIs();
        if (fluidOutputPos0.isPoI(side, position)) return tanks;
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (fluidOutputPos0 == null) InitializePoIs();
        return iTank == 0 && fluidOutputPos0.isPoI(side, position);
    }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed || redstonePos0 == null) return new int[0];
        return new int[]{redstonePos0.position};
    }

    @Override @Nonnull public int[] getEnergyPos() {
        if (!formed || energyInputPos0 == null) return new int[0];
        return new int[]{energyInputPos0.position};
    }

    @Override @Nonnull public int[] getOutputTanks() { return new int[]{0}; }

    @Override @Nonnull public FluxStorageAdvanced getFluxStorage() { return energyStorage; }

    @Override @Nonnull public NonNullList<ItemStack> getInventory() { return inventory; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) {
        efficientMarkDirty();
        markContainingBlockForUpdate(null);
    }

    @Override @Nonnull public NonNullList<ItemStack> getDroppedItems() { return inventory; }

    @Override public int getComparatedSize() { return slotCount; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() { return tanks; }

    @Override public void receiveMessageFromServer(ByteBuf buf) {
        int energy = buf.readInt();
        energyStorage.modifyEnergyStored(energy - energyStorage.getEnergyStored());
        heatLevel = buf.readDouble();
        processTimeRemaining = buf.readInt();
        processTimeMax = buf.readInt();
        isRunning = buf.readBoolean();
    }
}
