package mctmods.immersivetechnology.common.data.models;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.client.models.ITModelConfigurableSides;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class ITSideConfig<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {

    private ITModelConfigurableSides.Type type;
    private ResourceLocation baseName;

    protected ITSideConfig(T parent, ExistingFileHelper existingFileHelper) {
        super(ITModelConfigurableSides.Loader.NAME, parent, existingFileHelper, false);
    }

    public static <T extends ModelBuilder<T>> ITSideConfig<T> begin(@NotNull T parent, @NotNull ExistingFileHelper existingFileHelper) {
        return new ITSideConfig<>(parent, existingFileHelper);
    }

    public ITSideConfig<T> type(ITModelConfigurableSides.Type type) {
        Preconditions.checkNotNull(type);
        Preconditions.checkState(this.type == null);
        this.type = type;
        return this;
    }

    public ITSideConfig<T> baseName(ResourceLocation baseName) {
        Preconditions.checkNotNull(baseName);
        Preconditions.checkState(this.baseName == null);
        this.baseName = baseName;
        return this;
    }

    @Override @NotNull public JsonObject toJson(@NotNull JsonObject json) {
        json = super.toJson(json);
        if (type != null) { json.addProperty("type", type.getName()); }
        if (baseName != null) { json.addProperty("base_name", baseName.toString()); }
        return json;
    }
}
