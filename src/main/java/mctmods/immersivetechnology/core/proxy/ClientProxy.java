package mctmods.immersivetechnology.core.proxy;

import blusunrize.immersiveengineering.api.ManualHelper;
import com.immersiveconvergence.api.multiblock.TemplateMultiblock;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.Tree.InnerNode;
import mctmods.immersivetechnology.client.gui.*;
import com.immersiveconvergence.api.client.gui.BaseContainerScreen;
import mctmods.immersivetechnology.client.models.multiblock.RotorModels;
import mctmods.immersivetechnology.client.models.multiblock.SolarReflectorModels;
import mctmods.immersivetechnology.client.models.multiblock.StyleNOXModels;
import com.immersiveconvergence.api.client.StandaloneModel;
import com.immersiveconvergence.api.client.mirror.MirrorModelLoader;
import com.immersiveconvergence.api.client.BaseBlockEntityRenderer;
import com.immersiveconvergence.api.client.ConfigurableSidesModel;
import com.immersiveconvergence.api.fluid.FluidEntry;
import com.immersiveconvergence.api.client.split.SplitModelHandler;
import com.immersiveconvergence.api.particles.CampfireSmokeProvider;
import com.immersiveconvergence.api.particles.ColoredSmokeProvider;
import com.immersiveconvergence.api.particles.SmokeCustomProvider;
import mctmods.immersivetechnology.client.renderer.*;
import com.immersiveconvergence.api.block.IFlagItem;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SteelSheetmetalTankLogic;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.CommonConfig;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.registration.BlockEntities;
import mctmods.immersivetechnology.core.registration.ModBlocks;
import mctmods.immersivetechnology.core.registration.ModFluids;
import mctmods.immersivetechnology.core.registration.ModItems;
import mctmods.immersivetechnology.core.registration.MenuTypes;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import mctmods.immersivetechnology.core.registration.Particles;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.client.gui.screens.Screen;
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

import java.util.List;
import java.util.function.Supplier;
import com.immersiveconvergence.api.particles.ParticleSettings;
import com.immersiveconvergence.api.particles.ColoredSmoke;
import mctmods.immersivetechnology.core.ClientConfig;

@EventBusSubscriber(modid = Reference.MODID, value = Dist.CLIENT)
public class ClientProxy extends CommonProxy {
    private static final List<Vec3i> BASEHEATER_PARTS = List.of(new BlockPos(-1, 0, 0), BlockPos.ZERO, new BlockPos(1, 0, 0));

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        BaseBlockEntityRenderer.viewDistanceModifier = () -> ClientConfig.multiblockSpecialRenderDistanceModifier;
        ParticleSettings.particleCollide = () -> ClientConfig.particleCollide;
        ParticleSettings.coloredSmokeHeight = () -> ClientConfig.coloredSmokeHeight;
        ParticleSettings.customSmokeHeight = () -> ClientConfig.customSmokeHeight;
        ColoredSmoke.typeSupplier = Particles.COLORED_SMOKE::get;
        event.enqueueWork(() -> {
            FluidEntry.registerRenderLayers(ModFluids.ALL_ENTRIES);

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
                case "stylenox.energy_capacity" -> ServerConfig.stylenoxEnergyCapacity;
                case "stylenox.startup_time" -> ServerConfig.stylenoxStartupTime;
                case "barrel_creative.output_amount" -> CommonConfig.creativeBarrelOutputAmount;
                case "fluid_pipe.pressurized" -> ServerConfig.fluidPipeAmountPressurized;
                case "fluid_pipe.unpressurized" -> ServerConfig.fluidPipeAmountUnpressurized;
                default -> null;
            });
            InnerNode<ResourceLocation, ManualEntry> parent_category = instance.getRoot().getOrCreateSubnode(Reference.rl("main"), 99);

            ManualEntry.ManualEntryBuilder builder = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            builder.readFromFile(Reference.rl("intro"));
            instance.addEntry(parent_category, builder.create());

            InnerNode<ResourceLocation, ManualEntry> power_category = parent_category.getOrCreateSubnode(Reference.rl("it_power"), 0);
            InnerNode<ResourceLocation, ManualEntry> processing_category = parent_category.getOrCreateSubnode(Reference.rl("it_processing"), 1);

