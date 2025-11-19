package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IMirrorAble;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IUsesBooleanProperty;
import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.DistillerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.blocks.multiblocks.TileEntityITMultiblockPartDistiller;
import mctmods.immersivetechnology.common.util.ITFluidTank;
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
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityDistillerMaster extends TileEntityDistillerSlave implements ITFluidTank.TankListener, IMirrorAble {
    private static final int inputTankSize = Multiblocks.distiller.distiller_input_tankSize;
    private static final int outputTankSize = Multiblocks.distiller.distiller_output_tankSize;

    public ITFluidTank[] tanks = new ITFluidTank[] {new ITFluidTank(inputTankSize, this), new ITFluidTank(outputTankSize, this)};

    public static int slotCount = 4;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);

    private boolean running;
    private boolean previousRenderState;
    private float soundVolume;

    private PoICache energyInput, redstone, fluidInput, fluidOutput;
    private BlockPos fluidOutputFront;

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        running = nbt.getBoolean("running");
        if (!descPacket) inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
    }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setBoolean("running", running);
        if (!descPacket) nbt.setTag("inventory", Utils.writeInventory(inventory));
    }

    private void pumpOutputOut() {
        if (tanks[1].getFluidAmount() == 0) return;
        if (fluidOutputFront == null) InitializePoIs();
        IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputFront, fluidOutput.facing.getOpposite());
        if (output == null) return;
        FluidStack out = tanks[1].getFluid();
        int accepted = output.fill(out, false);
        if (accepted == 0) return;
        assert out != null;
        int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        this.tanks[1].drain(drained, true);
    }

    public void handleSounds() {
        if (running) { if (soundVolume < 1) soundVolume += 0.01f; }
        else if (soundVolume > 0) soundVolume -= 0.01f;
        BlockPos center = getPos();
        if (soundVolume == 0) ITSoundHandler.StopSound(center);
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float) player.getDistanceSq(center.getX(), center.getY(), center.getZ()) / 8, 1);
            ITSounds.distiller.PlayRepeating(center, soundVolume / attenuation, 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onChunkUnload() {
        ITSoundHandler.StopSound(getPos());
        super.onChunkUnload();
    }

    @Override
    public void disassemble() {
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

    @SuppressWarnings("unchecked")
    @Override
    public void update() {
        if (!formed) return;
        if (world.isRemote) { handleSounds(); return; }
        boolean update = false;
        if (energyStorage.getEnergyStored() > 0 && processQueue.size() < this.getProcessQueueMaxLength()) {
            if (tanks[0].getFluidAmount() > 0) {
                DistillerRecipe recipe = DistillerRecipe.findRecipe(tanks[0].getFluid());
                if (recipe != null) {
                    MultiblockProcessInMachine<DistillerRecipe> process = new MultiblockProcessInMachine<>(recipe).setInputTanks(0);
                    if (this.addProcessToQueue(process, false)) update = true;
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
        running = shouldRenderAsActive() && !processQueue.isEmpty() && processQueue.get(0).canProcess(this);
        if (previousRenderState != running) notifyNearbyClients();
        previousRenderState = running;
        if (update) {
            efficientMarkDirty();
            this.markContainingBlockForUpdate(null);
        }
    }

    @Override
    public void TankContentsChanged() { this.markContainingBlockForUpdate(null); }

    @Override
    public boolean isDummy() { return false; }

    @Override
    public TileEntityDistillerMaster master() { master = this; return this; }

    @Override
    public void receiveMessageFromServer(@Nonnull NBTTagCompound message) { running = message.getBoolean("running"); }

    @Override
    public boolean getIsMirrored() { return mirrored; }

    @Override
    public @Nonnull IEProperties.PropertyBoolInverted getBoolProperty(@Nonnull Class<? extends IUsesBooleanProperty> inf) { return IEProperties.BOOLEANS[0]; }

    @Override
    public void doProcessFluidOutput(@Nonnull FluidStack output) { tanks[1].fill(output, true); }

    @Override
    public void doProcessOutput(@Nonnull ItemStack output) {
        if (output.isEmpty()) return;
        if (fluidOutputFront == null) InitializePoIs();
        TileEntity inventoryTile = this.world.getTileEntity(fluidOutputFront);
        if (inventoryTile != null) output = Utils.insertStackIntoInventory(inventoryTile, output, fluidOutput.facing.getOpposite());
        if (!output.isEmpty()) Utils.dropStackAtPos(world, fluidOutputFront, output);
    }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, int position) {
        if (energyInput == null) InitializePoIs();
        return facing != null && energyInput.isPoI(facing, position);
    }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartDistiller.instance.pointsOfInterest) {
            switch (poi.name) {
                case "energy_input":
                    energyInput = new PoICache(facing, poi, mirrored);
                    break;
                case "redstone":
                    redstone = new PoICache(facing, poi, mirrored);
                    break;
                case "output":
                    fluidOutput = new PoICache(facing, poi, mirrored);
                    fluidOutputFront = getBlockPosForPos(fluidOutput.position).offset(fluidOutput.facing);
                    break;
                case "input":
                    fluidInput = new PoICache(facing, poi, mirrored);
                    break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(energyInput.position));
        notifyNeighbor(getBlockPosForPos(redstone.position));
        notifyNeighbor(getBlockPosForPos(fluidInput.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override
    public @Nonnull int[] getRedstonePos() {
        if (redstone == null) InitializePoIs();
        return new int[] {redstone.position};
    }

    @Override
    public @Nonnull int[] getEnergyPos() {
        if (energyInput == null) InitializePoIs();
        return new int[] {energyInput.position};
    }

    @Override
    protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if (fluidInput == null) InitializePoIs();
        if (fluidInput.isPoI(side, position)) return new ITFluidTank[] {tanks[0]};
        else if (fluidOutput.isPoI(side, position)) return new ITFluidTank[] {tanks[1]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override
    protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (!fluidInput.isPoI(side, position) || iTank != 0) return false;
        if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
        if (tanks[0].getFluid() == null) return DistillerRecipe.findRecipe(resource) != null;
        return resource.isFluidEqual(tanks[0].getFluid());
    }

    @Override
    protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        return fluidOutput.isPoI(side, position) && iTank == 1 && tanks[1].getFluidAmount() > 0;
    }
}
