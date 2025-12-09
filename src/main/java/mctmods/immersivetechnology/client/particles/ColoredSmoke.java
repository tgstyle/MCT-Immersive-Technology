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

@SuppressWarnings("deprecation")
public class ColoredSmoke implements ParticleOptions {
    public static final Codec<ColoredSmoke> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.x()),
            Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.y()),
            Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.z()),
            Codec.BOOL.optionalFieldOf("collide_horizontal", false).forGetter(d -> d.collideHorizontal),
            Codec.BOOL.optionalFieldOf("collide_vertical", false).forGetter(d -> d.collideVertical)
    ).apply(inst, ColoredSmoke::new));

    public static final Deserializer<ColoredSmoke> DESERIALIZER = new Deserializer<>() {
        @NotNull public ColoredSmoke fromCommand(@NotNull ParticleType<ColoredSmoke> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float r = reader.readFloat();
            reader.expect(' ');
            float g = reader.readFloat();
            reader.expect(' ');
            float b = reader.readFloat();
            boolean collideHorizontal = false;
            boolean collideVertical = false;
            if (reader.canRead()) {
                reader.expect(' ');
                collideHorizontal = reader.readBoolean();
                if (reader.canRead()) {
                    reader.expect(' ');
                    collideVertical = reader.readBoolean();
                }
            }
            return new ColoredSmoke(r, g, b, collideHorizontal, collideVertical);
        }

        @NotNull public ColoredSmoke fromNetwork(@NotNull ParticleType<ColoredSmoke> type, FriendlyByteBuf buf) {
            return new ColoredSmoke(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readBoolean(), buf.readBoolean());
        }
    };

    public final Vector3f color;
    public final boolean collideHorizontal;
    public final boolean collideVertical;

    public ColoredSmoke(float r, float g, float b) { this(r, g, b, false, false); }

    public ColoredSmoke(float r, float g, float b, boolean collideHorizontal, boolean collideVertical) {
        this.color = new Vector3f(r, g, b);
        this.collideHorizontal = collideHorizontal;
        this.collideVertical = collideVertical;
    }

    @Override @NotNull public ParticleType<?> getType() { return ITParticles.COLORED_SMOKE.get(); }

    @Override public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeFloat(color.x());
        buf.writeFloat(color.y());
        buf.writeFloat(color.z());
        buf.writeBoolean(collideHorizontal);
        buf.writeBoolean(collideVertical);
    }

    @Override @NotNull public String writeToString() { return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %b %b", Objects.requireNonNull(ForgeRegistries.PARTICLE_TYPES.getKey(this.getType())), this.color.x(), this.color.y(), this.color.z(), this.collideHorizontal, this.collideVertical); }
}
