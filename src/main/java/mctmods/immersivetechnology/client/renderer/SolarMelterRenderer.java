package mctmods.immersivetechnology.client.renderer;

import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import com.mojang.blaze3d.vertex.PoseStack;
import mctmods.immersivetechnology.client.renderer.helper.ITBaseBlockEntityRenderer;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.SolarMelterLogic;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.SolarMelterRecipe;
import mctmods.immersivetechnology.client.particles.ColoredBeam;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class SolarMelterRenderer extends ITBaseBlockEntityRenderer<MultiblockBlockEntityMaster<SolarMelterLogic.State>> {
    private static final ResourceLocation BEAM_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");

    @Override
    public void render(MultiblockBlockEntityMaster<SolarMelterLogic.State> be, float partialTicks, @NotNull PoseStack matrixStack, @NotNull MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        SolarMelterLogic.State state = be.getHelper().getState();
        Level level = be.getLevel();
        FluidStack fs = state.tanks.input().getFluid();
        double maxHeat = SolarMelterLogic.WORKING_HEAT_LEVEL;
        if (fs.getAmount() > 0) {
            SolarMelterRecipe recipe = SolarMelterRecipe.findRecipe(level, fs);
            if (recipe != null) { maxHeat = recipe.requiredTemp; }
        }
        if (state.heatLevel < maxHeat || !state.sunVisible || state.reflectorStrength <= 0) { return; }
        matrixStack.pushPose();
        matrixStack.translate(1, 0, 1);
        assert level != null;
        long time = level.getGameTime();
        float innerBottomG = 1.0F;
        float innerTopG = 0.0F;
        float innerA = 1.0F;
        float outerBottomG = 1.0F;
        float outerTopG = 0.0F;
        float outerA = 0.2F;
        double worldX = be.getBlockPos().getX() + 1.5D;
        double worldZ = be.getBlockPos().getZ() + 1.5D;
        double worldYBottom = be.getBlockPos().getY() + 2.0D;
        double worldYTop = be.getBlockPos().getY() + 18.0D;
        ColoredBeam.renderBeam(matrixStack, bufferIn, BEAM_TEXTURE, partialTicks, 1.0F, time, false, innerBottomG, innerTopG, innerA, outerBottomG, outerTopG, outerA, 2.0F, 18.0F, worldX, worldYBottom, worldYTop, worldZ);
        matrixStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull MultiblockBlockEntityMaster<SolarMelterLogic.State> be) { return true; }
}
