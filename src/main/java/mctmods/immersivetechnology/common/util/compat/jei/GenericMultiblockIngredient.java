package mctmods.immersivetechnology.common.util.compat.jei;

import mctmods.immersivetechnology.common.Config.ITConfig.Machines.Multiblock;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalMultiblock;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalMultiblock1;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GenericMultiblockIngredient {

    public static List<GenericMultiblockIngredient> list = new ArrayList<>();

    public static GenericMultiblockIngredient STEAM_TURBINE;
    public static GenericMultiblockIngredient DISTILLER;
    public static GenericMultiblockIngredient SOLAR_TOWER;
    public static GenericMultiblockIngredient BOILER;
    public static GenericMultiblockIngredient COOLING_TOWER;
    public static GenericMultiblockIngredient GAS_TURBINE;
    public static GenericMultiblockIngredient HEAT_EXCHANGER;
    public static GenericMultiblockIngredient HIGH_PRESSURE_STEAM_TURBINE;
    public static GenericMultiblockIngredient ELECTROLYTIC_CRUCIBLE_BATTERY;
    public static GenericMultiblockIngredient MELTING_CRUCIBLE;
    public static GenericMultiblockIngredient RADIATOR;

    static {
        if(Multiblock.enable_steamTurbine) STEAM_TURBINE = new GenericMultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.STEAM_TURBINE.getMeta()));
        if(Multiblock.enable_distiller) DISTILLER = new GenericMultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.DISTILLER.getMeta()));
        if(Multiblock.enable_solarTower) SOLAR_TOWER = new GenericMultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.SOLAR_TOWER.getMeta()));
        if(Multiblock.enable_boiler) BOILER = new GenericMultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.BOILER.getMeta()));
        if(Multiblock.enable_coolingTower) COOLING_TOWER = new GenericMultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.COOLING_TOWER.getMeta()));
        if(Multiblock.enable_gasTurbine) GAS_TURBINE = new GenericMultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.GAS_TURBINE.getMeta()));
        if(Multiblock.enable_heatExchanger) HEAT_EXCHANGER = new GenericMultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.HEAT_EXCHANGER.getMeta()));
        if(Multiblock.enable_highPressureSteamTurbine) HIGH_PRESSURE_STEAM_TURBINE = new GenericMultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.HIGH_PRESSURE_STEAM_TURBINE.getMeta()));
        if(Multiblock.enable_electrolyticCrucibleBattery) ELECTROLYTIC_CRUCIBLE_BATTERY = new GenericMultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.ELECTROLYTIC_CRUCIBLE_BATTERY.getMeta()));
        if(Multiblock.enable_meltingCrucible) MELTING_CRUCIBLE = new GenericMultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.MELTING_CRUCIBLE.getMeta()));
        if(Multiblock.enable_radiator) RADIATOR = new GenericMultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.RADIATOR.getMeta()));
    }

    public ItemStack renderStack;

    public GenericMultiblockIngredient(ItemStack renderStack) {
        this.renderStack = renderStack;
        list.add(this);
    }

}