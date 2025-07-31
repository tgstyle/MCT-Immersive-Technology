package mctmods.immersivetechnology.common.data;

import blusunrize.immersiveengineering.data.models.TransformationMap;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import javax.annotation.Nonnull;

public class TRSRModelBuilder extends ModelBuilder<TRSRModelBuilder> {
    private final TransformationMap transforms = new TransformationMap();

    public TRSRModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper) { super(outputLocation, existingFileHelper); }

    @Nonnull
    @Override
    public JsonObject toJson() {
        JsonObject ret = super.toJson();
        JsonObject transformJson = transforms.toJson();
        if (!transformJson.entrySet().isEmpty()) { ret.add("transform", transformJson); }
        return ret;
    }
}
