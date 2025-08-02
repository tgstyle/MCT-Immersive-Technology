package mctmods.immersivetechnology.core.registration;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import mctmods.immersivetechnology.common.blocks.multiblocks.*;
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

    public static final MultiblockRegistration<ITBoilerLogic.State> BOILER =
            metal(new ITBoilerLogic(), "boiler")
                    .structure(() -> getMBTemplate.apply("boiler"))
                    .gui(ITMenuTypes.BOILER_MENU)
                    .redstone(s -> s.rsState, ITBoilerLogic.REDSTONE_POS)
                    .component(new BoilerProcess())
                    .build();
    public static final MultiblockRegistration<ITSolarTowerLogic.State> SOLAR_TOWER =
            metal(new ITSolarTowerLogic(), "solar_tower")
                    .structure(() -> getMBTemplate.apply("solar_tower"))
                    .build();
    public static final MultiblockRegistration<ITAlternatorLogic.State> ALTERNATOR =
            metal(new ITAlternatorLogic(), "alternator")
                    .structure(() -> getMBTemplate.apply("alternator"))
                    .build();
    public static final MultiblockRegistration<ITSteamTurbineLogic.State> STEAM_TURBINE =
            metal(new ITSteamTurbineLogic(), "steam_turbine")
                    .structure(() -> getMBTemplate.apply("steam_turbine"))
                    .redstone(s -> s.rsState, ITSteamTurbineLogic.REDSTONE_POS)
                    .build();
    public static final MultiblockRegistration<ITGasTurbineLogic.State> GAS_TURBINE =
            metal(new ITGasTurbineLogic(), "gas_turbine")
                    .structure(() -> getMBTemplate.apply("gas_turbine"))
                    .redstone(s -> s.rsState, ITGasTurbineLogic.REDSTONE_POS)
                    .build();
    public static final MultiblockRegistration<ITAdvancedCokeOvenLogic.State> ADVANCED_COKE_OVEN =
            stone(new ITAdvancedCokeOvenLogic(), "advanced_coke_oven", false)
                    .structure(() -> getMBTemplate.apply("advanced_coke_oven"))
                    .gui(ITMenuTypes.ADVANCED_COKE_OVEN_MENU)
                    .build();
    public static final MultiblockRegistration<ITDistillerLogic.State> DISTILLER =
            metal(new ITDistillerLogic(), "distiller")
                    .structure(() -> getMBTemplate.apply("distiller"))
                    .redstone(s -> s.rsState, ITDistillerLogic.REDSTONE_POS)
                    .gui(ITMenuTypes.DISTILLER_MENU)
                    .build();

    public static void init() {
        registerMB("boiler", ITBoiler.INSTANCE, BOILER);
        registerMB("alternator", ITAlternator.INSTANCE, ALTERNATOR);
        registerMB("steam_turbine", ITSteamTurbine.INSTANCE, STEAM_TURBINE);
        registerMB("gas_turbine", ITGasTurbine.INSTANCE, GAS_TURBINE);
        registerMB("advanced_coke_oven", ITAdvancedCokeOven.INSTANCE, ADVANCED_COKE_OVEN);
        registerMB("solar_tower", ITSolarTower.INSTANCE, SOLAR_TOWER);
        registerMB("distiller", ITDistiller.INSTANCE, DISTILLER);
    }

    public static void forceClassLoad() { init(); }
}
