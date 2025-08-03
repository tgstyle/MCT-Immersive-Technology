package mctmods.immersivetechnology.core.registration;

import blusunrize.immersiveengineering.api.IEProperties;
import mctmods.immersivetechnology.common.blocks.helper.ITMultiblockBEType;
import mctmods.immersivetechnology.common.blocks.metal.CokeOvenHeaterBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.CreativeBarrelBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.OpenBarrelBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.SteelBarrelBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.TrashEnergyBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.TrashFluidBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.TrashItemBlockEntity;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("ConstantConditions")
public class ITBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ITLib.MODID);

    public static final ITMultiblockBEType<CokeOvenHeaterBlockEntity> COKE_OVEN_HEATER = new ITMultiblockBEType<>(
            "coke_oven_heater",
            REGISTER, CokeOvenHeaterBlockEntity::new,
            () -> ITBlocks.MetalDevices.COKE_OVEN_HEATER.get(),
            state -> !state.getValue(IEProperties.MULTIBLOCKSLAVE)
    );

    public static final RegistryObject<BlockEntityType<CreativeBarrelBlockEntity>> CREATIVE_BARREL = REGISTER.register(
            "creative_barrel",
            () -> BlockEntityType.Builder.of(CreativeBarrelBlockEntity::new, ITBlocks.MetalDevices.CREATIVE_BARREL.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<SteelBarrelBlockEntity>> STEEL_BARREL = REGISTER.register(
            "steel_barrel",
            () -> BlockEntityType.Builder.of(SteelBarrelBlockEntity::new, ITBlocks.MetalDevices.STEEL_BARREL.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<OpenBarrelBlockEntity>> OPEN_BARREL = REGISTER.register(
            "open_barrel",
            () -> BlockEntityType.Builder.of(OpenBarrelBlockEntity::new, ITBlocks.MetalDevices.OPEN_BARREL.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<TrashEnergyBlockEntity>> TRASH_ENERGY = REGISTER.register(
            "trash_energy",
            () -> BlockEntityType.Builder.of(TrashEnergyBlockEntity::new, ITBlocks.MetalDevices.TRASH_ENERGY.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<TrashFluidBlockEntity>> TRASH_FLUID = REGISTER.register(
            "trash_fluid",
            () -> BlockEntityType.Builder.of(TrashFluidBlockEntity::new, ITBlocks.MetalDevices.TRASH_FLUID.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<TrashItemBlockEntity>> TRASH_ITEM = REGISTER.register(
            "trash_item",
            () -> BlockEntityType.Builder.of(TrashItemBlockEntity::new, ITBlocks.MetalDevices.TRASH_ITEM.get()).build(null)
    );

    public static void init(IEventBus event) { REGISTER.register(event); }
}
