package mctmods.immersivetechnology.client.renderer;

import mctmods.immersivetechnology.client.models.multiblock.RotorModels;
import mctmods.immersivetechnology.client.models.ITDynamicModel;
import mctmods.immersivetechnology.client.renderer.helper.ITBaseBlockEntityRenderer;
import mctmods.immersivetechnology.client.renderer.helper.ITRenderUtils;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SteamTurbineLogic;
import mctmods.immersivetechnology.core.ITClientConfig;

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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import java.util.List;

public class SteamTurbineRenderer extends ITBaseBlockEntityRenderer<MultiblockBlockEntityMaster<SteamTurbineLogic.State>> {
    private static final Quaternionf ROTATION = new Quaternionf();

    public SteamTurbineRenderer() {}

    @Override @NotNull public AABB getRenderBoundingBox(MultiblockBlockEntityMaster<SteamTurbineLogic.State> tile) { return new AABB(tile.getBlockPos()).inflate(8); }

    @Override public void render(@NotNull MultiblockBlockEntityMaster<SteamTurbineLogic.State> tile, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        IMultiblockBEHelperMaster<SteamTurbineLogic.State> helper = tile.getHelper();
        IMultiblockContext<SteamTurbineLogic.State> context = helper.getContext();
        SteamTurbineLogic.State state = context.getState();
        MultiblockOrientation orientation = context.getLevel().getOrientation();
        BlockPos pos = tile.getBlockPos();
        Level level = tile.getLevel();
        Direction dir = orientation.front();
        Vec3 axisVec = Vec3.atLowerCornerOf(dir.getNormal());
        double angle = state.animation_fanRotation + state.animation_fanRotationStep * partialTicks;
        if (!ITClientConfig.doSpecialRenderSteamTurbine) { angle = 0; }
        ROTATION.rotationAxis((float)(angle * Mth.DEG_TO_RAD), (float) axisVec.x, (float) axisVec.y, (float) axisVec.z);
        ITDynamicModel selectedModel = (dir == Direction.EAST || dir == Direction.WEST) ? RotorModels.ROTOR_EAST_WEST : RotorModels.ROTOR;
        Vec3 rotorStart1 = Vec3.atLowerCornerOf(context.getLevel().toAbsolute(new BlockPos(2, 1, 0)).subtract(pos));
        poseStack.pushPose();
        poseStack.translate(rotorStart1.x + 0.5, rotorStart1.y + 0.5, rotorStart1.z + 0.5);
        poseStack.mulPose(ROTATION);
        renderDynamicModel(selectedModel, poseStack, buffer, level, pos, packedLight, false);
        poseStack.popPose();
        Vec3 rotorStart2 = Vec3.atLowerCornerOf(context.getLevel().toAbsolute(new BlockPos(2, 1, 6)).subtract(pos));
        poseStack.pushPose();
        poseStack.translate(rotorStart2.x + 0.5, rotorStart2.y + 0.5, rotorStart2.z + 0.5);
        poseStack.mulPose(ROTATION);
        renderDynamicModel(selectedModel, poseStack, buffer, level, pos, packedLight, true);
        poseStack.popPose();
    }

    private void renderDynamicModel(ITDynamicModel model, PoseStack matrix, MultiBufferSource buffer, Level level, BlockPos pos, int light, boolean useCachedLight) {
        matrix.pushPose();
        List<BakedQuad> quads = model.get().getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
        ITRenderUtils.renderModelTESRFancy(quads, buffer.getBuffer(RenderType.solid()), matrix, level, pos, useCachedLight, 0xffffff, light);
        matrix.popPose();
    }
}
