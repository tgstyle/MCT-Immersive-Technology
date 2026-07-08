package mctmods.immersivetechnology.common.blocks.metal.logic;

import com.immersiveconvergence.api.HeatCapabilities;
import com.immersiveconvergence.api.capability.IHeatProvider;
import mctmods.immersivetechnology.common.blocks.helper.ITBaseBlockEntity;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;

public class HeatCreativeBlockEntity extends ITBaseBlockEntity {
    public HeatCreativeBlockEntity(BlockPos pos, BlockState state) { super(ITBlockEntities.HEAT_CREATIVE.get(), pos, state); }

    @SuppressWarnings("unused")
    private static class Provider implements IHeatProvider {
        @Override public double getHeatLevel() { return HeatCapabilities.MAX_HEAT; }
    }

    @Override public void readCustomNBT(CompoundTag nbt, boolean descPacket) {}

    @Override public void writeCustomNBT(CompoundTag nbt, boolean descPacket) {}
}
