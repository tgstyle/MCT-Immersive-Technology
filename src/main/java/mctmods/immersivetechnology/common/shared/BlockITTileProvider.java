package mctmods.immersivetechnology.common.shared;

import com.immersiveconvergence.api.block.ICBlockBase;
import com.immersiveconvergence.api.block.ICBlockTileProvider;

import mctmods.immersivetechnology.common.ITBlockContext;
import mctmods.immersivetechnology.common.blocks.ItemBlockITBase;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;

public abstract class BlockITTileProvider<E extends Enum<E> & ICBlockBase.IBlockEnum> extends ICBlockTileProvider<E> {
    public BlockITTileProvider(String name, Material material, PropertyEnum<E> mainProperty, Class<? extends ItemBlockITBase> itemBlock, Object... additionalProperties) { super(ITBlockContext.CONTEXT, name, material, mainProperty, itemBlock, additionalProperties); }
}
