package mctmods.immersivetechnology.client.event;

import java.util.List;

import mctmods.immersivetechnology.common.multiblocks.ITBlockInterfaces.IAdvancedSelectionBounds;
import mctmods.immersivetechnology.common.util.shapes.BooleanOp;
import mctmods.immersivetechnology.common.util.shapes.Shapes;
import mctmods.immersivetechnology.common.util.shapes.VoxelShape;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
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

                VoxelShape union = Shapes.empty();
                for (AxisAlignedBB aabb : bounds) {
                    if (asb.isOverrideBox(aabb, player, target, bounds)) continue;
                    union = Shapes.joinUnoptimized(union, Shapes.create(aabb), BooleanOp.OR);
                }
                union = union.optimize();

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

                union.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> {
                    minX += pos.getX() - px;
                    minY += pos.getY() - py;
                    minZ += pos.getZ() - pz;
                    maxX += pos.getX() - px;
                    maxY += pos.getY() - py;
                    maxZ += pos.getZ() - pz;
                    buffer.pos(minX, minY, minZ).color(0.0F, 0.0F, 0.0F, 0.4F).endVertex();
                    buffer.pos(maxX, maxY, maxZ).color(0.0F, 0.0F, 0.0F, 0.4F).endVertex();
                });

                tessellator.draw();

                GlStateManager.depthMask(true);
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
            }
        }
    }
}
