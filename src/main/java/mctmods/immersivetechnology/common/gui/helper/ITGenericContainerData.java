package mctmods.immersivetechnology.common.gui.helper;

import blusunrize.immersiveengineering.api.energy.IMutableEnergyStorage;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class ITGenericContainerData<T> {
    private final ITGenericDataSerializers.DataSerializer<T> serializer;
    private final Supplier<T> get;
    private final Consumer<T> set;
    private T current;

    public ITGenericContainerData(ITGenericDataSerializers.DataSerializer<T> serializer, Supplier<T> get, Consumer<T> set) {
        this.serializer = serializer;
        this.get = get;
        this.set = set;
    }

    @SuppressWarnings("unused")
    public ITGenericContainerData(ITGenericDataSerializers.DataSerializer<T> serializer, GetterAndSetter<T> io) {
        this.serializer = serializer;
        this.get = io.getter();
        this.set = io.setter();
    }

    public static ITGenericContainerData<Integer> int32(Supplier<Integer> get, Consumer<Integer> set) { return new ITGenericContainerData<>(ITGenericDataSerializers.INT32, get, set); }
    public static ITGenericContainerData<Integer> energy(IMutableEnergyStorage storage) {
        Objects.requireNonNull(storage);
        Supplier<Integer> getEnergy = storage::getEnergyStored;
        Consumer<Integer> setEnergy = storage::setStoredEnergy;
        return int32(getEnergy, setEnergy);
    }

    public static ITGenericContainerData<FluidStack> fluid(FluidTank tank) {
        Objects.requireNonNull(tank);
        ITGenericDataSerializers.DataSerializer<FluidStack> serializer = ITGenericDataSerializers.FLUID_STACK;
        Supplier<FluidStack> getFluid = tank::getFluid;
        return new ITGenericContainerData<>(serializer, getFluid, tank::setFluid);
    }

    @SuppressWarnings("unused")
    public static ITGenericContainerData<Float> float32(Supplier<Float> get, Consumer<Float> set) { return new ITGenericContainerData<>(ITGenericDataSerializers.FLOAT, get, set); }

    public boolean needsUpdate() {
        T newValue = this.get.get();
        if (newValue == null && this.current == null) { return false; }
        else if (this.current != null && newValue != null && this.serializer.equals().test(this.current, newValue)) { return false; }
        else { this.current = this.serializer.copy().apply(newValue); return true; }
    }

    @SuppressWarnings("unchecked")
    public void processSync(Object receivedData) { this.current = (T)receivedData; this.set.accept(this.serializer.copy().apply(this.current)); }

    public ITGenericDataSerializers.DataPair<T> dataPair() { return new ITGenericDataSerializers.DataPair<>(this.serializer, this.current); }
}
