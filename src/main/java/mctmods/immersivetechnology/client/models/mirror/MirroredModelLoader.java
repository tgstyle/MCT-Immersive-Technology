package mctmods.immersivetechnology.client.models.mirror;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.client.models.util.ModelUtils;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.model.ExtendedBlockModelDeserializer;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MirroredModelLoader implements IGeometryLoader<MirroredGeometry> {
    public static final MirroredModelLoader INSTANCE = new MirroredModelLoader();

    public static final String INNER_MODEL = "inner_model";
    public static final ResourceLocation ID = Reference.rl("mirror");

    @Override @Nonnull public MirroredGeometry read(JsonObject modelContents, @NotNull JsonDeserializationContext deserializationContext) throws JsonParseException {
        JsonElement innerJson = modelContents.get(INNER_MODEL);
        BlockModel baseModel = ExtendedBlockModelDeserializer.INSTANCE.fromJson(innerJson, BlockModel.class);
        return new MirroredGeometry(baseModel);
    }

    public static List<BakedQuad> reversedQuads(List<BakedQuad> quads) {
        if (quads.isEmpty()) return ImmutableList.of();
        BakedQuad[] arr = new BakedQuad[quads.size()];
        for (int i = 0; i < quads.size(); i++) { arr[i] = ModelUtils.reverseOrder(quads.get(i)); }
        return List.of(arr);
    }

    public static List<BakedQuad> getReversedQuads(SimpleBakedModel model, @Nullable Direction face) {
        return reversedQuads(model.getQuads(null, face, RandomSource.create(), ModelData.EMPTY, null));
    }
}
