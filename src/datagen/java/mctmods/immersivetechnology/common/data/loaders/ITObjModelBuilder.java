package mctmods.immersivetechnology.common.data.loaders;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.HashMap;
import java.util.Map;

public class ITObjModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>> ITObjModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) { return new ITObjModelBuilder<>(parent, existingFileHelper); }
    private ResourceLocation model;
    private boolean flipV = false;
    private boolean automaticCulling = true;
    private ResourceLocation renderType;
    private final Map<String, Boolean> visibility = new HashMap<>();
    protected ITObjModelBuilder(T parent, ExistingFileHelper existingFileHelper) { super(ResourceLocation.fromNamespaceAndPath("immersivetechnology", "obj"), parent, existingFileHelper); }
    public ITObjModelBuilder<T> model(ResourceLocation model) { this.model = model; return this; }
    public ITObjModelBuilder<T> flipV(boolean flipV) { this.flipV = flipV; return this; }
    public ITObjModelBuilder<T> automaticCulling(boolean automaticCulling) { this.automaticCulling = automaticCulling; return this; }
    public ITObjModelBuilder<T> renderType(ResourceLocation renderType) { this.renderType = renderType; return this; }
    public ITObjModelBuilder<T> renderType(String renderType) { return renderType(ResourceLocation.parse(renderType)); }
    public ITObjModelBuilder<T> visibility(Map<String, Boolean> visibility) { this.visibility.putAll(visibility); return this; }
    @Override public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);
        Preconditions.checkNotNull(model, "model must not be null");
        json.addProperty("model", model.toString());
        if (flipV) { json.addProperty("flip_v", true); }
        if (!automaticCulling) { json.addProperty("automatic_culling", false); }
        if (renderType != null) { json.addProperty("render_type", renderType.toString()); }
        if (!visibility.isEmpty()) {
            JsonObject visJson = new JsonObject();
            visibility.forEach(visJson::addProperty);
            json.add("visibility", visJson);
        }
        return json;
    }
}
