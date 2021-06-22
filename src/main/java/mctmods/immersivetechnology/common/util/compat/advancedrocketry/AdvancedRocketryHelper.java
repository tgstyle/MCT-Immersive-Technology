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
        return 1.0005 * ((world.provider.getDimension() == ARConfiguration.getCurrentConfig().spaceDimId) ? DimensionManager.getEffectiveDimId(world, pos).getPeakInsolationMultiplierWithoutAtmosphere() : DimensionManager.getEffectiveDimId(world, pos).getPeakInsolationMultiplier());
    }
    public static double getHeatTransferCoefficient(World world, BlockPos pos) {
        //This is.... vaguely accurate. It has the right shape, and so it's a decent way to go about it
        double atmosphereDensity = DimensionManager.getEffectiveDimId(world, pos).getAtmosphereDensity();
        return Math.sqrt((4 + atmosphereDensity)/100);
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
        return AtmosphereHandler.hasAtmosphereHandler(world.provider.getDimension()) && (AtmosphereHandler.getOxygenHandler(world.provider.getDimension()).getAtmosphereType(pos) == AtmosphereType.PRESSURIZEDAIR || AtmosphereHandler.getOxygenHandler(world.provider.getDimension()).getAtmosphereType(pos) == AtmosphereType.VACUUM);
    }
}
