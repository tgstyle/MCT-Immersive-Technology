package mctmods.immersivetechnology.client.renderer;

import mctmods.immersivetechnology.client.models.ModDynamicModel;
import mctmods.immersivetechnology.client.models.multiblock.StyleNOXModels;
import mctmods.immersivetechnology.client.renderer.helper.BaseBlockEntityRenderer;
import mctmods.immersivetechnology.client.renderer.helper.RenderUtils;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.StyleNOXLogic;
import mctmods.immersivetechnology.core.ClientConfig;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class StyleNOXRenderer extends BaseBlockEntityRenderer<MultiblockBlockEntityMaster<StyleNOXLogic.State>> {
    private static final BlockPos ROD_COLUMN = new BlockPos(5, 0, 5);

    public StyleNOXRenderer() {}

    @Override @NotNull public AABB getRenderBoundingBox(MultiblockBlockEntityMaster<StyleNOXLogic.State> tile) { return new AABB(tile.getBlockPos()).inflate(12); }

    @Override public void render(@NotNull MultiblockBlockEntityMaster<StyleNOXLogic.State> tile, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        IMultiblockBEHelperMaster<StyleNOXLogic.State> helper = tile.getHelper();
        IMultiblockContext<StyleNOXLogic.State> context = helper.getContext();
        StyleNOXLogic.State state = context.getState();
        BlockPos pos = tile.getBlockPos();
        Level level = tile.getLevel();
        float rodPosition = state.animation_rodPrev + (state.animation_rodPosition - state.animation_rodPrev) * partialTicks;
        if (!ClientConfig.doSpecialRenderStyleNOX) { rodPosition = state.animation_rodPosition; }
        Vec3 base = Vec3.atLowerCornerOf(context.getLevel().toAbsolute(ROD_COLUMN).subtract(pos));
        poseStack.pushPose();
        poseStack.translate(base.x + 0.5, base.y - StyleNOXLogic.ROD_TRAVEL * (1 - rodPosition), base.z + 0.5);
        renderDynamicModel(StyleNOXModels.RODS, poseStack, buffer, level, pos, packedLight);
        poseStack.popPose();
    }

    private void renderDynamicModel(ModDynamicModel model, PoseStack matrix, MultiBufferSource buffer, Level level, BlockPos pos, int light) {
        matrix.pushPose();
        List<BakedQuad> quads = model.get().getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
        RenderUtils.renderModelTESRFancy(quads, buffer.getBuffer(RenderType.solid()), matrix, level, pos, false, 0xffffff, light);
        matrix.popPose();
    }
}
