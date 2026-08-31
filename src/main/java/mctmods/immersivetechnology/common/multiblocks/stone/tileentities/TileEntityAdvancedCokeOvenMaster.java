package mctmods.immersivetechnology.common.multiblocks.stone.tileentities;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import com.immersiveconvergence.api.multiblock.PoICache;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;

import com.immersiveconvergence.api.particles.ParticleCampfireSmoke;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityAdvancedCokeOvenBaseheater;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentitiesmultiblockpart.TileEntityITMultiblockPartAdvancedCokeOven;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;
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

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityAdvancedCokeOvenMaster extends TileEntityAdvancedCokeOvenSlave implements ITFluidTank.TankListener, IIEInventory, IComparatorOverride {

    private static int tankSize() { return Multiblocks.advancedCokeOven.advancedCokeOven_tankSize; }
    public static float baseSpeed = Multiblocks.advancedCokeOven.advancedCokeOven_speed_base;
    public static float baseheaterAdd = Multiblocks.advancedCokeOven.advancedCokeOven_baseheater_speed_increase;
    public static float baseheaterMult = Multiblocks.advancedCokeOven.advancedCokeOven_baseheater_speed_multiplier;
    public static int slotCount = 4;

    public ITFluidTank tank = new ITFluidTank(tankSize(), this);
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    public int processTimeRemaining = 0;
    public int processTimeMax = 0;
    public boolean active = false;

    private float soundVolume = 0;
    private CokeOvenRecipe cachedRecipe;
    private int soundGracePeriod = 0;
    private boolean isRunning = false;
    private boolean needsPoIInit = false;

    PoICache itemInputPos0;
    PoICache itemOutputPos0;
    PoICache fluidOutputPos0;
    PoICache baseheaterPos0;
    PoICache baseheaterPos1;
    private BlockPos soundPos0;
    private BlockPos smokePos0;

    BlockPos itemOutputTEPos0;
    BlockPos fluidOutputTEPos0;

    final IItemHandler inputHandler = new IEInventoryHandler(1, this, 0, new boolean[]{true}, new boolean[]{false});
    final IItemHandler outputHandler = new IEInventoryHandler(1, this, 1, new boolean[]{false}, new boolean[]{true});

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        processTimeRemaining = nbt.getInteger("processTimeRemaining");
        processTimeMax = nbt.getInteger("processTimeMax");
        active = nbt.getBoolean("active");
        tank.readFromNBT(nbt.getCompoundTag("tank"));
        inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
        isRunning = nbt.getBoolean("isRunning");
        soundGracePeriod = nbt.getInteger("soundGracePeriod");
        if (formed && !descPacket) needsPoIInit = true;
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setInteger("processTimeRemaining", processTimeRemaining);
        nbt.setInteger("processTimeMax", processTimeMax);
        nbt.setBoolean("active", active);
        nbt.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
        nbt.setTag("inventory", Utils.writeInventory(inventory));
        nbt.setBoolean("isRunning", isRunning);
        nbt.setInteger("soundGracePeriod", soundGracePeriod);
    }

    @SideOnly(Side.CLIENT)
    private void spawnParticles() {
        if (smokePos0 == null) InitializePoIs();
        if (smokePos0 == null || !isRunning) return;
        Random rand = new Random();
        int lessParticleSetting = Minecraft.getMinecraft().gameSettings.particleSetting;
        if (lessParticleSetting == 2 || (lessParticleSetting == 1 && rand.nextInt(3) == 0)) return;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (smokePos0.distanceSq(player.posX, player.posY, player.posZ) > 4096) return;
        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleCampfireSmoke(world,
                smokePos0.getX() + 0.5, smokePos0.getY() + 0.9, smokePos0.getZ() + 0.5,
                (rand.nextDouble() - 0.5) * 0.0125, 0.05, (rand.nextDouble() - 0.5) * 0.0125));
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (soundPos0 == null) InitializePoIs();
        float targetSoundLevel = isRunning ? 1f : 0f;
        if (soundVolume < targetSoundLevel) { soundVolume = Math.min(soundVolume + 0.01f, targetSoundLevel); }
        else if (soundVolume > targetSoundLevel) { soundVolume = Math.max(soundVolume - 0.01f, targetSoundLevel); }
        if (soundVolume <= 0) {
            ITSoundHandler.StopSound(soundPos0);
            soundVolume = 0;
        } else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos0.getX() + .5, soundPos0.getY() + .5, soundPos0.getZ() + .5) / 8, 1);
            ITSounds.advancedCokeOven.PlayRepeating(soundPos0, soundVolume / attenuation, 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        if (soundPos0 != null) ITSoundHandler.StopSound(soundPos0);
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
        if (soundPos0 == null) InitializePoIs();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
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

    @Override public void update() {
        if (!formed) return;
        if (needsPoIInit || itemInputPos0 == null) {
            InitializePoIs();
            needsPoIInit = false;
        }
        if (world.isRemote) {
            handleSounds();
            spawnParticles();
            return;
        }
        boolean update = false;
        boolean wasRunning = isRunning;
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
            TileEntity te = world.getTileEntity(itemOutputTEPos0);
            if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, itemOutputPos0.facing.getOpposite())) {
                IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, itemOutputPos0.facing.getOpposite());
                if (handler != null) {
                    ItemStack current = inventory.get(1).copy();
                    ItemStack remaining = ItemHandlerHelper.insertItemStacked(handler, current, false);
                    if (remaining.getCount() < current.getCount()) {
                        inventory.set(1, remaining);
                        doGraphicalUpdates(1);
                        update = true;
                    }
                }
            }
        }
        if (pumpOutputOut()) update = true;
        if (active) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;
        isRunning = soundGracePeriod > 0;
        if (isRunning != wasRunning) notifyNearbyClients();
        if (update || isRunning != wasRunning) {
            efficientMarkDirty();
            if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
            else { throttledBlockUpdate(); }
        }
    }

    private boolean pumpOutputOut() {
        if (tank.getFluidAmount() == 0) return false;
        IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputTEPos0, fluidOutputPos0.facing.getOpposite());
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
        PoICache[] heaters = {baseheaterPos0, baseheaterPos1};
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
        PoICache[] heaters = {baseheaterPos0, baseheaterPos1};
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
        for (PoIJSONSchema poi : TileEntityITMultiblockPartAdvancedCokeOven.instance.pointsOfInterest) {
            PoICache cache = new PoICache(facing, poi, mirrored);
            switch (poi.name) {
                case "item_input0":
                    itemInputPos0 = cache;
                    break;
                case "item_output0":
                    itemOutputPos0 = cache;
                    itemOutputTEPos0 = getBlockPosForPos(cache.position).offset(cache.facing);
                    break;
                case "fluid_output0":
                    fluidOutputPos0 = cache;
                    fluidOutputTEPos0 = getBlockPosForPos(cache.position).offset(cache.facing);
                    break;
                case "baseheater0":
                    baseheaterPos0 = cache;
                    break;
                case "baseheater1":
                    baseheaterPos1 = cache;
                    break;
                case "sound0":
                    soundPos0 = getBlockPosForPos(poi.position);
                    break;
                case "smoke0":
                    smokePos0 = getBlockPosForPos(poi.position);
                    break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        if (itemInputPos0 != null) notifyNeighbor(getBlockPosForPos(itemInputPos0.position));
        if (itemOutputPos0 != null) notifyNeighbor(getBlockPosForPos(itemOutputPos0.position));
        if (fluidOutputPos0 != null) notifyNeighbor(getBlockPosForPos(fluidOutputPos0.position));
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

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, BlockPos position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (fluidOutputPos0 == null) InitializePoIs();
        if (fluidOutputPos0.isPoI(side, position)) return new IFluidTank[]{tank};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        if (!formed || fluidOutputPos0 == null) InitializePoIs();
        return fluidOutputPos0.isPoI(side, position) && iTank == 0;
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (!formed) return false;
        if (itemInputPos0 == null) InitializePoIs();
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            return itemInputPos0.isPoI(facing, posInMultiblock()) || itemOutputPos0.isPoI(facing, posInMultiblock());
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            return fluidOutputPos0.isPoI(facing, posInMultiblock());
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (!formed) return super.getCapability(capability, facing);
        if (itemInputPos0 == null) InitializePoIs();
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            if (itemInputPos0.isPoI(facing, posInMultiblock())) return (T)inputHandler;
            if (itemOutputPos0.isPoI(facing, posInMultiblock())) return (T)outputHandler;
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null && fluidOutputPos0.isPoI(facing, posInMultiblock())) {
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
