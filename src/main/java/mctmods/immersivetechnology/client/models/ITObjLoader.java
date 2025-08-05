package mctmods.immersivetechnology.client.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.obj.ObjLoader;
import net.minecraftforge.client.model.obj.ObjModel;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.util.GsonHelper.getAsBoolean;
import static net.minecraft.util.GsonHelper.getAsString;

public class ITObjLoader implements IGeometryLoader<ITObjModel> {
    public static final ITObjLoader INSTANCE = new ITObjLoader();

    private ITObjLoader() {}

    @Override
    public ITObjModel read(JsonObject modelContents, JsonDeserializationContext deserializationContext) {
        if (!modelContents.has("model")) { throw new JsonParseException("OBJ Loader requires a 'model' key that points to a valid .OBJ model."); }

        String modelLocationStr = modelContents.get("model").getAsString();
        ResourceLocation modelLocation = ResourceLocation.parse(modelLocationStr);

        boolean automaticCulling = getAsBoolean(modelContents, "automatic_culling", true);
        boolean shadeQuads = getAsBoolean(modelContents, "shade_quads", true);
        boolean flipV = getAsBoolean(modelContents, "flip_v", false);
        boolean emissiveAmbient = getAsBoolean(modelContents, "emissive_ambient", true);
        String mtlOverride = getAsString(modelContents, "mtl_override", null);

        Map<String, Boolean> visibility = new HashMap<>();
        if (modelContents.has("visibility")) {
            JsonObject visJson = modelContents.getAsJsonObject("visibility");
            for (Map.Entry<String, JsonElement> entry : visJson.entrySet()) { visibility.put(entry.getKey(), entry.getValue().getAsBoolean()); }
        }

        ObjModel.ModelSettings settings = new ObjModel.ModelSettings(modelLocation, automaticCulling, shadeQuads, flipV, emissiveAmbient, mtlOverride);
        ObjModel inner = ObjLoader.INSTANCE.loadModel(settings);
        return new ITObjModel(inner, visibility);
    }
}
