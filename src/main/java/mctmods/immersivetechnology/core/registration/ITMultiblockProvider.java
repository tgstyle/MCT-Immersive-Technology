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
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.helper.ITMultiblockBuilder;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITMultiblockPartBlockWithMirrorState;
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
                .customBlock(ITBlocks.REGISTER, ITItems.REGISTER, r -> new ITMultiblockPartBlockWithMirrorState<>(IEBlocks.METAL_PROPERTIES_NO_OCCLUSION.get(), r), ITBlockItem::new)
                .customBEs(ITBlockEntities.REGISTER);
    }

    public static final MultiblockRegistration<BoilerLogic.State> BOILER =
            metal(new BoilerLogic(), "boiler")
                    .structure(() -> getMBTemplate.apply("boiler"))
                    .gui(ITMenuTypes.BOILER_MENU)
                    .redstone(s -> s.rsState, BoilerLogic.REDSTONE_POS)
                    .component(new BoilerProcess())
                    .build();
    public static final MultiblockRegistration<SolarTowerLogic.State> SOLAR_TOWER =
            metal(new SolarTowerLogic(), "solar_tower")
                    .structure(() -> getMBTemplate.apply("solar_tower"))
                    .build();
    public static final MultiblockRegistration<AlternatorLogic.State> ALTERNATOR =
            metal(new AlternatorLogic(), "alternator")
                    .structure(() -> getMBTemplate.apply("alternator"))
                    .build();
    public static final MultiblockRegistration<SteamTurbineLogic.State> STEAM_TURBINE =
            metal(new SteamTurbineLogic(), "steam_turbine")
                    .structure(() -> getMBTemplate.apply("steam_turbine"))
                    .redstone(s -> s.rsState, SteamTurbineLogic.REDSTONE_POS)
                    .build();
    public static final MultiblockRegistration<GasTurbineLogic.State> GAS_TURBINE =
            metal(new GasTurbineLogic(), "gas_turbine")
                    .structure(() -> getMBTemplate.apply("gas_turbine"))
                    .redstone(s -> s.rsState, GasTurbineLogic.REDSTONE_POS)
                    .build();
    public static final MultiblockRegistration<AdvancedCokeOvenLogic.State> ADVANCED_COKE_OVEN =
            stone(new AdvancedCokeOvenLogic(), "advanced_coke_oven", false)
                    .structure(() -> getMBTemplate.apply("advanced_coke_oven"))
                    .gui(ITMenuTypes.ADVANCED_COKE_OVEN_MENU)
                    .build();
    public static final MultiblockRegistration<DistillerLogic.State> DISTILLER =
            metal(new DistillerLogic(), "distiller")
                    .structure(() -> getMBTemplate.apply("distiller"))
                    .redstone(s -> s.rsState, DistillerLogic.REDSTONE_POS)
                    .gui(ITMenuTypes.DISTILLER_MENU)
                    .build();

    public static void init() {
        registerMB("boiler", Boiler.INSTANCE, BOILER);
        registerMB("alternator", Alternator.INSTANCE, ALTERNATOR);
        registerMB("steam_turbine", SteamTurbine.INSTANCE, STEAM_TURBINE);
        registerMB("gas_turbine", GasTurbine.INSTANCE, GAS_TURBINE);
        registerMB("advanced_coke_oven", AdvancedCokeOven.INSTANCE, ADVANCED_COKE_OVEN);
        registerMB("solar_tower", SolarTower.INSTANCE, SOLAR_TOWER);
        registerMB("distiller", Distiller.INSTANCE, DISTILLER);
    }

    public static void forceClassLoad() { init(); }
}
