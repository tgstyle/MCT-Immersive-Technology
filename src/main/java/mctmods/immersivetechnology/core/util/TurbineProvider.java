package mctmods.immersivetechnology.core.util;

import com.immersiveconvergence.api.capability.IMechanicalEnergyProvider;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public record TurbineProvider(IntSupplier speed, Supplier<Float> torque, IntSupplier maxSpeed, DoubleSupplier baseMass, DoubleSupplier driveTorque, DoubleSupplier friction) implements IMechanicalEnergyProvider {
    @Override public int getSpeed() { return speed.getAsInt(); }

    @Override public float getTorque() { return torque.get(); }

    @Override public int getMaxSpeed() { return maxSpeed.getAsInt(); }

    @Override public double getBaseMass() { return baseMass.getAsDouble(); }

    @Override public double getDriveTorque() { return driveTorque.getAsDouble(); }

    @Override public double getFriction() { return friction.getAsDouble(); }
}
