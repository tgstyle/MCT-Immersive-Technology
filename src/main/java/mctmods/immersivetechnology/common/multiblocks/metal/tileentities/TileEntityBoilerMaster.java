package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.crafting.BoilerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartBoiler;
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
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TileEntityBoilerMaster extends TileEntityBoilerSlave implements ITFluidTank.TankListener, IComparatorOverride, IIEInventory, IBinaryMessageReceiver {

    private static final int inputTankSize = Multiblocks.boiler.boiler_input_tankSize;
    private static final int outputTankSize = Multiblocks.boiler.boiler_output_tankSize;
    private static final int inputFuelTankSize = Multiblocks.boiler.boiler_fuel_tankSize;
    private static final int heatLossPerTick = Multiblocks.boiler.boiler_heat_lossPerTick;
    private static final int progressLossPerTick = Multiblocks.boiler.boiler_progress_lossInTicks;
    private static final double workingHeatLevel = Multiblocks.boiler.boiler_heat_workingLevel;

    public FluidTank[] tanks = new FluidTank[] {
            new ITFluidTank(inputFuelTankSize, this),
            new ITFluidTank(inputTankSize, this),
            new ITFluidTank(outputTankSize, this)
    };

    public static int slotCount = 6;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);

    public int burnRemaining = 0;
    public int recipeTimeRemaining = 0;
    public double heatLevel = 0;
    private int clientUpdateCooldown = 20;
    public BoilerRecipe.BoilerFuelRecipe lastFuel;
    public BoilerRecipe lastRecipe;
    protected PoICache fluidInput0, fluidInput1, fluidOutput0, redstone0;
    private BlockPos fluidOutputFront0, soundPos0;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
        heatLevel = nbt.getDouble("heatLevel");
        burnRemaining = nbt.getInteger("burnRemaining");
        recipeTimeRemaining = nbt.getInteger("recipeTimeRemaining");
        if (!descPacket) inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
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

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (soundPos0 == null) InitializePoIs();
        float level = (float)(heatLevel / workingHeatLevel);
        if (level <= 0) ITSoundHandler.StopSound(soundPos0);
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos0.getX(), soundPos0.getY(), soundPos0.getZ()) / 8, 1);
            ITSounds.boiler.PlayRepeating(soundPos0, (2 * level) / attenuation, level);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() { ITSoundHandler.StopSound(soundPos0); super.onChunkUnload(); }

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
        super.disassemble();
    }

    public void notifyNearbyClients() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeDouble(heatLevel);
        BinaryMessageTileSync.sendToAllTracking(world, getPos(), buf);
    }

    @Override public void receiveMessageFromServer(ByteBuf message) {
        heatLevel = message.readDouble();
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {
    }

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    private boolean heatUp() {
        double previous = heatLevel;
        if (lastFuel == null) { burnRemaining = 0; return true; }
        heatLevel = Math.min(heatLevel + lastFuel.getHeat(), workingHeatLevel);
        return previous != heatLevel;
    }

    private boolean cooldown() {
        double previous = heatLevel;
        double multiplier = 1.0;
        if (ITCompatModule.isAdvancedRocketryLoaded) { multiplier = AdvancedRocketryHelper.getHeatTransferCoefficient(world, getPos().add(0, 2, 0)); }
        heatLevel = Math.max(heatLevel - heatLossPerTick * multiplier, 0);
        return previous != heatLevel;
    }

    private boolean loseProgress() {
        if (lastRecipe == null) { recipeTimeRemaining = 0; return true; }
        int previous = recipeTimeRemaining;
        recipeTimeRemaining = Math.min(recipeTimeRemaining + progressLossPerTick, lastRecipe.getTotalProcessTime());
        return previous != recipeTimeRemaining;
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
        if (tanks[2].getFluidAmount() == 0) { return; }
        if (fluidOutput0 == null) { InitializePoIs(); }
        IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputFront0, fluidOutput0.facing.getOpposite());
        if (output == null) { return; }
        FluidStack out = tanks[2].getFluid();
        int accepted = output.fill(out, false);
        if (accepted > 0) {
            assert out != null;
            int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
            tanks[2].drain(drained, true);
        }
    }

    private boolean heatLogic() {
        boolean update = false;
        boolean canCombust = true;
        if (ITCompatModule.isAdvancedRocketryLoaded) { canCombust = AdvancedRocketryHelper.isAtmosphereSuitableForCombustion(world, ITUtils.LocalOffsetToWorldBlockPos(getPos(), 3, 0, 1, facing, mirrored)); }
        if (canCombust) {
            if (burnRemaining > 0) {
                burnRemaining--;
                if (heatUp()) { update = true; }
            } else if (!isRSDisabled() && tanks[0].getFluidAmount() > 0) {
                BoilerRecipe.BoilerFuelRecipe fuel = (lastFuel != null && Objects.requireNonNull(tanks[0].getFluid()).isFluidEqual(lastFuel.fluidInput)) ? lastFuel : BoilerRecipe.findFuel(tanks[0].getFluid());
                if (fuel != null && fuel.fluidInput.amount <= tanks[0].getFluidAmount()) {
                    lastFuel = fuel;
                    tanks[0].drain(fuel.fluidInput.amount, true);
                    burnRemaining = fuel.getTotalProcessTime() - 1;
                    markContainingBlockForUpdate(null);
                    if (heatUp()) { update = true; }
                } else if (cooldown()) { update = true; }
            } else if (cooldown()) { update = true; }
        } else if (cooldown()) { update = true; }
        return update;
    }

    private boolean recipeLogic() {
        boolean update = false;
        if (heatLevel >= workingHeatLevel) {
            if (recipeTimeRemaining > 0) { if (gainProgress()) update = true; }
            else if (tanks[1].getFluidAmount() > 0) {
                BoilerRecipe recipe = (lastRecipe != null && Objects.requireNonNull(tanks[1].getFluid()).isFluidEqual(lastRecipe.fluidInput)) ? lastRecipe : BoilerRecipe.findRecipe(tanks[1].getFluid());
                if (recipe != null && recipe.fluidInput.amount <= tanks[1].getFluidAmount() && recipe.fluidOutput.amount == tanks[2].fillInternal(recipe.fluidOutput, false)) {
                    lastRecipe = recipe;
                    recipeTimeRemaining = recipe.getTotalProcessTime();
                    if (gainProgress()) { update = true; }
                }
            }
        } else if (recipeTimeRemaining > 0) { if (loseProgress()) update = true; }
        return update;
    }

    private boolean outputTankLogic() {
        boolean update = false;
        if (tanks[2].getFluidAmount() > 0) {
            ItemStack filled = Utils.fillFluidContainer(tanks[2], inventory.get(4), inventory.get(5), null);
            if (!filled.isEmpty()) {
                if (!inventory.get(5).isEmpty() && OreDictionary.itemMatches(inventory.get(5), filled, true)) { inventory.get(5).grow(filled.getCount()); }
                else if (inventory.get(5).isEmpty()) { inventory.set(5, filled.copy()); }
                inventory.get(4).shrink(1);
                if (inventory.get(4).getCount() <= 0) { inventory.set(4, ItemStack.EMPTY); }
                markContainingBlockForUpdate(null);
                update = true;
            }
            pumpOutputOut();
        }
        return update;
    }

    private boolean fuelTankLogic() {
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

    private boolean inputTankLogic() {
        int prev = tanks[1].getFluidAmount();
        ItemStack empty = Utils.drainFluidContainer(tanks[1], inventory.get(2), inventory.get(3), null);
        if (prev != tanks[1].getFluidAmount()) {
            if (!inventory.get(3).isEmpty() && OreDictionary.itemMatches(inventory.get(3), empty, true)) { inventory.get(3).grow(empty.getCount()); }
            else if (inventory.get(3).isEmpty()) { inventory.set(3, empty.copy()); }
            inventory.get(2).shrink(1);
            if (inventory.get(2).getCount() <= 0) { inventory.set(2, ItemStack.EMPTY); }
            markContainingBlockForUpdate(null);
            return true;
        }
        return false;
    }

    @Override public void update() {
        if (formed && redstone0 == null) InitializePoIs();
        super.update();
        if (!formed) return;
        if (world.isRemote) { handleSounds(); return; }
        boolean update = heatLogic();
        if (recipeLogic()) update = true;
        if (outputTankLogic()) update = true;
        if (fuelTankLogic()) update = true;
        if (inputTankLogic()) update = true;
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

    @Override public TileEntityBoilerMaster master() { return this; }

    @Override public void TankContentsChanged() { markContainingBlockForUpdate(null); }

    @Override public int getComparatorInputOverride() { return (int)(15 * (heatLevel / workingHeatLevel)); }

    @Override public void doGraphicalUpdates(int slot) {
        this.markDirty();
        this.markContainingBlockForUpdate(null);
    }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (redstone0 == null) InitializePoIs();
        if (side == null) return tanks;
        if (fluidInput0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        if (fluidInput1.isPoI(side, position)) return new IFluidTank[] {tanks[1]};
        if (fluidOutput0.isPoI(side, position)) return new IFluidTank[] {tanks[2]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (!formed || redstone0 == null) InitializePoIs();
        if (iTank == 0 && fluidInput0.isPoI(side, position)) {
            if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
            if (tanks[0].getFluid() == null) return BoilerRecipe.findFuel(resource) != null;
            return resource.isFluidEqual(tanks[0].getFluid());
        }
        if (iTank == 1 && fluidInput1.isPoI(side, position)) {
            if (tanks[1].getFluidAmount() >= tanks[1].getCapacity()) return false;
            if (tanks[1].getFluid() == null) return BoilerRecipe.findRecipe(resource) != null;
            return resource.isFluidEqual(tanks[1].getFluid());
        }
        return false;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (!formed || redstone0 == null) InitializePoIs();
        return iTank == 2 && fluidOutput0.isPoI(side, position) && tanks[2].getFluidAmount() > 0;
    }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstone0 == null) InitializePoIs();
        return new int[] {redstone0.position};
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartBoiler.instance.pointsOfInterest) {
            switch (poi.name) {
                case "fluid_input0":
                    fluidInput0 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_input1":
                    fluidInput1 = new PoICache(facing, poi, mirrored);
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

    public static class BoilerFluidHandler implements IFluidHandler {
        private final IFluidTank[] accessibleTanks;
        private final TileEntityBoilerMaster master;
        private final EnumFacing side;
        private final int position;

        public BoilerFluidHandler(IFluidTank[] accessibleTanks, TileEntityBoilerMaster master, EnumFacing side, int position) {
            this.accessibleTanks = accessibleTanks;
            this.master = master;
            this.side = side;
            this.position = position;
        }

        private int getTankIndex(IFluidTank tank) {
            for (int i = 0; i < master.tanks.length; i++) {
                if (master.tanks[i] == tank) return i;
            }
            return -1;
        }

        @Override public IFluidTankProperties[] getTankProperties() {
            List<IFluidTankProperties> list = new ArrayList<>();
            for (IFluidTank tank : accessibleTanks) {
                int index = getTankIndex(tank);
                boolean canFill = index == 0 || index == 1;
                boolean canDrain = index == 2;
                list.add(new FluidTankProperties(tank.getFluid(), tank.getCapacity(), canFill, canDrain));
            }
            return list.toArray(new IFluidTankProperties[0]);
        }

        @Override public int fill(FluidStack resource, boolean doFill) {
            if (resource == null || resource.amount <= 0) return 0;
            TileEntityBoilerMaster master = this.master;
            if (master == null) return 0;
            for (int i = 0; i < master.tanks.length; i++) {
                if (master.canFillTankFrom(i, side, resource, position)) {
                    int filled = master.tanks[i].fill(resource, doFill);
                    if (filled > 0 && doFill) master.markDirty();
                    return filled;
                }
            }
            return 0;
        }

        @Override public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.amount <= 0) return null;
            TileEntityBoilerMaster master = this.master;
            if (master == null) return null;
            for (int i = 0; i < master.tanks.length; i++) {
                if (master.canDrainTankFrom(i, side, position)) {
                    FluidStack tankFluid = master.tanks[i].getFluid();
                    if (tankFluid != null && tankFluid.isFluidEqual(resource)) {
                        FluidStack drained = master.tanks[i].drain(resource.amount, doDrain);
                        if (drained != null && doDrain) master.markDirty();
                        return drained;
                    }
                }
            }
            return null;
        }

        @Override public FluidStack drain(int maxDrain, boolean doDrain) {
            if (maxDrain <= 0) return null;
            TileEntityBoilerMaster master = this.master;
            if (master == null) return null;
            for (int i = 0; i < master.tanks.length; i++) {
                if (master.canDrainTankFrom(i, side, position)) {
                    FluidStack drained = master.tanks[i].drain(maxDrain, doDrain);
                    if (drained != null && doDrain) master.markDirty();
                    return drained;
                }
            }
            return null;
        }
    }
}
