package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.common.multiblocks.helper.*;
import mctmods.immersivetechnology.common.multiblocks.metal.process.DistillerProcess;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.DistillerRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.DistillerShape;
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.core.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.core.lib.ITSound;
import mctmods.immersivetechnology.core.registration.ITSounds;
import mctmods.immersivetechnology.core.ITServerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class DistillerLogic implements IMultiblockLogic<DistillerLogic.State>, IServerTickableComponent<DistillerLogic.State>, IClientTickableComponent<DistillerLogic.State>, ITIPressurizedFluidOutput<DistillerLogic.State> {
    public static final int SLOT_INPUT_FILLED = 0;
    public static final int SLOT_INPUT_EMPTY = 1;
    public static final int SLOT_OUTPUT_EMPTY = 2;
    public static final int SLOT_OUTPUT_FILLED = 3;
    public static final int OUTPUT_SLOT = 4;

    public static final int INPUT_TANK_CAPACITY = ITServerConfig.distillerInputTankCapacity;
    public static final int OUTPUT_TANK_CAPACITY = ITServerConfig.distillerOutputTankCapacity;
    public static final int ENERGY_CAPACITY = ITServerConfig.distillerEnergyCapacity;

    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(DistillerShape.DATA.pointsOfInterest);

    public static final BlockPos REDSTONE_POI = ITMultiblockPOIHelper.getPosList(RAW_POIS, "redstone0").get(0);
    public static final List<BlockPos> INPUT_FLUID_POIS = ITMultiblockPOIHelper.getPosList(RAW_POIS, "fluid_input0");
    public static final List<BlockPos> OUTPUT_FLUID_POIS = ITMultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output0");
    private static final List<BlockPos> ENERGY_INPUT_POIS = ITMultiblockPOIHelper.getPosList(RAW_POIS, "energy_input0");
    private static final RelativeBlockFace ENERGY_INPUT_FACING = ITMultiblockPOIHelper.getFacing(RAW_POIS, "energy_input0");
    public static final MultiblockFace ITEM_OUTPUT_POI = new MultiblockFace(ITMultiblockPOIHelper.getFacing(RAW_POIS, "item_output0"), ITMultiblockPOIHelper.getPosList(RAW_POIS, "item_output0").get(0));
    private static final RelativeBlockFace INPUT_FLUID_FACING = ITMultiblockPOIHelper.getFacing(RAW_POIS, "fluid_input0");
    private static final RelativeBlockFace OUTPUT_FLUID_FACING = ITMultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output0");

    @Override public List<BlockPos> getOutputPositions() { return OUTPUT_FLUID_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FLUID_FACING); }

    @Override public List<ITMarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output); }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        List<BlockPos> soundPosList = ITMultiblockPOIHelper.getPosList(RAW_POIS, "sound0");
        BlockPos soundBlockPos = soundPosList.get(0);
        Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(soundBlockPos.getX() + 0.5, soundBlockPos.getY() + 0.5, soundBlockPos.getZ() + 0.5));
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { return; }
        float distSq = (float) player.distanceToSqr(soundPos);
        float attenuation = Math.max(distSq / 32f, 1f);
        float vol = 1f / attenuation;
        if (state.active && vol > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
            state.isSoundPlaying = ITSound.startSound(
                    () -> state.active, ctx.isValid(), soundPos, ITSounds.distiller,
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
        DistillerRecipe recipe = state.lastRecipeCache;
        if (recipe == null || !recipe.input.testIgnoringAmount(state.tanks.input.getFluid())) {
            recipe = DistillerRecipe.findRecipe(ctx.getLevel().getRawLevel(), state.tanks.input.getFluid());
            state.lastRecipeCache = recipe;
        }
        state.active = state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));
        tryEnqueueProcess(state, ctx.getLevel().getRawLevel(), recipe);
        tryEmptyContainer(state.tanks.input, state.inventory);
        FluidUtils.fillFluidContainer(state.tanks.output, SLOT_OUTPUT_EMPTY, SLOT_OUTPUT_FILLED, state.inventory);
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
        boolean update = activeChanged || energyChanged || tanksChanged || inventoryChanged;
        if (update) { ctx.markMasterDirty(); ctx.requestMasterBESync(); }
    }

    private void tryEmptyContainer(IFluidHandler tank, IItemHandlerModifiable inv) {
        ItemStack filledContainer = inv.getStackInSlot(DistillerLogic.SLOT_INPUT_FILLED);
        if (filledContainer.isEmpty()) { return; }
        FluidActionResult result = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.SIMULATE);
        if (!result.isSuccess()) { return; }
        ItemStack emptyContainer = result.getResult();
        ItemStack outputStack = inv.getStackInSlot(DistillerLogic.SLOT_INPUT_EMPTY);
        if (!outputStack.isEmpty() && !ItemHandlerHelper.canItemStacksStack(outputStack, emptyContainer)) { return; }
        if (outputStack.getCount() + emptyContainer.getCount() > emptyContainer.getMaxStackSize()) { return; }
        result = FluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.EXECUTE);
        filledContainer.shrink(1);
        inv.setStackInSlot(DistillerLogic.SLOT_INPUT_FILLED, filledContainer);
        if (outputStack.isEmpty()) { inv.setStackInSlot(DistillerLogic.SLOT_INPUT_EMPTY, result.getResult()); }
        else { outputStack.grow(result.getResult().getCount()); }
    }

    private void tryEnqueueProcess(State state, Level level, DistillerRecipe recipe) {
        if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) { return; }
        if (recipe == null) { return; }
        FluidStack inputFluid = state.tanks.input.getFluid();
        if (inputFluid.getAmount() < recipe.input.getAmount()) { return; }
        FluidStack outputFluid = recipe.fluidOutput;
        if (outputFluid != null && !outputFluid.isEmpty() && state.tanks.output.getFluidAmount() + outputFluid.getAmount() > state.tanks.output.getCapacity()) { return; }
        ItemStack itemOutput = recipe.itemOutput;
        if (!itemOutput.isEmpty()) {
            ItemStack currentOutput = state.inventory.getStackInSlot(OUTPUT_SLOT);
            if (!currentOutput.isEmpty() && (!ItemHandlerHelper.canItemStacksStack(currentOutput, itemOutput) || currentOutput.getCount() + itemOutput.getCount() > currentOutput.getMaxStackSize())) { return; }
        }
        DistillerProcess process = new DistillerProcess(recipe);
        process.setInputTanks(0);
        state.processor.addProcessToQueue(process, level, false);
    }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        State state = ctx.getState();
        BlockPos localPos = position.posInMultiblock();
        RelativeBlockFace side = position.side();
        if (cap == ForgeCapabilities.ENERGY) {
            if (ENERGY_INPUT_POIS.contains(localPos) && (side == null || side == ENERGY_INPUT_FACING)) { return state.energyCap.cast(ctx); }
        }
        else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (INPUT_FLUID_POIS.contains(localPos) && (side == null || side == INPUT_FLUID_FACING)) { return state.inputCap.cast(ctx); }
            if (OUTPUT_FLUID_POIS.contains(localPos) && (side == null || side == OUTPUT_FLUID_FACING)) { return state.outputCapSteam.cast(ctx); }
        }
        else if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (position.posInMultiblock().equals(ITEM_OUTPUT_POI.posInMultiblock()) && (position.side() == null || position.side() == ITEM_OUTPUT_POI.face())) { return state.itemOutputCap.cast(ctx); }
            return state.invCap.cast(ctx);
        }
        return LazyOptional.empty();
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { ITMultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return DistillerShape.GETTER; }

    public static class State implements IMultiblockState, ITIProcessContext.ProcessContextInMachine<DistillerRecipe>, ITIDisplayContext {
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final DistillerTank tanks;
        public final StoredCapability<IEnergyStorage> energyCap;
        public final StoredCapability<IFluidHandler> inputCap;
        public final StoredCapability<IFluidHandler> outputCapSteam;
        public final StoredCapability<IItemHandler> invCap;
        public final StoredCapability<IItemHandler> itemOutputCap;
        public final CapabilityReference<IItemHandler> outputRef;
        public final ITSlotwiseItemHandler inventory;
        private final IFluidTank[] tankArray;
        public final MultiblockProcessor.InMachineProcessor<DistillerRecipe> processor;
        public AveragingEnergyStorage energy;
        public boolean active;
        public BooleanSupplier isSoundPlaying = () -> false;
        public boolean tanksDirty = false;
        public boolean inventoryDirty = false;
        public DistillerRecipe lastRecipeCache;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); this.tanksDirty = true; this.inventoryDirty = true; };
            this.tanks = new DistillerTank(v -> { onChanged.run(); this.tanksDirty = true; });
            this.tankArray = new IFluidTank[]{tanks.input, tanks.output};
            inventory = new ITSlotwiseItemHandler(
                    List.of(
                            ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            ITSlotwiseItemHandler.IOConstraint.OUTPUT,
                            ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            ITSlotwiseItemHandler.IOConstraint.OUTPUT,
                            ITSlotwiseItemHandler.IOConstraint.OUTPUT
                    ),
                    () -> { onChanged.run(); this.inventoryDirty = true; }
            );
            this.inputCap = new StoredCapability<>(new ITArrayFluidHandler(tanks.input, false, true, () -> { onChanged.run(); this.tanksDirty = true; }));
            this.outputCapSteam = new StoredCapability<>(new ITArrayFluidHandler(tanks.output, true, false, () -> { onChanged.run(); this.tanksDirty = true; }));
            this.invCap = new StoredCapability<>(inventory);
            this.energy = new SyncEnergyStorage(ENERGY_CAPACITY, onChanged);
            this.energyCap = new StoredCapability<>(this.energy);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(1, 0f, 1, markDirty, DistillerRecipe.RECIPES::getById);
            this.itemOutputCap = new StoredCapability<>(
                    new ITWrappingItemHandler(
                            inventory,
                            false,
                            true,
                            List.of(
                                    new ITWrappingItemHandler.IntRange(SLOT_INPUT_EMPTY, SLOT_INPUT_EMPTY + 1),
                                    new ITWrappingItemHandler.IntRange(SLOT_OUTPUT_FILLED, SLOT_OUTPUT_FILLED + 1),
                                    new ITWrappingItemHandler.IntRange(OUTPUT_SLOT, OUTPUT_SLOT + 1)
                            )
                    )
            );
            this.outputRef = ctx.getCapabilityAt(ForgeCapabilities.ITEM_HANDLER, ITEM_OUTPUT_POI);
        }

        public ITSlotwiseItemHandler getInventory() { return inventory; }

        public DistillerTank getTanks() { return tanks; }

        @Override public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("energy", energy.serializeNBT());
            nbt.put("tanks", this.tanks.toNBT());
            nbt.put("processor", processor.toNBT());
            nbt.put("inventory", inventory.serializeNBT());
            nbt.putBoolean("active", active);
        }

        @Override public void readSaveNBT(CompoundTag nbt) {
            energy.deserializeNBT(nbt.get("energy"));
            this.tanks.readNBT(nbt.getCompound("tanks"));
            this.processor.fromNBT(nbt.getList("processor", Tag.TAG_COMPOUND), DistillerProcess::new);
            this.inventory.deserializeNBT(nbt.getCompound("inventory"));
            active = nbt.getBoolean("active");
            tanksDirty = false;
            inventoryDirty = false;
        }

        @Override public void writeSyncNBT(CompoundTag nbt) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display);
            nbt.put("display", display);
        }

        @Override public void readSyncNBT(CompoundTag nbt) {
            if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display")); }
        }

        @Override public AveragingEnergyStorage getEnergy() { return energy; }

        @Override public IFluidTank[] getInternalTanks() { return tankArray; }

        @Override public int[] getOutputTanks() { return new int[]{1}; }

        @Override public int[] getOutputSlots() { return new int[]{OUTPUT_SLOT}; }

        @Override public boolean isActive() { return active; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT());
            nbt.put("energy", energy.serializeNBT());
            nbt.put("inventory", inventory.serializeNBT());
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"));
            if (energy == null) { energy = new SyncEnergyStorage(ENERGY_CAPACITY, () -> {}); }
            energy.deserializeNBT(nbt.get("energy"));
            inventory.deserializeNBT(nbt.getCompound("inventory"));
            tanksDirty = false;
            inventoryDirty = false;
        }
    }

    public record DistillerTank(ITMarkableFluidTank input, ITMarkableFluidTank output) {
        public DistillerTank(Consumer<Void> markDirty) {
            this(new ITMarkableFluidTank(INPUT_TANK_CAPACITY, markDirty), new ITMarkableFluidTank(OUTPUT_TANK_CAPACITY, markDirty));
        }

        public static DistillerTank makeClient() { return new DistillerTank(v -> {}); }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("input", this.input.writeToNBT(new CompoundTag()));
            tag.put("output", this.output.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            this.input.readFromNBT(tag.getCompound("input"));
            this.output.readFromNBT(tag.getCompound("output"));
        }

        @SuppressWarnings("unused")
        public int getCapacity() { return Math.max(INPUT_TANK_CAPACITY, OUTPUT_TANK_CAPACITY); }
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
