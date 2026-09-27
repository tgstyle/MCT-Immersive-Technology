package mctmods.immersivetechnology.core.util;

import com.immersiveconvergence.api.capability.IMechanicalEnergyConsumer;
import com.immersiveconvergence.api.capability.MechanicalCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public record MechanicalLoad(boolean present, double mass, double friction, int maxSpeed) {
    public static MechanicalLoad at(Level level, BlockPos pos, Direction side) {
        BlockEntity entity = level.getBlockEntity(pos);
        IMechanicalEnergyConsumer consumer = entity != null ? entity.getCapability(MechanicalCapabilities.MECHANICAL_CONSUMER_CAPABILITY, side).resolve().orElse(null) : null;
        if (consumer == null) { return new MechanicalLoad(false, 0.0, 0.0, MechanicalCapabilities.maxRpm()); }
        return new MechanicalLoad(true, consumer.getMass(), consumer.getFriction(), consumer.getMaxSpeed());
    }

    public int limit(int max) { return present ? Math.min(max, maxSpeed) : max; }
}
