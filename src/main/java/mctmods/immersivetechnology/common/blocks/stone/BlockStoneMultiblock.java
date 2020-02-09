package mctmods.immersivetechnology.common.blocks.stone;

import blusunrize.immersiveengineering.api.IEProperties;
import mctmods.immersivetechnology.common.blocks.stone.tileentities.TileEntityCokeOvenAdvancedMaster;
import mctmods.immersivetechnology.common.blocks.stone.tileentities.TileEntityCokeOvenAdvancedSlave;
import mctmods.immersivetechnology.common.blocks.BlockITMultiblock;
import mctmods.immersivetechnology.common.blocks.ItemBlockITBase;
import mctmods.immersivetechnology.common.blocks.stone.types.BlockType_StoneMultiblock;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockStoneMultiblock extends BlockITMultiblock<BlockType_StoneMultiblock> {

	public BlockStoneMultiblock() {
		super("stone_multiblock", Material.ROCK, PropertyEnum.create("type", BlockType_StoneMultiblock.class), ItemBlockITBase.class, IEProperties.BOOLEANS[0]);
		setHardness(2.0F);
		setResistance(20f);
		this.setAllNotNormalBlock();
		lightOpacity = 0;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityCokeOvenAdvancedSlave) return ((TileEntityCokeOvenAdvancedSlave)te).pos == 1 || ((TileEntityCokeOvenAdvancedSlave)te).pos == 4 || ((TileEntityCokeOvenAdvancedSlave)te).pos == 7 || (((TileEntityCokeOvenAdvancedSlave)te).pos == 31);
		return true;
	}

	@Override
	public TileEntity createBasicTE(World worldIn, BlockType_StoneMultiblock type) {
		switch(type) {
			case COKE_OVEN_ADVANCED:
				return new TileEntityCokeOvenAdvancedMaster();
			case COKE_OVEN_ADVANCED_SLAVE:
				return new TileEntityCokeOvenAdvancedSlave();
		}

		return null;
	}
}