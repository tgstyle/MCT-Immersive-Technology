package mctmods.immersivetechnology.client.gui.helper;

import blusunrize.immersiveengineering.api.client.TextUtils;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import mctmods.immersivetechnology.client.models.util.FluidRender;
import mctmods.immersivetechnology.client.renderer.helper.RenderTypes;
import mctmods.immersivetechnology.core.util.TranslationKey;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class FluidInfoArea extends InfoArea {
    private final IFluidTank tank;
    private final Rect2i area;
    private final int overlayUMin;
    private final int overlayVMin;
    private final int overlayWidth;
    private final int overlayHeight;
    private final ResourceLocation overlayTexture;
    private final ResourceLocation overlaySprite;
    private final int spriteWidth;
    private final int spriteHeight;

    public FluidInfoArea(IFluidTank tank, Rect2i area, int overlayUMin, int overlayVMin, int overlayWidth, int overlayHeight, ResourceLocation overlayTexture) {
        super(area);
        this.tank = tank;
        this.area = area;
        this.overlayUMin = overlayUMin;
        this.overlayVMin = overlayVMin;
        this.overlayWidth = overlayWidth;
        this.overlayHeight = overlayHeight;
        this.overlayTexture = overlayTexture;
        this.overlaySprite = null;
        this.spriteWidth = 0;
        this.spriteHeight = 0;
    }

    public FluidInfoArea(IFluidTank tank, Rect2i area, int spriteWidth, int spriteHeight, ResourceLocation overlaySprite) {
        super(area);
        this.tank = tank;
        this.area = area;
        this.overlayUMin = 0;
        this.overlayVMin = 0;
        this.overlayWidth = 0;
        this.overlayHeight = 0;
        this.overlayTexture = null;
        this.overlaySprite = overlaySprite;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
    }

    public void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip) {
        Objects.requireNonNull(tooltip);
        fillTooltip(tank.getFluid(), tank.getCapacity(), tooltip::add);
    }

    public static void fillTooltip(FluidStack fluid, int tankCapacity, Consumer<Component> tooltip) {
        if (!fluid.isEmpty()) { tooltip.accept(fluid.getHoverName().copy().withStyle(fluid.getFluid().getFluidType().getRarity(fluid).getStyleModifier())); } else { tooltip.accept(Component.translatable(TranslationKey.GUI_EMPTY.getLocation())); }
        if (Minecraft.getInstance().options.advancedItemTooltips && !fluid.isEmpty()) {
            if (!Screen.hasShiftDown()) { tooltip.accept(Component.translatable(TranslationKey.DESC_HOLD_SHIFT_FOR_INFO.getLocation())); } else {
                tooltip.accept(TextUtils.applyFormat(Component.translatable(TranslationKey.GUI_FLUID_REGISTRY.getLocation(), BuiltInRegistries.FLUID.getKey(fluid.getFluid())), ChatFormatting.DARK_GRAY));
                tooltip.accept(TextUtils.applyFormat(Component.translatable(TranslationKey.GUI_FLUID_DENSITY.getLocation(), fluid.getFluid().getFluidType().getDensity(fluid)), ChatFormatting.DARK_GRAY));
                tooltip.accept(TextUtils.applyFormat(Component.translatable(TranslationKey.GUI_FLUID_TEMPERATURE.getLocation(), fluid.getFluid().getFluidType().getTemperature(fluid)), ChatFormatting.DARK_GRAY));
                tooltip.accept(TextUtils.applyFormat(Component.translatable(TranslationKey.GUI_FLUID_VISCOSITY.getLocation(), fluid.getFluid().getFluidType().getViscosity(fluid)), ChatFormatting.DARK_GRAY));
                tooltip.accept(TextUtils.applyFormat(Component.translatable(TranslationKey.GUI_FLUID_NBT.getLocation(), fluid.getComponentsPatch()), ChatFormatting.DARK_GRAY));
            }
        }
        if (tankCapacity > 0) { tooltip.accept(TextUtils.applyFormat(Component.translatable(TranslationKey.GUI_FLUID_CAPACITY.getLocation(), fluid.getAmount(), tankCapacity), ChatFormatting.GRAY)); } else if (tankCapacity == 0) { tooltip.accept(TextUtils.applyFormat(Component.translatable(TranslationKey.GUI_FLUID_AMOUNT.getLocation(), fluid.getAmount()), ChatFormatting.GRAY)); }
    }

    public void draw(GuiGraphics graphics) {
        FluidStack fluid = tank.getFluid();
        float cap = (float) tank.getCapacity();
        graphics.pose().pushPose();
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(new ByteBufferBuilder(1536));
        if (!fluid.isEmpty()) {
            int fluidHeight = (int) (area.getHeight() * ((float) fluid.getAmount() / cap));
            FluidRender.drawRepeatedFluidSpriteGui(buffer, graphics.pose(), fluid, area.getX(), area.getY() + area.getHeight() - fluidHeight, area.getWidth(), fluidHeight);
        }
        if (overlaySprite == null) {
            int xOff = (area.getWidth() - overlayWidth) / 2;
            int yOff = (area.getHeight() - overlayHeight) / 2;
            RenderType renderType = RenderTypes.getGui(overlayTexture);
            FluidRender.drawTexturedRect(buffer.getBuffer(renderType), graphics.pose(), area.getX() + xOff, area.getY() + yOff, overlayWidth, overlayHeight, 256.0F, overlayUMin, overlayUMin + overlayWidth, overlayVMin, overlayVMin + overlayHeight);
        }
        buffer.endBatch();
        graphics.pose().popPose();
        if (overlaySprite != null) {
            int xOff = (area.getWidth() - spriteWidth) / 2;
            int yOff = (area.getHeight() - spriteHeight) / 2;
            graphics.blitSprite(overlaySprite, spriteWidth, spriteHeight, 0, 0, area.getX() + xOff, area.getY() + yOff, spriteWidth, spriteHeight);
        }
    }
}
