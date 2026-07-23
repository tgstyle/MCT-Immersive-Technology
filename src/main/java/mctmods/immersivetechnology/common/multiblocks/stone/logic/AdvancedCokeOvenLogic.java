package mctmods.immersivetechnology.common.multiblocks.stone.logic;

import mctmods.immersivetechnology.common.blocks.metal.logic.AdvancedCokeOvenBaseHeaterBlockEntity;
import mctmods.immersivetechnology.common.multiblocks.helper.IDisplayContext;
import mctmods.immersivetechnology.common.multiblocks.helper.FurnaceHandler;
import mctmods.immersivetechnology.common.multiblocks.helper.MultiBlockInventoryUtils;
import mctmods.immersivetechnology.common.multiblocks.helper.MultiblockPOIHelper;
import mctmods.immersivetechnology.common.multiblocks.helper.SlotwiseItemHandler;
import mctmods.immersivetechnology.common.multiblocks.helper.WrappingItemHandler;
import mctmods.immersivetechnology.common.multiblocks.stone.process.AdvancedCokeOvenProcess;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetechnology.common.multiblocks.stone.shapes.AdvancedCokeOvenShape;
import mctmods.immersivetechnology.common.fluids.helper.ArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.MarkableFluidTank;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.lib.ModSound;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.core.util.CachedRecipe;
import mctmods.immersivetechnology.core.util.Utils;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.BiFunction;

public class AdvancedCokeOvenLogic implements IMultiblockLogic<AdvancedCokeOvenLogic.State>, IServerTickableComponent<AdvancedCokeOvenLogic.State>, IClientTickableComponent<AdvancedCokeOvenLogic.State> {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_EMPTY_CONTAINER = 2;
    public static final int SLOT_FILLED_CONTAINER = 3;
    public static final int TANK_CAPACITY = 12 * FluidType.BUCKET_VOLUME;

    public static double baseSpeed() { return ServerConfig.advancedCokeOvenSpeedBase; }
    public static double baseheaterAdd() { return ServerConfig.advancedCokeOvenBaseheaterSpeedIncrease; }
    public static double baseheaterMult() { return ServerConfig.advancedCokeOvenBaseheaterSpeedMultiplier; }

    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(AdvancedCokeOvenShape.DATA.pointsOfInterest);

