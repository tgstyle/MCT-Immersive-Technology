package mctmods.immersivetechnology.client.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mctmods.immersivetechnology.core.registration.ITParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import java.util.Locale;
import java.util.Objects;

public class ColoredSmokeData implements ParticleOptions {
    public static final Codec<ColoredSmokeData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.x()),
            Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.y()),
            Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.z())
    ).apply(inst, ColoredSmokeData::new));

    public static final Deserializer<ColoredSmokeData> DESERIALIZER = new Deserializer<ColoredSmokeData>() {
        public @NotNull ColoredSmokeData fromCommand(@NotNull ParticleType<ColoredSmokeData> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float r = reader.readFloat();
            reader.expect(' ');
            float g = reader.readFloat();
            reader.expect(' ');
            float b = reader.readFloat();
            return new ColoredSmokeData(r, g, b);
        }
        public @NotNull ColoredSmokeData fromNetwork(@NotNull ParticleType<ColoredSmokeData> type, FriendlyByteBuf buf) { return new ColoredSmokeData(buf.readFloat(), buf.readFloat(), buf.readFloat()); }
    };

    public final Vector3f color;

    public ColoredSmokeData(float r, float g, float b) {
        this.color = new Vector3f(r, g, b);
    }

    public ColoredSmokeData(Vector3f color) {
        this.color = color;
    }

    @Override
    public @NotNull ParticleType<?> getType() {
        return ITParticles.COLORED_SMOKE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeFloat(color.x());
        buf.writeFloat(color.y());
        buf.writeFloat(color.z());
    }

    @Override
    public @NotNull String writeToString() { return String.format(Locale.ROOT, "%s %.2f %.2f %.2f", Objects.requireNonNull(ForgeRegistries.PARTICLE_TYPES.getKey(this.getType())), this.color.x(), this.color.y(), this.color.z()); }
}
