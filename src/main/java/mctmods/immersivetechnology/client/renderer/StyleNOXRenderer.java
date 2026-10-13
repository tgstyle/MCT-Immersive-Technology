package mctmods.immersivetechnology.client.renderer;

import com.immersiveconvergence.api.client.BaseBlockEntityRenderer;
import com.immersiveconvergence.api.client.RenderUtils;
import mctmods.immersivetechnology.client.models.multiblock.StyleNOXModels;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.StyleNOXLogic;
import mctmods.immersivetechnology.core.ClientConfig;

import com.immersiveconvergence.api.util.ICUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class StyleNOXRenderer extends BaseBlockEntityRenderer<MultiblockBlockEntityMaster<StyleNOXLogic.State>> {
    private static final BlockPos ROD_COLUMN = new BlockPos(5, 0, 5);
    private static final float ROD_PITCH = 0.5f;
    private BakedModel splitModel;
    private final List<List<BakedQuad>> rodQuads = new ArrayList<>();

    public StyleNOXRenderer() {}

    @Override @Nonnull public AABB getRenderBoundingBox(MultiblockBlockEntityMaster<StyleNOXLogic.State> tile) { return new AABB(tile.getBlockPos()).inflate(12); }

    @Override public void render(@Nonnull MultiblockBlockEntityMaster<StyleNOXLogic.State> tile, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        IMultiblockBEHelperMaster<StyleNOXLogic.State> helper = tile.getHelper();
        IMultiblockContext<StyleNOXLogic.State> context = helper.getContext();
        IMultiblockLevel mbLevel = context.getLevel();
        StyleNOXLogic.State state = context.getState();
        BlockPos pos = tile.getBlockPos();
        Level level = tile.getLevel();
        splitRods(StyleNOXModels.RODS.get());
        float rodPosition = state.animation_rodPrev + (state.animation_rodPosition - state.animation_rodPrev) * partialTicks;
        float fuelPosition = state.animation_fuelPrev + (state.animation_fuelPosition - state.animation_fuelPrev) * partialTicks;
        if (!ClientConfig.doSpecialRenderStyleNOX) {
            rodPosition = state.animation_rodPosition;
            fuelPosition = state.animation_fuelPosition;
        }
        BlockPos column = mbLevel.toAbsolute(ROD_COLUMN);
        Vec3 base = Vec3.atLowerCornerOf(column.subtract(pos));
        VertexConsumer consumer = buffer.getBuffer(RenderType.solid());
        poseStack.pushPose();
        poseStack.translate(base.x + 0.5, base.y + StyleNOXLogic.CONTROL_ROD_LIFT - StyleNOXLogic.CONTROL_ROD_TRAVEL * (1 - rodPosition), base.z + 0.5);
        RenderUtils.renderModelTESRFancy(rodQuads.get(cellIndex(0, 0)), consumer, poseStack, level, pos, false, 0xffffff, packedLight);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(base.x + 0.5, base.y - StyleNOXLogic.FUEL_ROD_TRAVEL * fuelPosition, base.z + 0.5);
        for (int slot = 0; slot < StyleNOXLogic.FUEL_SLOTS; slot++) {
            if ((state.rodMask & (1 << slot)) == 0) { continue; }
            int[] cell = StyleNOXLogic.FUEL_SLOT_CELLS[slot];
            BlockPos offset = mbLevel.toAbsolute(ROD_COLUMN.offset(cell[0], 0, cell[1])).subtract(column);
            RenderUtils.renderModelTESRFancy(rodQuads.get(cellIndex(offset.getX(), offset.getZ())), consumer, poseStack, level, pos, true, 0xffffff, packedLight);
        }
        poseStack.popPose();
    }

    private static int cellIndex(int x, int z) { return (x + 1) * 3 + (z + 1); }

    private void splitRods(BakedModel model) {
        if (model == splitModel) { return; }
        rodQuads.clear();
        for (int i = 0; i < 9; i++) { rodQuads.add(new ArrayList<>()); }
        for (BakedQuad quad : model.getQuads(null, null, ICUtils.RANDOM_SOURCE, ModelData.EMPTY, null)) {
            int[] vertices = quad.getVertices();
            int stride = vertices.length / 4;
            float x = 0, z = 0;
            for (int v = 0; v < 4; v++) {
                x += Float.intBitsToFloat(vertices[v * stride]);
                z += Float.intBitsToFloat(vertices[v * stride + 2]);
            }
            int cx = Mth.clamp(Math.round(x / 4 / ROD_PITCH), -1, 1);
            int cz = Mth.clamp(Math.round(z / 4 / ROD_PITCH), -1, 1);
            rodQuads.get(cellIndex(cx, cz)).add(quad);
        }
        splitModel = model;
    }
}
