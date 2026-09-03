package mctmods.immersivetechnology.common.util.compat.jei;

import com.immersiveconvergence.api.jei.MultiblockIngredient;

import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock1;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock2;
import mctmods.immersivetechnology.common.multiblocks.stone.types.BlockType_StoneMultiblock;

import net.minecraft.item.ItemStack;

public final class ITMultiblockIngredients {
    public static MultiblockIngredient STEAM_TURBINE;
    public static MultiblockIngredient DISTILLER;
    public static MultiblockIngredient SOLAR_TOWER;
    public static MultiblockIngredient BOILER_TANK;
    public static MultiblockIngredient BOILER_LIQUID;
    public static MultiblockIngredient BOILER_SOLID;
    public static MultiblockIngredient COOLING_TOWER;
    public static MultiblockIngredient GAS_TURBINE;
    public static MultiblockIngredient HEAT_EXCHANGER;
    public static MultiblockIngredient HIGH_PRESSURE_STEAM_TURBINE;
    public static MultiblockIngredient ELECTROLYTIC_CRUCIBLE_BATTERY;
    public static MultiblockIngredient MELTING_CRUCIBLE;
    public static MultiblockIngredient RADIATOR;
    public static MultiblockIngredient SOLAR_MELTER;
    public static MultiblockIngredient ADVANCED_COKE_OVEN;

    private ITMultiblockIngredients() {}

    public static void init() {
        if (Multiblocks.enable.enable_steamTurbine) { STEAM_TURBINE = new MultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.STEAM_TURBINE.getMeta())); }
        if (Multiblocks.enable.enable_distiller) { DISTILLER = new MultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.DISTILLER.getMeta())); }
        if (Multiblocks.enable.enable_solarTower) { SOLAR_TOWER = new MultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.SOLAR_TOWER.getMeta())); }
        if (Multiblocks.enable.enable_boiler) {
            BOILER_TANK = new MultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.BOILER_TANK.getMeta()));
            BOILER_LIQUID = new MultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.BOILER_LIQUID.getMeta()));
        }
        if (Multiblocks.enable.enable_boilerSolid) { BOILER_SOLID = new MultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock2, 1, BlockType_MetalMultiblock2.BOILER_SOLID.getMeta())); }
        if (Multiblocks.enable.enable_coolingTower) { COOLING_TOWER = new MultiblockIngredient(new ItemStack(ITContent.blockStoneMultiblock, 1, BlockType_StoneMultiblock.COOLING_TOWER.getMeta())); }
        if (Multiblocks.enable.enable_gasTurbine) { GAS_TURBINE = new MultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.GAS_TURBINE.getMeta())); }
        if (Multiblocks.enable.enable_heatExchanger) { HEAT_EXCHANGER = new MultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.HEAT_EXCHANGER.getMeta())); }
        if (Multiblocks.enable.enable_highPressureSteamTurbine) { HIGH_PRESSURE_STEAM_TURBINE = new MultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.HIGH_PRESSURE_STEAM_TURBINE.getMeta())); }
        if (Multiblocks.enable.enable_electrolyticCrucibleBattery) { ELECTROLYTIC_CRUCIBLE_BATTERY = new MultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.ELECTROLYTIC_CRUCIBLE_BATTERY.getMeta())); }
        if (Multiblocks.enable.enable_meltingCrucible) { MELTING_CRUCIBLE = new MultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.MELTING_CRUCIBLE.getMeta())); }
        if (Multiblocks.enable.enable_radiator) { RADIATOR = new MultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.RADIATOR.getMeta())); }
        if (Multiblocks.enable.enable_solarMelter) { SOLAR_MELTER = new MultiblockIngredient(new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.SOLAR_MELTER.getMeta())); }
        if (Multiblocks.enable.enable_advancedCokeOven) { ADVANCED_COKE_OVEN = new MultiblockIngredient(new ItemStack(ITContent.blockStoneMultiblock, 1, BlockType_StoneMultiblock.ADVANCED_COKE_OVEN.getMeta())); }
    }
}
