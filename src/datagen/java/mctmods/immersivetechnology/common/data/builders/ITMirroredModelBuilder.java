package mctmods.immersivetechnology.common.data.builders;

import com.google.gson.JsonObject;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class ITMirroredModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>> ITMirroredModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) { return new ITMirroredModelBuilder<>(parent, existingFileHelper); }

    private ModelBuilder<?> inner;

    protected ITMirroredModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(ITLib.rl("mirror"), parent, existingFileHelper, false);
    }

    public ITMirroredModelBuilder<T> inner(ModelBuilder<?> inner) {
        this.inner = inner;
        return this;
    }

    @Override @NotNull public JsonObject toJson(@NotNull JsonObject json) {
        JsonObject result = super.toJson(json);
        if (inner != null) { result.add("inner_model", inner.toJson()); }
        return result;
    }
}
