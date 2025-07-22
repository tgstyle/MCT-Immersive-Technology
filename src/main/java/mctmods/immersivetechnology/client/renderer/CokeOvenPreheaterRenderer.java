package mctmods.immersivetechnology.client.renderer;

import blusunrize.immersiveengineering.client.utils.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import mctmods.immersivetechnology.client.models.ITDynamicModel;
import mctmods.immersivetechnology.common.blocks.metal.CokeOvenPreheaterBlockEntity;
import mctmods.immersivetechnology.core.ITClientConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

public class CokeOvenPreheaterRenderer extends ITBlockEntityRenderer<CokeOvenPreheaterBlockEntity>
{
    public static final String NAME = "coke_oven_preheater_fan";
    public static ITDynamicModel MODEL;
    @Override
    public void render(
            @Nonnull CokeOvenPreheaterBlockEntity bEntity,
            float partial, @Nonnull PoseStack transform, @Nonnull MultiBufferSource buffers, int light, int overlay
    )
    {
        boolean doRendering = ITClientConfig.doSpecialRenderGasTurbine.get();
        if(!doRendering) return;

        transform.pushPose();
        transform.translate(0.5, 0.5, 0.5);
        rotateForFacingNoCentering(transform, bEntity.getFacing());
        final float angle = bEntity.angle+CokeOvenPreheaterBlockEntity.ANGLE_PER_TICK*(bEntity.active?partial: 0);
        Vector3f axis = new Vector3f(0, 0, 1);
        transform.mulPose(new Quaternionf().rotateAxis(angle, axis));
        transform.translate(-0.5, -0.5, -0.5);
        RenderUtils.renderModelTESRFast(
                MODEL.getNullQuads(), buffers.getBuffer(RenderType.solid()), transform, light, overlay
        );
        transform.popPose();
    }
}
