package mctmods.immersivetechnology.common.data.models;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import static mctmods.immersivetechnology.core.lib.Reference.MODID;

public class NongeneratedModels extends ModelProvider<NongeneratedModels.ITNongeneratedModel> {
    public NongeneratedModels(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MODID, "block", NongeneratedModels.ITNongeneratedModel::new, existingFileHelper);
    }

    @Override protected void registerModels() { }

    @Override @NotNull public String getName() {
        return "Non-generated models";
    }

    public static class ITNongeneratedModel extends ModelBuilder<NongeneratedModels.ITNongeneratedModel> {

        protected ITNongeneratedModel(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper) {
            super(outputLocation, existingFileHelper);
        }
    }
}
