package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSolarMelter;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
import mctmods.immersivetechnology.common.util.compat.advancedrocketry.AdvancedRocketryHelper;
import mctmods.immersivetechnology.common.util.multiblock.PoICache;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
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

import mctmods.immersivetechnology.api.crafting.MeltingCrucibleRecipe;

public class TileEntitySolarMelterMaster extends TileEntitySolarMelterSlave implements ITFluidTank.TankListener, IIEInventory {
    private static final int outputTankSize = Multiblocks.solarMelter.solarMelter_output_tankSize;
    private static final int solarMaxRange = Multiblocks.solarReflector.solarReflector_maxRange;
    private static final int solarMinRange = Multiblocks.solarReflector.solarReflector_minRange;
    private static final int energyLossPerTick = Multiblocks.solarMelter.solarMelter_progress_lossEnergy;
    private static final double heatLossMultiplier = 0.00067D;
    private static final float speedMult = 1F;
    private static final double workingHeatLevel = Multiblocks.solarMelter.solarMelter_heat_workingLevel;
    private static final double maximumReflectorStrength = Multiblocks.solarMelter.solarMelter_maximum_reflector_strength;
    public FluidTank[] tanks = new FluidTank[] { new ITFluidTank(outputTankSize, this) };
    public static int slotCount = 3;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    IItemHandler insertionHandler = new IEInventoryHandler(1, this, 0, new boolean[]{true}, new boolean[]{false});
    public int recipeEnergyRemaining = 0;
    public double heatLevel = 0;
    public double reflectorStrength = 0;
    public int solarIncidenceAngleSection = 0;
    private float soundVolume;
    private boolean isRunning;
    private int gracePeriod = 60;
    private int clientUpdateCooldown = 1;
    public MeltingCrucibleRecipe cachedRecipe;
    private PoICache redstone0;
    private PoICache fluidOutput0;
    private PoICache itemInput0;
    private BlockPos soundPos0;
    private BlockPos fluidOutputFront0;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        recipeEnergyRemaining = nbt.getInteger("recipeEnergyRemaining");
        heatLevel = nbt.getDouble("heatLevel");
        reflectorStrength = nbt.getDouble("reflectorStrength");
        solarIncidenceAngleSection = nbt.getInteger("solarIncidenceAngleSection");
        if(!descPacket) inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setInteger("recipeEnergyRemaining", recipeEnergyRemaining);
        nbt.setDouble("heatLevel", heatLevel);
        nbt.setDouble("reflectorStrength", reflectorStrength);
        nbt.setInteger("solarIncidenceAngleSection", solarIncidenceAngleSection);
        if(!descPacket) nbt.setTag("inventory", Utils.writeInventory(inventory));
    }

    protected void checkReflectorPositions() {
        double totalMirrorStrength = 0;
        for(int x = -(solarMaxRange + 1); x <= (solarMaxRange + 1); x++) for(int z = -(solarMaxRange + 1); z <= (solarMaxRange + 1); z++) {
            double distance = Math.sqrt(this.getPos().distanceSq(this.getPos().add(x, -1, z)));
            if(distance >= solarMinRange && distance <= solarMaxRange && Utils.isBlockAt(world, this.getPos().add(x, -1, z), ITContent.blockMetalMultiblock, 2)) {
                TileEntity tile = world.getTileEntity(this.getPos().add(x, -1, z));
                if(tile instanceof TileEntitySolarReflectorMaster && ((TileEntitySolarReflectorMaster)tile).setTowerCollectorPosition(this.getPos().add(0, 16, 0))) totalMirrorStrength += ((TileEntitySolarReflectorMaster)tile).getSolarCollectorStrength();
            }
        }
        totalMirrorStrength *= (world.isRaining() ? 0.4f : 1f);
        if(ITCompatModule.isAdvancedRocketryLoaded) totalMirrorStrength *= AdvancedRocketryHelper.getInsolation(world, this.getPos());
        double humidityBonus = 0.075 * totalMirrorStrength * -((world.getBiome(this.getPos()).getRainfall() - 0.5)/0.5);
        if(ITCompatModule.isAdvancedRocketryLoaded) humidityBonus *= AdvancedRocketryHelper.getWaterPartialPressureMultiplier(world, this.getPos());
        totalMirrorStrength += humidityBonus;
        reflectorStrength = totalMirrorStrength;
    }

    protected void detachMirrors() {
        for(int x = -(solarMaxRange + 1); x <= (solarMaxRange + 1); x++) for(int z = -(solarMaxRange + 1); z <= (solarMaxRange + 1); z++) {
            double distance = Math.sqrt(this.getPos().distanceSq(this.getPos().add(x, -1, z)));
            if(distance >= solarMinRange && distance <= solarMaxRange && Utils.isBlockAt(world, this.getPos().add(x, -1, z), ITContent.blockMetalMultiblock, 2)) {
                TileEntity tile = world.getTileEntity(this.getPos().add(x, -1, z));
                if(tile instanceof TileEntitySolarReflectorMaster) ((TileEntitySolarReflectorMaster)tile).detachTower(this.getPos().add(0, 16, 0));
            }
        }
    }

    private boolean heatUp() {
        double previousHeatLevel = heatLevel;
        heatLevel = Math.min(getTemperatureIncrease() + heatLevel, workingHeatLevel);
        return previousHeatLevel != heatLevel;
    }

    protected float getTemperatureIncrease() { return speedMult * (1 + (solarIncidenceAngleSection - 1)) * 10 * (float)(reflectorStrength / maximumReflectorStrength) * (world.isRaining() ? 0.1f : world.isThundering() ? 0.05f : 1f); }

    private boolean cooldown() {
        double previousHeatLevel = heatLevel;
        double heatLost = world.getBiomeProvider().getTemperatureAtHeight(world.getBiome(this.getPos()).getTemperature(this.getPos()), this.getPos().getY());
        double conductionMultiplier = 1.0;
        if(ITCompatModule.isAdvancedRocketryLoaded) conductionMultiplier *= AdvancedRocketryHelper.getHeatTransferCoefficient(world, this.getPos().add(0, 19, 0));
        heatLevel = Math.max((heatLevel - ((world.isRaining() ? 2 : 1 * (1 / heatLost)) * heatLossMultiplier * conductionMultiplier)), 0);
        return previousHeatLevel != heatLevel;
    }

    private boolean loseProgress() {
        if(cachedRecipe == null || !cachedRecipe.itemInput.matches(inventory.get(0))) { cachedRecipe = null; recipeEnergyRemaining = 0; return true; }
        int previousProgress = recipeEnergyRemaining;
        recipeEnergyRemaining = (int)Math.min(recipeEnergyRemaining + energyLossPerTick * (ITCompatModule.isAdvancedRocketryLoaded ? AdvancedRocketryHelper.getHeatTransferCoefficient(world, this.getPos()) : 1), cachedRecipe.getTotalProcessEnergy());
        return previousProgress != recipeEnergyRemaining;
    }

    private boolean gainProgress() {
        if(cachedRecipe == null || !cachedRecipe.itemInput.matches(inventory.get(0))) { cachedRecipe = null; recipeEnergyRemaining = 0; return true; }
        int previousProgress = recipeEnergyRemaining;
        recipeEnergyRemaining -= (int)(solarIncidenceAngleSection * 7680 * (reflectorStrength / maximumReflectorStrength));
        boolean changed = previousProgress != recipeEnergyRemaining;
        if(recipeEnergyRemaining <= 0) {
            inventory.get(0).shrink(cachedRecipe.itemInput.inputSize);
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
        this.tanks[0].drain(drained, true);
        return drained > 0;
    }

    private boolean outputTankLogic() {
        boolean update = false;
        ItemStack filledContainer = Utils.fillFluidContainer(tanks[0], inventory.get(1), inventory.get(2), null);
        if(!filledContainer.isEmpty()) {
            if(!inventory.get(2).isEmpty() && OreDictionary.itemMatches(inventory.get(2), filledContainer, true)) {
                inventory.get(2).grow(filledContainer.getCount());
            } else if(inventory.get(2).isEmpty()) {
                inventory.set(2, filledContainer.copy());
            }
            inventory.get(1).shrink(1);
            if(inventory.get(1).getCount() <= 0) {
                inventory.set(1, ItemStack.EMPTY);
            }
            markContainingBlockForUpdate(null);
            update = true;
        }
        if(pumpOutputOut()) update = true;
        return update;
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if(isRunning) { soundVolume = Math.min(soundVolume + 0.02f, 1); } else { soundVolume = Math.max(soundVolume - 0.02f, 0); }
        if(soundVolume == 0) ITSoundHandler.StopSound(soundPos0);
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos0.getX(), soundPos0.getY(), soundPos0.getZ()) / 8, 1);
            ITSounds.heatExchanger.PlayRepeating(soundPos0, soundVolume / attenuation, 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        ITSoundHandler.StopSound(soundPos0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        super.disassemble();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
        detachMirrors();
    }

    public void efficientMarkDirty() { world.getChunk(this.getPos()).markDirty(); }

    private int computeSolarIncidenceAngleSection() {
        int light = world.getSkylightSubtracted();
        if(light == 3) return 1;
        else if(light == 2) return 2;
        else if(light == 1) return 3;
        else if(light == 0) return 4;
        return 0;
    }

    private boolean heatLogic() {
        boolean update = false;
        if(solarIncidenceAngleSection != 0) { if(heatUp()) { update = true; } }
        else { if(cooldown()) { update = true; } }
        return update;
    }

    private boolean recipeLogic() {
        boolean update = false;
        if(cachedRecipe != null && !cachedRecipe.itemInput.matches(inventory.get(0))) { cachedRecipe = null; recipeEnergyRemaining = 0; update = true; }
        BlockPos redstonePos = getBlockPosForPos(redstone0.position);
        int power = world.getStrongPower(redstonePos);
        boolean rsDisabled = power > 0;
        if(heatLevel >= workingHeatLevel && !rsDisabled) {
            if(recipeEnergyRemaining > 0) { update |= gainProgress(); }
            else if(!inventory.get(0).isEmpty()) {
                MeltingCrucibleRecipe recipe = cachedRecipe;
                if(recipe == null || !recipe.itemInput.matches(inventory.get(0))) recipe = MeltingCrucibleRecipe.findRecipe(inventory.get(0));
                if(recipe != null && recipe.fluidOutput.amount <= tanks[0].getCapacity() - tanks[0].getFluidAmount()) {
                    cachedRecipe = recipe;
                    recipeEnergyRemaining = recipe.getTotalProcessEnergy();
                    update |= gainProgress();
                }
            }
        } else if(recipeEnergyRemaining > 0) { update |= loseProgress(); }
        return update;
    }

    @Override public void update() {
        if (formed && redstone0 == null) InitializePoIs();
        super.update();
        if(!formed) return;
        if(world.isRemote) {
            handleSounds();
            return;
        }
        solarIncidenceAngleSection = computeSolarIncidenceAngleSection();
        boolean update = false;
        if(world.getTotalWorldTime() % 600 == 0) checkReflectorPositions();
        if(heatLogic()) update = true;
        if(solarIncidenceAngleSection != 0) if(recipeLogic()) update = true;
        if(outputTankLogic()) update = true;
        boolean shouldRun = recipeEnergyRemaining > 0 && heatLevel >= workingHeatLevel;
        if(shouldRun) { isRunning = true; gracePeriod = 60; } else { if(gracePeriod == 0) isRunning = false; else gracePeriod--; }
        clientUpdateCooldown--;
        if(clientUpdateCooldown <= 0) { notifyNearbyClients(); clientUpdateCooldown = 20; }
        if(update) {
            efficientMarkDirty();
            this.markContainingBlockForUpdate(null);
        }
    }

    @Override public void TankContentsChanged() {
        cachedRecipe = null;
        this.markContainingBlockForUpdate(null);
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntitySolarMelterMaster master() { return this; }

    public void notifyNearbyClients() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setDouble("heatLevel", heatLevel);
        tag.setInteger("solarIncidenceAngleSection", solarIncidenceAngleSection);
        tag.setBoolean("isRunning", isRunning);
        ImmersiveTechnology.packetHandler.sendToAllAround(new MessageTileSync(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 40));
    }

    @Override public void receiveMessageFromServer(@Nonnull NBTTagCompound message) {
        heatLevel = message.getDouble("heatLevel");
        solarIncidenceAngleSection = message.getInteger("solarIncidenceAngleSection");
        isRunning = message.getBoolean("isRunning");
    }

    private void InitializePoIs() {
        for(PoIJSONSchema poi : TileEntityITMultiblockPartSolarMelter.instance.pointsOfInterest) {
            switch(poi.name) {
                case "redstone0":
                    redstone0 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_output0":
                    fluidOutput0 = new PoICache(facing, poi, mirrored);
                    fluidOutputFront0 = getBlockPosForPos(fluidOutput0.position).offset(fluidOutput0.facing);
                    break;
                case "item_input0":
                    itemInput0 = new PoICache(facing, poi, mirrored);
                    break;
                case "sound0":
                    soundPos0 = getBlockPosForPos(poi.position);
                    break;
            }
        }
        if(!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(fluidOutput0.position));
        notifyNeighbor(getBlockPosForPos(redstone0.position));
        notifyNeighbor(getBlockPosForPos(itemInput0.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstone0 == null) InitializePoIs();
        return new int[] {redstone0.position};
    }

    @Override
    @Nonnull
    public IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if(!formed) return ITUtils.emptyIFluidTankList;
        if(fluidOutput0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) { return false; }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) { return fluidOutput0.isPoI(side, position) && iTank == 0; }

    protected @Nonnull IItemHandler[] getAccessibleItemHandlers(EnumFacing side, int position) {
        if(!formed) return new IItemHandler[0];
        if(itemInput0.isPoI(side, position)) return new IItemHandler[] {insertionHandler};
        return new IItemHandler[0];
    }

    @Override public NonNullList<ItemStack> getInventory() { return inventory; }

    @Override public boolean isStackValid(int slot, ItemStack stack) {
        if(slot == 0) return MeltingCrucibleRecipe.findRecipe(stack) != null;
        if(slot == 1) return Utils.isFluidRelatedItemStack(stack);
        return false;
    }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) {
        this.markDirty();
        this.markContainingBlockForUpdate(null);
    }

    @Override public NonNullList<ItemStack> getDroppedItems() { return getInventory(); }

    @Override public int getComparatedSize() { return getInventory().size(); }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }
}
