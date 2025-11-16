package mctmods.immersivetechnology.mixin.accessors.client;

import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraftforge.client.ChunkRenderTypeSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleBakedModel.class)
public interface SimpleModelAccess extends blusunrize.immersiveengineering.mixin.accessors.client.SimpleModelAccess {
    @Accessor(remap = false)
    ChunkRenderTypeSet getBlockRenderTypesFast();

    @Accessor(remap = false)
    List<RenderType> getItemRenderTypesFast();
}
