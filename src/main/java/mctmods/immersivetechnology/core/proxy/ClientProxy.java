package mctmods.immersivetechnology.core.proxy;

import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import mctmods.immersivetechnology.client.models.ITDynamicModel;
import mctmods.immersivetechnology.client.models.ITObjLoader;
import mctmods.immersivetechnology.client.models.RotorModels;
import mctmods.immersivetechnology.client.renderer.CokeOvenHeaterRenderer;
import mctmods.immersivetechnology.client.renderer.GasTurbineRenderer;
import mctmods.immersivetechnology.client.renderer.OpenBarrelRenderer;
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
        ev.register("obj", ITObjLoader.INSTANCE);
        CokeOvenHeaterRenderer.MODEL = new ITDynamicModel(CokeOvenHeaterRenderer.NAME);
        RotorModels.ROTOR = new ITDynamicModel("rotor");
        RotorModels.ROTOR_EAST_WEST = new ITDynamicModel("rotor_east_west");
    }

    @SubscribeEvent
    public static void registerRenders(EntityRenderersEvent.RegisterRenderers event) {
        registerBERenders(event);
    }

    private static <T extends BlockEntity> void registerBERenderNoContext(EntityRenderersEvent.RegisterRenderers event, Supplier<BlockEntityType<? extends T>> type, Supplier<BlockEntityRenderer<T>> render) {
        event.registerBlockEntityRenderer(type.get(), $ -> render.get());
    }

    private static <T extends BlockEntity> void registerBERenderNoContext(EntityRenderersEvent.RegisterRenderers event, BlockEntityType<? extends T> type, Supplier<BlockEntityRenderer<T>> render) {
        event.registerBlockEntityRenderer(type, $ -> render.get());
    }

    public static void registerBERenders(EntityRenderersEvent.RegisterRenderers event) {
        registerBERenderNoContext(event, ITBlockEntities.OPEN_BARREL::get, OpenBarrelRenderer::new);
        registerBERenderNoContext(event, ITBlockEntities.COKE_OVEN_HEATER.master(), CokeOvenHeaterRenderer::new);
        registerBERenderNoContext(event, ITMultiblockProvider.STEAM_TURBINE.masterBE(), SteamTurbineRenderer::new);
        registerBERenderNoContext(event, ITMultiblockProvider.GAS_TURBINE.masterBE(), GasTurbineRenderer::new);
    }
}
