package mctmods.immersivetechnology.core.registration;

import mctmods.immersivetechnology.common.blocks.metal.logic.*;
import mctmods.immersivetechnology.common.blocks.wooden.logic.CrateCreativeBlockEntity;
import mctmods.immersivetechnology.core.lib.Reference;

import com.immersiveconvergence.api.ICMods;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings("ConstantConditions")
public class BlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Reference.MODID);
    private static final DeferredRegister<BlockEntityType<?>> REGISTER_IE = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Reference.MODID);

    public static final Supplier<BlockEntityType<AdvancedCokeOvenBaseHeaterBlockEntity>> ADVANCED_COKE_OVEN_BASEHEATER = REGISTER.register(
            "advanced_coke_oven_baseheater",
            () -> BlockEntityType.Builder.of(AdvancedCokeOvenBaseHeaterBlockEntity::new, ModBlocks.Metal.ADVANCED_COKE_OVEN_BASEHEATER.get()).build(null)
    );

    public static final Supplier<BlockEntityType<BarrelCreativeBlockEntity>> BARREL_CREATIVE = REGISTER.register(
            "barrel_creative",
            () -> BlockEntityType.Builder.of(BarrelCreativeBlockEntity::new, ModBlocks.Metal.BARREL_CREATIVE.get()).build(null)
    );

    public static final Supplier<BlockEntityType<BarrelSteelBlockEntity>> BARREL_STEEL = REGISTER.register(
            "barrel_steel",
            () -> BlockEntityType.Builder.of(BarrelSteelBlockEntity::new, ModBlocks.Metal.BARREL_STEEL.get()).build(null)
    );

    public static final Supplier<BlockEntityType<BarrelOpenBlockEntity>> BARREL_OPEN = REGISTER.register(
            "barrel_open",
            () -> BlockEntityType.Builder.of(BarrelOpenBlockEntity::new, ModBlocks.Metal.BARREL_OPEN.get()).build(null)
    );

    public static final Supplier<BlockEntityType<CrateCreativeBlockEntity>> CRATE_CREATIVE = REGISTER.register(
            "crate_creative",
            () -> BlockEntityType.Builder.of(CrateCreativeBlockEntity::new, ModBlocks.Wooden.CRATE_CREATIVE.get()).build(null)
    );



    public static final Supplier<BlockEntityType<TrashEnergyBlockEntity>> TRASH_ENERGY = REGISTER.register(
            "trash_energy",
            () -> BlockEntityType.Builder.of(TrashEnergyBlockEntity::new, ModBlocks.Metal.TRASH_ENERGY.get()).build(null)
    );

    public static final Supplier<BlockEntityType<TrashFluidBlockEntity>> TRASH_FLUID = REGISTER.register(
            "trash_fluid",
            () -> BlockEntityType.Builder.of(TrashFluidBlockEntity::new, ModBlocks.Metal.TRASH_FLUID.get()).build(null)
    );

    public static final Supplier<BlockEntityType<TrashItemBlockEntity>> TRASH_ITEM = REGISTER.register(
            "trash_item",
            () -> BlockEntityType.Builder.of(TrashItemBlockEntity::new, ModBlocks.Metal.TRASH_ITEM.get()).build(null)
    );

    public static final Supplier<BlockEntityType<ValveFluidBlockEntity>> VALVE_FLUID = REGISTER_IE.register(
            "valve_fluid",
            () -> BlockEntityType.Builder.of(ValveFluidBlockEntity::new, ModBlocks.Metal.VALVE_FLUID.get()).build(null)
    );

    public static final Supplier<BlockEntityType<ValveLoadBlockEntity>> VALVE_LOAD = REGISTER_IE.register(
            "valve_load",
            () -> BlockEntityType.Builder.of(ValveLoadBlockEntity::new, ModBlocks.Metal.VALVE_LOAD.get()).build(null)
    );

    public static final Supplier<BlockEntityType<ValveLimiterBlockEntity>> VALVE_LIMITER = REGISTER_IE.register(
            "valve_limiter",
            () -> BlockEntityType.Builder.of(ValveLimiterBlockEntity::new, ModBlocks.Metal.VALVE_LIMITER.get()).build(null)
    );

    public static final List<Supplier<?>> FLUID_BES = withIE(List.of(
            BARREL_STEEL,
            BARREL_CREATIVE,
            BARREL_OPEN,
            TRASH_FLUID
    ), VALVE_FLUID);

    public static final List<Supplier<?>> ENERGY_BES = List.of(
            TRASH_ENERGY,
            ADVANCED_COKE_OVEN_BASEHEATER
    );

    public static final List<Supplier<?>> ITEM_BES = withIE(List.of(
            CRATE_CREATIVE,
            TRASH_ITEM
    ), VALVE_LIMITER);

    private static List<Supplier<?>> withIE(List<Supplier<?>> types, Supplier<?> ieType) { return ICMods.immersiveEngineering() ? Stream.concat(types.stream(), Stream.of(ieType)).toList() : types; }

    public static void init(IEventBus event) {
        REGISTER.register(event);
        if (ICMods.immersiveEngineering()) { REGISTER_IE.register(event); }
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
