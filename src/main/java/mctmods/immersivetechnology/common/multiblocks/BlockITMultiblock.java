package mctmods.immersivetechnology.common.multiblocks;

import com.immersiveconvergence.api.block.ICBlockBase;
import com.immersiveconvergence.api.block.ICBlockMultiblock;

import mctmods.immersivetechnology.common.ITBlockContext;
import mctmods.immersivetechnology.common.blocks.ItemBlockITBase;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;

public abstract class BlockITMultiblock<E extends Enum<E> & ICBlockBase.IBlockEnum> extends ICBlockMultiblock<E> {
    public BlockITMultiblock(String name, Material material, PropertyEnum<E> mainProperty, Class<? extends ItemBlockITBase> itemBlock, Object... additionalProperties) { super(ITBlockContext.CONTEXT, name, material, mainProperty, itemBlock, additionalProperties); }
}
