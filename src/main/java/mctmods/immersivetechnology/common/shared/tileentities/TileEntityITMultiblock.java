package mctmods.immersivetechnology.common.shared.tileentities;

import com.immersiveconvergence.api.multiblock.GenericShape;
import com.immersiveconvergence.api.multiblock.MultiblockUtils;
import com.immersiveconvergence.api.shapes.Shapes;
import com.immersiveconvergence.api.shapes.VoxelShape;

import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.TranslationKey;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import static com.immersiveconvergence.api.shapes.BooleanOp.OR;

public abstract class TileEntityITMultiblock<T extends TileEntityITMultiblock<T, R, M>, R extends IMultiblockRecipe, M extends T> extends TileEntityMultiblockMetal<T, R> implements IPlayerInteraction {
    private int blockUpdateCooldown = 0;
    private VoxelShape voxelShapeCache;
    private int voxelShapeCachePos = Integer.MIN_VALUE;
    private EnumFacing voxelShapeCacheFacing;
    private boolean voxelShapeCacheMirrored;

    public TileEntityITMultiblock(MultiblockHandler.IMultiblock instance, int[] structureDimensions, int energyCapacity, boolean redstoneControl) { super(instance, structureDimensions, energyCapacity, redstoneControl); }

    public TileEntityITMultiblock(TileEntityITMultiblockPart<?> instance, int energyCapacity, boolean redstoneControl) { super(instance, new int[]{instance.height, instance.length, instance.width}, energyCapacity, redstoneControl); }

    public abstract M master();

    protected abstract GenericShape getShapeGetter();

    protected boolean useMirroredShape() { return true; }

    protected boolean isInputFluidPoI(int position) { return false; }

    protected int clearInputTanks() { return 0; }

