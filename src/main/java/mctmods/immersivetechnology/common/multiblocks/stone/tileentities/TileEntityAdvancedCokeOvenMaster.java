package mctmods.immersivetechnology.common.multiblocks.stone.tileentities;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityAdvancedCokeOvenBaseheater;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentitiesmultiblockpart.TileEntityITMultiblockPartAdvancedCokeOven;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.multiblock.PoICache;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
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

public class TileEntityAdvancedCokeOvenMaster extends TileEntityAdvancedCokeOvenSlave implements ITFluidTank.TankListener, IIEInventory, IComparatorOverride {

    private static final int tankSize = Multiblocks.advancedCokeOven.advancedCokeOven_tankSize;
    public static float baseSpeed = Multiblocks.advancedCokeOven.advancedCokeOven_speed_base;
    public static float baseheaterAdd = Multiblocks.advancedCokeOven.advancedCokeOven_baseheater_speed_increase;
    public static float baseheaterMult = Multiblocks.advancedCokeOven.advancedCokeOven_baseheater_speed_multiplier;
    public static int slotCount = 4;

    public ITFluidTank tank = new ITFluidTank(tankSize, this);
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    public int processTimeRemaining = 0;
    public int processTimeMax = 0;
    public boolean active = false;

    private float soundVolume = 0;
    private CokeOvenRecipe cachedRecipe;
    private int soundGracePeriod = 0;
    private boolean isRunning;

    PoICache itemInput0;
    PoICache itemOutput0;
    PoICache fluidOutput0;
    PoICache baseheater0;
    PoICache baseheater1;
    PoICache redstone0;

    BlockPos itemOutputPos;
    BlockPos fluidOutputPos;

