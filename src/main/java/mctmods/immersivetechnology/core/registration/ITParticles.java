package mctmods.immersivetechnology.core.registration;

import com.mojang.serialization.MapCodec;
import mctmods.immersivetechnology.client.particles.ColoredSmoke;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ITParticles {
    public static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(Registries.PARTICLE_TYPE, ITLib.MODID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<ColoredSmoke>> COLORED_SMOKE = REGISTER.register("colored_smoke", () -> new ParticleType<>(false) {
        @Override
        @NotNull
        public MapCodec<ColoredSmoke> codec() {
            return ColoredSmoke.CODEC;
        }

        @Override
        public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, ColoredSmoke> streamCodec() {
            return ColoredSmoke.STREAM_CODEC;
        }
    });

    public static final Supplier<SimpleParticleType> SMOKE_CUSTOM = REGISTER.register("smoke_custom", () -> new SimpleParticleType(true));
}
