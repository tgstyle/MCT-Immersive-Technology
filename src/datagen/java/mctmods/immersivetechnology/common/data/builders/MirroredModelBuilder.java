package mctmods.immersivetechnology.common.data.builders;

import mctmods.immersivetechnology.core.lib.Reference;
import com.immersiveconvergence.api.client.mirror.MirrorModelLoader;

import com.google.gson.JsonObject;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class MirroredModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>> MirroredModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) { return new MirroredModelBuilder<>(parent, existingFileHelper); }

    private ModelBuilder<?> inner;

    protected MirroredModelBuilder(T parent, ExistingFileHelper existingFileHelper) { super(Reference.rl("mirror"), parent, existingFileHelper); }

    public MirroredModelBuilder<T> inner(ModelBuilder<?> inner) {
        this.inner = inner;
        return this;
    }

    @Override public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);
        if (inner != null) { json.add(MirrorModelLoader.INNER_MODEL, inner.toJson()); }
        return json;
    }
}
