package mctmods.immersivetechnology.common.util.compat.advancedrocketry;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import zmaster587.advancedRocketry.api.ARConfiguration;
import zmaster587.advancedRocketry.atmosphere.AtmosphereHandler;
import zmaster587.advancedRocketry.atmosphere.AtmosphereType;
import zmaster587.advancedRocketry.dimension.DimensionManager;

public class AdvancedRocketryHelper {
    public static double getInsolation(World world, BlockPos pos){
        return 1.0005 * ((isSpaceStation(world)) ? DimensionManager.getEffectiveDimId(world, pos).getPeakInsolationMultiplierWithoutAtmosphere() : DimensionManager.getEffectiveDimId(world, pos).getPeakInsolationMultiplier());
    }

    public static double getHeatTransferCoefficient(World world, BlockPos pos) {
        double planetaryAtmosphereDensity = DimensionManager.getEffectiveDimId(world, pos).getAtmosphereDensity();
        double actualAtmosphereDensity = isSpaceStation(world) ? 0 : planetaryAtmosphereDensity;
        return Math.sqrt((4 + actualAtmosphereDensity)/100);
    }

    public static double getRadiatorHeatTransferCoefficient(World world, BlockPos pos, double fluidTemperature, double radiationEfficiency) {
        if (isAtmosphereArtificial(world, pos)) return 0;
        double planetaryAtmosphereDensity = DimensionManager.getEffectiveDimId(world, pos).getAtmosphereDensity();
        double actualAtmosphereDensity = isSpaceStation(world) ? 0 : planetaryAtmosphereDensity;
        double temperature = DimensionManager.getEffectiveDimId(world, pos).averageTemperature * ((isSpaceStation(world)) ? 1/Math.max(1, (1.125d * Math.pow((planetaryAtmosphereDensity/100d), 0.25))) : 1);
        if (temperature > fluidTemperature) return 0;
        double deltaTemperatureRatio = (fluidTemperature - temperature)/212.9671;
        return (radiationEfficiency + (3.0 * Math.sqrt(actualAtmosphereDensity/100))) * Math.pow(deltaTemperatureRatio, 4);
    }

    public static boolean isAtmosphereSuitableForCombustion(World world, BlockPos pos) {
        return !AtmosphereHandler.hasAtmosphereHandler(world.provider.getDimension()) || AtmosphereHandler.getOxygenHandler(world.provider.getDimension()).getAtmosphereType(pos).allowsCombustion();
    }

    public static double getWaterPartialPressureMultiplier(World world, BlockPos pos) {
        return DimensionManager.getEffectiveDimId(world, pos).getAtmosphereDensity()/100d;
    }

    public static boolean isAtmosphereUnsuitableForCooling(World world, BlockPos pos) {
        return AtmosphereHandler.hasAtmosphereHandler(world.provider.getDimension()) && (isAtmosphereArtificial(world, pos) || AtmosphereHandler.getOxygenHandler(world.provider.getDimension()).getAtmosphereType(pos) == AtmosphereType.VACUUM);
    }

    public static boolean isAtmosphereArtificial(World world, BlockPos pos) {
        return AtmosphereHandler.hasAtmosphereHandler(world.provider.getDimension()) && AtmosphereHandler.getOxygenHandler(world.provider.getDimension()).getAtmosphereType(pos) == AtmosphereType.PRESSURIZEDAIR;
    }

    public static boolean isSpaceStation(World world) {
        return world.provider.getDimension() == ARConfiguration.getCurrentConfig().spaceDimId;
    }
}
