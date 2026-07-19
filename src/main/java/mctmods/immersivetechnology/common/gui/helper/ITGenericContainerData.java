package mctmods.immersivetechnology.common.gui.helper;

import blusunrize.immersiveengineering.api.energy.IMutableEnergyStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    public static ITGenericContainerData<ItemStack> itemStack(Supplier<ItemStack> get, Consumer<ItemStack> set) { return new ITGenericContainerData<>(ITGenericDataSerializers.ITEM_STACK, get, set); }

    @SuppressWarnings("unused")
    public static ITGenericContainerData<Float> float32(Supplier<Float> get, Consumer<Float> set) { return new ITGenericContainerData<>(ITGenericDataSerializers.FLOAT, get, set); }

    public boolean needsUpdate() {
        T newValue = this.get.get();
        if (newValue == null && this.current == null) { return false; }
        else if (this.current != null && newValue != null && this.serializer.equals().test(this.current, newValue)) { return false; }
        else { this.current = this.serializer.copy().apply(newValue); return true; }
    }

    @SuppressWarnings("unchecked")
    public void processSync(Object receivedData) { this.set.accept(this.serializer.copy().apply((T)receivedData)); }

    public ITGenericDataSerializers.DataPair<T> dataPair() { return new ITGenericDataSerializers.DataPair<>(this.serializer, this.current); }
}
