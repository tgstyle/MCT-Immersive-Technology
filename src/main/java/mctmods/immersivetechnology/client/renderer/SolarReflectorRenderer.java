package mctmods.immersivetechnology.client.renderer;

import mctmods.immersivetechnology.client.models.multiblock.SolarReflectorModels;
import com.immersiveconvergence.api.client.StandaloneModel;
import com.immersiveconvergence.api.client.BaseBlockEntityRenderer;
import com.immersiveconvergence.api.client.RenderUtils;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarReflectorLogic;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
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
import org.joml.Vector3f;
import java.util.List;

import javax.annotation.Nonnull;

public class SolarReflectorRenderer extends BaseBlockEntityRenderer<MultiblockBlockEntityMaster<SolarReflectorLogic.State>> {
    private static final Quaternionf IDENTITY = new Quaternionf();
    private static final Quaternionf ROT_Y90 = new Quaternionf().rotateY((float) Math.toRadians(90));
    private static final Quaternionf ROT_SUPPORT = new Quaternionf();
    private static final Quaternionf ROT_MIRROR = new Quaternionf();
    private static final Vector3f AXIS = new Vector3f();
    private static final BlockPos ORIGIN = new BlockPos(1, 0, 1);

    public SolarReflectorRenderer() {}

    @Override public void render(@Nonnull MultiblockBlockEntityMaster<SolarReflectorLogic.State> tile, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        IMultiblockBEHelperMaster<SolarReflectorLogic.State> helper = tile.getHelper();
        IMultiblockContext<SolarReflectorLogic.State> context = helper.getContext();
        SolarReflectorLogic.State state = context.getState();
        MultiblockOrientation orientation = context.getLevel().getOrientation();
        BlockPos pos = tile.getBlockPos();
        Level level = tile.getLevel();
        Direction dir = orientation.front();
        double supportAngle = state.animation_supportRotation;
        double mirrorAngle = state.animation_mirrorTilt;
        StandaloneModel supportModel = SolarReflectorModels.SUPPORT;
        StandaloneModel mirrorModel = SolarReflectorModels.MIRROR;
        BlockPos start = context.getLevel().toAbsolute(ORIGIN);
        double startX = start.getX() - pos.getX() + 0.5;
        double startY = start.getY() - pos.getY();
        double startZ = start.getZ() - pos.getZ() + 0.5;
        boolean isEW = dir.getStepX() != 0;
        Quaternionf orientRot = isEW ? ROT_Y90 : IDENTITY;
        ROT_SUPPORT.rotationY((float)(supportAngle * Mth.DEG_TO_RAD));
        AXIS.set(dir.getStepZ(), 0, dir.getStepX());
        orientRot.transform(AXIS);
        poseStack.pushPose();
        poseStack.translate(startX, startY, startZ);
        poseStack.mulPose(orientRot);
        poseStack.mulPose(ROT_SUPPORT);
        renderDynamicModel(supportModel, poseStack, buffer, level, pos, packedLight, false);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(startX, startY, startZ);
        poseStack.mulPose(orientRot);
        poseStack.mulPose(ROT_SUPPORT);
        poseStack.translate(0, 2, 0);
        ROT_MIRROR.rotationAxis((float)(-mirrorAngle * Mth.DEG_TO_RAD), AXIS);
        poseStack.mulPose(ROT_MIRROR);
        poseStack.translate(0, -2, 0);
        renderDynamicModel(mirrorModel, poseStack, buffer, level, pos, packedLight, true);
        poseStack.popPose();
    }

    private void renderDynamicModel(StandaloneModel model, PoseStack matrix, MultiBufferSource buffer, Level level, BlockPos pos, int light, boolean useCachedLight) {
        matrix.pushPose();
        List<BakedQuad> quads = model.get().getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
        RenderUtils.renderModelTESRFancy(quads, buffer.getBuffer(RenderType.solid()), matrix, level, pos, useCachedLight, 0xffffff, light);
        matrix.popPose();
    }
}
