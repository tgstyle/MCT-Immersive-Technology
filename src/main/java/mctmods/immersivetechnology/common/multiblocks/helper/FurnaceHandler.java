package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

@SuppressWarnings("unused")
public class FurnaceHandler<R extends IESerializableRecipe> {
    private double process = 0;
    private double processMax = 0;
    private double burnTime = 0;
    private double lastBurnTime = 0;
    public final ITFurnaceStateView stateView = new ITFurnaceStateView();
    private final int fuelSlot;
    private final List<InputSlot<R>> inputs;
    private final List<OutputSlot<R>> outputs;
    private final ToIntFunction<R> getProcessingTime;
    private final Runnable setChanged;

    public FurnaceHandler(int fuelSlot, List<InputSlot<R>> inputs, List<OutputSlot<R>> outputs, ToIntFunction<R> getProcessingTime, Runnable setChanged) { this.fuelSlot = fuelSlot; this.inputs = inputs; this.outputs = outputs; this.getProcessingTime = getProcessingTime; this.setChanged = setChanged; }

    public boolean tickServer(IMultiblockContext<? extends IFurnaceEnvironment<R>> ctx) {
        boolean active = false;
        final IFurnaceEnvironment<R> env = ctx.getState();
        final Level level = ctx.getLevel().getRawLevel();
        if (burnTime > 0) {
            double processSpeed = env.getProcessSpeed(ctx.getLevel());
            burnTime -= processSpeed;
            if (process > 0) {
                if (isAnyInputEmpty(env.getInventory())) { process = 0; processMax = 0; }
                else {
                    R recipe = getRecipe(env, level);
                    if (recipe != null && getProcessTime(recipe) != processMax) { processMax = 0; process = 0; }
                    else { process -= processSpeed; processSpeed = 0; active = true; }
                }
                setChanged.run();
            }
            if (process <= 0) {
                if (processMax > 0) { doRecipeIO(env, level); processMax = 0; burnTime -= process; }
                R recipe = getRecipe(env, level);
                if (recipe != null) { final double time = getProcessTime(recipe); this.process = time - processSpeed; this.processMax = time; active = true; }
            }
        }
        if (burnTime <= 0 && getRecipe(env, level) != null) {
            final IItemHandlerModifiable inv = env.getInventory();
            final ItemStack fuel = inv.getStackInSlot(fuelSlot);
            final int addedBurntime = env.getBurnTimeOf(level, fuel);
            if (addedBurntime > 0) {
                lastBurnTime = addedBurntime;
                burnTime += lastBurnTime;
                if (fuel.hasCraftingRemainingItem() && fuel.getCount() == 1) inv.setStackInSlot(fuelSlot, fuel.getCraftingRemainingItem());
                else fuel.shrink(1);
                setChanged.run();
            }
        }
        if (!active) env.turnOff(ctx.getLevel());
        return active;
    }

    public Tag toNBT() {
        final CompoundTag result = new CompoundTag();
        result.putDouble("process", process);
        result.putDouble("processMax", processMax);
        result.putDouble("burnTime", burnTime);
        result.putDouble("lastBurnTime", lastBurnTime);
        return result;
    }

    public void readNBT(Tag nbt) {
        if (!(nbt instanceof CompoundTag compound)) return;
        process = compound.getDouble("process");
        processMax = compound.getDouble("processMax");
        burnTime = compound.getDouble("burnTime");
        lastBurnTime = compound.getDouble("lastBurnTime");
    }

    private boolean isAnyInputEmpty(IItemHandler inv) {
        for (InputSlot<R> i : inputs) if (inv.getStackInSlot(i.slotIndex).isEmpty()) return true;
        return false;
    }

