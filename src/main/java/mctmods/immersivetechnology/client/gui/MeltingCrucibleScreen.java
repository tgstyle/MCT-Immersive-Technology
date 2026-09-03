package mctmods.immersivetechnology.client.gui;

import com.immersiveconvergence.api.client.gui.BaseContainerScreen;
import com.immersiveconvergence.api.client.gui.GuiEnergyArea;
import com.immersiveconvergence.api.client.gui.GuiFluidArea;
import com.immersiveconvergence.api.client.gui.GuiInfoArea;
import mctmods.immersivetechnology.common.multiblocks.gui.MeltingCrucibleMenu;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.MeltingCrucibleLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.MeltingRecipe;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class MeltingCrucibleScreen extends BaseContainerScreen<MeltingCrucibleMenu> {
    private static final ResourceLocation TEXTURE = Reference.makeTextureLocation("melting_crucible");

    public MeltingCrucibleScreen(MeltingCrucibleMenu container, Inventory inventoryPlayer, Component title) {
        super(container, inventoryPlayer, title, TEXTURE);
    }

    @Override @Nonnull protected List<GuiInfoArea> makeInfoAreas() {
        return List.of(
                new GuiEnergyArea(this.leftPos + 16, this.topPos + 22, menu.energy),
                new GuiFluidArea(menu.inputTank, new Rect2i(this.leftPos + 102, this.topPos + 21, 16, 47), 177, 31, 20, 51, TEXTURE),
                new GuiFluidArea(menu.outputTank, new Rect2i(this.leftPos + 126, this.topPos + 21, 16, 47), 177, 31, 20, 51, TEXTURE)
        );
    }

    private double getMaxHeat() {
        double workingHeat = MeltingCrucibleLogic.workingHeatLevel();
        FluidStack fs = menu.inputTank.getFluid();
        if (fs.getAmount() <= 0) { return workingHeat; }
        assert minecraft != null;
        MeltingRecipe recipe = MeltingRecipe.findRecipe(minecraft.level, fs);
        if (recipe == null) { return workingHeat; }
        return recipe.requiredTemp;
    }

    @Override protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float f, int mx, int my) {
        int heat = menu.state.get(3);
        double max = getMaxHeat();
        int heatBarSize = (int)(51 * Math.min(1.0, heat / max));
        graphics.blit(TEXTURE, leftPos + 30, topPos + 9, 176, 0, heatBarSize, 9);
    }

    @Override protected void gatherAdditionalTooltips(int mouseX, int mouseY, Consumer<Component> addLine, Consumer<Component> addGray) {
        if (mouseX >= leftPos + 30 && mouseX < leftPos + 81 && mouseY >= topPos + 9 && mouseY < topPos + 18) {
            int internalHeat = menu.state.get(3);
            double maxHeat = getMaxHeat();
            addLine.accept(Component.literal("Temperature"));
            addLine.accept(Component.literal(String.format("%.0f/%.0f C", (double)internalHeat, maxHeat)));
        }
    }
}
