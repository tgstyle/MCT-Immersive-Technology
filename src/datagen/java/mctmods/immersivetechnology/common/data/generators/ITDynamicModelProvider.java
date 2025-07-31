package mctmods.immersivetechnology.common.data.generators;

import mctmods.immersivetechnology.client.renderer.CokeOvenHeaterRenderer;
import mctmods.immersivetechnology.client.renderer.GasTurbineRenderer;
import mctmods.immersivetechnology.client.renderer.SteamTurbineRenderer;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.client.model.generators.loaders.ObjModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Map.Entry;
import java.util.Objects;

public class ITDynamicModelProvider extends ModelProvider<ITDynamicModelProvider.SimpleModelBuilder>  {
    private final ITBlockStateProvider multiblocks;

    public ITDynamicModelProvider(ITBlockStateProvider multiblocks, PackOutput output, ExistingFileHelper existingFileHelper)  {
        super(output, ITLib.MODID, "dynamic", rl -> new SimpleModelBuilder(rl, existingFileHelper), existingFileHelper);
        this.multiblocks = multiblocks;
    }

    @Override
    protected void registerModels() {
        for(Entry<Block, ModelFile> multiblock : multiblocks.unsplitModels.entrySet()) { withExistingParent(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(multiblock.getKey())).getPath(), multiblock.getValue().getLocation()); }
        getBuilder(CokeOvenHeaterRenderer.NAME)
                .customLoader(ObjModelBuilder::begin)
                .modelLocation(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "models/block/metal/obj/coke_oven_heater_fan.obj"))
                .flipV(true);
        getBuilder(SteamTurbineRenderer.NAME)
                .customLoader(ObjModelBuilder::begin)
                .modelLocation(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "models/block/multiblock/obj/steam_turbine/steam_turbine_rotor.obj"))
                .flipV(true);
        getBuilder(SteamTurbineRenderer.NAME_EAST_WEST)
                .customLoader(ObjModelBuilder::begin)
                .modelLocation(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "models/block/multiblock/obj/steam_turbine/steam_turbine_rotor_west_east.obj"))
                .flipV(true);
        getBuilder(GasTurbineRenderer.NAME)
                .customLoader(ObjModelBuilder::begin)
                .modelLocation(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "models/block/multiblock/obj/gas_turbine/gas_turbine_rotor.obj"))
                .flipV(true);
        getBuilder(GasTurbineRenderer.NAME_EAST_WEST)
                .customLoader(ObjModelBuilder::begin)
                .modelLocation(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "models/block/multiblock/obj/gas_turbine/gas_turbine_rotor_east_west.obj"))
                .flipV(true);
    }

    @Nonnull
    @Override
    public String getName() { return "IT Dynamic models"; }

    public static class SimpleModelBuilder extends ModelBuilder<SimpleModelBuilder> {  public SimpleModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper) { super(outputLocation, existingFileHelper); } }
}
