package mctmods.immersivetechnology.core.registration;

import com.mojang.serialization.Codec;
import mctmods.immersivetechnology.client.particles.ColoredSmokeData;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class ITParticles {
    public static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ITLib.MODID);

    public static final RegistryObject<ParticleType<ColoredSmokeData>> COLORED_SMOKE = REGISTER.register("colored_smoke", () -> new ParticleType<ColoredSmokeData>(false, ColoredSmokeData.DESERIALIZER) {
        @Override
        public @NotNull Codec<ColoredSmokeData> codec() { return ColoredSmokeData.CODEC; }
    });
}
