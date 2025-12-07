package mctmods.immersivetechnology.common.data.generators;

import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITFluids;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class ITItemModelProvider extends ItemModelProvider {
    public ITItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) { super(generator.getPackOutput(), ITLib.MODID, existingFileHelper); }

    private void generateBlockItem(String item_name, String parent_loc) {
        generateBlockItem(item_name, parent_loc, true);
    }

    private void generateBlockItem(String item_name, String parent_loc, boolean useBlockPrefix) {
        String prefix = useBlockPrefix ? "block/" : "";
        ModelFile parentModel = new ModelFile.UncheckedModelFile(modLoc(prefix + parent_loc));
        getBuilder(item_name).parent(parentModel)
                .transforms()
                .transform(ItemDisplayContext.GUI).rotation(30, 225, 0).translation(0, 0, 0).scale(0.625f, 0.625f, 0.625f).end()
                .transform(ItemDisplayContext.FIXED).rotation(0, 180, 0).scale(0.5f, 0.5f, 0.5f).end()
                .transform(ItemDisplayContext.GROUND).translation(0, 3, 0).scale(0.25f, 0.25f, 0.25f).end()
                .transform(ItemDisplayContext.HEAD).rotation(0, 180, 0).translation(0, 0, 0).scale(1f, 1f, 1f).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 135, 0).scale(0.4f, 0.4f, 0.4f).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 315, 0).scale(0.4f, 0.4f, 0.4f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(75, 45, 0).translation(0, 2.5f, 0).scale(0.375f, 0.375f, 0.375f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(75, 45, 0).translation(0, 2.5f, 0).scale(0.375f, 0.375f, 0.375f).end()
                .end();
    }

    private void generateGeneratedItem() {
        withExistingParent("formation_tool", mcLoc("item/generated")).texture("layer0", modLoc("item/" + "formation_tool"));
        withExistingParent("salt", mcLoc("item/generated")).texture("layer0", modLoc("item/" + "salt"));
    }

    private void createBucket(ITFluids.FluidEntry entry) {
        boolean isGas = entry.type().get().getDensity() < 0;
        withExistingParent(name(entry.getBucket()), forgeLoc()).customLoader(DynamicFluidContainerModelBuilder::begin).fluid(entry.getStill()).flipGas(isGas);
    }

    @Override
    protected void registerModels() {
        generateBlockItem("barrel_creative", "metal/barrel_creative");
        generateBlockItem("barrel_open", "metal/barrel_open");
        generateBlockItem("barrel_steel", "metal/barrel_steel");
        generateBlockItem("rotor_creative", "dynamic/rotor", false);
        generateBlockItem("reinforced_coke_brick", "stone/reinforced_coke_brick");
        generateBlockItem("slab_reinforced_coke_brick", "stone/slab_reinforced_coke_brick");
        generateBlockItem("technology_engineering", "metal/technology_engineering");
        generateBlockItem("trash_energy", "metal/trash_energy");
        generateBlockItem("trash_fluid", "metal/trash_fluid");
        generateBlockItem("trash_item", "metal/trash_item");
        generateBlockItem("valve_fluid", "metal/valve_fluid_open");
        generateBlockItem("valve_limiter", "metal/valve_limiter");
        generateBlockItem("valve_load", "metal/valve_load_open");

        generateGeneratedItem();

        ITFluids.ALL_ENTRIES.forEach(this::createBucket);
    }

    private String name(ItemLike item) { return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item.asItem())).getPath(); }

    private ResourceLocation forgeLoc() { return ResourceLocation.fromNamespaceAndPath("forge", "item/bucket"); }
}
