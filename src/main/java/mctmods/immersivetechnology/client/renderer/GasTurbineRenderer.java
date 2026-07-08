package mctmods.immersivetechnology.client.renderer;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import com.mojang.blaze3d.vertex.PoseStack;
import mctmods.immersivetechnology.client.models.multiblock.RotorModels;
import mctmods.immersivetechnology.client.models.ITDynamicModel;
import mctmods.immersivetechnology.client.renderer.helper.ITBaseBlockEntityRenderer;
import mctmods.immersivetechnology.client.renderer.helper.ITRenderUtils;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.GasTurbineLogic;
import mctmods.immersivetechnology.core.ITClientConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import java.util.List;

public class GasTurbineRenderer extends ITBaseBlockEntityRenderer<MultiblockBlockEntityMaster<GasTurbineLogic.State>> {
    public GasTurbineRenderer() {}

    @Override @NotNull public net.minecraft.world.phys.AABB getRenderBoundingBox(MultiblockBlockEntityMaster<GasTurbineLogic.State> tile) { return new net.minecraft.world.phys.AABB(tile.getBlockPos()).inflate(8); }

    @Override public void render(@NotNull MultiblockBlockEntityMaster<GasTurbineLogic.State> tile, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        IMultiblockBEHelperMaster<GasTurbineLogic.State> helper = tile.getHelper();
        IMultiblockContext<GasTurbineLogic.State> context = helper.getContext();
        GasTurbineLogic.State state = context.getState();
        MultiblockOrientation orientation = context.getLevel().getOrientation();
        BlockPos pos = tile.getBlockPos();
        Level level = tile.getLevel();
        Direction dir = orientation.front();
        Vec3 axisVec = Vec3.atLowerCornerOf(dir.getNormal());
        double angle = state.animation_fanRotation + state.animation_fanRotationStep * partialTicks;
        if (!ITClientConfig.doSpecialRenderGasTurbine) { angle = 0; }
        Vec3 rotorStart = Vec3.atLowerCornerOf(context.getLevel().toAbsolute(new BlockPos(1, 1, 0)).subtract(pos));
        Vec3 delta = rotorStart.add(axisVec.scale(0));
        poseStack.pushPose();
        poseStack.translate(delta.x + 0.5, delta.y + 0.5, delta.z + 0.5);
        poseStack.mulPose(new Quaternionf().rotateAxis((float)(angle * Mth.DEG_TO_RAD), axisVec.toVector3f()));
        ITDynamicModel selectedModel = (dir == Direction.EAST || dir == Direction.WEST) ? RotorModels.ROTOR_EAST_WEST : RotorModels.ROTOR;
        renderDynamicModel(selectedModel, poseStack, buffer, level, pos, packedLight);
        poseStack.popPose();
    }

    private void renderDynamicModel(ITDynamicModel model, PoseStack matrix, MultiBufferSource buffer, Level level, BlockPos pos, int light) {
        matrix.pushPose();
        List<BakedQuad> quads = model.get().getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
        ITRenderUtils.renderModelTESRFancy(quads, buffer.getBuffer(RenderType.solid()), matrix, level, pos, false, 0xffffff, light);
        matrix.popPose();
    }
}
