package mctmods.immersivetechnology.common.blocks.helper;

import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import net.minecraftforge.common.util.LazyOptional;

public final class ITResettableCapability<T> {
    private final T containedValue;
    private LazyOptional<T> currentOptional = LazyOptional.empty();

    public ITResettableCapability(T containedValue) { this.containedValue = containedValue; }

    public LazyOptional<T> getLO() {
        if (!this.currentOptional.isPresent()) { this.currentOptional = CapabilityUtils.constantOptional(this.containedValue); }
        return this.currentOptional;
    }

    public T get() { return this.containedValue; }

    public <A> LazyOptional<A> cast() { return this.getLO().cast(); }

    public void reset() { this.currentOptional.invalidate(); }
}
