package mctmods.immersivetechnology.common.data.generators;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.core.registration.ITParticles;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ITParticleProvider implements DataProvider {
    private PackOutput.PathProvider particlesPath;
    private Map<ResourceLocation, List<String>> descriptions = new HashMap<>();

    public ITParticleProvider(PackOutput output, ExistingFileHelper helper) {
        this.particlesPath = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "particles");
    }

    protected void addDescriptions() {
        List<String> bigSmokeTextures = IntStream.range(0, 12)
                .mapToObj(i -> "minecraft:big_smoke_" + i)
                .collect(Collectors.toList());
        descriptions.put(ITParticles.COLORED_SMOKE.getId(), bigSmokeTextures);
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        addDescriptions();

        return CompletableFuture.allOf(descriptions.entrySet().stream().map(entry -> {
            JsonArray textures = new JsonArray();
            entry.getValue().forEach(textures::add);
            JsonObject json = new JsonObject();
            json.add("textures", textures);
            Path path = particlesPath.json(entry.getKey());
            return DataProvider.saveStable(cache, json, path);
        }).toArray(CompletableFuture[]::new));
    }

    @Override
    public @NotNull String getName() {
        return "Particle Descriptions";
    }
}