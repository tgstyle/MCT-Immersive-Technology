package mctmods.immersivetechnology.client.renderer;

import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mctmods.immersivetechnology.client.renderer.helper.ITBaseBlockEntityRenderer;
import mctmods.immersivetechnology.client.gui.helper.ITGuiHelper;
import mctmods.immersivetechnology.client.renderer.helper.ITRenderTypes;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SteelSheetmetalTankLogic.State;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class SteelSheetmetalTankRenderer extends ITBaseBlockEntityRenderer<MultiblockBlockEntityMaster<State>> {

    public SteelSheetmetalTankRenderer() {}

    @Override
    public void render(MultiblockBlockEntityMaster<State> tile, float partialTicks, PoseStack matrixStack, @NotNull MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        final State state = tile.getHelper().getState();
        matrixStack.pushPose();
        rotateForFacing(matrixStack, tile.getHelper().getContext().getLevel().getOrientation().front());
        matrixStack.translate(.5, 0, .5);
        FluidStack fs = state.tank.getFluid();
        matrixStack.translate(0, 3.5f, 0);
        float baseScale = .0625f;
        matrixStack.scale(baseScale, -baseScale, baseScale);
        float xx = -.5f / baseScale;
        float zz = (1.5f + .001f) / baseScale;
        RenderSystem.enablePolygonOffset();
        RenderSystem.polygonOffset(0.0f, 1.0f);
        for (int side = 0; side < 4; side++) {
            matrixStack.pushPose();
            matrixStack.mulPose(Axis.YP.rotationDegrees(side * 90f));
            matrixStack.translate(xx, 0, zz);
            Matrix4f mat = matrixStack.last().pose();
            final VertexConsumer builder = bufferIn.getBuffer(ITRenderTypes.TRANSLUCENT_POSITION_COLOR);
            builder.vertex(mat, 6, 0, 0).color(0x22, 0x22, 0x22, 0xff).endVertex();
            builder.vertex(mat, 6, 16, 0).color(0x22, 0x22, 0x22, 0xff).endVertex();
            builder.vertex(mat, 10, 16, 0).color(0x22, 0x22, 0x22, 0xff).endVertex();
            builder.vertex(mat, 10, 0, 0).color(0x22, 0x22, 0x22, 0xff).endVertex();
            if (!fs.isEmpty()) {
                float h = fs.getAmount() / (float) state.tank.getCapacity();
                matrixStack.translate(0, 0, .004f);
                ITGuiHelper.drawRepeatedFluidSprite(bufferIn.getBuffer(RenderType.solid()), matrixStack, fs, 6, 0 + (1 - h) * 16, 4, h * 16);
            }
            matrixStack.popPose();
        }
        RenderSystem.disablePolygonOffset();
        matrixStack.popPose();
    }
}
