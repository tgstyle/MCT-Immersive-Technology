package mctmods.immersivetechnology.common.blocks.metal.logic;

import mctmods.immersivetechnology.common.blocks.helper.ITEnums.IOSideConfig;
import mctmods.immersivetechnology.common.blocks.metal.BarrelSteelBlock;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class BarrelSteelIBlockEntity extends BarrelCommonIBlockEntity {
    private static final int tankSize = 24 * FluidType.BUCKET_VOLUME;

    public BarrelSteelIBlockEntity(BlockPos pos, BlockState state) { super(ITBlockEntities.BARREL_STEEL.get(), pos, state, tankSize); }

    @Override public void tickServer() {
        if (level == null || level.isClientSide) return;
        if (isRSPowered()) return;
        boolean update = false;
        for (Direction side : neighbors.keySet()) {
            if (tank.getFluidAmount() > 0 && sideConfig.get(side) == IOSideConfig.OUTPUT) {
                IFluidHandler handler = neighbors.get(side).getNullable();
                if (handler != null) {
                    FluidStack simulatedDrain = tank.drain(Math.min(transferSpeed, tank.getFluidAmount()), IFluidHandler.FluidAction.SIMULATE);
                    if (!simulatedDrain.isEmpty()) {
                        int accepted = handler.fill(simulatedDrain, IFluidHandler.FluidAction.SIMULATE);
                        if (accepted > 0) {
                            FluidStack drained = tank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
                            handler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                            update = true;
                        }
                    }
                }
            }
        }
        if (update) { setChanged(); markContainingBlockForUpdate(null); }
    }

    @Override protected boolean isFluidValid(@NotNull FluidStack fluid) { return !fluid.isEmpty(); }

    @Override protected void postRead(boolean descPacket) {
        if (descPacket) markContainingBlockForUpdate(null);
        else updateState();
    }

    @Override protected boolean canConfigureSide(Direction side) { return true; }

    @Override protected void updateState() {
        if (level == null || level.isClientSide) return;
        BlockState current = getBlockState();
        BlockState newState = current.setValue(BarrelSteelBlock.TOP_CONFIG, sideConfig.getOrDefault(Direction.UP, IOSideConfig.INPUT)).setValue(BarrelSteelBlock.BOTTOM_CONFIG, sideConfig.getOrDefault(Direction.DOWN, IOSideConfig.OUTPUT));
        if (!current.equals(newState)) level.setBlock(getBlockPos(), newState, 3);
    }
}
