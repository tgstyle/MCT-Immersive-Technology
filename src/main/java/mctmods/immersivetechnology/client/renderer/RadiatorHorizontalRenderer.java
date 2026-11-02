package mctmods.immersivetechnology.client.renderer;

import com.immersiveconvergence.api.block.ICProperties;
import com.immersiveconvergence.api.client.ICClientUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;

public class RadiatorHorizontalRenderer {
    @SuppressWarnings("SuspiciousNameCombination") public static void render(IBlockState masterState, int x, int y, int z) {
        IBlockState state = masterState.withProperty(ICProperties.FACING_HORIZONTAL, EnumFacing.EAST).withProperty(ICProperties.MULTIBLOCKSLAVE, false).withProperty(ICProperties.BOOLEANS[0], true);
        BlockRendererDispatcher dispatcher = ICClientUtils.mc().getBlockRendererDispatcher();
        IBakedModel model = dispatcher.getBlockModelShapes().getModelForState(state);
        GlStateManager.pushMatrix();
        GlStateManager.translate(z, x, y);
        GlStateManager.disableCull();
        dispatcher.getBlockModelRenderer().renderModelBrightnessColor(model, 1F, 1F, 1F, 1F);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }
}
