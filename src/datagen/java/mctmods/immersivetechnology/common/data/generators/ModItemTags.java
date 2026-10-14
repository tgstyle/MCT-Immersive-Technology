package mctmods.immersivetechnology.common.data.generators;

import com.immersiveconvergence.api.registration.BlockEntry;
import com.immersiveconvergence.api.registration.ItemEntry;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.ModBlocks;
import mctmods.immersivetechnology.core.registration.ModItems;
import mctmods.immersivetechnology.core.registration.ModTags;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class ModItemTags extends ItemTagsProvider {
    public ModItemTags(PackOutput output, CompletableFuture<Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blocks, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blocks, Reference.MODID, existingFileHelper);
    }

    @Override protected void addTags(@Nonnull Provider provider) {
        tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", "salts"))).add(ModItems.SALT.get());
        tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", "dusts/salt"))).add(ModItems.SALT.get());
        tag(ModTags.igniters).add(Items.TORCH, Items.FLINT_AND_STEEL);
        tag(ModTags.igniters_consume).add(Items.TORCH);
        tag(ModTags.formationTools).add(ModItems.FORMATION_TOOL.get()).addOptional(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "hammer"));
        tag(ModTags.screwdrivers).addOptional(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "screwdriver"));
        tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "tools/hammers"))).add(ModItems.FORMATION_TOOL.get());

        registerMaterial("ingots", "chromium", ModItems.INGOT_CHROMIUM);
        registerMaterial("ingots", "depleted_uranium", ModItems.INGOT_DEPLETED_URANIUM);
        registerMaterial("ingots", "enriched_uranium", ModItems.INGOT_ENRICHED_URANIUM);
        registerMaterial("ingots", "magnesium", ModItems.INGOT_MAGNESIUM);
        registerMaterial("ingots", "magnox", ModItems.INGOT_MAGNOX);
        registerMaterial("ingots", "plutonium", ModItems.INGOT_PLUTONIUM);
        registerMaterial("ingots", "stainless_steel", ModItems.INGOT_STAINLESS_STEEL);
        registerMaterial("dusts", "chromium", ModItems.DUST_CHROMIUM);
        registerMaterial("dusts", "magnesium", ModItems.DUST_MAGNESIUM);
        registerMaterial("dusts", "magnox", ModItems.DUST_MAGNOX);
        registerMaterial("dusts", "spent_fuel", ModItems.DUST_SPENT_FUEL);
        registerMaterial("dusts", "stainless_steel", ModItems.DUST_STAINLESS_STEEL);
        registerMaterial("raw_materials", "chromium", ModItems.RAW_CHROMIUM);
        registerMaterial("raw_materials", "magnesium", ModItems.RAW_MAGNESIUM);
        registerMaterial("nuggets", "nuclear_waste", ModItems.NUGGET_NUCLEAR_WASTE);
        registerMaterial("nuggets", "stainless_steel", ModItems.NUGGET_STAINLESS_STEEL);
        registerMaterial("plates", "magnox", ModItems.PLATE_MAGNOX);
        registerMaterial("plates", "stainless_steel", ModItems.PLATE_STAINLESS_STEEL);
        registerMaterial("rods", "stainless_steel", ModItems.STICK_STAINLESS_STEEL);

        copy(cBlockTag("storage_blocks/chromium"), cItemTag("storage_blocks/chromium"));
        copy(cBlockTag("storage_blocks/magnesium"), cItemTag("storage_blocks/magnesium"));
        copy(cBlockTag("storage_blocks/stainless_steel"), cItemTag("storage_blocks/stainless_steel"));
        copy(cBlockTag("storage_blocks/hop_graphite"), cItemTag("storage_blocks/hop_graphite"));
        copy(cBlockTag("sheetmetals/stainless_steel"), cItemTag("sheetmetals/stainless_steel"));
        copy(cBlockTag("ores/chromium"), cItemTag("ores/chromium"));
        copy(cBlockTag("ores/magnesium"), cItemTag("ores/magnesium"));
        copy(cBlockTag("ores"), cItemTag("ores"));
        ModBlocks.NEEDS_IE.forEach(entry -> tag(drops(entry)).addOptional(entry.getId()));
    }

    static TagKey<Item> drops(BlockEntry<?> entry) { return ItemTags.create(Reference.rl("drops/" + entry.getId().getPath())); }

    private void registerMaterial(String category, String material, ItemEntry<?> item) {
        tag(cItemTag(category + "/" + material)).add(item.get());
        tag(cItemTag(category)).addTag(cItemTag(category + "/" + material));
    }

    private static TagKey<Item> cItemTag(String path) { return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", path)); }

    private static TagKey<Block> cBlockTag(String path) { return BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", path)); }
}
