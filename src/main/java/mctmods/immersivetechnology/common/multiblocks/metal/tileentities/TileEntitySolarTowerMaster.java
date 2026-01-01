package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.crafting.SolarTowerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSolarTower;
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
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TileEntitySolarTowerMaster extends TileEntitySolarTowerSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver {

    private static final int inputTankSize = Multiblocks.solarTower.solarTower_input_tankSize;
    private static final int outputTankSize = Multiblocks.solarTower.solarTower_output_tankSize;
    private static final int solarMaxRange = Multiblocks.solarReflector.solarReflector_maxRange;
    private static final int solarMinRange = Multiblocks.solarReflector.solarReflector_minRange;
    private static final int progressLossPerTick = Multiblocks.solarTower.solarTower_progress_lossInTicks;
    private static final double heatLossMultiplier = Multiblocks.solarTower.solarTower_heat_loss_multiplier;
    private static final float speedMult = Multiblocks.solarTower.solarTower_speed_multiplier;
    private static final double workingHeatLevel = Multiblocks.solarTower.solarTower_heat_workingLevel;
    private static final double maximumReflectorStrength = Multiblocks.solarTower.solarTower_maximum_reflector_strength;

    public FluidTank[] tanks = new FluidTank[] {
            new ITFluidTank(inputTankSize, this),
            new ITFluidTank(outputTankSize, this)
    };

    public static int slotCount = 4;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    public int recipeTimeRemaining = 0;
    public double heatLevel = 0;
    public double reflectorStrength = 0;
    public int solarIncidenceAngleSection = 0;
    private int clientUpdateCooldown = 20;
    private float soundVolume = 0;
    private boolean isRunning;
    private int gracePeriod = 60;
    public SolarTowerRecipe cachedRecipe;
    private PoICache redstone0, fluidInput0, fluidOutput0;
    private BlockPos basePos, collectorPos0, fluidOutputFront0, soundPos0;
    private boolean isLoaded = false;
    private boolean registered = false;
    private boolean reCheckOnLoad = false;
    private boolean savedRegistered = false;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        heatLevel = nbt.getDouble("heatLevel");
        recipeTimeRemaining = nbt.getInteger("recipeTimeRemaining");
        reflectorStrength = nbt.getDouble("reflectorStrength");
        solarIncidenceAngleSection = nbt.getInteger("solarIncidenceAngleSection");
        if (!descPacket) { inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount); }
        registered = nbt.getBoolean("registered");
        savedRegistered = nbt.getBoolean("savedRegistered");
        reCheckOnLoad = nbt.getBoolean("reCheckOnLoad");
        isLoaded = nbt.getBoolean("isLoaded");
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setDouble("heatLevel", heatLevel);
        nbt.setInteger("recipeTimeRemaining", recipeTimeRemaining);
        nbt.setDouble("reflectorStrength", reflectorStrength);
        nbt.setInteger("solarIncidenceAngleSection", solarIncidenceAngleSection);
        if (!descPacket) { nbt.setTag("inventory", Utils.writeInventory(inventory)); }
        nbt.setBoolean("registered", registered);
        nbt.setBoolean("savedRegistered", savedRegistered);
        nbt.setBoolean("reCheckOnLoad", reCheckOnLoad);
        nbt.setBoolean("isLoaded", isLoaded);
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (isRunning) { if (soundVolume < 1) soundVolume += 0.01f; }
        else { if (soundVolume > 0) soundVolume -= 0.01f; }
        if (soundVolume <= 0) { ITSoundHandler.StopSound(soundPos0); }
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos0.getX(), soundPos0.getY(), soundPos0.getZ()) / 8, 1);
            ITSounds.solarTower.PlayRepeating(soundPos0, (2 * soundVolume) / attenuation, soundVolume);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() { ITSoundHandler.StopSound(soundPos0); super.onChunkUnload(); }

    @Override public void disassemble() {
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
        detachMirrors();
        SolarRegistry.unregisterTower(world, basePos);
        super.disassemble();
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
        isRunning = message.readBoolean();
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {}

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    private void checkReflectorPositions() {
        double totalMirrorStrength = 0;
        Set<BlockPos> reflectors = SolarRegistry.getReflectorsInRange(world, basePos, solarMinRange, solarMaxRange);
        for (BlockPos pos : reflectors) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntitySolarReflectorMaster) {
                TileEntitySolarReflectorMaster master = (TileEntitySolarReflectorMaster)tile;
                if (master.setTowerCollectorPosition(collectorPos0)) { totalMirrorStrength += master.getSolarCollectorStrength(); }
            }
        }
        totalMirrorStrength *= world.isRaining() ? 0.4 : 1;
        if (ITCompatModule.isAdvancedRocketryLoaded) { totalMirrorStrength *= AdvancedRocketryHelper.getInsolation(world, getPos()); }
        double humidityBonus = 0.075 * totalMirrorStrength * -((world.getBiome(getPos()).getRainfall() - 0.5) / 0.5);
        if (ITCompatModule.isAdvancedRocketryLoaded) { humidityBonus *= AdvancedRocketryHelper.getWaterPartialPressureMultiplier(world, getPos()); }
        totalMirrorStrength += humidityBonus;
        reflectorStrength = totalMirrorStrength;
    }

    private void detachMirrors() {
        Set<BlockPos> reflectors = SolarRegistry.getReflectorsInRange(world, basePos, solarMinRange, solarMaxRange);
        for (BlockPos pos : reflectors) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntitySolarReflectorMaster) { ((TileEntitySolarReflectorMaster)tile).detachTower(collectorPos0); }
        }
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
        if (cachedRecipe == null) { recipeTimeRemaining = 0; return true; }
        int previous = recipeTimeRemaining;
        recipeTimeRemaining = Math.min(recipeTimeRemaining + progressLossPerTick, cachedRecipe.getTotalProcessTime());
        return previous != recipeTimeRemaining;
    }

    private boolean gainProgress() {
        if (cachedRecipe == null) { recipeTimeRemaining = 0; return true; }
        recipeTimeRemaining--;
        if (recipeTimeRemaining == 0) {
            tanks[0].drain((int)(cachedRecipe.fluidInput.amount * reflectorStrength / maximumReflectorStrength), true);
            tanks[1].fill(new FluidStack(cachedRecipe.fluidOutput.getFluid(), (int)(cachedRecipe.fluidOutput.amount * reflectorStrength / maximumReflectorStrength)), true);
            markContainingBlockForUpdate(null);
            return true;
        }
        return false;
    }

    private boolean pumpOutputOut() {
        if (tanks[1].getFluidAmount() == 0) { return false; }
        IFluidHandler handler = FluidUtil.getFluidHandler(world, fluidOutputFront0, fluidOutput0.facing.getOpposite());
        if (handler == null) { return false; }
        FluidStack out = tanks[1].getFluid();
        int accepted = handler.fill(out, false);
        if (accepted == 0) { return false; }
        assert out != null;
        int drained = handler.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        tanks[1].drain(drained, true);
        return drained > 0;
    }

    private boolean outputTankLogic() {
        boolean update = false;
        if (tanks[1].getFluidAmount() > 0) {
            ItemStack filled = Utils.fillFluidContainer(tanks[1], inventory.get(2), inventory.get(3), null);
            if (!filled.isEmpty()) {
                if (!inventory.get(3).isEmpty() && OreDictionary.itemMatches(inventory.get(3), filled, true)) { inventory.get(3).grow(filled.getCount()); }
                else if (inventory.get(3).isEmpty()) { inventory.set(3, filled.copy()); }
                inventory.get(2).shrink(1);
                if (inventory.get(2).getCount() <= 0) { inventory.set(2, ItemStack.EMPTY); }
                markContainingBlockForUpdate(null);
                update = true;
            }
            if (pumpOutputOut()) { update = true; }
        }
        return update;
    }

    private boolean inputTankLogic() {
        int prev = tanks[0].getFluidAmount();
        ItemStack empty = Utils.drainFluidContainer(tanks[0], inventory.get(0), inventory.get(1), null);
        if (prev != tanks[0].getFluidAmount()) {
            if (!inventory.get(1).isEmpty() && OreDictionary.itemMatches(inventory.get(1), empty, true)) { inventory.get(1).grow(empty.getCount()); }
            else if (inventory.get(1).isEmpty()) { inventory.set(1, empty.copy()); }
            inventory.get(0).shrink(1);
            if (inventory.get(0).getCount() <= 0) { inventory.set(0, ItemStack.EMPTY); }
            markContainingBlockForUpdate(null);
            return true;
        }
        return false;
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
            if (recipeTimeRemaining > 0) { if (gainProgress()) update = true; }
            else if (tanks[0].getFluidAmount() > 0) {
                SolarTowerRecipe recipe = cachedRecipe;
                if (recipe == null || !Objects.requireNonNull(tanks[0].getFluid()).isFluidEqual(recipe.fluidInput)) { recipe = SolarTowerRecipe.findRecipe(tanks[0].getFluid()); }
                if (recipe != null && recipe.fluidInput.amount <= tanks[0].getFluidAmount() && recipe.fluidOutput.amount <= tanks[1].getCapacity() - tanks[1].getFluidAmount()) {
                    cachedRecipe = recipe;
                    recipeTimeRemaining = (int)(recipe.getTotalProcessTime() / (speedMult * solarIncidenceAngleSection));
                    if (gainProgress()) { update = true; }
                }
            }
        } else if (recipeTimeRemaining > 0) { if (loseProgress()) update = true; }
        return update;
    }

    private int computeSolarIncidenceAngleSection() {
        int light = world.getSkylightSubtracted();
        if (light == 3) { return 1; }
        if (light == 2) { return 2; }
        if (light == 1) { return 3; }
        if (light == 0) { return 4; }
        return 0;
    }

    @Override public void update() {
        if (formed && redstone0 == null) { InitializePoIs(); }
        super.update();
        if (!formed) { return; }
        if (world.isRemote) { handleSounds(); return; }
        if (!isLoaded) {
            isLoaded = true;
            notifyIONeighbors();
            SolarRegistry.RegisterResult result = SolarRegistry.registerTower(world, basePos);
            registered = result.success;
            if (!registered && savedRegistered) {
                int y = basePos.getY();
                Set<BlockPos> towersAtY = SolarRegistry.getData(world).towerBasesByY.computeIfAbsent(y, k -> new HashSet<>());
                towersAtY.add(basePos);
                SolarRegistry.getData(world).markDirty();
                registered = true;
            }
            if (registered) { checkReflectorPositions(); }
        }
        if (reCheckOnLoad) {
            reCheckOnLoad = false;
            if (registered) { checkReflectorPositions(); }
        }
        solarIncidenceAngleSection = computeSolarIncidenceAngleSection();
        boolean update = false;
        if (world.getTotalWorldTime() % 600 == 0) { checkReflectorPositions(); }
        if (heatLogic()) { update = true; }
        if (solarIncidenceAngleSection != 0 && recipeLogic()) { update = true; }
        if (outputTankLogic()) { update = true; }
        if (inputTankLogic()) { update = true; }
        if (recipeTimeRemaining > 0) {
            isRunning = true;
            gracePeriod = 60;
        } else {
            if (gracePeriod > 0) { gracePeriod--; }
            else { isRunning = false; }
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

    @Override public boolean isDummy() { return false; }

    @Override public TileEntitySolarTowerMaster master() { return this; }

    @Override public void TankContentsChanged() {
        FluidStack input = tanks[0].getFluid();
        cachedRecipe = input != null && input.amount > 0 ? SolarTowerRecipe.findRecipe(input) : null;
        markContainingBlockForUpdate(null);
    }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        if (!formed) { return ITUtils.emptyIFluidTankList; }
        if (redstone0 == null) { InitializePoIs(); }
        if (side == null) { return tanks; }
        if (fluidInput0.isPoI(side, position)) { return new IFluidTank[] {tanks[0]}; }
        if (fluidOutput0.isPoI(side, position)) { return new IFluidTank[] {tanks[1]}; }
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (!formed || redstone0 == null) { InitializePoIs(); }
        if (!fluidInput0.isPoI(side, position)) { return false; }
        if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) { return false; }
        FluidStack current = tanks[0].getFluid();
        if (current == null) { return SolarTowerRecipe.findRecipe(resource) != null; }
        return resource.isFluidEqual(current);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (!formed || redstone0 == null) { InitializePoIs(); }
        return fluidOutput0.isPoI(side, position) && tanks[1].getFluidAmount() > 0;
    }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) { return new int[0]; }
        if (redstone0 == null) { InitializePoIs(); }
        return new int[] {redstone0.position};
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartSolarTower.instance.pointsOfInterest) {
            switch (poi.name) {
                case "fluid_input0":
                    fluidInput0 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_output0":
                    fluidOutput0 = new PoICache(facing, poi, mirrored);
                    fluidOutputFront0 = getBlockPosForPos(fluidOutput0.position).offset(fluidOutput0.facing);
                    break;
                case "redstone0":
                    redstone0 = new PoICache(facing, poi, mirrored);
                    break;
                case "sound0":
                    soundPos0 = getBlockPosForPos(poi.position);
                    break;
                case "link0":
                    basePos = getBlockPosForPos(new PoICache(facing, poi, mirrored).position);
                    break;
                case "collector0":
                    collectorPos0 = getBlockPosForPos(new PoICache(facing, poi, mirrored).position);
                    break;
            }
        }
        if (!world.isRemote) { notifyIONeighbors(); }
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(fluidInput0.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput0.position));
        notifyNeighbor(getBlockPosForPos(redstone0.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    public static class SolarTowerFluidHandler implements IFluidHandler {
        private final IFluidTank[] accessibleTanks;
        private final TileEntitySolarTowerMaster master;
        private final EnumFacing side;
        private final int position;

        public SolarTowerFluidHandler(IFluidTank[] accessibleTanks, TileEntitySolarTowerMaster master, EnumFacing side, int position) {
            this.accessibleTanks = accessibleTanks;
            this.master = master;
            this.side = side;
            this.position = position;
        }

        @Override public IFluidTankProperties[] getTankProperties() {
            List<IFluidTankProperties> list = new ArrayList<>();
            for (IFluidTank tank : accessibleTanks) {
                boolean canFill = tank == master.tanks[0];
                boolean canDrain = tank == master.tanks[1];
                list.add(new FluidTankProperties(tank.getFluid(), tank.getCapacity(), canFill, canDrain));
            }
            return list.toArray(new IFluidTankProperties[0]);
        }

        @Override public int fill(FluidStack resource, boolean doFill) {
            if (resource == null) { return 0; }
            resource = resource.copy();
            int filled = 0;
            for (int i = 0; i < master.tanks.length; i++) {
                for (IFluidTank accessible : accessibleTanks) {
                    if (accessible == master.tanks[i] && master.canFillTankFrom(i, side, resource, position)) {
                        int f = accessible.fill(resource, doFill);
                        filled += f;
                        resource.amount -= f;
                        if (doFill && f > 0) { master.TankContentsChanged(); }
                        if (resource.amount <= 0) { return filled; }
                    }
                }
            }
            return filled;
        }

        @Override public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null) { return null; }
            resource = resource.copy();
            FluidStack drained = null;
            for (int i = 0; i < master.tanks.length; i++) {
                for (IFluidTank accessible : accessibleTanks) {
                    if (accessible == master.tanks[i] && master.canDrainTankFrom(i, side, position)) {
                        FluidStack tankFluid = accessible.getFluid();
                        if (tankFluid != null && tankFluid.isFluidEqual(resource)) {
                            int amount = Math.min(resource.amount, tankFluid.amount);
                            FluidStack d = accessible.drain(amount, doDrain);
                            if (d != null) {
                                if (drained == null) { drained = d.copy(); }
                                else { drained.amount += d.amount; }
                                resource.amount -= d.amount;
                                if (doDrain && d.amount > 0) { master.TankContentsChanged(); }
                                if (resource.amount <= 0) { return drained; }
                            }
                        }
                    }
                }
            }
            return drained;
        }

        @Override public FluidStack drain(int maxDrain, boolean doDrain) {
            int toDrain = maxDrain;
            FluidStack drained = null;
            for (int i = 0; i < master.tanks.length; i++) {
                for (IFluidTank accessible : accessibleTanks) {
                    if (accessible == master.tanks[i] && master.canDrainTankFrom(i, side, position)) {
                        FluidStack d = accessible.drain(toDrain, doDrain);
                        if (d != null) {
                            if (drained == null) { drained = d.copy(); }
                            else if (drained.isFluidEqual(d)) { drained.amount += d.amount; }
                            toDrain -= d.amount;
                            if (doDrain && d.amount > 0) { master.TankContentsChanged(); }
                            if (toDrain <= 0) { return drained; }
                        }
                    }
                }
            }
            return drained;
        }
    }
}
