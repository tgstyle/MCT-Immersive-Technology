package mctmods.immersivetechnology.core.registration;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockItem;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.NonMirrorableWithActiveBlock;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import mctmods.immersivetechnology.common.blocks.multiblocks.*;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.*;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.helper.ITMultiblockBuilder;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public class ITMultiblockProvider {
    public static HashMap<String, MultiblockRegistration<?>> MB_REGISTRY_MAP = new HashMap<>();
    public static final HashMap<String, TemplateMultiblock> MB_TEMPLATE_MAP = new HashMap<>();
    public static Function<String, MultiblockRegistration<?>> getMB = MB_REGISTRY_MAP::get;
    public static Function<String, TemplateMultiblock> getMBTemplate = MB_TEMPLATE_MAP::get;

    private static <T extends MultiblockHandler.IMultiblock>
    T registerMultiblock(T multiblock) {
        MultiblockHandler.registerMultiblock(multiblock);
        return multiblock;
    }

    private static void registerMB(String registry_name, ITTemplateMultiblock block, MultiblockRegistration<?> registration) {
        registerMultiblockTemplate(registry_name, block);
        MB_REGISTRY_MAP.put(registry_name, registration);
    }

    public static void registerMultiblockTemplate(String registry_name, TemplateMultiblock template) {
        MB_TEMPLATE_MAP.put(registry_name, registerMultiblock(template));
    }

    public static <S extends IMultiblockState> MultiblockRegistration<S> registerMetalMultiblock(String name, IMultiblockLogic<S> logic, Supplier<TemplateMultiblock> structure) {
        return registerMetalMultiblock(name, logic, structure, null);
    }

    public static <S extends IMultiblockState> MultiblockRegistration<S> registerMetalMultiblock(String name, IMultiblockLogic<S> logic, Supplier<TemplateMultiblock> structure, @Nullable Consumer<ITMultiblockBuilder<S>> extras) {
        BlockBehaviour.Properties prop = BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL)
                .strength(3, 15)
                .requiresCorrectToolForDrops()
                .isViewBlocking((state, blockReader, pos) -> false)
                .noOcclusion()
                .dynamicShape()
                .pushReaction(PushReaction.BLOCK);

        return registerMultiblock(name, logic, structure, extras, prop);
    }

    public static <S extends IMultiblockState> MultiblockRegistration<S> registerMultiblock(String name, IMultiblockLogic<S> logic, Supplier<TemplateMultiblock> structure, @Nullable Consumer<ITMultiblockBuilder<S>> extras, BlockBehaviour.Properties prop) {
        ITMultiblockBuilder<S> builder = new ITMultiblockBuilder<>(logic, name)
                .structure(structure)
                .defaultBEs(ITBlockEntities.REGISTER)
                .defaultBlock(ITBlocks.REGISTER, ITItems.REGISTER, prop);

        if (extras != null) { extras.accept(builder); }
        return builder.build();
    }

    public static <S extends IMultiblockState>
    ITMultiblockBuilder<S> stone(IMultiblockLogic<S> logic, String name, boolean solid) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .strength(2, 20);
        if (!solid)
            properties.noOcclusion();
        return new ITMultiblockBuilder<>(logic, name)
                .notMirrored()
                .customBlock(
                        ITBlocks.REGISTER, ITItems.REGISTER,
                        r -> new NonMirrorableWithActiveBlock<>(properties, r),
                        MultiblockItem::new
                )
                .defaultBEs(ITBlockEntities.REGISTER);
    }

    public static <S extends IMultiblockState>
    ITMultiblockBuilder<S> metal(IMultiblockLogic<S> logic, String name) {
        return new ITMultiblockBuilder<>(logic, name)
                .defaultBEs(ITBlockEntities.REGISTER)
                .defaultBlock(ITBlocks.REGISTER, ITItems.REGISTER, IEBlocks.METAL_PROPERTIES_NO_OCCLUSION.get());
    }

    public static final MultiblockRegistration<ITBoilerLogic.State> BOILER =
            metal(new ITBoilerLogic(), "boiler")
                    .structure(() -> getMBTemplate.apply("boiler"))
                    .gui(ITMenuTypes.BOILER_MENU)
                    .redstone(s -> s.rsState, ITBoilerLogic.REDSTONE_POS)
                    .build();
    public static final MultiblockRegistration<ITSolarTowerLogic.State> SOLAR_TOWER =
            metal(new ITSolarTowerLogic(), "solar_tower")
                    .structure(() -> getMBTemplate.apply("solar_tower"))
                    //.gui(ITMenuTypes.SOLAR_TOWER_MENU)
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
    public static final MultiblockRegistration<ITAdvancedCokeOvenLogic.State> ADV_COKE_OVEN =
            stone(new ITAdvancedCokeOvenLogic(), "coke_oven_advanced", false)
                    .structure(() -> getMBTemplate.apply("coke_oven_advanced"))
                    .gui(ITMenuTypes.ADVANCED_COKE_OVEN_MENU)
                    .build();
    public static final MultiblockRegistration<ITAdvancedCokeOvenLogic.State> DISTILLER =
            stone(new ITAdvancedCokeOvenLogic(), "distiller", false)
                    .structure(() -> getMBTemplate.apply("distiller"))
                    .build();

    public static void init() {
        registerMB("boiler", ITBoiler.INSTANCE, BOILER);
        registerMB("alternator", ITAlternator.INSTANCE, ALTERNATOR);
        registerMB("steam_turbine", ITSteamTurbine.INSTANCE, STEAM_TURBINE);
        registerMB("gas_turbine", ITGasTurbine.INSTANCE, GAS_TURBINE);
        registerMB("coke_oven_advanced", ITAdvancedCokeOven.INSTANCE, ADV_COKE_OVEN);
        registerMB("solar_tower", ITSolarTower.INSTANCE, SOLAR_TOWER);
    }

    public static void forceClassLoad() {
        init();
    }
}
