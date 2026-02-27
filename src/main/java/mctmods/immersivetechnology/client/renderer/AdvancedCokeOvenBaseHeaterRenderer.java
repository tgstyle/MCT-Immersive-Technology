package mctmods.immersivetechnology.client.renderer;

import blusunrize.immersiveengineering.api.ApiUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import mctmods.immersivetechnology.client.models.ITDynamicModel;
import mctmods.immersivetechnology.client.renderer.helper.ITBaseBlockEntityRenderer;
import mctmods.immersivetechnology.client.renderer.helper.ITRenderUtils;
import mctmods.immersivetechnology.common.blocks.metal.logic.AdvancedCokeOvenBaseHeaterBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import java.util.List;

public class AdvancedCokeOvenBaseHeaterRenderer extends ITBaseBlockEntityRenderer<AdvancedCokeOvenBaseHeaterBlockEntity> {
    public static ITDynamicModel FAN_MODEL;

    public AdvancedCokeOvenBaseHeaterRenderer() {}

    @Override public void render(@NotNull AdvancedCokeOvenBaseHeaterBlockEntity tile, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.dummy || tile.getLevel() == null || FAN_MODEL == null) { return; }
        Level level = tile.getLevel();
        BlockPos pos = tile.getBlockPos();
        Direction facing = tile.getFacing();
        float angle = tile.getFanRotation(partialTicks);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(new Quaternionf().rotateY(facing.toYRot() * Mth.DEG_TO_RAD));
        poseStack.mulPose(new Quaternionf().rotateX(angle * Mth.DEG_TO_RAD));
        poseStack.translate(-0.5, -0.5, -0.5);

        renderDynamicModel(FAN_MODEL, poseStack, buffer, level, pos, packedLight);
        poseStack.popPose();
    }

    private void renderDynamicModel(ITDynamicModel model, PoseStack matrix, MultiBufferSource buffer, Level level, BlockPos pos, int light) {
        matrix.pushPose();
        List<BakedQuad> quads = model.get().getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
        ITRenderUtils.renderModelTESRFancy(quads, buffer.getBuffer(RenderType.solid()), matrix, level, pos, false, 0xffffff, light);
        matrix.popPose();
    }
}
