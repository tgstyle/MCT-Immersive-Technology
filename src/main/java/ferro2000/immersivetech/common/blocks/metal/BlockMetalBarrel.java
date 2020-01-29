package ferro2000.immersivetech.common.blocks.metal;

import ferro2000.immersivetech.common.blocks.BlockITTileProvider;
import ferro2000.immersivetech.common.blocks.ItemBlockITBase;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityBarrel;
import ferro2000.immersivetech.common.blocks.metal.types.BlockType_MetalBarrel;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockMetalBarrel extends BlockITTileProvider<BlockType_MetalBarrel> {
	public BlockMetalBarrel() {
		super("metal_barrel", Material.IRON, PropertyEnum.create("type", BlockType_MetalBarrel.class), ItemBlockITBase.class);
		this.setHardness(3.0F);
		this.setResistance(15.0F);
		lightOpacity = 0;
		this.setAllNotNormalBlock();
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		state = super.getExtendedState(state, world, pos);
		return state;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		switch(BlockType_MetalBarrel.values()[meta]) {
		case BARREL:
			return new TileEntityBarrel();
		default:
			break;
		}
		return null;
	}

	@Override
	public boolean allowHammerHarvest(IBlockState state) {
		return true;
	}

}