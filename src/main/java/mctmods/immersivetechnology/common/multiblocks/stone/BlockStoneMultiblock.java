package mctmods.immersivetechnology.common.multiblocks.stone;

import mctmods.immersivetechnology.common.blocks.ItemBlockITBase;
import mctmods.immersivetechnology.common.multiblocks.BlockITMultiblock;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityAdvancedCokeOvenMaster;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityAdvancedCokeOvenSlave;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityCoolingTowerMaster;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityCoolingTowerSlave;
import mctmods.immersivetechnology.common.multiblocks.stone.types.BlockType_StoneMultiblock;
import com.immersiveconvergence.api.block.ICProperties;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.Properties;

import javax.annotation.Nonnull;

public class BlockStoneMultiblock extends BlockITMultiblock<BlockType_StoneMultiblock> {
    public BlockStoneMultiblock() {
        super("stone_multiblock", Material.ROCK, PropertyEnum.create("type", BlockType_StoneMultiblock.class), ItemBlockITBase.class, ICProperties.DYNAMICRENDER, ICProperties.BOOLEANS[0], Properties.AnimationProperty, ICProperties.OBJ_TEXTURE_REMAP);
        setHardness(2.0F);
        setResistance(20f);
        this.setAllNotNormalBlock();
        lightOpacity = 0;
    }

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
}
