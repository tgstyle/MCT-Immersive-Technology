package mctmods.immersivetechnology.client.models.split;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ITSplitModelLoader implements IGeometryLoader<ITUnbakedSplitModel> {
    public static final ITSplitModelLoader INSTANCE = new ITSplitModelLoader();

    public static final ResourceLocation LOCATION = ITLib.rl("basic_split");
    public static final String PARTS = "split_parts";
    public static final String INNER_MODEL = "inner_model";
    public static final String DYNAMIC = "dynamic";

    @Override @Nonnull public ITUnbakedSplitModel read(JsonObject modelContents, @Nonnull JsonDeserializationContext deserializationContext) throws JsonParseException {
        JsonElement innerJson = modelContents.get(INNER_MODEL);
        UnbakedModel baseModel = deserializationContext.deserialize(innerJson, BlockModel.class);

        JsonArray partsJson = modelContents.getAsJsonArray(PARTS);
        List<Vec3i> parts = new ArrayList<>(partsJson.size());
        for (JsonElement e : partsJson) { parts.add(fromJson(e.getAsJsonArray())); }

        List<BlockPos> positions = parts.stream().map(BlockPos::new).collect(Collectors.toList());
        BoundingBox box = BoundingBox.encapsulatingPositions(positions).orElseThrow(() -> new IllegalStateException("No positions to encapsulate"));
        Vec3i size = new Vec3i(box.getXSpan(), box.getYSpan(), box.getZSpan());

        return new ITUnbakedSplitModel(baseModel, parts, modelContents.get(DYNAMIC).getAsBoolean(), size);
    }

    private static Vec3i fromJson(JsonArray a) { return new Vec3i(a.get(0).getAsInt(), a.get(1).getAsInt(), a.get(2).getAsInt()); }
}
