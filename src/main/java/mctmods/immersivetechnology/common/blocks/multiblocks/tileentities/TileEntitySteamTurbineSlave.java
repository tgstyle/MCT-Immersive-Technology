package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.api.crafting.SteamTurbineRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.blocks.ITBlockInterfaces.IMechanicalEnergy;
import mctmods.immersivetechnology.common.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.blocks.multiblocks.TileEntityITMultiblockPartSteamTurbine;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.SteamTurbineShape;

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

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;

import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.ImmutableList;

public class TileEntitySteamTurbineSlave extends TileEntityITMultiblock<TileEntitySteamTurbineSlave, SteamTurbineRecipe, TileEntitySteamTurbineMaster> implements IMechanicalEnergy, IBlockBounds, IAdvancedCollisionBounds, IAdvancedSelectionBounds {
    private static final float outputtorque = Multiblocks.steamTurbine.steamTurbine_torque;
    TileEntitySteamTurbineMaster master;

    public TileEntitySteamTurbineSlave() { super(TileEntityITMultiblockPartSteamTurbine.instance, 0, true); }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if(isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
    }

    @Override public boolean isDummy() { return true; }

    @Override public TileEntitySteamTurbineMaster master() {
        if(master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntitySteamTurbineMaster?(TileEntitySteamTurbineMaster) te: null;
        return master;
    }

    @Override public boolean isValid() { return formed; }

    @Override public boolean isMechanicalEnergyTransmitter(EnumFacing facing) { TileEntitySteamTurbineMaster m = master(); return m != null && m.isMechanicalEnergyTransmitter(facing, pos); }

    @Override public boolean isMechanicalEnergyReceiver(EnumFacing facing) { return false; }

    @Override public int getSpeed() { TileEntitySteamTurbineMaster m = master(); return m == null ? 0 : m.speed; }

    @Override public float getTorqueMultiplier() { return outputtorque; }

    public MechanicalEnergyAnimation getAnimation() { TileEntitySteamTurbineMaster m = master(); return m == null ? null : m.animation; }

    @Override public NonNullList<ItemStack> getInventory() { return null; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 0; }

    @Override public @Nonnull IFluidTank[] getInternalTanks() { TileEntitySteamTurbineMaster m = master(); return m == null ? new IFluidTank[0] : m.tanks; }

    @Override protected @Nullable SteamTurbineRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return null; }

    @Override public @Nonnull int[] getRedstonePos() { return new int[] { 32 }; }

    @Override public @Nonnull int[] getOutputTanks() { return new int[] { 1 }; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<SteamTurbineRecipe> process) { return true; }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        TileEntitySteamTurbineMaster m = master();
        if (m == null) return ITUtils.emptyIFluidTankList;
        return m.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        TileEntitySteamTurbineMaster m = master();
        if (m == null) return false;
        return m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        TileEntitySteamTurbineMaster m = master();
        if (m == null) return false;
        return m.canDrainTankFrom(iTank, side, position);
    }

    @Nonnull
    @Override public float[] getBlockBounds() {
        if (!formed) return new float[]{0f,0f,0f,1f,1f,1f};
        List<AxisAlignedBB> list = getAdvancedBounds();
        if (list.isEmpty() || (list.size() == 1 && list.get(0).equals(new AxisAlignedBB(0,0,0,1,1,1)))) return new float[]{0f,0f,0f,1f,1f,1f};
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE, maxZ = Double.MIN_VALUE;
        for (AxisAlignedBB aabb : list) {
            minX = Math.min(minX, aabb.minX);
            minY = Math.min(minY, aabb.minY);
            minZ = Math.min(minZ, aabb.minZ);
            maxX = Math.max(maxX, aabb.maxX);
            maxY = Math.max(maxY, aabb.maxY);
            maxZ = Math.max(maxZ, aabb.maxZ);
        }
        return new float[]{(float)minX, (float)minY, (float)minZ, (float)maxX, (float)maxY, (float)maxZ};
    }

    @Nonnull
    @Override
    public List<AxisAlignedBB> getAdvancedColisionBounds() { return getAdvancedBounds(); }

    @Nonnull
    @Override
    public List<AxisAlignedBB> getAdvancedSelectionBounds() { return getAdvancedBounds(); }

    @Override
    public boolean isOverrideBox(@Nonnull AxisAlignedBB box, @Nonnull EntityPlayer player, @Nonnull RayTraceResult mop, @Nonnull ArrayList<AxisAlignedBB> list) { return false; }

    private BlockPos posToMultiblock() {
        final int width = 3;
        final int height = 4;
        final int length = 10;
        int y = pos / (length * width);
        int z = (pos % (length * width)) / width;
        int x = pos % width;
        return new BlockPos(x, y, z);
    }

    private List<AxisAlignedBB> getAdvancedBounds() {
        if (!formed) return ImmutableList.of(new AxisAlignedBB(0, 0, 0, 1, 1, 1));
        BlockPos posInMultiblock = posToMultiblock();
        List<AxisAlignedBB> list = SteamTurbineShape.GETTER.getShape(posInMultiblock);
        if (list.isEmpty()) return ImmutableList.of(new AxisAlignedBB(0, 0, 0, 1, 1, 1));
        List<AxisAlignedBB> rotated = new ArrayList<>(list.size());
        for (AxisAlignedBB aabb : list) { rotated.add(rotateAABB(aabb, facing)); }
        return rotated;
    }
}
