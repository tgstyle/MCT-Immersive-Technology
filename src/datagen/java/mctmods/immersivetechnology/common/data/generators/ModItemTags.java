package mctmods.immersivetechnology.common.data.generators;

import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.ModItems;
import mctmods.immersivetechnology.core.registration.ModTags;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModItemTags extends ItemTagsProvider {
    public ModItemTags(PackOutput output, CompletableFuture<Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blocks, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blocks, Reference.MODID, existingFileHelper);
    }

    @Override protected void addTags(@NotNull Provider provider) {
        tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", "salts"))).add(ModItems.SALT.get());
        tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", "dusts/salt"))).add(ModItems.SALT.get());
        tag(ModTags.igniters).add(Items.TORCH, Items.FLINT_AND_STEEL);
        tag(ModTags.igniters_consume).add(Items.TORCH);
        tag(ModTags.formationTools).add(ModItems.FORMATION_TOOL.get()).addOptional(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "hammer"));
        tag(ModTags.screwdrivers).addOptional(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "screwdriver"));
        tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "tools/hammers"))).add(ModItems.FORMATION_TOOL.get());
    }
}
