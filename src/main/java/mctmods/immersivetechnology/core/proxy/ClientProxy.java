package mctmods.immersivetechnology.core.proxy;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.Tree.InnerNode;
import mctmods.immersivetechnology.client.gui.*;
import mctmods.immersivetechnology.client.gui.helper.ITContainerScreen;
import mctmods.immersivetechnology.client.models.multiblock.RotorModels;
import mctmods.immersivetechnology.client.models.multiblock.SolarReflectorModels;
import mctmods.immersivetechnology.client.models.ITDynamicModel;
import mctmods.immersivetechnology.client.models.ITModelConfigurableSides;
import mctmods.immersivetechnology.client.models.obj.ITObjLoader;
import mctmods.immersivetechnology.client.models.mirror.ITMirroredModelLoader;
import mctmods.immersivetechnology.client.models.split.ITSplitModelLoader;
import mctmods.immersivetechnology.client.particles.helper.ITColoredSmokeProvider;
import mctmods.immersivetechnology.client.particles.helper.ITSmokeCustomProvider;
import mctmods.immersivetechnology.client.renderer.*;
import mctmods.immersivetechnology.common.items.helper.ITIFlagItem;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import mctmods.immersivetechnology.core.registration.ITFluids;
import mctmods.immersivetechnology.core.registration.ITItems;
import mctmods.immersivetechnology.core.registration.ITMenuTypes;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import mctmods.immersivetechnology.core.registration.ITParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.api.distmarker.Dist;

import java.util.function.Supplier;

