package mctmods.immersivetechnology.client.render.multiblock;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class TileRenderITMultiblockStatic<T extends TileEntityMultiblockPart<?>> extends TileEntitySpecialRenderer<T> {
    private static final double MAX_RENDER_DISTANCE_SQ = 256 * 256;
    private static final long REBUILD_INTERVAL_TICKS = 200;
    private static final Map<TileEntityMultiblockPart<?>, CachedList> CACHE = new HashMap<>();

    private static class CachedList {
        int listId;
        long builtTime;
        int lightSample;
    }

    protected abstract int getTotalBlocks();

    protected abstract Block getMultiblockBlock();

    protected void renderDynamic(T te, float partialTicks) {}

    @Override public void render(T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!te.formed || te.isInvalid()) {
            free(te);
            return;
        }
        if (!te.getWorld().isBlockLoaded(te.getPos(), false)) { return; }
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (te.getPos().distanceSq(player.posX, player.posY, player.posZ) > MAX_RENDER_DISTANCE_SQ) { return; }

        ClientUtils.bindAtlas();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableCull();
        if (Minecraft.isAmbientOcclusionEnabled()) { GlStateManager.shadeModel(GL11.GL_SMOOTH); }
        else { GlStateManager.shadeModel(GL11.GL_FLAT); }

        GlStateManager.callList(getOrBuildList(te));
        renderDynamic(te, partialTicks);

        GlStateManager.enableCull();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private int getOrBuildList(T te) {
        CachedList cached = CACHE.get(te);
        long now = te.getWorld().getTotalWorldTime();
        int light = te.getWorld().getCombinedLight(te.getPos().up(), 0);
        if (cached != null && light == cached.lightSample && now - cached.builtTime < REBUILD_INTERVAL_TICKS) { return cached.listId; }
        if (cached == null) {
            sweepInvalid();
            cached = new CachedList();
            cached.listId = GLAllocation.generateDisplayLists(1);
            CACHE.put(te, cached);
        }
        cached.builtTime = now;
        cached.lightSample = light;
        buildList(te, cached.listId);
        return cached.listId;
    }

    private void buildList(T te, int listId) {
        final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.glNewList(listId, GL11.GL_COMPILE);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        BlockPos masterPos = te.getPos();
        buffer.setTranslation(-masterPos.getX(), -masterPos.getY(), -masterPos.getZ());
        int totalBlocks = getTotalBlocks();
        for (int i = 0; i < totalBlocks; i++) {
            BlockPos pos = te.getBlockPosForPos(i);
            IBlockState state = te.getWorld().getBlockState(pos);
            if (state.getBlock() != getMultiblockBlock()) { continue; }
            try {
                state = state.getActualState(te.getWorld(), pos);
            } catch (IllegalArgumentException ignored) {}
            IBakedModel model = blockRenderer.getModelForState(state);
            blockRenderer.getBlockModelRenderer().renderModel(te.getWorld(), model, state, pos, buffer, false, MathHelper.getCoordinateRandom(pos.getX(), pos.getY(), pos.getZ()));
        }
        buffer.setTranslation(0, 0, 0);
        tessellator.draw();
        GlStateManager.glEndList();
    }

    private static void free(TileEntityMultiblockPart<?> te) {
        CachedList cached = CACHE.remove(te);
        if (cached != null) { GLAllocation.deleteDisplayLists(cached.listId); }
    }

    private static void sweepInvalid() {
        Iterator<Map.Entry<TileEntityMultiblockPart<?>, CachedList>> it = CACHE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<TileEntityMultiblockPart<?>, CachedList> entry = it.next();
            if (entry.getKey().isInvalid() || !entry.getKey().formed) {
                GLAllocation.deleteDisplayLists(entry.getValue().listId);
                it.remove();
            }
        }
    }

    public static void clearAll() {
        for (CachedList cached : CACHE.values()) { GLAllocation.deleteDisplayLists(cached.listId); }
        CACHE.clear();
    }

    @Override public boolean isGlobalRenderer(@Nonnull T te) { return true; }
}
