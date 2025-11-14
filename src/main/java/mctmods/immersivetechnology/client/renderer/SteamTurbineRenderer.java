package mctmods.immersivetechnology.client.renderer;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import mctmods.immersivetechnology.client.models.helper.ITDynamicModel;
import mctmods.immersivetechnology.client.models.RotorModels;
import mctmods.immersivetechnology.client.renderer.helper.ITBaseBlockEntityRenderer;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SteamTurbineLogic;
import mctmods.immersivetechnology.core.ITClientConfig;
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

public class SteamTurbineRenderer extends ITBaseBlockEntityRenderer<MultiblockBlockEntityMaster<SteamTurbineLogic.State>> {
    @Override
    public void render(@NotNull MultiblockBlockEntityMaster<SteamTurbineLogic.State> tile, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!ITClientConfig.doSpecialRenderSteamTurbine) { return; }
        IMultiblockBEHelperMaster<SteamTurbineLogic.State> helper = tile.getHelper();
        IMultiblockContext<SteamTurbineLogic.State> context = helper.getContext();
        SteamTurbineLogic.State state = context.getState();
        MultiblockOrientation orientation = context.getLevel().getOrientation();
        BlockPos pos = tile.getBlockPos();
        Level level = tile.getLevel();
        Direction dir = orientation.front();
        Vec3 axisVec = Vec3.atLowerCornerOf(dir.getNormal());
        double angle = state.animation_fanRotation + state.animation_fanRotationStep * partialTicks;
        ITDynamicModel selectedModel = (dir == Direction.EAST || dir == Direction.WEST) ? RotorModels.ROTOR_EAST_WEST : RotorModels.ROTOR;
        // First rotor
        Vec3 rotorStart1 = Vec3.atLowerCornerOf(context.getLevel().toAbsolute(new BlockPos(1, 1, 0)).subtract(pos));
        Vec3 delta1 = rotorStart1.add(axisVec.scale(0));
        poseStack.pushPose();
        poseStack.translate(delta1.x + 0.5, delta1.y + 0.5, delta1.z + 0.5);
        poseStack.mulPose(new Quaternionf().rotateAxis((float)(angle * Mth.DEG_TO_RAD), axisVec.toVector3f()));
        renderDynamicModel(selectedModel, poseStack, buffer, level, pos, packedLight);
        poseStack.popPose();
        // Second rotor
        Vec3 rotorStart2 = Vec3.atLowerCornerOf(context.getLevel().toAbsolute(new BlockPos(1, 1, 5)).subtract(pos));
        Vec3 delta2 = rotorStart2.add(axisVec.scale(0));
        poseStack.pushPose();
        poseStack.translate(delta2.x + 0.5, delta2.y + 0.5, delta2.z + 0.5);
        poseStack.mulPose(new Quaternionf().rotateAxis((float)(angle * Mth.DEG_TO_RAD), axisVec.toVector3f()));
        renderDynamicModel(selectedModel, poseStack, buffer, level, pos, packedLight);
        poseStack.popPose();
    }

    private void renderDynamicModel(ITDynamicModel model, PoseStack matrix, MultiBufferSource buffer, Level level, BlockPos pos, int light) {
        matrix.pushPose();
        List<BakedQuad> quads = model.get().getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
        RenderUtils.renderModelTESRFancy(quads, buffer.getBuffer(RenderType.solid()), matrix, level, pos, false, 0xffffff, light);
        matrix.popPose();
    }
}
