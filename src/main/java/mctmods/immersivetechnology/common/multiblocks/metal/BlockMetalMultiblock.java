package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;

import mctmods.immersivetechnology.common.blocks.ItemBlockITBase;
import mctmods.immersivetechnology.common.multiblocks.BlockITMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityAlternatorMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityAlternatorSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerSlave;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityCoolingTowerMaster;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityCoolingTowerSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityDistillerMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityDistillerSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarReflectorMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarReflectorSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarTowerMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarTowerSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySteamTurbineMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySteamTurbineSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySteelSheetmetalTankMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySteelSheetmetalTankSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
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

public class BlockMetalMultiblock extends BlockITMultiblock<BlockType_MetalMultiblock> {
    public BlockMetalMultiblock() {
        super("metal_multiblock", Material.IRON, PropertyEnum.create("type", BlockType_MetalMultiblock.class), ItemBlockITBase.class, IEProperties.DYNAMICRENDER, IEProperties.BOOLEANS[0], Properties.AnimationProperty, IEProperties.OBJ_TEXTURE_REMAP);
        setHardness(3.0F);
        setResistance(15.0F);
        setMetaBlockLayer(BlockType_MetalMultiblock.STEEL_TANK.getMeta(), BlockRenderLayer.CUTOUT);
        setMetaBlockLayer(BlockType_MetalMultiblock.BOILER.getMeta(), BlockRenderLayer.CUTOUT);
        setMetaBlockLayer(BlockType_MetalMultiblock.BOILER_SLAVE.getMeta(), BlockRenderLayer.CUTOUT);
        setAllNotNormalBlock();
        lightOpacity = 0;
    }

    @Override public boolean useCustomStateMapper() { return true; }

    @Override @Nonnull public String getCustomStateMapping(int meta, boolean itemBlock) {
        return BlockType_MetalMultiblock.values()[meta].needsCustomState() ? BlockType_MetalMultiblock.values()[meta].getCustomState() : "";
    }

    @Override public boolean allowHammerHarvest(IBlockState state) { return true; }

    @Override public TileEntity createBasicTE(World worldIn, BlockType_MetalMultiblock type) {
        switch (type) {
            case ALTERNATOR: return new TileEntityAlternatorMaster();
            case ALTERNATOR_SLAVE: return new TileEntityAlternatorSlave();
            case BOILER: return new TileEntityBoilerMaster();
            case BOILER_SLAVE: return new TileEntityBoilerSlave();
            case DISTILLER: return new TileEntityDistillerMaster();
            case DISTILLER_SLAVE: return new TileEntityDistillerSlave();
            case SOLAR_REFLECTOR: return new TileEntitySolarReflectorMaster();
            case SOLAR_REFLECTOR_SLAVE: return new TileEntitySolarReflectorSlave();
            case SOLAR_TOWER: return new TileEntitySolarTowerMaster();
            case SOLAR_TOWER_SLAVE: return new TileEntitySolarTowerSlave();
            case STEAM_TURBINE: return new TileEntitySteamTurbineMaster();
            case STEAM_TURBINE_SLAVE: return new TileEntitySteamTurbineSlave();
            case STEEL_TANK: return new TileEntitySteelSheetmetalTankMaster();
            case STEEL_TANK_SLAVE: return new TileEntitySteelSheetmetalTankSlave();
            case COOLING_TOWER: return new TileEntityCoolingTowerMaster();
            case COOLING_TOWER_SLAVE: return new TileEntityCoolingTowerSlave();
        }
        return null;
    }

    @Override @Nonnull public AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        TileEntity te = source.getTileEntity(pos);
        if (te instanceof ITBlockInterfaces.IBlockBounds) {
            float[] bounds = ((ITBlockInterfaces.IBlockBounds)te).getBlockBounds();
            return new AxisAlignedBB(bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
        }
        return FULL_BLOCK_AABB;
    }

    @SideOnly(Side.CLIENT)
    @Override @Nonnull public AxisAlignedBB getSelectedBoundingBox(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ITBlockInterfaces.IAdvancedSelectionBounds) {
            List<AxisAlignedBB> list = ((ITBlockInterfaces.IAdvancedSelectionBounds)te).getAdvancedSelectionBounds();
            if (!list.isEmpty()) { return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D); }
        }
        return getBoundingBox(state, world, pos).offset(pos);
    }

    @Override public RayTraceResult collisionRayTrace(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ITBlockInterfaces.IAdvancedSelectionBounds) {
            List<AxisAlignedBB> list = ((ITBlockInterfaces.IAdvancedSelectionBounds)te).getAdvancedSelectionBounds();
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
        if (te instanceof ITBlockInterfaces.IAdvancedCollisionBounds) {
            List<AxisAlignedBB> list = ((ITBlockInterfaces.IAdvancedCollisionBounds)te).getAdvancedCollisionBounds();
            for (AxisAlignedBB aabb : list) {
                AxisAlignedBB worldAABB = aabb.offset(pos);
                if (worldAABB.intersects(entityBox)) { collidingBoxes.add(worldAABB); }
            }
            hasAdvanced = !list.isEmpty();
        }
        if (!hasAdvanced) { super.addCollisionBoxToList(state, world, pos, entityBox, collidingBoxes, entityIn, isActualState); }
    }
}
