package mctmods.immersivetechnology.client.gui.helper;

import blusunrize.immersiveengineering.api.client.TextUtils;
import com.mojang.blaze3d.vertex.Tesselator;
import mctmods.immersivetechnology.client.renderer.helper.ITRenderTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ITFluidInfoArea extends ITInfoArea {
    private final IFluidTank tank;
    private final Rect2i area;
    private final int overlayUMin;
    private final int overlayVMin;
    private final int overlayWidth;
    private final int overlayHeight;
    private final ResourceLocation overlayTexture;

    public ITFluidInfoArea(IFluidTank tank, Rect2i area, int overlayUMin, int overlayVMin, int overlayWidth, int overlayHeight, ResourceLocation overlayTexture) {
        super(area);
        this.tank = tank;
        this.area = area;
        this.overlayUMin = overlayUMin;
        this.overlayVMin = overlayVMin;
        this.overlayWidth = overlayWidth;
        this.overlayHeight = overlayHeight;
        this.overlayTexture = overlayTexture;
    }

    public void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip) {
        if (ModList.get().isLoaded("jei")) { return; }
        FluidStack fluid = tank.getFluid();
        int capacity = tank.getCapacity();
        Objects.requireNonNull(tooltip);
        fillTooltip(fluid, capacity, tooltip::add);
    }

    public static void fillTooltip(FluidStack fluid, int tankCapacity, Consumer<Component> tooltip) {
        if (!fluid.isEmpty()) { tooltip.accept(fluid.getDisplayName().copy().withStyle(fluid.getFluid().getFluidType().getRarity(fluid).getStyleModifier())); }
        else { tooltip.accept(Component.translatable("gui.immersiveengineering.empty")); }

        if (Minecraft.getInstance().options.advancedItemTooltips && !fluid.isEmpty()) {
            if (!Screen.hasShiftDown()) { tooltip.accept(Component.translatable("desc.immersiveengineering.info.holdShiftForInfo")); }
            else {
                tooltip.accept(TextUtils.applyFormat(Component.literal("Fluid Registry: " + ForgeRegistries.FLUIDS.getKey(fluid.getFluid())), ChatFormatting.DARK_GRAY));
                tooltip.accept(TextUtils.applyFormat(Component.literal("Density: " + fluid.getFluid().getFluidType().getDensity(fluid)), ChatFormatting.DARK_GRAY));
                tooltip.accept(TextUtils.applyFormat(Component.literal("Temperature: " + fluid.getFluid().getFluidType().getTemperature(fluid)), ChatFormatting.DARK_GRAY));
                tooltip.accept(TextUtils.applyFormat(Component.literal("Viscosity: " + fluid.getFluid().getFluidType().getViscosity(fluid)), ChatFormatting.DARK_GRAY));
                tooltip.accept(TextUtils.applyFormat(Component.literal("NBT Data: " + fluid.getTag()), ChatFormatting.DARK_GRAY));
            }
        }

        if (tankCapacity > 0) { tooltip.accept(TextUtils.applyFormat(Component.literal(fluid.getAmount() + "/" + tankCapacity + "mB"), ChatFormatting.GRAY)); }
        else if (tankCapacity == 0) { tooltip.accept(TextUtils.applyFormat(Component.literal(fluid.getAmount() + "mB"), ChatFormatting.GRAY)); }
    }

    public void draw(GuiGraphics graphics) {
        FluidStack fluid = tank.getFluid();
        float capacity = (float) tank.getCapacity();
        graphics.pose().pushPose();
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        if (!fluid.isEmpty()) {
            int fluidHeight = (int) ((float) area.getHeight() * ((float) fluid.getAmount() / capacity));
            ITGuiHelper.drawRepeatedFluidSpriteGui(buffer, graphics.pose(), fluid, area.getX(), area.getY() + area.getHeight() - fluidHeight, area.getWidth(), fluidHeight);
        }
        int xOff = (area.getWidth() - overlayWidth) / 2;
        int yOff = (area.getHeight() - overlayHeight) / 2;
        RenderType renderType = ITRenderTypes.getGui(overlayTexture);
        ITGuiHelper.drawTexturedRect(buffer.getBuffer(renderType), graphics.pose(), area.getX() + xOff, area.getY() + yOff, overlayWidth, overlayHeight, 256.0F, overlayUMin, overlayUMin + overlayWidth, overlayVMin, overlayVMin + overlayHeight);
        buffer.endBatch();
        graphics.pose().popPose();
    }
}
