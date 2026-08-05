package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.common.multiblocks.helper.*;
import mctmods.immersivetechnology.common.multiblocks.metal.process.DistillerProcess;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.DistillerRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.DistillerShape;
import mctmods.immersivetechnology.common.fluids.helper.ArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.MarkableFluidTank;
import mctmods.immersivetechnology.core.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.core.lib.ModSound;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.util.CachedRecipe;
import mctmods.immersivetechnology.core.util.Utils;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.BiFunction;

public class DistillerLogic implements IMultiblockLogic<DistillerLogic.State>, IServerTickableComponent<DistillerLogic.State>, IClientTickableComponent<DistillerLogic.State>, IPressurizedFluidOutput<DistillerLogic.State> {
    public static final int SLOT_INPUT_FILLED = 0;
    public static final int SLOT_INPUT_EMPTY = 1;
    public static final int SLOT_OUTPUT_EMPTY = 2;
    public static final int SLOT_OUTPUT_FILLED = 3;
    public static final int OUTPUT_SLOT = 4;

    public static int inputTankCapacity() { return ServerConfig.distillerInputTankCapacity; }
    public static int outputTankCapacity() { return ServerConfig.distillerOutputTankCapacity; }
    public static int energyCapacity() { return ServerConfig.distillerEnergyCapacity; }

    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(DistillerShape.DATA.pointsOfInterest);

