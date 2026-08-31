package mctmods.immersivetechnology.common.multiblocks.stone;

import blusunrize.immersiveengineering.api.IEProperties;

import mctmods.immersivetechnology.common.blocks.ItemBlockITBase;
import mctmods.immersivetechnology.common.multiblocks.BlockITMultiblock;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityAdvancedCokeOvenMaster;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityAdvancedCokeOvenSlave;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityCoolingTowerMaster;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityCoolingTowerSlave;
import mctmods.immersivetechnology.common.multiblocks.stone.types.BlockType_StoneMultiblock;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockStoneMultiblock extends BlockITMultiblock<BlockType_StoneMultiblock> {
    public BlockStoneMultiblock() {
        super("stone_multiblock", Material.ROCK, PropertyEnum.create("type", BlockType_StoneMultiblock.class), ItemBlockITBase.class, IEProperties.DYNAMICRENDER, IEProperties.BOOLEANS[0], Properties.AnimationProperty, IEProperties.OBJ_TEXTURE_REMAP);
        setHardness(2.0F);
        setResistance(20f);
        this.setAllNotNormalBlock();
        lightOpacity = 0;
    }

    @Override public boolean useCustomStateMapper() { return true; }

    @Override @Nonnull public String getCustomStateMapping(int meta, boolean itemBlock) { return BlockType_StoneMultiblock.values()[meta].needsCustomState() ? BlockType_StoneMultiblock.values()[meta].getCustomState() : ""; }

    @Override public boolean allowHammerHarvest(IBlockState state) { return true; }

    @Override @SuppressWarnings("deprecation") public boolean isSideSolid(@Nonnull IBlockState state, IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityAdvancedCokeOvenSlave) {
            int p = ((TileEntityAdvancedCokeOvenSlave)te).pos;
            return p == 1 || p == 4 || p == 7 || p == 31;
        }
        if (te instanceof TileEntityCoolingTowerSlave) { return super.isSideSolid(state, world, pos, side); }
        return true;
    }

    @Override public TileEntity createBasicTE(World worldIn, BlockType_StoneMultiblock type) {
        switch (type) {
            case ADVANCED_COKE_OVEN: return new TileEntityAdvancedCokeOvenMaster();
            case ADVANCED_COKE_OVEN_SLAVE: return new TileEntityAdvancedCokeOvenSlave();
            case COOLING_TOWER: return new TileEntityCoolingTowerMaster();
            case COOLING_TOWER_SLAVE: return new TileEntityCoolingTowerSlave();
        }
        return null;
    }

    @Override @Nonnull public AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        TileEntity te = source.getTileEntity(pos);
        if (te instanceof ICBlockInterfaces.IBlockBounds) {
            float[] bounds = ((ICBlockInterfaces.IBlockBounds)te).getBlockBounds();
            return new AxisAlignedBB(bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
        }
        return FULL_BLOCK_AABB;
    }

    @SideOnly(Side.CLIENT)
    @Override @Nonnull public AxisAlignedBB getSelectedBoundingBox(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ICBlockInterfaces.IAdvancedSelectionBounds) {
            List<AxisAlignedBB> list = ((ICBlockInterfaces.IAdvancedSelectionBounds)te).getAdvancedSelectionBounds();
            if (!list.isEmpty()) { return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D); }
        }
        return getBoundingBox(state, world, pos).offset(pos);
    }

    @Override public RayTraceResult collisionRayTrace(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ICBlockInterfaces.IAdvancedSelectionBounds) {
            List<AxisAlignedBB> list = ((ICBlockInterfaces.IAdvancedSelectionBounds)te).getAdvancedSelectionBounds();
            RayTraceResult minMOP = null;
            double minDist = Double.POSITIVE_INFINITY;
            int subHit = 0;
            for (AxisAlignedBB aabb : list) {
                RayTraceResult mop = aabb.offset(pos).calculateIntercept(start, end);
                if (mop != null) {
                    mop = new RayTraceResult(mop.hitVec, mop.sideHit, pos);
                    double dist = mop.hitVec.squareDistanceTo(start);
                    if (dist < minDist) {
                        minMOP = mop;
                        minMOP.subHit = subHit;
                        minDist = dist;
                    }
                }
                subHit++;
            }
            if (minMOP != null) { return minMOP; }
        }
        return super.collisionRayTrace(state, world, pos, start, end);
    }

    @Override public void addCollisionBoxToList(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        TileEntity te = world.getTileEntity(pos);
        boolean hasAdvanced = false;
        if (te instanceof ICBlockInterfaces.IAdvancedCollisionBounds) {
            List<AxisAlignedBB> list = ((ICBlockInterfaces.IAdvancedCollisionBounds)te).getAdvancedCollisionBounds();
            for (AxisAlignedBB aabb : list) {
                AxisAlignedBB worldAABB = aabb.offset(pos);
                if (worldAABB.intersects(entityBox)) { collidingBoxes.add(worldAABB); }
            }
            hasAdvanced = !list.isEmpty();
        }
        if (!hasAdvanced) { super.addCollisionBoxToList(state, world, pos, entityBox, collidingBoxes, entityIn, isActualState); }
    }
}
