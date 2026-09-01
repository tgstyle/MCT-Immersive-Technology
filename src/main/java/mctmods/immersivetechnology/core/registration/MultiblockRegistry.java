package mctmods.immersivetechnology.core.registration;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import com.immersiveconvergence.api.multiblock.ClearTank;
import mctmods.immersivetechnology.common.multiblocks.metal.*;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.*;
import mctmods.immersivetechnology.common.multiblocks.metal.process.BoilerLiquidProcess;
import mctmods.immersivetechnology.common.multiblocks.metal.process.BoilerSolidProcess;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.*;
import mctmods.immersivetechnology.common.multiblocks.stone.AdvancedCokeOven;
import mctmods.immersivetechnology.common.multiblocks.stone.CoolingTower;
import mctmods.immersivetechnology.common.multiblocks.stone.logic.AdvancedCokeOvenLogic;
import mctmods.immersivetechnology.common.multiblocks.stone.logic.CoolingTowerLogic;
import com.immersiveconvergence.api.block.ModBlockItem;
import mctmods.immersivetechnology.core.util.TranslationKey;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.multiblock.MultiblockPartBlockNonMirror;
import com.immersiveconvergence.api.multiblock.MultiblockPartBlockWithMirror;
import com.immersiveconvergence.api.multiblock.MultiblockPartBlockNonMirrorActive;
import com.immersiveconvergence.api.multiblock.MultiblockBuilder;
import mctmods.immersivetechnology.common.multiblocks.gui.helper.MultiblockGui;
import mctmods.immersivetechnology.core.lib.Reference;

public class MultiblockRegistry {
    public static HashMap<String, MultiblockRegistration<?>> MB_REGISTRY_MAP = new HashMap<>();
    public static final HashMap<String, TemplateMultiblock> MB_TEMPLATE_MAP = new HashMap<>();
    public static Function<String, TemplateMultiblock> getMBTemplate = MB_TEMPLATE_MAP::get;

    private static <T extends MultiblockHandler.IMultiblock> T registerMultiblock(T multiblock) { return com.immersiveconvergence.api.multiblock.MultiblockRegistry.register(multiblock); }

    private static void registerMB(String registry_name, MachineTemplateMultiblock block, MultiblockRegistration<?> registration) { registerMultiblockTemplate(registry_name, block); MB_REGISTRY_MAP.put(registry_name, registration); }

    public static void registerMultiblockTemplate(String registry_name, TemplateMultiblock template) { MB_TEMPLATE_MAP.put(registry_name, registerMultiblock(template)); }

    private static BlockBehaviour.Properties multiblockBaseProperties() {
        return BlockBehaviour.Properties.of().strength(2, 20).noOcclusion();
    }

