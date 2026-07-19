package mctmods.immersivetechnology.core.util;

import java.util.function.BiFunction;

public class CachedRecipe {
    @FunctionalInterface public interface HintedGetter2<K1, K2, R> { R get(K1 k1, K2 k2, R hint); }

    @FunctionalInterface public interface HintedGetter3<K1, K2, K3, R> { R get(K1 k1, K2 k2, K3 k3, R hint); }

    @FunctionalInterface public interface TriFunction<K1, K2, K3, R> { R apply(K1 k1, K2 k2, K3 k3); }

    public static <K1, K2, R> BiFunction<K1, K2, R> cached(HintedGetter2<K1, K2, R> getRecipeWithHint) {
        Object[] holder = new Object[1];
        return (k1, k2) -> {
            @SuppressWarnings("unchecked") R hint = (R) holder[0];
            R result = getRecipeWithHint.get(k1, k2, hint);
            holder[0] = result;
            return result;
        };
    }

    public static <K1, K2, K3, R> TriFunction<K1, K2, K3, R> cached3(HintedGetter3<K1, K2, K3, R> getRecipeWithHint) {
        Object[] holder = new Object[1];
        return (k1, k2, k3) -> {
            @SuppressWarnings("unchecked") R hint = (R) holder[0];
            R result = getRecipeWithHint.get(k1, k2, k3, hint);
            holder[0] = result;
            return result;
        };
    }
}
