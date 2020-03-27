package mctmods.immersivetechnology.common.util.compat.jei;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalMultiblock;
import mctmods.immersivetechnology.common.blocks.stone.types.BlockType_StoneMultiblock;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GenericMultiblockIngredient {

    public static List<GenericMultiblockIngredient> list = new ArrayList<>();

    public static GenericMultiblockIngredient STEAM_TURBINE = new GenericMultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.STEAM_TURBINE.getMeta()));
    public static GenericMultiblockIngredient DISTILLER = new GenericMultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.DISTILLER.getMeta()));
    public static GenericMultiblockIngredient SOLAR_TOWER = new GenericMultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.SOLAR_TOWER.getMeta()));
    public static GenericMultiblockIngredient BOILER = new GenericMultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.BOILER.getMeta()));
    public static GenericMultiblockIngredient COKE_OVEN_ADVANCED = new GenericMultiblockIngredient(new ItemStack(ITContent.blockStoneMultiblock, 1, BlockType_StoneMultiblock.COKE_OVEN_ADVANCED.getMeta()));

    public ItemStack renderStack;

    public GenericMultiblockIngredient(ItemStack renderStack) {
        this.renderStack = renderStack;
        list.add(this);
    }

}
