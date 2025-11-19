package mctmods.immersivetechnology.client.event;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;

@SideOnly(Side.CLIENT)
public class ClientEventHandler {
    private ClientEventHandler() { }

    @SubscribeEvent
    public static void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        RayTraceResult target = event.getTarget();
        if (target.typeOfHit != RayTraceResult.Type.BLOCK) return;

        BlockPos pos = target.getBlockPos();
        TileEntity tile = event.getPlayer().world.getTileEntity(pos);
        if (tile == null) return;

        if (tile instanceof IAdvancedSelectionBounds) {
            IAdvancedSelectionBounds asb = (IAdvancedSelectionBounds) tile;
            List<AxisAlignedBB> bounds = asb.getAdvancedSelectionBounds();
            if (!bounds.isEmpty()) {
                event.setCanceled(true);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.glLineWidth(2.0F);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);

                EntityPlayer player = event.getPlayer();
                double px = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
                double py = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
                double pz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();

                Tessellator tessellator = Tessellator.getInstance();
                net.minecraft.client.renderer.BufferBuilder buffer = tessellator.getBuffer();
                buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);

                float r = 0.0F;
                float g = 0.0F;
                float b = 0.0F;
                float a = 0.5F;

                for (AxisAlignedBB aabb : bounds) {
                    if (asb.isOverrideBox(aabb, player, target, new ArrayList<>(bounds))) continue;
                    aabb = aabb.offset(pos.getX(), pos.getY(), pos.getZ()).grow(0.002).offset(-px, -py, -pz);
                    drawAABBOutline(buffer, aabb, r, g, b, a);
                }

                tessellator.draw();

                GlStateManager.depthMask(true);
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
            }
        }
    }

    private static void drawAABBOutline(net.minecraft.client.renderer.BufferBuilder buffer, AxisAlignedBB aabb, float r, float g, float b, float a) {
        buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex();
        buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex();
        buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex();
        buffer.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex();
        buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex();
        buffer.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex();
        buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex();
        buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex();
        buffer.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex();
        buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex();
    }
}
