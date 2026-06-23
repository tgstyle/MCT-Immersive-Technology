package mctmods.immersivetechnology.core.registration;

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


    public static final RegistryObject<BlockEntityType<AdvancedCokeOvenBaseHeaterBlockEntity>> ADVANCED_COKE_OVEN_BASEHEATER = REGISTER.register(
            "advanced_coke_oven_baseheater",
            () -> BlockEntityType.Builder.of(AdvancedCokeOvenBaseHeaterBlockEntity::new, ITBlocks.Metal.ADVANCED_COKE_OVEN_BASEHEATER.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<BarrelCreativeBlockEntity>> BARREL_CREATIVE = REGISTER.register(
            "barrel_creative",
            () -> BlockEntityType.Builder.of(BarrelCreativeBlockEntity::new, ITBlocks.Metal.BARREL_CREATIVE.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<BarrelSteelBlockEntity>> BARREL_STEEL = REGISTER.register(
            "barrel_steel",
            () -> BlockEntityType.Builder.of(BarrelSteelBlockEntity::new, ITBlocks.Metal.BARREL_STEEL.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<BarrelOpenBlockEntity>> BARREL_OPEN = REGISTER.register(
            "barrel_open",
            () -> BlockEntityType.Builder.of(BarrelOpenBlockEntity::new, ITBlocks.Metal.BARREL_OPEN.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<CrateCreativeBlockEntity>> CRATE_CREATIVE = REGISTER.register(
            "crate_creative",
            () -> BlockEntityType.Builder.of(CrateCreativeBlockEntity::new, ITBlocks.Wooden.CRATE_CREATIVE.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<HeatCreativeBlockEntity>> HEAT_CREATIVE = REGISTER.register(
            "heat_creative",
            () -> BlockEntityType.Builder.of(HeatCreativeBlockEntity::new, ITBlocks.Metal.HEAT_CREATIVE.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<RotorCreativeBlockEntity>> ROTOR_CREATIVE = REGISTER.register(
            "rotor_creative",
            () -> BlockEntityType.Builder.of(RotorCreativeBlockEntity::new, ITBlocks.Metal.ROTOR_CREATIVE.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<TrashEnergyBlockEntity>> TRASH_ENERGY = REGISTER.register(
            "trash_energy",
            () -> BlockEntityType.Builder.of(TrashEnergyBlockEntity::new, ITBlocks.Metal.TRASH_ENERGY.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<TrashFluidBlockEntity>> TRASH_FLUID = REGISTER.register(
            "trash_fluid",
            () -> BlockEntityType.Builder.of(TrashFluidBlockEntity::new, ITBlocks.Metal.TRASH_FLUID.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<TrashItemBlockEntity>> TRASH_ITEM = REGISTER.register(
            "trash_item",
            () -> BlockEntityType.Builder.of(TrashItemBlockEntity::new, ITBlocks.Metal.TRASH_ITEM.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<ValveFluidBlockEntity>> VALVE_FLUID = REGISTER.register(
            "valve_fluid",
            () -> BlockEntityType.Builder.of(ValveFluidBlockEntity::new, ITBlocks.Metal.VALVE_FLUID.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<ValveLoadBlockEntity>> VALVE_LOAD = REGISTER.register(
            "valve_load",
            () -> BlockEntityType.Builder.of(ValveLoadBlockEntity::new, ITBlocks.Metal.VALVE_LOAD.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<ValveLimiterBlockEntity>> VALVE_LIMITER = REGISTER.register(
            "valve_limiter",
            () -> BlockEntityType.Builder.of(ValveLimiterBlockEntity::new, ITBlocks.Metal.VALVE_LIMITER.get()).build(null)
    );

    public static void init(IEventBus event) { REGISTER.register(event); }
}
