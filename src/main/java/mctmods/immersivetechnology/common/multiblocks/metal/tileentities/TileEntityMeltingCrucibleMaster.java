package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.crafting.MeltingCrucibleRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Optional;

public class TileEntityMeltingCrucibleMaster extends TileEntityMeltingCrucibleSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver, IIEInventory, IComparatorOverride {

    private static final int outputTankSize = Multiblocks.meltingCrucible.meltingCrucible_output_tankSize;
    private static final int energyCapacity = Multiblocks.meltingCrucible.meltingCrucible_energy_size;
    private static final int energyMaxInput = Multiblocks.meltingCrucible.meltingCrucible_energy_maxInput;

    public FluxStorageAdvanced energyStorage = new FluxStorageAdvanced(energyCapacity, energyMaxInput, energyMaxInput);
    public FluidTank[] tanks = new FluidTank[] {
            new ITFluidTank(outputTankSize, this)
    };
    public IItemHandler insertionHandler = new IEInventoryHandler(slotCount, this, 0, new boolean[]{true}, new boolean[]{false});

    public static int slotCount = 1;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    private PoICache energyInput0, fluidOutput0, itemInput0, redstone0;
    private BlockPos sound0, fluidOutputPos0;
    private float soundVolume = 0f;
    private int soundGracePeriod = 60;
    private double distanceSqToTE = Double.MAX_VALUE;
    private int playerDimension = Integer.MIN_VALUE;
    private boolean isRunning = false;
    public boolean redstoneControlInverted = false;
    public Optional<Boolean> computerOn = Optional.empty();
    private boolean needsPoIInit = false;

