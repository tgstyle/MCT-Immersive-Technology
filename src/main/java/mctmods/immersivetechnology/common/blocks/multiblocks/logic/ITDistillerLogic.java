package mctmods.immersivetechnology.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.sound.MultiblockSound;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.DistillerRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.FullblockShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.lib.ITMultiblockSound;
import mctmods.immersivetechnology.core.registration.ITSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ITDistillerLogic implements IMultiblockLogic<ITDistillerLogic.State>, IServerTickableComponent<ITDistillerLogic.State>, IClientTickableComponent<ITDistillerLogic.State>
{
    private static final CapabilityPosition FLUID_OUTPUT_CAP = new CapabilityPosition(2, 0, 1, RelativeBlockFace.LEFT);
    private static final Set<CapabilityPosition> FLUID_INPUT_CAPS = Set.of(
            new CapabilityPosition(0, 0, 1, RelativeBlockFace.RIGHT)
    );
    private static final Set<BlockPos> FLUID_INPUTS = FLUID_INPUT_CAPS.stream()
            .map(CapabilityPosition::posInMultiblock)
            .collect(Collectors.toSet());
    private static final Set<CapabilityPosition> ENERGY_INPUTS = Set.of(new CapabilityPosition(2,1, 2, RelativeBlockFace.UP));

    public static final BlockPos REDSTONE_POS = new BlockPos(0, 1, 2);

    public static final int TANK_CAPACITY = 24* FluidType.BUCKET_VOLUME;

    @Override
    public State createInitialState(IInitialMultiblockContext iInitialMultiblockContext) {
        return new State(iInitialMultiblockContext);
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) {
        return FullblockShape.GETTER;
    }

    @Override
    public<T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        final State state = ctx.getState();
        if(cap == ForgeCapabilities.ENERGY)
        {
            if((position.side()==null || ENERGY_INPUTS.contains(position))) return state.energyCap.cast(ctx);
        }
        if(cap == ForgeCapabilities.FLUID_HANDLER)
        {
            if(FLUID_OUTPUT_CAP.equals(position))
                return state.outputCap.cast(ctx);
            else if(FLUID_INPUT_CAPS.contains(position))
                return state.inputCap.cast(ctx);
        }
        return LazyOptional.empty();
    }

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        if(!state.isSoundPlaying.getAsBoolean())
        {
            final Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(2.5, 1.5, 1.5));
            state.isSoundPlaying = ITMultiblockSound.startSound(
                    () -> state.active, ctx.isValid(), soundPos, ITSounds.distiller, () -> {
                        LocalPlayer player = Minecraft.getInstance().player;
                        if (player == null) { return 0f; }
                        float attenuation = (float) Math.max(player.distanceToSqr(soundPos) / 8, 1);
                        return attenuation;
                    },
                    () -> 1f
            );
        }
    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx)
    {
        final State state = ctx.getState();
        if(state.mbLevelGetter == null) state.mbLevelGetter = ctx::getLevel;

        if(!state.tanks.waterInput.isEmpty()) tryRunRecipe(state, ctx.getLevel().getRawLevel());
        final boolean wasActive = state.active;
        state.active = state.processor.tickServer(state, ctx.getLevel(), state.rsState.isEnabled(ctx));
        if(state.processor.getQueueSize() > 0) ctx.requestMasterBESync();

        if((wasActive != state.active))
        {
            ctx.requestMasterBESync();
        }

        if(!state.tanks.output.isEmpty())
        {
            drainOutputTank(ctx, state.fluidOutput, state.tanks.output);
            ctx.requestMasterBESync();
        }
    }

    private void drainOutputTank(IMultiblockContext<State> context, CapabilityReference<IFluidHandler> outputRef, FluidTank tank)
    {
        int outSize = Math.min(FluidType.BUCKET_VOLUME, tank.getFluidAmount());
        FluidStack out = Utils.copyFluidStackWithAmount(tank.getFluid(), outSize, false);
        IFluidHandler output = outputRef.getNullable();

        if(output==null)
            return;

        int accepted = output.fill(out, IFluidHandler.FluidAction.SIMULATE);
        if(accepted > 0)
        {
            int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), IFluidHandler.FluidAction.EXECUTE);
            tank.drain(drained, IFluidHandler.FluidAction.EXECUTE);
            context.markMasterDirty();
            context.requestMasterBESync();
        }
    }

    private void tryRunRecipe(State state, Level level)
    {
        if(state.energy.getEnergyStored() <= 0 || state.processor.getQueueSize() >= state.processor.getMaxQueueSize()) return;

        final FluidStack input = state.tanks.waterInput.getFluid();
        if(input.isEmpty()) return;
        DistillerRecipe recipe = DistillerRecipe.findRecipe(level, input);
        if(recipe == null) return;
        MultiblockProcessInMachine<DistillerRecipe> process = new MultiblockProcessInMachine<>(recipe);
        if(input.isEmpty()) process.setInputTanks(0);

        if(state.processor.addProcessToQueue(process, level, true))
        {
            state.tanks.waterInput.drain(recipe.water.getAmount(), IFluidHandler.FluidAction.EXECUTE);
            state.processor.addProcessToQueue(process, level, false);
        }

        ITLib.IT_LOGGER.info("Finished tryRunRecipe");
    }

    public static class State implements IMultiblockState, ProcessContext.ProcessContextInMachine<DistillerRecipe>
    {
        public boolean active;

        private BooleanSupplier isSoundPlaying = () -> false;

        private final StoredCapability<IEnergyStorage> energyCap;
        private final StoredCapability<IFluidHandler> inputCap;
        private final StoredCapability<IFluidHandler> outputCap;
        private final CapabilityReference<IFluidHandler> fluidOutput;

        private Supplier<IMultiblockLevel> mbLevelGetter;

        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();

        private final DistillerTank tanks = new DistillerTank();

        private final IFluidTank[] tankArray = {tanks.waterInput, tanks.output};

        AveragingEnergyStorage energy = new AveragingEnergyStorage(32000);

        private final MultiblockProcessor.InMachineProcessor<DistillerRecipe> processor;

        public State(IInitialMultiblockContext ctx)
        {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            this.inputCap = new StoredCapability<>(new ArrayFluidHandler(false, true, markDirty, this.tanks.waterInput));
            this.outputCap = new StoredCapability<>(new ArrayFluidHandler(true, false, markDirty, this.tanks.output));
            this.energyCap = new StoredCapability<>(this.energy);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(1, 0, 1, markDirty, DistillerRecipe.RECIPES::getById);
            this.fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, new MultiblockFace(FLUID_OUTPUT_CAP.side(), FLUID_OUTPUT_CAP.posInMultiblock().east()));
            Set<Fluid> allowedInput = Set.of(Fluids.WATER);
            this.tanks.waterInput.setValidator(f -> allowedInput.contains(f.getFluid()));
        }

        @Override
        public void onProcessFinish(MultiblockProcess<DistillerRecipe, ?> process, Level level)
        {
            try {
                DistillerRecipe recipe = process.getRecipe(level);
                assert recipe != null;
                tanks.output.fill(recipe.fluidOutput, IFluidHandler.FluidAction.EXECUTE);
            } catch(Exception error)
            {
                ITLib.IT_LOGGER.error("Error: {}", error.getMessage());
            }
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt)
        {
            nbt.put("energy", energy.serializeNBT());
            nbt.put("tanks", this.tanks.toNBT());
            nbt.put("processor", processor.toNBT());
        }

        @Override
        public void readSaveNBT(CompoundTag nbt)
        {
            energy.deserializeNBT(nbt.get("energy"));
            this.tanks.readNBT(nbt.getCompound("tanks"));
            this.processor.fromNBT(nbt.get("processor"), MultiblockProcessInMachine::new);
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

        @Override
        public AveragingEnergyStorage getEnergy()
        {
            return energy;
        }

        @Override
        public IFluidTank[] getInternalTanks()
        {
            return tankArray;
        }

        @Override
        public int[] getOutputTanks()
        {
            return new int[]{1};
        }
    }

    public record DistillerTank(FluidTank output, FluidTank waterInput)
    {
        public DistillerTank()
        {
            this(new FluidTank(TANK_CAPACITY), new FluidTank(TANK_CAPACITY));
        }

        public DistillerTank(FluidTank output, FluidTank waterInput)
        {
            this.output = output;
            this.waterInput = waterInput;
        }

        public Tag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("waterIn", this.waterInput.writeToNBT(new CompoundTag()));
            tag.put("out", this.output.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            this.waterInput.readFromNBT(tag.getCompound("waterIn"));
            this.output.readFromNBT(tag.getCompound("out"));
        }

        public int getCapacity() {
            return TANK_CAPACITY;
        }
    }
}
