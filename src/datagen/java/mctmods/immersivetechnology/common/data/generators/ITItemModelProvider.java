package mctmods.immersivetechnology.common.data.generators;

import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITFluids;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.Objects;

public class ITItemModelProvider extends ItemModelProvider {
    private final Logger logger = ITLib.getNewLogger();
    public ITItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) { super(generator.getPackOutput(), ITLib.MODID, existingFileHelper); }

    private void generateBlockItem(String item_name, String parent_loc) {
        ModelFile parentModel = new ModelFile.UncheckedModelFile(modLoc("block/" + parent_loc));
        getBuilder(item_name).parent(parentModel);
    }

    private void generateGeneratedItem() { withExistingParent("it_formation_tool", mcLoc("item/generated")).texture("layer0", modLoc("item/" + "it_formation_tool")); }

    private void createBucket(ITFluids.FluidEntry entry) {
        boolean isGas = entry.type().get().getDensity() < 0;
        withExistingParent(name(entry.getBucket()), forgeLoc()).customLoader(DynamicFluidContainerModelBuilder::begin).fluid(entry.getStill()).flipGas(isGas);
    }

    @Override
    protected void registerModels() {
        generateBlockItem("reinforced_coke_brick", "stone/reinforced_coke_brick");
        generateBlockItem("creative_barrel", "metal/creative_barrel");
        generateBlockItem("steel_barrel", "metal/steel_barrel");
        generateBlockItem("open_barrel", "metal/open_barrel");
        generateGeneratedItem();
        ITFluids.ALL_ENTRIES.forEach(this::createBucket);
    }

    private String name(ItemLike item) { return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item.asItem())).getPath(); }

    private ResourceLocation forgeLoc() { return ResourceLocation.fromNamespaceAndPath("forge", "item/bucket"); }
}
