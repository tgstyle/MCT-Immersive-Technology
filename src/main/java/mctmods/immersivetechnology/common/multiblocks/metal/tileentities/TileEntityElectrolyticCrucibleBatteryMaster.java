package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.client.ICSoundHandler;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces;
import com.immersiveconvergence.api.multiblock.TileEntityTemplateMultiblock;
import com.immersiveconvergence.api.network.BinaryTileSyncMessage;
import com.immersiveconvergence.api.network.IBinaryMessageReceiver;
import com.immersiveconvergence.api.util.ICFluidTank;
import com.immersiveconvergence.api.util.ICInventoryHandler;
import com.immersiveconvergence.api.util.IICInventory;
import com.immersiveconvergence.api.util.ICFluxStorageAdvanced;
import com.immersiveconvergence.api.util.ICUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.api.crafting.ElectrolyticCrucibleBatteryRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityElectrolyticCrucibleBatteryMaster extends TileEntityElectrolyticCrucibleBatterySlave implements ICFluidTank.TankListener, IBinaryMessageReceiver, ICBlockInterfaces.IMirrorAble, ICBlockInterfaces.IUsesBooleanProperty, ICBlockInterfaces.IComparatorOverride, IICInventory {
    private static int inputTankSize() { return Multiblocks.electrolyticCrucibleBattery.electrolyticCrucibleBattery_input_tankSize; }

    private static int outputTankSize() { return Multiblocks.electrolyticCrucibleBattery.electrolyticCrucibleBattery_output_tankSize; }

    private static int energyCapacity() { return Multiblocks.electrolyticCrucibleBattery.electrolyticCrucibleBattery_energy_size; }

    private static int energyMaxInput() { return Multiblocks.electrolyticCrucibleBattery.electrolyticCrucibleBattery_energy_maxInput; }

    public static final int slotCount = 1;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    public final IItemHandler extractionHandler = new ICInventoryHandler(slotCount, this, 0, new boolean[]{false}, new boolean[]{true});
    public ICFluxStorageAdvanced energyStorage = new ICFluxStorageAdvanced(energyCapacity(), energyMaxInput(), energyMaxInput());
    public FluidTank[] tanks = new FluidTank[]{
            new ICFluidTank(inputTankSize(), this),
            new ICFluidTank(outputTankSize(), this),
            new ICFluidTank(outputTankSize(), this),
            new ICFluidTank(outputTankSize(), this)
    };

    private float soundVolume = 0f;
    private int soundGracePeriod = 0;
    private boolean isRunning = false;
    private double distanceToTE;
    private int playerDimension;
    public boolean redstoneControlInverted = false;
    private int oldComparatorOutput;

    private int tickCountdown = 5;
    private int oldEnergy;
    private boolean oldIsRunning;

    public void efficientMarkDirty() {
        world.getChunk(getPos()).markDirty();
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        energyStorage.readFromNBT(nbt.getCompoundTag("energy"));
        if (nbt.hasKey("inventory")) inventory = ICUtils.readInventory(nbt.getTagList("inventory", 10), slotCount);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
        tanks[3].readFromNBT(nbt.getCompoundTag("tank3"));
        redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
        isRunning = nbt.getBoolean("isRunning");
        soundGracePeriod = nbt.getInteger("soundGracePeriod");
        oldComparatorOutput = nbt.getInteger("oldComparatorOutput");
        if (!descPacket && !formed) processQueue.clear();
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("inventory", ICUtils.writeInventory(inventory));
        nbt.setTag("energy", energyStorage.writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank2", tanks[2].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank3", tanks[3].writeToNBT(new NBTTagCompound()));
        nbt.setBoolean("redstoneControlInverted", redstoneControlInverted);
        nbt.setBoolean("isRunning", isRunning);
        nbt.setInteger("soundGracePeriod", soundGracePeriod);
        nbt.setInteger("oldComparatorOutput", oldComparatorOutput);
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        BlockPos soundPos = poiWorldPos("sound0");
        float targetSoundLevel = isRunning ? 1f : 0f;
        if (soundVolume < targetSoundLevel) { soundVolume = Math.min(soundVolume + 0.01f, targetSoundLevel); }
        else if (soundVolume > targetSoundLevel) { soundVolume = Math.max(soundVolume - 0.01f, targetSoundLevel); }
        if (soundVolume <= 0f) { ICSoundHandler.stopSound(soundPos); }
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float) player.getDistanceSq(soundPos.getX() + .5, soundPos.getY() + .5, soundPos.getZ() + .5) / 32, 1);
            ITSounds.electrolyticCrucibleBattery.PlayRepeating(soundPos, soundVolume / attenuation, 1f);
        }
    }

    @SideOnly(Side.CLIENT)
    private void clientUpdate() {
        BlockPos soundPos = poiWorldPos("sound0");
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distSq = player.getDistanceSq(soundPos.getX() + .5, soundPos.getY() + .5, soundPos.getZ() + .5);
        if (world.provider.getDimension() == player.dimension && distSq < 400 && (distanceToTE > 400 || playerDimension != player.dimension)) requestUpdate();
        distanceToTE = distSq;
        playerDimension = player.dimension;
        handleSounds();
    }

    private boolean pushItemOut() {
        if (inventory.get(0).isEmpty()) return false;
        TileEntity target = world.getTileEntity(poiFrontPos("item_output0"));
        if (target == null) return false;
        ItemStack before = inventory.get(0).copy();
        ItemStack remaining = ICUtils.insertStackIntoInventory(target, before, poi("item_output0").facing.getOpposite());
        inventory.set(0, remaining == null ? ItemStack.EMPTY : remaining);
        return inventory.get(0).getCount() != before.getCount();
    }

    public boolean isItemOutputPoI(@Nullable EnumFacing side, BlockPos position) {
        return isPoI("item_output0", side, position);
    }

    @Override @Nonnull public NonNullList<ItemStack> getInventory() { return inventory; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) { efficientMarkDirty(); }

    @Override public void disassemble() {
        if (!world.isRemote) {
            for (ItemStack stack : inventory) { if (!stack.isEmpty()) ICUtils.dropStackAtPos(world, getPos(), stack); }
            inventory.clear();
        }
        super.disassemble();
    }

    public void requestUpdate() {
        BinaryTileSyncMessage.sendToServer(getPos(), Unpooled.copyBoolean(true));
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {
        boolean request = message.readBoolean();
        if (request) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeInt(energyStorage.getEnergyStored());
            buf.writeBoolean(isRunning);
            BinaryTileSyncMessage.sendToPlayer(player, getPos(), buf);
        }
    }

    @Override public void receiveMessageFromServer(ByteBuf message) {
        int readEnergy = message.readInt();
        energyStorage.modifyEnergyStored(readEnergy - energyStorage.getEnergyStored());
        isRunning = message.readBoolean();
    }

    @Override public void update() {
        if (!formed) return;
        if (world.isRemote) {
            clientUpdate();
            return;
        }
        super.update();
        boolean update = pumpOutputOut();
        if (pushItemOut()) update = true;
        boolean wasRunning = isRunning;
        if (processQueue.size() < getProcessQueueMaxLength()) {
            FluidStack input = tanks[0].getFluid();
            if (input != null && input.amount > 0) {
                ElectrolyticCrucibleBatteryRecipe recipe = ElectrolyticCrucibleBatteryRecipe.findRecipe(input);
                if (recipe != null) {
                    MultiblockProcess<ElectrolyticCrucibleBatteryRecipe> process = new ElectrolyticCrucibleBatteryProcess(recipe).setInputTanks(0);
                    if (addProcessToQueue(process, true)) {
                        addProcessToQueue(process, false);
                        update = true;
                    }
                }
            }
        }
        boolean didWork = tickedProcesses > 0 && !isRSDisabled();
        if (didWork) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;
        isRunning = soundGracePeriod > 0;
        int currentEnergy = energyStorage.getEnergyStored();
        boolean changed = oldEnergy != currentEnergy || oldIsRunning != isRunning;
        if (changed && tickCountdown-- <= 0) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeInt(currentEnergy);
            buf.writeBoolean(isRunning);
            BinaryTileSyncMessage.sendToAllTracking(world, getPos(), buf);
            tickCountdown = 5;
            world.markChunkDirty(getPos(), this);
            if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
            else { throttledBlockUpdate(); }
        }
        int comparator = comparatorValue();
        if (comparator != oldComparatorOutput) {
            oldComparatorOutput = comparator;
            notifyComparators();
        }
        oldEnergy = currentEnergy;
        oldIsRunning = isRunning;
        if (update || changed) {
            if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
            else { throttledBlockUpdate(); }
        }
    }

    @Override public TileEntityElectrolyticCrucibleBatteryMaster master() { return this; }

    private boolean pumpOutputOut() {
        boolean update = false;
        IFluidHandler handler;
        if (tanks[1].getFluidAmount() > 0) {
            handler = FluidUtil.getFluidHandler(world, poiFrontPos("fluid_output0"), poi("fluid_output0").facing.getOpposite());
            if (handler != null) {
                FluidStack out = tanks[1].getFluid();
                if (out != null) {
                    int accepted = handler.fill(out, false);
                    if (accepted > 0) {
                        int drained = handler.fill(ICUtils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                        tanks[1].drain(drained, true);
                        update |= drained > 0;
                    }
                }
            }
        }
        if (tanks[2].getFluidAmount() > 0) {
            handler = FluidUtil.getFluidHandler(world, poiFrontPos("fluid_output1"), poi("fluid_output1").facing.getOpposite());
            if (handler != null) {
                FluidStack out = tanks[2].getFluid();
                if (out != null) {
                    int accepted = handler.fill(out, false);
                    if (accepted > 0) {
                        int drained = handler.fill(ICUtils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                        tanks[2].drain(drained, true);
                        update |= drained > 0;
                    }
                }
            }
        }
        if (tanks[3].getFluidAmount() > 0) {
            handler = FluidUtil.getFluidHandler(world, poiFrontPos("fluid_output2"), poi("fluid_output2").facing.getOpposite());
            if (handler != null) {
                FluidStack out = tanks[3].getFluid();
                if (out != null) {
                    int accepted = handler.fill(out, false);
                    if (accepted > 0) {
                        int drained = handler.fill(ICUtils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                        tanks[3].drain(drained, true);
                        update |= drained > 0;
                    }
                }
            }
        }
        return update;
    }

    @Override public void TankContentsChanged() {
        requestClientSync();
    }

    @Override public int getComparatorInputOverride() { return isComparatorPos() ? comparatorValue() : 0; }

    public int comparatorValue() {
        if (!formed) return 0;
        return 15 * tanks[1].getFluidAmount() / tanks[1].getCapacity();
    }

    @Override public boolean isDummy() { return false; }

    @Override @Nonnull public ICFluxStorageAdvanced getStorage() { return energyStorage; }

    @Override public boolean getIsMirrored() { return mirrored; }

    @Override public int getProcessQueueMaxLength() { return 3; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<ElectrolyticCrucibleBatteryRecipe> process) { return true; }

    @Override public void onProcessFinish(@Nonnull MultiblockProcess<ElectrolyticCrucibleBatteryRecipe> process) {
        if (process.recipe == null) return;
        tanks[1].fillInternal(process.recipe.fluidOutput0, true);
        if (process.recipe.fluidOutput1 != null) tanks[2].fillInternal(process.recipe.fluidOutput1, true);
        if (process.recipe.fluidOutput2 != null) tanks[3].fillInternal(process.recipe.fluidOutput2, true);
        if (process.recipe.itemOutput != null && !process.recipe.itemOutput.isEmpty()) {
            ItemStack buffered = inventory.get(0);
            ItemStack produced = process.recipe.itemOutput.copy();
            if (buffered.isEmpty()) { inventory.set(0, produced); }
            else if (ItemHandlerHelper.canItemStacksStack(buffered, produced)) { buffered.grow(produced.getCount()); }
        }
    }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, BlockPos position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (side == null) return tanks;
        if (isPoI("fluid_input0", side, position)) return tankView(0, tanks[0]);
        if (isPoI("fluid_output0", side, position)) return tankView(1, tanks[1]);
        if (isPoI("fluid_output1", side, position)) return tankView(2, tanks[2]);
        if (isPoI("fluid_output2", side, position)) return tankView(3, tanks[3]);
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        if (!formed) return false;
        if (!isPoI("fluid_input0", side, position)) return false;
        if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
        FluidStack current = tanks[0].getFluid();
        return current == null || resource.isFluidEqual(current);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        if (!formed) return false;
        if (isPoI("fluid_output0", side, position)) return tanks[1].getFluidAmount() > 0;
        if (isPoI("fluid_output1", side, position)) return tanks[2].getFluidAmount() > 0;
        if (isPoI("fluid_output2", side, position)) return tanks[3].getFluidAmount() > 0;
        return false;
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return ITUtils.EMPTY_INT_ARRAY; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return ITUtils.EMPTY_INT_ARRAY; }

    @Override @Nonnull protected MultiblockProcess<ElectrolyticCrucibleBatteryRecipe> loadProcessFromNBT(@Nonnull NBTTagCompound tag) {
        ElectrolyticCrucibleBatteryRecipe recipe = ElectrolyticCrucibleBatteryRecipe.loadFromNBT(tag);
        int[] inputSlots = tag.getIntArray("process_inputSlots");
        int[] inputTanks = tag.getIntArray("process_inputTanks");
        ElectrolyticCrucibleBatteryProcess process = new ElectrolyticCrucibleBatteryProcess(recipe, inputSlots);
        if (inputTanks.length > 0) process.setInputTanks(inputTanks);
        if (tag.hasKey("process_maxTicks")) { process.maxTicks = tag.getInteger("process_maxTicks"); }
        return process;
    }

    @Override @Nonnull protected NBTTagCompound writeProcessToNBT(@Nonnull MultiblockProcess<ElectrolyticCrucibleBatteryRecipe> process) {
        NBTTagCompound tag = super.writeProcessToNBT(process);
        tag.setInteger("process_maxTicks", process.maxTicks);
        return tag;
    }

    static class ElectrolyticCrucibleBatteryProcess extends ProcessInMachine<ElectrolyticCrucibleBatteryRecipe> {
        public ElectrolyticCrucibleBatteryProcess(ElectrolyticCrucibleBatteryRecipe recipe, int... inputSlots) { super(recipe, inputSlots); }

        private int getEnergyPerTick() { return (int) Math.floor((float) recipe.getTotalProcessEnergy() / recipe.getTotalProcessTime()); }

        @Override @Nonnull public ElectrolyticCrucibleBatteryProcess setInputTanks(@Nonnull int... tanks) { super.setInputTanks(tanks); return this; }

        @Override public boolean canProcess(TileEntityTemplateMultiblock<?, ?, ?> multiblock) {
            TileEntityElectrolyticCrucibleBatteryMaster master = (TileEntityElectrolyticCrucibleBatteryMaster) multiblock;
            if (recipe == null) return false;
            int energyPerTick = getEnergyPerTick();
            if (energyPerTick > 0 && master.energyStorage.extractEnergy(energyPerTick, true) < energyPerTick) return false;
            if (recipe.fluidOutput0 == null || recipe.fluidOutput0.getFluid() == null || master.tanks[1].fillInternal(recipe.fluidOutput0, false) != recipe.fluidOutput0.amount) return false;
            if (recipe.fluidOutput1 != null && (recipe.fluidOutput1.getFluid() == null || master.tanks[2].fillInternal(recipe.fluidOutput1, false) != recipe.fluidOutput1.amount)) return false;
            return recipe.fluidOutput2 == null || recipe.fluidOutput2.getFluid() == null || master.tanks[3].fillInternal(recipe.fluidOutput2, false) == recipe.fluidOutput2.amount;
        }

        @Override public void doProcessTick(TileEntityTemplateMultiblock<?, ?, ?> multiblock) {
            if (recipe == null) return;
            int energyPerTick = getEnergyPerTick();
            if (energyPerTick > 0) ((TileEntityElectrolyticCrucibleBatteryMaster) multiblock).energyStorage.extractEnergy(energyPerTick, false);
            super.doProcessTick(multiblock);
        }
    }
}
