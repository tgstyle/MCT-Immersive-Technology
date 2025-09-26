package mctmods.immersivetechnology.core.proxy;

import mctmods.immersivetechnology.client.gui.*;
import org.jetbrains.annotations.Nullable;
import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.Tree.InnerNode;
import mctmods.immersivetechnology.client.models.helper.ITDynamicModel;
import mctmods.immersivetechnology.client.models.helper.ITObjLoader;
import mctmods.immersivetechnology.client.models.RotorModels;
import mctmods.immersivetechnology.client.models.SolarReflectorModels;
import mctmods.immersivetechnology.client.particles.helper.ITColoredSmokeProvider;
import mctmods.immersivetechnology.client.particles.helper.ITSmokeCustomProvider;
import mctmods.immersivetechnology.client.renderer.GasTurbineRenderer;
import mctmods.immersivetechnology.client.renderer.OpenBarrelRenderer;
import mctmods.immersivetechnology.client.renderer.SolarMelterRenderer;
import mctmods.immersivetechnology.client.renderer.SolarReflectorRenderer;
import mctmods.immersivetechnology.client.renderer.SteamTurbineRenderer;
import mctmods.immersivetechnology.common.blocks.helper.ITBlockType;
import mctmods.immersivetechnology.common.items.helper.ITFlagItem;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import mctmods.immersivetechnology.core.registration.ITFluids;
import mctmods.immersivetechnology.core.registration.ITItems;
import mctmods.immersivetechnology.core.registration.ITMenuTypes;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import mctmods.immersivetechnology.core.registration.ITParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
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

import java.util.function.Supplier;

@SuppressWarnings("unused")
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

            BlockEntityRenderers.register(ITBlockEntities.OPEN_BARREL.get(), context -> new OpenBarrelRenderer());

            MenuScreens.register(ITMenuTypes.BOILER_LIQUID_MENU.getType(), BoilerLiquidScreen::new);
            MenuScreens.register(ITMenuTypes.BOILER_SOLID_MENU.getType(), BoilerSolidScreen::new);
            MenuScreens.register(ITMenuTypes.BOILER_TANK_MENU.getType(), BoilerTankScreen::new);
            MenuScreens.register(ITMenuTypes.DISTILLER_MENU.getType(), DistillerScreen::new);
            MenuScreens.register(ITMenuTypes.TRASH_ITEM.getType(), TrashItemScreen::new);
            MenuScreens.register(ITMenuTypes.SOLAR_MELTER_MENU.getType(), SolarScreen::new);
            MenuScreens.register(ITMenuTypes.SOLAR_TOWER_MENU.getType(), SolarScreen::new);

            ManualInstance instance = ManualHelper.getManual();
            InnerNode<ResourceLocation, ManualEntry> parent_category = instance.getRoot().getOrCreateSubnode(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "main"), 99);
            ManualEntry.ManualEntryBuilder builder = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            builder.readFromFile(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "intro"));
            instance.addEntry(parent_category, builder.create());
            InnerNode<ResourceLocation, ManualEntry> multiblock_category = parent_category.getOrCreateSubnode(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "it_multiblocks"), 0);
            ManualEntry.ManualEntryBuilder multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "alternator"));
            instance.addEntry(multiblock_category, multiblock.create());
            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "boiler_liquid"));
            instance.addEntry(multiblock_category, multiblock.create());
            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "boiler_solid"));
            instance.addEntry(multiblock_category, multiblock.create());
            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "boiler_tank"));
            instance.addEntry(multiblock_category, multiblock.create());
            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "cooling_tower"));
            instance.addEntry(multiblock_category, multiblock.create());
            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "distiller"));
            instance.addEntry(multiblock_category, multiblock.create());
            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "gas_turbine"));
            instance.addEntry(multiblock_category, multiblock.create());
            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "solar_melter"));
            instance.addEntry(multiblock_category, multiblock.create());
            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "solar_reflector"));
            instance.addEntry(multiblock_category, multiblock.create());
            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "solar_tower"));
            instance.addEntry(multiblock_category, multiblock.create());
            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "steam_turbine"));
            instance.addEntry(multiblock_category, multiblock.create());
        });
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ITParticles.COLORED_SMOKE.get(), ITColoredSmokeProvider::new);
        event.registerSpriteSet(ITParticles.SMOKE_CUSTOM.get(), ITSmokeCustomProvider::new);
    }

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
        RotorModels.ROTOR = new ITDynamicModel("rotor");
        RotorModels.ROTOR_EAST_WEST = new ITDynamicModel("rotor_east_west");
        SolarReflectorModels.SUPPORT = new ITDynamicModel("solar_reflector_support");
        SolarReflectorModels.MIRROR = new ITDynamicModel("solar_reflector_mirror");
    }

    @SubscribeEvent
    public static void registerRenders(EntityRenderersEvent.RegisterRenderers event) { registerBERenders(event); }

    private static <T extends BlockEntity> void registerBERenderNoContext(EntityRenderersEvent.RegisterRenderers event, Supplier<BlockEntityType<? extends T>> type, Supplier<BlockEntityRenderer<T>> render) { event.registerBlockEntityRenderer(type.get(), $ -> render.get()); }

    private static <T extends BlockEntity> void registerBERenderNoContext(EntityRenderersEvent.RegisterRenderers event, BlockEntityType<? extends T> type, Supplier<BlockEntityRenderer<T>> render) { event.registerBlockEntityRenderer(type, $ -> render.get()); }

    public static void registerBERenders(EntityRenderersEvent.RegisterRenderers event) {
        registerBERenderNoContext(event, ITBlockEntities.OPEN_BARREL::get, OpenBarrelRenderer::new);
        registerBERenderNoContext(event, ITMultiblockProvider.STEAM_TURBINE.masterBE(), SteamTurbineRenderer::new);
        registerBERenderNoContext(event, ITMultiblockProvider.GAS_TURBINE.masterBE(), GasTurbineRenderer::new);
        registerBERenderNoContext(event, ITMultiblockProvider.SOLAR_REFLECTOR.masterBE(), SolarReflectorRenderer::new);
        registerBERenderNoContext(event, ITMultiblockProvider.SOLAR_MELTER.masterBE(), SolarMelterRenderer::new);
    }
}
