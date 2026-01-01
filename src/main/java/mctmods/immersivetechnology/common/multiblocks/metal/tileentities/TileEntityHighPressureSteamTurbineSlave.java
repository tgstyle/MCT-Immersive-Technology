package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.api.crafting.HighPressureSteamTurbineRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces.IMechanicalEnergy;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartHighPressureSteamTurbine;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.HighPressureSteamTurbineShape;

import mctmods.immersivetechnology.common.util.shapes.*;
import static mctmods.immersivetechnology.common.util.shapes.BooleanOp.OR;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TileEntityHighPressureSteamTurbineSlave extends TileEntityITMultiblock<TileEntityHighPressureSteamTurbineSlave, HighPressureSteamTurbineRecipe, TileEntityHighPressureSteamTurbineMaster> implements IMechanicalEnergy, ITBlockInterfaces.IBlockBounds, ITBlockInterfaces.IAdvancedCollisionBounds, ITBlockInterfaces.IAdvancedSelectionBounds {
    private static final float outputtorque = Multiblocks.steamTurbine.steamTurbine_torque;
    TileEntityHighPressureSteamTurbineMaster master;

    public TileEntityHighPressureSteamTurbineSlave() { super(TileEntityITMultiblockPartHighPressureSteamTurbine.instance, 0, true); }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if(isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
    }

    @Override public boolean isDummy() { return true; }

    @Override public TileEntityHighPressureSteamTurbineMaster master() {
        if(master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityHighPressureSteamTurbineMaster?(TileEntityHighPressureSteamTurbineMaster) te: null;
        return master;
    }

    @Override public boolean isValid() { return formed; }

    @Override public boolean isMechanicalEnergyTransmitter(EnumFacing facing) { TileEntityHighPressureSteamTurbineMaster m = master(); return m != null && m.isMechanicalEnergyTransmitter(facing, pos); }

    @Override public boolean isMechanicalEnergyReceiver(EnumFacing facing) { return false; }

    @Override public int getSpeed() { TileEntityHighPressureSteamTurbineMaster m = master(); return m == null ? 0 : m.speed; }

    @Override public float getTorqueMultiplier() { return outputtorque; }

    public MechanicalEnergyAnimation getAnimation() { TileEntityHighPressureSteamTurbineMaster m = master(); return m == null ? null : m.animation; }

    @Override public NonNullList<ItemStack> getInventory() { return null; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 0; }

    @Override public @Nonnull IFluidTank[] getInternalTanks() { TileEntityHighPressureSteamTurbineMaster m = master(); return m == null ? new IFluidTank[0] : m.tanks; }

    @Override @Nonnull protected HighPressureSteamTurbineRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return HighPressureSteamTurbineRecipe.loadFromNBT(tag); }

    @Override @Nonnull public int[] getRedstonePos() { return master() == null ? new int[0] : master.getRedstonePos(); }

    @Override public @Nonnull int[] getOutputTanks() { return new int[] {1}; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<HighPressureSteamTurbineRecipe> process) { return true; }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        TileEntityHighPressureSteamTurbineMaster m = master();
        if (m == null) return ITUtils.emptyIFluidTankList;
        return m.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        TileEntityHighPressureSteamTurbineMaster m = master();
        if (m == null) return false;
        return m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        TileEntityHighPressureSteamTurbineMaster m = master();
        if (m == null) return false;
        return m.canDrainTankFrom(iTank, side, position);
    }

    private BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartHighPressureSteamTurbine.instance.width;
        int length = TileEntityITMultiblockPartHighPressureSteamTurbine.instance.length;
        int y = pos / (length * width);
        int rem = pos % (length * width);
        int z = rem / width;
        int x = rem % width;
        if (mirrored) x = width - 1 - x;
        return new BlockPos(x, y, z);
    }

    private VoxelShape getVoxelShape() {
        BlockPos posInMultiblock = posToMultiblock();
        List<AxisAlignedBB> list = HighPressureSteamTurbineShape.GETTER.getShape(posInMultiblock);
        if (list.isEmpty()) return Shapes.empty();
        List<AxisAlignedBB> rotatedList = new ArrayList<>(list.size());
        for (AxisAlignedBB aabb : list) rotatedList.add(ITUtils.rotateAABB(aabb, facing, mirrored));
        VoxelShape vs = Shapes.empty();
        for (AxisAlignedBB aabb : rotatedList) vs = Shapes.joinUnoptimized(vs, Shapes.create(aabb), OR);
        return vs.optimize();
    }

    @Nonnull
    @Override public List<AxisAlignedBB> getAdvancedCollisionBounds() { return getVoxelShape().toAabbs(); }

    @Nonnull
    @Override public List<AxisAlignedBB> getAdvancedSelectionBounds() { return getVoxelShape().toAabbs(); }

    @Override public boolean isOverrideBox(@Nonnull AxisAlignedBB box, @Nonnull EntityPlayer player, @Nonnull RayTraceResult mop, @Nonnull List<AxisAlignedBB> list) { return false; }

    @Nonnull
    @Override public float[] getBlockBounds() {
        VoxelShape vs = getVoxelShape();
        if (vs.isEmpty()) return new float[]{0f, 0f, 0f, 1f, 1f, 1f};
        AxisAlignedBB bb = vs.bounds();
        return new float[]{(float)bb.minX, (float)bb.minY, (float)bb.minZ, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ};
    }
}
