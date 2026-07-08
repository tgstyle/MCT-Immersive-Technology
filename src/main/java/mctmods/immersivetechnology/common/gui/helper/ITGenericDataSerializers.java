package mctmods.immersivetechnology.common.gui.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public class ITGenericDataSerializers {
    private static final List<DataSerializer<?>> SERIALIZERS = new ArrayList<>();
    public static final DataSerializer<Integer> INT32 = register(RegistryFriendlyByteBuf::readVarInt, RegistryFriendlyByteBuf::writeVarInt);
    public static final DataSerializer<FluidStack> FLUID_STACK = register(
            FluidStack.OPTIONAL_STREAM_CODEC::decode,
            FluidStack.OPTIONAL_STREAM_CODEC::encode,
            FluidStack::copy,
            FluidStack::matches
    );
    public static final DataSerializer<Float> FLOAT = register(RegistryFriendlyByteBuf::readFloat, RegistryFriendlyByteBuf::writeFloat);
    public static final DataSerializer<Double> DOUBLE = register(RegistryFriendlyByteBuf::readDouble, RegistryFriendlyByteBuf::writeDouble);
    public static final DataSerializer<ItemStack> ITEM_STACK = register(
            ItemStack.OPTIONAL_STREAM_CODEC::decode,
            ItemStack.OPTIONAL_STREAM_CODEC::encode,
            ItemStack::copy,
            ItemStack::matches
    );

    private static <T> DataSerializer<T> register(Function<RegistryFriendlyByteBuf, T> read, BiConsumer<RegistryFriendlyByteBuf, T> write) { return register(read, write, (t) -> t, Objects::equals); }
    private static <T> DataSerializer<T> register(Function<RegistryFriendlyByteBuf, T> read, BiConsumer<RegistryFriendlyByteBuf, T> write, UnaryOperator<T> copy, BiPredicate<T, T> equals) {
        DataSerializer<T> serializer = new DataSerializer<>(read, write, copy, equals, SERIALIZERS.size());
        SERIALIZERS.add(serializer);
        return serializer;
    }

    public static DataPair<?> read(RegistryFriendlyByteBuf buffer) {
        DataSerializer<?> serializer = SERIALIZERS.get(buffer.readVarInt());
        return serializer.read(buffer);
    }

    public record DataSerializer<T>(Function<RegistryFriendlyByteBuf, T> read, BiConsumer<RegistryFriendlyByteBuf, T> write, UnaryOperator<T> copy, BiPredicate<T, T> equals, int id) {
        public DataPair<T> read(RegistryFriendlyByteBuf from) { return new DataPair<>(this, this.read().apply(from)); }
    }

    public record DataPair<T>(DataSerializer<T> serializer, T data) {
        public void write(RegistryFriendlyByteBuf to) { to.writeVarInt(this.serializer.id()); this.serializer.write().accept(to, this.data); }
    }
}
