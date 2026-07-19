package mctmods.immersivetechnology.common.data.builders;

import com.google.gson.JsonObject;
import mctmods.immersivetechnology.core.lib.Reference;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class MirroredModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>> MirroredModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) { return new MirroredModelBuilder<>(parent, existingFileHelper); }

    private ModelBuilder<?> inner;

    protected MirroredModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(Reference.rl("mirror"), parent, existingFileHelper, false);
    }

    public MirroredModelBuilder<T> inner(ModelBuilder<?> inner) {
        this.inner = inner;
        return this;
    }

    @Override @NotNull public JsonObject toJson(@NotNull JsonObject json) {
        JsonObject result = super.toJson(json);
        if (inner != null) { result.add("inner_model", inner.toJson()); }
        return result;
    }
}
