package mctmods.immersivetechnology.core.proxy;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.Tree.InnerNode;
import mctmods.immersivetechnology.client.gui.*;
import mctmods.immersivetechnology.client.gui.helper.ContainerScreen;
import mctmods.immersivetechnology.client.models.multiblock.RotorModels;
import mctmods.immersivetechnology.client.models.multiblock.SolarReflectorModels;
import mctmods.immersivetechnology.client.models.ModDynamicModel;
import mctmods.immersivetechnology.client.models.ModelConfigurableSides;
import mctmods.immersivetechnology.client.models.obj.ModObjLoader;
import mctmods.immersivetechnology.client.models.mirror.MirroredModelLoader;
import mctmods.immersivetechnology.client.models.split.SplitModelLoader;
import mctmods.immersivetechnology.client.particles.helper.ColoredSmokeProvider;
import mctmods.immersivetechnology.client.particles.helper.SmokeCustomProvider;
import mctmods.immersivetechnology.client.renderer.*;
import mctmods.immersivetechnology.common.items.helper.IFlagItem;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SteelSheetmetalTankLogic;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.CommonConfig;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.registration.BlockEntities;
import mctmods.immersivetechnology.core.registration.ModFluids;
import mctmods.immersivetechnology.core.registration.ModItems;
import mctmods.immersivetechnology.core.registration.MenuTypes;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import mctmods.immersivetechnology.core.registration.Particles;
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

