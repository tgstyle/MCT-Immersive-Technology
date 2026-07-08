package mctmods.immersivetechnology.common.data.builders;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ITSplitModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static final ResourceLocation LOCATION = ITLib.rl("split");

    public static <T extends ModelBuilder<T>> ITSplitModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) {
        return new ITSplitModelBuilder<>(parent, existingFileHelper);
    }

    private List<Vec3i> parts;
    private ModelBuilder<?> modelToSplit;
    private boolean isDynamic;

    protected ITSplitModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(LOCATION, parent, existingFileHelper, false);
    }

    public ITSplitModelBuilder<T> parts(List<Vec3i> parts) {
        Preconditions.checkNotNull(parts);
        Preconditions.checkState(this.parts == null);
        this.parts = parts;
        return this;
    }

    public ITSplitModelBuilder<T> innerModel(ModelBuilder<?> modelToSplit) {
        Preconditions.checkNotNull(modelToSplit);
        Preconditions.checkState(this.modelToSplit == null);
        this.modelToSplit = modelToSplit;
        return this;
    }

    public ITSplitModelBuilder<T> dynamic(boolean isDynamic) {
        this.isDynamic = isDynamic;
        return this;
    }

    @Override
    @NotNull
    public JsonObject toJson(@NotNull JsonObject json) {
        json = super.toJson(json);
        json.addProperty("dynamic", isDynamic);
        json.add("inner_model", modelToSplit.toJson());

        JsonArray partsJson = new JsonArray();
        for (Vec3i part : parts) {
            JsonArray posArray = new JsonArray();
            posArray.add(part.getX());
            posArray.add(part.getY());
            posArray.add(part.getZ());
            partsJson.add(posArray);
        }
        json.add("split_parts", partsJson);
        return json;
    }
}
