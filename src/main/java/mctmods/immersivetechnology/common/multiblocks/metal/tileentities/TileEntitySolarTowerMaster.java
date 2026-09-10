package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.ImmersiveConvergence;
import com.immersiveconvergence.api.client.ICSoundHandler;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.IComparatorOverride;
import com.immersiveconvergence.api.network.BinaryTileSyncMessage;
import com.immersiveconvergence.api.network.IBinaryMessageReceiver;
import com.immersiveconvergence.api.util.ICFluidTank;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.util.IICInventory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.api.crafting.SolarTowerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
import mctmods.immersivetechnology.common.util.compat.advancedrocketry.AdvancedRocketryHelper;
import mctmods.immersivetechnology.common.util.solarregistry.SolarRegistry;

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
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;

public class TileEntitySolarTowerMaster extends TileEntitySolarTowerSlave implements ICFluidTank.TankListener, IBinaryMessageReceiver, IICInventory, IComparatorOverride {
    private static int inputTankSize() { return Multiblocks.solarTower.solarTower_input_tankSize; }

    private static int outputTankSize() { return Multiblocks.solarTower.solarTower_output_tankSize; }

    private static int solarMaxRange() { return Multiblocks.solarReflector.solarReflector_maxRange; }

    private static int solarMinRange() { return Multiblocks.solarReflector.solarReflector_minRange; }

    private static int progressLossPerTick() { return Multiblocks.solarTower.solarTower_progress_lossInTicks; }

    private static double heatLossMultiplier() { return Multiblocks.solarTower.solarTower_heat_loss_multiplier; }

    private static float speedMult() { return Multiblocks.solarTower.solarTower_speed_multiplier; }

    private static double workingHeatLevel() { return Multiblocks.solarTower.solarTower_heat_workingTemperature; }

    private static double dayMinHeatLoss() { return Multiblocks.solarTower.solarTower_heat_dayMinLoss; }

    private static double lossPerSectionDrop() { return Multiblocks.solarTower.solarTower_heat_lossPerSectionDrop; }

    private static double tempDependentLossFactor() { return Multiblocks.solarTower.solarTower_heat_tempDependentLossFactor; }

    private static double heatIncreaseFactor() { return Multiblocks.solarTower.solarTower_heat_increaseFactor; }

    public FluidTank[] tanks = new FluidTank[] {
            new ICFluidTank(inputTankSize(), this),
            new ICFluidTank(outputTankSize(), this)
    };