    public MeltingCrucibleRecipe cachedMeltingRecipe;
    public int processEnergyRemaining = 0;
    private int oldComparatorOutput = 0;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
        energyStorage.readFromNBT(nbt.getCompoundTag("energy"));
        processEnergyRemaining = nbt.getInteger("processEnergyRemaining");
        if (!descPacket && formed) needsPoIInit = true;
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("inventory", Utils.writeInventory(inventory));
        nbt.setTag("energy", energyStorage.writeToNBT(new NBTTagCompound()));
        nbt.setInteger("processEnergyRemaining", processEnergyRemaining);
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (sound0 == null) InitializePoIs();
        if (distanceSqToTE > 4096) {
            ITSoundHandler.StopSound(sound0);
            soundVolume = 0f;
            return;
        }
        if (soundVolume <= 0f) {
            ITSoundHandler.StopSound(sound0);
        } else {
            double distance = Math.sqrt(distanceSqToTE);
            float attenuation = Math.max((float)distance / 16f, 1f);
            ITSounds.heatExchanger.PlayRepeating(sound0, soundVolume / attenuation, 1f);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        ITSoundHandler.StopSound(sound0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        if (sound0 == null) InitializePoIs();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(sound0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), sound0.getX(), sound0.getY(), sound0.getZ(), 0));
        if (!world.isRemote) {
            for (ItemStack stack : inventory) {
                if (!stack.isEmpty()) Utils.dropStackAtPos(world, getPos(), stack.copy());
            }
            inventory.clear();
        }
        super.disassemble();
    }

    @SideOnly(Side.CLIENT)
    private void clientUpdate() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double currentDistance = player.getDistanceSq(sound0.getX() + 0.5, sound0.getY() + 0.5, sound0.getZ() + 0.5);
        if (world.provider.getDimension() == player.dimension && currentDistance < 400 && (distanceSqToTE > 400 || playerDimension != player.dimension)) requestUpdate();
        distanceSqToTE = currentDistance;
        playerDimension = player.dimension;

        float targetSoundLevel = isRunning ? 1f : 0f;
        if (soundVolume < targetSoundLevel) {
            soundVolume = Math.min(soundVolume + 0.02f, targetSoundLevel);
            soundGracePeriod = 60;
        } else if (soundVolume > targetSoundLevel) {
            if (soundGracePeriod > 0) soundGracePeriod--;
            else soundVolume = Math.max(soundVolume - 0.02f, targetSoundLevel);
        }
        handleSounds();
    }

    private void requestUpdate() { ImmersiveTechnology.packetHandler.sendToServer(new BinaryMessageTileSync(getPos(), Unpooled.buffer())); }

    public void notifyNearbyClients() { ImmersiveTechnology.packetHandler.sendToAllAround(new BinaryMessageTileSync(getPos(), Unpooled.copyBoolean(isRunning)), new NetworkRegistry.TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 40)); }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) { ImmersiveTechnology.packetHandler.sendTo(new BinaryMessageTileSync(getPos(), Unpooled.copyBoolean(isRunning)), player); }

    @Override public void receiveMessageFromServer(ByteBuf message) { isRunning = message.readBoolean(); }

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    private boolean pumpOutputOut() {
        if (tanks[0].getFluidAmount() == 0) return false;
        if (fluidOutput0 == null) InitializePoIs();
        IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputPos0, fluidOutput0.facing.getOpposite());
        if (output == null) return false;
        FluidStack out = tanks[0].getFluid();
        if (out == null || out.amount <= 0) return false;
        int accepted = output.fill(out.copy(), false);
        if (accepted == 0) return false;
        int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        tanks[0].drain(drained, true);
        return drained > 0;
    }

    @Override public void update() {
        super.update();
        if (!formed) return;
        if (needsPoIInit || energyInput0 == null) {
            InitializePoIs();
            needsPoIInit = false;
        }
        if (world.isRemote) {
            clientUpdate();
            return;
        }

        boolean update = pumpOutputOut();

        boolean shouldRun = !isRSDisabled();

        if (processEnergyRemaining == 0 && shouldRun) {
            ItemStack inputStack = inventory.get(0);
            if (!inputStack.isEmpty()) {
                MeltingCrucibleRecipe recipe = MeltingCrucibleRecipe.findRecipe(inputStack);
                if (recipe != null && inputStack.getCount() >= recipe.itemInput.inputSize && tanks[0].fill(recipe.fluidOutput, false) == recipe.fluidOutput.amount) {
                    cachedMeltingRecipe = recipe;
                    inputStack.shrink(recipe.itemInput.inputSize);
                    if (inputStack.getCount() <= 0) inventory.set(0, ItemStack.EMPTY);
                    processEnergyRemaining = recipe.getTotalProcessEnergy();
                    update = true;
                }
            }
        }

        if (processEnergyRemaining > 0 && shouldRun) {
            if (cachedMeltingRecipe != null) {
                int energyPerTick = cachedMeltingRecipe.getTotalProcessEnergy() / cachedMeltingRecipe.getTotalProcessTime();
                int consume = Math.min(energyPerTick, processEnergyRemaining);
                int extracted = energyStorage.extractEnergy(consume, true);
                if (extracted >= consume) {
                    energyStorage.extractEnergy(consume, false);
                    processEnergyRemaining -= consume;
                    update = true;
                    if (processEnergyRemaining <= 0) {
                        processEnergyRemaining = 0;
                        tanks[0].fill(cachedMeltingRecipe.fluidOutput, true);
                        cachedMeltingRecipe = null;
                    }
                }
            } else {
                processEnergyRemaining = 0;
            }
        }

        boolean wasRunning = isRunning;
        isRunning = processEnergyRemaining > 0 && shouldRun;
        if (isRunning != wasRunning) {
            notifyNearbyClients();
            update = true;
        }

        int comp = getComparatorInputOverride();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            world.notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
            update = true;
        }

        if (update) {
            efficientMarkDirty();
            markContainingBlockForUpdate(null);
        }
    }

    @Override public void TankContentsChanged() { markContainingBlockForUpdate(null); }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityMeltingCrucibleMaster master() { return this; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (fluidOutput0 == null) InitializePoIs();
        if (side == null || fluidOutput0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) { return false; }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (fluidOutput0 == null) InitializePoIs();
        return iTank == 0 && fluidOutput0.isPoI(side, position) && tanks[0].getFluidAmount() > 0;
    }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstone0 == null) InitializePoIs();
        return new int[] {redstone0.position};
    }

    @Override public boolean isRSDisabled() {
        if (computerOn.isPresent()) return !computerOn.get();
        int[] rsPositions = getRedstonePos();
        if (rsPositions.length < 1) return false;
        for (int rsPos : rsPositions) {
            TileEntity tile = world.getTileEntity(getBlockPosForPos(rsPos));
            if (tile != null) {
                int power = world.getRedstonePowerFromNeighbors(tile.getPos());
                boolean b = power > 0;
                return redstoneControlInverted != b;
            }
        }
        return false;
    }

    @Override @Nonnull public int[] getEnergyPos() {
        if (!formed) return new int[0];
        if (energyInput0 == null) InitializePoIs();
        return new int[] {energyInput0.position};
    }

    @Override @Nonnull public FluxStorageAdvanced getFluxStorage() { return energyStorage; }

    @Override @Nonnull public NonNullList<ItemStack> getInventory() { return inventory; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) {
        markDirty();
        markContainingBlockForUpdate(null);
    }

    @Override @Nonnull public NonNullList<ItemStack> getDroppedItems() { return getInventory(); }

    @Override public int getComparatedSize() { return slotCount; }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, int position) {
        if (!formed) return false;
        if (energyInput0 == null) InitializePoIs();
        return facing != null && energyInput0.isPoI(facing, position);
    }

    public boolean isItemInputPosition(@Nullable EnumFacing facing, int position) {
        if (!formed) return false;
        if (itemInput0 == null) InitializePoIs();
        return facing != null && itemInput0.isPoI(facing, position);
    }

    private void InitializePoIs() {
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
        if (!world.isRemote) notifyIONeighbors();
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

    @Override public int getComparatorInputOverride() {
        return 15 * tanks[0].getFluidAmount() / tanks[0].getCapacity();
    }
}