    @Nullable private R getRecipe(IFurnaceEnvironment<R> env, Level level) {
        R recipe = env.getRecipeForInput(level);
        if (recipe == null) return null;
        final IItemHandlerModifiable inv = env.getInventory();
        for (OutputSlot<R> out : outputs) {
            ItemStack currentStack = inv.getStackInSlot(out.slotIndex);
            ItemStack outputSlot = out.get(recipe);
            if (!currentStack.isEmpty()) {
                if (!ItemStack.isSameItem(currentStack, outputSlot)) return null;
                else if (currentStack.getCount() + outputSlot.getCount() > inv.getSlotLimit(out.slotIndex)) return null;
            }
        }
        return recipe;
    }

    private void doRecipeIO(IFurnaceEnvironment<R> env, Level level) {
        R recipe = getRecipe(env, level);
        if (recipe == null) return;
        final IItemHandlerModifiable inv = env.getInventory();
        for (InputSlot<R> slot : inputs) {
            int reqSize = inputs.stream().map(matchSlot -> matchSlot.get(recipe)).filter(ingredient -> ingredient.test(inv.getStackInSlot(slot.slotIndex))).mapToInt(IngredientWithSize::getCount).findFirst().orElse(0);
            inv.getStackInSlot(slot.slotIndex).shrink(reqSize);
        }
        for (OutputSlot<R> slot : outputs) {
            ItemStack result = slot.get(recipe);
            if (!result.isEmpty()) {
                if (!inv.getStackInSlot(slot.slotIndex).isEmpty()) inv.getStackInSlot(slot.slotIndex).grow(result.getCount());
                else inv.setStackInSlot(slot.slotIndex, result.copy());
            }
        }
    }

    private int getProcessTime(R recipe) { return getProcessingTime.applyAsInt(recipe); }

    public interface IFurnaceEnvironment<R extends IESerializableRecipe> {
        net.neoforged.neoforge.items.IItemHandlerModifiable getInventory();

        @Nullable R getRecipeForInput(Level level);

        int getBurnTimeOf(Level level, ItemStack fuel);

        double getProcessSpeed(IMultiblockLevel level);

        void turnOff(IMultiblockLevel level);
    }

    public class ITFurnaceStateView implements ContainerData {
        public static final int LAST_BURN_TIME = 0;
        public static final int BURN_TIME = 1;
        public static final int PROCESS_MAX = 2;
        public static final int CURRENT_PROCESS = 3;
        public static final int NUM_SLOTS = 4;

        public static int getLastBurnTime(ContainerData data) { return data.get(LAST_BURN_TIME); }

        public static int getBurnTime(ContainerData data) { return data.get(BURN_TIME); }

        public static int getMaxProcess(ContainerData data) { return data.get(PROCESS_MAX); }

        public static int getProcess(ContainerData data) { return data.get(CURRENT_PROCESS); }

        @Override public int get(int index) {
            return switch (index) {
                case LAST_BURN_TIME -> (int)lastBurnTime;
                case BURN_TIME -> (int)burnTime;
                case PROCESS_MAX -> (int)processMax;
                case CURRENT_PROCESS -> (int)process;
                default -> throw new IllegalArgumentException("Unknown index " + index);
            };
        }

        @Override public void set(int index, int value) {
            switch (index) {
                case LAST_BURN_TIME: lastBurnTime = value; break;
                case BURN_TIME: burnTime = value; break;
                case PROCESS_MAX: processMax = value; break;
                case CURRENT_PROCESS: process = value; break;
                default: throw new IllegalArgumentException("Unknown index " + index);
            }
        }

        @Override public int getCount() { return NUM_SLOTS; }
    }

    public record InputSlot<R>(Function<R, IngredientWithSize> getFromRecipe, int slotIndex) {
        public IngredientWithSize get(R recipe) { return getFromRecipe.apply(recipe);}
    }

    public record OutputSlot<R>(Function<R, Lazy<ItemStack>> getFromRecipe, int slotIndex) {
        public ItemStack get(R recipe) { return getFromRecipe.apply(recipe).get();}
    }
}