    final IItemHandler inputHandler = new IEInventoryHandler(1, this, 0, new boolean[]{true}, new boolean[]{false});
    final IItemHandler outputHandler = new IEInventoryHandler(1, this, 1, new boolean[]{false}, new boolean[]{true});

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        processTimeRemaining = nbt.getInteger("processTimeRemaining");
        processTimeMax = nbt.getInteger("processTimeMax");
        active = nbt.getBoolean("active");
        tank.readFromNBT(nbt.getCompoundTag("tank"));
        inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setInteger("processTimeRemaining", processTimeRemaining);
        nbt.setInteger("processTimeMax", processTimeMax);
        nbt.setBoolean("active", active);
        nbt.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
        nbt.setTag("inventory", Utils.writeInventory(inventory));
    }

    @SideOnly(Side.CLIENT)
    private void handleSounds() {
        if (isRunning) {
            if (soundVolume < 1) soundVolume += 0.01f;
        } else if (soundVolume > 0) {
            soundVolume -= 0.01f;
        }
        BlockPos center = getPos();
        if (soundVolume <= 0) {
            ITSoundHandler.StopSound(center);
            soundVolume = 0;
        } else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(center.getX() + .5, center.getY() + .5, center.getZ() + .5) / 8, 1);
            ITSounds.advancedCokeOven.PlayRepeating(center, soundVolume / attenuation, 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        ITSoundHandler.StopSound(getPos());
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        if (!world.isRemote) {
            for (ItemStack stack : inventory) {
                if (!stack.isEmpty()) {
                    world.spawnEntity(new EntityItem(world, getPos().getX() + .5, getPos().getY() + .5, getPos().getZ() + .5, stack.copy()));
                }
            }
            inventory.clear();
        }
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(center), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
        super.disassemble();
    }

    private void notifyNearbyClients() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("active", active);
        tag.setBoolean("isRunning", isRunning);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageTileSync(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
    }

    private void notifyProcessUpdate() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("processTimeRemaining", processTimeRemaining);
        tag.setInteger("processTimeMax", processTimeMax);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageTileSync(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
    }

    @Override public void receiveMessageFromServer(NBTTagCompound message) {
        if (message.hasKey("active")) active = message.getBoolean("active");
        if (message.hasKey("isRunning")) isRunning = message.getBoolean("isRunning");
        if (message.hasKey("processTimeRemaining")) {
            processTimeRemaining = message.getInteger("processTimeRemaining");
            processTimeMax = message.getInteger("processTimeMax");
        }
    }

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    @Override public void update() {
        if (!formed) return;
        if (itemInput0 == null) InitializePoIs();
        if (world.isRemote) {
            handleSounds();
            return;
        }
        boolean update = false;
        if (!inventory.get(0).isEmpty()) {
            if (cachedRecipe == null) {
                cachedRecipe = getRecipe();
                if (cachedRecipe == null) {
                    if (active) {
                        active = false;
                        processTimeRemaining = 0;
                        processTimeMax = 0;
                        update = true;
                        notifyNearbyClients();
                        setHeatersActive();
                    }
                } else if (!active) {
                    processTimeRemaining = processTimeMax = cachedRecipe.time;
                    active = true;
                    update = true;
                    notifyNearbyClients();
                    notifyProcessUpdate();
                }
            }
            if (active && processTimeRemaining > 0) {
                processTimeRemaining -= (int)getProcessSpeed();
                if (processTimeRemaining < 0) processTimeRemaining = 0;
                update = true;
                if (world.getTotalWorldTime() % 8 == 0) notifyProcessUpdate();
            }
            if (cachedRecipe != null && processTimeRemaining <= 0) {
                if (tank.getFluidAmount() + cachedRecipe.creosoteOutput <= tank.getCapacity() && inventory.get(1).getCount() + cachedRecipe.output.getCount() <= getSlotLimit(1)) {
                    Utils.modifyInvStackSize(inventory, 0, -1);
                    doGraphicalUpdates(0);
                    if (inventory.get(1).isEmpty()) inventory.set(1, cachedRecipe.output.copy());
                    else inventory.get(1).grow(cachedRecipe.output.getCount());
                    doGraphicalUpdates(1);
                    tank.fill(new FluidStack(IEContent.fluidCreosote, cachedRecipe.creosoteOutput), true);
                    cachedRecipe = getRecipe();
                    if (cachedRecipe != null) {
                        processTimeRemaining = processTimeMax = cachedRecipe.time;
                        notifyProcessUpdate();
                    } else {
                        active = false;
                        processTimeRemaining = 0;
                        processTimeMax = 0;
                    }
                    update = true;
                    notifyNearbyClients();
                    if (!active) setHeatersActive();
                } else if (active) {
                    active = false;
                    update = true;
                    notifyNearbyClients();
                    setHeatersActive();
                }
            }
        } else if (active) {
            active = false;
            update = true;
            processTimeRemaining = 0;
            processTimeMax = 0;
            cachedRecipe = null;
            notifyNearbyClients();
            setHeatersActive();
        }
        if (tank.getFluidAmount() > 0 && (inventory.get(3).isEmpty() || ItemHandlerHelper.canItemStacksStack(inventory.get(3), Utils.fillFluidContainer(tank, inventory.get(2), inventory.get(3), null)))) {
            ItemStack filled = Utils.fillFluidContainer(tank, inventory.get(2), inventory.get(3), null);
            if (!filled.isEmpty()) {
                if (inventory.get(2).getCount() == 1 && !Utils.isFluidContainerFull(filled)) {
                    inventory.set(2, filled);
                    doGraphicalUpdates(2);
                } else {
                    if (inventory.get(3).isEmpty()) inventory.set(3, filled);
                    else inventory.get(3).grow(filled.getCount());
                    Utils.modifyInvStackSize(inventory, 2, -filled.getCount());
                    doGraphicalUpdates(2);
                    doGraphicalUpdates(3);
                }
                update = true;
            }
        }
        if (!inventory.get(1).isEmpty()) {
            ItemStack stack = inventory.get(1).copy();
            int prevCount = stack.getCount();
            stack = ItemHandlerHelper.insertItemStacked(outputHandler, stack, false);
            inventory.set(1, stack);
            if (stack.getCount() < prevCount) doGraphicalUpdates(1);
        }
        if (pumpOutputOut()) update = true;
        boolean wasRunning = isRunning;
        if (active) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;
        isRunning = soundGracePeriod > 0;
        if (isRunning != wasRunning) notifyNearbyClients();
        if (update || isRunning != wasRunning) {
            efficientMarkDirty();
            markContainingBlockForUpdate(null);
        }
    }

    private boolean pumpOutputOut() {
        if (tank.getFluidAmount() == 0) return false;
        IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputPos, fluidOutput0.facing.getOpposite());
        if (output == null) return false;
        FluidStack available = tank.getFluid();
        if (available == null) return false;
        int accepted = output.fill(available, false);
        if (accepted <= 0) return false;
        FluidStack toPush = Utils.copyFluidStackWithAmount(available, accepted, false);
        int filled = output.fill(toPush, true);
        if (filled > 0) tank.drain(filled, true);
        return filled > 0;
    }

    private CokeOvenRecipe getRecipe() {
        CokeOvenRecipe recipe = CokeOvenRecipe.findRecipe(inventory.get(0));
        if (recipe == null) return null;
        if (inventory.get(1).isEmpty() || (OreDictionary.itemMatches(inventory.get(1), recipe.output, false) && inventory.get(1).getCount() + recipe.output.getCount() <= getSlotLimit(1)))
            if (tank.getFluidAmount() + recipe.creosoteOutput <= tank.getCapacity()) return recipe;
        return null;
    }

    private float getProcessSpeed() {
        int activeBaseheaters = 0;
        PoICache[] heaters = {baseheater0, baseheater1};
        for (PoICache poi : heaters) {
            if (poi == null) continue;
            BlockPos pos = getBlockPosForPos(poi.position).offset(poi.facing);
            TileEntity tile = Utils.getExistingTileEntity(world, pos);
            if (!(tile instanceof TileEntityAdvancedCokeOvenBaseheater)) continue;
            TileEntityAdvancedCokeOvenBaseheater heater = (TileEntityAdvancedCokeOvenBaseheater)tile;
            if (heater.facing != poi.facing.getOpposite() || !heater.doSpeedup()) continue;
            activeBaseheaters++;
        }
        return (baseSpeed + activeBaseheaters * baseheaterAdd) * (1 + activeBaseheaters * (baseheaterMult - 1));
    }

    private void setHeatersActive() {
        PoICache[] heaters = {baseheater0, baseheater1};
        for (PoICache poi : heaters) {
            if (poi == null) continue;
            BlockPos pos = getBlockPosForPos(poi.position).offset(poi.facing);
            TileEntity tile = Utils.getExistingTileEntity(world, pos);
            if (tile instanceof TileEntityAdvancedCokeOvenBaseheater) {
                TileEntityAdvancedCokeOvenBaseheater heater = (TileEntityAdvancedCokeOvenBaseheater)tile;
                if (heater.active) {
                    heater.active = false;
                    heater.markContainingBlockForUpdate(null);
                    heater.updateDummies();
                }
            }
        }
    }

    private void notifyNeighbor(BlockPos pos) {
        if (pos != null) world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false);
    }

    void InitializePoIs() {
        itemInput0 = itemOutput0 = fluidOutput0 = baseheater0 = baseheater1 = redstone0 = null;
        itemOutputPos = fluidOutputPos = null;
        for (PoIJSONSchema poi : TileEntityITMultiblockPartAdvancedCokeOven.instance.pointsOfInterest) {
            PoICache cache = new PoICache(facing, poi, mirrored);
            switch (poi.name) {
                case "item_input0":
                    itemInput0 = cache;
                    break;
                case "item_output0":
                    itemOutput0 = cache;
                    itemOutputPos = getBlockPosForPos(cache.position).offset(cache.facing);
                    break;
                case "fluid_output0":
                    fluidOutput0 = cache;
                    fluidOutputPos = getBlockPosForPos(cache.position).offset(cache.facing);
                    break;
                case "baseheater0":
                    baseheater0 = cache;
                    break;
                case "baseheater1":
                    baseheater1 = cache;
                    break;
                case "redstone0":
                    redstone0 = cache;
                    break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        if (itemInput0 != null) notifyNeighbor(getBlockPosForPos(itemInput0.position));
        if (itemOutput0 != null) notifyNeighbor(getBlockPosForPos(itemOutput0.position));
        if (fluidOutput0 != null) notifyNeighbor(getBlockPosForPos(fluidOutput0.position));
        if (redstone0 != null) world.updateComparatorOutputLevel(getBlockPosForPos(redstone0.position), getBlockType());
    }

    @Override public void TankContentsChanged() {
        efficientMarkDirty();
        markContainingBlockForUpdate(null);
    }

    @Override public int getComparatorInputOverride() {
        if (!formed || processTimeMax <= 0) return 0;
        return 15 * (processTimeMax - processTimeRemaining) / processTimeMax;
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityAdvancedCokeOvenMaster master() { return this; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (fluidOutput0 == null) InitializePoIs();
        if (fluidOutput0.isPoI(side, position)) return new IFluidTank[]{tank};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (!formed || fluidOutput0 == null) InitializePoIs();
        return fluidOutput0.isPoI(side, position) && iTank == 0;
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (!formed) return false;
        if (itemInput0 == null) InitializePoIs();
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            return itemInput0.isPoI(facing, this.pos) || itemOutput0.isPoI(facing, this.pos);
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            return fluidOutput0.isPoI(facing, this.pos);
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (!formed) return super.getCapability(capability, facing);
        if (itemInput0 == null) InitializePoIs();
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            if (itemInput0.isPoI(facing, this.pos)) return (T)inputHandler;
            if (itemOutput0.isPoI(facing, this.pos)) return (T)outputHandler;
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null && fluidOutput0.isPoI(facing, this.pos)) {
            return (T)new AdvancedCokeOvenFluidHandler(this);
        }
        return super.getCapability(capability, facing);
    }

    @Override public NonNullList<ItemStack> getInventory() { return inventory; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) {
        efficientMarkDirty();
        markContainingBlockForUpdate(null);
    }

    @Override public boolean getIsActive() { return active; }

    @Override public TileEntity getGuiMaster() { return this; }

    public static class AdvancedCokeOvenFluidHandler implements IFluidHandler {
        private final ITFluidTank tank;
        private final TileEntityAdvancedCokeOvenMaster master;

        public AdvancedCokeOvenFluidHandler(TileEntityAdvancedCokeOvenMaster master) {
            this.master = master;
            this.tank = master.tank;
        }

        @Override public IFluidTankProperties[] getTankProperties() {
            return new IFluidTankProperties[]{new FluidTankProperties(tank.getFluid(), tank.getCapacity(), false, true)};
        }

        @Override public int fill(FluidStack resource, boolean doFill) { return 0; }

        @Override public @Nullable FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.amount <= 0) return null;
            FluidStack drained = tank.drain(resource, doDrain);
            if (drained != null && drained.amount > 0 && doDrain) master.TankContentsChanged();
            return drained;
        }

        @Override public @Nullable FluidStack drain(int maxDrain, boolean doDrain) {
            FluidStack drained = tank.drain(maxDrain, doDrain);
            if (drained != null && drained.amount > 0 && doDrain) master.TankContentsChanged();
            return drained;
        }
    }
}
