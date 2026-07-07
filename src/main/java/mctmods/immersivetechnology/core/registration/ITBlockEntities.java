package mctmods.immersivetechnology.core.registration;

import mctmods.immersivetechnology.common.blocks.connectors.logic.ConnectorTimerBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.logic.*;
import mctmods.immersivetechnology.common.blocks.wooden.logic.CrateCreativeBlockEntity;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("ConstantConditions")
public class ITBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ITLib.MODID);


    public static final RegistryObject<BlockEntityType<AdvancedCokeOvenBaseHeaterIBlockEntity>> ADVANCED_COKE_OVEN_BASEHEATER = REGISTER.register(
            "advanced_coke_oven_baseheater",
            () -> BlockEntityType.Builder.of(AdvancedCokeOvenBaseHeaterIBlockEntity::new, ITBlocks.Metal.ADVANCED_COKE_OVEN_BASEHEATER.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<BarrelCreativeIBlockEntity>> BARREL_CREATIVE = REGISTER.register(
            "barrel_creative",
            () -> BlockEntityType.Builder.of(BarrelCreativeIBlockEntity::new, ITBlocks.Metal.BARREL_CREATIVE.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<BarrelSteelIBlockEntity>> BARREL_STEEL = REGISTER.register(
            "barrel_steel",
            () -> BlockEntityType.Builder.of(BarrelSteelIBlockEntity::new, ITBlocks.Metal.BARREL_STEEL.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<BarrelOpenIBlockEntity>> BARREL_OPEN = REGISTER.register(
            "barrel_open",
            () -> BlockEntityType.Builder.of(BarrelOpenIBlockEntity::new, ITBlocks.Metal.BARREL_OPEN.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<CrateCreativeBlockEntity>> CRATE_CREATIVE = REGISTER.register(
            "crate_creative",
            () -> BlockEntityType.Builder.of(CrateCreativeBlockEntity::new, ITBlocks.Wooden.CRATE_CREATIVE.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<HeatCreativeIBlockEntity>> HEAT_CREATIVE = REGISTER.register(
            "heat_creative",
            () -> BlockEntityType.Builder.of(HeatCreativeIBlockEntity::new, ITBlocks.Metal.HEAT_CREATIVE.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<RotorCreativeIBlockEntity>> ROTOR_CREATIVE = REGISTER.register(
            "rotor_creative",
            () -> BlockEntityType.Builder.of(RotorCreativeIBlockEntity::new, ITBlocks.Metal.ROTOR_CREATIVE.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<TrashEnergyIBlockEntity>> TRASH_ENERGY = REGISTER.register(
            "trash_energy",
            () -> BlockEntityType.Builder.of(TrashEnergyIBlockEntity::new, ITBlocks.Metal.TRASH_ENERGY.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<TrashFluidIBlockEntity>> TRASH_FLUID = REGISTER.register(
            "trash_fluid",
            () -> BlockEntityType.Builder.of(TrashFluidIBlockEntity::new, ITBlocks.Metal.TRASH_FLUID.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<TrashItemIBlockEntity>> TRASH_ITEM = REGISTER.register(
            "trash_item",
            () -> BlockEntityType.Builder.of(TrashItemIBlockEntity::new, ITBlocks.Metal.TRASH_ITEM.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<ValveFluidIBlockEntity>> VALVE_FLUID = REGISTER.register(
            "valve_fluid",
            () -> BlockEntityType.Builder.of(ValveFluidIBlockEntity::new, ITBlocks.Metal.VALVE_FLUID.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<ValveLoadIBlockEntity>> VALVE_LOAD = REGISTER.register(
            "valve_load",
            () -> BlockEntityType.Builder.of(ValveLoadIBlockEntity::new, ITBlocks.Metal.VALVE_LOAD.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<ValveLimiterIBlockEntity>> VALVE_LIMITER = REGISTER.register(
            "valve_limiter",
            () -> BlockEntityType.Builder.of(ValveLimiterIBlockEntity::new, ITBlocks.Metal.VALVE_LIMITER.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<ConnectorTimerBlockEntity>> CONNECTOR_TIMER = REGISTER.register(
            "connector_timer",
            () -> BlockEntityType.Builder.of(ConnectorTimerBlockEntity::new, ITBlocks.Connector.CONNECTOR_TIMER.get()).build(null)
    );

    public static void init(IEventBus event) { REGISTER.register(event); }
}
