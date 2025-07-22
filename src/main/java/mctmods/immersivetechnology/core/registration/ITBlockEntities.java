package mctmods.immersivetechnology.core.registration;

import mctmods.immersivetechnology.common.blocks.metal.CreativeBarrelBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.CokeOvenPreheaterBlockEntity;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ITBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ITLib.MODID);

    public static final RegistryObject<BlockEntityType<CokeOvenPreheaterBlockEntity>> COKE_OVEN_PREHEATER = REGISTER.register(
            "coke_oven_preheater",
            () -> BlockEntityType.Builder.of(CokeOvenPreheaterBlockEntity::new, new Block[]{ITBlocks.MetalDevices.COKE_OVEN_PREHEATER.get()}).build(null)
    );

    public static final RegistryObject<BlockEntityType<CreativeBarrelBlockEntity>> CREATIVE_BARREL = REGISTER.register(
            "creative_barrel",
            () -> BlockEntityType.Builder.of(CreativeBarrelBlockEntity::new, new Block[]{ITBlocks.MetalDevices.CREATIVE_BARREL.get()}).build(null)
    );

    public static void init(IEventBus event) {
        REGISTER.register(event);
    }
}
