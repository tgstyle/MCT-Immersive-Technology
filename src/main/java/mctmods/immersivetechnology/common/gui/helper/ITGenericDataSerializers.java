package mctmods.immersivetechnology.common.gui.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeFriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;

public class ITGenericDataSerializers {
    private static final List<DataSerializer<?>> SERIALIZERS = new ArrayList<>();
    public static final DataSerializer<Integer> INT32 = register(FriendlyByteBuf::readVarInt, FriendlyByteBuf::writeVarInt);
    public static final DataSerializer<FluidStack> FLUID_STACK = register(IForgeFriendlyByteBuf::readFluidStack, IForgeFriendlyByteBuf::writeFluidStack, FluidStack::copy, FluidStack::isFluidStackIdentical);
    public static final DataSerializer<Float> FLOAT = register(FriendlyByteBuf::readFloat, FriendlyByteBuf::writeFloat);
    public static final DataSerializer<ItemStack> ITEM_STACK = register(FriendlyByteBuf::readItem, FriendlyByteBuf::writeItem, ItemStack::copy, Object::equals);

    private static <T> DataSerializer<T> register(Function<FriendlyByteBuf, T> read, BiConsumer<FriendlyByteBuf, T> write) { return register(read, write, (t) -> t, Objects::equals); }
    private static <T> DataSerializer<T> register(Function<FriendlyByteBuf, T> read, BiConsumer<FriendlyByteBuf, T> write, UnaryOperator<T> copy, BiPredicate<T, T> equals) {
        DataSerializer<T> serializer = new DataSerializer<>(read, write, copy, equals, SERIALIZERS.size());
        SERIALIZERS.add(serializer);
        return serializer;
    }

    public static DataPair<?> read(FriendlyByteBuf buffer) {
        DataSerializer<?> serializer = SERIALIZERS.get(buffer.readVarInt());
        return serializer.read(buffer);
    }

    public record DataSerializer<T>(Function<FriendlyByteBuf, T> read, BiConsumer<FriendlyByteBuf, T> write, UnaryOperator<T> copy, BiPredicate<T, T> equals, int id) {
        public DataPair<T> read(FriendlyByteBuf from) { return new DataPair<>(this, this.read().apply(from)); }
    }

    public record DataPair<T>(DataSerializer<T> serializer, T data) {
        public void write(FriendlyByteBuf to) { to.writeVarInt(this.serializer.id()); this.serializer.write().accept(to, this.data); }
    }
}
