package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.api.crafting.MeltingCrucibleRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartMeltingCrucible;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;
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
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class TileEntityMeltingCrucibleMaster extends TileEntityMeltingCrucibleSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver, IIEInventory {

    private static final int outputTankSize = Multiblocks.meltingCrucible.meltingCrucible_output_tankSize;
    private static final int energyCapacity = Multiblocks.meltingCrucible.meltingCrucible_energy_size;
    private static final int energyMaxInput = Multiblocks.meltingCrucible.meltingCrucible_energy_maxInput;

    public FluxStorageAdvanced energyStorage = new FluxStorageAdvanced(energyCapacity, energyMaxInput, energyMaxInput);
    public FluidTank[] tanks = new FluidTank[] {new ITFluidTank(outputTankSize, this)};
    public static int slotCount = 1;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    public IItemHandler insertionHandler = new IEInventoryHandler(slotCount, this, 0, new boolean[]{true}, new boolean[]{false});
    private PoICache energyInput0, fluidOutput0, itemInput0, redstone0;
    private BlockPos sound0, fluidOutputPos0;
    private float soundVolume;
    private double distanceSqToTE = Double.MAX_VALUE;
    private int playerDimension = Integer.MIN_VALUE;
    private boolean isRunning;
    private boolean needsNeighborNotify = false;
    protected boolean redstoneControlInverted = false;
    public Optional<Boolean> computerOn = Optional.empty();

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
        energyStorage.readFromNBT(nbt.getCompoundTag("energy"));
        needsNeighborNotify = nbt.getBoolean("needsNeighborNotify");
        if (!descPacket && formed) { needsNeighborNotify = true; }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("inventory", Utils.writeInventory(inventory));
        nbt.setTag("energy", energyStorage.writeToNBT(new NBTTagCompound()));
        nbt.setBoolean("needsNeighborNotify", needsNeighborNotify);
    }

    public void handleSounds() {
        if (distanceSqToTE > 4096) { ITSoundHandler.StopSound(sound0); soundVolume = 0; return; }
        if (isRunning) { if (soundVolume < 1) soundVolume += 0.01f; }
        else { if (soundVolume > 0) soundVolume -= 0.01f; }
        if (soundVolume == 0) { ITSoundHandler.StopSound(sound0); }
        else {
            float attenuation = Math.max((float)distanceSqToTE / 64f, 1f);
            ITSounds.heatExchanger.PlayRepeating(sound0, soundVolume / (4 * attenuation), 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        ITSoundHandler.StopSound(sound0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        if (sound0 == null) { InitializePoIs(); }
        if (sound0 != null) {
            ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(sound0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), sound0.getX(), sound0.getY(), sound0.getZ(), 0));
        }
        if (!world.isRemote) {
            for (ItemStack stack : inventory) {
                if (!stack.isEmpty()) {
                    Utils.dropStackAtPos(world, getPos(), stack.copy());
                }
            }
            inventory.clear();
        }
        super.disassemble();
    }

    private void clientUpdate() {
        if (sound0 == null) { InitializePoIs(); }
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double currentDistance = player.getDistanceSq(sound0.getX() + 0.5, sound0.getY() + 0.5, sound0.getZ() + 0.5);
        if (world.provider.getDimension() == player.dimension && currentDistance < 400 && (distanceSqToTE > 400 || playerDimension != player.dimension)) { requestUpdate(); }
        distanceSqToTE = currentDistance;
        playerDimension = player.dimension;
        handleSounds();
    }

    public void requestUpdate() { BinaryMessageTileSync.sendToServer(getPos(), Unpooled.copyBoolean(true)); }

    public void notifyNearbyClients() {
        ImmersiveTechnology.packetHandler.sendToAllAround(new BinaryMessageTileSync(getPos(), Unpooled.copyBoolean(isRunning)), new NetworkRegistry.TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 40));
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) { BinaryMessageTileSync.sendToPlayer(player, getPos(), Unpooled.copyBoolean(isRunning)); }

    @Override public void receiveMessageFromServer(ByteBuf message) { isRunning = message.readBoolean(); }

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    public static class MeltingProcess extends MultiblockProcessInMachine<MeltingCrucibleRecipe> {
        public MeltingProcess(MeltingCrucibleRecipe recipe, int... inputSlots) { super(recipe, inputSlots); }

        private int getEnergyPerTick() {
            if (recipe.getTotalProcessTime() <= 0) { return 0; }
            return recipe.getTotalProcessEnergy() / recipe.getTotalProcessTime();
        }

        @Override public boolean canProcess(@Nonnull TileEntityMultiblockMetal multiblock) {
            int energyPerTick = getEnergyPerTick();
            if (energyPerTick == 0) { return true; }
            int simulated = multiblock.getFluxStorage().extractEnergy(energyPerTick, true);
            if (simulated < energyPerTick) { return false; }
            TileEntityMeltingCrucibleMaster master = (TileEntityMeltingCrucibleMaster)multiblock;
            return master.tanks[0].fill(recipe.fluidOutput, false) == recipe.fluidOutput.amount;
        }

        @Override public void doProcessTick(@Nonnull TileEntityMultiblockMetal multiblock) {
            int energyPerTick = getEnergyPerTick();
            if (energyPerTick > 0) {
                int extracted = multiblock.getFluxStorage().extractEnergy(energyPerTick, false);
                if (extracted < energyPerTick) { return; }
            }
            super.doProcessTick(multiblock);
        }
    }

    @Override @Nonnull protected MultiblockProcess<MeltingCrucibleRecipe> loadProcessFromNBT(@Nonnull NBTTagCompound tag) {
        MeltingCrucibleRecipe recipe = MeltingCrucibleRecipe.loadFromNBT(tag);
        return new MeltingProcess(recipe, tag.getIntArray("process_inputSlots"));
    }

    private void pumpOutputOut() {
        if (tanks[0].getFluidAmount() == 0) { return; }
        if (fluidOutput0 == null) { InitializePoIs(); }
        IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputPos0, fluidOutput0.facing.getOpposite());
        if (output == null) { return; }
        FluidStack out = tanks[0].getFluid();
        int accepted = output.fill(out, false);
        if (accepted == 0) { return; }
        assert out != null;
        int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        tanks[0].drain(drained, true);
    }

    @Override public void update() {
        if (formed && redstone0 == null) { InitializePoIs(); }
        if (!world.isRemote && needsNeighborNotify && formed) { notifyIONeighbors(); needsNeighborNotify = false; }
        if (world.isRemote) { clientUpdate(); return; }
        super.update();
        pumpOutputOut();
        boolean update = false;
        if (!isRSDisabled() && processQueue.size() < getProcessQueueMaxLength()) {
            ItemStack inputStack = inventory.get(0);
            if (!inputStack.isEmpty()) {
                MeltingCrucibleRecipe recipe = MeltingCrucibleRecipe.findRecipe(inputStack);
                if (recipe != null) {
                    MeltingProcess process = new MeltingProcess(recipe, 0);
                    if (inputStack.getCount() >= recipe.itemInput.inputSize && process.canProcess(this) && addProcessToQueue(process, true)) {
                        inputStack.shrink(recipe.itemInput.inputSize);
                        addProcessToQueue(process, false);
                        update = true;
                    }
                }
            }
        }
        boolean wasRunning = isRunning;
        isRunning = shouldRenderAsActive() && !processQueue.isEmpty() && processQueue.get(0).canProcess(this);
        if (isRunning != wasRunning) { notifyNearbyClients(); }
        if (update) {
            efficientMarkDirty();
            markContainingBlockForUpdate(null);
        }
    }

    @Override public void onProcessFinish(@Nonnull MultiblockProcess<MeltingCrucibleRecipe> process) {
        tanks[0].fill(process.recipe.fluidOutput, true);
        markDirty();
        markContainingBlockForUpdate(null);
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityMeltingCrucibleMaster master() { return this; }

    @Override public void TankContentsChanged() { markContainingBlockForUpdate(null); }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        if (!formed) { return ITUtils.emptyIFluidTankList; }
        if (side == null) { return new IFluidTank[] {tanks[0]}; }
        if (redstone0 == null) { InitializePoIs(); }
        if (isFluidOutputPosition(side, position)) { return new IFluidTank[] {tanks[0]}; }
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) { return false; }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (redstone0 == null) { InitializePoIs(); }
        return isFluidOutputPosition(side, position);
    }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) { return new int[0]; }
        if (redstone0 == null) { InitializePoIs(); }
        return new int[] {redstone0.position};
    }

    @Override public boolean isRSDisabled() {
        if (computerOn.isPresent()) { return !computerOn.get(); }
        int[] rsPositions = getRedstonePos();
        if (rsPositions.length < 1) { return false; }
        for (int rsPos : rsPositions) {
            TileEntityMeltingCrucibleSlave tile = getTileForPos(rsPos);
            if (tile != null) {
                int power = world.getRedstonePowerFromNeighbors(tile.getPos());
                boolean b = power > 0;
                return redstoneControlInverted != b;
            }
        }
        return false;
    }

    @Override @Nonnull public int[] getEnergyPos() {
        if (!formed) { return new int[0]; }
        if (energyInput0 == null) { InitializePoIs(); }
        return new int[] {energyInput0.position};
    }

    @Override @Nonnull public FluxStorage getFluxStorage() { return energyStorage; }

    @Override public NonNullList<ItemStack> getDroppedItems() { return getInventory(); }

    @Override public int getComparatedSize() { return getInventory().size(); }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) {
        markDirty();
        markContainingBlockForUpdate(null);
    }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, int position) {
        if (!formed) { return false; }
        if (energyInput0 == null) { InitializePoIs(); }
        return facing != null && energyInput0.isPoI(facing, position);
    }

    public boolean isItemInputPosition(@Nullable EnumFacing facing, int position) {
        if (!formed) { return false; }
        if (itemInput0 == null) { InitializePoIs(); }
        return facing != null && itemInput0.isPoI(facing, position);
    }

    public boolean isFluidOutputPosition(@Nullable EnumFacing side, int position) {
        if (!formed) { return false; }
        if (fluidOutput0 == null) { InitializePoIs(); }
        return side != null && fluidOutput0.isPoI(side, position);
    }

    void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartMeltingCrucible.instance.pointsOfInterest) {
            switch (poi.name) {
                case "redstone0":
                    redstone0 = new PoICache(facing, poi, mirrored);
                    break;
                case "energy_input0":
                    energyInput0 = new PoICache(facing, poi, mirrored);
                    break;
                case "item_input0":
                    itemInput0 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_output0":
                    fluidOutput0 = new PoICache(facing, poi, mirrored);
                    fluidOutputPos0 = getBlockPosForPos(fluidOutput0.position).offset(fluidOutput0.facing);
                    break;
                case "sound0":
                    sound0 = getBlockPosForPos(poi.position);
                    break;
            }
        }
        if (!world.isRemote) { notifyIONeighbors(); }
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(itemInput0.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput0.position));
        notifyNeighbor(getBlockPosForPos(redstone0.position));
        notifyNeighbor(getBlockPosForPos(energyInput0.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }
}
