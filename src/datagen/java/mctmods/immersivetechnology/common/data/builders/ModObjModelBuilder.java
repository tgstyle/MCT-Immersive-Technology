package mctmods.immersivetechnology.common.data.builders;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class ModObjModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>> ModObjModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) { return new ModObjModelBuilder<>(parent, existingFileHelper); }

    private ResourceLocation modelLocation;
    private boolean automaticCulling = true;
    private boolean shadeQuads = true;
    private boolean flipV = false;
    private boolean emissiveAmbient = true;
    private String mtlOverride;
    private String renderType;
    private final Map<String, Boolean> visibility = new HashMap<>();

    public ModObjModelBuilder(T parent, ExistingFileHelper existingFileHelper) { super(Reference.rl("obj"), parent, existingFileHelper); }

    public ModObjModelBuilder<T> modelLocation(ResourceLocation modelLocation) { this.modelLocation = modelLocation; return this; }
    public ModObjModelBuilder<T> automaticCulling(boolean automaticCulling) { this.automaticCulling = automaticCulling; return this; }
    public ModObjModelBuilder<T> shadeQuads(boolean shadeQuads) { this.shadeQuads = shadeQuads; return this; }
    public ModObjModelBuilder<T> flipV(boolean flipV) { this.flipV = flipV; return this; }
    public ModObjModelBuilder<T> emissiveAmbient(boolean emissiveAmbient) { this.emissiveAmbient = emissiveAmbient; return this; }
    public ModObjModelBuilder<T> mtlOverride(String mtlOverride) { this.mtlOverride = mtlOverride; return this; }
    public ModObjModelBuilder<T> visibility(String part, boolean show) { visibility.put(part, show); return this; }
    @SuppressWarnings("UnusedReturnValue")
    public ModObjModelBuilder<T> renderType(String renderType) { this.renderType = renderType; return this; }

    @Override public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);
        Preconditions.checkNotNull(modelLocation, "model must be set on obj model");
        json.addProperty("model", modelLocation.toString());
        json.addProperty("automatic_culling", automaticCulling);
        json.addProperty("shade_quads", shadeQuads);
        json.addProperty("flip_v", flipV);
        json.addProperty("emissive_ambient", emissiveAmbient);
        if (mtlOverride != null) { json.addProperty("mtl_override", mtlOverride); }
        if (renderType != null) { json.addProperty("render_type", renderType); }
        if (!visibility.isEmpty()) {
            JsonObject visJson = new JsonObject();
            visibility.forEach(visJson::addProperty);
            json.add("visibility", visJson);
        }
        return json;
    }
}
