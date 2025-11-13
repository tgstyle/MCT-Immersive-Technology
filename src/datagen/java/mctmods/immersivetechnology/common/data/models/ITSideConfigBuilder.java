package mctmods.immersivetechnology.common.data.models;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.client.models.helper.ITModelConfigurableSides;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ITSideConfigBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>> ITSideConfigBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) {
        return new ITSideConfigBuilder<>(parent, existingFileHelper);
    }

    protected ITSideConfigBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(ITModelConfigurableSides.Loader.NAME, parent, existingFileHelper);
    }

    private ITModelConfigurableSides.Type type;
    private ResourceLocation baseName;

    public ITSideConfigBuilder<T> type(ITModelConfigurableSides.Type type) {
        Preconditions.checkNotNull(type);
        Preconditions.checkState(this.type == null);
        this.type = type;
        return this;
    }

    public ITSideConfigBuilder<T> baseName(ResourceLocation baseName) {
        Preconditions.checkNotNull(baseName);
        Preconditions.checkState(this.baseName == null);
        this.baseName = baseName;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);
        json.addProperty("type", type.getName());
        json.addProperty("base_name", baseName.toString());
        return json;
    }
}
