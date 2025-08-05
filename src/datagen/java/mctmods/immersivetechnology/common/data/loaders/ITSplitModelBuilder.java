package mctmods.immersivetechnology.common.data.loaders;

import blusunrize.immersiveengineering.client.models.split.SplitModelLoader;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.Vec3i;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.List;

public class ITSplitModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>> ITSplitModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) { return new ITSplitModelBuilder<>(parent, existingFileHelper); }
    private List<Vec3i> parts;
    private ModelBuilder<?> modelToSplit;
    private boolean isDynamic;
    protected ITSplitModelBuilder(T parent, ExistingFileHelper existingFileHelper) { super(SplitModelLoader.LOCATION, parent, existingFileHelper); }
    public ITSplitModelBuilder<T> parts(List<Vec3i> parts) { Preconditions.checkNotNull(parts); Preconditions.checkState(this.parts==null); this.parts = parts; return this; }
    public ITSplitModelBuilder<T> innerModel(ModelBuilder<?> modelToSplit) { Preconditions.checkNotNull(modelToSplit); Preconditions.checkState(this.modelToSplit==null); this.modelToSplit = modelToSplit; return this; }
    public ITSplitModelBuilder<T> dynamic(boolean isDynamic) { this.isDynamic = isDynamic; return this; }
    @Override public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);
        json.addProperty(SplitModelLoader.DYNAMIC, isDynamic);
        json.add(SplitModelLoader.INNER_MODEL, modelToSplit.toJson());
        JsonArray partsJson = new JsonArray();
        for(Vec3i part : parts) {
            JsonArray posArray = new JsonArray();
            posArray.add(part.getX());
            posArray.add(part.getY());
            posArray.add(part.getZ());
            partsJson.add(posArray);
        }
        json.add(SplitModelLoader.PARTS, partsJson);
        return json;
    }
}
