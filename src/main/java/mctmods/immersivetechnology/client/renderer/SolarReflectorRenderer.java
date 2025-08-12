package mctmods.immersivetechnology.client.renderer;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import mctmods.immersivetechnology.client.models.SolarReflectorModels;
import mctmods.immersivetechnology.client.models.helper.ITDynamicModel;
import mctmods.immersivetechnology.client.renderer.helper.ITBaseBlockEntityRenderer;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.SolarReflectorLogic;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import java.util.List;

public class SolarReflectorRenderer extends ITBaseBlockEntityRenderer<MultiblockBlockEntityMaster<SolarReflectorLogic.State>> {
    @Override
    public void render(@NotNull MultiblockBlockEntityMaster<SolarReflectorLogic.State> tile, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        IMultiblockBEHelperMaster<SolarReflectorLogic.State> helper = tile.getHelper();
        IMultiblockContext<SolarReflectorLogic.State> context = helper.getContext();
        SolarReflectorLogic.State state = context.getState();
        MultiblockOrientation orientation = context.getLevel().getOrientation();
        BlockPos pos = tile.getBlockPos();
        Level level = tile.getLevel();
        Direction dir = orientation.front();
        Vec3 axisVec = new Vec3(dir.getStepZ(), 0, dir.getStepX());
        double supportAngle = state.animation_supportRotation + state.animation_supportRotationStep * partialTicks;
        double mirrorAngle = state.animation_mirrorTilt + state.animation_mirrorTiltStep * partialTicks;
        ITDynamicModel supportModel = SolarReflectorModels.SUPPORT;
        ITDynamicModel mirrorModel = SolarReflectorModels.MIRROR;
        Vec3 supportStart = Vec3.atLowerCornerOf(context.getLevel().toAbsolute(new BlockPos(1, 1, 1)).subtract(pos));
        Vec3 mirrorStart = Vec3.atLowerCornerOf(context.getLevel().toAbsolute(new BlockPos(1, 2, 1)).subtract(pos));
        // Render support
        poseStack.pushPose();
        poseStack.translate(supportStart.x + 0.5, supportStart.y - 0.25, supportStart.z + 0.5);
        poseStack.mulPose(new Quaternionf().rotateY((float)(supportAngle * Mth.DEG_TO_RAD)));
        renderDynamicModel(supportModel, poseStack, buffer, level, pos, packedLight);
        poseStack.popPose();
        // Render mirror
        poseStack.pushPose();
        poseStack.translate(mirrorStart.x + 0.5, mirrorStart.y - 0.25, mirrorStart.z + 0.5);
        poseStack.mulPose(new Quaternionf().rotateY((float)(supportAngle * Mth.DEG_TO_RAD)));
        poseStack.mulPose(new Quaternionf().rotateAxis((float)(mirrorAngle * Mth.DEG_TO_RAD), axisVec.toVector3f()));
        renderDynamicModel(mirrorModel, poseStack, buffer, level, pos, packedLight);
        poseStack.popPose();
    }

    private void renderDynamicModel(ITDynamicModel model, PoseStack matrix, MultiBufferSource buffer, Level level, BlockPos pos, int light) {
        matrix.pushPose();
        List<BakedQuad> quads = model.get().getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
        RenderUtils.renderModelTESRFancy(quads, buffer.getBuffer(RenderType.solid()), matrix, level, pos, false, 0xffffff, light);
        matrix.popPose();
    }
}
