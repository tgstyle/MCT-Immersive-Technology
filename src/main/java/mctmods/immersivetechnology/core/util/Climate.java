package mctmods.immersivetechnology.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public class Climate {
    public static double coolingMultiplier(Level level, BlockPos pos, double tempFactor, double humidityFactor) {
        if (tempFactor <= 0.0D && humidityFactor <= 0.0D) { return 1.0D; }
        if (tempFactor > 0.0D && level.dimension() == Level.NETHER) { return 0.0D; }
        Biome biome = level.getBiome(pos).value();
        double multiplier = 1.0D;
        if (tempFactor > 0.0D) { multiplier -= (biome.getBaseTemperature() - 0.8D) * tempFactor; }
        if (humidityFactor > 0.0D) { multiplier += 0.075D * humidityFactor * -((biome.getModifiedClimateSettings().downfall() - 0.5D) / 0.5D); }
        return Math.max(multiplier, 0.01D);
    }
}
