package mctmods.immersivetechnology.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mctmods.immersivetechnology.common.blocks.metal.OpenBarrelBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class OpenBarrelRenderer implements BlockEntityRenderer<OpenBarrelBlockEntity> {
    public OpenBarrelRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(OpenBarrelBlockEntity te, float partialTicks, @NotNull PoseStack matrixStack, @NotNull MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        FluidStack fluidStack = te.tank.getFluid();
        if (fluidStack.isEmpty()) return;
        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid);
        int color = extensions.getTintColor(fluidStack);
        int r = (color >> 16 & 255);
        int g = (color >> 8 & 255);
        int b = (color & 255);
        int a = (color >> 24 & 255);
        if (a == 0) { a = 255; }
        ResourceLocation still = extensions.getStillTexture(fluidStack);
        ResourceLocation blockAtlas = new ResourceLocation("textures/atlas/blocks.png");
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(blockAtlas).apply(still);
        if (sprite == null) return;
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
        int luminosity = fluid.getFluidType().getLightLevel(fluidStack);
        Level level = te.getLevel();
        BlockPos pos = te.getBlockPos();
        assert level != null;
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        blockLight = Math.max(blockLight, luminosity);
        combinedLight = (skyLight << 20) | (blockLight << 4);
        float ratio = (float) te.tank.getFluidAmount() / te.tank.getCapacity();
        float yFilled = (float) (0.8125 * ratio);
        float yStartOffset = 0.125f;
        float fluidHeight = yStartOffset + yFilled;
        float startPos = 0.0625f;
        float endPos = 1 - startPos;
        VertexConsumer builder = buffer.getBuffer(RenderType.translucent());
        Matrix4f pose = matrixStack.last().pose();
        Matrix3f normalMatrix = matrixStack.last().normal();
        float nx = 0;
        float ny = 1;
        float nz = 0;
        builder.vertex(pose, startPos, fluidHeight, startPos).color(r, g, b, a).uv(minU, minV).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normalMatrix, nx, ny, nz).endVertex();
        builder.vertex(pose, endPos, fluidHeight, startPos).color(r, g, b, a).uv(maxU, minV).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normalMatrix, nx, ny, nz).endVertex();
        builder.vertex(pose, endPos, fluidHeight, endPos).color(r, g, b, a).uv(maxU, maxV).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normalMatrix, nx, ny, nz).endVertex();
        builder.vertex(pose, startPos, fluidHeight, endPos).color(r, g, b, a).uv(minU, maxV).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normalMatrix, nx, ny, nz).endVertex();
    }
}

