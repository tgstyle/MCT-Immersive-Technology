package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.wooden.WoodenBarrelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SteelMetalBarrelBlockEntity extends WoodenBarrelBlockEntity
{

    public SteelMetalBarrelBlockEntity(BlockEntityType<? extends WoodenBarrelBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        this.tank.setCapacity(24000);
    }
}
