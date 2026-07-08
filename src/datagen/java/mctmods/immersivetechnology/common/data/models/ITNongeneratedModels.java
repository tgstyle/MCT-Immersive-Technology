package mctmods.immersivetechnology.common.data.models;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import static mctmods.immersivetechnology.core.lib.ITLib.MODID;

public class ITNongeneratedModels extends ModelProvider<ITNongeneratedModels.ITNongeneratedModel> {
    public ITNongeneratedModels(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MODID, "block", ITNongeneratedModels.ITNongeneratedModel::new, existingFileHelper);
    }

    @Override protected void registerModels() { }

    @Override @NotNull public String getName() {
        return "Non-generated models";
    }

    public static class ITNongeneratedModel extends ModelBuilder<ITNongeneratedModels.ITNongeneratedModel> {

        protected ITNongeneratedModel(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper) {
            super(outputLocation, existingFileHelper);
        }
    }
}
