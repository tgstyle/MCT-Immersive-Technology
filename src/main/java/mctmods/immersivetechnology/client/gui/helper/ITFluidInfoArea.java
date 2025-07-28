package mctmods.immersivetechnology.client.gui.helper;

import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.common.fluids.PotionFluid;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

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
        FluidStack var10000 = this.tank.getFluid();
        int var10001 = this.tank.getCapacity();
        Objects.requireNonNull(tooltip);
        fillTooltip(var10000, var10001, tooltip::add);
    }

    public static void fillTooltip(FluidStack fluid, int tankCapacity, Consumer<Component> tooltip) {
        if (!fluid.isEmpty()) { tooltip.accept(TextUtils.applyFormat(fluid.getDisplayName(), new ChatFormatting[]{fluid.getFluid().getFluidType().getRarity(fluid).color})); }
        else { tooltip.accept(Component.translatable("gui.immersiveengineering.empty")); }

        Fluid var4 = fluid.getFluid();
        if (var4 instanceof PotionFluid potion) { potion.addInformation(fluid, tooltip); }

        if (ClientUtils.mc().options.advancedItemTooltips && !fluid.isEmpty()) {
            if (!Screen.hasShiftDown()) { tooltip.accept(Component.translatable("desc.immersiveengineering.info.holdShiftForInfo")); }
            else {
                DefaultedRegistry<Fluid> var10001 = BuiltInRegistries.FLUID;
                tooltip.accept(TextUtils.applyFormat(Component.literal("Fluid Registry: " + var10001.getKey(fluid.getFluid())), new ChatFormatting[]{ChatFormatting.DARK_GRAY}));
                tooltip.accept(TextUtils.applyFormat(Component.literal("Density: " + fluid.getFluid().getFluidType().getDensity(fluid)), new ChatFormatting[]{ChatFormatting.DARK_GRAY}));
                tooltip.accept(TextUtils.applyFormat(Component.literal("Temperature: " + fluid.getFluid().getFluidType().getTemperature(fluid)), new ChatFormatting[]{ChatFormatting.DARK_GRAY}));
                tooltip.accept(TextUtils.applyFormat(Component.literal("Viscosity: " + fluid.getFluid().getFluidType().getViscosity(fluid)), new ChatFormatting[]{ChatFormatting.DARK_GRAY}));
                tooltip.accept(TextUtils.applyFormat(Component.literal("NBT Data: " + fluid.getTag()), new ChatFormatting[]{ChatFormatting.DARK_GRAY}));
            }
        }

        if (tankCapacity > 0) {
            int var5 = fluid.getAmount();
            tooltip.accept(TextUtils.applyFormat(Component.literal(var5 + "/" + tankCapacity + "mB"), new ChatFormatting[]{ChatFormatting.GRAY}));
        }
        else if (tankCapacity == 0) { tooltip.accept(TextUtils.applyFormat(Component.literal(fluid.getAmount() + "mB"), new ChatFormatting[]{ChatFormatting.GRAY})); }
    }

    public void draw(GuiGraphics graphics) {
        FluidStack fluid = this.tank.getFluid();
        float capacity = (float)this.tank.getCapacity();
        graphics.pose().pushPose();
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        if (!fluid.isEmpty()) {
            int fluidHeight = (int)((float)this.area.getHeight() * ((float)fluid.getAmount() / capacity));
            GuiHelper.drawRepeatedFluidSpriteGui(buffer, graphics.pose(), fluid, (float)this.area.getX(), (float)(this.area.getY() + this.area.getHeight() - fluidHeight), (float)this.area.getWidth(), (float)fluidHeight);
        }
        int xOff = (this.area.getWidth() - this.overlayWidth) / 2;
        int yOff = (this.area.getHeight() - this.overlayHeight) / 2;
        RenderType renderType = IERenderTypes.getGui(this.overlayTexture);
        GuiHelper.drawTexturedRect(buffer.getBuffer(renderType), graphics.pose(), this.area.getX() + xOff, this.area.getY() + yOff, this.overlayWidth, this.overlayHeight, 256.0F, this.overlayUMin, this.overlayUMin + this.overlayWidth, this.overlayVMin, this.overlayVMin + this.overlayHeight);
        buffer.endBatch();
        graphics.pose().popPose();
    }
}
