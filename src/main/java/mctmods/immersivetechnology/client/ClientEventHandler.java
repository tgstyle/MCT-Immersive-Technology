package mctmods.immersivetechnology.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mctmods.immersivetechnology.common.blocks.helper.BlockInterfaces;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Reference.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.SELECTED_ITEM_NAME, ResourceLocation.fromNamespaceAndPath(Reference.MODID, "it_osd"), (guiGraphics, deltaTracker) -> {
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
        });
    }

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
