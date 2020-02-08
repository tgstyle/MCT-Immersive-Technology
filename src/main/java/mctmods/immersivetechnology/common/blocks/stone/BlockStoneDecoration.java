package mctmods.immersivetechnology.common.blocks.stone;

import mctmods.immersivetechnology.common.blocks.BlockITBase;
import mctmods.immersivetechnology.common.blocks.ItemBlockITBase;
import mctmods.immersivetechnology.common.blocks.stone.types.BlockType_StoneDecoration;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;

public class BlockStoneDecoration extends BlockITBase<BlockType_StoneDecoration> {

	public BlockStoneDecoration() {
		super("stone_decoration", Material.ROCK, PropertyEnum.create("type", BlockType_StoneDecoration.class), ItemBlockITBase.class);
		this.setHardness(2.0F);
		this.setResistance(15.0F);
	}

}