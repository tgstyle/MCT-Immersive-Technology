package mctmods.immersivetechnology.common.blocks.metal;

import mctmods.immersivetechnology.common.blocks.BlockITBase;
import mctmods.immersivetechnology.common.blocks.ItemBlockITBase;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalDecoration;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;

public class BlockMetalDecoration extends BlockITBase<BlockType_MetalDecoration> {
	public BlockMetalDecoration() {
		super("metal_decoration", Material.IRON, PropertyEnum.create("type", BlockType_MetalDecoration.class), ItemBlockITBase.class);
		this.setHardness(3.0F);
		this.setResistance(15.0F);
	}
}
