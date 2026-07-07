package mctmods.immersivetechnology.common.blocks.metal.logic;

import mctmods.immersivetechnology.common.blocks.helper.ITEnums.IOSideConfig;
import mctmods.immersivetechnology.common.blocks.metal.BarrelOpenBlock;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BarrelOpenIBlockEntity extends BarrelCommonIBlockEntity {
    private static final Random RANDOM = new Random();
    private static final int tankSize = 12 * FluidType.BUCKET_VOLUME;

    public BarrelOpenIBlockEntity(BlockPos pos, BlockState state) { super(ITBlockEntities.BARREL_OPEN.get(), pos, state, tankSize); }

    @Override public void tickServer() {
        boolean update = false;
        if (level == null || level.isClientSide) return;
        if (tank.getFluidAmount() < tank.getCapacity() && RANDOM.nextInt(20) == 0) {
            FluidStack fs = tank.getFluid();
            if (fs.isEmpty() || fs.getFluid() == Fluids.WATER) {
                Biome biome = level.getBiome(worldPosition).value();
                float temp = biome.getBaseTemperature();
                if (level.isRainingAt(worldPosition.above()) && level.canSeeSky(worldPosition.above()) && temp > 0.05F && temp < 2.0F) {
                    int amount = level.isThundering() ? 200 : 100;
                    tank.fill(new FluidStack(Fluids.WATER, amount), IFluidHandler.FluidAction.EXECUTE);
                    update = true;
                } else if (temp >= 2.0F) {
                    tank.drain(Math.min(100, tank.getFluidAmount()), IFluidHandler.FluidAction.EXECUTE);
                    update = true;
                }
            }
        }
        if (tank.getFluidAmount() > 0 && sideConfig.get(Direction.DOWN) == IOSideConfig.OUTPUT) {
            IFluidHandler output = neighbors.get(Direction.DOWN).getNullable();
            if (output != null) {
                FluidStack simulatedDrain = tank.drain(Math.min(transferSpeed, tank.getFluidAmount()), IFluidHandler.FluidAction.SIMULATE);
                if (!simulatedDrain.isEmpty()) {
                    int accepted = output.fill(simulatedDrain, IFluidHandler.FluidAction.SIMULATE);
                    if (accepted > 0) {
                        FluidStack drained = tank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
                        output.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                        update = true;
                    }
                }
            }
        }
        if (update) { setChanged(); markContainingBlockForUpdate(null); }
    }

    @Override protected boolean isFluidValid(@NotNull FluidStack fluid) { return !fluid.isEmpty() && fluid.getFluid().getFluidType().getDensity(fluid) >= 0; }

    @Override protected void postRead(boolean descPacket) { if (!descPacket) updateState(); }

    @Override protected boolean canConfigureSide(Direction side) { return side != Direction.UP; }

    @Override protected void updateState() {
        if (level == null || level.isClientSide) return;
        BlockState current = getBlockState();
        BlockState newState = current.setValue(BarrelOpenBlock.BOTTOM_CONFIG, sideConfig.getOrDefault(Direction.DOWN, IOSideConfig.OUTPUT));
        if (!current.equals(newState)) level.setBlock(getBlockPos(), newState, 3);
    }
}
