package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.crafting.MeltingCrucibleRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSolarMelter;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
import mctmods.immersivetechnology.common.util.compat.advancedrocketry.AdvancedRocketryHelper;
import mctmods.immersivetechnology.common.util.multiblock.PoICache;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.IBinaryMessageReceiver;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.solarregistry.SolarRegistry;
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

import java.util.HashSet;
import java.util.Set;

public class TileEntitySolarMelterMaster extends TileEntitySolarMelterSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver, IIEInventory, IComparatorOverride {

    private static final int outputTankSize = Multiblocks.solarMelter.solarMelter_output_tankSize;
    private static final int solarMaxRange = Multiblocks.solarReflector.solarReflector_maxRange;
    private static final int solarMinRange = Multiblocks.solarReflector.solarReflector_minRange;
    private static final int energyLossPerTick = Multiblocks.solarMelter.solarMelter_progress_lossEnergy;
    private static final double heatLossMultiplier = 0.00067;
    private static final float speedMult = 1f;
    private static final double workingHeatLevel = Multiblocks.solarMelter.solarMelter_heat_workingLevel;
    private static final double maximumReflectorStrength = Multiblocks.solarMelter.solarMelter_maximum_reflector_strength;

    public FluidTank[] tanks = new FluidTank[] {
            new ITFluidTank(outputTankSize, this)
    };
    public IItemHandler insertionHandler = new IEInventoryHandler(1, this, 0, new boolean[]{true}, new boolean[]{false});

    public MeltingCrucibleRecipe cachedSolarMelterRecipe;

    public static int slotCount = 3;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    public int processEnergyRemaining = 0;
    public double heatLevel = 0;
    public double reflectorStrength = 0;
    public int solarIncidenceAngleSection = 0;

    private float soundVolume = 0f;
    private int soundGracePeriod = 60;
    private boolean isRunning = false;

    private PoICache fluidOutput0, itemInput0, redstone0;
    private BlockPos basePos0, collectorPos0, fluidOutputFront0, soundPos0;

    private boolean isLoaded = false;
    private boolean registered = false;
    private boolean reCheckOnLoad = false;
    private boolean savedRegistered = false;
    private boolean needsPoIInit = true;

    private double distanceSqToTE;
    private int playerDimension;

    private int oldComparatorOutput = 0;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        processEnergyRemaining = nbt.getInteger("processEnergyRemaining");
        heatLevel = nbt.getDouble("heatLevel");
        reflectorStrength = nbt.getDouble("reflectorStrength");
        if (!descPacket) {
            inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
            if (processEnergyRemaining > 0 && !inventory.get(0).isEmpty()) {
                cachedSolarMelterRecipe = MeltingCrucibleRecipe.findRecipe(inventory.get(0));
                if (cachedSolarMelterRecipe == null) processEnergyRemaining = 0;
            }
            if (formed) needsPoIInit = true;
        }
        registered = nbt.getBoolean("registered");
        savedRegistered = nbt.getBoolean("savedRegistered");
        reCheckOnLoad = nbt.getBoolean("reCheckOnLoad");
        isLoaded = nbt.getBoolean("isLoaded");
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setInteger("processEnergyRemaining", processEnergyRemaining);
        nbt.setDouble("heatLevel", heatLevel);
        nbt.setDouble("reflectorStrength", reflectorStrength);
        if (!descPacket) nbt.setTag("inventory", Utils.writeInventory(inventory));
        nbt.setBoolean("registered", registered);
        nbt.setBoolean("savedRegistered", savedRegistered);
        nbt.setBoolean("reCheckOnLoad", reCheckOnLoad);
        nbt.setBoolean("isLoaded", isLoaded);
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (distanceSqToTE > 4096) {
            ITSoundHandler.StopSound(soundPos0);
            soundVolume = 0f;
            return;
        }

        float targetSoundLevel = isRunning ? 1f : 0f;
        if (soundVolume < targetSoundLevel) {
            soundVolume = Math.min(soundVolume + 0.02f, targetSoundLevel);
            soundGracePeriod = 60;
        } else if (soundVolume > targetSoundLevel) {
            if (soundGracePeriod > 0) soundGracePeriod--;
            else soundVolume = Math.max(soundVolume - 0.02f, targetSoundLevel);
        }

