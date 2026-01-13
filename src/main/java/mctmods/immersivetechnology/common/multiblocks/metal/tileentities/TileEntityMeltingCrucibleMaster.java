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
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
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

import java.util.Optional;

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

    public FluxStorageAdvanced energyStorage = new FluxStorageAdvanced(energyCapacity, energyMaxInput, energyMaxInput);
    public FluidTank[] tanks = new FluidTank[] { new ITFluidTank(outputTankSize, this) };

    public static int slotCount = 3;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);

    public int processTimeRemaining = 0;
    public double heatLevel = 0;

    public MeltingCrucibleRecipe cachedMeltingRecipe;
    private float soundVolume = 0f;
    private int soundGracePeriod = 60;
    private boolean isRunning = false;
    private double distanceSqToTE = Double.MAX_VALUE;
    private int playerDimension = Integer.MIN_VALUE;
    public boolean redstoneControlInverted = false;
    public Optional<Boolean> computerOn = Optional.empty();
    private int oldComparatorOutput = 0;

    private PoICache energyInput0, fluidOutput0, itemInput0, redstone0;
    private BlockPos sound0, fluidOutputPos0;

    private boolean needsPoIInit = false;

    public IItemHandler insertionHandler = new IEInventoryHandler(1, this, 0, new boolean[]{true}, new boolean[]{false});

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
        energyStorage.readFromNBT(nbt.getCompoundTag("energy"));
        processTimeRemaining = nbt.getInteger("processTimeRemaining");
        heatLevel = nbt.getDouble("heatLevel");
        if (!descPacket && formed) needsPoIInit = true;
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("inventory", Utils.writeInventory(inventory));
        nbt.setTag("energy", energyStorage.writeToNBT(new NBTTagCompound()));
        nbt.setInteger("processTimeRemaining", processTimeRemaining);
        nbt.setDouble("heatLevel", heatLevel);
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (sound0 == null) InitializePoIs();
        if (distanceSqToTE > 4096) { ITSoundHandler.StopSound(sound0); soundVolume = 0f; return; }
        float targetSoundLevel = isRunning ? 1f : 0f;
        if (soundVolume < targetSoundLevel) {
            soundVolume = Math.min(soundVolume + 0.02f, targetSoundLevel);
            soundGracePeriod = 60;
        } else if (soundVolume > targetSoundLevel) {
            if (soundGracePeriod > 0) soundGracePeriod--;
            else soundVolume = Math.max(soundVolume - 0.02f, targetSoundLevel);
        }
        if (soundVolume <= 0f) ITSoundHandler.StopSound(sound0);
        else {
            double distance = Math.sqrt(distanceSqToTE);
            float attenuation = Math.max((float)distance / 16f, 1f);
            ITSounds.heatExchanger.PlayRepeating(sound0, soundVolume / attenuation, 1f);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() { if (sound0 != null) ITSoundHandler.StopSound(sound0); super.onChunkUnload(); }

    @SideOnly(Side.CLIENT)
    private void clientUpdate() {
        if (sound0 == null) InitializePoIs();
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double currentDistance = player.getDistanceSq(sound0.getX() + 0.5, sound0.getY() + 0.5, sound0.getZ() + 0.5);
        if (world.provider.getDimension() == player.dimension && currentDistance < 400 && (distanceSqToTE > 400 || playerDimension != player.dimension)) requestUpdate();
        distanceSqToTE = currentDistance;
        playerDimension = player.dimension;
        handleSounds();
    }

    @Override public void disassemble() {
        if (sound0 == null) InitializePoIs();
        if (sound0 != null) {
            ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(sound0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), sound0.getX(), sound0.getY(), sound0.getZ(), 0));
        }
        if (!world.isRemote) {
            for (ItemStack stack : inventory) if (!stack.isEmpty()) Utils.dropStackAtPos(world, getPos(), stack.copy());
            inventory.clear();
        }
        super.disassemble();
    }

    private void requestUpdate() { ImmersiveTechnology.packetHandler.sendToServer(new BinaryMessageTileSync(getPos(), Unpooled.buffer())); }

    public void notifyNearbyClients() { ImmersiveTechnology.packetHandler.sendToAllAround(new BinaryMessageTileSync(getPos(), Unpooled.copyBoolean(isRunning)), new NetworkRegistry.TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 40)); }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) { ImmersiveTechnology.packetHandler.sendTo(new BinaryMessageTileSync(getPos(), Unpooled.copyBoolean(isRunning)), player); }

    @Override public void receiveMessageFromServer(ByteBuf message) { isRunning = message.readBoolean(); }

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    @Override public void update() {
        super.update();
        if (!formed) return;
        if (needsPoIInit || energyInput0 == null) { InitializePoIs(); needsPoIInit = false; }
        if (world.isRemote) { clientUpdate(); return; }

        boolean update = false;
        boolean shouldRun = !isRSDisabled();

        int energyThisTick = (heatLevel >= workingHeatLevel && processTimeRemaining <= 0) ? energyPerTickToMaintain : energyPerTickToHeat;
        boolean heating = shouldRun && energyStorage.extractEnergy(energyThisTick, true) >= energyThisTick;
        if (heating) energyStorage.extractEnergy(energyThisTick, false);

        update |= heatLogic(heating, heating ? energyThisTick : 0);
        update |= recipeLogic(shouldRun);
        update |= outputTankLogic();

        boolean active = processTimeRemaining > 0 && shouldRun;
        if (active) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;

        boolean wasRunning = isRunning;
        isRunning = soundGracePeriod > 0;
        if (isRunning != wasRunning) { notifyNearbyClients(); update = true; }

        int comp = getComparatorInputOverride();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            world.notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
            update = true;
        }

        if (update) { efficientMarkDirty(); markContainingBlockForUpdate(null); }
    }

    private boolean pumpOutputOut() {
        if (tanks[0].getFluidAmount() == 0) return false;
        if (fluidOutput0 == null) InitializePoIs();
        IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputPos0, fluidOutput0.facing.getOpposite());
        if (output == null) return false;
        FluidStack out = tanks[0].getFluid();
        if (out == null || out.amount <= 0) return false;
        int accepted = output.fill(out.copy(), false);
        if (accepted == 0) return false;
        int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        tanks[0].drain(drained, true);
        return drained > 0;
    }

    private boolean heatLogic(boolean heating, int energyUsed) {
        boolean changed = false;
        double prev = heatLevel;
        heatLevel -= getCooldownAmount();
        heatLevel = Math.max(heatLevel, 0D);
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
            ItemStack inputStack = inventory.get(0);
            if (!inputStack.isEmpty()) {
                MeltingCrucibleRecipe recipe = MeltingCrucibleRecipe.findRecipe(inputStack);
                if (recipe != null && inputStack.getCount() >= recipe.itemInput.inputSize && tanks[0].fill(recipe.fluidOutput, false) == recipe.fluidOutput.amount) {
                    cachedMeltingRecipe = recipe;
                    inputStack.shrink(recipe.itemInput.inputSize);
                    if (inputStack.getCount() <= 0) inventory.set(0, ItemStack.EMPTY);
                    processTimeRemaining = recipe.getTotalProcessTime() * progressResolution;
                    update = true;
                }
            }
        }
        if (processTimeRemaining > 0 && shouldRun) {
            if (cachedMeltingRecipe != null) {
                int prev = processTimeRemaining;
                if (heatLevel >= workingHeatLevel) processTimeRemaining -= progressResolution;
                if (prev != processTimeRemaining) update = true;
                if (processTimeRemaining <= 0) {
                    processTimeRemaining = 0;
                    tanks[0].fill(cachedMeltingRecipe.fluidOutput, true);
                    cachedMeltingRecipe = null;
                }
            } else processTimeRemaining = 0;
        }
        return update;
    }

    private boolean outputTankLogic() {
        boolean update = false;
        ItemStack filled = Utils.fillFluidContainer(tanks[0], inventory.get(1), inventory.get(2), null);
        if (!filled.isEmpty()) {
            if (!inventory.get(2).isEmpty() && OreDictionary.itemMatches(inventory.get(2), filled, true)) inventory.get(2).grow(filled.getCount());
            else if (inventory.get(2).isEmpty()) inventory.set(2, filled.copy());
            inventory.get(1).shrink(1);
            if (inventory.get(1).getCount() <= 0) inventory.set(1, ItemStack.EMPTY);
            update = true;
        }
        ItemStack empty = Utils.drainFluidContainer(tanks[0], inventory.get(1), inventory.get(2), null);
        if (!empty.isEmpty()) {
            if (!inventory.get(2).isEmpty() && OreDictionary.itemMatches(inventory.get(2), empty, true)) inventory.get(2).grow(empty.getCount());
            else if (inventory.get(2).isEmpty()) inventory.set(2, empty.copy());
            inventory.get(1).shrink(1);
            if (inventory.get(1).getCount() <= 0) inventory.set(1, ItemStack.EMPTY);
            update = true;
        }
        if (pumpOutputOut()) update = true;
        return update;
    }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartMeltingCrucible.instance.pointsOfInterest) {
            switch (poi.name) {
                case "redstone0":
                    redstone0 = new PoICache(facing, poi, mirrored);
                    break;
                case "energy_input0":
                    energyInput0 = new PoICache(facing, poi, mirrored);
                    break;
                case "item_input0":
                    itemInput0 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_output0":
                    fluidOutput0 = new PoICache(facing, poi, mirrored);
                    fluidOutputPos0 = getBlockPosForPos(fluidOutput0.position).offset(fluidOutput0.facing);
                    break;
                case "sound0":
                    sound0 = getBlockPosForPos(poi.position);
                    break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(itemInput0.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput0.position));
        notifyNeighbor(getBlockPosForPos(redstone0.position));
        notifyNeighbor(getBlockPosForPos(energyInput0.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override public void TankContentsChanged() { markContainingBlockForUpdate(null); }

    @Override public boolean isRSDisabled() {
        if (computerOn.isPresent()) return !computerOn.get();
        int[] rsPositions = getRedstonePos();
        if (rsPositions.length < 1) return false;
        for (int rsPos : rsPositions) {
            TileEntity tile = world.getTileEntity(getBlockPosForPos(rsPos));
            if (tile != null) {
                int power = world.getRedstonePowerFromNeighbors(tile.getPos());
                return redstoneControlInverted != (power > 0);
            }
        }
        return false;
    }

    @Override public int getComparatorInputOverride() { return (int)(15 * heatLevel / workingHeatLevel); }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityMeltingCrucibleMaster master() { return this; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (fluidOutput0 == null) InitializePoIs();
        if (side == null || fluidOutput0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) { return false; }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (fluidOutput0 == null) InitializePoIs();
        return iTank == 0 && fluidOutput0.isPoI(side, position) && tanks[0].getFluidAmount() > 0;
    }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstone0 == null) InitializePoIs();
        return new int[] {redstone0.position};
    }

    @Override @Nonnull public int[] getEnergyPos() {
        if (!formed) return new int[0];
        if (energyInput0 == null) InitializePoIs();
        return new int[] {energyInput0.position};
    }

    @Override @Nonnull public FluxStorageAdvanced getFluxStorage() { return energyStorage; }

    public boolean isItemInputPosition(@Nullable EnumFacing facing, int position) {
        if (!formed) return false;
        if (itemInput0 == null) InitializePoIs();
        return facing != null && itemInput0.isPoI(facing, position);
    }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, int position) {
        if (!formed) return false;
        if (energyInput0 == null) InitializePoIs();
        return facing != null && energyInput0.isPoI(facing, position);
    }

    @Override @Nonnull public NonNullList<ItemStack> getInventory() { return inventory; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) { markDirty(); markContainingBlockForUpdate(null); }

    @Override @Nonnull public NonNullList<ItemStack> getDroppedItems() { return getInventory(); }

    @Override public int getComparatedSize() { return slotCount; }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }
}
