package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import mctmods.immersivetechnology.common.blocks.BlockITBase;
import mctmods.immersivetechnology.common.blocks.BlockITTileProvider;
import mctmods.immersivetechnology.common.blocks.ItemBlockITBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Locale;

public class BlockValve extends BlockITTileProvider<BlockValve.BlockType_Valve> {

    @Nullable
    @Override
    public TileEntity createBasicTE(World worldIn, BlockType_Valve type) {
        return new TileEntityFluidValve();
    }

    public enum BlockType_Valve implements IStringSerializable, BlockITBase.IBlockEnum {
        FLUID_VALVE;

        @Override
        public String getName() {
            return this.toString().toLowerCase(Locale.ENGLISH);
        }

        @Override
        public int getMeta() {
            return ordinal();
        }

        @Override
        public boolean listForCreative() {
            return true;
        }
    }

    public BlockValve() {
        super("valve", Material.IRON, PropertyEnum.create("type", BlockType_Valve.class), ItemBlockITBase.class, IEProperties.FACING_ALL);
        this.setHardness(3.0F);
        this.setResistance(15.0F);
        lightOpacity = 0;
        this.setAllNotNormalBlock();
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(IEProperties.FACING_ALL, facing);
    }
}
