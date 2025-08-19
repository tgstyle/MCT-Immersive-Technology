package mctmods.immersivetechnology.core.registration;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import mctmods.immersivetechnology.common.blocks.multiblocks.*;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.*;
import mctmods.immersivetechnology.common.blocks.multiblocks.process.BoilerProcess;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITMultiblockBuilder;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITMultiblockPartBlockWithMirror;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITNonMirrorableWithActiveBlock;
import mctmods.immersivetechnology.common.items.helper.ITBlockItem;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

import java.util.HashMap;
import java.util.function.Function;

public class ITMultiblockProvider {
    public static HashMap<String, MultiblockRegistration<?>> MB_REGISTRY_MAP = new HashMap<>();
    public static final HashMap<String, TemplateMultiblock> MB_TEMPLATE_MAP = new HashMap<>();
    public static Function<String, TemplateMultiblock> getMBTemplate = MB_TEMPLATE_MAP::get;

    private static <T extends MultiblockHandler.IMultiblock> T registerMultiblock(T multiblock) { MultiblockHandler.registerMultiblock(multiblock); return multiblock; }

    private static void registerMB(String registry_name, ITTemplateMultiblock block, MultiblockRegistration<?> registration) { registerMultiblockTemplate(registry_name, block); MB_REGISTRY_MAP.put(registry_name, registration); }

    public static void registerMultiblockTemplate(String registry_name, TemplateMultiblock template) { MB_TEMPLATE_MAP.put(registry_name, registerMultiblock(template)); }

    public static <S extends IMultiblockState> ITMultiblockBuilder<S> stone(IMultiblockLogic<S> logic, String name, boolean solid) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .strength(2, 20);
        if (!solid) properties.noOcclusion();
        return new ITMultiblockBuilder<>(logic, name)
                .notMirrored()
                .customBlock(ITBlocks.REGISTER, ITItems.REGISTER, r -> new ITNonMirrorableWithActiveBlock<>(properties, r), ITBlockItem::new)
                .customBEs(ITBlockEntities.REGISTER);
    }

    public static <S extends IMultiblockState> ITMultiblockBuilder<S> metal(IMultiblockLogic<S> logic, String name) {
        return new ITMultiblockBuilder<>(logic, name)
                .customBlock(ITBlocks.REGISTER, ITItems.REGISTER, r -> new ITMultiblockPartBlockWithMirror<>(IEBlocks.METAL_PROPERTIES_NO_OCCLUSION.get(), r), ITBlockItem::new)
                .customBEs(ITBlockEntities.REGISTER);
    }

    public static final MultiblockRegistration<AdvancedCokeOvenLogic.State> ADVANCED_COKE_OVEN =
            stone(new AdvancedCokeOvenLogic(), "advanced_coke_oven", false)
                    .structure(() -> getMBTemplate.apply("advanced_coke_oven"))
                    .gui(ITMenuTypes.ADVANCED_COKE_OVEN_MENU)
                    .build();
    public static final MultiblockRegistration<AlternatorLogic.State> ALTERNATOR =
            metal(new AlternatorLogic(), "alternator")
                    .structure(() -> getMBTemplate.apply("alternator"))
                    .build();
    public static final MultiblockRegistration<BoilerLogic.State> BOILER =
            metal(new BoilerLogic(), "boiler")
                    .structure(() -> getMBTemplate.apply("boiler"))
                    .gui(ITMenuTypes.BOILER_MENU)
                    .redstone(s -> s.rsState, BoilerLogic.REDSTONE_POI)
                    .component(new BoilerProcess())
                    .build();
    public static final MultiblockRegistration<DistillerLogic.State> DISTILLER =
            metal(new DistillerLogic(), "distiller")
                    .structure(() -> getMBTemplate.apply("distiller"))
                    .redstone(s -> s.rsState, DistillerLogic.REDSTONE_POI)
                    .gui(ITMenuTypes.DISTILLER_MENU)
                    .build();
    public static final MultiblockRegistration<GasTurbineLogic.State> GAS_TURBINE =
            metal(new GasTurbineLogic(), "gas_turbine")
                    .structure(() -> getMBTemplate.apply("gas_turbine"))
                    .redstone(s -> s.rsState, GasTurbineLogic.REDSTONE_POI)
                    .build();
    public static final MultiblockRegistration<SolarReflectorLogic.State> SOLAR_REFLECTOR =
            metal(new SolarReflectorLogic(), "solar_reflector")
                    .structure(() -> getMBTemplate.apply("solar_reflector"))
                    .build();
    public static final MultiblockRegistration<SolarMelterLogic.State> SOLAR_MELTER =
            metal(new SolarMelterLogic(), "solar_melter")
                    .structure(() -> getMBTemplate.apply("solar_melter"))
                    .redstone(s -> s.rsState, SolarTowerLogic.REDSTONE_POI)
                    .gui(ITMenuTypes.SOLAR_MELTER_MENU)
                    .build();
    public static final MultiblockRegistration<SolarTowerLogic.State> SOLAR_TOWER =
            metal(new SolarTowerLogic(), "solar_tower")
                    .structure(() -> getMBTemplate.apply("solar_tower"))
                    .redstone(s -> s.rsState, SolarTowerLogic.REDSTONE_POI)
                    .gui(ITMenuTypes.SOLAR_TOWER_MENU)
                    .build();
    public static final MultiblockRegistration<SteamTurbineLogic.State> STEAM_TURBINE =
            metal(new SteamTurbineLogic(), "steam_turbine")
                    .structure(() -> getMBTemplate.apply("steam_turbine"))
                    .redstone(s -> s.rsState, SteamTurbineLogic.REDSTONE_POI)
                    .build();

    public static void init() {
        registerMB("advanced_coke_oven", AdvancedCokeOven.INSTANCE, ADVANCED_COKE_OVEN);
        registerMB("alternator", Alternator.INSTANCE, ALTERNATOR);
        registerMB("boiler", Boiler.INSTANCE, BOILER);
        registerMB("distiller", Distiller.INSTANCE, DISTILLER);
        registerMB("gas_turbine", GasTurbine.INSTANCE, GAS_TURBINE);
        registerMB("solar_melter", SolarMelter.INSTANCE, SOLAR_MELTER);
        registerMB("solar_reflector", SolarReflector.INSTANCE, SOLAR_REFLECTOR);
        registerMB("solar_tower", SolarTower.INSTANCE, SOLAR_TOWER);
        registerMB("steam_turbine", SteamTurbine.INSTANCE, STEAM_TURBINE);
    }

    public static void forceClassLoad() { init(); }
}
