package mctmods.immersivetechnology.core.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ProcessQueue<P> extends ArrayList<P> {
    private final transient Function<P, CompoundTag> writer;
    private final transient BiFunction<Level, CompoundTag, P> reader;
    private transient ListTag pending;

    public ProcessQueue(Function<P, CompoundTag> writer, BiFunction<Level, CompoundTag, P> reader) {
        this.writer = writer;
        this.reader = reader;
    }

    public void restore(Level level) {
        if (pending == null) { return; }
        for (int i = 0; i < pending.size(); i++) {
            P process = reader.apply(level, pending.getCompound(i));
            if (process != null) { add(process); }
        }
        pending = null;
    }

    public void write(CompoundTag nbt) {
        ListTag queue = new ListTag();
        if (pending != null) { queue.addAll(pending); }
        for (P process : this) { queue.add(writer.apply(process)); }
        nbt.put("processQueue", queue);
    }

    public void read(CompoundTag nbt) {
        clear();
        pending = nbt.getList("processQueue", Tag.TAG_COMPOUND);
    }
}