            ManualEntry.ManualEntryBuilder multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("advanced_coke_oven"));
            instance.addEntry(processing_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("alternator"));
            instance.addEntry(power_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("boiler_liquid"));
            instance.addEntry(power_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("boiler_solid"));
            instance.addEntry(power_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("boiler_tank"));
            instance.addEntry(power_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("cooling_tower"));
            instance.addEntry(power_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("distiller"));
            instance.addEntry(processing_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("electrolytic_crucible_battery"));
            instance.addEntry(processing_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("gas_turbine"));
            instance.addEntry(power_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("heat_exchanger"));
            instance.addEntry(power_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("melting_crucible"));
            instance.addEntry(processing_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("radiator"));
            instance.addEntry(power_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("solar_melter"));
            instance.addEntry(processing_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("solar_reflector"));
            instance.addEntry(power_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("solar_tower"));
            instance.addEntry(power_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("steam_turbine"));
            instance.addEntry(power_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("steel_sheetmetal_tank"));
            instance.addEntry(processing_category, multiblock.create());

            multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            multiblock.readFromFile(Reference.rl("stylenox"));
            instance.addEntry(power_category, multiblock.create());

            ManualEntry.ManualEntryBuilder normalBlock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            normalBlock.readFromFile(Reference.rl("open_barrel"));
            instance.addEntry(parent_category, normalBlock.create());

            normalBlock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            normalBlock.readFromFile(Reference.rl("steel_barrel"));
            instance.addEntry(parent_category, normalBlock.create());

            normalBlock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            normalBlock.readFromFile(Reference.rl("barrel_creative"));
            instance.addEntry(parent_category, normalBlock.create());

            normalBlock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            normalBlock.readFromFile(Reference.rl("valves"));
            instance.addEntry(parent_category, normalBlock.create());

            normalBlock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            normalBlock.readFromFile(Reference.rl("technologists_wrench"));
            instance.addEntry(parent_category, normalBlock.create());

            normalBlock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
            normalBlock.readFromFile(Reference.rl("fluid_pipes"));
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
        event.register(MenuTypes.SOLAR_MELTER_MENU.getType(), SolarScreen::new);
        event.register(MenuTypes.SOLAR_TOWER_MENU.getType(), SolarScreen::new);
        event.register(MenuTypes.STYLENOX_MENU.getType(), StyleNOXScreen::new);
        event.register(MenuTypes.TRASH_ITEM.getType(), TrashItemScreen::new);
        event.register(MenuTypes.VALVE_FLUID.getType(), ValveFluidScreen::new);
        event.register(MenuTypes.VALVE_LIMITER.getType(), ValveLimiterScreen::new);
        event.register(MenuTypes.VALVE_LOAD.getType(), ValveLoadScreen::new);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(Particles.COLORED_SMOKE.get(), ColoredSmokeProvider::new);
        event.registerSpriteSet(Particles.SMOKE_CUSTOM.get(), SmokeCustomProvider::new);
        event.registerSpriteSet(Particles.CAMPFIRE_SMOKE.get(), CampfireSmokeProvider::new);
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
        FluidEntry.registerItemColors(event, ModFluids.ALL_ENTRIES);
    }

    @SubscribeEvent
    public static void onBlockColor(RegisterColorHandlersEvent.Block event) {
        FluidEntry.registerBlockColors(event, ModFluids.ALL_ENTRIES);
    }

    @Override
    public void reinitializeGUI() {
        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen instanceof BaseContainerScreen) { currentScreen.init(Minecraft.getInstance(), currentScreen.width, currentScreen.height); }
    }

    @Override
    public Level getClientWorld() { return Minecraft.getInstance().level; }

    @Override
    public Player getClientPlayer() { return Minecraft.getInstance().player; }

    @SubscribeEvent
    public static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders ev) {
        ev.register(Reference.rl("conf_sides"), ConfigurableSidesModel.Loader.INSTANCE);
        ev.register(Reference.rl("mirror"), MirrorModelLoader.INSTANCE);
        RotorModels.ROTOR = new StandaloneModel(Reference.rl("dynamic/rotor"));
        RotorModels.ROTOR_EAST_WEST = new StandaloneModel(Reference.rl("dynamic/rotor_east_west"));
        SolarReflectorModels.SUPPORT = new StandaloneModel(Reference.rl("dynamic/solar_reflector_support"));
        SolarReflectorModels.MIRROR = new StandaloneModel(Reference.rl("dynamic/solar_reflector_mirror"));
        AdvancedCokeOvenBaseHeaterRenderer.FAN_MODEL = new StandaloneModel(Reference.rl("dynamic/advanced_coke_oven_baseheater_fan"));
        StyleNOXModels.RODS = new StandaloneModel(Reference.rl("dynamic/stylenox_rods"));
        registerSplitModels();
    }

    private static void registerSplitModels() {
        for (TemplateMultiblock template : MultiblockRegistry.MB_TEMPLATE_MAP.values()) {
            if (template instanceof MachineTemplateMultiblock multiblock) { SplitModelHandler.register(multiblock.getBlock(), multiblock.getUniqueName()); }
        }
        SplitModelHandler.register(ModBlocks.Metal.ADVANCED_COKE_OVEN_BASEHEATER.get(), BASEHEATER_PARTS, Direction.SOUTH);
    }

    @SubscribeEvent
    public static void registerRenders(EntityRenderersEvent.RegisterRenderers event) { registerBERenders(event); }

    private static <T extends BlockEntity> void registerBERender(EntityRenderersEvent.RegisterRenderers event, Supplier<BlockEntityType<? extends T>> type, BlockEntityRendererProvider<T> provider) { event.registerBlockEntityRenderer(type.get(), provider); }

    public static void registerBERenders(EntityRenderersEvent.RegisterRenderers event) {
        registerBERender(event, BlockEntities.ADVANCED_COKE_OVEN_BASEHEATER::get, ctx -> new AdvancedCokeOvenBaseHeaterRenderer());
        registerBERender(event, BlockEntities.BARREL_OPEN::get, ctx3 -> new OpenBarrelRenderer());
        registerBERender(event, MultiblockRegistry.STEAM_TURBINE.masterBE(), ctx2 -> new SteamTurbineRenderer());
        registerBERender(event, MultiblockRegistry.GAS_TURBINE.masterBE(), ctx -> new GasTurbineRenderer());
        registerBERender(event, MultiblockRegistry.SOLAR_REFLECTOR.masterBE(), ctx1 -> new SolarReflectorRenderer());
        registerBERender(event, MultiblockRegistry.SOLAR_MELTER.masterBE(), ctx -> new SolarMelterRenderer());
        registerBERender(event, MultiblockRegistry.STEEL_SHEETMETAL_TANK.masterBE(), ctx -> new SteelSheetmetalTankRenderer());
        registerBERender(event, MultiblockRegistry.STYLENOX.masterBE(), ctx -> new StyleNOXRenderer());
    }
}
