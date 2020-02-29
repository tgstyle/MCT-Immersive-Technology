package mctmods.immersivetechnology.common.blocks.wooden;

import mctmods.immersivetechnology.common.blocks.BlockITTileProvider;
import mctmods.immersivetechnology.common.blocks.ItemBlockITBase;
import mctmods.immersivetechnology.common.blocks.wooden.types.BlockType_WoodenCrate;
import mctmods.immersivetechnology.common.blocks.wooden.tileentities.TileEntityCrate;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockWoodenCrate extends BlockITTileProvider<BlockType_WoodenCrate> {
	public BlockWoodenCrate() {
		super("wooden_crate", Material.IRON, PropertyEnum.create("type", BlockType_WoodenCrate.class), ItemBlockITBase.class);
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
	public TileEntity createBasicTE(World worldIn, BlockType_WoodenCrate type) {
		switch(type) {
		case CRATE:
			return new TileEntityCrate();
		default:
			break;
		}
		return null;
	}

}