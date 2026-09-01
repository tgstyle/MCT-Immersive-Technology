package mctmods.immersivetechnology.client.renderer;

import mctmods.immersivetechnology.client.models.ModDynamicModel;
import mctmods.immersivetechnology.client.renderer.helper.BaseBlockEntityRenderer;
import com.immersiveconvergence.api.client.RenderUtils;
import mctmods.immersivetechnology.common.blocks.metal.logic.AdvancedCokeOvenBaseHeaterBlockEntity;

import blusunrize.immersiveengineering.api.ApiUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import java.util.List;

public class AdvancedCokeOvenBaseHeaterRenderer extends BaseBlockEntityRenderer<AdvancedCokeOvenBaseHeaterBlockEntity> {
    private static final Quaternionf ROT_Y = new Quaternionf();
    private static final Quaternionf ROT_X = new Quaternionf();
    public static ModDynamicModel FAN_MODEL;

    public AdvancedCokeOvenBaseHeaterRenderer() {}

    @Override public void render(@Nonnull AdvancedCokeOvenBaseHeaterBlockEntity tile, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.dummy || tile.getLevel() == null || FAN_MODEL == null) { return; }
        Level level = tile.getLevel();
        BlockPos pos = tile.getBlockPos();
        Direction facing = tile.getFacing();
        float angle = tile.getFanRotation(partialTicks);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        ROT_Y.rotationY(facing.toYRot() * Mth.DEG_TO_RAD);
        ROT_X.rotationX(angle * Mth.DEG_TO_RAD);
        poseStack.mulPose(ROT_Y);
        poseStack.mulPose(ROT_X);
        poseStack.translate(-0.5, -0.5, -0.5);

        renderDynamicModel(FAN_MODEL, poseStack, buffer, level, pos, packedLight);
        poseStack.popPose();
    }

    private void renderDynamicModel(ModDynamicModel model, PoseStack matrix, MultiBufferSource buffer, Level level, BlockPos pos, int light) {
        matrix.pushPose();
        List<BakedQuad> quads = model.get().getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
        RenderUtils.renderModelTESRFancy(quads, buffer.getBuffer(RenderType.solid()), matrix, level, pos, false, 0xffffff, light);
        matrix.popPose();
    }
}
