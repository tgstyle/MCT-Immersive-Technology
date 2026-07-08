package mctmods.immersivetechnology.common.data;

import mctmods.immersivetechnology.common.data.generators.*;
import mctmods.immersivetechnology.common.data.generators.ITComplexItemModel;
import mctmods.immersivetechnology.common.data.generators.ITDynamicModel;
import mctmods.immersivetechnology.common.data.generators.ITItemModel;
import mctmods.immersivetechnology.core.lib.ITLib;
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

@EventBusSubscriber(modid = ITLib.MODID)
public class ITDataProvider {

    @SubscribeEvent
    public static void generate(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();
        PackOutput out = generator.getPackOutput();
        final var lookup = event.getLookupProvider();

        if (event.includeServer()) {
            ITBlockTags blockTags = new ITBlockTags(out, lookup, helper);
            generator.addProvider(event.includeServer(), blockTags);
            generator.addProvider(event.includeServer(), new ITItemTags(out, lookup, blockTags.contentsGetter(), helper));
            generator.addProvider(event.includeServer(), new ITFluidTags(out, lookup, helper));
            generator.addProvider(event.includeServer(), new ITRecipes(out, lookup));
            generator.addProvider(event.includeServer(), new LootTableProvider(out, Collections.emptySet(), List.of(new LootTableProvider.SubProviderEntry(ITBlockLoot::new, LootContextParamSets.BLOCK)), lookup));
        }

        if (event.includeClient()) {
            ITBlockState blockStateProvider = new ITBlockState(generator, helper);
            generator.addProvider(event.includeClient(), blockStateProvider);
            generator.addProvider(event.includeClient(), new ITComplexItemModel(out, helper));
            generator.addProvider(event.includeClient(), new ITDynamicModel(blockStateProvider, out, helper));
            generator.addProvider(event.includeClient(), new ITItemModel(generator, helper));
            generator.addProvider(event.includeClient(), new ITParticle(out));
            generator.addProvider(event.includeClient(), new ITSound(out, helper));
        }
    }
}
