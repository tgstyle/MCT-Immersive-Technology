package mctmods.immersivetechnology.client;

import mctmods.immersivetechnology.client.renderer.CokeOvenHeaterRenderer;
import mctmods.immersivetechnology.client.renderer.GasTurbineRenderer;
import mctmods.immersivetechnology.client.renderer.OpenBarrelRenderer;
import mctmods.immersivetechnology.client.renderer.SteamTurbineRenderer;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ITLib.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModBusEventHandlers {
    @SubscribeEvent
    public static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) { }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        registerBlockEntityRenderers(event);
    }

    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        registerBERenderNoContext(event, ITBlockEntities.OPEN_BARREL::get, OpenBarrelRenderer::new);
        registerBERenderNoContext(event, ITBlockEntities.COKE_OVEN_HEATER::master, CokeOvenHeaterRenderer::new);
        registerBERenderNoContext(event, () -> ITMultiblockProvider.GAS_TURBINE.masterBE().get(), GasTurbineRenderer::new);
        registerBERenderNoContext(event, () -> ITMultiblockProvider.STEAM_TURBINE.masterBE().get(), SteamTurbineRenderer::new);
    }

    private static <T extends BlockEntity> void registerBERenderNoContext(EntityRenderersEvent.RegisterRenderers event, Supplier<BlockEntityType<? extends T>> type, Supplier<BlockEntityRenderer<T>> render) { event.registerBlockEntityRenderer(type.get(), ctx -> render.get()); }
}
