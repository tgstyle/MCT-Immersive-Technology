package ferro2000.immersivetech.common.blocks.stone;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDevices;
import ferro2000.immersivetech.common.blocks.BlockITMultiblock;
import ferro2000.immersivetech.common.blocks.ItemBlockITBase;
import ferro2000.immersivetech.common.blocks.stone.tileentities.TileEntityCokeOvenAdvanced;
import ferro2000.immersivetech.common.blocks.stone.types.BlockType_StoneMultiblock;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockStoneMultiblock extends BlockITMultiblock<BlockType_StoneMultiblock> {

	public BlockStoneMultiblock()
	{
		super("stone_multiblock",Material.ROCK, PropertyEnum.create("type", BlockType_StoneMultiblock.class), ItemBlockITBase.class, IEProperties.BOOLEANS[0]);
		setHardness(2.0F);
		setResistance(20f);
		this.setAllNotNormalBlock();
		lightOpacity = 0;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityCokeOvenAdvanced)
			return ((TileEntityCokeOvenAdvanced)te).pos==1 || ((TileEntityCokeOvenAdvanced)te).pos==4 || ((TileEntityCokeOvenAdvanced)te).pos==7 || (((TileEntityCokeOvenAdvanced)te).pos==31);
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		
		switch(BlockType_StoneMultiblock.values()[meta])
		{
			case COKE_OVEN_ADVANCED:
				return new TileEntityCokeOvenAdvanced();
		}
		return null;
		
	}

}
