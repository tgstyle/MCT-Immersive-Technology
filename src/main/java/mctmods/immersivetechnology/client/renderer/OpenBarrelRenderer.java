package mctmods.immersivetechnology.client.renderer;

import mctmods.immersivetechnology.client.renderer.helper.BaseBlockEntityRenderer;
import mctmods.immersivetechnology.common.blocks.metal.logic.BarrelOpenBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class OpenBarrelRenderer extends BaseBlockEntityRenderer<BarrelOpenBlockEntity> {

    public OpenBarrelRenderer() {}

    @Override public void render(BarrelOpenBlockEntity be, float partialTicks, @NotNull PoseStack matrixStack, @NotNull MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        Level level = be.getLevel();
        if (level == null) { return; }
        FluidStack fluidStack = be.tank.getFluid();
        if (fluidStack.isEmpty()) { return; }
        Fluid fluid = fluidStack.getFluid();
        if (fluid == null) { return; }
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid);
        FluidState fluidState = fluid.defaultFluidState();
        int color = extensions.getTintColor(fluidState, level, be.getBlockPos());
        if ((color >>> 24) == 0) { color |= 0xFF000000; }
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;
        ResourceLocation still = extensions.getStillTexture(fluidStack);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);
        if (sprite == null) { return; }
        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();
        float diffU = maxU - minU;
        float diffV = maxV - minV;
        float multiplier = 0.25f;
        minU += diffU * multiplier;
        maxU -= diffU * multiplier;
        minV += diffV * multiplier;
        maxV -= diffV * multiplier;
        BlockPos pos = be.getBlockPos();
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        int luminosity = fluid.getFluidType().getLightLevel(fluidStack);
        blockLight = Math.max(blockLight, luminosity);
        int packedLight = (skyLight << 20) | (blockLight << 4);
        float ratio = (float) fluidStack.getAmount() / be.tank.getCapacity();
        float yFilled = 0.8125f * ratio;
        float yStartOffset = 0.125f;
        float fluidHeight = yStartOffset + yFilled;
        float startPos = 0.0625f;
        float endPos = 1 - startPos;
        VertexConsumer builder = buffer.getBuffer(RenderType.translucent());
        matrixStack.pushPose();
        matrixStack.translate(0, 0.001f, 0);
        Matrix4f pose = matrixStack.last().pose();
        Matrix3f normalMatrix = matrixStack.last().normal();
        float nx = 0, ny = 1, nz = 0;
        builder.vertex(pose, startPos, fluidHeight, startPos).color(r, g, b, a).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normalMatrix, nx, ny, nz).endVertex();
        builder.vertex(pose, startPos, fluidHeight, endPos).color(r, g, b, a).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normalMatrix, nx, ny, nz).endVertex();
        builder.vertex(pose, endPos, fluidHeight, endPos).color(r, g, b, a).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normalMatrix, nx, ny, nz).endVertex();
        builder.vertex(pose, endPos, fluidHeight, startPos).color(r, g, b, a).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normalMatrix, nx, ny, nz).endVertex();
        matrixStack.popPose();
    }
}
