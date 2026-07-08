package mctmods.immersivetechnology.client.models.mirror;

import blusunrize.immersiveengineering.api.ApiUtils;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.client.models.util.ITModelUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.ExtendedBlockModelDeserializer;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ITMirroredModelLoader implements IGeometryLoader<ITMirroredGeometry>
{
    public static final String INNER_MODEL = "inner_model";
    public static final ResourceLocation ID = ITLib.rl("mirror");
    public static final ITMirroredModelLoader INSTANCE = new ITMirroredModelLoader();

    @Override @Nonnull public ITMirroredGeometry read(
            JsonObject modelContents, @NotNull JsonDeserializationContext deserializationContext
    ) throws JsonParseException
    {
        JsonElement innerJson = modelContents.get(INNER_MODEL);
        BlockModel baseModel = ExtendedBlockModelDeserializer.INSTANCE.fromJson(innerJson, BlockModel.class);
        return new ITMirroredGeometry(baseModel);
    }

    public static List<BakedQuad> reversedQuads(List<BakedQuad> quads)
    {
        if(quads.isEmpty()) return ImmutableList.of();
        return quads.stream()
                .map(ITModelUtils::reverseOrder)
                .toList();
    }

    public static List<BakedQuad> getReversedQuads(
            BakedModel model,
            @Nullable BlockState state,
            @Nullable Direction side,
            @Nonnull RandomSource rand,
            @Nonnull ModelData extraData,
            @Nullable RenderType layer
    )
    {
        return reversedQuads(model.getQuads(state, side, rand, extraData, layer));
    }

    public static List<BakedQuad> getReversedQuads(SimpleBakedModel model, @Nullable Direction face)
    {
        return getReversedQuads(model, null, face, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
    }
}
