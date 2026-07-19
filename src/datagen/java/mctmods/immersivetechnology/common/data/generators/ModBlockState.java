package mctmods.immersivetechnology.common.data.generators;

import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.data.generators.blockstate.BasicStateGenerator;
import mctmods.immersivetechnology.common.data.generators.blockstate.MultiblockStateGenerator;
import mctmods.immersivetechnology.core.lib.Reference;

import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.IGeneratedBlockState;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ModBlockState extends BlockStateProvider {
    public final Map<Block, ModelFile> unsplitModels = new HashMap<>();
    public static final Map<ResourceLocation, String> generatedParticleTextures = new HashMap<>();

    private final MultiblockStateGenerator multiblockGen;
    private final BasicStateGenerator basicGen;

    private final PackOutput packOutput;
    private final ClearableBlockModelProvider blockModels;
    private final ClearableItemModelProvider itemModels;

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();

    private static class ClearableBlockModelProvider extends BlockModelProvider {
        public ClearableBlockModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
            super(output, modid, existingFileHelper);
        }
        @Override protected void registerModels() {}
        public void clearModels() { clear(); }
        public CompletableFuture<?> genAll(CachedOutput cache) { return generateAll(cache); }
    }

    private static class ClearableItemModelProvider extends ItemModelProvider {
        public ClearableItemModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
            super(output, modid, existingFileHelper);
        }
        @Override protected void registerModels() {}
        public void clearModels() { clear(); }
        public CompletableFuture<?> genAll(CachedOutput cache) { return generateAll(cache); }
    }

    public ModBlockState(DataGenerator generator, ExistingFileHelper helper) {
        super(generator.getPackOutput(), Reference.MODID, helper);
        this.packOutput = generator.getPackOutput();
        this.blockModels = new ClearableBlockModelProvider(generator.getPackOutput(), Reference.MODID, helper);
        this.itemModels = new ClearableItemModelProvider(generator.getPackOutput(), Reference.MODID, helper);
        this.multiblockGen = new MultiblockStateGenerator(this, helper);
        this.basicGen = new BasicStateGenerator(this, helper);
    }

    public PackOutput getPackOutput() { return packOutput; }

    public @NotNull ResourceLocation modLoc(@NotNull String name) { return super.modLoc(name); }

    public @NotNull ResourceLocation mcLoc(@NotNull String name) { return super.mcLoc(name); }

    @Override public @NotNull BlockModelProvider models() { return blockModels; }

    @Override public @NotNull ItemModelProvider itemModels() { return itemModels; }

    @Override @NotNull public CompletableFuture<?> run(@NotNull CachedOutput cache) {
        blockModels.clearModels();
        itemModels.clearModels();
        registeredBlocks.clear();
        registerStatesAndModels();
        CompletableFuture<?>[] futures = new CompletableFuture[registeredBlocks.size() + 2];
        int i = 0;
        futures[i++] = blockModels.genAll(cache);
        futures[i++] = itemModels.genAll(cache);
        for (Map.Entry<Block, IGeneratedBlockState> entry : registeredBlocks.entrySet()) {
            futures[i++] = saveBlockState(entry.getKey(), entry.getValue().toJson(), cache);
        }
        return CompletableFuture.allOf(futures);
    }

    private CompletableFuture<?> saveBlockState(Block owner, JsonObject stateJson, CachedOutput cache) {
        return CompletableFuture.runAsync(() -> {
            ResourceLocation blockName = Preconditions.checkNotNull(ForgeRegistries.BLOCKS.getKey(owner));
            ResourceLocation outputLocation = ResourceLocation.fromNamespaceAndPath(blockName.getNamespace(), "blockstates/" + blockName.getPath());
            Path path = packOutput.getOutputFolder().resolve("assets/" + outputLocation.getNamespace() + "/" + outputLocation.getPath() + ".json");
            try {
                String jsonStr = GSON.toJson(stateJson);
                byte[] bytes = jsonStr.getBytes(StandardCharsets.UTF_8);
                com.google.common.hash.HashCode hash = Hashing.sha256().hashBytes(bytes);
                cache.writeIfNeeded(path, bytes, hash);
            } catch (IOException e) { LOGGER.error("Couldn't save blockstate to {}", path, e); }
        }, Util.backgroundExecutor());
    }

    @Override protected void registerStatesAndModels() {
        multiblockGen.generate();
        basicGen.generate();
    }
}