        if (soundVolume <= 0f) {
            ITSoundHandler.StopSound(soundPos0);
        } else {
            double distance = Math.sqrt(distanceSqToTE);
            float attenuation = Math.max((float)distance / 16f, 1f);
            ITSounds.heatExchanger.PlayRepeating(soundPos0, soundVolume / attenuation, 1f);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        if (soundPos0 != null) ITSoundHandler.StopSound(soundPos0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        if (soundPos0 == null) InitializePoIs();
        if (soundPos0 != null) {
            ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
        }
        if (!world.isRemote) {
            for (ItemStack stack : inventory) {
                if (!stack.isEmpty()) Utils.dropStackAtPos(world, getPos(), stack.copy());
            }
            inventory.clear();
        }
        detachMirrors();
        SolarRegistry.unregisterTower(world, basePos0);
        super.disassemble();
    }

    private void detachMirrors() {
        Set<BlockPos> reflectors = SolarRegistry.getReflectorsInRange(world, basePos0, solarMinRange, solarMaxRange);
        for (BlockPos pos : reflectors) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntitySolarReflectorSlave) {
                TileEntitySolarReflectorMaster ref = ((TileEntitySolarReflectorSlave)tile).master();
                if (ref != null) ref.detachTower();
            }
        }
    }

    public void requestUpdate() {
        ImmersiveTechnology.packetHandler.sendToServer(new BinaryMessageTileSync(getPos(), Unpooled.buffer()));
    }

    public void notifyNearbyClients() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeDouble(heatLevel);
        buf.writeInt(solarIncidenceAngleSection);
        buf.writeBoolean(isRunning);
        ImmersiveTechnology.packetHandler.sendToAllAround(new BinaryMessageTileSync(getPos(), buf), new NetworkRegistry.TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 40));
    }

    @Override public void receiveMessageFromServer(ByteBuf message) {
        heatLevel = message.readDouble();
        solarIncidenceAngleSection = message.readInt();
        isRunning = message.readBoolean();
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeDouble(heatLevel);
        buf.writeInt(solarIncidenceAngleSection);
        buf.writeBoolean(isRunning);
        ImmersiveTechnology.packetHandler.sendTo(new BinaryMessageTileSync(getPos(), buf), player);
    }

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    private void checkReflectorPositions() {
        Set<BlockPos> reflectors = SolarRegistry.getReflectorsInRange(world, basePos0, solarMinRange, solarMaxRange);

        for (BlockPos pos : reflectors) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntitySolarReflectorSlave) {
                TileEntitySolarReflectorMaster ref = ((TileEntitySolarReflectorSlave)tile).master();
                if (ref != null && ref.isMirrorTaken) ref.detachTower();
            }
        }

        double totalMirrorStrength = 0;
        for (BlockPos pos : reflectors) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntitySolarReflectorSlave) {
                TileEntitySolarReflectorMaster ref = ((TileEntitySolarReflectorSlave)tile).master();
                if (ref != null && ref.setTowerCollectorPosition(collectorPos0)) {
                    totalMirrorStrength += ref.getSolarCollectorStrength();
                }
            }
        }

        totalMirrorStrength *= world.isRaining() ? 0.4 : 1;
        if (ITCompatModule.isAdvancedRocketryLoaded) totalMirrorStrength *= AdvancedRocketryHelper.getInsolation(world, getPos());
        double humidityBonus = 0.075 * totalMirrorStrength * -((world.getBiome(getPos()).getRainfall() - 0.5) / 0.5);
        if (ITCompatModule.isAdvancedRocketryLoaded) humidityBonus *= AdvancedRocketryHelper.getWaterPartialPressureMultiplier(world, getPos());
        totalMirrorStrength += humidityBonus;

        reflectorStrength = totalMirrorStrength;
    }

    private boolean heatUp() {
        double previous = heatLevel;
        heatLevel = Math.min(heatLevel + getTemperatureIncrease(), workingHeatLevel);
        return previous != heatLevel;
    }

    private float getTemperatureIncrease() {
        return speedMult * (1 + (solarIncidenceAngleSection - 1)) * 10 * (float)(reflectorStrength / maximumReflectorStrength) * (world.isRaining() ? 0.1f : world.isThundering() ? 0.05f : 1f);
    }

    private boolean cooldown() {
        double previous = heatLevel;
        double heatLost = world.getBiomeProvider().getTemperatureAtHeight(world.getBiome(getPos()).getTemperature(getPos()), getPos().getY());
        double conduction = 1.0;
        if (ITCompatModule.isAdvancedRocketryLoaded) conduction *= AdvancedRocketryHelper.getHeatTransferCoefficient(world, getPos());
        heatLevel = Math.max(heatLevel - ((world.isRaining() ? 2 : 1) * (1 / heatLost) * heatLossMultiplier * conduction), 0);
        return previous != heatLevel;
    }

    private boolean loseProgress() {
        if (cachedSolarMelterRecipe == null || !cachedSolarMelterRecipe.itemInput.matches(inventory.get(0))) {
            cachedSolarMelterRecipe = null;
            processEnergyRemaining = 0;
            return true;
        }
        int previous = processEnergyRemaining;
        double coeff = ITCompatModule.isAdvancedRocketryLoaded ? AdvancedRocketryHelper.getHeatTransferCoefficient(world, getPos()) : 1;
        processEnergyRemaining = (int)Math.min(processEnergyRemaining + energyLossPerTick * coeff, cachedSolarMelterRecipe.getTotalProcessEnergy());
        return previous != processEnergyRemaining;
    }

    private boolean gainProgress() {
        if (cachedSolarMelterRecipe == null || !cachedSolarMelterRecipe.itemInput.matches(inventory.get(0))) {
            cachedSolarMelterRecipe = null;
            processEnergyRemaining = 0;
            return true;
        }
        int previous = processEnergyRemaining;
        processEnergyRemaining -= (int)(solarIncidenceAngleSection * 7680 * (reflectorStrength / maximumReflectorStrength));
        boolean changed = previous != processEnergyRemaining;
        if (processEnergyRemaining <= 0) {
            inventory.get(0).shrink(cachedSolarMelterRecipe.itemInput.inputSize);
            if (inventory.get(0).getCount() <= 0) inventory.set(0, ItemStack.EMPTY);
            tanks[0].fill(new FluidStack(cachedSolarMelterRecipe.fluidOutput.getFluid(), cachedSolarMelterRecipe.fluidOutput.amount), true);
            cachedSolarMelterRecipe = null;
            processEnergyRemaining = 0;
            changed = true;
        }
        return changed;
    }

    private boolean pumpOutputOut() {
        boolean changed = false;
        FluidStack out = tanks[0].getFluid();
        if (out != null && out.amount > 0 && fluidOutputFront0 != null) {
            IFluidHandler handler = FluidUtil.getFluidHandler(world, fluidOutputFront0, fluidOutput0.facing.getOpposite());
            if (handler != null) {
                FluidStack sim = out.copy();
                int accepted = handler.fill(sim, false);
                if (accepted > 0) {
                    FluidStack push = Utils.copyFluidStackWithAmount(out, accepted, false);
                    int pushed = handler.fill(push, true);
                    tanks[0].drain(pushed, true);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private boolean outputTankLogic() {
        boolean update = false;
        FluidStack fluid = tanks[0].getFluid();
        if (fluid != null && fluid.amount > 0) {
            ItemStack filled = Utils.fillFluidContainer(tanks[0], inventory.get(1), inventory.get(2), null);
            if (!filled.isEmpty()) {
                if (!inventory.get(2).isEmpty() && OreDictionary.itemMatches(inventory.get(2), filled, true)) inventory.get(2).grow(filled.getCount());
                else if (inventory.get(2).isEmpty()) inventory.set(2, filled.copy());
                inventory.get(1).shrink(1);
                if (inventory.get(1).getCount() <= 0) inventory.set(1, ItemStack.EMPTY);
                update = true;
            }
            if (pumpOutputOut()) update = true;
        }
        return update;
    }

    private boolean heatLogic() {
        boolean update = false;
        if (solarIncidenceAngleSection != 0) {
            if (heatUp()) update = true;
        } else {
            if (cooldown()) update = true;
        }
        return update;
    }

    private boolean recipeLogic() {
        boolean update = false;
        if (heatLevel >= workingHeatLevel && !isRSDisabled()) {
            if (processEnergyRemaining > 0) {
                if (gainProgress()) update = true;
            } else if (!inventory.get(0).isEmpty()) {
                MeltingCrucibleRecipe recipe = cachedSolarMelterRecipe;
                if (recipe == null || !recipe.itemInput.matches(inventory.get(0))) {
                    recipe = MeltingCrucibleRecipe.findRecipe(inventory.get(0));
                }
                if (recipe != null && recipe.fluidOutput.amount <= tanks[0].getCapacity() - tanks[0].getFluidAmount()) {
                    cachedSolarMelterRecipe = recipe;
                    processEnergyRemaining = recipe.getTotalProcessEnergy();
                    if (gainProgress()) update = true;
                }
            }
        } else if (processEnergyRemaining > 0) {
            if (loseProgress()) update = true;
        }
        return update;
    }

    @SideOnly(Side.CLIENT)
    private void clientUpdate() {
        if (soundPos0 == null) InitializePoIs();
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distSq = player.getDistanceSq(soundPos0.getX() + 0.5, soundPos0.getY() + 0.5, soundPos0.getZ() + 0.5);
        if (world.provider.getDimension() == player.dimension && distSq < 400 && (distanceSqToTE > 400 || playerDimension != player.dimension)) requestUpdate();
        distanceSqToTE = distSq;
        playerDimension = player.dimension;
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

    @Override public void update() {
        super.update();
        if (world.isRemote) { clientUpdate(); return; }
        if (!formed) return;
        if (needsPoIInit || redstone0 == null) { InitializePoIs(); needsPoIInit = false; }
        if (!isLoaded) {
            isLoaded = true;
            notifyIONeighbors();
            SolarRegistry.RegisterResult result = SolarRegistry.registerTower(world, basePos0);
            registered = result.success;
            if (!registered && savedRegistered) {
                int y = basePos0.getY();
                Set<BlockPos> towersAtY = SolarRegistry.getData(world).towerBasesByY.computeIfAbsent(y, k -> new HashSet<>());
                towersAtY.add(basePos0);
                SolarRegistry.getData(world).markDirty();
                registered = true;
            }
            if (registered) checkReflectorPositions();
        }
        if (reCheckOnLoad) {
            reCheckOnLoad = false;
            if (registered) checkReflectorPositions();
        }
        solarIncidenceAngleSection = computeSolarIncidenceAngleSection();
        boolean update = false;
        double oldRef = reflectorStrength;
        if (world.getTotalWorldTime() % 600 == 0) checkReflectorPositions();
        if (reflectorStrength != oldRef) update = true;
        update |= heatLogic();
        if (solarIncidenceAngleSection != 0) update |= recipeLogic();
        update |= outputTankLogic();

        boolean active = processEnergyRemaining > 0;
        if (active) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;
        boolean wasRunning = isRunning;
        isRunning = soundGracePeriod > 0;
        if (isRunning != wasRunning) notifyNearbyClients();

        int comp = getComparatorInputOverride();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            world.notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
            update = true;
        }

        if (update || isRunning != wasRunning) {
            efficientMarkDirty();
            markContainingBlockForUpdate(null);
        }
    }

    @Override public void TankContentsChanged() {
        markContainingBlockForUpdate(null);
        efficientMarkDirty();
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntitySolarMelterMaster master() { return this; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (redstone0 == null) InitializePoIs();
        if (side == null) return tanks;
        if (fluidOutput0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) { return false; }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (fluidOutput0 == null) InitializePoIs();
        return iTank == 0 && fluidOutput0.isPoI(side, position) && tanks[0].getFluidAmount() > 0;
    }

    @Nonnull protected IItemHandler[] getAccessibleItemHandlers(@Nullable EnumFacing side, int position) {
        if (!formed) return new IItemHandler[0];
        if (redstone0 == null) InitializePoIs();
        if (side != null && itemInput0.isPoI(side, position)) return new IItemHandler[] {insertionHandler};
        return new IItemHandler[0];
    }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstone0 == null) InitializePoIs();
        return new int[] {redstone0.position};
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartSolarMelter.instance.pointsOfInterest) {
            switch (poi.name) {
                case "fluid_output0":
                    fluidOutput0 = new PoICache(facing, poi, mirrored);
                    fluidOutputFront0 = getBlockPosForPos(fluidOutput0.position).offset(fluidOutput0.facing);
                    break;
                case "item_input0":
                    itemInput0 = new PoICache(facing, poi, mirrored);
                    break;
                case "redstone0":
                    redstone0 = new PoICache(facing, poi, mirrored);
                    break;
                case "sound0":
                    soundPos0 = getBlockPosForPos(poi.position);
                    break;
                case "link0":
                    basePos0 = getBlockPosForPos(new PoICache(facing, poi, mirrored).position);
                    break;
                case "collector0":
                    collectorPos0 = getBlockPosForPos(new PoICache(facing, poi, mirrored).position);
                    break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(fluidOutput0.position));
        notifyNeighbor(getBlockPosForPos(itemInput0.position));
        notifyNeighbor(getBlockPosForPos(redstone0.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override @Nonnull public NonNullList<ItemStack> getInventory() { return inventory; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) {
        markDirty();
        markContainingBlockForUpdate(null);
    }

    @Override @Nonnull public NonNullList<ItemStack> getDroppedItems() { return getInventory(); }

    @Override public int getComparatedSize() { return slotCount; }

    @Override public int getComparatorInputOverride() {
        return (int)(15 * heatLevel / workingHeatLevel);
    }
}
