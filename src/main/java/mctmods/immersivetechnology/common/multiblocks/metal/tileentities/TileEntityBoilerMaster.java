package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.BoilerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartBoiler;
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

public class TileEntityBoilerMaster extends TileEntityBoilerSlave implements ITFluidTank.TankListener, IComparatorOverride {
    private static final int inputTankSize = Multiblocks.boiler.boiler_input_tankSize;
    private static final int outputTankSize = Multiblocks.boiler.boiler_output_tankSize;
    private static final int inputFuelTankSize = Multiblocks.boiler.boiler_fuel_tankSize;
    private static final int heatLossPerTick = Multiblocks.boiler.boiler_heat_lossPerTick;
    private static final int progressLossPerTick = Multiblocks.boiler.boiler_progress_lossInTicks;
    private static final double workingHeatLevel = Multiblocks.boiler.boiler_heat_workingLevel;

    public FluidTank[] tanks = new FluidTank[] {new ITFluidTank(inputFuelTankSize, this), new ITFluidTank(inputTankSize, this), new ITFluidTank(outputTankSize, this)};

    public static int slotCount = 6;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);

    public int burnRemaining = 0;
    public int recipeTimeRemaining = 0;
    public double heatLevel = 0;
    private int clientUpdateCooldown = 20;

    public BoilerRecipe.BoilerFuelRecipe lastFuel;
    public BoilerRecipe lastRecipe;

    private PoICache fluidInput0, fluidInput1, fluidOutput0, redstone0;
    private BlockPos fluidOutputFront0;
    private BlockPos soundPos;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
        heatLevel = nbt.getDouble("heatLevel");
        burnRemaining = nbt.getInteger("burnRemaining");
        recipeTimeRemaining = nbt.getInteger("recipeTimeRemaining");
        if (!descPacket) {
            inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
        }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank2", tanks[2].writeToNBT(new NBTTagCompound()));
        nbt.setDouble("heatLevel", heatLevel);
        nbt.setInteger("burnRemaining", burnRemaining);
        nbt.setInteger("recipeTimeRemaining", recipeTimeRemaining);
        if (!descPacket) nbt.setTag("inventory", Utils.writeInventory(inventory));
    }

    private boolean heatUp() {
        double previousHeatLevel = heatLevel;
        if (lastFuel == null) { burnRemaining = 0; return true; }
        heatLevel = Math.min(lastFuel.getHeat() + heatLevel, workingHeatLevel);
        return previousHeatLevel != heatLevel;
    }

    private boolean cooldown() {
        double previousHeatLevel = heatLevel;
        double heatTransferMultiplier = 1.0;
        if (ITCompatModule.isAdvancedRocketryLoaded) heatTransferMultiplier = AdvancedRocketryHelper.getHeatTransferCoefficient(world, getPos().add(0, 2, 0));
        heatLevel = Math.max(heatLevel - heatLossPerTick * heatTransferMultiplier, 0);
        return previousHeatLevel != heatLevel;
    }

    private boolean loseProgress() {
        int previousProgress = recipeTimeRemaining;
        if (lastRecipe == null) { recipeTimeRemaining = 0; return true; }
        recipeTimeRemaining = Math.min(recipeTimeRemaining + progressLossPerTick, lastRecipe.getTotalProcessTime());
        return previousProgress != recipeTimeRemaining;
    }

    private boolean gainProgress() {
        if (lastRecipe == null) { recipeTimeRemaining = 0; return true; }
        recipeTimeRemaining--;
        if (recipeTimeRemaining == 0) {
            tanks[1].drain(lastRecipe.fluidInput.amount, true);
            tanks[2].fillInternal(lastRecipe.fluidOutput, true);
            markContainingBlockForUpdate(null);
            return true;
        }
        return false;
    }

    private void pumpOutputOut() {
        if (tanks[2].getFluidAmount() == 0) return;
        if (fluidOutput0 == null) InitializePoIs();
        IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputFront0, fluidOutput0.facing.getOpposite());
        if (output == null) return;
        FluidStack out = tanks[2].getFluid();
        int accepted = output.fill(out, false);
        if (accepted == 0) return;
        assert out != null;
        int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        this.tanks[2].drain(drained, true);
    }

    public void handleSounds() {
        BlockPos center = soundPos;
        float level = (float) (heatLevel / workingHeatLevel);
        if (level == 0) ITSoundHandler.StopSound(center);
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float) player.getDistanceSq(center.getX(), center.getY(), center.getZ()) / 8, 1);
            ITSounds.boiler.PlayRepeating(center, (2 * level) / attenuation, level);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() { ITSoundHandler.StopSound(soundPos); super.onChunkUnload(); }

    @Override public void disassemble() {
        BlockPos center = soundPos;
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(center), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
        super.disassemble();
    }

    public void notifyNearbyClients() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setDouble("heat", heatLevel);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendToAllAround(new MessageTileSync(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 40));
    }

    @Override public void receiveMessageFromServer(@Nonnull NBTTagCompound message) { heatLevel = message.getDouble("heat"); }

    public void efficientMarkDirty() { world.getChunk(this.getPos()).markDirty(); }

    private boolean heatLogic() {
        boolean update = false;
        boolean canFuelCombust = true;
        if (ITCompatModule.isAdvancedRocketryLoaded) canFuelCombust = AdvancedRocketryHelper.isAtmosphereSuitableForCombustion(world, ITUtils.LocalOffsetToWorldBlockPos(this.getPos(), 3, 0, 1, facing, mirrored));
        if (canFuelCombust) {
            if (burnRemaining > 0) {
                burnRemaining--;
                if (heatUp()) update = true;
            } else if (!isRSDisabled() && tanks[0].getFluid() != null) {
                BoilerRecipe.BoilerFuelRecipe fuel = (lastFuel != null && tanks[0].getFluid().isFluidEqual(lastFuel.fluidInput)) ? lastFuel : BoilerRecipe.findFuel(tanks[0].getFluid());
                if (fuel != null && fuel.fluidInput.amount <= tanks[0].getFluidAmount()) {
                    lastFuel = fuel;
                    tanks[0].drain(fuel.fluidInput.amount, true);
                    burnRemaining = fuel.getTotalProcessTime() - 1;
                    markContainingBlockForUpdate(null);
                    if (heatUp()) update = true;
                } else if (cooldown()) update = true;
            } else if (cooldown()) update = true;
        } else if (cooldown()) update = true;
        return update;
    }

    private boolean recipeLogic() {
        boolean update = false;
        if (heatLevel >= workingHeatLevel) {
            if (recipeTimeRemaining > 0) {
                if (gainProgress()) update = true;
            } else if (tanks[1].getFluid() != null) {
                BoilerRecipe recipe = (lastRecipe != null && tanks[1].getFluid().isFluidEqual(lastRecipe.fluidInput)) ? lastRecipe : BoilerRecipe.findRecipe(tanks[1].getFluid());
                if (recipe != null && recipe.fluidInput.amount <= tanks[1].getFluidAmount() && recipe.fluidOutput.amount == tanks[2].fillInternal(recipe.fluidOutput, false)) {
                    lastRecipe = recipe;
                    recipeTimeRemaining = recipe.getTotalProcessTime();
                    gainProgress();
                    update = true;
                }
            }
        } else if (recipeTimeRemaining > 0) if (loseProgress()) update = true;
        return update;
    }

    private boolean outputTankLogic() {
        boolean update = false;
        if (this.tanks[2].getFluidAmount() > 0) {
            ItemStack filledContainer = Utils.fillFluidContainer(tanks[2], inventory.get(4), inventory.get(5), null);
            if (!filledContainer.isEmpty()) {
                if (!inventory.get(5).isEmpty() && OreDictionary.itemMatches(inventory.get(5), filledContainer, true)) inventory.get(5).grow(filledContainer.getCount());
                else if (inventory.get(5).isEmpty()) inventory.set(5, filledContainer.copy());
                inventory.get(4).shrink(1);
                if (inventory.get(4).getCount() <= 0) inventory.set(4, ItemStack.EMPTY);
                markContainingBlockForUpdate(null);
                update = true;
            }
            pumpOutputOut();
        }
        return update;
    }

    private boolean fuelTankLogic() {
        int amount_prev = tanks[0].getFluidAmount();
        ItemStack emptyContainer = Utils.drainFluidContainer(tanks[0], inventory.get(0), inventory.get(1), null);
        if (amount_prev != tanks[0].getFluidAmount()) {
            if (!inventory.get(1).isEmpty() && OreDictionary.itemMatches(inventory.get(1), emptyContainer, true)) inventory.get(1).grow(emptyContainer.getCount());
            else if (inventory.get(1).isEmpty()) inventory.set(1, emptyContainer.copy());
            inventory.get(0).shrink(1);
            if (inventory.get(0).getCount() <= 0) inventory.set(0, ItemStack.EMPTY);
            markContainingBlockForUpdate(null);
            return true;
        }
        return false;
    }

    private boolean inputTankLogic() {
        int amount_prev = tanks[1].getFluidAmount();
        ItemStack emptyContainer = Utils.drainFluidContainer(tanks[1], inventory.get(2), inventory.get(3), null);
        if (amount_prev != tanks[1].getFluidAmount()) {
            if (!inventory.get(3).isEmpty() && OreDictionary.itemMatches(inventory.get(3), emptyContainer, true)) inventory.get(3).grow(emptyContainer.getCount());
            else if (inventory.get(3).isEmpty()) inventory.set(3, emptyContainer.copy());
            inventory.get(2).shrink(1);
            if (inventory.get(2).getCount() <= 0) inventory.set(2, ItemStack.EMPTY);
            markContainingBlockForUpdate(null);
            return true;
        }
        return false;
    }

    @Override public void update() {
        if (formed && redstone0 == null) InitializePoIs();
        super.update();
        if (!formed || world.isRemote) {
            if (world.isRemote) handleSounds();
            return;
        }
        boolean update = heatLogic();
        if (recipeLogic()) update = true;
        if (outputTankLogic()) update = true;
        if (fuelTankLogic()) update = true;
        if (inputTankLogic()) update = true;
        if (clientUpdateCooldown > 1) clientUpdateCooldown--;
        if (update) {
            efficientMarkDirty();
            if (clientUpdateCooldown == 1) {
                notifyNearbyClients();
                clientUpdateCooldown = 20;
            }
        }
    }

    @Override public void TankContentsChanged() { this.markContainingBlockForUpdate(null); }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityBoilerMaster master() {
        master = this;
        return this;
    }

    @Override public int getComparatorInputOverride() { return (int)(15 * (heatLevel / workingHeatLevel)); }

    protected void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartBoiler.instance.pointsOfInterest) {
            switch (poi.name) {
                case "fluid_input0":
                    fluidInput0 = new PoICache(this.facing, poi, this.mirrored);
                    break;
                case "fluid_input1":
                    fluidInput1 = new PoICache(this.facing, poi, this.mirrored);
                    break;
                case "fluid_output0":
                    fluidOutput0 = new PoICache(this.facing, poi, this.mirrored);
                    fluidOutputFront0 = getBlockPosForPos(fluidOutput0.position).offset(fluidOutput0.facing);
                    break;
                case "redstone0":
                    redstone0 = new PoICache(this.facing, poi, this.mirrored);
                    break;
                case "sound0":
                    soundPos = getBlockPosForPos(poi.position);
                    break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(fluidInput0.position));
        notifyNeighbor(getBlockPosForPos(fluidInput1.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput0.position));
        notifyNeighbor(getBlockPosForPos(redstone0.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override
    public @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if (side == null) return tanks;
        if (redstone0 == null) InitializePoIs();
        if (fluidInput0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        if (fluidInput1.isPoI(side, position)) return new IFluidTank[] {tanks[1]};
        if (fluidOutput0.isPoI(side, position)) return new IFluidTank[] {tanks[2]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override
    public boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (redstone0 == null) InitializePoIs();
        if (fluidInput0.isPoI(side, position)) {
            if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
            if (tanks[0].getFluid() == null) return BoilerRecipe.findFuel(resource) != null;
            return resource.isFluidEqual(tanks[0].getFluid());
        } else if (fluidInput1.isPoI(side, position)) {
            if (tanks[1].getFluidAmount() >= tanks[1].getCapacity()) return false;
            if (tanks[1].getFluid() == null) return BoilerRecipe.findRecipe(resource) != null;
            return resource.isFluidEqual(tanks[1].getFluid());
        }
        return false;
    }

    @Override
    public boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (redstone0 == null) InitializePoIs();
        return fluidOutput0.isPoI(side, position);
    }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstone0 == null) InitializePoIs();
        return new int[] {redstone0.position};
    }
}
