package mctmods.immersivetechnology.common.blocks;

import mctmods.immersivetechnology.common.ITBlockContext;

import com.immersiveconvergence.api.block.ICBlockBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;

public class BlockITBase<E extends Enum<E> & ICBlockBase.IBlockEnum> extends ICBlockBase<E> {
    public BlockITBase(String name, Material material, PropertyEnum<E> mainProperty, Class<? extends ItemBlockITBase> itemBlock, Object... additionalProperties) { super(ITBlockContext.CONTEXT, name, material, mainProperty, itemBlock, additionalProperties); }
}
