package mctmods.immersivetechnology.common.data.generators;

import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.ModBlocks;
import mctmods.immersivetechnology.core.registration.ModBlocks.BlockEntry;
import mctmods.immersivetechnology.core.registration.ModItems;
import mctmods.immersivetechnology.core.registration.ModTags;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

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
        tag(ModTags.screwdrivers).addOptional(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "screwdriver")).addOptional(ResourceLocation.fromNamespaceAndPath("immersivefabrication", "screwdriver"));
        ingredientTag("sheetmetal/iron", "sheetmetal_iron", "iron_sheetmetal");
        ingredientTag("slab/sheetmetal_iron", "slab_sheetmetal_iron", "iron_sheetmetal_slab");
        ingredientTag("sheetmetal/steel", "sheetmetal_steel", "steel_sheetmetal");
        ingredientTag("slab/sheetmetal_steel", "slab_sheetmetal_steel", "steel_sheetmetal_slab");
        ingredientTag("cokebrick", "cokebrick", "coke_brick");
        ingredientTag("fluid_pump", "fluid_pump", "fluid_pump");
        ingredientTag("coil_hv", "coil_hv", "coil_hv");
        ingredientTag("furnace_heater", "furnace_heater", "furnace_heater");
        ingredientTag("component_steel", "component_steel", "component_steel");
        tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "tools/hammers"))).add(ModItems.FORMATION_TOOL.get());
        ModBlocks.NEEDS_IE.forEach(entry -> tag(drops(entry)).addOptional(entry.getId()));
    }

    static TagKey<Item> drops(BlockEntry<?> entry) { return ItemTags.create(Reference.rl("drops/" + entry.getId().getPath())); }

    static TagKey<Item> itTag(String path) { return ItemTags.create(Reference.rl(path)); }

    private void ingredientTag(String path, String ieId, String ifabId) {
        tag(itTag(path)).addOptional(ResourceLocation.fromNamespaceAndPath("immersiveengineering", ieId)).addOptional(ResourceLocation.fromNamespaceAndPath("immersivefabrication", ifabId));
    }
}
