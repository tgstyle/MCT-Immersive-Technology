package mctmods.immersivetechnology.core.registration;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import mctmods.immersivetechnology.common.multiblocks.helper.*;
import mctmods.immersivetechnology.common.multiblocks.metal.*;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.*;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.*;
import mctmods.immersivetechnology.common.multiblocks.metal.process.BoilerLiquidProcess;
import mctmods.immersivetechnology.common.multiblocks.metal.process.BoilerSolidProcess;
import mctmods.immersivetechnology.common.items.helper.ITBlockItem;
import mctmods.immersivetechnology.common.multiblocks.stone.CoolingTower;
import mctmods.immersivetechnology.common.multiblocks.stone.logic.CoolingTowerLogic;
import mctmods.immersivetechnology.common.multiblocks.stone.shapes.CoolingTowerShape;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.List;
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
        if (!solid) { properties.noOcclusion(); }
        return new ITMultiblockBuilder<>(logic, name)
                .notMirrored()
                .customBlock(ITBlocks.REGISTER, ITItems.REGISTER, r -> new ITNonMirrorableWithActiveBlock<>(properties, r), ITBlockItem::new)
                .customBEs(ITBlockEntities.REGISTER);
    }
    public static <S extends IMultiblockState> ITMultiblockBuilder<S> metal(IMultiblockLogic<S> logic, String name) {
        return new ITMultiblockBuilder<>(logic, name)
                .customBlock(ITBlocks.REGISTER, ITItems.REGISTER, r -> new ITMultiblockPartBlockWithMirror<>(ITBlocks.METAL_PROPERTIES_NO_OCCLUSION.get(), r), ITBlockItem::new)
                .customBEs(ITBlockEntities.REGISTER);
    }
    public static <S extends IMultiblockState> ITMultiblockBuilder<S> metalNoMirror(IMultiblockLogic<S> logic, String name) {
        return new ITMultiblockBuilder<>(logic, name)
                .notMirrored()
                .customBlock(ITBlocks.REGISTER, ITItems.REGISTER, r -> new ITMultiblockPartBlockNonMirror<>(ITBlocks.METAL_PROPERTIES_NO_OCCLUSION.get(), r), ITBlockItem::new)
                .customBEs(ITBlockEntities.REGISTER);
    }
    public static <S extends IMultiblockState> ITMultiblockBuilder<S> metalNoMirrorWithActive(IMultiblockLogic<S> logic, String name) {
        return new ITMultiblockBuilder<>(logic, name)
                .notMirrored()
                .customBlock(ITBlocks.REGISTER, ITItems.REGISTER, r -> new ITMultiblockPartBlockNonMirrorActive<>(ITBlocks.METAL_PROPERTIES_NO_OCCLUSION.get(), r), ITBlockItem::new)
                .customBEs(ITBlockEntities.REGISTER);
    }
    public static final MultiblockRegistration<AlternatorLogic.State> ALTERNATOR =
            metalNoMirror(new AlternatorLogic(), "alternator")
                    .structure(() -> getMBTemplate.apply("alternator"))
                    .component(new ITDisassemblyTicker<>(AlternatorShape.DISASSEMBLY_POS), state -> null)
                    .build();
    public static final MultiblockRegistration<BoilerLiquidLogic.State> BOILER_LIQUID =
            metalNoMirror(new BoilerLiquidLogic(), "boiler_liquid")
                    .structure(() -> getMBTemplate.apply("boiler_liquid"))
                    .redstone(s -> s.rsState, BoilerLiquidLogic.REDSTONE_POI)
                    .component(new BoilerLiquidProcess())
                    .component(new ITClearTank<>(BoilerLiquidLogic.FLUID_INPUT_POI, s -> s.tanks.input1().drain(Integer.MAX_VALUE, FluidAction.EXECUTE), Component.translatable(TranslationKey.GUI_INPUT_TANK_CLEARED.getLocation())))
                    .component(new ITDisassemblyTicker<>(BoilerLiquidShape.DISASSEMBLY_POS), state -> null)
                    .gui(ITMenuTypes.BOILER_LIQUID_MENU)
                    .build();
    public static final MultiblockRegistration<BoilerSolidLogic.State> BOILER_SOLID =
            metalNoMirrorWithActive(new BoilerSolidLogic(), "boiler_solid")
                    .structure(() -> getMBTemplate.apply("boiler_solid"))
                    .redstone(s -> s.rsState, BoilerSolidLogic.REDSTONE_POI)
                    .component(new BoilerSolidProcess())
                    .component(new ITDisassemblyTicker<>(BoilerSolidShape.DISASSEMBLY_POS), state -> null)
                    .gui(ITMenuTypes.BOILER_SOLID_MENU)
                    .build();
    public static final MultiblockRegistration<BoilerTankLogic.State> BOILER_TANK =
            metalNoMirror(new BoilerTankLogic(), "boiler_tank")
                    .structure(() -> getMBTemplate.apply("boiler_tank"))
                    .component(new ITClearTank<>(BoilerTankLogic.FLUID_INPUT_POI, s -> s.tanks.input().drain(Integer.MAX_VALUE, FluidAction.EXECUTE), Component.translatable(TranslationKey.GUI_INPUT_TANK_CLEARED.getLocation())))
                    .component(new ITDisassemblyTicker<>(BoilerTankShape.DISASSEMBLY_POS), state -> null)
                    .gui(ITMenuTypes.BOILER_TANK_MENU)
                    .build();
    public static final MultiblockRegistration<CoolingTowerLogic.State> COOLING_TOWER =
            metal(new CoolingTowerLogic(), "cooling_tower")
                    .structure(() -> getMBTemplate.apply("cooling_tower"))
                    .component(new ITClearTank<>(CoolingTowerLogic.FLUID_INPUT_POIS, s -> { s.tanks.input0().drain(Integer.MAX_VALUE, FluidAction.EXECUTE); s.tanks.input1().drain(Integer.MAX_VALUE, FluidAction.EXECUTE); }, Component.translatable(TranslationKey.GUI_INPUT_TANKS_CLEARED.getLocation())))
                    .component(new ITDisassemblyTicker<>(CoolingTowerShape.DISASSEMBLY_POS), state -> null)
                    .build();
    public static final MultiblockRegistration<DistillerLogic.State> DISTILLER =
            metal(new DistillerLogic(), "distiller")
                    .structure(() -> getMBTemplate.apply("distiller"))
                    .redstone(s -> s.rsState, DistillerLogic.REDSTONE_POI)
                    .component(new ITClearTank<>(ImmutableList.of(DistillerLogic.INPUT_FLUID_POI.posInMultiblock()), s -> s.tanks.input().drain(Integer.MAX_VALUE, FluidAction.EXECUTE), Component.translatable(TranslationKey.GUI_INPUT_TANK_CLEARED.getLocation())))
                    .component(new ITDisassemblyTicker<>(DistillerShape.DISASSEMBLY_POS), state -> null)
                    .gui(ITMenuTypes.DISTILLER_MENU)
                    .build();
    public static final MultiblockRegistration<GasTurbineLogic.State> GAS_TURBINE =
            metal(new GasTurbineLogic(), "gas_turbine")
                    .structure(() -> getMBTemplate.apply("gas_turbine"))
                    .redstone(s -> s.rsState, GasTurbineLogic.REDSTONE_POI)
                    .component(new ITClearTank<>(ImmutableList.of(GasTurbineLogic.INPUT_FLUID_POI.posInMultiblock()), s -> s.tanks.input().drain(Integer.MAX_VALUE, FluidAction.EXECUTE), Component.translatable(TranslationKey.GUI_INPUT_TANK_CLEARED.getLocation())))
                    .component(new ITDisassemblyTicker<>(GasTurbineShape.DISASSEMBLY_POS), state -> null)
                    .build();
    public static final MultiblockRegistration<SolarMelterLogic.State> SOLAR_MELTER =
            metalNoMirror(new SolarMelterLogic(), "solar_melter")
                    .structure(() -> getMBTemplate.apply("solar_melter"))
                    .redstone(s -> s.rsState, SolarMelterLogic.REDSTONE_POI)
                    .component(new ITClearTank<>(ImmutableList.of(SolarMelterLogic.INPUT_FLUID_POI.posInMultiblock()), s -> s.tanks.input().drain(Integer.MAX_VALUE, FluidAction.EXECUTE), Component.translatable(TranslationKey.GUI_INPUT_TANK_CLEARED.getLocation())))
                    .component(new ITDisassemblyTicker<>(SolarMelterShape.DISASSEMBLY_POS), state -> null)
                    .gui(ITMenuTypes.SOLAR_MELTER_MENU)
                    .build();
    public static final MultiblockRegistration<SolarReflectorLogic.State> SOLAR_REFLECTOR =
            metalNoMirror(new SolarReflectorLogic(), "solar_reflector")
                    .structure(() -> getMBTemplate.apply("solar_reflector"))
                    .component(new ITDisassemblyTicker<>(SolarReflectorShape.DISASSEMBLY_POS), state -> null)
                    .build();
    public static final MultiblockRegistration<SolarTowerLogic.State> SOLAR_TOWER =
            metalNoMirror(new SolarTowerLogic(), "solar_tower")
                    .structure(() -> getMBTemplate.apply("solar_tower"))
                    .redstone(s -> s.rsState, SolarTowerLogic.REDSTONE_POI)
                    .component(new ITClearTank<>(ImmutableList.of(SolarTowerLogic.INPUT_FLUID_POI.posInMultiblock()), s -> s.tanks.input().drain(Integer.MAX_VALUE, FluidAction.EXECUTE), Component.translatable(TranslationKey.GUI_INPUT_TANK_CLEARED.getLocation())))
                    .component(new ITDisassemblyTicker<>(SolarTowerShape.DISASSEMBLY_POS), state -> null)
                    .gui(ITMenuTypes.SOLAR_TOWER_MENU)
                    .build();
    public static final MultiblockRegistration<SteamTurbineLogic.State> STEAM_TURBINE =
            metal(new SteamTurbineLogic(), "steam_turbine")
                    .structure(() -> getMBTemplate.apply("steam_turbine"))
                    .redstone(s -> s.rsState, SteamTurbineLogic.REDSTONE_POI)
                    .component(new ITClearTank<>(ImmutableList.of(SteamTurbineLogic.INPUT_FLUID_POI.posInMultiblock()), s -> s.tanks.input().drain(Integer.MAX_VALUE, FluidAction.EXECUTE), Component.translatable(TranslationKey.GUI_INPUT_TANK_CLEARED.getLocation())))
                    .component(new ITDisassemblyTicker<>(SteamTurbineShape.DISASSEMBLY_POS), state -> null)
                    .build();
    public static final MultiblockRegistration<SteelSheetmetalTankLogic.State> STEEL_SHEETMETAL_TANK =
            metalNoMirror(new SteelSheetmetalTankLogic(), "steel_sheetmetal_tank")
                    .structure(() -> getMBTemplate.apply("steel_sheetmetal_tank"))
                    .redstone(s -> s.rsState, SteelSheetmetalTankLogic.REDSTONE_POI)
                    .withComparator()
                    .build();

    @SuppressWarnings("unused")
    public static List<Class<? extends Block>> getAllBlockClasses() {
        return List.of(ALTERNATOR.block().get().getClass(), BOILER_LIQUID.block().get().getClass(), BOILER_SOLID.block().get().getClass(), BOILER_TANK.block().get().getClass(), COOLING_TOWER.block().get().getClass(), DISTILLER.block().get().getClass(), GAS_TURBINE.block().get().getClass(), SOLAR_MELTER.block().get().getClass(), SOLAR_REFLECTOR.block().get().getClass(), SOLAR_TOWER.block().get().getClass(), STEAM_TURBINE.block().get().getClass(), STEEL_SHEETMETAL_TANK.block().get().getClass());
    }

    public static void init() {
        registerMB("alternator", Alternator.INSTANCE, ALTERNATOR);
        registerMB("boiler_liquid", BoilerLiquid.INSTANCE, BOILER_LIQUID);
        registerMB("boiler_solid", BoilerSolid.INSTANCE, BOILER_SOLID);
        registerMB("boiler_tank", BoilerTank.INSTANCE, BOILER_TANK);
        registerMB("cooling_tower", CoolingTower.INSTANCE, COOLING_TOWER);
        registerMB("distiller", Distiller.INSTANCE, DISTILLER);
        registerMB("gas_turbine", GasTurbine.INSTANCE, GAS_TURBINE);
        registerMB("solar_melter", SolarMelter.INSTANCE, SOLAR_MELTER);
        registerMB("solar_reflector", SolarReflector.INSTANCE, SOLAR_REFLECTOR);
        registerMB("solar_tower", SolarTower.INSTANCE, SOLAR_TOWER);
        registerMB("steam_turbine", SteamTurbine.INSTANCE, STEAM_TURBINE);
        registerMB("steel_sheetmetal_tank", SteelSheetmetalTank.INSTANCE, STEEL_SHEETMETAL_TANK);
    }

    public static void forceClassLoad() { init(); }
}
