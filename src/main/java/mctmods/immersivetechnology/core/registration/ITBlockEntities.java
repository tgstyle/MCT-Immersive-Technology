package mctmods.immersivetechnology.core.registration;

import mctmods.immersivetechnology.common.blocks.connectors.logic.ConnectorTimerBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.logic.*;
import mctmods.immersivetechnology.common.blocks.wooden.logic.CrateCreativeBlockEntity;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("ConstantConditions")
public class ITBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ITLib.MODID);

    public static final Supplier<BlockEntityType<AdvancedCokeOvenBaseHeaterBlockEntity>> ADVANCED_COKE_OVEN_BASEHEATER = REGISTER.register(
            "advanced_coke_oven_baseheater",
            () -> BlockEntityType.Builder.of(AdvancedCokeOvenBaseHeaterBlockEntity::new, ITBlocks.Metal.ADVANCED_COKE_OVEN_BASEHEATER.get()).build(null)
    );

    public static final Supplier<BlockEntityType<BarrelCreativeBlockEntity>> BARREL_CREATIVE = REGISTER.register(
            "barrel_creative",
            () -> BlockEntityType.Builder.of(BarrelCreativeBlockEntity::new, ITBlocks.Metal.BARREL_CREATIVE.get()).build(null)
    );

    public static final Supplier<BlockEntityType<BarrelSteelBlockEntity>> BARREL_STEEL = REGISTER.register(
            "barrel_steel",
            () -> BlockEntityType.Builder.of(BarrelSteelBlockEntity::new, ITBlocks.Metal.BARREL_STEEL.get()).build(null)
    );

    public static final Supplier<BlockEntityType<BarrelOpenBlockEntity>> BARREL_OPEN = REGISTER.register(
            "barrel_open",
            () -> BlockEntityType.Builder.of(BarrelOpenBlockEntity::new, ITBlocks.Metal.BARREL_OPEN.get()).build(null)
    );

    public static final Supplier<BlockEntityType<CrateCreativeBlockEntity>> CRATE_CREATIVE = REGISTER.register(
            "crate_creative",
            () -> BlockEntityType.Builder.of(CrateCreativeBlockEntity::new, ITBlocks.Wooden.CRATE_CREATIVE.get()).build(null)
    );

    public static final Supplier<BlockEntityType<HeatCreativeBlockEntity>> HEAT_CREATIVE = REGISTER.register(
            "heat_creative",
            () -> BlockEntityType.Builder.of(HeatCreativeBlockEntity::new, ITBlocks.Metal.HEAT_CREATIVE.get()).build(null)
    );

    public static final Supplier<BlockEntityType<RotorCreativeBlockEntity>> ROTOR_CREATIVE = REGISTER.register(
            "rotor_creative",
            () -> BlockEntityType.Builder.of(RotorCreativeBlockEntity::new, ITBlocks.Metal.ROTOR_CREATIVE.get()).build(null)
    );

    public static final Supplier<BlockEntityType<TrashEnergyBlockEntity>> TRASH_ENERGY = REGISTER.register(
            "trash_energy",
            () -> BlockEntityType.Builder.of(TrashEnergyBlockEntity::new, ITBlocks.Metal.TRASH_ENERGY.get()).build(null)
    );

    public static final Supplier<BlockEntityType<TrashFluidBlockEntity>> TRASH_FLUID = REGISTER.register(
            "trash_fluid",
            () -> BlockEntityType.Builder.of(TrashFluidBlockEntity::new, ITBlocks.Metal.TRASH_FLUID.get()).build(null)
    );

    public static final Supplier<BlockEntityType<TrashItemBlockEntity>> TRASH_ITEM = REGISTER.register(
            "trash_item",
            () -> BlockEntityType.Builder.of(TrashItemBlockEntity::new, ITBlocks.Metal.TRASH_ITEM.get()).build(null)
    );

    public static final Supplier<BlockEntityType<ValveFluidBlockEntity>> VALVE_FLUID = REGISTER.register(
            "valve_fluid",
            () -> BlockEntityType.Builder.of(ValveFluidBlockEntity::new, ITBlocks.Metal.VALVE_FLUID.get()).build(null)
    );

    public static final Supplier<BlockEntityType<ValveLoadBlockEntity>> VALVE_LOAD = REGISTER.register(
            "valve_load",
            () -> BlockEntityType.Builder.of(ValveLoadBlockEntity::new, ITBlocks.Metal.VALVE_LOAD.get()).build(null)
    );

    public static final Supplier<BlockEntityType<ValveLimiterBlockEntity>> VALVE_LIMITER = REGISTER.register(
            "valve_limiter",
            () -> BlockEntityType.Builder.of(ValveLimiterBlockEntity::new, ITBlocks.Metal.VALVE_LIMITER.get()).build(null)
    );

    public static final Supplier<BlockEntityType<ConnectorTimerBlockEntity>> CONNECTOR_TIMER = REGISTER.register(
            "connector_timer",
            () -> BlockEntityType.Builder.of(ConnectorTimerBlockEntity::new, ITBlocks.Connector.CONNECTOR_TIMER.get()).build(null)
    );

    public static final List<Supplier<?>> FLUID_BES = List.of(
            BARREL_STEEL,
            BARREL_CREATIVE,
            BARREL_OPEN,
            TRASH_FLUID,
            VALVE_FLUID
    );

    public static final List<Supplier<?>> ENERGY_BES = List.of(
            TRASH_ENERGY,
            ADVANCED_COKE_OVEN_BASEHEATER
    );

    public static final List<Supplier<?>> ITEM_BES = List.of(
            CRATE_CREATIVE,
            TRASH_ITEM,
            VALVE_LIMITER
    );

    public static void init(IEventBus event) {
        REGISTER.register(event);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (Supplier<?> sup : FLUID_BES) {
            BlockEntityType<?> type = (BlockEntityType<?>) sup.get();
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, type, (be, side) -> {
                if (be instanceof BarrelCommonBlockEntity b) return b.getFluidHandler(side);
                if (be instanceof BarrelCreativeBlockEntity b) return b.getFluidHandler(side);
                if (be instanceof TrashFluidBlockEntity b) return b.getFluidHandler(side);
                if (be instanceof ValveFluidBlockEntity b) return b.getFluidHandler(side);
                return null;
            });
        }

        for (Supplier<?> sup : ENERGY_BES) {
            BlockEntityType<?> type = (BlockEntityType<?>) sup.get();
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, type, (be, side) -> {
                if (be instanceof TrashEnergyBlockEntity b) return b.getEnergyHandler(side);
                if (be instanceof AdvancedCokeOvenBaseHeaterBlockEntity b) return b.getEnergyHandler(side);
                return null;
            });
        }

        for (Supplier<?> sup : ITEM_BES) {
            BlockEntityType<?> type = (BlockEntityType<?>) sup.get();
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (be, side) -> {
                if (be instanceof CrateCreativeBlockEntity b) return b;
                if (be instanceof TrashItemBlockEntity b) return b.getItemHandler(side);
                if (be instanceof ValveLimiterBlockEntity b) return b.getItemHandler(side);
                return null;
            });
        }
    }
}
