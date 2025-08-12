package mctmods.immersivetechnology.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mctmods.immersivetechnology.client.renderer.helper.ITBaseBlockEntityRenderer;
import mctmods.immersivetechnology.common.blocks.metal.OpenBarrelBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import java.util.logging.Logger;

public class OpenBarrelRenderer extends ITBaseBlockEntityRenderer<OpenBarrelBlockEntity> {
    private static final Logger LOGGER = Logger.getLogger("ImmersiveTechnology");

    @Override
    public void render(OpenBarrelBlockEntity te, float partialTicks, @NotNull PoseStack matrixStack, @NotNull MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        FluidStack fluidStack = te.tank.getFluid();
        if (fluidStack.isEmpty()) {
            LOGGER.info("No fluid in open barrel at " + te.getBlockPos());
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        if (fluid == null) {
            LOGGER.warning("Null fluid in open barrel at " + te.getBlockPos());
            return;
        }

        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid);
        FluidState fluidState = fluid.defaultFluidState();
        int color = extensions.getTintColor(fluidState, te.getLevel(), te.getBlockPos());
        if ((color >>> 24) == 0) color |= 0xFF000000;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        ResourceLocation still = extensions.getStillTexture(fluidStack);
        ResourceLocation blockAtlas = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/atlas/blocks.png");
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(blockAtlas).apply(still);
        if (sprite == null) {
            LOGGER.warning("Failed to load fluid texture: " + still + " for fluid: " + fluid.getFluidType().getDescriptionId() + " at " + te.getBlockPos());
            return;
        }
        LOGGER.info("Rendering fluid: " + fluid.getFluidType().getDescriptionId() + ", Texture: " + still + ", Sprite: " + sprite.contents().name() + " at " + te.getBlockPos());

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

        Level level = te.getLevel();
        BlockPos pos = te.getBlockPos();
        if (level == null) {
            LOGGER.warning("Null level for open barrel at " + pos);
            return;
        }
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        int luminosity = fluid.getFluidType().getLightLevel(fluidStack);
        blockLight = Math.max(blockLight, luminosity);
        int packedLight = (skyLight << 20) | (blockLight << 4);
        LOGGER.info("Light: Block=" + blockLight + ", Sky=" + skyLight + ", Packed=" + packedLight + " at " + pos);

        float ratio = (float) fluidStack.getAmount() / te.tank.getCapacity();
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
        builder = buffer.getBuffer(RenderType.solid());
        builder.vertex(pose, startPos, fluidHeight + 0.002f, startPos).color(r, g, b, a).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normalMatrix, nx, ny, nz).endVertex();
        builder.vertex(pose, startPos, fluidHeight + 0.002f, endPos).color(r, g, b, a).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normalMatrix, nx, ny, nz).endVertex();
        builder.vertex(pose, endPos, fluidHeight + 0.002f, endPos).color(r, g, b, a).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normalMatrix, nx, ny, nz).endVertex();
        builder.vertex(pose, endPos, fluidHeight + 0.002f, startPos).color(r, g, b, a).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normalMatrix, nx, ny, nz).endVertex();

        matrixStack.popPose();
    }
}
