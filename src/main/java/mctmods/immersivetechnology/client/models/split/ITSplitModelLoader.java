package mctmods.immersivetechnology.client.models.split;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.client.model.ExtendedBlockModelDeserializer;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ITSplitModelLoader implements IGeometryLoader<ITUnbakedSplitModel> {
    public static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "basic_split");
    public static final String PARTS = "split_parts";
    public static final String INNER_MODEL = "inner_model";
    public static final String DYNAMIC = "dynamic";

    @Nonnull
    @Override
    public ITUnbakedSplitModel read(JsonObject modelContents, @Nonnull JsonDeserializationContext deserializationContext) {
        UnbakedModel baseModel;
        JsonElement innerJson = modelContents.get(INNER_MODEL);
        baseModel = ExtendedBlockModelDeserializer.INSTANCE.fromJson(innerJson, BlockModel.class);
        JsonArray partsJson = modelContents.getAsJsonArray(PARTS);
        List<Vec3i> parts = new ArrayList<>(partsJson.size());
        for (JsonElement e : partsJson) { parts.add(fromJson(e.getAsJsonArray())); }
        List<BlockPos> positions = parts.stream().map(BlockPos::new).collect(Collectors.toList());
        Optional<BoundingBox> optBox = BoundingBox.encapsulatingPositions(positions);
        BoundingBox box = optBox.orElseThrow(() -> new IllegalStateException("No positions to encapsulate"));
        Vec3i size = new Vec3i(box.getXSpan(), box.getYSpan(), box.getZSpan());
        return new ITUnbakedSplitModel(baseModel, parts, modelContents.get(DYNAMIC).getAsBoolean(), size);
    }

    private Vec3i fromJson(JsonArray a) { return new Vec3i(a.get(0).getAsInt(), a.get(1).getAsInt(), a.get(2).getAsInt()); }
}
