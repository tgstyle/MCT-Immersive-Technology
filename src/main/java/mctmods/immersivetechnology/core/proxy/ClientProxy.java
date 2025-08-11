package mctmods.immersivetechnology.core.proxy;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import blusunrize.immersiveengineering.client.gui.IEContainerScreen;

import mctmods.immersivetechnology.client.models.ITDynamicModel;
import mctmods.immersivetechnology.client.models.ITObjLoader;
import mctmods.immersivetechnology.client.models.RotorModels;
import mctmods.immersivetechnology.client.particles.ColoredSmokeParticleProvider;
import mctmods.immersivetechnology.client.renderer.CokeOvenHeaterRenderer;
import mctmods.immersivetechnology.client.renderer.GasTurbineRenderer;
import mctmods.immersivetechnology.client.renderer.OpenBarrelRenderer;
import mctmods.immersivetechnology.client.renderer.SteamTurbineRenderer;
import mctmods.immersivetechnology.common.blocks.helper.ITBlockType;
import mctmods.immersivetechnology.common.items.helper.ITFlagItem;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import mctmods.immersivetechnology.core.registration.ITFluids;
import mctmods.immersivetechnology.core.registration.ITItems;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import mctmods.immersivetechnology.core.registration.ITParticles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ITLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientProxy extends CommonProxy implements ItemColor, BlockColor {
    public static final ClientProxy INSTANCE = new ClientProxy();

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (ITFluids.FluidEntry entry : ITFluids.ALL_ENTRIES) {
                ItemBlockRenderTypes.setRenderLayer(entry.getStill(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(entry.getFlowing(), RenderType.translucent());
            }
        });
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) { event.registerSpriteSet(ITParticles.COLORED_SMOKE.get(), ColoredSmokeParticleProvider::new); }

    @SubscribeEvent
    public static void onItemColor(RegisterColorHandlersEvent.Item event) {
        for (RegistryObject<? extends Item> holder : ITItems.getItemRegistryMap().values()) {
            Item i = holder.get();
            if (i instanceof ITFlagItem) { event.register(INSTANCE, i); }
        }
        for (ITFluids.FluidEntry entry : ITFluids.ALL_ENTRIES) {
            final int tint = entry.tintColor();
            event.register((stack, index) -> { if (index == 1) { return tint; } return -1; }, entry.bucket().get());
        }
    }

    @SubscribeEvent
    public static void onBlockColor(RegisterColorHandlersEvent.Block event) {
        for (ITFluids.FluidEntry entry : ITFluids.ALL_ENTRIES) {
            final int tint = entry.tintColor();
            event.register((state, level, pos, index) -> tint, entry.block().get());
        }
    }

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter getter, @Nullable BlockPos pos, int index) {
        if (state.getBlock() instanceof ITBlockType type) { return type.getColor(index); }
        return 0xffffff;
    }

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if (stack.getItem() instanceof ITFlagItem type) { return type.getColor(tintIndex); }
        return 0xffffff;
    }

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
    public static void registerRenders(EntityRenderersEvent.RegisterRenderers event) { registerBERenders(event); }

    private static <T extends BlockEntity> void registerBERenderNoContext(EntityRenderersEvent.RegisterRenderers event, Supplier<BlockEntityType<? extends T>> type, Supplier<BlockEntityRenderer<T>> render) { event.registerBlockEntityRenderer(type.get(), $ -> render.get()); }

    private static <T extends BlockEntity> void registerBERenderNoContext(EntityRenderersEvent.RegisterRenderers event, BlockEntityType<? extends T> type, Supplier<BlockEntityRenderer<T>> render) { event.registerBlockEntityRenderer(type, $ -> render.get()); }

    public static void registerBERenders(EntityRenderersEvent.RegisterRenderers event) {
        registerBERenderNoContext(event, ITBlockEntities.OPEN_BARREL::get, OpenBarrelRenderer::new);
        registerBERenderNoContext(event, ITBlockEntities.COKE_OVEN_HEATER.master(), CokeOvenHeaterRenderer::new);
        registerBERenderNoContext(event, ITMultiblockProvider.STEAM_TURBINE.masterBE(), SteamTurbineRenderer::new);
        registerBERenderNoContext(event, ITMultiblockProvider.GAS_TURBINE.masterBE(), GasTurbineRenderer::new);
    }
}
