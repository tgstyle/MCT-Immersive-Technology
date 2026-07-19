package mctmods.immersivetechnology.common.data.builders;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class ModObjModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>> ModObjModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) {
        return new ModObjModelBuilder<>(parent, existingFileHelper);
    }

    private ResourceLocation modelLocation;
    private boolean automaticCulling = true;
    private boolean shadeQuads = true;
    private boolean flipV = false;
    private boolean emissiveAmbient = true;
    private String mtlOverride;

    public ModObjModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(Reference.rl("obj"), parent, existingFileHelper, false);
    }

    public ModObjModelBuilder<T> modelLocation(ResourceLocation modelLocation) {
        this.modelLocation = modelLocation;
        return this;
    }

    public ModObjModelBuilder<T> automaticCulling(boolean automaticCulling) {
        this.automaticCulling = automaticCulling;
        return this;
    }

    public ModObjModelBuilder<T> shadeQuads(boolean shadeQuads) {
        this.shadeQuads = shadeQuads;
        return this;
    }

    public ModObjModelBuilder<T> flipV(boolean flipV) {
        this.flipV = flipV;
        return this;
    }

    public ModObjModelBuilder<T> emissiveAmbient(boolean emissiveAmbient) {
        this.emissiveAmbient = emissiveAmbient;
        return this;
    }

    public ModObjModelBuilder<T> mtlOverride(String mtlOverride) {
        this.mtlOverride = mtlOverride;
        return this;
    }

    @Override
    @NotNull
    public JsonObject toJson(@NotNull JsonObject json) {
        json = super.toJson(json);
        json.addProperty("loader", Reference.rl("obj").toString());
        Preconditions.checkNotNull(modelLocation, "model must be set on obj model");
        json.addProperty("model", modelLocation.toString());
        json.addProperty("automatic_culling", automaticCulling);
        json.addProperty("shade_quads", shadeQuads);
        json.addProperty("flip_v", flipV);
        json.addProperty("emissive_ambient", emissiveAmbient);
        if (mtlOverride != null) {
            json.addProperty("mtl_override", mtlOverride);
        }
        return json;
    }
}
