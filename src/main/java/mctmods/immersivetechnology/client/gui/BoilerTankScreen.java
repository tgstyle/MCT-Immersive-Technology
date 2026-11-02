package mctmods.immersivetechnology.client.gui;

import mctmods.immersivetechnology.common.gui.BoilerTankMenu;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.util.TranslationKey;

import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.client.gui.BaseContainerScreen;
import com.immersiveconvergence.api.client.gui.GuiFluidArea;
import com.immersiveconvergence.api.client.gui.GuiInfoArea;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import java.util.List;
import javax.annotation.Nonnull;

public class BoilerTankScreen extends BaseContainerScreen<BoilerTankMenu> {
    private static final ResourceLocation TEXTURE = Reference.makeTextureLocation("boiler_tank");

    public BoilerTankScreen(BoilerTankMenu container, Inventory inventoryPlayer, Component title) { super(container, inventoryPlayer, title, TEXTURE); }

    @Override protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics) {
        float heatLevel = menu.getHeatLevel();
        double workingHeatLevel = menu.getWorkingHeatLevel();
        int barWidth = Mth.clamp(Math.round(41 * heatLevel / (float)workingHeatLevel), 0, 41);
        graphics.blit(TEXTURE, leftPos + 67, topPos + 5, 176, 0, barWidth, 9);
    }

    @Override @Nonnull protected List<GuiInfoArea> makeInfoAreas() {
        return ImmutableList.of(
                new GuiFluidArea(menu.tanks.input(), new Rect2i(leftPos + 67, topPos + 20, 16, 47), 177, 31, 20, 51, TEXTURE),
                new GuiFluidArea(menu.tanks.output(), new Rect2i(leftPos + 92, topPos + 20, 16, 47), 177, 31, 20, 51, TEXTURE),
                new GuiInfoArea(new Rect2i(leftPos + 67, topPos + 5, 41, 9)) {
                    @Override protected void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip) {
                        tooltip.add(Component.translatable(TranslationKey.GUI_TEMPERATURE.getLocation()));
                        float heatLevel = menu.getHeatLevel();
                        double maxHeat = menu.getWorkingHeatLevel();
                        tooltip.add(Component.translatable(TranslationKey.GUI_HEAT_LEVEL_DETAILED.getLocation(), (int)heatLevel, (int)maxHeat).withStyle(ChatFormatting.RED));
                    }

                    @Override public void draw(GuiGraphics graphics) { }
                }
        );
    }
}
