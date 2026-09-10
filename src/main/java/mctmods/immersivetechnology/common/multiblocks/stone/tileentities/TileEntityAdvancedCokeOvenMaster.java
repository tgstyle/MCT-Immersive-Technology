package mctmods.immersivetechnology.common.multiblocks.stone.tileentities;

import com.immersiveconvergence.ImmersiveConvergence;
import com.immersiveconvergence.api.ICIntegration;
import com.immersiveconvergence.api.crafting.ICCokeOvenRecipe;
import com.immersiveconvergence.api.client.ICSoundHandler;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.IComparatorOverride;
import com.immersiveconvergence.api.multiblock.PoICache;
import com.immersiveconvergence.api.network.ITileSyncReceiver;
import com.immersiveconvergence.api.network.TileSyncMessage;
import com.immersiveconvergence.api.particles.ParticleCampfireSmoke;
import com.immersiveconvergence.api.util.ICFluidTank;
import com.immersiveconvergence.api.util.ICInventoryHandler;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.util.IICInventory;

import mctmods.immersivetechnology.common.Config;
import mctmods.immersivetechnology.common.Config.ITConfig;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityAdvancedCokeOvenBaseheater;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;

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
import net.minecraftforge.fluids.capability.IFluidHandler;
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

public class TileEntityAdvancedCokeOvenMaster extends TileEntityAdvancedCokeOvenSlave implements ICFluidTank.TankListener, IICInventory, IComparatorOverride, ITileSyncReceiver {
    private static int tankSize() { return Multiblocks.advancedCokeOven.advancedCokeOven_tankSize; }

    public static float baseSpeed = Multiblocks.advancedCokeOven.advancedCokeOven_speed_base;
    public static float baseheaterAdd = Multiblocks.advancedCokeOven.advancedCokeOven_baseheater_speed_increase;
    public static float baseheaterMult = Multiblocks.advancedCokeOven.advancedCokeOven_baseheater_speed_multiplier;
    public static int slotCount = 4;

    public ICFluidTank tank = new ICFluidTank(tankSize(), this);
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    public int processTimeRemaining = 0;
    public int processTimeMax = 0;
    public boolean active = false;

    private float soundVolume = 0;
    private ICCokeOvenRecipe cachedRecipe;
    private int soundGracePeriod = 0;
    private int oldComparatorOutput = -1;
    private boolean isRunning = false;

