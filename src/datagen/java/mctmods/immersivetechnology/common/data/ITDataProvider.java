package mctmods.immersivetechnology.common.data;

import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.common.data.generators.*;
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

@Mod.EventBusSubscriber(modid = ITLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ITDataProvider {
    public static Logger log = LogManager.getLogger(ITLib.MODID + "/DataGenerator");

    @SubscribeEvent
    public static void generate(GatherDataEvent event){
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();
        PackOutput out = generator.getPackOutput();
        final var lookup = event.getLookupProvider();

        log.info("-===== Starting Data Generation for Immersive Technoloogy =====-");

        if(event.includeServer()){
            ITBlockStateProvider blockStateProvider = new ITBlockStateProvider(generator, helper);
            generator.addProvider(true, blockStateProvider);
            generator.addProvider(true, new ITItemModelProvider(generator, helper));
            generator.addProvider(true, new ITComplexItemModelProvider(out, helper));
            BlockTagsProvider blockTags = new ITBlockTags(out, lookup, helper);
            generator.addProvider(true, blockTags);
            generator.addProvider(true, new ITFluidTags(out, lookup, helper));
            generator.addProvider(true, new ITItemTags(out, lookup, blockTags.contentsGetter(), helper));
            generator.addProvider(true, new ITDynamicModelProvider(blockStateProvider, out, helper));
            generator.addProvider(true, new ITRecipes(out));
            generator.addProvider(true, new LootTableProvider(out, Collections.emptySet(), List.of(new LootTableProvider.SubProviderEntry(ITBlockLootProvider::new, LootContextParamSets.BLOCK))));
            generator.addProvider(true, new ITParticleProvider(out, helper));
        }
    }
}
