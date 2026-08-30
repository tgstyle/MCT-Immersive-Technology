package mctmods.immersivetechnology.client.render.multiblock;

import com.immersiveconvergence.api.client.split.BakedSplitModel;
import com.immersiveconvergence.api.multiblock.TemplateMultiblock;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartAlternator;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartBoiler;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartCoolingTower;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartDistiller;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartElectrolyticCrucibleBattery;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartGasTurbine;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartHeatExchanger;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartHighPressureSteamTurbine;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartMeltingCrucible;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartRadiator;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSolarMelter;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSolarTower;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSteamTurbine;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSteelSheetmetalTank;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentitiesmultiblockpart.TileEntityITMultiblockPartAdvancedCokeOven;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ImmersiveTechnology.MODID, value = Side.CLIENT)
public class SplitModelHandler {
    private static Map<String, Machine> machinesByFile() {
        Map<String, Machine> byFile = new HashMap<>();
        addMachine(byFile, "metal_multiblock_alternator", TileEntityITMultiblockPartAlternator.instance);
        addMachine(byFile, "metal_multiblock_boiler", TileEntityITMultiblockPartBoiler.instance);
        addMachine(byFile, "metal_multiblock_cooling_tower", TileEntityITMultiblockPartCoolingTower.instance);
        addMachine(byFile, "metal_multiblock_distiller", TileEntityITMultiblockPartDistiller.instance);
        addMachine(byFile, "metal_multiblock_solar_tower", TileEntityITMultiblockPartSolarTower.instance);
        addMachine(byFile, "metal_multiblock_steam_turbine", TileEntityITMultiblockPartSteamTurbine.instance);
        addMachine(byFile, "metal_multiblock_steel_tank", TileEntityITMultiblockPartSteelSheetmetalTank.instance);
        addMachine(byFile, "metal_multiblock1_electrolytic_crucible_battery", TileEntityITMultiblockPartElectrolyticCrucibleBattery.instance);
        addMachine(byFile, "metal_multiblock1_gas_turbine", TileEntityITMultiblockPartGasTurbine.instance);
        addMachine(byFile, "metal_multiblock1_heat_exchanger", TileEntityITMultiblockPartHeatExchanger.instance);
        addMachine(byFile, "metal_multiblock1_high_pressure_steam_turbine", TileEntityITMultiblockPartHighPressureSteamTurbine.instance);
        addMachine(byFile, "metal_multiblock1_melting_crucible", TileEntityITMultiblockPartMeltingCrucible.instance);
        addMachine(byFile, "metal_multiblock1_radiator", TileEntityITMultiblockPartRadiator.instance);
        addMachine(byFile, "metal_multiblock1_solar_melter", TileEntityITMultiblockPartSolarMelter.instance);
        addMachine(byFile, "stone_multiblock_advanced_coke_oven", TileEntityITMultiblockPartAdvancedCokeOven.instance);
        return byFile;
    }

    private static void addMachine(Map<String, Machine> byFile, String masterFile, TemplateMultiblock instance) {
        Machine machine = new Machine(masterFile, instance);
        byFile.put(masterFile, machine);
        byFile.put(masterFile + "_slave", machine);
    }

    @SubscribeEvent public static void onModelBake(ModelBakeEvent event) {
        IRegistry<ModelResourceLocation, IBakedModel> registry = event.getModelRegistry();
        Map<String, Machine> byFile = machinesByFile();
        List<ModelResourceLocation> keys = new ArrayList<>();
        for (ModelResourceLocation mrl : registry.getKeys()) {
            if (ImmersiveTechnology.MODID.equals(mrl.getNamespace()) && byFile.containsKey(mrl.getPath()) && isSplitVariant(mrl.getVariant())) { keys.add(mrl); }
        }
        Map<String, IBakedModel> bases = new HashMap<>();
        for (ModelResourceLocation mrl : keys) {
            Machine machine = byFile.get(mrl.getPath());
            if (mrl.getPath().equals(machine.masterFile) && mrl.getVariant().contains("_0multiblockslave=false")) { bases.put(variantKey(machine, mrl.getVariant()), registry.getObject(mrl)); }
        }
        Map<String, BakedSplitModel> wrappers = new HashMap<>();
        for (ModelResourceLocation mrl : keys) {
            Machine machine = byFile.get(mrl.getPath());
            String key = variantKey(machine, mrl.getVariant());
            IBakedModel base = bases.get(key);
            if (base == null) { continue; }
            BakedSplitModel wrapper = wrappers.get(key);
            if (wrapper == null) {
                wrapper = new BakedSplitModel(base, machine.instance.worldOffsetsFromMaster(facingOf(mrl.getVariant()), mirroredOf(mrl.getVariant())));
                wrappers.put(key, wrapper);
            }
            registry.putObject(mrl, wrapper);
        }
    }

    private static boolean isSplitVariant(String variant) { return !variant.contains("inventory") && !variant.contains("_1dynamicrender=true") && variant.contains("facing="); }

    private static String variantKey(Machine machine, String variant) { return machine.masterFile + "|" + facingOf(variant) + "|" + mirroredOf(variant); }

    private static EnumFacing facingOf(String variant) {
        int start = variant.indexOf("facing=") + 7;
        int end = variant.indexOf(',', start);
        String name = end < 0 ? variant.substring(start) : variant.substring(start, end);
        EnumFacing facing = EnumFacing.byName(name);
        return facing == null ? EnumFacing.NORTH : facing;
    }

    private static boolean mirroredOf(String variant) { return variant.contains("boolean0=true"); }

    private static final class Machine {
        final String masterFile;
        final TemplateMultiblock instance;

        Machine(String masterFile, TemplateMultiblock instance) {
            this.masterFile = masterFile;
            this.instance = instance;
        }
    }
}
