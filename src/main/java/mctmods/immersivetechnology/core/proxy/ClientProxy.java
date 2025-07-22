package mctmods.immersivetechnology.core.proxy;

import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import mctmods.immersivetechnology.client.models.ITDynamicModel;
import mctmods.immersivetechnology.client.renderer.CokeOvenPreheaterRenderer;
import mctmods.immersivetechnology.client.renderer.GasTurbineRenderer;
import mctmods.immersivetechnology.client.renderer.SteamTurbineRenderer;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ITLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientProxy extends CommonProxy {
    @Override
    public void reinitializeGUI() {
        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen instanceof IEContainerScreen) { currentScreen.init(Minecraft.getInstance(), currentScreen.width, currentScreen.height); }
    }

    @Override
    public Level getClientWorld() { return Minecraft.getInstance().level; }

    @Override
    public Player getClientPlayer() { return Minecraft.getInstance().player; }

    @SubscribeEvent
    public static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders ev) {
        CokeOvenPreheaterRenderer.MODEL = new ITDynamicModel(CokeOvenPreheaterRenderer.NAME);
        SteamTurbineRenderer.MODEL = new ITDynamicModel(SteamTurbineRenderer.NAME);
        SteamTurbineRenderer.MODEL_EAST_WEST = new ITDynamicModel(SteamTurbineRenderer.NAME_EAST_WEST);
        GasTurbineRenderer.MODEL = new ITDynamicModel(GasTurbineRenderer.NAME);
        GasTurbineRenderer.MODEL_EAST_WEST = new ITDynamicModel(GasTurbineRenderer.NAME_EAST_WEST);
    }

    @SubscribeEvent
    public static void registerRenders(EntityRenderersEvent.RegisterRenderers event) {
        registerBERenders(event);
        registerEntityRenders(event);
    }

    private static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
    }

    private static <T extends BlockEntity> void registerBERenderNoContext(EntityRenderersEvent.RegisterRenderers event, Supplier<BlockEntityType<? extends T>> type, Supplier<BlockEntityRenderer<T>> render) {
        registerBERenderNoContext(event, type.get(), render);
    }

    private static <T extends BlockEntity> void registerBERenderNoContext(EntityRenderersEvent.RegisterRenderers event, BlockEntityType<? extends T> type, Supplier<BlockEntityRenderer<T>> render) {
        event.registerBlockEntityRenderer(type, $ -> render.get());
    }

    public static void registerBERenders(EntityRenderersEvent.RegisterRenderers event) {
        registerBERenderNoContext(event, ITBlockEntities.COKE_OVEN_PREHEATER.get(), CokeOvenPreheaterRenderer::new);
        registerBERenderNoContext(event, ITMultiblockProvider.STEAM_TURBINE.masterBE(), SteamTurbineRenderer::new);
        registerBERenderNoContext(event, ITMultiblockProvider.GAS_TURBINE.masterBE(), GasTurbineRenderer::new);
    }
}
