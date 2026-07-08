package mctmods.immersivetechnology.client.renderer.helper;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import java.util.function.Function;

public class ITRenderTypes extends RenderStateShard {
    public static final RenderType TRANSLUCENT_POSITION_COLOR;
    public static final RenderType FLUID_SOLID_NO_CULL;
    protected static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }, RenderSystem::disableBlend);
    protected static final RenderStateShard.TextureStateShard BLOCK_SHEET_MIPPED = new RenderStateShard.TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, true);
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_POSITION_COLOR = new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeLightningShader);
    protected static final RenderStateShard.ShaderStateShard POSITION_COLOR_TEX_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getPositionTexColorShader);
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_SOLID_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeSolidShader);
    private static final Function<ResourceLocation, RenderType> GUI_CUTOUT = Util.memoize(texture -> {
        RenderType.CompositeState.CompositeStateBuilder builder = makeGuiState(texture);
        return createDefault("gui_" + texture, DefaultVertexFormat.POSITION_TEX_COLOR, builder.createCompositeState(false));
    });
    private static final Function<ResourceLocation, RenderType> GUI_TRANSLUCENT = Util.memoize(texture -> {
        RenderType.CompositeState.CompositeStateBuilder builder = makeGuiState(texture).setTransparencyState(TRANSLUCENT_TRANSPARENCY);
        return createDefault("gui_translucent_" + texture, DefaultVertexFormat.POSITION_TEX_COLOR, builder.createCompositeState(false));
    });

    static {
        RenderType.CompositeState translucentNoTextureState = RenderType.CompositeState.builder()
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setTextureState(BLOCK_SHEET_MIPPED)
                .setShaderState(RENDERTYPE_POSITION_COLOR)
                .createCompositeState(false);
        TRANSLUCENT_POSITION_COLOR = createDefault("immersivetechnology:translucent_pos_color", DefaultVertexFormat.POSITION_COLOR, translucentNoTextureState);
        RenderType.CompositeState fluidSolidNoCullState = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_SOLID_SHADER)
                .setTextureState(BLOCK_SHEET_MIPPED)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setCullState(NO_CULL)
                .createCompositeState(true);
        FLUID_SOLID_NO_CULL = RenderType.create("immersivetechnology:fluid_solid_no_cull", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, true, false, fluidSolidNoCullState);
    }

    private ITRenderTypes(String name, Runnable setup, Runnable destroy) { super(name, setup, destroy); }

    private static RenderType createDefault(String name, VertexFormat format, RenderType.CompositeState state) { return RenderType.create(name, format, VertexFormat.Mode.QUADS, 256, false, false, state); }

    private static RenderType.CompositeState.CompositeStateBuilder makeGuiState(ResourceLocation texture) {
        return RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setShaderState(POSITION_COLOR_TEX_SHADER)
                .setDepthTestState(NO_DEPTH_TEST)
                .setWriteMaskState(COLOR_WRITE)
                .setLayeringState(VIEW_OFFSET_Z_LAYERING);
    }

    public static RenderType getGui(ResourceLocation texture) { return GUI_CUTOUT.apply(texture); }

    public static RenderType getGuiTranslucent(ResourceLocation texture) { return GUI_TRANSLUCENT.apply(texture); }
}