    public static int slotCount = 4;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);

    public int processTimeRemaining = 0;
    public int processTimeMax = 0;
    public double heatLevel = 0;
    public double reflectorStrength = 0;
    public int solarIncidenceAngleSection = 0;

    public SolarTowerRecipe cachedSolarTowerRecipe;

    private float soundVolume = 0f;
    private int soundGracePeriod = 0;
    private boolean isRunning = false;
    private boolean sunVisible = true;
    private boolean isLoaded = false;
    private boolean registered = false;
    private boolean reCheckOnLoad = false;
    private boolean savedRegistered = false;

    private double distanceSqToTE;
    private int playerDimension;
    private int clientSyncTimer = 0;
    private int oldComparatorOutput = 0;

    public void efficientMarkDirty() {
        world.getChunk(getPos()).markDirty();
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        heatLevel = nbt.getDouble("heatLevel");
        processTimeRemaining = nbt.getInteger("processTimeRemaining");
        processTimeMax = nbt.getInteger("processTimeMax");
        reflectorStrength = nbt.getDouble("reflectorStrength");
        inventory = ICUtils.readInventory(nbt.getTagList("inventory", 10), slotCount);
        registered = nbt.getBoolean("registered");
        savedRegistered = nbt.getBoolean("savedRegistered");
        reCheckOnLoad = nbt.getBoolean("reCheckOnLoad");
        isLoaded = nbt.getBoolean("isLoaded");
        soundGracePeriod = nbt.getInteger("soundGracePeriod");
        if (!descPacket) {
            if (nbt.hasKey("cachedRecipe")) cachedSolarTowerRecipe = SolarTowerRecipe.loadFromNBT(nbt.getCompoundTag("cachedRecipe"));
            if (processTimeRemaining > 0 && cachedSolarTowerRecipe == null) processTimeRemaining = 0;
        }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setDouble("heatLevel", heatLevel);
        nbt.setInteger("processTimeRemaining", processTimeRemaining);
        nbt.setInteger("processTimeMax", processTimeMax);
        nbt.setDouble("reflectorStrength", reflectorStrength);
        nbt.setTag("inventory", ICUtils.writeInventory(inventory));
        nbt.setBoolean("registered", registered);
        nbt.setBoolean("savedRegistered", savedRegistered);
        nbt.setBoolean("reCheckOnLoad", reCheckOnLoad);
        nbt.setBoolean("isLoaded", isLoaded);
        nbt.setInteger("soundGracePeriod", soundGracePeriod);
        if (!descPacket && cachedSolarTowerRecipe != null) nbt.setTag("cachedRecipe", cachedSolarTowerRecipe.writeToNBT(new NBTTagCompound()));
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        BlockPos soundPos = poiWorldPos("sound0");
        if (distanceSqToTE > 4096) {
            ICSoundHandler.stopSound(soundPos);
            soundVolume = 0f;
            return;
        }
        double maxHeat = targetTemperature();
        boolean shouldPlay = maxHeat > 0 && heatLevel >= maxHeat && reflectorStrength > 0 && sunVisible;
        float heatFactor = shouldPlay ? (float)(heatLevel / maxHeat) : 0f;
        float targetSoundLevel = shouldPlay ? heatFactor : 0f;
        if (soundVolume < targetSoundLevel) { soundVolume = Math.min(soundVolume + 0.02f, targetSoundLevel); }else if (soundVolume > targetSoundLevel) { soundVolume = Math.max(soundVolume - 0.02f, targetSoundLevel); }
        if (soundVolume <= 0f) { ICSoundHandler.stopSound(soundPos); }else {
            float attenuation = Math.max((float)distanceSqToTE / 32f, 1f);
            ITSounds.solarTower.PlayRepeating(soundPos, (2 * soundVolume) / attenuation, 1f);
        }
    }

    @Override public void disassemble() {
        if (!world.isRemote) {
            for (ItemStack stack : inventory) if (!stack.isEmpty()) ICUtils.dropStackAtPos(world, getPos(), stack.copy());
            inventory.clear();
            detachMirrors();
            SolarRegistry.unregisterTower(world, poiWorldPos("link0"));
        }
        super.disassemble();
    }

    public void requestUpdate() {
        ImmersiveConvergence.packetHandler.sendToServer(new BinaryTileSyncMessage(getPos(), Unpooled.buffer()));
    }

    public void notifyNearbyClients() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeDouble(heatLevel);
        buf.writeInt(solarIncidenceAngleSection);
        buf.writeBoolean(isRunning);
        buf.writeBoolean(sunVisible);
        NetworkRegistry.TargetPoint tp = new NetworkRegistry.TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 40);
        ImmersiveConvergence.packetHandler.sendToAllAround(new BinaryTileSyncMessage(getPos(), buf), tp);
    }

    @Override public void receiveMessageFromServer(ByteBuf message) {
        heatLevel = message.readDouble();
        solarIncidenceAngleSection = message.readInt();
        isRunning = message.readBoolean();
        sunVisible = message.readBoolean();
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeDouble(heatLevel);
        buf.writeInt(solarIncidenceAngleSection);
        buf.writeBoolean(isRunning);
        buf.writeBoolean(sunVisible);
        ImmersiveConvergence.packetHandler.sendTo(new BinaryTileSyncMessage(getPos(), buf), player);
    }

    @SideOnly(Side.CLIENT)
    private void clientUpdate() {
        BlockPos soundPos = poiWorldPos("sound0");
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distSq = player.getDistanceSq(soundPos.getX() + 0.5, soundPos.getY() + 0.5, soundPos.getZ() + 0.5);
        if (world.provider.getDimension() == player.dimension && distSq < 400 && (distanceSqToTE > 400 || playerDimension != player.dimension)) requestUpdate();
        distanceSqToTE = distSq;
        playerDimension = player.dimension;
        if (distSq < 4096) {
            clientSyncTimer++;
            if (clientSyncTimer >= 40) {
                clientSyncTimer = 0;
                requestUpdate();
            }
        }
        handleSounds();
    }

    private int computeSolarIncidenceAngleSection() {
        int light = world.getSkylightSubtracted();
        if (light == 3) return 1;
        if (light == 2) return 2;
        if (light == 1) return 3;
        if (light == 0) return 4;
        return 0;
    }

    private void detachMirrors() {
        Set<BlockPos> reflectors = SolarRegistry.getReflectorsInRange(world, poiWorldPos("link0"), solarMinRange(), solarMaxRange());
        for (BlockPos pos : reflectors) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntitySolarReflectorSlave) {
                TileEntitySolarReflectorMaster ref = ((TileEntitySolarReflectorSlave)tile).master();
                if (ref != null) ref.detachTower();
            }
        }
    }

    private void checkReflectorPositions() {
        BlockPos collectorPos = poiWorldPos("collector0");
        Set<BlockPos> reflectors = SolarRegistry.getReflectorsInRange(world, poiWorldPos("link0"), solarMinRange(), solarMaxRange());
        for (BlockPos pos : reflectors) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntitySolarReflectorSlave) {
                TileEntitySolarReflectorMaster ref = ((TileEntitySolarReflectorSlave)tile).master();
                if (ref != null && ref.isMirrorTaken && !ref.getCollectorPosition().equals(collectorPos)) ref.detachTower();
            }
        }
        double totalMirrorStrength = 0;
        for (BlockPos pos : reflectors) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntitySolarReflectorSlave) {
                TileEntitySolarReflectorMaster ref = ((TileEntitySolarReflectorSlave)tile).master();
                if (ref != null && ref.setTowerCollectorPosition(collectorPos)) totalMirrorStrength += ref.getSolarCollectorStrength();
            }
        }
        totalMirrorStrength *= world.isRaining() ? 0.4 : 1;
        if (ITCompatModule.isAdvancedRocketryLoaded) totalMirrorStrength *= AdvancedRocketryHelper.getInsolation(world, getPos());
        double humidityBonus = 0.075 * totalMirrorStrength * -((world.getBiome(getPos()).getRainfall() - 0.5) / 0.5);
        if (ITCompatModule.isAdvancedRocketryLoaded) humidityBonus *= AdvancedRocketryHelper.getWaterPartialPressureMultiplier(world, getPos());
        totalMirrorStrength += humidityBonus;
        reflectorStrength = totalMirrorStrength;
    }

    public void forceReflectorCheck() {
        checkReflectorPositions();
    }

    private FluidStack targetLookupFluid;
    private SolarTowerRecipe targetLookupRecipe;
    private boolean targetLookupDone;

    public double targetTemperature() {
        if (cachedSolarTowerRecipe != null) { return cachedSolarTowerRecipe.requiredTemp; }
        FluidStack input = tanks[0].getFluid();
        if (!targetLookupDone || !isSameFluid(input, targetLookupFluid)) {
            targetLookupRecipe = SolarTowerRecipe.findRecipe(input);
            targetLookupFluid = input == null ? null : input.copy();
            targetLookupDone = true;
        }
        return targetLookupRecipe != null ? targetLookupRecipe.requiredTemp : workingHeatLevel();
    }

    private static boolean isSameFluid(FluidStack a, FluidStack b) {
        if (a == null || b == null) { return a == b; }
        return a.isFluidEqual(b);
    }

    private double getTemperatureIncrease() {
        if (!registered || reflectorStrength <= 0 || world.isRaining() || !world.isDaytime() || !sunVisible) { return 0; }
        return reflectorStrength * heatIncreaseFactor() * solarIncidenceAngleSection;
    }

    private double getTemperatureLoss() {
        double conduction = 1.0;
        if (ITCompatModule.isAdvancedRocketryLoaded) conduction *= AdvancedRocketryHelper.getHeatTransferCoefficient(world, poiWorldPos("collector0"));
        double loss = dayMinHeatLoss();
        loss += lossPerSectionDrop() * (4 - solarIncidenceAngleSection);
        loss += heatLevel * tempDependentLossFactor();
        return loss * heatLossMultiplier() * conduction;
    }

    private boolean pumpOutputOut() {
        BlockPos fluidOutputFront = poiFrontPos("fluid_output0");
        boolean changed = false;
        FluidStack out = tanks[1].getFluid();
        if (out != null && out.amount > 0) {
            IFluidHandler handler = FluidUtil.getFluidHandler(world, fluidOutputFront, poi("fluid_output0").facing.getOpposite());
            if (handler != null) {
                FluidStack sim = out.copy();
                int accepted = handler.fill(sim, false);
                if (accepted > 0) {
                    FluidStack push = ICUtils.copyFluidStackWithAmount(out, accepted, false);
                    int pushed = handler.fill(push, true);
                    tanks[1].drain(pushed, true);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private boolean outputTankLogic() {
        boolean update = false;
        FluidStack fluid = tanks[1].getFluid();
        if (fluid != null && fluid.amount > 0) {
            ItemStack filled = ICUtils.fillFluidContainer(tanks[1], inventory.get(2), inventory.get(3), null);
            if (!filled.isEmpty()) {
                if (!inventory.get(3).isEmpty() && OreDictionary.itemMatches(inventory.get(3), filled, true)) inventory.get(3).grow(filled.getCount());
                else if (inventory.get(3).isEmpty()) inventory.set(3, filled.copy());
                inventory.get(2).shrink(1);
                if (inventory.get(2).getCount() <= 0) inventory.set(2, ItemStack.EMPTY);
                doGraphicalUpdates(2);
                doGraphicalUpdates(3);
                update = true;
            }
            if (pumpOutputOut()) update = true;
        }
        return update;
    }

    private boolean inputTankLogic() {
        boolean update = false;
        int prev = tanks[0].getFluidAmount();
        ItemStack empty = ICUtils.drainFluidContainer(tanks[0], inventory.get(0), inventory.get(1), null);
        if (prev != tanks[0].getFluidAmount()) {
            if (!inventory.get(1).isEmpty() && OreDictionary.itemMatches(inventory.get(1), empty, true)) inventory.get(1).grow(empty.getCount());
            else if (inventory.get(1).isEmpty()) inventory.set(1, empty.copy());
            inventory.get(0).shrink(1);
            if (inventory.get(0).getCount() <= 0) inventory.set(0, ItemStack.EMPTY);
            doGraphicalUpdates(0);
            doGraphicalUpdates(1);
            update = true;
        }
        return update;
    }

    private boolean heatLogic() {
        double previous = heatLevel;
        double increase = isRSDisabled() ? 0 : getTemperatureIncrease();
        heatLevel = Math.max(0, heatLevel + increase - getTemperatureLoss());
        heatLevel = Math.min(heatLevel, targetTemperature());
        return previous != heatLevel;
    }

    private boolean recipeLogic() {
        boolean update = false;
        boolean didWork = false;
        FluidStack current = tanks[0].getFluid();
        if (current == null || current.amount <= 0) {
            if (processTimeRemaining != 0 || processTimeMax != 0) {
                processTimeRemaining = 0;
                processTimeMax = 0;
                update = true;
            }
            cachedSolarTowerRecipe = null;
            if (soundGracePeriod > 0) soundGracePeriod--;
            return update;
        }
        SolarTowerRecipe recipe = cachedSolarTowerRecipe;
        if (recipe == null || !current.isFluidEqual(recipe.fluidInput)) {
            recipe = SolarTowerRecipe.findRecipe(current);
            if (recipe != cachedSolarTowerRecipe) {
                cachedSolarTowerRecipe = recipe;
                update = true;
            }
            if (processTimeRemaining != 0 || processTimeMax != 0) {
                processTimeRemaining = 0;
                processTimeMax = 0;
                update = true;
            }
        }
        if (recipe == null) {
            if (soundGracePeriod > 0) soundGracePeriod--;
            return update;
        }
        int total = recipe.getTotalProcessTime();
        if (processTimeMax != total) {
            processTimeMax = total;
            processTimeRemaining = total;
            update = true;
        }
        if (!isRSDisabled() && heatLevel >= recipe.requiredTemp) {
            processTimeRemaining -= (int)speedMult();
            didWork = true;
            update = true;
        } else if (processTimeRemaining < total) {
            processTimeRemaining = Math.min(processTimeRemaining + progressLossPerTick(), total);
            update = true;
        }
        if (processTimeRemaining <= 0) {
            FluidStack out = recipe.fluidOutput.copy();
            if (tanks[1].fillInternal(out.copy(), false) == out.amount) {
                FluidStack drained = tanks[0].drain(recipe.fluidInput.amount, true);
                if (drained != null && drained.amount == recipe.fluidInput.amount && drained.isFluidEqual(recipe.fluidInput)) {
                    tanks[1].fillInternal(out, true);
                    processTimeRemaining = total;
                    update = true;
                }
            }
        }
        if (didWork) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;
        return update;
    }

    @Override public void update() {
        BlockPos collectorPos = poiWorldPos("collector0");
        BlockPos linkPos = poiWorldPos("link0");
        super.update();
        if (!formed) return;
        if (world.isRemote) {
            clientUpdate();
            return;
        }
        solarIncidenceAngleSection = computeSolarIncidenceAngleSection();
        boolean update = false;
        boolean wasSunVisible = sunVisible;
        sunVisible = world.canBlockSeeSky(collectorPos);
        if (sunVisible != wasSunVisible) update = true;
        if (!isLoaded) {
            isLoaded = true;
            SolarRegistry.RegisterResult result = SolarRegistry.registerTower(world, linkPos);
            registered = result.success;
            if (!registered && savedRegistered) {
                int y = linkPos.getY();
                Set<BlockPos> towersAtY = SolarRegistry.getData(world).towerBasesByY.computeIfAbsent(y, k -> new HashSet<>());
                towersAtY.add(linkPos);
                SolarRegistry.getData(world).markDirty();
                registered = true;
            }
            if (registered) checkReflectorPositions();
        }
        if (reCheckOnLoad) {
            reCheckOnLoad = false;
            if (registered) checkReflectorPositions();
        }
        boolean enabled = !isRSDisabled();
        if (!enabled && reflectorStrength > 0) {
            detachMirrors();
            reflectorStrength = 0;
            update = true;
        }
        double oldRef = reflectorStrength;
        if (enabled && (world.getTotalWorldTime() % 60 == 0 || reflectorStrength == 0)) checkReflectorPositions();
        if (reflectorStrength != oldRef) {
            update = true;
            notifyNearbyClients();
        }
        update |= heatLogic();
        update |= recipeLogic();
        update |= outputTankLogic();
        update |= inputTankLogic();
        boolean wasRunning = isRunning;
        isRunning = soundGracePeriod > 0;
        if (isRunning != wasRunning) notifyNearbyClients();
        int comp = comparatorValue();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            notifyComparators();
            update = true;
        }
        if (update) {
            efficientMarkDirty();
            if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
            else { throttledBlockUpdate(); }
        }
    }

    @Override public void TankContentsChanged() {
        if (processTimeRemaining == 0) {
            FluidStack input = tanks[0].getFluid();
            cachedSolarTowerRecipe = input != null && input.amount > 0 ? SolarTowerRecipe.findRecipe(input) : null;
        }
        efficientMarkDirty();
        requestClientSync();
    }

    @Override public int getComparatorInputOverride() { return isComparatorPos() ? comparatorValue() : 0; }

    public int comparatorValue() {
        return (int)Math.min(15, 15 * heatLevel / targetTemperature());
    }

    @Override public boolean isDummy() {
        return false;
    }

    @Override public TileEntitySolarTowerMaster master() {
        return this;
    }

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
            FluidStack current = tanks[0].getFluid();
            if (current == null) { return true; }
            return resource.isFluidEqual(current);
        }
        return false;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        return iTank == 1 && isPoI("fluid_output0", side, position) && tanks[1].getFluidAmount() > 0;
    }

    @Override @Nonnull public int[] getOutputTanks() {
        return new int[] {1};
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() {
        return ITUtils.EMPTY_INT_ARRAY;
    }

    @Override @Nonnull public int[] getCurrentProcessesMax() {
        return ITUtils.EMPTY_INT_ARRAY;
    }

    @Override @Nonnull public NonNullList<ItemStack> getInventory() {
        return inventory;
    }

    @Override public boolean isStackValid(int slot, ItemStack stack) {
        return true;
    }

    @Override public int getSlotLimit(int slot) {
        return 64;
    }

    @Override public void doGraphicalUpdates(int slot) {
        efficientMarkDirty();
        markContainingBlockForUpdate(null);
    }

    @Override @Nonnull public NonNullList<ItemStack> getDroppedItems() {
        return getInventory();
    }

    @Override public int getComparatedSize() {
        return slotCount;
    }
}
