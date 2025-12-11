package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.SolarTowerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.multiblocks.tileentitiesmultiblockpart.TileEntityITMultiblockPartSolarTower;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
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
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;

import java.util.Objects;

public class TileEntitySolarTowerMaster extends TileEntitySolarTowerSlave implements ITFluidTank.TankListener {
    private static final int inputTankSize = Multiblocks.solarTower.solarTower_input_tankSize;
    private static final int outputTankSize = Multiblocks.solarTower.solarTower_output_tankSize;
    private static final int solarMaxRange = Multiblocks.solarReflector.solarReflector_maxRange;
    private static final int solarMinRange = Multiblocks.solarReflector.solarReflector_minRange;
    private static final int progressLossPerTick = Multiblocks.solarTower.solarTower_progress_lossInTicks;
    private static final double heatLossMultiplier = Multiblocks.solarTower.solarTower_heat_loss_multiplier;
    private static final float speedMult = Multiblocks.solarTower.solarTower_speed_multiplier;
    private static final double workingHeatLevel = Multiblocks.solarTower.solarTower_heat_workingLevel;
    private static final double maximumReflectorStrength = Multiblocks.solarTower.solarTower_maximum_reflector_strength;
    public FluidTank[] tanks = new FluidTank[] { new ITFluidTank(inputTankSize, this), new ITFluidTank(outputTankSize, this) };
    public static int slotCount = 4;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    public int recipeTimeRemaining = 0;
    public double heatLevel = 0;
    public double reflectorStrength = 0;
    public int solarIncidenceAngleSection = 0;
    private int clientUpdateCooldown = 20;
    private float soundVolume;
    private boolean isRunning;
    private int gracePeriod = 60;
    private SolarTowerRecipe cachedRecipe;
    private PoICache redstone0, fluidInput0, fluidOutput0;
    private BlockPos soundPos0, fluidOutputFront0;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        heatLevel = nbt.getDouble("heatLevel");
        recipeTimeRemaining = nbt.getInteger("recipeTimeRemaining");
        reflectorStrength = nbt.getDouble("reflectorStrength");
        solarIncidenceAngleSection = nbt.getInteger("solarIncidenceAngleSection");
        if(!descPacket) inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setDouble("heatLevel", heatLevel);
        nbt.setInteger("recipeTimeRemaining", recipeTimeRemaining);
        nbt.setDouble("reflectorStrength", reflectorStrength);
        nbt.setInteger("solarIncidenceAngleSection", solarIncidenceAngleSection);
        if(!descPacket) nbt.setTag("inventory", Utils.writeInventory(inventory));
    }

    protected void checkReflectorPositions() {
        double totalMirrorStrength = 0;
        for(int x = -(solarMaxRange + 1); x <= (solarMaxRange + 1); x++) for(int z = -(solarMaxRange + 1); z <= (solarMaxRange + 1); z++) {
            double distance = Math.sqrt(this.getPos().distanceSq(this.getPos().add(x, 0, z)));
            if(distance >= solarMinRange && distance <= solarMaxRange && Utils.isBlockAt(world, this.getPos().add(x, 0, z), ITContent.blockMetalMultiblock, 2)) {
                TileEntity tile = world.getTileEntity(this.getPos().add(x, 0, z));
                if(tile instanceof TileEntitySolarReflectorMaster && ((TileEntitySolarReflectorMaster)tile).setTowerCollectorPosition(this.getPos().add(0, 17, 0))) totalMirrorStrength += ((TileEntitySolarReflectorMaster)tile).getSolarCollectorStrength();
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
            double distance = Math.sqrt(this.getPos().distanceSq(this.getPos().add(x, 0, z)));
            if(distance >= solarMinRange && distance <= solarMaxRange && Utils.isBlockAt(world, this.getPos().add(x, 0, z), ITContent.blockMetalMultiblock, 2)) {
                TileEntity tile = world.getTileEntity(this.getPos().add(x, 0, z));
                if(tile instanceof TileEntitySolarReflectorMaster) ((TileEntitySolarReflectorMaster)tile).detachTower(this.getPos().add(0, 17, 0));
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
        int previousProgress = recipeTimeRemaining;
        if(cachedRecipe == null) { recipeTimeRemaining = 0; return true; }
        recipeTimeRemaining = Math.min(recipeTimeRemaining + progressLossPerTick, cachedRecipe.getTotalProcessTime());
        return previousProgress != recipeTimeRemaining;
    }

    private boolean gainProgress() {
        if(cachedRecipe == null) { recipeTimeRemaining = 0; return true; }
        recipeTimeRemaining--;
        if(recipeTimeRemaining == 0) {
            tanks[0].drain((int)(cachedRecipe.fluidInput.amount * reflectorStrength / maximumReflectorStrength), true);
            tanks[1].fillInternal(new FluidStack(cachedRecipe.fluidOutput.getFluid(), (int)(cachedRecipe.fluidOutput.amount * reflectorStrength / maximumReflectorStrength)), true);
            markContainingBlockForUpdate(null);
            return true;
        }
        return false;
    }

    private boolean pumpOutputOut() {
        if(tanks[1].getFluidAmount() == 0) return false;
        IFluidHandler handler = FluidUtil.getFluidHandler(world, fluidOutputFront0, fluidOutput0.facing.getOpposite());
        if(handler == null) return false;
        FluidStack out = tanks[1].getFluid();
        int accepted = handler.fill(out, false);
        if(accepted == 0) return false;
        assert out != null;
        int drained = handler.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        this.tanks[1].drain(drained, true);
        return drained > 0;
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if(isRunning) { soundVolume = Math.min(soundVolume + 0.01f, 1); } else { soundVolume = Math.max(soundVolume - 0.01f, 0); }
        if(soundVolume == 0) ITSoundHandler.StopSound(soundPos0);
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos0.getX(), soundPos0.getY(), soundPos0.getZ()) / 8, 1);
            ITSounds.solarTower.PlayRepeating(soundPos0, (2 * soundVolume) / attenuation, soundVolume);
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

    public void notifyNearbyClients() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setDouble("heat", heatLevel);
        tag.setInteger("solarIncidenceAngleSection", solarIncidenceAngleSection);
        tag.setBoolean("isRunning", isRunning);
        ImmersiveTechnology.packetHandler.sendToAllAround(new MessageTileSync(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 40));
    }

    @Override public void receiveMessageFromServer(@Nonnull NBTTagCompound message) {
        heatLevel = message.getDouble("heat");
        solarIncidenceAngleSection = message.getInteger("solarIncidenceAngleSection");
        isRunning = message.getBoolean("isRunning");
    }

    public void efficientMarkDirty() { world.getChunk(this.getPos()).markDirty(); }

    private boolean heatLogic() {
        boolean update = false;
        if(solarIncidenceAngleSection != 0) { if(heatUp()) { update = true; } }
        else { if(cooldown()) { update = true; } }
        return update;
    }

    private boolean recipeLogic() {
        boolean update = false;
        if(heatLevel >= workingHeatLevel && !isRSDisabled()) {
            if(recipeTimeRemaining > 0) { if(gainProgress()) { update = true; } }
            else if(tanks[0].getFluidAmount() > 0) {
                SolarTowerRecipe recipe = cachedRecipe;
                if(recipe == null || !Objects.requireNonNull(tanks[0].getFluid()).isFluidEqual(recipe.fluidInput)) { recipe = SolarTowerRecipe.findRecipe(tanks[0].getFluid()); }
                if(recipe != null && recipe.fluidInput.amount <= tanks[0].getFluidAmount() && recipe.fluidOutput.amount <= tanks[1].getCapacity() - tanks[1].getFluidAmount()) {
                    cachedRecipe = recipe;
                    recipeTimeRemaining = (int)(recipe.getTotalProcessTime() / (speedMult * solarIncidenceAngleSection));
                    gainProgress();
                    update = true;
                }
            }
        } else if(recipeTimeRemaining > 0) { if(loseProgress()) { update = true; } }
        return update;
    }

    private boolean outputTankLogic() {
        boolean update = false;
        if(this.tanks[1].getFluidAmount() > 0) {
            ItemStack filledContainer = Utils.fillFluidContainer(tanks[1], inventory.get(2), inventory.get(3), null);
            if(!filledContainer.isEmpty()) {
                if(!inventory.get(3).isEmpty() && OreDictionary.itemMatches(inventory.get(3), filledContainer, true)) { inventory.get(3).grow(filledContainer.getCount()); }
                else if(inventory.get(3).isEmpty()) { inventory.set(3, filledContainer.copy()); }
                inventory.get(2).shrink(1);
                if(inventory.get(2).getCount() <= 0) { inventory.set(2, ItemStack.EMPTY); }
                markContainingBlockForUpdate(null);
                update = true;
            }
            if(pumpOutputOut()) { update = true; }
        }
        return update;
    }

    private boolean inputTankLogic() {
        int amount_prev = tanks[0].getFluidAmount();
        ItemStack emptyContainer = Utils.drainFluidContainer(tanks[0], inventory.get(0), inventory.get(1), null);
        if(amount_prev != tanks[0].getFluidAmount()) {
            if(!inventory.get(1).isEmpty() && OreDictionary.itemMatches(inventory.get(1), emptyContainer, true)) { inventory.get(1).grow(emptyContainer.getCount()); }
            else if(inventory.get(1).isEmpty()) { inventory.set(1, emptyContainer.copy()); }
            inventory.get(0).shrink(1);
            if(inventory.get(0).getCount() <= 0) { inventory.set(0, ItemStack.EMPTY); }
            markContainingBlockForUpdate(null);
            return true;
        }
        return false;
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
        if(inputTankLogic()) update = true;
        if(recipeTimeRemaining > 0) { isRunning = true; gracePeriod = 60; } else { if(gracePeriod == 0) isRunning = false; else gracePeriod--; }
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

    @Override public TileEntitySolarTowerMaster master() { return this; }

    private void InitializePoIs() {
        for(PoIJSONSchema poi : TileEntityITMultiblockPartSolarTower.instance.pointsOfInterest) {
            switch(poi.name) {
                case "redstone": redstone0 = new PoICache(facing, poi, mirrored); break;
                case "fluid_input": fluidInput0 = new PoICache(facing, poi, mirrored); break;
                case "fluid_output": fluidOutput0 = new PoICache(facing, poi, mirrored); fluidOutputFront0 = getBlockPosForPos(fluidOutput0.position).offset(fluidOutput0.facing); break;
                case "sound": soundPos0 = getBlockPosForPos(poi.position); break;
            }
        }
        if(!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(fluidInput0.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput0.position));
        notifyNeighbor(getBlockPosForPos(redstone0.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override public @Nonnull int[] getRedstonePos() {
        if (!formed) return new int[0];
        if(redstone0 == null) InitializePoIs();
        return new int[]{redstone0.position};
    }

    @Override protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if(!formed) return ITUtils.emptyIFluidTankList;
        if(fluidInput0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        if(fluidOutput0.isPoI(side, position)) return new IFluidTank[] {tanks[1]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if(fluidInput0.isPoI(side, position) && iTank == 0) {
            if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
            FluidStack current = tanks[0].getFluid();
            if (current == null) return SolarTowerRecipe.findRecipeByFluid(resource.getFluid()) != null;
            return resource.isFluidEqual(current);
        }
        return false;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) { return fluidOutput0.isPoI(side, position) && iTank == 1; }

    private int computeSolarIncidenceAngleSection() {
        int light = world.getSkylightSubtracted();
        if (light == 3) { return 1; }
        else if (light == 2) { return 2; }
        else if (light == 1) { return 3; }
        else if (light == 0) { return 4; }
        return 0;
    }
}
