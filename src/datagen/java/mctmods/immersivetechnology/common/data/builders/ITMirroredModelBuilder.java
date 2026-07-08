package mctmods.immersivetechnology.common.data.builders;

import mctmods.immersivetechnology.client.models.mirror.ITMirroredModelLoader;

import com.google.gson.JsonObject;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ITMirroredModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>> ITMirroredModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) { return new ITMirroredModelBuilder<>(parent, existingFileHelper); }

    private ModelBuilder<?> inner;

    protected ITMirroredModelBuilder(T parent, ExistingFileHelper existingFileHelper) { super(ITMirroredModelLoader.ID, parent, existingFileHelper); }

    public ITMirroredModelBuilder<T> inner(ModelBuilder<?> inner) {
        this.inner = inner;
        return this;
    }

    @Override public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);
        if (inner != null) { json.add(ITMirroredModelLoader.INNER_MODEL, inner.toJson()); }
        return json;
    }
}
