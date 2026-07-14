package mctmods.immersivetechnology.client.renderer;

import mctmods.immersivetechnology.client.renderer.helper.ITBaseBlockEntityRenderer;
import mctmods.immersivetechnology.client.models.util.ITFluidRender;
import mctmods.immersivetechnology.client.renderer.helper.ITRenderTypes;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SteelSheetmetalTankLogic.State;

import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class SteelSheetmetalTankRenderer extends ITBaseBlockEntityRenderer<MultiblockBlockEntityMaster<State>> {

    public SteelSheetmetalTankRenderer() {}

    @Override @NotNull public AABB getRenderBoundingBox(@NotNull MultiblockBlockEntityMaster<State> tile) { return new AABB(tile.getBlockPos()).inflate(9); }

    @Override public void render(MultiblockBlockEntityMaster<State> tile, float partialTicks, PoseStack matrixStack, @NotNull MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        final State state = tile.getHelper().getState();
        matrixStack.pushPose();
        Vec3 center = Vec3.atLowerCornerOf(tile.getHelper().getContext().getLevel().toAbsolute(new BlockPos(2, 0, 2)).subtract(tile.getBlockPos()));
        matrixStack.translate(center.x + .5, 0, center.z + .5);
        FluidStack fs = state.tank.getFluid();
        matrixStack.translate(0, 5.5f, 0);
        float baseScale = .0625f;
        matrixStack.scale(baseScale, -baseScale, baseScale);
        float xx = -0.8125f;
        float zz = 2.49f;
        xx /= baseScale;
        zz /= baseScale;
        matrixStack.mulPose(Axis.YP.rotationDegrees(90f));
        for (int side = 0; side < 2; side++) {
            matrixStack.pushPose();
            matrixStack.translate(xx, 0, zz);
            Matrix4f mat = matrixStack.last().pose();
            final VertexConsumer builder = bufferIn.getBuffer(ITRenderTypes.TRANSLUCENT_POSITION_COLOR);
            builder.addVertex(mat, -4, -4, 0).setColor(0x22, 0x22, 0x22, 0xff);
            builder.addVertex(mat, -4, 80, 0).setColor(0x22, 0x22, 0x22, 0xff);
            builder.addVertex(mat, 30, 80, 0).setColor(0x22, 0x22, 0x22, 0xff);
            builder.addVertex(mat, 30, -4, 0).setColor(0x22, 0x22, 0x22, 0xff);
            if (!fs.isEmpty()) {
                float h = fs.getAmount() / (float) state.tank.getCapacity();
                matrixStack.translate(0, 8, 0.008f);
                ITFluidRender.drawRepeatedFluidSprite(bufferIn.getBuffer(RenderType.solid()), matrixStack, fs, 5, (1 - h) * 60, 16, h * 60);
            }
            matrixStack.popPose();
            matrixStack.mulPose(Axis.YP.rotationDegrees(180f));
        }
        matrixStack.popPose();
    }
}
