package mctmods.immersivetechnology.common.util.compat.jei;

import mctmods.immersivetechnology.common.Config.ITConfig.Machines.Multiblock;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalMultiblock;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalMultiblock1;
import mctmods.immersivetechnology.common.blocks.stone.types.BlockType_StoneMultiblock;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GenericMultiblockIngredient {

    public static List<GenericMultiblockIngredient> list = new ArrayList<>();

    public static GenericMultiblockIngredient STEAM_TURBINE;
    public static GenericMultiblockIngredient DISTILLER;
    public static GenericMultiblockIngredient SOLAR_TOWER;
    public static GenericMultiblockIngredient BOILER;
    public static GenericMultiblockIngredient COKE_OVEN_ADVANCED;
    public static GenericMultiblockIngredient COOLING_TOWER;
    public static GenericMultiblockIngredient GAS_TURBINE;

    static {
        if(Multiblock.enable_steamTurbine) STEAM_TURBINE = new GenericMultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.STEAM_TURBINE.getMeta()));
        if(Multiblock.enable_distiller) DISTILLER = new GenericMultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.DISTILLER.getMeta()));
        if(Multiblock.enable_solarTower) SOLAR_TOWER = new GenericMultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.SOLAR_TOWER.getMeta()));
        if(Multiblock.enable_boiler) BOILER = new GenericMultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.BOILER.getMeta()));
        if(Multiblock.enable_advancedCokeOven) COKE_OVEN_ADVANCED = new GenericMultiblockIngredient(new ItemStack(ITContent.blockStoneMultiblock, 1, BlockType_StoneMultiblock.COKE_OVEN_ADVANCED.getMeta()));
        if(Multiblock.enable_coolingTower) COOLING_TOWER = new GenericMultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.COOLING_TOWER.getMeta()));
        if(Multiblock.enable_gasTurbine) GAS_TURBINE = new GenericMultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.GAS_TURBINE.getMeta()));
    }

    public ItemStack renderStack;

    public GenericMultiblockIngredient(ItemStack renderStack) {
        this.renderStack = renderStack;
        list.add(this);
    }

}