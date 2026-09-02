package mctmods.immersivetechnology.common.data.builders;

import mctmods.immersivetechnology.core.lib.Reference;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.immersiveconvergence.api.client.ConfigurableSidesModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

public class SideConfigBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {

    private ConfigurableSidesModel.Type type;
    private ResourceLocation baseName;

    protected SideConfigBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(Reference.rl("conf_sides"), parent, existingFileHelper, false);
    }

    public static <T extends ModelBuilder<T>> SideConfigBuilder<T> begin(@Nonnull T parent, @Nonnull ExistingFileHelper existingFileHelper) {
        return new SideConfigBuilder<>(parent, existingFileHelper);
    }

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

    @Override @Nonnull public JsonObject toJson(@Nonnull JsonObject json) {
        json = super.toJson(json);
        if (type != null) { json.addProperty("type", type.getName()); }
        if (baseName != null) { json.addProperty("base_name", baseName.toString()); }
        return json;
    }
}
