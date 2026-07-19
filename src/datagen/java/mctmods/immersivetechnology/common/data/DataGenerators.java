package mctmods.immersivetechnology.common.data;

import mctmods.immersivetechnology.common.data.generators.*;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(modid = Reference.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    public static Logger log = LogManager.getLogger(Reference.MODID + "/DataGenerator");

    @SubscribeEvent public static void generate(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();
        PackOutput out = generator.getPackOutput();
        final var lookup = event.getLookupProvider();
        log.info("-===== Starting Data Generation for Immersive Technology =====-");
        if (event.includeServer()) {
            ModBlockState blockStateProvider = new ModBlockState(generator, helper);
            BlockTagsProvider blockTags = new ModBlockTags(out, lookup, helper);
            generator.addProvider(event.includeClient(), new SoundDefinitions(out, helper));
            generator.addProvider(true, blockStateProvider);
            generator.addProvider(true, blockTags);
            generator.addProvider(true, new ComplexItemModel(out, helper));
            generator.addProvider(true, new DynamicModelProvider(blockStateProvider, out, helper));
            generator.addProvider(true, new ModFluidTags(out, lookup, helper));
            generator.addProvider(true, new ItemModel(generator, helper));
            generator.addProvider(true, new ModItemTags(out, lookup, blockTags.contentsGetter(), helper));
            generator.addProvider(true, new ModParticle(out));
            generator.addProvider(true, new Recipes(out));
            generator.addProvider(true, new LootTableProvider(out, Collections.emptySet(), List.of(new LootTableProvider.SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK))));
        }
    }
}
