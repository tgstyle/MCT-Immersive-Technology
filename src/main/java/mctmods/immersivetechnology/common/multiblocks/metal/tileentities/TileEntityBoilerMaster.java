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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
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

    public int fuelBurnRemaining = 0;
    public int processTimeRemaining = 0;
    public double heatLevel = 0;

    private float soundVolume = 0f;
    private int soundGracePeriod = 0;
    private boolean isRunning = false;
    public BoilerRecipe.BoilerFuelRecipe cachedFuelRecipe;
    public BoilerRecipe cachedBoilerRecipe;
    public boolean redstoneControlInverted = false;
    private int oldComparatorOutput = 0;
    private int tickCountdown = 5;

    private boolean needsPoIInit = false;
    private boolean needsNotify = false;

    protected PoICache fluidInputPos0, fluidInputPos1, fluidOutputPos0, redstonePos0;
    private BlockPos fluidOutputTEPos0, soundPos0;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
        heatLevel = nbt.getDouble("heatLevel");
        fuelBurnRemaining = nbt.getInteger("fuelBurnRemaining");
        processTimeRemaining = nbt.getInteger("processTimeRemaining");
        redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
        oldComparatorOutput = nbt.getInteger("oldComparatorOutput");
        soundGracePeriod = nbt.getInteger("soundGracePeriod");
        if (!descPacket) inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
        if (formed && !descPacket) {
            needsPoIInit = true;
            needsNotify = true;
        }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank2", tanks[2].writeToNBT(new NBTTagCompound()));
        nbt.setDouble("heatLevel", heatLevel);
        nbt.setInteger("fuelBurnRemaining", fuelBurnRemaining);
        nbt.setInteger("processTimeRemaining", processTimeRemaining);
        nbt.setBoolean("redstoneControlInverted", redstoneControlInverted);
        nbt.setInteger("oldComparatorOutput", oldComparatorOutput);
        nbt.setInteger("soundGracePeriod", soundGracePeriod);
        if (!descPacket) nbt.setTag("inventory", Utils.writeInventory(inventory));
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (soundPos0 == null) InitializePoIs();
        float targetSoundLevel = isRunning ? (float)(heatLevel / workingHeatLevel) : 0f;
        if (soundVolume < targetSoundLevel) { soundVolume = Math.min(soundVolume + 0.01f, targetSoundLevel); }
        else if (soundVolume > targetSoundLevel) { soundVolume = Math.max(soundVolume - 0.01f, targetSoundLevel); }
        if (soundVolume <= 0f) { ITSoundHandler.StopSound(soundPos0); }
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos0.getX() + 0.5, soundPos0.getY() + 0.5, soundPos0.getZ() + 0.5) / 8, 1);
            ITSounds.boiler.PlayRepeating(soundPos0, (2 * soundVolume) / attenuation, soundVolume);
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
            for (ItemStack stack : inventory) if (!stack.isEmpty()) Utils.dropStackAtPos(world, getPos(), stack);
            inventory.clear();
        }
        super.disassemble();
    }

    void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartBoiler.instance.pointsOfInterest) {
            switch (poi.name) {
                case "fluid_input0":
                    fluidInputPos0 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_input1":
                    fluidInputPos1 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_output0":
                    fluidOutputPos0 = new PoICache(facing, poi, mirrored);
                    fluidOutputTEPos0 = getBlockPosForPos(fluidOutputPos0.position).offset(fluidOutputPos0.facing);
                    break;
                case "redstone0":
                    redstonePos0 = new PoICache(facing, poi, mirrored);
                    break;
                case "sound0":
                    soundPos0 = getBlockPosForPos(poi.position);
                    break;
            }
        }
    }

    private void notifyIONeighbors() {
        if (fluidInputPos0 != null) world.notifyNeighborsOfStateChange(getBlockPosForPos(fluidInputPos0.position), getBlockType(), true);
        if (fluidInputPos1 != null) world.notifyNeighborsOfStateChange(getBlockPosForPos(fluidInputPos1.position), getBlockType(), true);
        if (fluidOutputPos0 != null) world.notifyNeighborsOfStateChange(getBlockPosForPos(fluidOutputPos0.position), getBlockType(), true);
        if (redstonePos0 != null) world.updateComparatorOutputLevel(getBlockPosForPos(redstonePos0.position), getBlockType());
    }

    private void notifyNearbyClients() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeDouble(heatLevel);
        buf.writeInt(fuelBurnRemaining);
        buf.writeInt(processTimeRemaining);
        buf.writeBoolean(isRunning);
        BinaryMessageTileSync.sendToAllTracking(world, getPos(), buf);
    }

    @Override public void receiveMessageFromServer(ByteBuf message) {
        heatLevel = message.readDouble();
        fuelBurnRemaining = message.readInt();
        processTimeRemaining = message.readInt();
        isRunning = message.readBoolean();
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {}

    @Override public void update() {
        super.update();
        if (!formed) return;
        if (needsPoIInit || fluidInputPos0 == null || fluidInputPos1 == null || fluidOutputPos0 == null || redstonePos0 == null || soundPos0 == null) {
            InitializePoIs();
            needsPoIInit = false;
        }
        if (needsNotify) {
            notifyIONeighbors();
            needsNotify = false;
        }
        if (world.isRemote) {
            handleSounds();
            return;
        }
        boolean changed = heatLogic();
        if (recipeLogic()) changed = true;
        if (outputTankLogic()) changed = true;
        if (fuelTankLogic()) changed = true;
        if (inputTankLogic()) changed = true;

        boolean didWork = fuelBurnRemaining > 0 || processTimeRemaining > 0;
        if (didWork) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;
        boolean wasRunning = isRunning;
        isRunning = soundGracePeriod > 0;

        if (changed || isRunning != wasRunning) {
            if (tickCountdown-- <= 0) {
                notifyNearbyClients();
                tickCountdown = 5;
            }
            world.markChunkDirty(getPos(), this);
        }
        int comp = getComparatorInputOverride();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            if (redstonePos0 != null) world.updateComparatorOutputLevel(getBlockPosForPos(redstonePos0.position), getBlockType());
        }
        if (changed) markContainingBlockForUpdate(null);
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            if (fluidInputPos0 == null) InitializePoIs();
            if (fluidInputPos0.isPoI(facing, pos) || fluidInputPos1.isPoI(facing, pos) || fluidOutputPos0.isPoI(facing, pos)) return true;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            if (fluidInputPos0.isPoI(facing, pos) || fluidInputPos1.isPoI(facing, pos) || fluidOutputPos0.isPoI(facing, pos)) {
                return (T)new BoilerFluidHandler(getAccessibleFluidTanks(facing, pos), this, facing, pos);
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityBoilerMaster master() { return this; }

    @Override public boolean isRSDisabled() {
        int[] rsPositions = getRedstonePos();
        if (rsPositions.length < 1) return false;
        for (int rsPos : rsPositions) {
            TileEntity tile = world.getTileEntity(getBlockPosForPos(rsPos));
            if (tile != null) return redstoneControlInverted != (world.getRedstonePowerFromNeighbors(tile.getPos()) > 0);
        }
        return false;
    }

    @Override public TileEntity getGuiMaster() { return this; }

    private boolean heatLogic() {
        boolean update = false;
        boolean canCombust = true;
        if (ITCompatModule.isAdvancedRocketryLoaded) { canCombust = AdvancedRocketryHelper.isAtmosphereSuitableForCombustion(world, ITUtils.LocalOffsetToWorldBlockPos(getPos(), 3, 0, 1, facing, mirrored)); }
        if (canCombust) {
            if (fuelBurnRemaining > 0) {
                fuelBurnRemaining--;
                if (heatUp()) update = true;
            } else if (!isRSDisabled() && tanks[0].getFluidAmount() > 0) {
                cachedFuelRecipe = (cachedFuelRecipe != null && Objects.requireNonNull(tanks[0].getFluid()).isFluidEqual(cachedFuelRecipe.fluidInput)) ? cachedFuelRecipe : BoilerRecipe.findFuel(tanks[0].getFluid());
                if (cachedFuelRecipe != null && cachedFuelRecipe.fluidInput.amount <= tanks[0].getFluidAmount()) {
                    tanks[0].drain(cachedFuelRecipe.fluidInput.amount, true);
                    fuelBurnRemaining = cachedFuelRecipe.getTotalProcessTime() - 1;
                    heatUp();
                    update = true;
                } else if (cooldown()) update = true;
            } else if (cooldown()) update = true;
        } else if (cooldown()) update = true;
        return update;
    }

    private boolean recipeLogic() {
        boolean update = false;
        if (heatLevel >= workingHeatLevel) {
            if (processTimeRemaining > 0) {
                if (gainProgress()) update = true;
            } else if (tanks[1].getFluidAmount() > 0) {
                cachedBoilerRecipe = (cachedBoilerRecipe != null && Objects.requireNonNull(tanks[1].getFluid()).isFluidEqual(cachedBoilerRecipe.fluidInput)) ? cachedBoilerRecipe : BoilerRecipe.findRecipe(tanks[1].getFluid());
                if (cachedBoilerRecipe != null && cachedBoilerRecipe.fluidInput.amount <= tanks[1].getFluidAmount() && cachedBoilerRecipe.fluidOutput.amount == tanks[2].fillInternal(cachedBoilerRecipe.fluidOutput, false)) {
                    processTimeRemaining = cachedBoilerRecipe.getTotalProcessTime();
                    if (gainProgress()) update = true;
                }
            }
        } else if (processTimeRemaining > 0) {
            if (loseProgress()) update = true;
        }
        return update;
    }

    private boolean outputTankLogic() {
        boolean update = false;
        if (tanks[2].getFluidAmount() > 0) {
            ItemStack filled = Utils.fillFluidContainer(tanks[2], inventory.get(4), inventory.get(5), null);
            if (!filled.isEmpty()) {
                if (!inventory.get(5).isEmpty() && OreDictionary.itemMatches(inventory.get(5), filled, true)) inventory.get(5).grow(filled.getCount());
                else if (inventory.get(5).isEmpty()) inventory.set(5, filled.copy());
                inventory.get(4).shrink(1);
                if (inventory.get(4).getCount() <= 0) inventory.set(4, ItemStack.EMPTY);
                update = true;
            }
            if (pumpOutputOut()) update = true;
        }
        return update;
    }

    private boolean fuelTankLogic() {
        int prev = tanks[0].getFluidAmount();
        ItemStack empty = Utils.drainFluidContainer(tanks[0], inventory.get(0), inventory.get(1), null);
        if (prev != tanks[0].getFluidAmount()) {
            if (!inventory.get(1).isEmpty() && OreDictionary.itemMatches(inventory.get(1), empty, true)) inventory.get(1).grow(empty.getCount());
            else if (inventory.get(1).isEmpty()) inventory.set(1, empty.copy());
            inventory.get(0).shrink(1);
            if (inventory.get(0).getCount() <= 0) inventory.set(0, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    private boolean inputTankLogic() {
        int prev = tanks[1].getFluidAmount();
        ItemStack empty = Utils.drainFluidContainer(tanks[1], inventory.get(2), inventory.get(3), null);
        if (prev != tanks[1].getFluidAmount()) {
            if (!inventory.get(3).isEmpty() && OreDictionary.itemMatches(inventory.get(3), empty, true)) inventory.get(3).grow(empty.getCount());
            else if (inventory.get(3).isEmpty()) inventory.set(3, empty.copy());
            inventory.get(2).shrink(1);
            if (inventory.get(2).getCount() <= 0) inventory.set(2, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    private boolean heatUp() {
        double previous = heatLevel;
        if (cachedFuelRecipe == null) { fuelBurnRemaining = 0; return true; }
        heatLevel = Math.min(heatLevel + cachedFuelRecipe.getHeat(), workingHeatLevel);
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
        if (cachedBoilerRecipe == null) { processTimeRemaining = 0; return true; }
        int previous = processTimeRemaining;
        processTimeRemaining = Math.min(processTimeRemaining + progressLossPerTick, cachedBoilerRecipe.getTotalProcessTime());
        return previous != processTimeRemaining;
    }

    private boolean gainProgress() {
        if (cachedBoilerRecipe == null) { processTimeRemaining = 0; return true; }
        processTimeRemaining--;
        if (processTimeRemaining == 0) {
            tanks[1].drain(cachedBoilerRecipe.fluidInput.amount, true);
            tanks[2].fillInternal(cachedBoilerRecipe.fluidOutput, true);
            return true;
        }
        return false;
    }

    private boolean pumpOutputOut() {
        if (tanks[2].getFluidAmount() == 0) return false;
        if (fluidOutputPos0 == null) InitializePoIs();
        IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputTEPos0, fluidOutputPos0.facing.getOpposite());
        if (output == null) return false;
        FluidStack out = tanks[2].getFluid();
        int accepted = output.fill(out, false);
        if (accepted <= 0) return false;
        assert out != null;
        int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        tanks[2].drain(drained, true);
        return drained > 0;
    }

    @Override public void TankContentsChanged() { markContainingBlockForUpdate(null); }

    @Override public int getComparatorInputOverride() { return (int)(15 * (heatLevel / workingHeatLevel)); }

    @Override @Nonnull public NonNullList<ItemStack> getInventory() { return inventory; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) { markDirty(); markContainingBlockForUpdate(null); }

    @Override @Nonnull public NonNullList<ItemStack> getDroppedItems() { return inventory; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (fluidInputPos0 == null) InitializePoIs();
        if (side == null) return tanks;
        if (fluidInputPos0.isPoI(side, position)) return new IFluidTank[]{tanks[0]};
        if (fluidInputPos1.isPoI(side, position)) return new IFluidTank[]{tanks[1]};
        if (fluidOutputPos0.isPoI(side, position)) return new IFluidTank[]{tanks[2]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (fluidInputPos0 == null) InitializePoIs();
        if (iTank == 0 && fluidInputPos0.isPoI(side, position)) {
            if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
            if (tanks[0].getFluid() == null) return BoilerRecipe.findFuel(resource) != null;
            return resource.isFluidEqual(tanks[0].getFluid());
        }
        if (iTank == 1 && fluidInputPos1.isPoI(side, position)) {
            if (tanks[1].getFluidAmount() >= tanks[1].getCapacity()) return false;
            if (tanks[1].getFluid() == null) return BoilerRecipe.findRecipe(resource) != null;
            return resource.isFluidEqual(tanks[1].getFluid());
        }
        return false;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (fluidOutputPos0 == null) InitializePoIs();
        return iTank == 2 && fluidOutputPos0.isPoI(side, position);
    }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstonePos0 == null) InitializePoIs();
        return new int[]{redstonePos0.position};
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }

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
            if (resource == null) return 0;
            resource = resource.copy();
            int filled = 0;
            for (IFluidTank accessible : accessibleTanks) {
                int iTank = getTankIndex(accessible);
                if (iTank != -1 && master.canFillTankFrom(iTank, side, resource, position)) {
                    int f = accessible.fill(resource, doFill);
                    filled += f;
                    resource.amount -= f;
                    if (doFill && f > 0) master.TankContentsChanged();
                    if (resource.amount <= 0) return filled;
                }
            }
            return filled;
        }

        @Override public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null) return null;
            resource = resource.copy();
            FluidStack drained = null;
            for (IFluidTank accessible : accessibleTanks) {
                int iTank = getTankIndex(accessible);
                if (iTank != -1 && master.canDrainTankFrom(iTank, side, position)) {
                    FluidStack tankFluid = accessible.getFluid();
                    if (tankFluid != null && tankFluid.isFluidEqual(resource)) {
                        int amount = Math.min(resource.amount, tankFluid.amount);
                        FluidStack d = accessible.drain(amount, doDrain);
                        if (d != null) {
                            if (drained == null) drained = d.copy();
                            else drained.amount += d.amount;
                            resource.amount -= d.amount;
                            if (doDrain && d.amount > 0) master.TankContentsChanged();
                            if (resource.amount <= 0) return drained;
                        }
                    }
                }
            }
            return drained;
        }

        @Override public FluidStack drain(int maxDrain, boolean doDrain) {
            int toDrain = maxDrain;
            FluidStack drained = null;
            for (IFluidTank accessible : accessibleTanks) {
                int iTank = getTankIndex(accessible);
                if (iTank != -1 && master.canDrainTankFrom(iTank, side, position)) {
                    FluidStack d = accessible.drain(toDrain, doDrain);
                    if (d != null) {
                        if (drained == null) drained = d.copy();
                        else if (drained.isFluidEqual(d)) drained.amount += d.amount;
                        toDrain -= d.amount;
                        if (doDrain && d.amount > 0) master.TankContentsChanged();
                        if (toDrain <= 0) return drained;
                    }
                }
            }
            return drained;
        }
    }
}
