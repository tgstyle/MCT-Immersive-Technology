package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;

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

public class TileEntitySolarMelterMaster extends TileEntitySolarMelterSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver {

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

    public MeltingCrucibleRecipe cachedRecipe;

    public static int slotCount = 3;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    public int recipeEnergyRemaining = 0;
    public double heatLevel = 0;
    public double reflectorStrength = 0;
    public int solarIncidenceAngleSection = 0;
    private int clientUpdateCooldown = 20;
    private float soundVolume;
    private boolean isRunning;
    private int gracePeriod = 60;
    private PoICache fluidOutput0, itemInput0, redstone0;
    private BlockPos basePos0, collectorPos0, fluidOutputFront0, soundPos0;
    private boolean isLoaded = false;
    private boolean registered = false;
    private boolean reCheckOnLoad = false;
    private boolean savedRegistered = false;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        recipeEnergyRemaining = nbt.getInteger("recipeEnergyRemaining");
        heatLevel = nbt.getDouble("heatLevel");
        reflectorStrength = nbt.getDouble("reflectorStrength");
        if (!descPacket) {
            inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
            if (recipeEnergyRemaining > 0 && !inventory.get(0).isEmpty()) {
                cachedRecipe = MeltingCrucibleRecipe.findRecipe(inventory.get(0));
                if (cachedRecipe == null) recipeEnergyRemaining = 0;
            }
        }
        registered = nbt.getBoolean("registered");
        savedRegistered = nbt.getBoolean("savedRegistered");
        reCheckOnLoad = nbt.getBoolean("reCheckOnLoad");
        isLoaded = nbt.getBoolean("isLoaded");
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setInteger("recipeEnergyRemaining", recipeEnergyRemaining);
        nbt.setDouble("heatLevel", heatLevel);
        nbt.setDouble("reflectorStrength", reflectorStrength);
        if (!descPacket) { nbt.setTag("inventory", Utils.writeInventory(inventory)); }
        nbt.setBoolean("registered", registered);
        nbt.setBoolean("savedRegistered", savedRegistered);
        nbt.setBoolean("reCheckOnLoad", reCheckOnLoad);
        nbt.setBoolean("isLoaded", isLoaded);
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (isRunning) { if (soundVolume < 1) soundVolume += 0.02f; }
        else { if (soundVolume > 0) soundVolume -= 0.02f; }
        if (soundVolume == 0) { ITSoundHandler.StopSound(soundPos0); }
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos0.getX(), soundPos0.getY(), soundPos0.getZ()) / 8, 1);
            ITSounds.heatExchanger.PlayRepeating(soundPos0, soundVolume / attenuation, 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() { ITSoundHandler.StopSound(soundPos0); super.onChunkUnload(); }

    @Override public void disassemble() {
        if (soundPos0 != null) {
            ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
        }
        if (!world.isRemote) {
            for (ItemStack stack : inventory) {
                if (!stack.isEmpty()) {
                    Utils.dropStackAtPos(world, getPos(), stack);
                }
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

    public void notifyNearbyClients() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeDouble(heatLevel);
        buf.writeInt(solarIncidenceAngleSection);
        buf.writeBoolean(isRunning);
        BinaryMessageTileSync.sendToAllTracking(world, getPos(), buf);
    }

    @Override public void receiveMessageFromServer(ByteBuf message) {
        heatLevel = message.readDouble();
        solarIncidenceAngleSection = message.readInt();
        isRunning = message.getBoolean(0);
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {}

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
        if (ITCompatModule.isAdvancedRocketryLoaded) { totalMirrorStrength *= AdvancedRocketryHelper.getInsolation(world, getPos()); }
        double humidityBonus = 0.075 * totalMirrorStrength * -((world.getBiome(getPos()).getRainfall() - 0.5) / 0.5);
        if (ITCompatModule.isAdvancedRocketryLoaded) { humidityBonus *= AdvancedRocketryHelper.getWaterPartialPressureMultiplier(world, getPos()); }
        totalMirrorStrength += humidityBonus;

        reflectorStrength = totalMirrorStrength;
    }

    private boolean heatUp() {
        double previous = heatLevel;
        heatLevel = Math.min(heatLevel + getTemperatureIncrease(), workingHeatLevel);
        return previous != heatLevel;
    }

    private float getTemperatureIncrease() { return speedMult * (1 + (solarIncidenceAngleSection - 1)) * 10 * (float)(reflectorStrength / maximumReflectorStrength) * (world.isRaining() ? 0.1f : world.isThundering() ? 0.05f : 1f); }

    private boolean cooldown() {
        double previous = heatLevel;
        double heatLost = world.getBiomeProvider().getTemperatureAtHeight(world.getBiome(getPos()).getTemperature(getPos()), getPos().getY());
        double conduction = 1.0;
        if (ITCompatModule.isAdvancedRocketryLoaded) { conduction *= AdvancedRocketryHelper.getHeatTransferCoefficient(world, getPos().add(0, 19, 0)); }
        heatLevel = Math.max(heatLevel - ((world.isRaining() ? 2 : 1 * (1 / heatLost)) * heatLossMultiplier * conduction), 0);
        return previous != heatLevel;
    }

    private boolean loseProgress() {
        if (cachedRecipe == null || !cachedRecipe.itemInput.matches(inventory.get(0))) { cachedRecipe = null; recipeEnergyRemaining = 0; return true; }
        int previous = recipeEnergyRemaining;
        recipeEnergyRemaining = (int)Math.min(recipeEnergyRemaining + energyLossPerTick * (ITCompatModule.isAdvancedRocketryLoaded ? AdvancedRocketryHelper.getHeatTransferCoefficient(world, getPos()) : 1), cachedRecipe.getTotalProcessEnergy());
        return previous != recipeEnergyRemaining;
    }

    private boolean gainProgress() {
        if (cachedRecipe == null || !cachedRecipe.itemInput.matches(inventory.get(0))) { cachedRecipe = null; recipeEnergyRemaining = 0; return true; }
        int previous = recipeEnergyRemaining;
        recipeEnergyRemaining -= (int)(solarIncidenceAngleSection * 7680 * (reflectorStrength / maximumReflectorStrength));
        boolean changed = previous != recipeEnergyRemaining;
        if (recipeEnergyRemaining <= 0) {
            inventory.get(0).shrink(cachedRecipe.itemInput.inputSize);
            if (inventory.get(0).getCount() <= 0) { inventory.set(0, ItemStack.EMPTY); }
            tanks[0].fillInternal(new FluidStack(cachedRecipe.fluidOutput.getFluid(), cachedRecipe.fluidOutput.amount), true);
            cachedRecipe = null;
            recipeEnergyRemaining = 0;
            markContainingBlockForUpdate(null);
            changed = true;
        }
        return changed;
    }

    private boolean pumpOutputOut() {
        if (tanks[0].getFluidAmount() == 0) return false;
        IFluidHandler handler = FluidUtil.getFluidHandler(world, fluidOutputFront0, fluidOutput0.facing.getOpposite());
        if (handler == null) return false;
        FluidStack out = tanks[0].getFluid();
        int accepted = handler.fill(out, false);
        if (accepted == 0) return false;
        assert out != null;
        int drained = handler.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        tanks[0].drain(drained, true);
        return drained > 0;
    }

    private boolean outputTankLogic() {
        boolean update = false;
        if (tanks[0].getFluidAmount() > 0) {
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
        if (solarIncidenceAngleSection != 0) { if (heatUp()) update = true; }
        else { if (cooldown()) update = true; }
        return update;
    }

    private boolean recipeLogic() {
        boolean update = false;
        if (heatLevel >= workingHeatLevel && !isRSDisabled()) {
            if (recipeEnergyRemaining > 0) { if (gainProgress()) update = true; }
            else if (!inventory.get(0).isEmpty()) {
                MeltingCrucibleRecipe recipe = cachedRecipe;
                if (recipe == null || !recipe.itemInput.matches(inventory.get(0))) recipe = MeltingCrucibleRecipe.findRecipe(inventory.get(0));
                if (recipe != null && recipe.fluidOutput.amount <= tanks[0].getCapacity() - tanks[0].getFluidAmount()) {
                    cachedRecipe = recipe;
                    recipeEnergyRemaining = recipe.getTotalProcessEnergy();
                    if (gainProgress()) update = true;
                }
            }
        } else if (recipeEnergyRemaining > 0) { if (loseProgress()) update = true; }
        return update;
    }

    @Override public void update() {
        if (formed && redstone0 == null) InitializePoIs();
        super.update();
        if (!formed) return;
        if (world.isRemote) { handleSounds(); return; }
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
        if (world.getTotalWorldTime() % 600 == 0) checkReflectorPositions();
        if (heatLogic()) update = true;
        if (solarIncidenceAngleSection != 0 && recipeLogic()) update = true;
        if (outputTankLogic()) update = true;
        if (recipeEnergyRemaining > 0) {
            isRunning = true;
            gracePeriod = 60;
        } else {
            if (gracePeriod > 0) gracePeriod--;
            else isRunning = false;
        }
        clientUpdateCooldown--;
        if (clientUpdateCooldown <= 0) {
            notifyNearbyClients();
            clientUpdateCooldown = 20;
        }
        if (update) {
            efficientMarkDirty();
            markContainingBlockForUpdate(null);
        }
    }

    @Override public void TankContentsChanged() { markContainingBlockForUpdate(null); }

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
        if (!formed || redstone0 == null) InitializePoIs();
        return fluidOutput0.isPoI(side, position) && tanks[0].getFluidAmount() > 0;
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

    private int computeSolarIncidenceAngleSection() {
        int light = world.getSkylightSubtracted();
        if (light == 3) return 1;
        if (light == 2) return 2;
        if (light == 1) return 3;
        if (light == 0) return 4;
        return 0;
    }

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

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }
}
