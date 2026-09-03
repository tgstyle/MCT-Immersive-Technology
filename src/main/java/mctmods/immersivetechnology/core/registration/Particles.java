package mctmods.immersivetechnology.core.registration;

import com.mojang.serialization.Codec;
import com.immersiveconvergence.api.particles.ColoredSmoke;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class Particles {
    public static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Reference.MODID);

    public static final RegistryObject<ParticleType<ColoredSmoke>> COLORED_SMOKE = REGISTER.register("colored_smoke", () -> new ParticleType<>(false, ColoredSmoke.DESERIALIZER) {
        @Override @Nonnull public Codec<ColoredSmoke> codec() {return ColoredSmoke.CODEC;}
    });

    public static final RegistryObject<SimpleParticleType> SMOKE_CUSTOM = REGISTER.register("smoke_custom", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> CAMPFIRE_SMOKE = REGISTER.register("campfire_smoke", () -> new SimpleParticleType(true));
}
