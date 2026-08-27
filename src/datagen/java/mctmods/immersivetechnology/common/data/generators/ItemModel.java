package mctmods.immersivetechnology.common.data.generators;

import mctmods.immersivetechnology.common.data.builders.ModObjModelBuilder;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.ModFluids;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Objects;

public class ItemModel extends ItemModelProvider {
    public ItemModel(DataGenerator generator, ExistingFileHelper existingFileHelper) { super(generator.getPackOutput(), Reference.MODID, existingFileHelper); }

    private void generateBlockItem(String item_name, String parent_loc) {
        generateBlockItem(item_name, parent_loc, true, 0, 0, 0.625f);
    }

    private void generateBlockItem(String item_name, String parent_loc, boolean useBlockPrefix, float guiTransX, float guiTransY, float guiScale) {
        String prefix = useBlockPrefix ? "block/" : "";
        ModelFile parentModel = new ModelFile.UncheckedModelFile(modLoc(prefix + parent_loc));
        getBuilder(item_name).parent(parentModel)
                .transforms()
                .transform(ItemDisplayContext.GUI).rotation(30, 225, 0).translation(guiTransX, guiTransY, 0).scale(guiScale, guiScale, guiScale).end()
                .transform(ItemDisplayContext.FIXED).rotation(0, 180, 0).scale(0.5f, 0.5f, 0.5f).end()
                .transform(ItemDisplayContext.GROUND).translation(0, 3, 0).scale(0.25f, 0.25f, 0.25f).end()
                .transform(ItemDisplayContext.HEAD).rotation(0, 180, 0).translation(0, 0, 0).scale(1f, 1f, 1f).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 135, 0).scale(0.4f, 0.4f, 0.4f).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 315, 0).scale(0.4f, 0.4f, 0.4f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(75, 45, 0).translation(0, 2.5f, 0).scale(0.375f, 0.375f, 0.375f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(75, 45, 0).translation(0, 2.5f, 0).scale(0.375f, 0.375f, 0.375f).end()
                .end();
    }

    private void generateBaseHeaterItem() {
        ItemModelBuilder builder = getBuilder("advanced_coke_oven_baseheater")
                .customLoader(ModObjModelBuilder::new)
                .modelLocation(modLoc("models/block/metal/obj/advanced_coke_oven_baseheater/advanced_coke_oven_baseheater.obj"))
                .automaticCulling(false)
                .shadeQuads(true)
                .flipV(true)
                .emissiveAmbient(false)
                .visibility("Fan", false)
                .visibility("fan", false)
                .visibility("Rotor", false)
                .visibility("rotor", false)
                .end();
        builder.transforms()
                .transform(ItemDisplayContext.GUI).rotation(30, 225, 0).translation(0, 0, 0).scale(0.3f, 0.3f, 0.3f).end()
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
        generateFlatItem("formation_tool", "item/formation_tool");
        generateFlatItem("salt", "item/salt");
        generateFlatItem("fuel_element", "item/fuel_element");
        generateFlatItem("spent_element", "item/spent_element");
        generateFlatItem("ingot_chromium", "item/metal/ingots/chromium");
        generateFlatItem("ingot_magnesium", "item/metal/ingots/magnesium");
        generateFlatItem("ingot_magnox", "item/metal/ingots/magnox");
        generateFlatItem("ingot_plutonium", "item/metal/ingots/plutonium");
        generateFlatItem("ingot_stainless_steel", "item/metal/ingots/stainless_steel");
        generateFlatItem("dust_chromium", "item/metal/dusts/chromium");
        generateFlatItem("dust_magnesium", "item/metal/dusts/magnesium");
        generateFlatItem("dust_magnox", "item/metal/dusts/magnox");
        generateFlatItem("dust_spent_fuel", "item/metal/dusts/spent_fuel");
        generateFlatItem("dust_stainless_steel", "item/metal/dusts/stainless_steel");
        generateFlatItem("raw_chromium", "item/metal/raw/chromium");
        generateFlatItem("raw_magnesium", "item/metal/raw/magnesium");
        generateFlatItem("nugget_nuclear_waste", "item/metal/nuggets/nuclear_waste");
        generateFlatItem("nugget_stainless_steel", "item/metal/nuggets/stainless_steel");
        generateFlatItem("plate_magnox", "item/metal/plates/magnox");
        generateFlatItem("plate_stainless_steel", "item/metal/plates/stainless_steel");
        generateFlatItem("stick_stainless_steel", "item/metal/rods/stainless_steel");
        generateFlatItem("pellet_uranium", "item/metal/pellets/uranium");
    }

    private void generateFlatItem(String name, String texture) { withExistingParent(name, mcLoc("item/generated")).texture("layer0", modLoc(texture)); }

    private void createBucket(ModFluids.FluidEntry entry) {
        boolean isGas = entry.type().get().getDensity() < 0;
        getBuilder(name(entry.getBucket()))
                .parent(new ModelFile.UncheckedModelFile("neoforge:item/bucket"))
                .customLoader(DynamicFluidContainerModelBuilder::begin)
                .fluid(entry.getStill())
                .flipGas(isGas);
    }

    @Override protected void registerModels() {
        generateBaseHeaterItem();
        generateBlockItem("barrel_creative", "metal/barrel_creative");
        generateBlockItem("barrel_open", "metal/barrel_open");
        generateBlockItem("barrel_steel", "metal/barrel_steel");
        generateBlockItem("crate_creative", "wooden/crate_creative");
        generateBlockItem("heat_creative", "metal/heat_creative");
        generateBlockItem("reinforced_coke_brick", "stone/reinforced_coke_brick");
        generateBlockItem("rotor_creative", "dynamic/rotor", false, -8, 4, 0.7f);
        generateBlockItem("slab_reinforced_coke_brick", "stone/slab_reinforced_coke_brick");
        generateBlockItem("graphite_moderator", "metal/graphite_moderator");
        generateBlockItem("nuclear_engineering", "metal/nuclear_engineering");
        generateBlockItem("reactor_cladding", "metal/reactor_cladding");
        generateBlockItem("storage_stainless_steel", "metal/storage_stainless_steel");
        generateBlockItem("storage_chromium", "metal/storage_chromium");
        generateBlockItem("storage_magnesium", "metal/storage_magnesium");
        generateBlockItem("sheetmetal_stainless_steel", "metal/sheetmetal_stainless_steel");
        generateBlockItem("ore_chromium", "metal/ore_chromium");
        generateBlockItem("deepslate_ore_chromium", "metal/deepslate_ore_chromium");
        generateBlockItem("ore_magnesium", "metal/ore_magnesium");
        generateBlockItem("deepslate_ore_magnesium", "metal/deepslate_ore_magnesium");
        generateBlockItem("technology_engineering", "metal/technology_engineering");
        generateBlockItem("trash_energy", "metal/trash_energy");
        generateBlockItem("trash_fluid", "metal/trash_fluid");
        generateBlockItem("trash_item", "metal/trash_item");
        generateBlockItem("valve_fluid", "metal/valve_fluid_open");
        generateBlockItem("valve_limiter", "metal/valve_limiter");
        generateBlockItem("valve_load", "metal/valve_load_open");

        generateGeneratedItem();

        ModFluids.ALL_ENTRIES.forEach(this::createBucket);
    }

    private String name(ItemLike item) { return Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item.asItem())).getPath(); }
}
