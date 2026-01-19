package mctmods.immersivetechnology.common.blocks.connectors;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorRedstone;

import mctmods.immersivetechnology.common.blocks.ItemBlockITBase;
import mctmods.immersivetechnology.common.blocks.connectors.tileentities.TileEntityTimer;
import mctmods.immersivetechnology.common.blocks.connectors.types.BlockType_Connectors;
import mctmods.immersivetechnology.common.shared.BlockITTileProvider;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class BlockConnectors extends BlockITTileProvider<BlockType_Connectors> {
    public static final PropertyInteger ROTATION = PropertyInteger.create("rotation", 0, 3);

    public BlockConnectors() {
        super("connectors", Material.IRON, PropertyEnum.create("type", BlockType_Connectors.class), ItemBlockITBase.class, IEProperties.FACING_ALL, IEProperties.BOOLEANS[0], IEProperties.BOOLEANS[1], IOBJModelCallback.PROPERTY, ROTATION);
        setHardness(3.0F);
        setResistance(15.0F);
        lightOpacity = 0;
        setMetaBlockLayer(BlockType_Connectors.CONNECTORS_TIMER.getMeta(), BlockRenderLayer.CUTOUT, BlockRenderLayer.TRANSLUCENT, BlockRenderLayer.SOLID);
        setAllNotNormalBlock();
    }

    @SuppressWarnings("rawtypes")
    @Override @Nonnull protected BlockStateContainer createBlockState() {
        BlockStateContainer base = super.createBlockState();
        IUnlistedProperty[] unlisted = (base instanceof ExtendedBlockState) ? ((ExtendedBlockState) base).getUnlistedProperties().toArray(new IUnlistedProperty[0]) : new IUnlistedProperty[0];
        unlisted = Arrays.copyOf(unlisted, unlisted.length+1);
        unlisted[unlisted.length-1] = IEProperties.CONNECTIONS;
        return new ExtendedBlockState(this, base.getProperties().toArray(new IProperty[0]), unlisted);
    }

    @Override @Nonnull public IBlockState getExtendedState(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        state = super.getExtendedState(state, world, pos);
        if (state instanceof IExtendedBlockState) {
            IExtendedBlockState ext = (IExtendedBlockState) state;
            TileEntity te = world.getTileEntity(pos);
            if (!(te instanceof TileEntityImmersiveConnectable)) return state;
            state = ext.withProperty(IEProperties.CONNECTIONS, ((TileEntityImmersiveConnectable)te).genConnBlockstate());
        }
        return state;
    }

    @Override public void neighborChanged(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull net.minecraft.block.Block blockIn, @Nonnull BlockPos fromPos) {
        super.neighborChanged(state, world, pos, blockIn, fromPos);
        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileEntityConnectorRedstone) {
            TileEntityConnectorRedstone connector = (TileEntityConnectorRedstone) te;
            if (world.isAirBlock(pos.offset(connector.facing))) {
                this.dropBlockAsItem(connector.getWorld(), pos, world.getBlockState(pos), 0);
                connector.getWorld().setBlockToAir(pos);
                return;
            }
            if (connector.isRSInput()) connector.rsDirty = true;
        }
    }

    @SuppressWarnings("deprecation")
    @Override @Nonnull public IBlockState getStateForPlacement(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing clickedSide, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer) {
        IBlockState state = super.getStateForPlacement(world, pos, clickedSide, hitX, hitY, hitZ, meta, placer);
        state = state.withProperty(IEProperties.FACING_ALL, clickedSide.getOpposite());
        if (BlockType_Connectors.values()[meta] == BlockType_Connectors.CONNECTORS_TIMER) {
            float yaw = placer.rotationYaw;
            if (yaw < 0) yaw += 360f;
            yaw += 180f;
            yaw %= 360f;
            int rotation = MathHelper.floor(yaw / 90f + 0.5f) & 3;
            state = state.withProperty(ROTATION, rotation);
        }
        return state;
    }

    @Override public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityTimer) {
            TileEntityTimer timer = (TileEntityTimer) te;
            int rot = state.getValue(ROTATION);
            timer.setValue("rotation", rot);
            timer.markDirty();
            worldIn.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    @Override public TileEntity createBasicTE(@Nonnull World world, @Nonnull BlockType_Connectors type) {
        if (type == BlockType_Connectors.CONNECTORS_TIMER) {
            return new TileEntityTimer();
        }
        return null;
    }

    @Override public boolean canConnectRedstone(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side) {
        if (side == null) return false;
        BlockType_Connectors type = state.getValue(property);
        if (type == BlockType_Connectors.CONNECTORS_TIMER) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityTimer) {
                EnumFacing inputSide = ((TileEntityTimer) te).getInputSide();
                return side == inputSide.getOpposite();
            }
        }
        return super.canConnectRedstone(state, world, pos, side);
    }
}
