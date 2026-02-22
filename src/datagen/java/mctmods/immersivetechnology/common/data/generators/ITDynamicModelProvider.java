package mctmods.immersivetechnology.common.data.generators;

import mctmods.immersivetechnology.common.data.loaders.ITObjModelBuilder;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

public class ITDynamicModelProvider extends ModelProvider<ITDynamicModelProvider.SimpleModelBuilder> {
    private final ITBlockStateProvider multiblocks;

    public ITDynamicModelProvider(ITBlockStateProvider multiblocks, PackOutput output, ExistingFileHelper existingFileHelper) { super(output, ITLib.MODID, "dynamic", rl -> new SimpleModelBuilder(rl, existingFileHelper), existingFileHelper); this.multiblocks = multiblocks; }

    @Override protected void registerModels() {
        for(Map.Entry<Block, ModelFile> multiblock : multiblocks.unsplitModels.entrySet()) { withExistingParent(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(multiblock.getKey())).getPath(), multiblock.getValue().getLocation()); }
        getBuilder("dynamic/rotor")
                .customLoader(ITObjModelBuilder::new)
                .modelLocation(modLoc("models/multiblock/metal/obj/rotor/rotor.obj"))
                .flipV(true)
                .automaticCulling(false)
                .shadeQuads(true)
                .emissiveAmbient(true)
                .end()
                .texture("particle", modLoc("multiblock/metal/rotor"))
                .renderType("cutout");
        getBuilder("dynamic/rotor_east_west")
                .customLoader(ITObjModelBuilder::new)
                .modelLocation(modLoc("models/multiblock/metal/obj/rotor/rotor_east_west.obj"))
                .flipV(true)
                .automaticCulling(false)
                .shadeQuads(true)
                .emissiveAmbient(true)
                .end()
                .texture("particle", modLoc("multiblock/metal/rotor"))
                .renderType("cutout");
        getBuilder("dynamic/solar_reflector_mirror")
                .customLoader(ITObjModelBuilder::new)
                .modelLocation(modLoc("models/multiblock/metal/obj/solar_reflector/solar_reflector_mirror.obj"))
                .flipV(true)
                .automaticCulling(false);
        getBuilder("dynamic/solar_reflector_support")
                .customLoader(ITObjModelBuilder::new)
                .modelLocation(modLoc("models/multiblock/metal/obj/solar_reflector/solar_reflector_support.obj"))
                .flipV(true)
                .automaticCulling(false);
    }

    @Override @Nonnull public String getName() { return "IT Dynamic models"; }

    public static class SimpleModelBuilder extends ModelBuilder<SimpleModelBuilder> {
        public SimpleModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper) { super(outputLocation, existingFileHelper); }
    }
}
