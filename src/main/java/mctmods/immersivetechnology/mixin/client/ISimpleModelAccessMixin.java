package mctmods.immersivetechnology.mixin.client;

import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@SuppressWarnings("unused")
@Mixin(SimpleBakedModel.class)
public interface ISimpleModelAccessMixin extends blusunrize.immersiveengineering.mixin.accessors.client.SimpleModelAccess {
    @Accessor(value = "blockRenderTypes", remap = false)
    ChunkRenderTypeSet getBlockRenderTypesFast();

    @Accessor(value = "itemRenderTypes", remap = false)
    List<RenderType> getItemRenderTypesFast();
}