@EventBusSubscriber(modid = ITLib.MODID, value = Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (ITFluids.FluidEntry entry : ITFluids.ALL_ENTRIES) {
                ItemBlockRenderTypes.setRenderLayer(entry.getStill(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(entry.getFlowing(), RenderType.translucent());
            }

            ManualInstance instance = ManualHelper.getManual();
            InnerNode<ResourceLocation, ManualEntry> parent_category = instance.getRoot().getOrCreateSubnode(ITLib.rl("main"), 99);

            ManualEntry.ManualEntryBuilder builder = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            builder.readFromFile(ITLib.rl("intro"));
            instance.addEntry(parent_category, builder.create());

            InnerNode<ResourceLocation, ManualEntry> multiblock_category = parent_category.getOrCreateSubnode(ITLib.rl("it_multiblocks"), 0);

            ManualEntry.ManualEntryBuilder multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ITLib.rl("advanced_coke_oven"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ITLib.rl("alternator"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ITLib.rl("boiler_liquid"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ITLib.rl("boiler_solid"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ITLib.rl("boiler_tank"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ITLib.rl("cooling_tower"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ITLib.rl("distiller"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ITLib.rl("electrolytic_crucible_battery"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ITLib.rl("gas_turbine"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ITLib.rl("heat_exchanger"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ITLib.rl("melting_crucible"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ITLib.rl("radiator"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ITLib.rl("solar_melter"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ITLib.rl("solar_reflector"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ITLib.rl("solar_tower"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ITLib.rl("steam_turbine"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(ITLib.rl("steel_sheetmetal_tank"));
            instance.addEntry(multiblock_category, multiblock.create());
        });
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ITMenuTypes.ADVANCED_COKE_OVEN_MENU.getType(), AdvancedCokeOvenScreen::new);
        event.register(ITMenuTypes.BOILER_LIQUID_MENU.getType(), BoilerLiquidScreen::new);
        event.register(ITMenuTypes.BOILER_SOLID_MENU.getType(), BoilerSolidScreen::new);
        event.register(ITMenuTypes.BOILER_TANK_MENU.getType(), BoilerTankScreen::new);
        event.register(ITMenuTypes.CRATE_CREATIVE.getType(), CrateCreativeScreen::new);
        event.register(ITMenuTypes.DISTILLER_MENU.getType(), DistillerScreen::new);
        event.register(ITMenuTypes.MELTING_CRUCIBLE_MENU.getType(), MeltingCrucibleScreen::new);
        event.register(ITMenuTypes.ROTOR_CREATIVE.getType(), RotorCreativeScreen::new);
        event.register(ITMenuTypes.SOLAR_MELTER_MENU.getType(), SolarScreen::new);
        event.register(ITMenuTypes.SOLAR_TOWER_MENU.getType(), SolarScreen::new);
        event.register(ITMenuTypes.TRASH_ITEM.getType(), TrashItemScreen::new);
        event.register(ITMenuTypes.VALVE_FLUID.getType(), ValveFluidScreen::new);
        event.register(ITMenuTypes.VALVE_LIMITER.getType(), ValveLimiterScreen::new);
        event.register(ITMenuTypes.VALVE_LOAD.getType(), ValveLoadScreen::new);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ITParticles.COLORED_SMOKE.get(), ITColoredSmokeProvider::new);
        event.registerSpriteSet(ITParticles.SMOKE_CUSTOM.get(), ITSmokeCustomProvider::new);
    }

    @SubscribeEvent
    public static void onItemColor(RegisterColorHandlersEvent.Item event) {
        for (Supplier<? extends Item> holder : ITItems.getItemRegistryMap().values()) {
            Item i = holder.get();
            if (i instanceof ITIFlagItem) {
                event.register((stack, tintIndex) -> {
                    if (stack.getItem() instanceof ITIFlagItem type) { return type.getColor(tintIndex); }
                    return 0xffffff;
                }, i);
            }
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
    public void reinitializeGUI() {
        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen instanceof ITContainerScreen) { currentScreen.init(Minecraft.getInstance(), currentScreen.width, currentScreen.height); }
    }

    @Override
    public Level getClientWorld() { return Minecraft.getInstance().level; }

    @Override
    public Player getClientPlayer() { return Minecraft.getInstance().player; }

    @SubscribeEvent
    public static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders ev) {
        ev.register(ITLib.rl("obj"), ITObjLoader.INSTANCE);
        ev.register(ITModelConfigurableSides.Loader.NAME, ITModelConfigurableSides.Loader.INSTANCE);
        ev.register(ITSplitModelLoader.LOCATION, ITSplitModelLoader.INSTANCE);
        ev.register(ITMirroredModelLoader.ID, ITMirroredModelLoader.INSTANCE);
        RotorModels.ROTOR = new ITDynamicModel("rotor");
        RotorModels.ROTOR_EAST_WEST = new ITDynamicModel("rotor_east_west");
        SolarReflectorModels.SUPPORT = new ITDynamicModel("solar_reflector_support");
        SolarReflectorModels.MIRROR = new ITDynamicModel("solar_reflector_mirror");
        AdvancedCokeOvenBaseHeaterRenderer.FAN_MODEL = new ITDynamicModel("advanced_coke_oven_baseheater_fan");
    }

    @SubscribeEvent
    public static void registerRenders(EntityRenderersEvent.RegisterRenderers event) { registerBERenders(event); }

    private static <T extends BlockEntity> void registerBERender(EntityRenderersEvent.RegisterRenderers event, Supplier<BlockEntityType<? extends T>> type, BlockEntityRendererProvider<T> provider) { event.registerBlockEntityRenderer(type.get(), provider); }

    public static void registerBERenders(EntityRenderersEvent.RegisterRenderers event) {
        registerBERender(event, ITBlockEntities.ADVANCED_COKE_OVEN_BASEHEATER::get, ctx -> new AdvancedCokeOvenBaseHeaterRenderer());
        registerBERender(event, ITBlockEntities.BARREL_OPEN::get, ctx3 -> new OpenBarrelRenderer());
        registerBERender(event, ITBlockEntities.ROTOR_CREATIVE::get, context -> new RotorCreativeRenderer());
        registerBERender(event, ITMultiblockProvider.STEAM_TURBINE.masterBE(), ctx2 -> new SteamTurbineRenderer());
        registerBERender(event, ITMultiblockProvider.GAS_TURBINE.masterBE(), ctx -> new GasTurbineRenderer());
        registerBERender(event, ITMultiblockProvider.SOLAR_REFLECTOR.masterBE(), ctx1 -> new SolarReflectorRenderer());
        registerBERender(event, ITMultiblockProvider.SOLAR_MELTER.masterBE(), ctx -> new SolarMelterRenderer());
        registerBERender(event, ITMultiblockProvider.STEEL_SHEETMETAL_TANK.masterBE(), ctx -> new SteelSheetmetalTankRenderer());
    }
}
