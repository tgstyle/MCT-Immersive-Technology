package ferro2000.immersivetech.common.blocks.stone;

import ferro2000.immersivetech.common.blocks.BlockITBase;
import ferro2000.immersivetech.common.blocks.ItemBlockITBase;
import ferro2000.immersivetech.common.blocks.stone.types.BlockType_StoneDecoration;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;

public class BlockStoneDecoration extends BlockITBase<BlockType_StoneDecoration> {

	public BlockStoneDecoration()
	{
		super("stone_decoration", Material.ROCK, PropertyEnum.create("type", BlockType_StoneDecoration.class), ItemBlockITBase.class);
	}

}
