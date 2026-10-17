package mctmods.immersivetechnology.client.renderer;

import com.immersiveconvergence.api.client.RenderUtils;
import com.immersiveconvergence.api.client.StandaloneModel;
import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockContext;
import com.immersiveconvergence.api.util.ICUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.List;

public final class MirrorRendering {
    private static final Quaternionf IDENTITY = new Quaternionf();
    private static final Quaternionf ROT_Y90 = new Quaternionf().rotateY((float) Math.toRadians(90));
    private static final Quaternionf ROT_SUPPORT = new Quaternionf();
    private static final Quaternionf ROT_MIRROR = new Quaternionf();
    private static final Vector3f AXIS = new Vector3f();
    private static final BlockPos ORIGIN = new BlockPos(1, 0, 1);
    private static final double PIVOT_HEIGHT = 2.57;

    private MirrorRendering() {}

    public static void render(IMultiblockContext<?> context, BlockPos pos, Level level, PoseStack poseStack, MultiBufferSource buffer, int packedLight, StandaloneModel supportModel, StandaloneModel mirrorModel, float supportAngle, float mirrorTilt, double modelOffset) {
        Direction dir = context.getLevel().getOrientation().front();
        BlockPos start = context.getLevel().toAbsolute(ORIGIN);
        double startX = start.getX() - pos.getX() + 0.5;
        double startY = start.getY() - pos.getY();
        double startZ = start.getZ() - pos.getZ() + 0.5;
        Quaternionf orientRot = dir.getStepX() != 0 ? ROT_Y90 : IDENTITY;
        ROT_SUPPORT.rotationY(supportAngle * Mth.DEG_TO_RAD);
        AXIS.set(dir.getStepZ(), 0, dir.getStepX());
        orientRot.transform(AXIS);
        poseStack.pushPose();
        poseStack.translate(startX, startY, startZ);
        poseStack.mulPose(orientRot);
        poseStack.mulPose(ROT_SUPPORT);
        poseStack.translate(modelOffset, 0, modelOffset);
        renderModel(supportModel, poseStack, buffer, level, pos, packedLight, false);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(startX, startY, startZ);
        poseStack.mulPose(orientRot);
        poseStack.mulPose(ROT_SUPPORT);
        poseStack.translate(0, PIVOT_HEIGHT, 0);
        ROT_MIRROR.rotationAxis(-mirrorTilt * Mth.DEG_TO_RAD, AXIS);
        poseStack.mulPose(ROT_MIRROR);
        poseStack.translate(modelOffset, -PIVOT_HEIGHT, modelOffset);
        renderModel(mirrorModel, poseStack, buffer, level, pos, packedLight, true);
        poseStack.popPose();
    }

    private static void renderModel(StandaloneModel model, PoseStack matrix, MultiBufferSource buffer, Level level, BlockPos pos, int light, boolean useCachedLight) {
        List<BakedQuad> quads = model.get().getQuads(null, null, ICUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
        RenderUtils.renderModelTESRFancy(quads, buffer.getBuffer(RenderType.solid()), matrix, level, pos, useCachedLight, 0xffffff, light);
    }
}
