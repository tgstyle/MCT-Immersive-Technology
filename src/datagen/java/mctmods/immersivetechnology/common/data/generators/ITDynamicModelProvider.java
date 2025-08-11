package mctmods.immersivetechnology.common.data.generators;

import mctmods.immersivetechnology.client.renderer.GasTurbineRenderer;
import mctmods.immersivetechnology.client.renderer.SteamTurbineRenderer;
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
import java.util.Map.Entry;
import java.util.Objects;
import com.google.common.collect.ImmutableMap;

public class ITDynamicModelProvider extends ModelProvider<ITDynamicModelProvider.SimpleModelBuilder> {
    private final ITBlockStateProvider multiblocks;

    public ITDynamicModelProvider(ITBlockStateProvider multiblocks, PackOutput output, ExistingFileHelper existingFileHelper) { super(output, ITLib.MODID, "dynamic", rl -> new SimpleModelBuilder(rl, existingFileHelper), existingFileHelper); this.multiblocks = multiblocks; }

    @Override
    protected void registerModels() {
        for(Entry<Block, ModelFile> multiblock : multiblocks.unsplitModels.entrySet()) { withExistingParent(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(multiblock.getKey())).getPath(), multiblock.getValue().getLocation()); }
        getBuilder("dynamic/rotor")
                .customLoader(ITObjModelBuilder::begin)
                .model(modLoc("models/block/multiblock/obj/rotor/rotor.obj"))
                .flipV(true)
                .automaticCulling(false);
        getBuilder("dynamic/rotor_east_west")
                .customLoader(ITObjModelBuilder::begin)
                .model(modLoc("models/block/multiblock/obj/rotor/rotor_east_west.obj"))
                .flipV(true)
                .automaticCulling(false);
        getBuilder("coke_oven_heater_fan")
                .customLoader(ITObjModelBuilder::begin)
                .model(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "models/block/metal/obj/coke_oven_heater.obj"))
                .flipV(true)
                .visibility(ImmutableMap.of("body", false, "fan", true));
    }

    @Nonnull
    @Override
    public String getName() { return "IT Dynamic models"; }

    public static class SimpleModelBuilder extends ModelBuilder<SimpleModelBuilder> {
        public SimpleModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper) { super(outputLocation, existingFileHelper); }
    }
}