package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IMirrorAble;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IUsesBooleanProperty;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.crafting.DistillerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartDistiller;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.ITSounds;
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
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TileEntityDistillerMaster extends TileEntityDistillerSlave implements ITFluidTank.TankListener, IMirrorAble, IIEInventory {
    private static final int inputTankSize = Multiblocks.distiller.distiller_input_tankSize;
    private static final int outputTankSize = Multiblocks.distiller.distiller_output_tankSize;
    private static final int energyCapacity = Multiblocks.distiller.distiller_energy_size;
    private static final int energyMaxInput = Multiblocks.distiller.distiller_energy_maxInput;

    public FluxStorageAdvanced energyStorage = new FluxStorageAdvanced(energyCapacity, energyMaxInput, energyMaxInput);

    public ITFluidTank[] tanks = new ITFluidTank[] {new ITFluidTank(inputTankSize, this), new ITFluidTank(outputTankSize, this)};

    public static int slotCount = 5;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);

    private boolean running;
    private boolean previousRenderState;
    private float soundVolume;

    private PoICache energyInput0;
    private PoICache redstone0;
    private PoICache fluidInput0;
    private PoICache fluidOutput0;
    PoICache itemOutput0;
    private BlockPos fluidOutputPos0, itemOutputPos0;

    public TileEntityDistillerMaster() { super(); }

    @Override @Nonnull public FluxStorage getFluxStorage() { return energyStorage; }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        energyStorage.readFromNBT(nbt.getCompoundTag("energy"));
        running = nbt.getBoolean("running");
        if (!descPacket) inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setTag("energy", energyStorage.writeToNBT(new NBTTagCompound()));
        nbt.setBoolean("running", running);
        if (!descPacket) nbt.setTag("inventory", Utils.writeInventory(inventory));
    }

    private void pumpOutputOut() {
        if (tanks[1].getFluidAmount() == 0) return;
        IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputPos0, fluidOutput0.facing.getOpposite());
        if (output == null) return;
        FluidStack out = tanks[1].getFluid();
        int accepted = output.fill(out, false);
        if (accepted == 0) return;
        assert out != null;
        int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        this.tanks[1].drain(drained, true);
    }

    public void handleSounds() {
        if (running) { if (soundVolume < 1) soundVolume += 0.01f; } else if (soundVolume > 0) soundVolume -= 0.01f;
        BlockPos center = getPos();
        if (soundVolume == 0) ITSoundHandler.StopSound(center);
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float) player.getDistanceSq(center.getX(), center.getY(), center.getZ()) / 8, 1);
            ITSounds.distiller.PlayRepeating(center, soundVolume / attenuation, 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() { ITSoundHandler.StopSound(getPos()); super.onChunkUnload(); }

    @Override public void disassemble() {
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(center), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
        super.disassemble();
    }

    public void notifyNearbyClients() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("running", running);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageTileSync(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
    }

    public void efficientMarkDirty() { world.getChunk(this.getPos()).markDirty(); }

    public static class DistillerProcess extends MultiblockProcessInMachine<DistillerRecipe> {
        public DistillerProcess(DistillerRecipe recipe, int... inputSlots) { super(recipe, inputSlots); }

        private int getEnergyPerTick() { return this.recipe.getTotalProcessEnergy() / this.recipe.getTotalProcessTime(); }

        @Override @Nonnull public DistillerProcess setInputTanks(@Nonnull int... tanks) { super.setInputTanks(tanks); return this; }

        @Override public boolean canProcess(TileEntityMultiblockMetal multiblock) {
            int energyPerTick = getEnergyPerTick();
            int simulated = multiblock.getFluxStorage().extractEnergy(energyPerTick, true);
            if (simulated < energyPerTick) return false;
            List<ItemStack> outputs = recipe.getItemOutputs();
            if (outputs != null && !outputs.isEmpty()) {
                int[] outputSlots = multiblock.getOutputSlots();
                for (ItemStack output : outputs) {
                    if (!output.isEmpty()) {
                        boolean canOutput = false;
                        for (int iOutputSlot : outputSlots) {
                            ItemStack s = multiblock.getInventory().get(iOutputSlot);
                            if (s.isEmpty() || ItemHandlerHelper.canItemStacksStack(s, output) && s.getCount() + output.getCount() <= multiblock.getSlotLimit(iOutputSlot)) {
                                canOutput = true;
                                break;
                            }
                        }
                        if (!canOutput) return false;
                    }
                }
            }
            List<FluidStack> fluidOutputs = recipe.getFluidOutputs();
            if (fluidOutputs != null && !fluidOutputs.isEmpty()) {
                IFluidTank[] tanks = multiblock.getInternalTanks();
                int[] outputTanks = multiblock.getOutputTanks();
                for (FluidStack output : fluidOutputs) {
                    if (output != null && output.amount > 0) {
                        boolean canOutput = false;
                        for (int iOutputTank : outputTanks) {
                            if (iOutputTank >= 0 && iOutputTank < tanks.length && tanks[iOutputTank] != null && tanks[iOutputTank].fill(output, false) == output.amount) {
                                canOutput = true;
                                break;
                            }
                        }
                        if (!canOutput) return false;
                    }
                }
            }
            return true;
        }

        @Override public void doProcessTick(@Nonnull TileEntityMultiblockMetal multiblock) {
            int energyPerTick = getEnergyPerTick();
            multiblock.getFluxStorage().extractEnergy(energyPerTick, false);
            super.doProcessTick(multiblock);
        }
    }

    @Override public void update() {
        if (formed && redstone0 == null) InitializePoIs();
        if (!formed) return;
        if (world.isRemote) { handleSounds(); return; }
        boolean update = false;
        if (energyStorage.getEnergyStored() > 0 && processQueue.size() < this.getProcessQueueMaxLength()) {
            FluidStack input = tanks[0].getFluid();
            if (input != null && input.amount > 0) {
                DistillerRecipe recipe = DistillerRecipe.findRecipe(input);
                if (recipe != null) {
                    DistillerProcess process = new DistillerProcess(recipe, new int[0]).setInputTanks(0);
                    if (process.canProcess(this) && this.addProcessToQueue(process, true)) {
                        tanks[0].drain(recipe.fluidInput.amount, true);
                        this.addProcessToQueue(process, false);
                        update = true;
                    }
                }
            }
        }
        super.update();
        if (this.tanks[1].getFluidAmount() > 0) {
            ItemStack filledContainer = Utils.fillFluidContainer(tanks[1], inventory.get(2), inventory.get(3), null);
            if (!filledContainer.isEmpty()) {
                if (!inventory.get(3).isEmpty() && OreDictionary.itemMatches(inventory.get(3), filledContainer, true)) inventory.get(3).grow(filledContainer.getCount());
                else if (inventory.get(3).isEmpty()) inventory.set(3, filledContainer.copy());
                inventory.get(2).shrink(1);
                if (inventory.get(2).getCount() <= 0) inventory.set(2, ItemStack.EMPTY);
            }
        }
        ItemStack emptyContainer = Utils.drainFluidContainer(tanks[0], inventory.get(0), inventory.get(1), null);
        pumpOutputOut();
        if (!emptyContainer.isEmpty() && emptyContainer.getCount() > 0) {
            if (!inventory.get(1).isEmpty() && OreDictionary.itemMatches(inventory.get(1), emptyContainer, true)) inventory.get(1).grow(emptyContainer.getCount());
            else if (inventory.get(1).isEmpty()) inventory.set(1, emptyContainer.copy());
            inventory.get(0).shrink(1);
            if (inventory.get(0).getCount() <= 0) inventory.set(0, ItemStack.EMPTY);
        }
        for (int slot : new int[]{1, 3, 4}) {
            ItemStack stack = inventory.get(slot);
            if (stack.isEmpty()) continue;
            if (itemOutput0 == null) InitializePoIs();
            TileEntity te = world.getTileEntity(itemOutputPos0);
            if (te != null) {
                IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, itemOutput0.facing);
                if (handler != null) stack = ItemHandlerHelper.insertItemStacked(handler, stack, false);
            }
            inventory.set(slot, stack);
        }
        running = shouldRenderAsActive() && !processQueue.isEmpty() && processQueue.get(0).canProcess(this);
        if (previousRenderState != running) notifyNearbyClients();
        previousRenderState = running;
        if (update) { efficientMarkDirty(); this.markContainingBlockForUpdate(null); }
    }

    @Override public void TankContentsChanged() { this.markContainingBlockForUpdate(null); }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityDistillerMaster master() { master = this; return this; }

    @Override public void receiveMessageFromServer(@Nonnull NBTTagCompound message) { running = message.getBoolean("running"); }

    @Override public boolean getIsMirrored() { return mirrored; }

    @Override public @Nonnull IEProperties.PropertyBoolInverted getBoolProperty(@Nonnull Class<? extends IUsesBooleanProperty> inf) { return IEProperties.BOOLEANS[0]; }

    @Override public void doProcessFluidOutput(@Nonnull FluidStack output) { tanks[1].fill(output, true); }

    @Override public void doProcessOutput(@Nonnull ItemStack output) {
        if (output.isEmpty()) return;
        ItemStack current = inventory.get(4);
        if (current.isEmpty()) { inventory.set(4, output.copy()); }
        else if (OreDictionary.itemMatches(current, output, true) && current.getCount() + output.getCount() <= current.getMaxStackSize()) current.grow(output.getCount());
    }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, int position) {
        if (!formed) return false;
        if (energyInput0 == null) InitializePoIs();
        return facing != null && energyInput0.isPoI(facing, position);
    }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartDistiller.instance.pointsOfInterest) {
            switch (poi.name) {
                case "energy_input0":
                    energyInput0 = new PoICache(this.facing, poi, this.mirrored);
                    break;
                case "redstone0":
                    redstone0 = new PoICache(this.facing, poi, this.mirrored);
                    break;
                case "fluid_output0":
                    fluidOutput0 = new PoICache(this.facing, poi, this.mirrored);
                    fluidOutputPos0 = getBlockPosForPos(fluidOutput0.position).offset(fluidOutput0.facing);
                    break;
                case "fluid_input0":
                    fluidInput0 = new PoICache(this.facing, poi, this.mirrored);
                    break;
                case "item_output0":
                    itemOutput0 = new PoICache(this.facing, poi, this.mirrored);
                    itemOutputPos0 = getBlockPosForPos(itemOutput0.position).offset(itemOutput0.facing.getOpposite());
                    break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(energyInput0.position));
        notifyNeighbor(getBlockPosForPos(redstone0.position));
        notifyNeighbor(getBlockPosForPos(fluidInput0.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput0.position));
        notifyNeighbor(getBlockPosForPos(itemOutput0.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstone0 == null) InitializePoIs();
        return new int[] {redstone0.position};
    }

    @Override public @Nonnull int[] getEnergyPos() {
        if (!formed) return new int[0];
        if (energyInput0 == null) InitializePoIs();
        return new int[] {energyInput0.position};
    }

    @Override public @Nonnull int[] getOutputSlots() { return new int[]{4}; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<DistillerRecipe> process) {
        DistillerRecipe recipe = process.recipe;
        return recipe.fluidOutput == null || recipe.fluidOutput.amount <= 0 || tanks[1].getFluidAmount() + recipe.fluidOutput.amount <= tanks[1].getCapacity();
    }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (fluidInput0 == null) InitializePoIs();
        if (fluidInput0.isPoI(side, position)) return new ITFluidTank[] {tanks[0]};
        if (fluidOutput0.isPoI(side, position)) return new ITFluidTank[] {tanks[1]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (fluidInput0 == null) InitializePoIs();
        if (!fluidInput0.isPoI(side, position) || iTank != 0) return false;
        if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
        if (tanks[0].getFluid() == null) return DistillerRecipe.findRecipe(resource) != null;
        return resource.isFluidEqual(tanks[0].getFluid());
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (fluidOutput0 == null) InitializePoIs();
        return fluidOutput0.isPoI(side, position) && iTank == 1;
    }

    @Override public boolean isRSDisabled() {
        if (computerOn.isPresent()) { boolean on = computerOn.get(); return !on; }
        int[] rsPositions = getRedstonePos();
        if (rsPositions.length < 1) return false;
        for (int rsPos : rsPositions) {
            TileEntityDistillerSlave tile = getTileForPos(rsPos);
            if (tile != null) {
                BlockPos pos = tile.getPos();
                int power = world.getRedstonePowerFromNeighbors(pos);
                boolean b = power > 0;
                return redstoneControlInverted != b;
            }
        }
        return false;
    }

    @Override @Nonnull protected MultiblockProcess<DistillerRecipe> loadProcessFromNBT(@Nonnull NBTTagCompound tag) {
        DistillerRecipe recipe = readRecipeFromNBT(tag);
        DistillerProcess process = new DistillerProcess(recipe, tag.getIntArray("process_inputSlots"));
        process.setInputTanks(tag.getIntArray("process_inputTanks"));
        return process;
    }

    @Override public NonNullList<ItemStack> getInventory() { return inventory; }

    @Override public boolean isStackValid(int slot, ItemStack stack) {
        if (slot == 0 || slot == 2) return stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        return false;
    }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) { efficientMarkDirty(); markContainingBlockForUpdate(null); }
}