    @Override public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        if (!formed || !player.isSneaking() || !Utils.isHammer(heldItem)) { return false; }
        M master = master();
        if (master == null || !master.isInputFluidPoI(pos)) { return false; }
        if (!world.isRemote) {
            int cleared = master.clearInputTanks();
            player.sendStatusMessage(new TextComponentTranslation(cleared > 1 ? TranslationKey.GUI_INPUT_TANKS_CLEARED.location : TranslationKey.GUI_INPUT_TANK_CLEARED.location), true);
        }
        return true;
    }

    protected AxisAlignedBB preprocessShapeAABB(AxisAlignedBB aabb) { return aabb; }

    protected abstract IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position);

    protected abstract boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource, int position);

    protected abstract boolean canDrainTankFrom(int iTank, EnumFacing side, int position);

    public boolean shouldDropOriginal = true;
    public boolean shouldDropInventory = true;

    @Override protected void setWorldCreate(@Nonnull World worldIn) { this.world = worldIn; }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        formed = nbt.getBoolean("formed");
        pos = nbt.getInteger("pos");
        offset = nbt.getIntArray("offset");
        facing = EnumFacing.values()[nbt.getInteger("facing")];
        mirrored = nbt.getBoolean("mirrored");
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setBoolean("formed", formed);
        nbt.setInteger("pos", pos);
        nbt.setIntArray("offset", offset);
        nbt.setInteger("facing", facing.ordinal());
        nbt.setBoolean("mirrored", mirrored);
    }

    @SuppressWarnings("unchecked")
    @Override @Nullable public T getTileForPos(int targetPos) {
        BlockPos target = getBlockPosForPos(targetPos);
        TileEntity tile = Utils.getExistingTileEntity(world, target);
        if (tile instanceof TileEntityITMultiblock && tile.getClass().isInstance(this)) return (T)tile;
        return null;
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            assert facing != null;
            if (this.getAccessibleFluidTanks(facing).length > 0) return true;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <TE> TE getCapability(@Nonnull Capability<TE> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            assert facing != null;
            if (this.getAccessibleFluidTanks(facing).length > 0) return (TE)new MultiblockFluidWrapper(this, facing);
        }
        return super.getCapability(capability, facing);
    }

    private BlockPos posToMultiblock() {
        TileEntityITMultiblockPart<?> instance = (TileEntityITMultiblockPart<?>)mutliblockInstance;
        int width = instance.width;
        int length = instance.length;
        int y = pos / (length * width);
        int rem = pos % (length * width);
        int z = rem / width;
        int x = rem % width;
        return adjustPosInMultiblock(new BlockPos(x, y, z), width);
    }

    protected BlockPos adjustPosInMultiblock(BlockPos posInMultiblock, int width) { return posInMultiblock; }

    private VoxelShape getVoxelShape() {
        if (voxelShapeCache != null && voxelShapeCachePos == pos && voxelShapeCacheFacing == facing && voxelShapeCacheMirrored == mirrored) { return voxelShapeCache; }
        BlockPos posInMultiblock = posToMultiblock();
        List<AxisAlignedBB> list = getShapeGetter().getShape(posInMultiblock);
        List<AxisAlignedBB> rotatedList = new ArrayList<>(list.size());
        for (AxisAlignedBB aabb : list) { rotatedList.add(ITUtils.rotateAABB(preprocessShapeAABB(aabb), facing, useMirroredShape() && mirrored)); }
        VoxelShape vs = Shapes.empty();
        for (AxisAlignedBB aabb : rotatedList) { vs = Shapes.joinUnoptimized(vs, Shapes.create(aabb), OR); }
        vs = vs.optimize();
        voxelShapeCache = vs;
        voxelShapeCachePos = pos;
        voxelShapeCacheFacing = facing;
        voxelShapeCacheMirrored = mirrored;
        return vs;
    }

    @Nonnull public List<AxisAlignedBB> getAdvancedCollisionBounds() { return getVoxelShape().toAabbs(); }

    @Nonnull public List<AxisAlignedBB> getAdvancedSelectionBounds() { return getVoxelShape().toAabbs(); }

    @SuppressWarnings("unused") public boolean isOverrideBox(@Nonnull AxisAlignedBB box, @Nonnull EntityPlayer player, @Nonnull RayTraceResult mop, @Nonnull List<AxisAlignedBB> list) { return true; }

    @Override @Nonnull public float[] getBlockBounds() {
        VoxelShape vs = getVoxelShape();
        if (vs.isEmpty()) { return new float[]{0f, 0f, 0f, 1f, 1f, 1f}; }
        AxisAlignedBB bb = vs.bounds();
        return new float[]{(float)bb.minX, (float)bb.minY, (float)bb.minZ, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ};
    }

    @Override @Nonnull public ItemStack getOriginalBlock() { return MultiblockUtils.GetItemStack(pos, ((TileEntityITMultiblockPart<?>)this.mutliblockInstance).structureExport); }

    @Override public void doGraphicalUpdates(int slot) { this.markDirty(); this.markContainingBlockForUpdate(null); }

    @Override @Nonnull public R findRecipeForInsertion(@Nonnull ItemStack inserting) { throw new UnsupportedOperationException(); }

    @Override @Nonnull public int[] getEnergyPos() { return new int[0]; }

    @Override @Nonnull public int[] getOutputSlots() { return new int[0]; }

    @Override @Nonnull public int[] getRedstonePos() { return master() == null ? new int[0] : Objects.requireNonNull(master()).getRedstonePos(); }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<R> process) { return false; }

    @Override public void doProcessOutput(@Nonnull ItemStack output) {}

    @Override public void doProcessFluidOutput(@Nonnull FluidStack output) {}

    @Override public void onProcessFinish(@Nonnull MultiblockProcess<R> process) {}

    @Override public int getMaxProcessPerTick() { return 0; }

    @Override public int getProcessQueueMaxLength() { return 0; }

    @Override public float getMinProcessDistance(@Nonnull MultiblockProcess<R> process) { return 0f; }

    @Override public boolean isInWorldProcessingMachine() { return false; }

    @Override @Nonnull protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
        M master = master();
        if (master == null) return new IFluidTank[0];
        return master.getAccessibleFluidTanks(side, this.pos);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource) {
        M master = master();
        if (master == null) return false;
        return master.canFillTankFrom(iTank, side, resource, this.pos);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side) {
        M master = master();
        if (master == null) return false;
        return master.canDrainTankFrom(iTank, side, this.pos);
    }

    @Override public void disassemble() {
        if (formed && !world.isRemote) {
            BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
            TileEntity teMaster = world.getTileEntity(masterPos);
            if (teMaster instanceof IIEInventory && shouldDropInventory) {
                NonNullList<ItemStack> inv = ((IIEInventory)teMaster).getInventory();
                for (ItemStack stack : inv) {
                    if (!stack.isEmpty()) {
                        float rx = world.rand.nextFloat() * 0.8F + 0.1F;
                        float ry = world.rand.nextFloat() * 0.8F + 0.1F;
                        float rz = world.rand.nextFloat() * 0.8F + 0.1F;
                        EntityItem entityitem = new EntityItem(world, masterPos.getX() + rx, masterPos.getY() + ry, masterPos.getZ() + rz, stack.copy());
                        entityitem.motionX = world.rand.nextGaussian() * 0.05;
                        entityitem.motionY = world.rand.nextGaussian() * 0.05 + 0.2F;
                        entityitem.motionZ = world.rand.nextGaussian() * 0.05;
                        world.spawnEntity(entityitem);
                    }
                }
                inv.clear();
            }
            BlockPos startPos = getBlockPosForPos(0);
            long time = world.getTotalWorldTime();
            for (int h = 0; h < structureDimensions[0]; h++) for (int l = 0; l < structureDimensions[1]; l++) for (int w = 0; w < structureDimensions[2]; w++) {
                int ww = mirrored ? -w : w;
                BlockPos pos2 = startPos.offset(facing, l).offset(facing.rotateY(), ww).add(0, h, 0);
                ItemStack s = ItemStack.EMPTY;
                TileEntity te = world.getTileEntity(pos2);
                if (te instanceof TileEntityMultiblockPart) {
                    TileEntityMultiblockPart<?> part = (TileEntityMultiblockPart<?>)te;
                    Vec3i diff = pos2.subtract(masterPos);
                    if (part.offset[0] != diff.getX() || part.offset[1] != diff.getY() || part.offset[2] != diff.getZ()) continue;
                    if (time != part.onlyLocalDissassembly) { s = part.getOriginalBlock(); part.formed = false; }
                }
                if (pos2.equals(getPos())) s = this.getOriginalBlock();
                IBlockState state = Utils.getStateFromItemStack(s);
                if (state != null) {
                    if (pos2.equals(getPos())) { if (shouldDropOriginal) world.spawnEntity(new EntityItem(world, pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5, s)); }
                    else replaceStructureBlock(pos2, state, s, h, l, w);
                }
            }
        }
    }

    protected void throttledBlockUpdate() {
        if (blockUpdateCooldown > 0) {
            blockUpdateCooldown--;
            return;
        }
        blockUpdateCooldown = 20;
        markContainingBlockForUpdate(null);
    }
}
