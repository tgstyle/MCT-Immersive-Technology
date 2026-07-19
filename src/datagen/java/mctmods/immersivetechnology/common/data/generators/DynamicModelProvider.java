package mctmods.immersivetechnology.common.data.generators;

import mctmods.immersivetechnology.common.data.builders.ModObjModelBuilder;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.util.Map.Entry;
import java.util.Objects;

public class DynamicModelProvider extends ModelProvider<DynamicModelProvider.SimpleModelBuilder> {
    private final ModBlockState multiblocks;

    public DynamicModelProvider(ModBlockState multiblocks, PackOutput output, ExistingFileHelper existingFileHelper) { super(output, Reference.MODID, "dynamic", rl -> new SimpleModelBuilder(rl, existingFileHelper), existingFileHelper); this.multiblocks = multiblocks; }

    @Override protected void registerModels() {
        for(Entry<Block, ModelFile> multiblock : multiblocks.unsplitModels.entrySet()) { withExistingParent(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(multiblock.getKey())).getPath(), multiblock.getValue().getLocation()); }
        getBuilder("dynamic/advanced_coke_oven_baseheater_fan")
                .customLoader(ModObjModelBuilder::new)
                .modelLocation(modLoc("models/block/metal/obj/advanced_coke_oven_baseheater/advanced_coke_oven_baseheater_fan.obj"))
                .flipV(true)
                .automaticCulling(false)
                .shadeQuads(true)
                .emissiveAmbient(true)
                .end()
                .texture("particle", modLoc("block/metal/advanced_coke_oven_baseheater"))
                .renderType("cutout");
        getBuilder("dynamic/rotor")
                .customLoader(ModObjModelBuilder::new)
                .modelLocation(modLoc("models/multiblock/metal/obj/rotor/rotor.obj"))
                .flipV(true)
                .automaticCulling(false)
                .shadeQuads(true)
                .emissiveAmbient(true)
                .end()
                .texture("particle", modLoc("multiblock/metal/rotor"))
                .renderType("cutout");
        getBuilder("dynamic/rotor_east_west")
                .customLoader(ModObjModelBuilder::new)
                .modelLocation(modLoc("models/multiblock/metal/obj/rotor/rotor_east_west.obj"))
                .flipV(true)
                .automaticCulling(false)
                .shadeQuads(true)
                .emissiveAmbient(true)
                .end()
                .texture("particle", modLoc("multiblock/metal/rotor"))
                .renderType("cutout");
        getBuilder("dynamic/solar_reflector_mirror")
                .customLoader(ModObjModelBuilder::new)
                .modelLocation(modLoc("models/multiblock/metal/obj/solar_reflector/solar_reflector_mirror.obj"))
                .flipV(true)
                .automaticCulling(false);
        getBuilder("dynamic/solar_reflector_support")
                .customLoader(ModObjModelBuilder::new)
                .modelLocation(modLoc("models/multiblock/metal/obj/solar_reflector/solar_reflector_support.obj"))
                .flipV(true)
                .automaticCulling(false);
    }


    @Override @Nonnull public String getName() { return "IT Dynamic models"; }

    public static class SimpleModelBuilder extends ModelBuilder<SimpleModelBuilder> {
        public SimpleModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper) { super(outputLocation, existingFileHelper); }
    }
}
