package mctmods.immersivetechnology.common.gui.helper;

import blusunrize.immersiveengineering.api.energy.IMutableEnergyStorage;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class GenericContainerData<T> {
    private final GenericDataSerializers.DataSerializer<T> serializer;
    private final Supplier<T> get;
    private final Consumer<T> set;
    private T current;

    public GenericContainerData(GenericDataSerializers.DataSerializer<T> serializer, Supplier<T> get, Consumer<T> set) {
        this.serializer = serializer;
        this.get = get;
        this.set = set;
    }

    public static GenericContainerData<Integer> int32(Supplier<Integer> get, Consumer<Integer> set) { return new GenericContainerData<>(GenericDataSerializers.INT32, get, set); }
    public static GenericContainerData<Integer> energy(IMutableEnergyStorage storage) {
        Objects.requireNonNull(storage);
        Supplier<Integer> getEnergy = storage::getEnergyStored;
        Consumer<Integer> setEnergy = storage::setStoredEnergy;
        return int32(getEnergy, setEnergy);
    }

    public static GenericContainerData<FluidStack> fluid(FluidTank tank) {
        Objects.requireNonNull(tank);
        GenericDataSerializers.DataSerializer<FluidStack> serializer = GenericDataSerializers.FLUID_STACK;
        Supplier<FluidStack> getFluid = tank::getFluid;
        return new GenericContainerData<>(serializer, getFluid, tank::setFluid);
    }

    public static GenericContainerData<ItemStack> itemStack(Supplier<ItemStack> get, Consumer<ItemStack> set) { return new GenericContainerData<>(GenericDataSerializers.ITEM_STACK, get, set); }

    @SuppressWarnings("unused")
    public static GenericContainerData<Float> float32(Supplier<Float> get, Consumer<Float> set) { return new GenericContainerData<>(GenericDataSerializers.FLOAT, get, set); }

    public boolean needsUpdate() {
        T newValue = this.get.get();
        if (newValue == null && this.current == null) { return false; }
        else if (this.current != null && newValue != null && this.serializer.equals().test(this.current, newValue)) { return false; }
        else { this.current = this.serializer.copy().apply(newValue); return true; }
    }

    @SuppressWarnings("unchecked")
    public void processSync(Object receivedData) { this.set.accept(this.serializer.copy().apply((T)receivedData)); }

    public GenericDataSerializers.DataPair<T> dataPair() { return new GenericDataSerializers.DataPair<>(this.serializer, this.current); }
}
