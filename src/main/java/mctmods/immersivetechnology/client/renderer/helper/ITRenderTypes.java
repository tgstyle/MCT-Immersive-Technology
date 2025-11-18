package mctmods.immersivetechnology.client.renderer.helper;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.InventoryMenu;

public class ITRenderTypes extends RenderStateShard {
    public static final RenderType TRANSLUCENT_POSITION_COLOR;
    protected static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }, RenderSystem::disableBlend);
    protected static final RenderStateShard.TextureStateShard BLOCK_SHEET_MIPPED = new RenderStateShard.TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, true);
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_POSITION_COLOR = RENDERTYPE_LIGHTNING_SHADER;
    private static RenderType createDefault(RenderType.CompositeState state) { return RenderType.create("immersivetechnology:translucent_pos_color", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, false, state); }

    static {
        RenderType.CompositeState translucentNoTextureState = RenderType.CompositeState.builder().setTransparencyState(TRANSLUCENT_TRANSPARENCY).setTextureState(BLOCK_SHEET_MIPPED).setShaderState(RENDERTYPE_POSITION_COLOR).createCompositeState(false);
        TRANSLUCENT_POSITION_COLOR = createDefault(translucentNoTextureState);
    }

    private ITRenderTypes(String p_110161_, Runnable p_110162_, Runnable p_110163_) { super(p_110161_, p_110162_, p_110163_); }
}
