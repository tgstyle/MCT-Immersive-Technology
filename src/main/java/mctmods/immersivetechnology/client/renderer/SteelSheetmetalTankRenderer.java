package mctmods.immersivetechnology.client.renderer;

import mctmods.immersivetechnology.client.models.util.ITFluidRender;
import mctmods.immersivetechnology.client.renderer.helper.ITBaseBlockEntityRenderer;
import mctmods.immersivetechnology.client.renderer.helper.ITRenderTypes;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SteelSheetmetalTankLogic.State;

import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class SteelSheetmetalTankRenderer extends ITBaseBlockEntityRenderer<MultiblockBlockEntityMaster<State>> {

    public SteelSheetmetalTankRenderer() {}

    @Override public void render(MultiblockBlockEntityMaster<State> tile, float partialTicks, PoseStack matrixStack, @NotNull MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        final State state = tile.getHelper().getState();
        matrixStack.pushPose();
        rotateForFacing(matrixStack, tile.getHelper().getContext().getLevel().getOrientation().front());
        matrixStack.translate(.5, 0, .5);
        FluidStack fs = state.tank.getFluid();
        matrixStack.translate(0, 3.5f, 0);
        float baseScale = .0625f;
        matrixStack.scale(baseScale, -baseScale, baseScale);
        float xx = -.5f;
        float zz = 1.5f - 0.01f;
        xx /= baseScale;
        zz /= baseScale;
        for (int side = 0; side < 4; side++) {
            matrixStack.pushPose();
            matrixStack.translate(xx, 0, zz);
            Matrix4f mat = matrixStack.last().pose();
            final VertexConsumer builder = bufferIn.getBuffer(ITRenderTypes.TRANSLUCENT_POSITION_COLOR);
            builder.vertex(mat, -4, -4, 0).color(0x22, 0x22, 0x22, 0xff).endVertex();
            builder.vertex(mat, -4, 20, 0).color(0x22, 0x22, 0x22, 0xff).endVertex();
            builder.vertex(mat, 20, 20, 0).color(0x22, 0x22, 0x22, 0xff).endVertex();
            builder.vertex(mat, 20, -4, 0).color(0x22, 0x22, 0x22, 0xff).endVertex();
            if (!fs.isEmpty()) {
                float h = fs.getAmount() / (float) state.tank.getCapacity();
                matrixStack.translate(0, 0, 0.008f);
                ITFluidRender.drawRepeatedFluidSprite(bufferIn.getBuffer(RenderType.solid()), matrixStack, fs, 0, 0 + (1 - h) * 16, 16, h * 16);
            }
            matrixStack.popPose();
            matrixStack.mulPose(Axis.YP.rotationDegrees(90f));
        }
        matrixStack.popPose();
    }
}
