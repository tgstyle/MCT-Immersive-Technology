package mctmods.immersivetechnology.client.event;

import com.immersiveconvergence.api.shapes.BooleanOp;
import com.immersiveconvergence.api.shapes.Shapes;
import com.immersiveconvergence.api.shapes.VoxelShape;

import java.util.List;

import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.ISelectionBounds;

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
    private static BlockPos cachedPos;
    private static long cachedMask;
    private static int cachedBoundsHash;
    private static VoxelShape cachedUnion;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        RayTraceResult target = event.getTarget();
        if (target.typeOfHit != RayTraceResult.Type.BLOCK) return;

        BlockPos pos = target.getBlockPos();
        TileEntity tile = event.getPlayer().world.getTileEntity(pos);
        if (tile == null) return;

        if (tile instanceof ISelectionBounds) {
            ISelectionBounds asb = (ISelectionBounds) tile;
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

                VoxelShape union = getSelectionShape(asb, bounds, pos, player, target);

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

    private static VoxelShape getSelectionShape(ISelectionBounds asb, List<AxisAlignedBB> bounds, BlockPos pos, EntityPlayer player, RayTraceResult target) {
        long mask = 0;
        int boundsHash = 1;
        for (int i = 0; i < bounds.size(); i++) {
            AxisAlignedBB aabb = bounds.get(i);
            boundsHash = 31 * boundsHash + aabb.hashCode();
            if (i < 64 && asb.isOverrideBox(aabb, player, target, bounds)) { mask |= 1L << i; }
        }
        if (cachedUnion != null && pos.equals(cachedPos) && mask == cachedMask && boundsHash == cachedBoundsHash) { return cachedUnion; }

        VoxelShape union = Shapes.empty();
        for (int i = 0; i < bounds.size(); i++) {
            boolean included = i < 64 ? (mask & (1L << i)) != 0 : asb.isOverrideBox(bounds.get(i), player, target, bounds);
            if (included) { union = Shapes.joinUnoptimized(union, Shapes.create(bounds.get(i)), BooleanOp.OR); }
        }
        union = union.optimize();

        cachedPos = pos.toImmutable();
        cachedMask = mask;
        cachedBoundsHash = boundsHash;
        cachedUnion = union;
        return union;
    }
}
