package mctmods.immersivetechnology.common.blocks.multiblocks.logic;

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
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import blusunrize.immersiveengineering.common.util.sound.MultiblockSound;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.FullblockShape;
import mctmods.immersivetechnology.core.registration.ITMenuTypes;
import mctmods.immersivetechnology.core.registration.ITSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public class ITBoilerLogic implements IMultiblockLogic<ITBoilerLogic.State>, IServerTickableComponent<ITBoilerLogic.State>, IClientTickableComponent<ITBoilerLogic.State>
{
    public static final BlockPos MASTER_OFFSET = new BlockPos(0,0,0);
    public static final BlockPos REDSTONE_POS = new BlockPos(4, 1, 0);
    private static final CapabilityPosition FLUID_OUTPUT_CAP = new CapabilityPosition(0, 2, 1, RelativeBlockFace.UP);
    private static final Set<CapabilityPosition> FLUID_INPUT_CAPS = Set.of(
            new CapabilityPosition(0, 0, 1, RelativeBlockFace.RIGHT),
            new CapabilityPosition(4, 0, 1, RelativeBlockFace.LEFT)
    );
    private static final Set<BlockPos> FLUID_INPUTS = FLUID_INPUT_CAPS.stream()
            .map(CapabilityPosition::posInMultiblock)
            .collect(Collectors.toSet());
    public static final int TANK_CAPACITY = 24* FluidType.BUCKET_VOLUME;
    public static final int MAX_HEAT = 1200;
    public static final int NUM_SLOTS = 6;
    public static final int SLOT_FUEL_IN = 0;
    public static final int SLOT_FUEL_EMPTY_OUT = 1;
    public static final int SLOT_WATER_IN = 2;
    public static final int SLOT_WATER_EMPTY_OUT = 3;
    public static final int SLOT_STEAM_EMPTY_IN = 4;
    public static final int SLOT_STEAM_FULL_OUT = 5;

    @Override
    public InteractionResult click(
            IMultiblockContext<State> ctx, BlockPos posInMultiblock,
            Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient
    )
    {
        if(isClient)
            return InteractionResult.SUCCESS;
        final State state = ctx.getState();
        IFluidHandler tank = null;
        if(FLUID_INPUTS.contains(posInMultiblock))
            tank = posInMultiblock.getX() < 2?state.tanks.fuelInput: state.tanks.waterInput;
        else if(FLUID_OUTPUT_CAP.posInMultiblock().equals(posInMultiblock))
            tank = state.tanks.output;
        if(tank!=null)
        {
            FluidUtils.interactWithFluidHandler(player, hand, tank);
            ctx.markMasterDirty();
        }
        else
            player.openMenu(ITMenuTypes.BOILER_MENU.provide(ctx, posInMultiblock));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tickClient(IMultiblockContext<State> ctx)
    {
        final State state = ctx.getState();

        if(!state.isSoundPlaying.getAsBoolean())
        {
            final Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(2.5, 1.5, 1.5));
            state.isSoundPlaying = MultiblockSound.startSound(
                    () -> state.heat > 0, ctx.isValid(), soundPos, ITSounds.boiler, 0.5f
            );
        }
    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx)
    {
        final State state = ctx.getState();
        final boolean wasActive = state.active;
        state.energy.receiveEnergy(1, false);
        state.active = state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));
        BoilerRecipe recipe = BoilerRecipe.findRecipe(ctx.getLevel().getRawLevel(), state.tanks.fuelInput.getFluid(), state.tanks.waterInput.getFluid());
        if (!state.tanks.fuelInput.isEmpty())
            while (state.heat < MAX_HEAT)
                state.heat += heatUp(recipe);
        if(wasActive!=state.active)
            ctx.requestMasterBESync();
        tryEnqueueProcess(state, ctx.getLevel().getRawLevel(), recipe);
        FluidUtils.multiblockFluidOutput(
                state.fluidOutput, state.tanks.output(), SLOT_STEAM_EMPTY_IN, SLOT_STEAM_FULL_OUT, state.inventory
        );
    }

    private void tryEnqueueProcess(State state, Level level, BoilerRecipe recipe)
    {
        if(state.processor.getQueueSize() >= state.processor.getMaxQueueSize())
            return;
        final FluidStack leftInput = state.tanks.fuelInput.getFluid();
        final FluidStack rightInput = state.tanks.waterInput.getFluid();
        if(leftInput.isEmpty()&&rightInput.isEmpty())
            return;
        if(recipe==null)
            return;

        MultiblockProcessInMachine<BoilerRecipe> process = new MultiblockProcessInMachine<>(recipe);
        if(!leftInput.isEmpty()&&!rightInput.isEmpty())
            process.setInputTanks(0, 1);
        else if(!leftInput.isEmpty())
        {
            process.setInputTanks(0);
            if (!state.tanks.fuelInput.isEmpty())
                while (state.heat < MAX_HEAT)
                    state.heat += heatUp(recipe);
        }
        else
            process.setInputTanks(1);
        state.processor.addProcessToQueue(process, level, false);
    }

    @Override
    public State createInitialState(IInitialMultiblockContext<State> context)
    {
        return new ITBoilerLogic.State(context);
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) {
        return FullblockShape.GETTER;
    }

    @Override
    public <T>
    LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
    {
        final State state = ctx.getState();
        if(cap==ForgeCapabilities.FLUID_HANDLER)
        {
            if(FLUID_OUTPUT_CAP.equals(position))
                return state.outputCapSteam.cast(ctx);
            else if(FLUID_INPUT_CAPS.contains(position))
                return state.inputCap.cast(ctx);
        }
        return LazyOptional.empty();
    }

    private int heatUp(BoilerRecipe recipe)
    {
        return recipe.getHeatPerTick();
    }

    @Override
    public void dropExtraItems(State state, Consumer<ItemStack> drop)
    {
        MBInventoryUtils.dropItems(state.getInventory(), drop);
    }

    private void drainOutputTank(ITBoilerLogic.State state, IMultiblockContext<ITBoilerLogic.State> context, CapabilityReference<IFluidHandler> outputRef)
    {
        int outSize = Math.min(FluidType.BUCKET_VOLUME, state.tanks.output().getFluidAmount());
        FluidStack out = Utils.copyFluidStackWithAmount(state.tanks.output().getFluid(), outSize, false);
        IFluidHandler output = outputRef.getNullable();

        if(output==null)
            return;

        int accepted = output.fill(out, IFluidHandler.FluidAction.SIMULATE);
        if(accepted > 0)
        {
            int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), IFluidHandler.FluidAction.EXECUTE);
            state.tanks.output().drain(drained, IFluidHandler.FluidAction.EXECUTE);
            context.markMasterDirty();
            context.requestMasterBESync();
        }
    }

    public static class State implements IMultiblockState, ProcessContext.ProcessContextInMachine<BoilerRecipe>
    {
        public static final int MAX_PROCESS_TIME = 0;
        public static final int PROCESS_TIME = 1;
        public static final int NUM_SLOTS = 6;

        public boolean active;

        private final AveragingEnergyStorage energy = new AveragingEnergyStorage(1);

        private final BoilerTank tanks = new BoilerTank();
        private final IFluidTank[] tankArray = {tanks.fuelInput, tanks.waterInput, tanks.output};
        private final StoredCapability<IFluidHandler> inputCap;
        private final StoredCapability<IFluidHandler> outputCapSteam;
        private final CapabilityReference<IFluidHandler> fluidOutput;

        private final MultiblockProcessor.InMachineProcessor<BoilerRecipe> processor;

        private BooleanSupplier isSoundPlaying = () -> false;

        private int heat = 0;

        private final SlotwiseItemHandler inventory;

        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();

        public State(IInitialMultiblockContext<State> ctx)
        {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            this.processor = new MultiblockProcessor.InMachineProcessor<>(1, 0, 1, markDirty, BoilerRecipe.RECIPES::getById);
            this.inputCap = new StoredCapability<>(new ArrayFluidHandler(false, true, markDirty, this.tanks.fuelInput, this.tanks.waterInput));
            this.outputCapSteam = new StoredCapability<>(new ArrayFluidHandler(true, false, markDirty, this.tanks.output));
            final Supplier<@Nullable Level> levelGetter = ctx.levelSupplier();
            inventory = new SlotwiseItemHandler(
                    List.of(
                            SlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            SlotwiseItemHandler.IOConstraint.OUTPUT,
                            SlotwiseItemHandler.IOConstraint.FLUID_INPUT,
                            SlotwiseItemHandler.IOConstraint.OUTPUT,
                            SlotwiseItemHandler.IOConstraint.ANY_INPUT,
                            SlotwiseItemHandler.IOConstraint.OUTPUT
                    ),
                    ctx.getMarkDirtyRunnable()
            );
            this.fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, new MultiblockFace(FLUID_OUTPUT_CAP.side(), FLUID_OUTPUT_CAP.posInMultiblock().above()));
            Set<Fluid> allowedFuels = Set.of(IEFluids.BIODIESEL.getStill());
            this.tanks.fuelInput.setValidator(f -> allowedFuels.contains(f.getFluid()));
        }

        @Override
        public IFluidTank[] getInternalTanks()
        {
            return tankArray;
        }

        @Override
        public int[] getOutputTanks()
        {
            return new int[]{2};
        }

        @Override
        public AveragingEnergyStorage getEnergy() {
            return energy;
        }

        public SlotwiseItemHandler getInventory()
        {
            return inventory;
        }

        public BoilerTank getTanks()
        {
            return tanks;
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt)
        {
            nbt.put("energy", energy.serializeNBT());
            nbt.put("tanks", this.tanks.toNBT());
            nbt.put("processor", processor.toNBT());
            nbt.put("inventory", inventory.serializeNBT());
        }

        @Override
        public void readSaveNBT(CompoundTag nbt)
        {
            energy.deserializeNBT(nbt.get("energy"));
            this.tanks.readNBT(nbt.getCompound("tanks"));
            this.processor.fromNBT(nbt.get("processor"), MultiblockProcessInMachine::new);
            this.inventory.deserializeNBT(nbt.getCompound("inventory"));

        }

        @Override
        public void writeSyncNBT(CompoundTag nbt)
        {
            nbt.putBoolean("active", active);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt)
        {
            active = nbt.getBoolean("active");
        }
    }

    public record BoilerTank(FluidTank output, FluidTank fuelInput, FluidTank waterInput)
    {
        public BoilerTank()
        {
            this(new FluidTank(TANK_CAPACITY), new FluidTank(TANK_CAPACITY), new FluidTank(TANK_CAPACITY));
        }

        public BoilerTank(FluidTank output, FluidTank fuelInput, FluidTank waterInput)
        {
            this.output = output;
            this.fuelInput = fuelInput;
            this.waterInput = waterInput;
        }

        public Tag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("fuelIn", this.fuelInput.writeToNBT(new CompoundTag()));
            tag.put("waterIn", this.waterInput.writeToNBT(new CompoundTag()));
            tag.put("out", this.output.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            this.fuelInput.readFromNBT(tag.getCompound("fuelIn"));
            this.waterInput.readFromNBT(tag.getCompound("waterIn"));
            this.output.readFromNBT(tag.getCompound("out"));
        }

        public int getCapacity() {
            return TANK_CAPACITY;
        }
    }
}
