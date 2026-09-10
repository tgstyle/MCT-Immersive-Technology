package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.capability.IMechanicalEnergyProvider;
import com.immersiveconvergence.common.event.ICTickingRegistry;
import com.immersiveconvergence.api.client.MechanicalEnergyAnimation;
import com.immersiveconvergence.api.multiblock.GenericShape;

import mctmods.immersivetechnology.api.crafting.HighPressureSteamTurbineRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartHighPressureSteamTurbine;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.ICollisionBounds;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.ISelectionBounds;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.IBlockBounds;
import com.immersiveconvergence.api.multiblock.TileEntityTemplateMultiblock;
import mctmods.immersivetechnology.common.util.ITUtils;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public class TileEntityHighPressureSteamTurbineSlave extends TileEntityTemplateMultiblock<TileEntityHighPressureSteamTurbineSlave, HighPressureSteamTurbineRecipe, TileEntityHighPressureSteamTurbineMaster>
        implements IMechanicalEnergyProvider, IBlockBounds, ICollisionBounds, ISelectionBounds {
    private static float outputTorque() { return Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_torque; }

    private int loadGrace = 0;

    public TileEntityHighPressureSteamTurbineSlave() {
        super(TileEntityITMultiblockPartHighPressureSteamTurbine.instance, 0, true);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (!formed) return;
        if (isDummy()) ICTickingRegistry.removeFromTicking(this);
        super.update();
        TileEntityHighPressureSteamTurbineMaster m = master();
        if (m == null) { if (++loadGrace > 20) disassemble(); }
        else { loadGrace = 0; }
    }

    @Override public boolean isDummy() { return true; }

    @Override public TileEntityHighPressureSteamTurbineMaster master() { return resolveMaster(TileEntityHighPressureSteamTurbineMaster.class); }

    @Override protected GenericShape getShapeGetter() { return ITShapes.get("high_pressure_steam_turbine"); }

    @Override public NonNullList<ItemStack> getInventory() { return null; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 0; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() {
        TileEntityHighPressureSteamTurbineMaster m = master();
        return m == null ? new IFluidTank[0] : m.tanks;
    }

    @Override @Nonnull protected HighPressureSteamTurbineRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return HighPressureSteamTurbineRecipe.loadFromNBT(tag); }

    @Override @Nonnull public int[] getOutputTanks() { return new int[]{1}; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<HighPressureSteamTurbineRecipe> process) { return true; }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return ITUtils.EMPTY_INT_ARRAY; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return ITUtils.EMPTY_INT_ARRAY; }

    @Override @Nonnull protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side, BlockPos position) {
        TileEntityHighPressureSteamTurbineMaster m = master();
        return m == null ? ITUtils.emptyIFluidTankList : m.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        TileEntityHighPressureSteamTurbineMaster m = master();
        return m != null && m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        TileEntityHighPressureSteamTurbineMaster m = master();
        return m != null && m.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean isValid() { return formed; }

    @Override public boolean isMechanicalEnergyTransmitter(EnumFacing facing) {
        TileEntityHighPressureSteamTurbineMaster m = master();
        return m != null && m.isMechanicalEnergyTransmitter(facing, posInMultiblock());
    }

    @Override public int getSpeed() {
        TileEntityHighPressureSteamTurbineMaster m = master();
        return m == null ? 0 : m.speed;
    }

    @Override public int getMaxSpeed() { return TileEntityHighPressureSteamTurbineMaster.maxSpeed(); }

    @Override public double getBaseMass() { return Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_baseMass; }

    @Override public double getDriveTorque() { return Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_driveTorque; }

    @Override public double getFriction() { return Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_friction; }

    @Override public float getTorqueMultiplier() {
        TileEntityHighPressureSteamTurbineMaster m = master();
        return m == null ? outputTorque() : m.currentTorque;
    }

    @Override public MechanicalEnergyAnimation getAnimation() {
        TileEntityHighPressureSteamTurbineMaster m = master();
        return m == null ? null : m.animation;
    }

    @Override public int getComparatorInputOverride() {
        TileEntityHighPressureSteamTurbineMaster m = master();
        return m == null || !isComparatorPos() ? 0 : m.comparatorValue();
    }
}
