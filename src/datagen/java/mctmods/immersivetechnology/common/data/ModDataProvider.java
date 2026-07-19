package mctmods.immersivetechnology.common.data;

import mctmods.immersivetechnology.common.data.generators.*;
import mctmods.immersivetechnology.common.data.generators.ComplexItemModel;
import mctmods.immersivetechnology.common.data.generators.DynamicModelProvider;
import mctmods.immersivetechnology.common.data.generators.ItemModel;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;

@EventBusSubscriber(modid = Reference.MODID)
public class ModDataProvider {

    @SubscribeEvent
    public static void generate(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();
        PackOutput out = generator.getPackOutput();
        final var lookup = event.getLookupProvider();

        if (event.includeServer()) {
            ModBlockTags blockTags = new ModBlockTags(out, lookup, helper);
            generator.addProvider(event.includeServer(), blockTags);
            generator.addProvider(event.includeServer(), new ModItemTags(out, lookup, blockTags.contentsGetter(), helper));
            generator.addProvider(event.includeServer(), new ModFluidTags(out, lookup, helper));
            generator.addProvider(event.includeServer(), new Recipes(out, lookup));
            generator.addProvider(event.includeServer(), new LootTableProvider(out, Collections.emptySet(), List.of(new LootTableProvider.SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK)), lookup));
        }

        if (event.includeClient()) {
            ModBlockState blockStateProvider = new ModBlockState(generator, helper);
            generator.addProvider(event.includeClient(), blockStateProvider);
            generator.addProvider(event.includeClient(), new ComplexItemModel(out, helper));
            generator.addProvider(event.includeClient(), new DynamicModelProvider(blockStateProvider, out, helper));
            generator.addProvider(event.includeClient(), new ItemModel(generator, helper));
            generator.addProvider(event.includeClient(), new ModParticle(out));
            generator.addProvider(event.includeClient(), new SoundDefinitions(out, helper));
        }
    }
}
