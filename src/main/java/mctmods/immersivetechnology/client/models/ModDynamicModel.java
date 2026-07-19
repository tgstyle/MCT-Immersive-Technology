package mctmods.immersivetechnology.client.models;
import mctmods.immersivetechnology.core.lib.Reference;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Reference.MODID, value = Dist.CLIENT) public class ModDynamicModel {
    private static final List<ModelResourceLocation> MODELS = new ArrayList<>();
    private final ModelResourceLocation name;
    @SubscribeEvent public static void registerModels(ModelEvent.RegisterAdditional ev) { for (ModelResourceLocation model : MODELS) { ev.register(model); } }
    public ModDynamicModel(String desc) { this.name = new ModelResourceLocation(Reference.rl("dynamic/" + desc), "standalone"); MODELS.add(this.name); }
    public BakedModel get() {
        final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        return blockRenderer.getBlockModelShaper().getModelManager().getModel(this.name);
    }
    public ResourceLocation getName() { return this.name.id(); }
}
