package mctmods.immersivetechnology.client.gui;

import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.client.gui.BaseContainerScreen;
import com.immersiveconvergence.api.client.gui.GuiFluidArea;
import com.immersiveconvergence.api.client.gui.GuiInfoArea;
import mctmods.immersivetechnology.common.multiblocks.gui.SolarMenu;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarMelterLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarTowerLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.MeltingRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.SolarTowerRecipe;
import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MenuTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class SolarScreen extends BaseContainerScreen<SolarMenu> {
    private static final ResourceLocation TEXTURE = Reference.makeTextureLocation("solar");
    public final boolean isMelter;

    public SolarScreen(SolarMenu container, Inventory inventoryPlayer, Component title) {
        super(container, inventoryPlayer, title, TEXTURE);
        this.isMelter = menu.getType() == MenuTypes.SOLAR_MELTER_MENU.getType();
    }

    @Override @Nonnull protected List<GuiInfoArea> makeInfoAreas() { return ImmutableList.of(new GuiFluidArea(menu.inputTank, new Rect2i(leftPos + 102, topPos + 21, 16, 47), 177, 31, 20, 51, TEXTURE), new GuiFluidArea(menu.outputTank, new Rect2i(leftPos + 126, topPos + 21, 16, 47), 177, 31, 20, 51, TEXTURE)); }

    @Override protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float f, int mx, int my) {
        double heatLevel = menu.state.get(0);
        double max = getMaxHeat();
        int heatBarSize = (int) Math.round(42 * Math.min(1, heatLevel / max));
        graphics.blit(TEXTURE, leftPos + 16, topPos + 9, 176, 0, heatBarSize, 9);
        int[] counts = {menu.state.get(2), menu.state.get(3), menu.state.get(4), menu.state.get(5)};
        assert minecraft != null;
        assert minecraft.player != null;
        Direction front = Direction.fromYRot(minecraft.player.getYRot());
        Direction right = front.getClockWise();
        Direction back = front.getOpposite();
        Direction left = front.getCounterClockWise();
        int bit_front = getBitForDir(front);
        int bit_right = getBitForDir(right);
        int bit_back = getBitForDir(back);
        int bit_left = getBitForDir(left);
        int[][] blitCoordinates = {{32, 24}, {48, 40}, {32, 56}, {16, 40}};
        String[] labels = {TranslationKey.GUI_DIRECTION_NORTH.text(), TranslationKey.GUI_DIRECTION_EAST.text(), TranslationKey.GUI_DIRECTION_SOUTH.text(), TranslationKey.GUI_DIRECTION_WEST.text()};
        boolean canHeat = menu.state.get(1) > 0 && minecraft.level != null && !minecraft.level.isRaining() && menu.state.get(7) == 1;
        for (int pos = 0; pos < 4; pos++) {
            int bit = switch (pos) {
                case 0 -> bit_front;
                case 1 -> bit_right;
                case 2 -> bit_back;
                case 3 -> bit_left;
                default -> -1;
            };
            if (bit >= 0 && counts[bit] > 0) {
                int x = leftPos + blitCoordinates[pos][0];
                int y = topPos + blitCoordinates[pos][1];
                if (canHeat) {
                    float brightness = Math.min(5, counts[bit]) / 5f;
                    graphics.setColor(brightness, brightness, brightness, 1f);
                    graphics.blit(TEXTURE, x, y, 198, 31, 10, 10);
                    graphics.setColor(1f, 1f, 1f, 1f);
                }
                graphics.drawString(font, labels[bit], x + 3, y + 2, 0x606060, false);
            }
        }
    }

    private static int getBitForDir(Direction d) {
        return switch (d) {
            case NORTH -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> -1;
        };
    }

    private double getMaxHeat() {
        double workingHeat = isMelter ? SolarMelterLogic.workingHeatLevel() : SolarTowerLogic.workingHeatLevel();
        FluidStack fs = menu.inputTank.getFluid();
        if (fs.getAmount() <= 0) { return workingHeat; }
        assert minecraft != null;
        if (isMelter) {
            MeltingRecipe recipe = MeltingRecipe.findRecipe(minecraft.level, fs);
            if (recipe == null) { return workingHeat; }
            return recipe.requiredTemp;
        }
        else {
            SolarTowerRecipe recipe = SolarTowerRecipe.findRecipe(minecraft.level, fs);
            if (recipe == null) { return workingHeat; }
            return recipe.requiredTemp;
        }
    }

    @Override protected void gatherAdditionalTooltips(int mouseX, int mouseY, Consumer<Component> addLine, Consumer<Component> addGray) {
        if (mouseX >= leftPos + 16 && mouseX < leftPos + 58 && mouseY >= topPos + 9 && mouseY < topPos + 17) {
            double heat = menu.state.get(0);
            double maxHeat = getMaxHeat();
            addLine.accept(Component.translatable(TranslationKey.GUI_TEMPERATURE.getLocation()));
            addLine.accept(Component.translatable(TranslationKey.GUI_HEAT_LEVEL_DETAILED.getLocation(), heat, maxHeat).withStyle(ChatFormatting.RED));
        }
    }
}