@EventBusSubscriber(modid = Reference.MODID, value = Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (ModFluids.FluidEntry entry : ModFluids.ALL_ENTRIES) {
                ItemBlockRenderTypes.setRenderLayer(entry.getStill(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(entry.getFlowing(), RenderType.translucent());
            }

            ManualInstance instance = ManualHelper.getManual();

            ManualHelper.addConfigGetter(key -> switch (key) {
                case "advanced_coke_oven.baseheater_energy_consumption" -> ServerConfig.advancedCokeOvenBaseheaterEnergyConsumption;
                case "alternator.alternator_energy_capacity" -> ServerConfig.alternatorEnergyCapacity;
                case "alternator.alternator_max_output" -> ServerConfig.alternatorMaxOutput;
                case "boiler_tank.tank_capacity" -> ServerConfig.boilerTankCapacity;
                case "distiller.energy_capacity" -> ServerConfig.distillerEnergyCapacity;
                case "electrolytic_crucible_battery.energy_capacity" -> ServerConfig.electrolyticCrucibleBatteryEnergyCapacity;
                case "heat_exchanger.energy_capacity" -> ServerConfig.heatExchangerEnergyCapacity;
                case "melting_crucible.energy_capacity" -> ServerConfig.meltingCrucibleEnergyCapacity;
                case "steel_sheetmetal_tank.capacity" -> ServerConfig.steelSheetmetalTankCapacity;
                case "steel_sheetmetal_tank.comparator_height" -> SteelSheetmetalTankLogic.COMPARATOR_HEIGHT;
                case "barrel_creative.output_amount" -> CommonConfig.creativeBarrelOutputAmount;
                default -> null;
            });
            InnerNode<ResourceLocation, ManualEntry> parent_category = instance.getRoot().getOrCreateSubnode(Reference.rl("main"), 99);

            ManualEntry.ManualEntryBuilder builder = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            builder.readFromFile(Reference.rl("intro"));
            instance.addEntry(parent_category, builder.create());

            InnerNode<ResourceLocation, ManualEntry> multiblock_category = parent_category.getOrCreateSubnode(Reference.rl("it_multiblocks"), 0);

            ManualEntry.ManualEntryBuilder multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("advanced_coke_oven"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("alternator"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("boiler_liquid"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("boiler_solid"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("boiler_tank"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("cooling_tower"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("distiller"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("electrolytic_crucible_battery"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("gas_turbine"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("heat_exchanger"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("melting_crucible"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("radiator"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("solar_melter"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("solar_reflector"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("solar_tower"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("steam_turbine"));
            instance.addEntry(multiblock_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("steel_sheetmetal_tank"));
            instance.addEntry(multiblock_category, multiblock.create());

            ManualEntry.ManualEntryBuilder normalBlock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            normalBlock.readFromFile(Reference.rl("open_barrel"));
            instance.addEntry(parent_category, normalBlock.create());

            normalBlock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            normalBlock.readFromFile(Reference.rl("steel_barrel"));
            instance.addEntry(parent_category, normalBlock.create());

            normalBlock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            normalBlock.readFromFile(Reference.rl("barrel_creative"));
            instance.addEntry(parent_category, normalBlock.create());
        });
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(MenuTypes.ADVANCED_COKE_OVEN_MENU.getType(), AdvancedCokeOvenScreen::new);
        event.register(MenuTypes.BOILER_LIQUID_MENU.getType(), BoilerLiquidScreen::new);
        event.register(MenuTypes.BOILER_SOLID_MENU.getType(), BoilerSolidScreen::new);
        event.register(MenuTypes.BOILER_TANK_MENU.getType(), BoilerTankScreen::new);
        event.register(MenuTypes.CRATE_CREATIVE.getType(), CrateCreativeScreen::new);
        event.register(MenuTypes.DISTILLER_MENU.getType(), DistillerScreen::new);
        event.register(MenuTypes.MELTING_CRUCIBLE_MENU.getType(), MeltingCrucibleScreen::new);
        event.register(MenuTypes.ROTOR_CREATIVE.getType(), RotorCreativeScreen::new);
        event.register(MenuTypes.SOLAR_MELTER_MENU.getType(), SolarScreen::new);
        event.register(MenuTypes.SOLAR_TOWER_MENU.getType(), SolarScreen::new);
        event.register(MenuTypes.TRASH_ITEM.getType(), TrashItemScreen::new);
        event.register(MenuTypes.VALVE_FLUID.getType(), ValveFluidScreen::new);
        event.register(MenuTypes.VALVE_LIMITER.getType(), ValveLimiterScreen::new);
        event.register(MenuTypes.VALVE_LOAD.getType(), ValveLoadScreen::new);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(Particles.COLORED_SMOKE.get(), ColoredSmokeProvider::new);
        event.registerSpriteSet(Particles.SMOKE_CUSTOM.get(), SmokeCustomProvider::new);
    }

    @SubscribeEvent
    public static void onItemColor(RegisterColorHandlersEvent.Item event) {
        for (Supplier<? extends Item> holder : ModItems.getItemRegistryMap().values()) {
            Item i = holder.get();
            if (i instanceof IFlagItem) {
                event.register((stack, tintIndex) -> {
                    if (stack.getItem() instanceof IFlagItem type) { return type.getColor(tintIndex); }
                    return 0xffffff;
                }, i);
            }
        }
        for (ModFluids.FluidEntry entry : ModFluids.ALL_ENTRIES) {
            final int tint = entry.tintColor();
            event.register((stack, index) -> { if (index == 1) { return tint; } return -1; }, entry.bucket().get());
        }
    }

    @SubscribeEvent
    public static void onBlockColor(RegisterColorHandlersEvent.Block event) {
        for (ModFluids.FluidEntry entry : ModFluids.ALL_ENTRIES) {
            final int tint = entry.tintColor();
            event.register((state, level, pos, index) -> tint, entry.block().get());
        }
    }

    @Override
    public void reinitializeGUI() {
        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen instanceof ContainerScreen) { currentScreen.init(Minecraft.getInstance(), currentScreen.width, currentScreen.height); }
    }

    @Override
    public Level getClientWorld() { return Minecraft.getInstance().level; }

    @Override
    public Player getClientPlayer() { return Minecraft.getInstance().player; }

    @SubscribeEvent
    public static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders ev) {
        ev.register(Reference.rl("obj"), ModObjLoader.INSTANCE);
        ev.register(ModelConfigurableSides.Loader.NAME, ModelConfigurableSides.Loader.INSTANCE);
        ev.register(SplitModelLoader.LOCATION, SplitModelLoader.INSTANCE);
        ev.register(MirroredModelLoader.ID, MirroredModelLoader.INSTANCE);
        RotorModels.ROTOR = new ModDynamicModel("rotor");
        RotorModels.ROTOR_EAST_WEST = new ModDynamicModel("rotor_east_west");
        SolarReflectorModels.SUPPORT = new ModDynamicModel("solar_reflector_support");
        SolarReflectorModels.MIRROR = new ModDynamicModel("solar_reflector_mirror");
        AdvancedCokeOvenBaseHeaterRenderer.FAN_MODEL = new ModDynamicModel("advanced_coke_oven_baseheater_fan");
    }

    @SubscribeEvent
    public static void registerRenders(EntityRenderersEvent.RegisterRenderers event) { registerBERenders(event); }

    private static <T extends BlockEntity> void registerBERender(EntityRenderersEvent.RegisterRenderers event, Supplier<BlockEntityType<? extends T>> type, BlockEntityRendererProvider<T> provider) { event.registerBlockEntityRenderer(type.get(), provider); }

    public static void registerBERenders(EntityRenderersEvent.RegisterRenderers event) {
        registerBERender(event, BlockEntities.ADVANCED_COKE_OVEN_BASEHEATER::get, ctx -> new AdvancedCokeOvenBaseHeaterRenderer());
        registerBERender(event, BlockEntities.BARREL_OPEN::get, ctx3 -> new OpenBarrelRenderer());
        registerBERender(event, BlockEntities.ROTOR_CREATIVE::get, context -> new RotorCreativeRenderer());
        registerBERender(event, MultiblockRegistry.STEAM_TURBINE.masterBE(), ctx2 -> new SteamTurbineRenderer());
        registerBERender(event, MultiblockRegistry.GAS_TURBINE.masterBE(), ctx -> new GasTurbineRenderer());
        registerBERender(event, MultiblockRegistry.SOLAR_REFLECTOR.masterBE(), ctx1 -> new SolarReflectorRenderer());
        registerBERender(event, MultiblockRegistry.SOLAR_MELTER.masterBE(), ctx -> new SolarMelterRenderer());
        registerBERender(event, MultiblockRegistry.STEEL_SHEETMETAL_TANK.masterBE(), ctx -> new SteelSheetmetalTankRenderer());
    }
}
