package mctmods.immersivetechnology.client.models.obj;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.obj.ObjLoader;
import net.neoforged.neoforge.client.model.obj.ObjModel;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static net.minecraft.util.GsonHelper.getAsBoolean;
import static net.minecraft.util.GsonHelper.getAsString;

public class ITObjLoader implements IGeometryLoader<ITObjLoader.ITObjModel> {
    public static final ITObjLoader INSTANCE = new ITObjLoader();

    private ITObjLoader() {}

    @Override public @NotNull ITObjModel read(JsonObject modelContents, @NotNull JsonDeserializationContext deserializationContext) {
        if (!modelContents.has("model")) {
            throw new JsonParseException("OBJ Loader requires a 'model' key that points to a valid .OBJ model.");
        }

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
            for (Map.Entry<String, JsonElement> entry : visJson.entrySet()) {
                visibility.put(entry.getKey(), entry.getValue().getAsBoolean());
            }
        }

        ObjModel.ModelSettings settings = new ObjModel.ModelSettings(modelLocation, automaticCulling, shadeQuads, flipV, emissiveAmbient, mtlOverride);
        ObjModel inner = ObjLoader.INSTANCE.loadModel(settings);
        return new ITObjModel(inner, visibility);
    }

    public record ITObjModel(ObjModel inner, Map<String, Boolean> defaultVisibility) implements IUnbakedGeometry<ITObjModel> {
        @Override public @NotNull BakedModel bake(@NotNull IGeometryBakingContext context, @NotNull ModelBaker baker, @NotNull Function<Material, TextureAtlasSprite> spriteGetter, @NotNull ModelState modelState, @NotNull ItemOverrides overrides) {
            return inner.bake(context, baker, spriteGetter, modelState, overrides);
        }
    }
}
