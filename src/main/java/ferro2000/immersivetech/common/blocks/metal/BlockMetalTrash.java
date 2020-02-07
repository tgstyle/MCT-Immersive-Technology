package ferro2000.immersivetech.common.blocks.metal;

import ferro2000.immersivetech.common.blocks.BlockITTileProvider;
import ferro2000.immersivetech.common.blocks.ItemBlockITBase;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityTrashEnergy;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityTrashFluid;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityTrashItem;
import ferro2000.immersivetech.common.blocks.metal.types.BlockType_MetalTrash;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockMetalTrash extends BlockITTileProvider<BlockType_MetalTrash> {
	public BlockMetalTrash() {
		super("metal_trash", Material.IRON, PropertyEnum.create("type", BlockType_MetalTrash.class), ItemBlockITBase.class);
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
	public boolean allowHammerHarvest(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createBasicTE(World worldIn, BlockType_MetalTrash type) {
		switch(type) {
		case TRASH_ITEM:
			return new TileEntityTrashItem();
		case TRASH_FLUID:
			return new TileEntityTrashFluid();
		case TRASH_ENERGY:
			return new TileEntityTrashEnergy();
		default:
			break;
		}
		return null;
	}

}