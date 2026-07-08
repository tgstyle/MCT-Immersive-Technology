package mctmods.immersivetechnology.common.data;

import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.data.models.ITTransformationMap;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import javax.annotation.Nonnull;

public class ITTRSRModelBuilder extends ModelBuilder<ITTRSRModelBuilder> {
    private final ITTransformationMap transforms = new ITTransformationMap();

    public ITTRSRModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper) { super(outputLocation, existingFileHelper); }

    @Override @Nonnull public JsonObject toJson() {
        JsonObject ret = super.toJson();
        JsonObject transformJson = transforms.toJson();
        if (!transformJson.entrySet().isEmpty()) { ret.add("transform", transformJson); }
        return ret;
    }
}
