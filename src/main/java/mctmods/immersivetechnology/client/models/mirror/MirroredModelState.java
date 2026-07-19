package mctmods.immersivetechnology.client.models.mirror;

import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.ModelState;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

public class MirroredModelState implements ModelState {
    private static final Transformation MIRRORED_IDENTITY = new Transformation(null, null, new Vector3f(-1.0F, 1.0F, 1.0F), null);
    private final ModelState inner;
    private final Transformation mirroredMainRotation;

    public MirroredModelState(ModelState inner) {
        this.inner = inner;
        this.mirroredMainRotation = mirror(inner.getRotation());
    }

    @Nonnull public Transformation getRotation() { return this.mirroredMainRotation; }

    public boolean isUvLocked() { return this.inner.isUvLocked(); }

    private static Transformation mirror(Transformation in) { return in.compose(MIRRORED_IDENTITY); }
}
