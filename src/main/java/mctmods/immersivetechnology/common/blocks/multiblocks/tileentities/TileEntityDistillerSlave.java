package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IMirrorAble;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IUsesBooleanProperty;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.api.ITGUI;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.DistillerRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.tileentitiesmultiblockpart.TileEntityITMultiblockPartDistiller;
import mctmods.immersivetechnology.common.tileentities.TileEntityITMultiblock;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Objects;

public class TileEntityDistillerSlave extends TileEntityITMultiblock<TileEntityDistillerSlave, DistillerRecipe, TileEntityDistillerMaster> implements IGuiTile, IMirrorAble, IUsesBooleanProperty, IFluxReceiver, IIEInternalFluxHandler {
    public TileEntityDistillerSlave() { super(TileEntityITMultiblockPartDistiller.instance, 16000, true); }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override
    public void update() { if(isDummy()) ITUtils.RemoveDummyFromTicking(this); super.update(); }

    @Override
    public boolean isDummy() { return true; }

    TileEntityDistillerMaster master;

    public TileEntityDistillerMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityDistillerMaster ? (TileEntityDistillerMaster)te : null;
        return master;
    }

    @Override
    public NonNullList<ItemStack> getInventory() { return master() == null ? NonNullList.withSize(4, ItemStack.EMPTY) : master.inventory; }

    @Override
    public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override
    public int getSlotLimit(int slot) { return 64; }

    @Override
    public @Nonnull IFluidTank[] getInternalTanks() { return master() == null ? new IFluidTank[0] : master.tanks; }

    @Override
    protected @Nonnull DistillerRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return DistillerRecipe.loadFromNBT(tag); }

    @Override
    public @Nonnull int[] getRedstonePos() { return master() == null ? new int[0] : master.getRedstonePos(); }

    @Override
    public @Nonnull int[] getOutputTanks() { return new int[]{1}; }

    @Override
    public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<DistillerRecipe> process) { return true; }

    @Override
    public int getMaxProcessPerTick() { return 1; }

    @Override
    public int getProcessQueueMaxLength() { return 1; }

    @Override
    public float getMinProcessDistance(@Nonnull MultiblockProcess<DistillerRecipe> process) { return 1f; }

    @Override
    protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) { return master() != null ? master.getAccessibleFluidTanks(side, position) : ITUtils.emptyIFluidTankList; }

    @Override
    protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) { return master() != null && master.canFillTankFrom(iTank, side, resource, position); }

    @Override
    protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) { return master() != null && master.canDrainTankFrom(iTank, side, position); }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityEnergy.ENERGY && facing != null && master() != null && master.isEnergyPosition(facing, pos)) return true;
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nonnull <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityEnergy.ENERGY && facing != null && master() != null && master.isEnergyPosition(facing, pos)) return (T)new EnergyHelper.IEForgeEnergyWrapper(this, facing);
        return Objects.requireNonNull(super.getCapability(capability, facing));
    }

    @Override
    public @Nonnull FluxStorage getFluxStorage() { return master() == null ? new FluxStorage(0) : master.energyStorage; }

    @Override
    public @Nonnull SideConfig getEnergySideConfig(@Nullable EnumFacing facing) { return formed && master() != null && master.isEnergyPosition(facing, pos) ? SideConfig.INPUT : SideConfig.NONE; }

    @Override
    public int receiveEnergy(@Nullable EnumFacing from, int energy, boolean simulate) { return !formed ? 0 : energyStorage.receiveEnergy(energy, simulate); }

    @Override
    public boolean canOpenGui() { return formed; }

    @Override
    public int getGuiID() { return ITGUI.GUIID_Distiller; }

    @Override
    public TileEntity getGuiMaster() { return master(); }

    @Override
    public boolean getIsMirrored() { return mirrored; }

    @Override
    public @Nonnull IEProperties.PropertyBoolInverted getBoolProperty(@Nonnull Class<? extends IUsesBooleanProperty> inf) { return IEProperties.BOOLEANS[0]; }

    public BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartDistiller.instance.width;
        int length = TileEntityITMultiblockPartDistiller.instance.length;
        int h = pos / (width * length);
        int l = (pos % (width * length)) / width;
        int w = pos % width;
        return new BlockPos(w, h, l);
    }
}