    public static final CapabilityPosition OUTPUT_FLUID_POI = new CapabilityPosition(MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output0").getFirst(), MultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output0"));
    public static final MultiblockFace ITEM_OUTPUT_POI = new MultiblockFace(MultiblockPOIHelper.getFacing(RAW_POIS, "item_output0"), MultiblockPOIHelper.getPosList(RAW_POIS, "item_output0").getFirst());
    public static final MultiblockFace ITEM_INPUT_POI = new MultiblockFace(MultiblockPOIHelper.getFacing(RAW_POIS, "item_input0"), MultiblockPOIHelper.getPosList(RAW_POIS, "item_input0").getFirst());
    public static final BlockPos SMOKE_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "smoke0").getFirst();
    public static final BlockPos SOUND_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "sound0").getFirst();
    public static final List<BlockPos> COMPARATOR_POSITIONS = MultiblockPOIHelper.getPosList(RAW_POIS, "comparator0");
    public static final BlockPos BASEHEATER0_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "baseheater0").getFirst();
    public static final BlockPos BASEHEATER1_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "baseheater1").getFirst();

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        final IMultiblockLevel level = ctx.getLevel();
        if (state.active) {
            final Vec3 particlePos = level.toAbsolute(new Vec3(SMOKE_POI.getX() + 0.5, SMOKE_POI.getY() + 0.9, SMOKE_POI.getZ() + 0.5));
            level.getRawLevel().addAlwaysVisibleParticle(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    ApiUtils.RANDOM.nextDouble(-0.00625, 0.00625),
                    0.05,
                    ApiUtils.RANDOM.nextDouble(-0.00625, 0.00625)
            );
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            if (!state.active) { state.isSoundPlaying = () -> false; }
            return;
        }
        if (state.active) {
            final Vec3 soundPos = level.toAbsolute(new Vec3(SOUND_POI.getX() + 0.5, SOUND_POI.getY() + 0.5, SOUND_POI.getZ() + 0.5));
            float att = (float) Math.max(player.distanceToSqr(soundPos) / 8, 1);
            float vol = 1f / att;
            if (vol > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
                state.isSoundPlaying = ModSound.startSound(
                        () -> state.active,
                        ctx.isValid(),
                        soundPos,
                        Sounds.advancedCokeOven,
                        () -> {
                            LocalPlayer p = Minecraft.getInstance().player;
                            if (p == null) { return 0f; }
                            float attenuation = (float) Math.max(p.distanceToSqr(soundPos) / 8, 1);
                            return 1f / attenuation;
                        },
                        () -> 1f
                );
            }
        } else {
            state.isSoundPlaying = () -> false;
        }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        final Level level = ctx.getLevel().getRawLevel();
        boolean prevTanksDirty = state.tanksDirty;
        boolean wasActive = state.active;
        state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));
        state.active = !state.processor.getQueue().isEmpty();
        RecipeHolder<AdvancedCokeOvenRecipe> recipeHolder = state.recipeGetter.apply(level, state.inventory.getStackInSlot(SLOT_INPUT));
        tryEnqueueProcess(state, level, recipeHolder);
        if (!state.processor.getQueue().isEmpty()) { state.active = true; }
        FluidUtils.fillFluidContainer(state.tanks.output, SLOT_EMPTY_CONTAINER, SLOT_FILLED_CONTAINER, state.inventory);
        if (state.tanks.output.getFluidAmount() > 0) {
            IFluidHandler output = state.fluidOutput.get();
            if (output != null) {
                FluidStack fs = state.tanks.output.getFluid().copy();
                int accepted = output.fill(fs, FluidAction.SIMULATE);
                if (accepted > 0) {
                    int drained = output.fill(Utils.copyFluidStackWithAmount(fs, accepted, false), FluidAction.EXECUTE);
                    state.tanks.output.drain(drained, FluidAction.EXECUTE);
                }
            }
        }
        final IItemHandlerModifiable inventory = state.inventory;
        ItemStack itemOutput = inventory.getStackInSlot(SLOT_OUTPUT);
        if (!itemOutput.isEmpty()) {
            itemOutput = Utils.insertStackIntoInventory(state.outputRef, itemOutput, false);
            inventory.setStackInSlot(SLOT_OUTPUT, itemOutput);
        }
        ItemStack filledContainer = inventory.getStackInSlot(SLOT_FILLED_CONTAINER);
        if (!filledContainer.isEmpty()) {
            filledContainer = Utils.insertStackIntoInventory(state.outputRef, filledContainer, false);
            inventory.setStackInSlot(SLOT_FILLED_CONTAINER, filledContainer);
        }
        boolean activeChanged = wasActive != state.active;
        boolean tanksChanged = prevTanksDirty != state.tanksDirty;
        var queue = state.processor.getQueue();
        int newComparatorValue = 0;
        if (!queue.isEmpty()) {
            int maxTicks = queue.getFirst().getMaxTicks(level);
            newComparatorValue = maxTicks > 0 ? (15 * queue.getFirst().processTick) / maxTicks : 0;
        }
        boolean comparatorChanged = newComparatorValue != state.lastComparatorValue;
        if (comparatorChanged) { for (BlockPos pos : COMPARATOR_POSITIONS) { ctx.setComparatorOutputFor(pos, newComparatorValue); } state.lastComparatorValue = newComparatorValue; }
        if (activeChanged || tanksChanged || comparatorChanged) { ctx.markMasterDirty(); ctx.requestMasterBESync(); }
    }

    private void tryEnqueueProcess(State state, Level level, @Nullable RecipeHolder<AdvancedCokeOvenRecipe> recipeHolder) {
        if (state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) { return; }
        if (recipeHolder == null) { return; }
        AdvancedCokeOvenRecipe recipe = recipeHolder.value();
        ItemStack inputStack = state.inventory.getStackInSlot(SLOT_INPUT);
        if (inputStack.getCount() < 1) { return; }
        ItemStack currentOutputStack = state.inventory.getStackInSlot(SLOT_OUTPUT);
        boolean canOutputItem = currentOutputStack.isEmpty() || (ItemStack.isSameItemSameComponents(currentOutputStack, recipe.itemOutput.get()) && currentOutputStack.getCount() + recipe.itemOutput.get().getCount() <= currentOutputStack.getMaxStackSize());
        if (!canOutputItem) { return; }
        if (state.tanks.output.getFluidAmount() + recipe.creosoteOutput > state.tanks.output.getCapacity()) { return; }
        AdvancedCokeOvenProcess process = new AdvancedCokeOvenProcess(recipeHolder);
        state.processor.addProcessToQueue(process, level, false);
    }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.ItemHandler.BLOCK, (state, position) -> {
            if (ITEM_INPUT_POI.posInMultiblock().equals(position.posInMultiblock()) && (position.side() == null || position.side() == ITEM_INPUT_POI.face())) { return state.itemInputCap; }
            if (ITEM_OUTPUT_POI.posInMultiblock().equals(position.posInMultiblock()) && (position.side() == null || position.side() == ITEM_OUTPUT_POI.face())) { return state.itemOutputCap; }
            return state.invCap;
        });
        register.register(Capabilities.FluidHandler.BLOCK, (state, position) -> {
            if (position.posInMultiblock().equals(OUTPUT_FLUID_POI.posInMultiblock()) && (position.side() == null || position.side() == OUTPUT_FLUID_POI.side())) { return state.fluidCap; }
            return null;
        });
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { MultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return AdvancedCokeOvenShape.GETTER; }

    public ItemInteractionResult click(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) {
        return ItemInteractionResult.SUCCESS;
    }

    public static class State implements IMultiblockState, ContainerData, ProcessContext.ProcessContextInMachine<AdvancedCokeOvenRecipe>, FurnaceHandler.IFurnaceEnvironment<AdvancedCokeOvenRecipe>, IDisplayContext {
        public final BiFunction<Level, ItemStack, RecipeHolder<AdvancedCokeOvenRecipe>> recipeGetter = CachedRecipe.cached(AdvancedCokeOvenRecipe::findRecipe);
        public static final int MAX_PROCESS_TIME = 0;
        public static final int REMAINING_PROCESS_TIME = 1;
        public static final int NUM_SLOTS = 2;

        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public boolean active;
        public final AdvancedCokeOvenTank tanks;
        private final IFluidTank[] tankArray;
        public final SlotwiseItemHandler inventory;
        private final MultiblockProcessor.InMachineProcessor<AdvancedCokeOvenRecipe> processor;
        public IItemHandler invCap;
        public IFluidHandler fluidCap;
        public IItemHandler itemOutputCap;
        public IItemHandler itemInputCap;
        private final Supplier<IFluidHandler> fluidOutput;
        private final Supplier<IItemHandler> outputRef;
        public BooleanSupplier isSoundPlaying = () -> false;
        private final AveragingEnergyStorage energy = new AveragingEnergyStorage(0);
        public boolean tanksDirty = false;
        public int lastComparatorValue = -1;

        public State(IInitialMultiblockContext<State> ctx) {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            final Runnable sync = ctx.getSyncRunnable();
            final Runnable onChanged = () -> { markDirty.run(); sync.run(); this.tanksDirty = true; };
            this.tanks = new AdvancedCokeOvenTank(v -> onChanged.run());
            this.tankArray = new IFluidTank[]{tanks.output};
            this.inventory = new SlotwiseItemHandler(
                    List.of(
                            SlotwiseItemHandler.IOConstraint.input(i -> AdvancedCokeOvenRecipe.findRecipe(ctx.levelSupplier().get(), i, null) != null),
                            SlotwiseItemHandler.IOConstraint.OUTPUT,
                            SlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            SlotwiseItemHandler.IOConstraint.OUTPUT
                    ),
                    onChanged
            );
            this.processor = new MultiblockProcessor.InMachineProcessor<>(1, 0f, 1, markDirty, AdvancedCokeOvenRecipe::getById);
            this.invCap = this.inventory;
            this.fluidCap = new ArrayFluidHandler(tanks.output, true, false, onChanged);
            this.itemOutputCap = new WrappingItemHandler(
                    inventory,
                    false,
                    true,
                    List.of(
                            new WrappingItemHandler.IntRange(SLOT_OUTPUT, SLOT_OUTPUT + 1),
                            new WrappingItemHandler.IntRange(SLOT_FILLED_CONTAINER, SLOT_FILLED_CONTAINER + 1)
                    )
            );
            this.itemInputCap = new WrappingItemHandler(
                    inventory,
                    true,
                    false,
                    List.of(new WrappingItemHandler.IntRange(SLOT_INPUT, SLOT_INPUT + 1))
            );
            MultiblockFace outputMBFace = new MultiblockFace(OUTPUT_FLUID_POI.side(), OUTPUT_FLUID_POI.posInMultiblock());
            CapabilityPosition opposingCP = CapabilityPosition.opposing(outputMBFace);
            MultiblockFace opposingMBFace = new MultiblockFace(opposingCP.side(), opposingCP.posInMultiblock());
            this.fluidOutput = ctx.getCapabilityAt(Capabilities.FluidHandler.BLOCK, opposingMBFace);
            this.outputRef = ctx.getCapabilityAt(Capabilities.ItemHandler.BLOCK, ITEM_OUTPUT_POI);
        }

        public AdvancedCokeOvenTank getTanks() { return tanks; }

        public boolean isActive() { return active; }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.put("processor", processor.toNBT(provider));
            nbt.put("inventory", inventory.serializeNBT(provider));
            nbt.putBoolean("active", active);
            CompoundTag rsTag = new CompoundTag();
            rsState.writeSaveNBT(rsTag, provider);
            nbt.put("rsState", rsTag);
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            processor.fromNBT(nbt.getList("processor", Tag.TAG_COMPOUND), AdvancedCokeOvenProcess::create, provider);
            inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
            active = nbt.getBoolean("active");
            if (nbt.contains("rsState", Tag.TAG_COMPOUND)) { rsState.readSaveNBT(nbt.getCompound("rsState"), provider); }
            tanksDirty = false;
        }

        @Override public void writeSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display, provider);
            nbt.put("display", display);
        }

        @Override public void readSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display"), provider); }
        }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT(provider));
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            tanksDirty = false;
        }

        @Override public int get(int index) {
            if (processor.getQueue().isEmpty()) { return 0; }
            AdvancedCokeOvenProcess process = (AdvancedCokeOvenProcess) processor.getQueue().getFirst();
            return switch (index) {
                case MAX_PROCESS_TIME -> process.getMaxProcessTime();
                case REMAINING_PROCESS_TIME -> process.getMaxProcessTime() - process.getCurrentProcessTime();
                default -> throw new IllegalArgumentException("Unknown index " + index);
            };
        }

        @Override public void set(int index, int value) {
            if (processor.getQueue().isEmpty()) { return; }
            switch (index) {
                case MAX_PROCESS_TIME, REMAINING_PROCESS_TIME -> {}
                default -> throw new IllegalArgumentException("Unknown index " + index);
            }
        }

        @Override public int getCount() { return NUM_SLOTS; }

        @Override public AveragingEnergyStorage getEnergy() { return energy; }

        @Override public IFluidTank[] getInternalTanks() { return tankArray; }

        @Override public int[] getOutputSlots() { return new int[]{SLOT_OUTPUT}; }

        @Override public int[] getOutputTanks() { return new int[]{0}; }

        @Override public IItemHandlerModifiable getInventory() { return inventory; }

        @Override @Nullable public AdvancedCokeOvenRecipe getRecipeForInput(Level level) {
            RecipeHolder<AdvancedCokeOvenRecipe> holder = AdvancedCokeOvenRecipe.findRecipe(level, inventory.getStackInSlot(SLOT_INPUT), null);
            return holder != null ? holder.value() : null;
        }

        @Override public int getBurnTimeOf(Level level, ItemStack fuel) { return 0; }

        @Override public double getProcessSpeed(IMultiblockLevel level) {
            int activeBaseheaters = 0;

            BlockPos heater1World = level.toAbsolute(BASEHEATER0_POI);
            BlockEntity be1 = level.getRawLevel().getBlockEntity(heater1World);
            if (be1 instanceof AdvancedCokeOvenBaseHeaterBlockEntity heater && heater.doSpeedup()) { activeBaseheaters++; }

            BlockPos heater2World = level.toAbsolute(BASEHEATER1_POI);
            BlockEntity be2 = level.getRawLevel().getBlockEntity(heater2World);
            if (be2 instanceof AdvancedCokeOvenBaseHeaterBlockEntity heater && heater.doSpeedup()) { activeBaseheaters++; }

            return (baseSpeed() + activeBaseheaters * baseheaterAdd()) * (1 + activeBaseheaters * (baseheaterMult() - 1));
        }

        @Override public void turnOff(IMultiblockLevel level) { }
    }

    public record AdvancedCokeOvenTank(MarkableFluidTank output) {
        public AdvancedCokeOvenTank(java.util.function.Consumer<Void> markDirty) {
            this(new MarkableFluidTank(TANK_CAPACITY, markDirty));
        }

        public static AdvancedCokeOvenTank makeClient() { return new AdvancedCokeOvenTank(v -> {}); }

        public CompoundTag toNBT(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.put("out", this.output.writeToNBT(provider, new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag, HolderLookup.Provider provider) { this.output.readFromNBT(provider, tag.getCompound("out")); }

        @SuppressWarnings("unused")
        public int getCapacity() { return TANK_CAPACITY; }
    }
}
