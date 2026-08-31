package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;

import mctmods.immersivetechnology.common.blocks.ItemBlockITBase;
import mctmods.immersivetechnology.common.multiblocks.BlockITMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.*;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock1;

import com.immersiveconvergence.api.multiblock.ICBlockInterfaces;
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

public class BlockMetalMultiblock1 extends BlockITMultiblock<BlockType_MetalMultiblock1> {
    public BlockMetalMultiblock1() {
        super("metal_multiblock1", Material.IRON, PropertyEnum.create("type", BlockType_MetalMultiblock1.class), ItemBlockITBase.class, IEProperties.DYNAMICRENDER, IEProperties.BOOLEANS[0], Properties.AnimationProperty, IEProperties.OBJ_TEXTURE_REMAP);
        setHardness(3.0F);
        setResistance(15.0F);
        setMetaBlockLayer(BlockType_MetalMultiblock1.BOILER_LIQUID.getMeta(), BlockRenderLayer.CUTOUT);
        setMetaBlockLayer(BlockType_MetalMultiblock1.BOILER_LIQUID_SLAVE.getMeta(), BlockRenderLayer.CUTOUT);
        setAllNotNormalBlock();
        lightOpacity = 0;
    }

    @Override public boolean useCustomStateMapper() { return true; }

    @Override @Nonnull public String getCustomStateMapping(int meta, boolean itemBlock) { return BlockType_MetalMultiblock1.values()[meta].needsCustomState() ? BlockType_MetalMultiblock1.values()[meta].getCustomState() : ""; }

    @Override public boolean allowHammerHarvest(IBlockState state) { return true; }

    @Override public TileEntity createBasicTE(World worldIn, BlockType_MetalMultiblock1 type) {
        switch (type) {
            case GAS_TURBINE: { return new TileEntityGasTurbineMaster(); }
            case GAS_TURBINE_SLAVE: { return new TileEntityGasTurbineSlave(); }
            case HEAT_EXCHANGER: { return new TileEntityHeatExchangerMaster(); }
            case HEAT_EXCHANGER_SLAVE: { return new TileEntityHeatExchangerSlave(); }
            case HIGH_PRESSURE_STEAM_TURBINE: { return new TileEntityHighPressureSteamTurbineMaster(); }
            case HIGH_PRESSURE_STEAM_TURBINE_SLAVE: { return new TileEntityHighPressureSteamTurbineSlave(); }
            case ELECTROLYTIC_CRUCIBLE_BATTERY: { return new TileEntityElectrolyticCrucibleBatteryMaster(); }
            case ELECTROLYTIC_CRUCIBLE_BATTERY_SLAVE: { return new TileEntityElectrolyticCrucibleBatterySlave(); }
            case MELTING_CRUCIBLE: { return new TileEntityMeltingCrucibleMaster(); }
            case MELTING_CRUCIBLE_SLAVE: { return new TileEntityMeltingCrucibleSlave(); }
            case RADIATOR: { return new TileEntityRadiatorMaster(); }
            case RADIATOR_SLAVE: { return new TileEntityRadiatorSlave(); }
            case SOLAR_MELTER: { return new TileEntitySolarMelterMaster(); }
            case SOLAR_MELTER_SLAVE: { return new TileEntitySolarMelterSlave(); }
            case BOILER_LIQUID: { return new TileEntityBoilerLiquidMaster(); }
            case BOILER_LIQUID_SLAVE: { return new TileEntityBoilerLiquidSlave(); }
        }
        return null;
    }

    @Override @Nonnull public AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        TileEntity te = source.getTileEntity(pos);
        if (te instanceof ICBlockInterfaces.IBlockBounds) {
            float[] bounds = ((ICBlockInterfaces.IBlockBounds) te).getBlockBounds();
            return new AxisAlignedBB(bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
        }
        return FULL_BLOCK_AABB;
    }

    @SideOnly(Side.CLIENT)
    @Override @Nonnull public AxisAlignedBB getSelectedBoundingBox(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ICBlockInterfaces.IAdvancedSelectionBounds) {
            List<AxisAlignedBB> list = ((ICBlockInterfaces.IAdvancedSelectionBounds) te).getAdvancedSelectionBounds();
            if (!list.isEmpty()) { return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D); }
        }
        return getBoundingBox(state, world, pos).offset(pos);
    }

    @Override public RayTraceResult collisionRayTrace(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ICBlockInterfaces.IAdvancedSelectionBounds) {
            List<AxisAlignedBB> list = ((ICBlockInterfaces.IAdvancedSelectionBounds) te).getAdvancedSelectionBounds();
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
            if (minMOP != null) return minMOP;
        }
        return super.collisionRayTrace(state, world, pos, start, end);
    }

    @Override public void addCollisionBoxToList(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        TileEntity te = world.getTileEntity(pos);
        boolean hasAdvanced = false;
        if (te instanceof ICBlockInterfaces.IAdvancedCollisionBounds) {
            List<AxisAlignedBB> list = ((ICBlockInterfaces.IAdvancedCollisionBounds) te).getAdvancedCollisionBounds();
            for (AxisAlignedBB aabb : list) {
                AxisAlignedBB worldAABB = aabb.offset(pos);
                if (worldAABB.intersects(entityBox)) { collidingBoxes.add(worldAABB); }
            }
            hasAdvanced = !list.isEmpty();
        }
        if (!hasAdvanced) { super.addCollisionBoxToList(state, world, pos, entityBox, collidingBoxes, entityIn, isActualState); }
    }
}
