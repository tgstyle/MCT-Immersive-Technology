package mctmods.immersivetechnology.common.data.generators;

import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.client.model.generators.loaders.ObjModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

public class DynamicModelProvider extends ModelProvider<DynamicModelProvider.SimpleModelBuilder> {
    public DynamicModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) { super(output, Reference.MODID, "dynamic", rl -> new SimpleModelBuilder(rl, existingFileHelper), existingFileHelper); }

    @Override protected void registerModels() {
        getBuilder("dynamic/advanced_coke_oven_baseheater_fan")
                .customLoader(ObjModelBuilder::begin)
                .modelLocation(modLoc("models/block/metal/advanced_coke_oven_baseheater/advanced_coke_oven_baseheater_fan.obj"))
                .flipV(true)
                .automaticCulling(false)
                .shadeQuads(true)
                .emissiveAmbient(true)
                .end()
                .texture("particle", modLoc("block/metal/advanced_coke_oven_baseheater"))
                .renderType("cutout");
        getBuilder("dynamic/rotor")
                .customLoader(ObjModelBuilder::begin)
                .modelLocation(modLoc("models/multiblock/metal/rotor/rotor.obj"))
                .flipV(true)
                .automaticCulling(false)
                .shadeQuads(true)
                .emissiveAmbient(true)
                .end()
                .texture("particle", modLoc("multiblock/metal/rotor"))
                .renderType("cutout");
        getBuilder("dynamic/rotor_east_west")
                .customLoader(ObjModelBuilder::begin)
                .modelLocation(modLoc("models/multiblock/metal/rotor/rotor_east_west.obj"))
                .flipV(true)
                .automaticCulling(false)
                .shadeQuads(true)
                .emissiveAmbient(true)
                .end()
                .texture("particle", modLoc("multiblock/metal/rotor"))
                .renderType("cutout");
        getBuilder("dynamic/gas_turbine_fan")
                .customLoader(ObjModelBuilder::begin)
                .modelLocation(modLoc("models/multiblock/metal/gas_turbine/gas_turbine_fan.obj"))
                .flipV(true)
                .automaticCulling(false)
                .shadeQuads(true)
                .emissiveAmbient(true)
                .end()
                .texture("particle", modLoc("multiblock/metal/gas_turbine"))
                .renderType("cutout");
        getBuilder("dynamic/solar_collector_panel")
                .customLoader(ObjModelBuilder::begin)
                .modelLocation(modLoc("models/multiblock/metal/solar_collector/solar_collector_panel.obj"))
                .flipV(true)
                .automaticCulling(false);
        getBuilder("dynamic/solar_collector_support")
                .customLoader(ObjModelBuilder::begin)
                .modelLocation(modLoc("models/multiblock/metal/solar_collector/solar_collector_support.obj"))
                .flipV(true)
                .automaticCulling(false);
        getBuilder("dynamic/solar_reflector_mirror")
                .customLoader(ObjModelBuilder::begin)
                .modelLocation(modLoc("models/multiblock/metal/solar_reflector/solar_reflector_mirror.obj"))
                .flipV(true)
                .automaticCulling(false);
        getBuilder("dynamic/solar_reflector_support")
                .customLoader(ObjModelBuilder::begin)
                .modelLocation(modLoc("models/multiblock/metal/solar_reflector/solar_reflector_support.obj"))
                .flipV(true)
                .automaticCulling(false);
        getBuilder("dynamic/stylenox_rods")
                .customLoader(ObjModelBuilder::begin)
                .modelLocation(modLoc("models/multiblock/metal/stylenox/stylenox_rods.obj"))
                .flipV(true)
                .automaticCulling(false)
                .shadeQuads(true)
                .emissiveAmbient(true)
                .end()
                .texture("particle", modLoc("multiblock/metal/stylenox"))
                .renderType("cutout");
    }


    @Override @Nonnull public String getName() { return "IT Dynamic models"; }

    public static class SimpleModelBuilder extends ModelBuilder<SimpleModelBuilder> {
        public SimpleModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper) { super(outputLocation, existingFileHelper); }
    }
}
