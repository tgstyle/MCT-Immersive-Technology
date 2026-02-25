package mctmods.immersivetechnology.common.data.loaders;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class ITObjModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>> ITObjModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) { return new ITObjModelBuilder<>(parent, existingFileHelper); }

    private ResourceLocation modelLocation;
    private boolean automaticCulling = true;
    private boolean shadeQuads = true;
    private boolean flipV = false;
    private boolean emissiveAmbient = true;
    private String mtlOverride;
    private final Map<String, Boolean> visibility = new HashMap<>();

    public ITObjModelBuilder(T parent, ExistingFileHelper existingFileHelper) { super(ITLib.rl("obj"), parent, existingFileHelper); }

    public ITObjModelBuilder<T> modelLocation(ResourceLocation modelLocation) { this.modelLocation = modelLocation; return this; }
    public ITObjModelBuilder<T> automaticCulling(boolean automaticCulling) { this.automaticCulling = automaticCulling; return this; }
    public ITObjModelBuilder<T> shadeQuads(boolean shadeQuads) { this.shadeQuads = shadeQuads; return this; }
    public ITObjModelBuilder<T> flipV(boolean flipV) { this.flipV = flipV; return this; }
    public ITObjModelBuilder<T> emissiveAmbient(boolean emissiveAmbient) { this.emissiveAmbient = emissiveAmbient; return this; }
    public ITObjModelBuilder<T> mtlOverride(String mtlOverride) { this.mtlOverride = mtlOverride; return this; }
    public ITObjModelBuilder<T> visibility(String part, boolean show) { visibility.put(part, show); return this; }

    @Override public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);
        Preconditions.checkNotNull(modelLocation, "model must be set on obj model");
        json.addProperty("model", modelLocation.toString());
        json.addProperty("automatic_culling", automaticCulling);
        json.addProperty("shade_quads", shadeQuads);
        json.addProperty("flip_v", flipV);
        json.addProperty("emissive_ambient", emissiveAmbient);
        if (mtlOverride != null) { json.addProperty("mtl_override", mtlOverride); }
        if (!visibility.isEmpty()) {
            JsonObject visJson = new JsonObject();
            visibility.forEach(visJson::addProperty);
            json.add("visibility", visJson);
        }
        return json;
    }
}