    public static final BlockPos REDSTONE_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "redstone0").getFirst();
    public static final List<BlockPos> INPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_input0");
    public static final List<BlockPos> OUTPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output0");
    private static final List<BlockPos> ENERGY_INPUT_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "energy_input0");
    private static final RelativeBlockFace ENERGY_INPUT_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "energy_input0");
    public static final MultiblockFace ITEM_OUTPUT_POI = new MultiblockFace(MultiblockPOIHelper.getFacing(RAW_POIS, "item_output0"), MultiblockPOIHelper.getPosList(RAW_POIS, "item_output0").getFirst());
    private static final RelativeBlockFace INPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_input0");
    private static final RelativeBlockFace OUTPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output0");

    @Override public List<BlockPos> getOutputPositions() { return OUTPUT_FLUID_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FLUID_FACING); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output()); }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        List<BlockPos> soundPosList = MultiblockPOIHelper.getPosList(RAW_POIS, "sound0");
        BlockPos soundBlockPos = soundPosList.getFirst();
        Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(soundBlockPos.getX() + 0.5, soundBlockPos.getY() + 0.5, soundBlockPos.getZ() + 0.5));
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { return; }
        float distSq = (float) player.distanceToSqr(soundPos);
        float attenuation = Math.max(distSq / 32f, 1f);
        float vol = 1f / attenuation;
        if (state.active && vol > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
            state.isSoundPlaying = ModSound.startSound(
                    () -> state.active, ctx.isValid(), soundPos, Sounds.distiller,
                    () -> {
                        LocalPlayer p = Minecraft.getInstance().player;
                        if (p == null) { return 0f; }
                        float a = (float) Math.max(p.distanceToSqr(soundPos) / 32f, 1f);
                        return 1f / a;
                    },
                    () -> 1f
            );
        }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        state.energy.updateAverage();
        int prevEnergy = state.energy.getEnergyStored();
        boolean prevTanksDirty = state.tanksDirty;
        boolean prevInventoryDirty = state.inventoryDirty;
        boolean wasActive = state.active;
        RecipeHolder<DistillerRecipe> recipeHolder = state.lastRecipeCache;
        FluidStack currentFluid = state.tanks.input().getFluid();
        if (recipeHolder == null || !recipeHolder.value().matches(currentFluid)) {
            recipeHolder = state.recipeGetter.apply(ctx.getLevel().getRawLevel(), currentFluid);
            state.lastRecipeCache = recipeHolder;
        }
        state.active = state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));
        tryEnqueueProcess(state, ctx.getLevel().getRawLevel(), recipeHolder);
        tryEmptyContainer(state.tanks.input(), state.inventory);
        FluidUtils.fillFluidContainer(state.tanks.output(), SLOT_OUTPUT_EMPTY, SLOT_OUTPUT_FILLED, state.inventory);
        pumpOutputs(ctx);
        IItemHandlerModifiable inventory = state.inventory;
        ItemStack drainedContainer = inventory.getStackInSlot(SLOT_INPUT_EMPTY);
        if (!drainedContainer.isEmpty()) {
            drainedContainer = Utils.insertStackIntoInventory(state.outputRef, drainedContainer, false);
            inventory.setStackInSlot(SLOT_INPUT_EMPTY, drainedContainer);
        }
        ItemStack filledContainer = inventory.getStackInSlot(SLOT_OUTPUT_FILLED);
        if (!filledContainer.isEmpty()) {
            filledContainer = Utils.insertStackIntoInventory(state.outputRef, filledContainer, false);
            inventory.setStackInSlot(SLOT_OUTPUT_FILLED, filledContainer);
        }
        ItemStack itemOutput = inventory.getStackInSlot(OUTPUT_SLOT);
        if (!itemOutput.isEmpty()) {
            itemOutput = Utils.insertStackIntoInventory(state.outputRef, itemOutput, false);
            inventory.setStackInSlot(OUTPUT_SLOT, itemOutput);
        }
        boolean activeChanged = wasActive != state.active;
        int currentEnergy = state.energy.getEnergyStored();
        boolean energyChanged = prevEnergy != currentEnergy;
        boolean tanksChanged = prevTanksDirty != state.tanksDirty;
        boolean inventoryChanged = prevInventoryDirty != state.inventoryDirty;
        int newQueueSize = state.processor.getQueueSize();
        boolean queueSizeChanged = newQueueSize != state.queueSize;
        if (queueSizeChanged) { state.queueSize = newQueueSize; }
        int maxEnergy = state.energy.getMaxEnergyStored();
        int newComparatorValue = maxEnergy > 0 ? (15 * state.energy.getEnergyStored()) / maxEnergy : 0;
        boolean comparatorChanged = newComparatorValue != state.lastComparatorValue;
        if (comparatorChanged) { ctx.setComparatorOutputFor(REDSTONE_POI, newComparatorValue); state.lastComparatorValue = newComparatorValue; }
        boolean update = activeChanged || energyChanged || tanksChanged || inventoryChanged || queueSizeChanged || comparatorChanged;
        if (update) { ctx.markMasterDirty(); ctx.requestMasterBESync(); }
    }

    private void tryEmptyContainer(IFluidHandler tank, IItemHandlerModifiable inv) {
        ItemStack filledContainer = inv.getStackInSlot(DistillerLogic.SLOT_INPUT_FILLED);
        if (filledContainer.isEmpty()) { return; }
        FluidActionResult result = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.SIMULATE);
        if (!result.isSuccess()) { return; }
        ItemStack emptyContainer = result.getResult();
        ItemStack outputStack = inv.getStackInSlot(DistillerLogic.SLOT_INPUT_EMPTY);
        if (!outputStack.isEmpty() && !ItemStack.isSameItemSameComponents(outputStack, emptyContainer)) { return; }
        if (outputStack.getCount() + emptyContainer.getCount() > emptyContainer.getMaxStackSize()) { return; }
        result = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.EXECUTE);
        filledContainer.shrink(1);
        inv.setStackInSlot(DistillerLogic.SLOT_INPUT_FILLED, filledContainer);
        if (outputStack.isEmpty()) { inv.setStackInSlot(DistillerLogic.SLOT_INPUT_EMPTY, result.getResult()); }
        else { outputStack.grow(result.getResult().getCount()); }
    }

    private void tryEnqueueProcess(State state, Level level, RecipeHolder<DistillerRecipe> recipeHolder) {
        if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) { return; }
        if (recipeHolder == null) { return; }
        DistillerRecipe recipe = recipeHolder.value();
        FluidStack inputFluid = state.tanks.input().getFluid();
        if (inputFluid.getAmount() < recipe.getInputAmount()) { return; }
        FluidStack outputFluid = recipe.fluidOutput;
        if (outputFluid != null && !outputFluid.isEmpty() && state.tanks.output().getFluidAmount() + outputFluid.getAmount() > state.tanks.output().getCapacity()) { return; }
        ItemStack itemOutput = recipe.itemOutput;
        if (!itemOutput.isEmpty()) {
            ItemStack currentOutput = state.inventory.getStackInSlot(OUTPUT_SLOT);
            if (!currentOutput.isEmpty() && (!ItemStack.isSameItemSameComponents(currentOutput, itemOutput) || currentOutput.getCount() + itemOutput.getCount() > currentOutput.getMaxStackSize())) { return; }
        }
        DistillerProcess process = new DistillerProcess(recipeHolder);
        state.processor.addProcessToQueue(process, level, false);
    }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.EnergyStorage.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (ENERGY_INPUT_POIS.contains(localPos) && (side == null || side == ENERGY_INPUT_FACING)) {
                return state.energy;
            }
            return null;
        });
        register.register(Capabilities.FluidHandler.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (INPUT_FLUID_POIS.contains(localPos) && (side == null || side == INPUT_FLUID_FACING)) {
                return state.inputCap;
            }
            if (OUTPUT_FLUID_POIS.contains(localPos) && (side == null || side == OUTPUT_FLUID_FACING)) {
                return state.outputCap;
            }
            return null;
        });
        register.register(Capabilities.ItemHandler.BLOCK, (state, position) -> {
            if (position.posInMultiblock().equals(ITEM_OUTPUT_POI.posInMultiblock()) && (position.side() == null || position.side() == ITEM_OUTPUT_POI.face())) {
                return state.itemOutputCap;
            }
            return state.invCap;
        });
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { MultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return DistillerShape.GETTER; }

    public static class State implements IMultiblockState, IProcessContext.IProcessContextInMachine<DistillerRecipe>, IDisplayContext {
        public final BiFunction<Level, FluidStack, RecipeHolder<DistillerRecipe>> recipeGetter = CachedRecipe.cached(DistillerRecipe::findRecipe);
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final DistillerTank tanks;
        public IFluidHandler inputCap;
        public IFluidHandler outputCap;
        public IItemHandler invCap;
        public IItemHandler itemOutputCap;
        public Supplier<IItemHandler> outputRef;
        public final SlotwiseItemHandler inventory;
        private final IFluidTank[] tankArray;
        public final MultiblockProcessor.InMachineProcessor<DistillerRecipe> processor;
        public AveragingEnergyStorage energy;
        public boolean active;
        public BooleanSupplier isSoundPlaying = () -> false;
        public boolean tanksDirty = false;
        public boolean inventoryDirty = false;
        public int queueSize = 0;
        public int lastComparatorValue = -1;
        public RecipeHolder<DistillerRecipe> lastRecipeCache;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); this.tanksDirty = true; this.inventoryDirty = true; };
            this.tanks = new DistillerTank(() -> { onChanged.run(); this.tanksDirty = true; });
            this.tankArray = new IFluidTank[]{tanks.input(), tanks.output()};
            inventory = new SlotwiseItemHandler(
                    List.of(
                            SlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            SlotwiseItemHandler.IOConstraint.OUTPUT,
                            SlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            SlotwiseItemHandler.IOConstraint.OUTPUT,
                            SlotwiseItemHandler.IOConstraint.OUTPUT
                    ),
                    () -> { onChanged.run(); this.inventoryDirty = true; }
            );
            this.inputCap = new ArrayFluidHandler(tanks.input(), false, true, () -> { onChanged.run(); this.tanksDirty = true; });
            this.outputCap = new ArrayFluidHandler(tanks.output(), true, false, () -> { onChanged.run(); this.tanksDirty = true; });
            this.invCap = inventory;
            this.energy = new SyncEnergyStorage(energyCapacity(), onChanged);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(1, 0f, 1, markDirty, DistillerRecipe.RECIPES::getById);
            this.itemOutputCap = new WrappingItemHandler(
                    inventory,
                    false,
                    true,
                    List.of(
                            new WrappingItemHandler.IntRange(SLOT_INPUT_EMPTY, SLOT_INPUT_EMPTY + 1),
                            new WrappingItemHandler.IntRange(SLOT_OUTPUT_FILLED, SLOT_OUTPUT_FILLED + 1),
                            new WrappingItemHandler.IntRange(OUTPUT_SLOT, OUTPUT_SLOT + 1)
                    )
            );
            this.outputRef = ctx.getCapabilityAt(Capabilities.ItemHandler.BLOCK, ITEM_OUTPUT_POI);
        }

        public SlotwiseItemHandler getInventory() { return inventory; }

        public DistillerTank getTanks() { return tanks; }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.put("energy", energy.serializeNBT(provider));
            nbt.put("tanks", this.tanks.toNBT(provider));
            nbt.put("processor", processor.toNBT(provider));
            nbt.put("inventory", inventory.serializeNBT(provider));
            nbt.putBoolean("active", active);
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            CompoundTag energyTag = nbt.getCompound("energy");
            if (!energyTag.isEmpty()) {
                energy.deserializeNBT(provider, energyTag);
            }
            this.tanks.readNBT(nbt.getCompound("tanks"), provider);
            this.processor.fromNBT(nbt.getList("processor", Tag.TAG_COMPOUND), DistillerProcess::new, provider);
            this.inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
            active = nbt.getBoolean("active");
            tanksDirty = false;
            inventoryDirty = false;
        }

        @Override public void writeSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display, provider);
            nbt.put("display", display);
        }

        @Override public void readSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            if (nbt.contains("display", Tag.TAG_COMPOUND)) {
                readDisplaySyncNBT(nbt.getCompound("display"), provider);
            }
        }

        @Override public AveragingEnergyStorage getEnergy() { return energy; }

        @Override public IFluidTank[] getInternalTanks() { return tankArray; }

        @Override public int[] getOutputTanks() { return new int[]{1}; }

        @Override public int[] getOutputSlots() { return new int[]{OUTPUT_SLOT}; }

        @Override public boolean isActive() { return active; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.put("energy", energy.serializeNBT(provider));
            nbt.put("inventory", inventory.serializeNBT(provider));
            nbt.putInt("queueSize", queueSize);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            if (energy == null) { energy = new SyncEnergyStorage(energyCapacity(), () -> {}); }
            CompoundTag energyTag = nbt.getCompound("energy");
            if (!energyTag.isEmpty()) {
                energy.deserializeNBT(provider, energyTag);
            }
            inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
            queueSize = nbt.getInt("queueSize");
            tanksDirty = false;
            inventoryDirty = false;
        }
    }

    public record DistillerTank(MarkableFluidTank input, MarkableFluidTank output) {
        public DistillerTank(Runnable markDirty) {
            this(new MarkableFluidTank(inputTankCapacity(), v -> markDirty.run()), new MarkableFluidTank(outputTankCapacity(), v -> markDirty.run()));
        }

        public static DistillerTank makeClient() { return new DistillerTank(() -> {}); }

        public CompoundTag toNBT(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.put("input", this.input().writeToNBT(provider, new CompoundTag()));
            tag.put("output", this.output().writeToNBT(provider, new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag, HolderLookup.Provider provider) {
            this.input().readFromNBT(provider, tag.getCompound("input"));
            this.output().readFromNBT(provider, tag.getCompound("output"));
        }

        @SuppressWarnings("unused")
        public int getCapacity() { return Math.max(inputTankCapacity(), outputTankCapacity()); }
    }

    private static class SyncEnergyStorage extends AveragingEnergyStorage {
        private final Runnable onChanged;

        public SyncEnergyStorage(int capacity, Runnable onChanged) {
            super(capacity);
            this.onChanged = onChanged;
        }

        @Override public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (received > 0 && !simulate) { onChanged.run(); }
            return received;
        }

        @Override public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (extracted > 0 && !simulate) { onChanged.run(); }
            return extracted;
        }

        public void setStoredEnergy(int energy) {
            int prev = getEnergyStored();
            super.setStoredEnergy(energy);
            if (energy != prev && onChanged != null) { onChanged.run(); }
        }
    }
}
