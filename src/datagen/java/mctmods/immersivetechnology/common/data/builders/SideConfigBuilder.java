package mctmods.immersivetechnology.common.data.builders;

import mctmods.immersivetechnology.core.lib.Reference;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.immersiveconvergence.api.client.ConfigurableSidesModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class SideConfigBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>> SideConfigBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) {
        return new SideConfigBuilder<>(parent, existingFileHelper);
    }

    protected SideConfigBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(Reference.rl("conf_sides"), parent, existingFileHelper);
    }

    private ConfigurableSidesModel.Type type;
    private ResourceLocation baseName;

    public SideConfigBuilder<T> type(ConfigurableSidesModel.Type type) {
        Preconditions.checkNotNull(type);
        Preconditions.checkState(this.type == null);
        this.type = type;
        return this;
    }

    public SideConfigBuilder<T> baseName(ResourceLocation baseName) {
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