    final IItemHandler inputHandler = new ICInventoryHandler(1, this, 0, new boolean[]{true}, new boolean[]{false});
    final IItemHandler outputHandler = new ICInventoryHandler(1, this, 1, new boolean[]{false}, new boolean[]{true});

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        processTimeRemaining = nbt.getInteger("processTimeRemaining");
        processTimeMax = nbt.getInteger("processTimeMax");
        active = nbt.getBoolean("active");
        tank.readFromNBT(nbt.getCompoundTag("tank"));
        inventory = ICUtils.readInventory(nbt.getTagList("inventory", 10), slotCount);
        isRunning = nbt.getBoolean("isRunning");
        soundGracePeriod = nbt.getInteger("soundGracePeriod");
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setInteger("processTimeRemaining", processTimeRemaining);
        nbt.setInteger("processTimeMax", processTimeMax);
        nbt.setBoolean("active", active);
        nbt.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
        nbt.setTag("inventory", ICUtils.writeInventory(inventory));
        nbt.setBoolean("isRunning", isRunning);
        nbt.setInteger("soundGracePeriod", soundGracePeriod);
    }

    @SideOnly(Side.CLIENT)
    private void spawnParticles() {
        BlockPos smokePos = poiWorldPos("smoke0");
        if (!isRunning) return;
        Random rand = world.rand;
        int lessParticleSetting = Minecraft.getMinecraft().gameSettings.particleSetting;
        if (lessParticleSetting == 2 || (lessParticleSetting == 1 && rand.nextInt(3) == 0)) return;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (smokePos.distanceSq(player.posX, player.posY, player.posZ) > 4096) return;
        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleCampfireSmoke(world,
                smokePos.getX() + 0.5, smokePos.getY() + 0.9, smokePos.getZ() + 0.5,
                (rand.nextDouble() - 0.5) * 0.0125, 0.05 * ITConfig.Client.particles.colored_smoke_height / Config.SMOKE_HEIGHT_DEFAULT, (rand.nextDouble() - 0.5) * 0.0125));
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        BlockPos soundPos = poiWorldPos("sound0");
        float targetSoundLevel = isRunning ? 1f : 0f;
        if (soundVolume < targetSoundLevel) { soundVolume = Math.min(soundVolume + 0.01f, targetSoundLevel); }
        else if (soundVolume > targetSoundLevel) { soundVolume = Math.max(soundVolume - 0.01f, targetSoundLevel); }
        if (soundVolume <= 0) {
            ICSoundHandler.stopSound(soundPos);
            soundVolume = 0;
        } else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos.getX() + .5, soundPos.getY() + .5, soundPos.getZ() + .5) / 8, 1);
            ITSounds.advancedCokeOven.PlayRepeating(soundPos, soundVolume / attenuation, 1);
        }
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
        super.disassemble();
    }

    private void notifyNearbyClients() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("active", active);
        tag.setBoolean("isRunning", isRunning);
        BlockPos center = getPos();
        ImmersiveConvergence.packetHandler.sendToAllTracking(new TileSyncMessage(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
    }

    private void notifyProcessUpdate() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("processTimeRemaining", processTimeRemaining);
        tag.setInteger("processTimeMax", processTimeMax);
        BlockPos center = getPos();
        ImmersiveConvergence.packetHandler.sendToAllTracking(new TileSyncMessage(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
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
        if (world.isRemote) {
            handleSounds();
            spawnParticles();
            return;
        }
        super.update();
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
                    ICUtils.modifyInvStackSize(inventory, 0, -cachedRecipe.inputSize);
                    doGraphicalUpdates(0);
                    if (inventory.get(1).isEmpty()) inventory.set(1, cachedRecipe.output.copy());
                    else inventory.get(1).grow(cachedRecipe.output.getCount());
                    doGraphicalUpdates(1);
                    tank.fill(new FluidStack(ICIntegration.creosote(), cachedRecipe.creosoteOutput), true);
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
        if (tank.getFluidAmount() > 0 && (inventory.get(3).isEmpty() || ItemHandlerHelper.canItemStacksStack(inventory.get(3), ICUtils.fillFluidContainer(tank, inventory.get(2), inventory.get(3), null)))) {
            ItemStack filled = ICUtils.fillFluidContainer(tank, inventory.get(2), inventory.get(3), null);
            if (!filled.isEmpty()) {
                if (inventory.get(2).getCount() == 1 && !ICUtils.isFluidContainerFull(filled)) {
                    inventory.set(2, filled);
                    doGraphicalUpdates(2);
                } else {
                    if (inventory.get(3).isEmpty()) inventory.set(3, filled);
                    else inventory.get(3).grow(filled.getCount());
                    ICUtils.modifyInvStackSize(inventory, 2, -filled.getCount());
                    doGraphicalUpdates(2);
                    doGraphicalUpdates(3);
                }
                update = true;
            }
        }
        if (!inventory.get(1).isEmpty()) {
            TileEntity te = world.getTileEntity(poiFrontPos("item_output0"));
            if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, poi("item_output0").facing.getOpposite())) {
                IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, poi("item_output0").facing.getOpposite());
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
        int comp = comparatorValue();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            notifyComparators();
        }
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
        IFluidHandler output = FluidUtil.getFluidHandler(world, poiFrontPos("fluid_output0"), poi("fluid_output0").facing.getOpposite());
        if (output == null) return false;
        FluidStack available = tank.getFluid();
        if (available == null) return false;
        int accepted = output.fill(available, false);
        if (accepted <= 0) return false;
        FluidStack toPush = ICUtils.copyFluidStackWithAmount(available, accepted, false);
        int filled = output.fill(toPush, true);
        if (filled > 0) tank.drain(filled, true);
        return filled > 0;
    }

    private ICCokeOvenRecipe getRecipe() {
        ICCokeOvenRecipe recipe = ICCokeOvenRecipe.findRecipe(inventory.get(0));
        if (recipe == null) return null;
        if (inventory.get(0).getCount() < recipe.inputSize) return null;
        if (inventory.get(1).isEmpty() || (OreDictionary.itemMatches(inventory.get(1), recipe.output, false) && inventory.get(1).getCount() + recipe.output.getCount() <= getSlotLimit(1)))
            if (tank.getFluidAmount() + recipe.creosoteOutput <= tank.getCapacity()) return recipe;
        return null;
    }

    private float getProcessSpeed() {
        int activeBaseheaters = 0;
        for (PoICache poi : poisWithPrefix("baseheater")) {
            BlockPos pos = getBlockPosForPos(poi.position).offset(poi.facing);
            TileEntity tile = ICUtils.getExistingTileEntity(world, pos);
            if (!(tile instanceof TileEntityAdvancedCokeOvenBaseheater)) continue;
            TileEntityAdvancedCokeOvenBaseheater heater = (TileEntityAdvancedCokeOvenBaseheater)tile;
            if (!heater.doSpeedup()) continue;
            activeBaseheaters++;
        }
        return (baseSpeed + activeBaseheaters * baseheaterAdd) * (1 + activeBaseheaters * (baseheaterMult - 1));
    }

    private void setHeatersActive() {
        for (PoICache poi : poisWithPrefix("baseheater")) {
            BlockPos pos = getBlockPosForPos(poi.position).offset(poi.facing);
            TileEntity tile = ICUtils.getExistingTileEntity(world, pos);
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

    @Override public void TankContentsChanged() {
        efficientMarkDirty();
        requestClientSync();
    }

    @Override public int getComparatorInputOverride() { return isComparatorPos() ? comparatorValue() : 0; }

    public int comparatorValue() {
        if (!formed || processTimeMax <= 0) return 0;
        return 15 * (processTimeMax - processTimeRemaining) / processTimeMax;
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityAdvancedCokeOvenMaster master() { return this; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, BlockPos position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (isPoI("fluid_output0", side, position)) return tankView(0, tank);
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        return isPoI("fluid_output0", side, position) && iTank == 0;
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (!formed) return false;
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            return isPoI("item_input0", facing, posInMultiblock()) || isPoI("item_output0", facing, posInMultiblock());
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nullable public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (!formed) return super.getCapability(capability, facing);
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            if (isPoI("item_input0", facing, posInMultiblock())) return (T)inputHandler;
            if (isPoI("item_output0", facing, posInMultiblock())) return (T)outputHandler;
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
}
