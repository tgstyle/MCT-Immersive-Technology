package mctmods.immersivetechnology.client.models.mirror;

import com.mojang.math.Transformation;
import org.joml.Vector3f;
import net.minecraft.client.resources.model.ModelState;

import javax.annotation.Nonnull;

public class ITMirroredModelState implements ModelState
{
    private static final Transformation MIRRORED_IDENTITY = new Transformation(
            null, null, new Vector3f(-1, 1, 1), null
    );
    private final ModelState inner;
    private final Transformation mirroredMainRotation;

    public ITMirroredModelState(ModelState inner)
    {
        this.inner = inner;
        this.mirroredMainRotation = mirror(inner.getRotation());
    }

    @Nonnull
    public Transformation getRotation()
    {
        return mirroredMainRotation;
    }

    public boolean isUvLocked()
    {
        return inner.isUvLocked();
    }

    private static Transformation mirror(Transformation in)
    {
        return in.compose(MIRRORED_IDENTITY);
    }
}
