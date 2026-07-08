package mctmods.immersivetechnology.client.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mctmods.immersivetechnology.core.registration.ITParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class ColoredSmoke implements ParticleOptions {
    public static final MapCodec<ColoredSmoke> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.x()),
            Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.y()),
            Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.z()),
            Codec.BOOL.optionalFieldOf("collide_horizontal", false).forGetter(d -> d.collideHorizontal),
            Codec.BOOL.optionalFieldOf("collide_vertical", false).forGetter(d -> d.collideVertical)
    ).apply(inst, ColoredSmoke::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ColoredSmoke> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, d -> d.color.x(),
            ByteBufCodecs.FLOAT, d -> d.color.y(),
            ByteBufCodecs.FLOAT, d -> d.color.z(),
            ByteBufCodecs.BOOL, d -> d.collideHorizontal,
            ByteBufCodecs.BOOL, d -> d.collideVertical,
            ColoredSmoke::new
    );

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
}
