package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import mctmods.immersivetechnology.common.fluids.ITMarkableFluidTank;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Random;

public class OpenBarrelBlockEntity extends SteelBarrelBlockEntity implements IEServerTickableBE {
    private static final int tankSize = 12000;
    private static final int transferSpeed = 40;

    private int lastRandom = 0;

    private static final Random RANDOM = new Random();

    public OpenBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
        this.tank = new ITMarkableFluidTank(tankSize, v -> this.setChanged());
        this.sideConfig = new int[]{1, -1};
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (level == null || level.isClientSide) return;

        if (tank.getFluidAmount() < tank.getCapacity()) {
            int random = 1 + RANDOM.nextInt(100);
            if (random == lastRandom) {
                FluidStack fs = tank.getFluid();
                if (fs.isEmpty() || fs.getFluid() == Fluids.WATER) {
                    Biome biome = level.getBiome(worldPosition).value();
                    float temp = biome.getBaseTemperature();
                    if (level.isRainingAt(worldPosition.above()) && level.canSeeSky(worldPosition.above()) && temp > 0.05F && temp < 2.0F) {
                        int amount = level.isThundering() ? 200 : 100;
                        tank.fill(new FluidStack(Fluids.WATER, amount), IFluidHandler.FluidAction.EXECUTE);
                    } else if (temp >= 2.0F) {
                        tank.drain(Math.min(100, tank.getFluidAmount()), IFluidHandler.FluidAction.EXECUTE);
                    }
                }
            }
            lastRandom = random;
        }

        if (tank.getFluidAmount() > 0 && sideConfig[0] == 1) {
            Direction face = Direction.DOWN;
            FluidUtil.getFluidHandler(level, worldPosition.relative(face), face.getOpposite()).ifPresent(output -> {
                if (sleep == 0) {
                    FluidStack simulatedDrain = tank.drain(Math.min(transferSpeed, tank.getFluidAmount()), IFluidHandler.FluidAction.SIMULATE);
                    if (simulatedDrain.getAmount() > 0) {
                        int accepted = output.fill(simulatedDrain, IFluidHandler.FluidAction.SIMULATE);
                        if (accepted > 0) {
                            FluidStack drained = tank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
                            output.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                            sleep = 0;
                        } else {
                            sleep = 20;
                        }
                    } else {
                        sleep = 20;
                    }
                } else {
                    sleep--;
                }
            });
        }
    }

    @Override
    public boolean isFluidValid(@NotNull FluidStack fluid) { return !fluid.isEmpty() && fluid.getFluid().getFluidType().getDensity(fluid) >= 0; }

    @Override
    public boolean interact(@NotNull Direction side, @NotNull Player player, @NotNull InteractionHand hand, @NotNull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        FluidStack contained = FluidUtil.getFluidContained(heldItem).orElse(FluidStack.EMPTY);
        if (!isFluidValid(contained)) {
            player.displayClientMessage(Component.translatable(TranslationKey.NO_GAS_ALLOWED.text()), false);
            return true;
        }
        return FluidUtil.interactWithFluidHandler(player, hand, tank);
    }

    @Override
    public boolean toggleSide(int side, Player player) { return false; }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER && (side == null || side == Direction.DOWN)) { return bottomHandler.cast(); }
        return super.getCapability(cap, side);
    }
}
