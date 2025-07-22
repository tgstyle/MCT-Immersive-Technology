package mctmods.immersivetechnology.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.SolarTowerRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.FullblockShape;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class ITDistillerLogic implements IMultiblockLogic<ITDistillerLogic.State>, IServerTickableComponent<ITDistillerLogic.State>, IClientTickableComponent<ITDistillerLogic.State>
{
    @Override
    public State createInitialState(IInitialMultiblockContext iInitialMultiblockContext) {
        return new State(iInitialMultiblockContext);
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) {
        return FullblockShape.GETTER;
    }

    @Override
    public LazyOptional getCapability(IMultiblockContext ctx, CapabilityPosition position, Capability cap) {
        return IMultiblockLogic.super.getCapability(ctx, position, cap);
    }

    @Override
    public void tickClient(IMultiblockContext<State> iMultiblockContext) {

    }

    @Override
    public void tickServer(IMultiblockContext<State> iMultiblockContext) {

    }

    public static class State implements IMultiblockState
    {
         AveragingEnergyStorage energy = new AveragingEnergyStorage(32000);
        public State(IInitialMultiblockContext ctx) {
        }

        @Override
        public void writeSaveNBT(CompoundTag compoundTag) {

        }

        @Override
        public void writeSyncNBT(CompoundTag nbt) {

        }

        @Override
        public void readSaveNBT(CompoundTag compoundTag) {

        }

        @Override
        public void readSyncNBT(CompoundTag nbt) {

        }

        //@Override
        //public AveragingEnergyStorage getEnergy() {
        //    return energy;
        //}
    }
}
