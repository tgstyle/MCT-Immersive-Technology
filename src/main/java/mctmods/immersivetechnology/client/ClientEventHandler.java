package mctmods.immersivetechnology.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.immersiveconvergence.api.block.BlockInterfaces;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Reference.MODID, value = Dist.CLIENT, bus = Bus.MOD)
public class ClientEventHandler {

    public static final IGuiOverlay OSD_OVERLAY = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null) { return; }

        HitResult mop = mc.hitResult;
        if (!(mop instanceof BlockHitResult blockHit)) { return; }

        Level level = mc.level;
        if (level == null) { return; }

        BlockEntity te = level.getBlockEntity(blockHit.getBlockPos());
        Player player = mc.player;
        boolean hammer = false;

        if (te instanceof BlockInterfaces.IBlockOverlayText overlay) {
            Component[] text = overlay.getOverlayText(player, mop, hammer);
            if (text != null && text.length > 0) { drawOverlayText(guiGraphics, text); }
        }
    };

    @SubscribeEvent public static void registerOverlays(RegisterGuiOverlaysEvent event) { event.registerAbove(VanillaGuiOverlay.ITEM_NAME.id(), "it_osd", OSD_OVERLAY); }

    private static void drawOverlayText(GuiGraphics guiGraphics, Component[] text) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(width / 2f, height / 2f + 30, 0);

        for (int i = 0; i < text.length; i++) {
            String s = text[i].getString();
            int lineWidth = font.width(s);
            float x = -lineWidth / 2f;
            int y = i * (font.lineHeight + 5);

            guiGraphics.fill((int)x - 4, y - 2, (int)x + lineWidth + 4, y + font.lineHeight + 2, 0xAA000000);
            guiGraphics.drawString(font, s, (int)x, y, 0xFFFFFF, true);
        }

        pose.popPose();
    }
}