    private static BlockBehaviour.Properties stoneProperties() {
        return multiblockBaseProperties().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM);
    }

    private static <S extends IMultiblockState> MultiblockBuilder<S> base(IMultiblockLogic<S> logic, String name) {
        return new MultiblockBuilder<>(logic, Reference.rl(name));
    }

    private static <S extends IMultiblockState> MultiblockBuilder<S> complete(MultiblockBuilder<S> builder) {
        return builder.customBEs(BlockEntities.REGISTER);
    }

    public static <S extends IMultiblockState> MultiblockBuilder<S> stoneNoMirror(IMultiblockLogic<S> logic, String name) {
        BlockBehaviour.Properties properties = stoneProperties();
        return complete(base(logic, name).notMirrored().customBlock(ModBlocks.REGISTER, ModItems.REGISTER, r -> new MultiblockPartBlockNonMirror<>(properties, r), ModBlockItem::new));
    }

    public static <S extends IMultiblockState> MultiblockBuilder<S> metal(IMultiblockLogic<S> logic, String name) {
        return complete(base(logic, name).customBlock(ModBlocks.REGISTER, ModItems.REGISTER, r -> new MultiblockPartBlockWithMirror<>(ModBlocks.METAL_PROPERTIES_NO_OCCLUSION.get(), r), ModBlockItem::new));
    }

    public static <S extends IMultiblockState> MultiblockBuilder<S> metalNoMirror(IMultiblockLogic<S> logic, String name) {
        return complete(base(logic, name).notMirrored().customBlock(ModBlocks.REGISTER, ModItems.REGISTER, r -> new MultiblockPartBlockNonMirror<>(ModBlocks.METAL_PROPERTIES_NO_OCCLUSION.get(), r), ModBlockItem::new));
    }

    public static <S extends IMultiblockState> MultiblockBuilder<S> metalNoMirrorWithActive(IMultiblockLogic<S> logic, String name) {
        return complete(base(logic, name).notMirrored().customBlock(ModBlocks.REGISTER, ModItems.REGISTER, r -> new MultiblockPartBlockNonMirrorActive<>(ModBlocks.METAL_PROPERTIES_NO_OCCLUSION.get(), r), ModBlockItem::new));
    }

    public static final MultiblockRegistration<AdvancedCokeOvenLogic.State> ADVANCED_COKE_OVEN =
            stoneNoMirror(new AdvancedCokeOvenLogic(), "advanced_coke_oven")
                    .structure(() -> getMBTemplate.apply("advanced_coke_oven"))
                    .component(new MultiblockGui<>(MenuTypes.ADVANCED_COKE_OVEN_MENU))
                    .withComparator()
                    .build();

    public static final MultiblockRegistration<AlternatorLogic.State> ALTERNATOR =
            metalNoMirror(new AlternatorLogic(), "alternator")
                    .structure(() -> getMBTemplate.apply("alternator"))
                    .withComparator()
                    .build();

    public static final MultiblockRegistration<BoilerLiquidLogic.State> BOILER_LIQUID =
            metalNoMirror(new BoilerLiquidLogic(), "boiler_liquid")
                    .structure(() -> getMBTemplate.apply("boiler_liquid"))
                    .redstone(s -> s.rsState, BoilerLiquidLogic.REDSTONE_POI)
                    .component(new BoilerLiquidProcess())
                    .component(new ClearTank<>(BoilerLiquidLogic.INPUT_FLUID_POIS, s -> s.tanks.input1().drain(Integer.MAX_VALUE, FluidAction.EXECUTE), Component.translatable(TranslationKey.GUI_INPUT_TANK_CLEARED.getLocation())))
                    .component(new MultiblockGui<>(MenuTypes.BOILER_LIQUID_MENU))
                    .withComparator()
                    .build();

    public static final MultiblockRegistration<BoilerSolidLogic.State> BOILER_SOLID =
            metalNoMirrorWithActive(new BoilerSolidLogic(), "boiler_solid")
                    .structure(() -> getMBTemplate.apply("boiler_solid"))
                    .redstone(s -> s.rsState, BoilerSolidLogic.REDSTONE_POI)
                    .component(new BoilerSolidProcess())
                    .component(new MultiblockGui<>(MenuTypes.BOILER_SOLID_MENU))
                    .withComparator()
                    .build();

    public static final MultiblockRegistration<BoilerTankLogic.State> BOILER_TANK =
            metalNoMirror(new BoilerTankLogic(), "boiler_tank")
                    .structure(() -> getMBTemplate.apply("boiler_tank"))
                    .component(new ClearTank<>(BoilerTankLogic.INPUT_FLUID_POIS, s -> s.tanks.input().drain(Integer.MAX_VALUE, FluidAction.EXECUTE), Component.translatable(TranslationKey.GUI_INPUT_TANK_CLEARED.getLocation())))
                    .component(new MultiblockGui<>(MenuTypes.BOILER_TANK_MENU))
                    .withComparator()
                    .build();

    public static final MultiblockRegistration<CoolingTowerLogic.State> COOLING_TOWER =
            stoneNoMirror(new CoolingTowerLogic(), "cooling_tower")
                    .structure(() -> getMBTemplate.apply("cooling_tower"))
                    .component(new ClearTank<>(CoolingTowerLogic.INPUT_FLUID_POIS, s -> { s.tanks.input0().drain(Integer.MAX_VALUE, FluidAction.EXECUTE); s.tanks.input1().drain(Integer.MAX_VALUE, FluidAction.EXECUTE); }, Component.translatable(TranslationKey.GUI_INPUT_TANKS_CLEARED.getLocation())))
                    .withComparator()
                    .build();

    public static final MultiblockRegistration<DistillerLogic.State> DISTILLER =
            metal(new DistillerLogic(), "distiller")
                    .structure(() -> getMBTemplate.apply("distiller"))
                    .redstone(s -> s.rsState, DistillerLogic.REDSTONE_POI)
                    .component(new ClearTank<>(DistillerLogic.INPUT_FLUID_POIS, s -> s.tanks.input().drain(Integer.MAX_VALUE, FluidAction.EXECUTE), Component.translatable(TranslationKey.GUI_INPUT_TANK_CLEARED.getLocation())))
                    .component(new MultiblockGui<>(MenuTypes.DISTILLER_MENU))
                    .withComparator()
                    .build();

    public static final MultiblockRegistration<ElectrolyticCrucibleBatteryLogic.State> ELECTROLYTIC_CRUCIBLE_BATTERY =
            metal(new ElectrolyticCrucibleBatteryLogic(), "electrolytic_crucible_battery")
                    .structure(() -> getMBTemplate.apply("electrolytic_crucible_battery"))
                    .redstone(s -> s.rsState, ElectrolyticCrucibleBatteryLogic.REDSTONE_POI)
                    .component(new ClearTank<>(ElectrolyticCrucibleBatteryLogic.INPUT_FLUID_POIS, s -> s.tanks.input().drain(Integer.MAX_VALUE, FluidAction.EXECUTE), Component.translatable(TranslationKey.GUI_INPUT_TANK_CLEARED.getLocation())))
                    .withComparator()
                    .build();

    public static final MultiblockRegistration<GasTurbineLogic.State> GAS_TURBINE =
            metal(new GasTurbineLogic(), "gas_turbine")
                    .structure(() -> getMBTemplate.apply("gas_turbine"))
                    .redstone(s -> s.rsState, GasTurbineLogic.REDSTONE_POI)
                    .component(new ClearTank<>(GasTurbineLogic.INPUT_FLUID_POIS, s -> s.tanks.input().drain(Integer.MAX_VALUE, FluidAction.EXECUTE), Component.translatable(TranslationKey.GUI_INPUT_TANK_CLEARED.getLocation())))
                    .withComparator()
                    .build();

    public static final MultiblockRegistration<HeatExchangerLogic.State> HEAT_EXCHANGER =
            metal(new HeatExchangerLogic(), "heat_exchanger")
                    .structure(() -> getMBTemplate.apply("heat_exchanger"))
                    .redstone(s -> s.rsState, HeatExchangerLogic.REDSTONE_POI)
                    .component(new ClearTank<>(HeatExchangerLogic.INPUT_FLUID_POIS, s -> { s.tanks.input0().drain(Integer.MAX_VALUE, FluidAction.EXECUTE); s.tanks.input1().drain(Integer.MAX_VALUE, FluidAction.EXECUTE); }, Component.translatable(TranslationKey.GUI_INPUT_TANKS_CLEARED.getLocation())))
                    .withComparator()
                    .build();

    public static final MultiblockRegistration<MeltingCrucibleLogic.State> MELTING_CRUCIBLE =
            metal(new MeltingCrucibleLogic(), "melting_crucible")
                    .structure(() -> getMBTemplate.apply("melting_crucible"))
                    .redstone(s -> s.rsState, MeltingCrucibleLogic.REDSTONE_POI)
                    .component(new ClearTank<>(MeltingCrucibleLogic.INPUT_FLUID_POIS, s -> s.tanks.input().drain(Integer.MAX_VALUE, FluidAction.EXECUTE), Component.translatable(TranslationKey.GUI_INPUT_TANK_CLEARED.getLocation())))
                    .component(new MultiblockGui<>(MenuTypes.MELTING_CRUCIBLE_MENU))
                    .withComparator()
                    .build();

    public static final MultiblockRegistration<RadiatorLogic.State> RADIATOR =
            metalNoMirror(new RadiatorLogic(), "radiator")
                    .structure(() -> getMBTemplate.apply("radiator"))
                    .redstone(s -> s.rsState, RadiatorLogic.REDSTONE_POI)
                    .withComparator()
                    .build();

    public static final MultiblockRegistration<RadiatorHorizontalLogic.State> RADIATOR_HORIZONTAL =
            metalNoMirror(new RadiatorHorizontalLogic(), "radiator_horizontal")
                    .structure(() -> getMBTemplate.apply("radiator_horizontal"))
                    .redstone(s -> s.rsState, RadiatorHorizontalLogic.REDSTONE_POI)
                    .withComparator()
                    .build();

    public static final MultiblockRegistration<SolarMelterLogic.State> SOLAR_MELTER =
            metal(new SolarMelterLogic(), "solar_melter")
                    .structure(() -> getMBTemplate.apply("solar_melter"))
                    .redstone(s -> s.rsState, SolarMelterLogic.REDSTONE_POI)
                    .component(new ClearTank<>(SolarMelterLogic.INPUT_FLUID_POIS, s -> s.tanks.input().drain(Integer.MAX_VALUE, FluidAction.EXECUTE), Component.translatable(TranslationKey.GUI_INPUT_TANK_CLEARED.getLocation())))
                    .component(new MultiblockGui<>(MenuTypes.SOLAR_MELTER_MENU))
                    .withComparator()
                    .build();

    public static final MultiblockRegistration<SolarReflectorLogic.State> SOLAR_REFLECTOR =
            metalNoMirror(new SolarReflectorLogic(), "solar_reflector")
                    .structure(() -> getMBTemplate.apply("solar_reflector"))
                    .build();

    public static final MultiblockRegistration<SolarTowerLogic.State> SOLAR_TOWER =
            metal(new SolarTowerLogic(), "solar_tower")
                    .structure(() -> getMBTemplate.apply("solar_tower"))
                    .redstone(s -> s.rsState, SolarTowerLogic.REDSTONE_POI)
                    .component(new ClearTank<>(SolarTowerLogic.INPUT_FLUID_POIS, s -> s.tanks.input().drain(Integer.MAX_VALUE, FluidAction.EXECUTE), Component.translatable(TranslationKey.GUI_INPUT_TANK_CLEARED.getLocation())))
                    .component(new MultiblockGui<>(MenuTypes.SOLAR_TOWER_MENU))
                    .withComparator()
                    .build();

    public static final MultiblockRegistration<SteamTurbineLogic.State> STEAM_TURBINE =
            metal(new SteamTurbineLogic(), "steam_turbine")
                    .structure(() -> getMBTemplate.apply("steam_turbine"))
                    .redstone(s -> s.rsState, SteamTurbineLogic.REDSTONE_POI)
                    .component(new ClearTank<>(SteamTurbineLogic.INPUT_FLUID_POIS, s -> s.tanks.input().drain(Integer.MAX_VALUE, FluidAction.EXECUTE), Component.translatable(TranslationKey.GUI_INPUT_TANK_CLEARED.getLocation())))
                    .withComparator()
                    .build();

    public static final MultiblockRegistration<SteelSheetmetalTankLogic.State> STEEL_SHEETMETAL_TANK =
            metalNoMirror(new SteelSheetmetalTankLogic(), "steel_sheetmetal_tank")
                    .structure(() -> getMBTemplate.apply("steel_sheetmetal_tank"))
                    .redstone(s -> s.rsState, SteelSheetmetalTankLogic.REDSTONE_POI)
                    .withComparator()
                    .build();

    @SuppressWarnings("unused")
    public static List<Class<? extends Block>> getAllBlockClasses() {
        return List.of(
                ADVANCED_COKE_OVEN.block().get().getClass(),
                ALTERNATOR.block().get().getClass(),
                BOILER_LIQUID.block().get().getClass(),
                BOILER_SOLID.block().get().getClass(),
                BOILER_TANK.block().get().getClass(),
                COOLING_TOWER.block().get().getClass(),
                DISTILLER.block().get().getClass(),
                ELECTROLYTIC_CRUCIBLE_BATTERY.block().get().getClass(),
                GAS_TURBINE.block().get().getClass(),
                HEAT_EXCHANGER.block().get().getClass(),
                MELTING_CRUCIBLE.block().get().getClass(),
                RADIATOR.block().get().getClass(),
                RADIATOR_HORIZONTAL.block().get().getClass(),
                SOLAR_MELTER.block().get().getClass(),
                SOLAR_REFLECTOR.block().get().getClass(),
                SOLAR_TOWER.block().get().getClass(),
                STEAM_TURBINE.block().get().getClass(),
                STEEL_SHEETMETAL_TANK.block().get().getClass()
        );
    }

    public static void init() {
        registerMB("advanced_coke_oven", AdvancedCokeOven.INSTANCE, ADVANCED_COKE_OVEN);
        registerMB("alternator", Alternator.INSTANCE, ALTERNATOR);
        registerMB("boiler_liquid", BoilerLiquid.INSTANCE, BOILER_LIQUID);
        registerMB("boiler_solid", BoilerSolid.INSTANCE, BOILER_SOLID);
        registerMB("boiler_tank", BoilerTank.INSTANCE, BOILER_TANK);
        registerMB("cooling_tower", CoolingTower.INSTANCE, COOLING_TOWER);
        registerMB("distiller", Distiller.INSTANCE, DISTILLER);
        registerMB("electrolytic_crucible_battery", ElectrolyticCrucibleBattery.INSTANCE, ELECTROLYTIC_CRUCIBLE_BATTERY);
        registerMB("gas_turbine", GasTurbine.INSTANCE, GAS_TURBINE);
        registerMB("heat_exchanger", HeatExchanger.INSTANCE, HEAT_EXCHANGER);
        registerMB("melting_crucible", MeltingCrucible.INSTANCE, MELTING_CRUCIBLE);
        registerMB("radiator", Radiator.INSTANCE, RADIATOR);
        registerMB("radiator_horizontal", RadiatorHorizontal.INSTANCE, RADIATOR_HORIZONTAL);
        registerMB("solar_melter", SolarMelter.INSTANCE, SOLAR_MELTER);
        registerMB("solar_reflector", SolarReflector.INSTANCE, SOLAR_REFLECTOR);
        registerMB("solar_tower", SolarTower.INSTANCE, SOLAR_TOWER);
        registerMB("steam_turbine", SteamTurbine.INSTANCE, STEAM_TURBINE);
        registerMB("steel_sheetmetal_tank", SteelSheetmetalTank.INSTANCE, STEEL_SHEETMETAL_TANK);
    }

    public static void forceClassLoad() { init(); }
}
