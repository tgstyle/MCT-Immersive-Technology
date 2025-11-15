package mctmods.immersivetechnology.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mctmods.immersivetechnology.common.blocks.helper.ITBlockInterfaces;
import mctmods.immersivetechnology.core.lib.ITLib;
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
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;

@Mod.EventBusSubscriber(modid = ITLib.MODID, value = Dist.CLIENT, bus = Bus.FORGE)
public class ITClientEventHandler {
    @SubscribeEvent
    public static void onRenderOverlayPost(RenderGuiOverlayEvent.Post event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.ITEM_NAME.id())) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null) return;

        HitResult mop = mc.hitResult;
        if (!(mop instanceof BlockHitResult blockHit)) return;

        Level level = mc.level;
        if (level == null) return;

        BlockEntity te = level.getBlockEntity(blockHit.getBlockPos());
        Player player = mc.player;
        boolean hammer = false;

        if (te instanceof ITBlockInterfaces.IBlockOverlayText overlay) {
            Component[] text = overlay.getOverlayText(player, mop, hammer);
            if (text != null && text.length > 0) drawOverlayText(event.getGuiGraphics(), text);
        }
    }

    private static void drawOverlayText(GuiGraphics guiGraphics, Component[] text) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(width / 2f, height / 2f + 30, 0); // positioned below vanilla block/item name like IE

        int maxWidth = 0;
        for (Component component : text) {
            String s = component.getString();
            maxWidth = Math.max(maxWidth, font.width(s));
        }

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
