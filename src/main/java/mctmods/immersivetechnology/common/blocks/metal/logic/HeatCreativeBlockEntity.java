package mctmods.immersivetechnology.common.blocks.metal.logic;

import com.immersiveconvergence.api.capability.HeatCapabilities;
import com.immersiveconvergence.api.capability.IHeatProvider;
import com.immersiveconvergence.api.block.BaseBlockEntity;
import mctmods.immersivetechnology.core.registration.BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HeatCreativeBlockEntity extends BaseBlockEntity {
    private final LazyOptional<IHeatProvider> providerCap = LazyOptional.of(Provider::new);

    public HeatCreativeBlockEntity(BlockPos pos, BlockState state) { super(BlockEntities.HEAT_CREATIVE.get(), pos, state); }

    @Override @Nonnull public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == HeatCapabilities.HEAT_PROVIDER_CAPABILITY) { return providerCap.cast(); }
        return super.getCapability(cap, side);
    }

    private static class Provider implements IHeatProvider {
        @Override public double getHeatLevel() { return HeatCapabilities.MAX_HEAT; }
    }

    @Override public void readCustomNBT(CompoundTag nbt, boolean descPacket) {}

    @Override public void writeCustomNBT(CompoundTag nbt, boolean descPacket) {}
}
