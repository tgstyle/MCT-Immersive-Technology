package mctmods.immersivetechnology.common.data.builders;

import mctmods.immersivetechnology.core.lib.Reference;

import com.google.gson.JsonObject;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import javax.annotation.Nonnull;

public class MirroredModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>> MirroredModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) { return new MirroredModelBuilder<>(parent, existingFileHelper); }

    private ModelBuilder<?> inner;

    protected MirroredModelBuilder(T parent, ExistingFileHelper existingFileHelper) { super(Reference.rl("mirror"), parent, existingFileHelper, false); }

    public MirroredModelBuilder<T> inner(ModelBuilder<?> inner) {
        this.inner = inner;
        return this;
    }

    @Override @Nonnull public JsonObject toJson(@Nonnull JsonObject json) {
        JsonObject result = super.toJson(json);
        if (inner != null) { result.add("inner_model", inner.toJson()); }
        return result;
    }
}
