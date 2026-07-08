package mctmods.immersivetechnology.client.models.split.geometry;

import com.google.common.collect.ImmutableList;

import java.util.List;

public record ITCyclicListWrapper<T>(List<T> wrapped) {
    public T get(int i) {
        return this.wrapped.get(this.toIndex(i));
    }

    public List<T> sublist(int begin, int end) {
        while (begin > end) { end += this.wrapped.size(); }
        ImmutableList.Builder<T> sublist = ImmutableList.builder();
        for (int i = begin; i < end; ++i) { sublist.add(this.get(i)); }
        return sublist.build();
    }

    private int toIndex(int cyclicIndex) {
        cyclicIndex %= this.wrapped.size();
        return (cyclicIndex + this.wrapped.size()) % this.wrapped.size();
    }
}
