package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.capability.IMechanicalEnergyConsumer;
import com.immersiveconvergence.api.client.MechanicalEnergyAnimation;
import com.immersiveconvergence.api.multiblock.GenericShape;

import mctmods.immersivetechnology.api.crafting.DummyRecipe;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartAlternator;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces;
import com.immersiveconvergence.api.multiblock.TileEntityTemplateMultiblock;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.util.ITUtils;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

public class TileEntityAlternatorSlave extends TileEntityTemplateMultiblock<TileEntityAlternatorSlave, IMultiblockRecipe, TileEntityAlternatorMaster>
        implements IMechanicalEnergyConsumer, IIEInternalFluxHandler, ICBlockInterfaces.IBlockBounds,
        ICBlockInterfaces.ICollisionBounds, ICBlockInterfaces.ISelectionBounds, IComparatorOverride {

    private int loadGrace = 0;
    private TileEntityAlternatorMaster master;

    public TileEntityAlternatorSlave() {
        super(TileEntityITMultiblockPartAlternator.instance, 0, false);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
    }

    @Override public void update() {
        if (!formed) return;
        if (isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
        TileEntityAlternatorMaster m = master();
        if (m == null) { if (loadGrace++ > 20) { invalidate(); }
        } else loadGrace = 0;
    }

    @Override public boolean isDummy() { return true; }

    public TileEntityAlternatorMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityAlternatorMaster ? (TileEntityAlternatorMaster)te : null;
        return master;
    }

    @Override protected GenericShape getShapeGetter() { return ITShapes.get("alternator"); }

    @Override protected boolean useMirroredShape() { return false; }

    @Override public NonNullList<ItemStack> getInventory() { return null; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 0; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() { return new IFluidTank[0]; }

    @Override @Nonnull public int[] getRedstonePos() { return new int[0]; }

    @Override @Nonnull public int[] getOutputTanks() { return new int[0]; }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) { return false; }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) { return false; }

    @Override @Nonnull protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side, BlockPos position) { return new IFluidTank[0]; }

    @Override @Nonnull protected IMultiblockRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return DummyRecipe.loadFromNBT(tag); }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityAlternatorMaster m = master();
            if (m != null && formed) return m.isEnergyPosition(facing, posInMultiblock());
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityAlternatorMaster m = master();
            if (m != null && formed && m.isEnergyPosition(facing, posInMultiblock())) {
                return (T)new EnergyHelper.IEForgeEnergyWrapper(this, facing);
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override @Nonnull public FluxStorage getFluxStorage() { return master() == null ? new FluxStorage(0) : Objects.requireNonNull(master()).energyStorage; }

    @Override @Nonnull public SideConfig getEnergySideConfig(@Nullable EnumFacing facing) {
        return formed && master() != null && Objects.requireNonNull(master()).isEnergyPosition(facing, posInMultiblock()) ? SideConfig.OUTPUT : SideConfig.NONE;
    }

    @Override public boolean isValid() { return formed; }

    @Override public boolean isMechanicalEnergyReceiver(EnumFacing facing) { return master() != null && Objects.requireNonNull(master()).isMechanicalEnergyReceiver(facing, posInMultiblock()); }

    @Override public int getSpeed() { return master() == null ? 0 : Objects.requireNonNull(master()).speed; }

    @Override public int getMaxSpeed() { return TileEntityAlternatorMaster.maxSpeed(); }

    @Override public float getTorqueMultiplier() { return master() == null ? 0f : Objects.requireNonNull(master()).torqueMult; }

    @Override public double getMass() { return Multiblocks.alternator.alternator_baseMass; }

    @Override public double getFriction() { return Multiblocks.alternator.alternator_friction; }

    @Override public MechanicalEnergyAnimation getAnimation() { return master() == null ? null : Objects.requireNonNull(master()).animation; }

    @Override public int getComparatorInputOverride() { return master() == null ? 0 : Objects.requireNonNull(master()).getComparatorInputOverride(); }
}
