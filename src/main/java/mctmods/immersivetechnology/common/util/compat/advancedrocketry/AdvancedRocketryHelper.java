package mctmods.immersivetechnology.common.util.compat.advancedrocketry;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import zmaster587.advancedRocketry.api.ARConfiguration;
import zmaster587.advancedRocketry.atmosphere.AtmosphereHandler;
import zmaster587.advancedRocketry.atmosphere.AtmosphereType;
import zmaster587.advancedRocketry.dimension.DimensionManager;

public class AdvancedRocketryHelper {
    public static double getInsolation(World world, BlockPos pos){
        //Slight corrective factor applied because Earth gives 0.999509ish. Gets the radiation power input per square meter as a ratio based on earth
        return 1.0005 * ((isSpaceStation(world)) ? DimensionManager.getEffectiveDimId(world, pos).getPeakInsolationMultiplierWithoutAtmosphere() : DimensionManager.getEffectiveDimId(world, pos).getPeakInsolationMultiplier());
    }
    public static double getHeatTransferCoefficient(World world, BlockPos pos) {
        //This is.... vaguely accurate. It has the right shape, and so it's a decent way to go about it
        double planetaryAtmosphereDensity = DimensionManager.getEffectiveDimId(world, pos).getAtmosphereDensity();
        double actualAtmosphereDensity = isSpaceStation(world) ? 0 : planetaryAtmosphereDensity;
        return Math.sqrt((4 + actualAtmosphereDensity)/100);
    }
    public static double getRadiatorHeatTransferCoefficient(World world, BlockPos pos, double fluidTemperature, double radiationEfficiency) {
        if (isAtmosphereArtificial(world, pos)) return 0;
        //This is.... sorta accurate? Just roll with it
        //This grabs atmosphere densities for later
        double planetaryAtmosphereDensity = DimensionManager.getEffectiveDimId(world, pos).getAtmosphereDensity();
        double actualAtmosphereDensity = isSpaceStation(world) ? 0 : planetaryAtmosphereDensity;
        //Determine the blackbody temperature of the radiator
        double temperature = DimensionManager.getEffectiveDimId(world, pos).averageTemperature * ((isSpaceStation(world)) ? 1/Math.max(1, (1.125d * Math.pow((planetaryAtmosphereDensity/100d), 0.25))) : 1);
        //Return if we would heat the fluid instead of cooling it
        if (temperature > fluidTemperature) return 0;
        //Grab the dT and use that for the cooling temperature
        double deltaTemperatureRatio = (fluidTemperature - temperature)/212.9671;
        //Return a minimum plus extra for if we have an atmosphere we're working in (why would you use it there, the cooling tower is MUCH better)
        //This is also pretty made up but it works mostly sort of, so roll with it
        return (radiationEfficiency + (3.0 * Math.sqrt(actualAtmosphereDensity/100))) * Math.pow(deltaTemperatureRatio, 4);
    }
    public static boolean isAtmosphereSuitableForCombustion(World world, BlockPos pos) {
        //No gas turbines in vacuum, nor boilers in vacuum. Provide some oxygen for them or use solar
        return !AtmosphereHandler.hasAtmosphereHandler(world.provider.getDimension()) || AtmosphereHandler.getOxygenHandler(world.provider.getDimension()).getAtmosphereType(pos).allowsCombustion();
    }
    public static double getWaterPartialPressureMultiplier(World world, BlockPos pos) {
        //Used to vary insolation with biome for the solar tower
        return DimensionManager.getEffectiveDimId(world, pos).getAtmosphereDensity()/100d;
    }
    public static boolean isAtmosphereUnsuitableForCooling(World world, BlockPos pos) {
        //Returns true if the atmosphere would either get to filled with steam as to be useless or if it would lose steam to the vacuum of space
        return AtmosphereHandler.hasAtmosphereHandler(world.provider.getDimension()) && (isAtmosphereArtificial(world, pos) || AtmosphereHandler.getOxygenHandler(world.provider.getDimension()).getAtmosphereType(pos) == AtmosphereType.VACUUM);
    }
    public static boolean isAtmosphereArtificial(World world, BlockPos pos) {
        return AtmosphereHandler.hasAtmosphereHandler(world.provider.getDimension()) && AtmosphereHandler.getOxygenHandler(world.provider.getDimension()).getAtmosphereType(pos) == AtmosphereType.PRESSURIZEDAIR;
    }
    public static boolean isSpaceStation(World world) {
        return world.provider.getDimension() == ARConfiguration.getCurrentConfig().spaceDimId;
    }
}
