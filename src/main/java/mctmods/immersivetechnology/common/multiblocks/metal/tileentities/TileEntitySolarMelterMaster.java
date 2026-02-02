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
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
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
    private static final double heatLossMultiplier = Multiblocks.solarMelter.solarMelter_heat_loss_multiplier;
    private static final float speedMult = Multiblocks.solarMelter.solarMelter_speed_multiplier;
    private static final double workingHeatLevel = Multiblocks.solarMelter.solarMelter_heat_workingLevel;
    private static final double maximumReflectorStrength = Multiblocks.solarMelter.solarMelter_maximum_reflector_strength;
    private static final int progressResolution = 64;

    public FluidTank[] tanks = new FluidTank[] { new ITFluidTank(outputTankSize, this) };

    public static int slotCount = 3;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);

    public int processTimeRemaining = 0;
    public double heatLevel = 0;
    public double reflectorStrength = 0;
    public int solarIncidenceAngleSection = 0;

    public MeltingCrucibleRecipe cachedSolarMelterRecipe;

    private float soundVolume = 0f;
    private int soundGracePeriod = 0;
    private boolean isRunning = false;
    private boolean isLoaded = false;
    private boolean registered = false;
    private boolean reCheckOnLoad = false;
    private boolean savedRegistered = false;

    private double distanceSqToTE;
    private int playerDimension;
    private int clientSyncTimer = 0;
    private int oldComparatorOutput = 0;

    PoICache fluidOutputPos0;
    PoICache itemInputPos0;
    PoICache redstonePos0;

    BlockPos basePos0;
    BlockPos collectorPos0;
    BlockPos fluidOutputTEPos0;
    BlockPos soundPos0;

    private boolean needsPoIInit = true;
    private boolean needsNotify = false;

    public IItemHandler insertionHandler = new IEInventoryHandler(1, this, 0, new boolean[]{true}, new boolean[]{false});

    public void efficientMarkDirty() {
        world.getChunk(getPos()).markDirty();
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        processTimeRemaining = nbt.getInteger("processTimeRemaining");
        heatLevel = nbt.getDouble("heatLevel");
        reflectorStrength = nbt.getDouble("reflectorStrength");
        soundGracePeriod = nbt.getInteger("soundGracePeriod");
        inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
        if (!descPacket) {
            if (processTimeRemaining > 0 && !inventory.get(0).isEmpty()) {
                cachedSolarMelterRecipe = MeltingCrucibleRecipe.findRecipe(inventory.get(0));
                if (cachedSolarMelterRecipe == null) processTimeRemaining = 0;
            }
            reCheckOnLoad = true;
        }
        registered = nbt.getBoolean("registered");
        savedRegistered = nbt.getBoolean("savedRegistered");
        reCheckOnLoad = nbt.getBoolean("reCheckOnLoad");
        isLoaded = nbt.getBoolean("isLoaded");
        if (formed) {
            needsPoIInit = true;
            needsNotify = true;
        }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setInteger("processTimeRemaining", processTimeRemaining);
        nbt.setDouble("heatLevel", heatLevel);
        nbt.setDouble("reflectorStrength", reflectorStrength);
        nbt.setInteger("soundGracePeriod", soundGracePeriod);
        nbt.setTag("inventory", Utils.writeInventory(inventory));
        nbt.setBoolean("registered", registered);
        nbt.setBoolean("savedRegistered", savedRegistered);
        nbt.setBoolean("reCheckOnLoad", reCheckOnLoad);
        nbt.setBoolean("isLoaded", isLoaded);
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (needsPoIInit) InitializePoIs();
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
        if (soundVolume <= 0f) ITSoundHandler.StopSound(soundPos0);
        else {
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
            for (ItemStack stack : inventory) if (!stack.isEmpty()) Utils.dropStackAtPos(world, getPos(), stack.copy());
            inventory.clear();
            detachMirrors();
            SolarRegistry.unregisterTower(world, basePos0);
        }
        super.disassemble();
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

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartSolarMelter.instance.pointsOfInterest) {
            if (poi == null) continue;
            switch (poi.name) {
                case "fluid_output0":
                    fluidOutputPos0 = new PoICache(facing, poi, mirrored);
                    fluidOutputTEPos0 = getBlockPosForPos(fluidOutputPos0.position).offset(fluidOutputPos0.facing);
                    break;
                case "item_input0":
                    itemInputPos0 = new PoICache(facing, poi, mirrored);
                    break;
                case "redstone0":
                    redstonePos0 = new PoICache(facing, poi, mirrored);
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
        if (!world.isRemote) needsNotify = true;
    }

    private void notifyIONeighbors() {
        if (fluidOutputPos0 != null) world.notifyNeighborsOfStateChange(getBlockPosForPos(fluidOutputPos0.position), getBlockType(), true);
        if (itemInputPos0 != null) world.notifyNeighborsOfStateChange(getBlockPosForPos(itemInputPos0.position), getBlockType(), true);
        if (redstonePos0 != null) world.updateComparatorOutputLevel(getBlockPosForPos(redstonePos0.position), getBlockType());
        needsNotify = false;
    }

    @SideOnly(Side.CLIENT)
    private void clientUpdate() {
        if (soundPos0 == null) InitializePoIs();
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distSq = player.getDistanceSq(soundPos0.getX() + 0.5, soundPos0.getY() + 0.5, soundPos0.getZ() + 0.5);
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
        Set<BlockPos> reflectors = SolarRegistry.getReflectorsInRange(world, basePos0, solarMinRange, solarMaxRange);
        for (BlockPos pos : reflectors) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntitySolarReflectorSlave) {
                TileEntitySolarReflectorMaster ref = ((TileEntitySolarReflectorSlave)tile).master();
                if (ref != null) ref.detachTower();
            }
        }
    }

    private void checkReflectorPositions() {
        Set<BlockPos> reflectors = SolarRegistry.getReflectorsInRange(world, basePos0, solarMinRange, solarMaxRange);
        for (BlockPos pos : reflectors) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntitySolarReflectorSlave) {
                TileEntitySolarReflectorMaster ref = ((TileEntitySolarReflectorSlave)tile).master();
                if (ref != null && ref.isMirrorTaken && !ref.getCollectorPosition().equals(collectorPos0)) ref.detachTower();
            }
        }
        double totalMirrorStrength = 0;
        for (BlockPos pos : reflectors) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntitySolarReflectorSlave) {
                TileEntitySolarReflectorMaster ref = ((TileEntitySolarReflectorSlave)tile).master();
                if (ref != null && ref.setTowerCollectorPosition(collectorPos0)) totalMirrorStrength += ref.getSolarCollectorStrength();
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
        double heatLost = world.getBiomeProvider().getTemperatureAtHeight(world.getBiome(collectorPos0).getTemperature(collectorPos0), collectorPos0.getY());
        if (heatLost <= 0) heatLost = 0.1;
        double conduction = 1.0;
        if (ITCompatModule.isAdvancedRocketryLoaded) conduction *= AdvancedRocketryHelper.getHeatTransferCoefficient(world, collectorPos0);
        heatLevel = Math.max(heatLevel - ((world.isRaining() ? 2 : 1) * (1 / heatLost) * heatLossMultiplier * conduction), 0);
        return previous != heatLevel;
    }

    private boolean heatLogic() {
        boolean update = false;
        float increase = getTemperatureIncrease();
        if (increase > 0) {
            if (heatUp()) update = true;
        } else {
            if (cooldown()) update = true;
        }
        return update;
    }

    private boolean recipeLogic() {
        boolean update = false;
        boolean didWork = false;
        boolean shouldRun = !isRSDisabled();
        if (processTimeRemaining == 0 && shouldRun && heatLevel >= workingHeatLevel) {
            ItemStack inputStack = inventory.get(0);
            if (!inputStack.isEmpty()) {
                MeltingCrucibleRecipe recipe = MeltingCrucibleRecipe.findRecipe(inputStack);
                if (recipe != null && inputStack.getCount() >= recipe.itemInput.inputSize && tanks[0].fill(recipe.fluidOutput, false) == recipe.fluidOutput.amount) {
                    cachedSolarMelterRecipe = recipe;
                    inputStack.shrink(recipe.itemInput.inputSize);
                    if (inputStack.getCount() <= 0) inventory.set(0, ItemStack.EMPTY);
                    doGraphicalUpdates(0);
                    processTimeRemaining = recipe.getTotalProcessTime() * progressResolution;
                    update = true;
                }
            }
        }
        if (processTimeRemaining > 0 && shouldRun) {
            if (cachedSolarMelterRecipe != null) {
                int prev = processTimeRemaining;
                if (heatLevel >= workingHeatLevel) {
                    processTimeRemaining -= progressResolution;
                    didWork = true;
                }
                if (prev != processTimeRemaining) update = true;
                if (processTimeRemaining <= 0) {
                    processTimeRemaining = 0;
                    tanks[0].fill(cachedSolarMelterRecipe.fluidOutput, true);
                    cachedSolarMelterRecipe = null;
                }
            } else processTimeRemaining = 0;
        }
        if (didWork) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;
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
            doGraphicalUpdates(1);
            doGraphicalUpdates(2);
            update = true;
        }
        ItemStack drained = Utils.drainFluidContainer(tanks[0], inventory.get(1), inventory.get(2), null);
        if (!drained.isEmpty()) {
            if (!inventory.get(2).isEmpty() && OreDictionary.itemMatches(inventory.get(2), drained, true)) inventory.get(2).grow(drained.getCount());
            else if (inventory.get(2).isEmpty()) inventory.set(2, drained.copy());
            inventory.get(1).shrink(1);
            if (inventory.get(1).getCount() <= 0) inventory.set(1, ItemStack.EMPTY);
            doGraphicalUpdates(1);
            doGraphicalUpdates(2);
            update = true;
        }
        if (pumpOutputOut()) update = true;
        return update;
    }

    private boolean pumpOutputOut() {
        boolean changed = false;
        FluidStack out = tanks[0].getFluid();
        if (out != null && out.amount > 0 && fluidOutputTEPos0 != null && fluidOutputPos0 != null) {
            IFluidHandler handler = FluidUtil.getFluidHandler(world, fluidOutputTEPos0, fluidOutputPos0.facing.getOpposite());
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

    @Override public void update() {
        super.update();
        if (!formed) return;
        if (needsPoIInit || redstonePos0 == null) {
            InitializePoIs();
            needsPoIInit = false;
        }
        if (needsNotify) notifyIONeighbors();
        if (world.isRemote) {
            clientUpdate();
            return;
        }
        solarIncidenceAngleSection = computeSolarIncidenceAngleSection();
        boolean update = false;
        if (!isLoaded) {
            isLoaded = true;
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
        boolean wasRunning = isRunning;
        isRunning = soundGracePeriod > 0;
        if (isRunning != wasRunning) notifyNearbyClients();
        int comp = getComparatorInputOverride();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            update = true;
        }
        if (update) {
            efficientMarkDirty();
            markContainingBlockForUpdate(null);
        }
    }

    @Override public void TankContentsChanged() {
        efficientMarkDirty();
        markContainingBlockForUpdate(null);
    }

    @Override public boolean isRSDisabled() {
        if (redstonePos0 == null) return false;
        int power = world.getRedstonePowerFromNeighbors(getBlockPosForPos(redstonePos0.position));
        return power > 0;
    }

    @Override public int getComparatorInputOverride() {
        return (int)(15 * heatLevel / workingHeatLevel);
    }

    @Override public boolean isDummy() {
        return false;
    }

    @Override public TileEntitySolarMelterMaster master() {
        return this;
    }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (fluidOutputPos0 == null) InitializePoIs();
        if (side != null && fluidOutputPos0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        return false;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (fluidOutputPos0 == null) InitializePoIs();
        return iTank == 0 && fluidOutputPos0.isPoI(side, position) && tanks[0].getFluidAmount() > 0;
    }

    @Nonnull protected IItemHandler[] getAccessibleItemHandlers(@Nullable EnumFacing side, int position) {
        if (!formed) return new IItemHandler[0];
        if (side != null && itemInputPos0 != null && itemInputPos0.isPoI(side, position)) return new IItemHandler[] {insertionHandler};
        return new IItemHandler[0];
    }

    @Override @Nonnull public int[] getRedstonePos() {
        if (redstonePos0 == null) InitializePoIs();
        return new int[] {redstonePos0.position};
    }

    @Override @Nonnull public int[] getOutputTanks() {
        return new int[] {0};
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() {
        return new int[0];
    }

    @Override @Nonnull public int[] getCurrentProcessesMax() {
        return new int[0];
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
        return inventory;
    }

    @Override public int getComparatedSize() {
        return slotCount;
    }

    public static class SolarMelterFluidHandler implements IFluidHandler {
        private final TileEntitySolarMelterSlave te;
        private final EnumFacing facing;
        private final IFluidTank[] tanks;
        private final int position;

        public SolarMelterFluidHandler(TileEntitySolarMelterSlave te, EnumFacing facing) {
            this.te = te;
            this.facing = facing;
            TileEntitySolarMelterMaster master = te.master();
            this.tanks = master != null ? master.getAccessibleFluidTanks(facing, te.pos) : new IFluidTank[0];
            this.position = te.pos;
        }

        @Override public IFluidTankProperties[] getTankProperties() {
            java.util.List<net.minecraftforge.fluids.capability.IFluidTankProperties> props = new java.util.ArrayList<>(tanks.length);
            TileEntitySolarMelterMaster master = te.master();
            if (master != null) {
                for (int i = 0; i < tanks.length; i++) {
                    boolean canDrain = master.canDrainTankFrom(i, facing, position);
                    props.add(new FluidTankProperties(tanks[i].getFluid(), tanks[i].getCapacity(), false, canDrain));
                }
            }
            return props.toArray(new IFluidTankProperties[0]);
        }

        @Override public int fill(FluidStack resource, boolean doFill) {
            return 0;
        }

        @Override public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.amount <= 0) return null;
            TileEntitySolarMelterMaster master = te.master();
            if (master == null) return null;
            for (int i = 0; i < tanks.length; i++) {
                if (master.canDrainTankFrom(i, facing, position)) {
                    FluidStack tankFluid = tanks[i].getFluid();
                    if (tankFluid != null && tankFluid.isFluidEqual(resource)) {
                        FluidStack drained = tanks[i].drain(resource.amount, doDrain);
                        if (drained != null && drained.amount > 0 && doDrain) master.efficientMarkDirty();
                        return drained;
                    }
                }
            }
            return null;
        }

        @Override public FluidStack drain(int maxDrain, boolean doDrain) {
            if (maxDrain <= 0) return null;
            TileEntitySolarMelterMaster master = te.master();
            if (master == null) return null;
            for (int i = 0; i < tanks.length; i++) {
                if (master.canDrainTankFrom(i, facing, position)) {
                    FluidStack drained = tanks[i].drain(maxDrain, doDrain);
                    if (drained != null && drained.amount > 0 && doDrain) master.efficientMarkDirty();
                    return drained;
                }
            }
            return null;
        }
    }
}
