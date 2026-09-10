package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.IPlayerInteraction;
import com.immersiveconvergence.api.util.ICFluidTank;
import com.immersiveconvergence.api.util.ICUtils;

import javax.annotation.Nonnull;

import java.util.Random;


import mctmods.immersivetechnology.common.Config.ITConfig.Blocks;
import mctmods.immersivetechnology.common.util.ITLib;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class TileEntityBarrelOpen extends TileEntityBarrelSteel implements IPlayerInteraction {

    private static int tankSize() { return Blocks.barrels.barrel_open_tankSize; }
    private static int transferSpeed() { return Blocks.barrels.barrel_open_transferSpeed; }

    private int sleep = 0;

    private static final Random RANDOM = new Random();

    public TileEntityBarrelOpen() {}

    @Override
    public void createTank() { tank = new ICFluidTank(tankSize(), this); }

    @Override
    public void update() {
        super.update();
        if (world.isRemote) { return; }
        if (tank.getFluidAmount() < tank.getCapacity() && RANDOM.nextInt(20) == 0) {
            if (tank.getFluid() == null || tank.getFluid().getFluid() == FluidRegistry.WATER) {
                float temp = world.getBiome(pos).getDefaultTemperature();
                if (world.isRainingAt(pos.up()) && world.canSeeSky(pos.up()) && temp > 0.05F && temp < 2.0F) {
                    int amount = world.isThundering() ? 200 : 100;
                    tank.fill(new FluidStack(FluidRegistry.WATER, amount), true);
                } else if (temp >= 2.0F) {
                    tank.drain(Math.min(100, tank.getFluidAmount()), true);
                }
            }
        }
        doFluidOutput();
    }

    @Override
    protected void doFluidOutput() {
        for (int index = 0; index < 2; index++) {
            if (tank.getFluidAmount() > 0 && sideConfig[index] == 1) {
                EnumFacing face = EnumFacing.byIndex(index);
                IFluidHandler output = FluidUtil.getFluidHandler(world, getPos().offset(face), face.getOpposite());
                if (output != null) {
                    if (sleep == 0) {
                        FluidStack accepted = ICUtils.copyFluidStackWithAmount(tank.getFluid(), Math.min(transferSpeed(), tank.getFluidAmount()), false);
                        assert accepted != null;
                        accepted.amount = output.fill(ICUtils.copyFluidStackWithAmount(accepted, accepted.amount, true), false);
                        if (accepted.amount > 0) {
                            int drained = output.fill(ICUtils.copyFluidStackWithAmount(accepted, accepted.amount, false), true);
                            acceptedAmount += drained;
                            tank.drain(drained, true);
                            sleep = 0;
                        } else { sleep = 20; }
                    } else { sleep--; }
                }
            }
        }
    }

    @Override
    public boolean toggleSide(int side, @Nonnull EntityPlayer p) { return side == 0 && super.toggleSide(side, p); }

    @Override
    public boolean isFluidInvalid(FluidStack fluid) { return fluid != null && fluid.getFluid() != null && !fluid.getFluid().isGaseous(fluid); }

    @Override
    public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        FluidStack fluid = FluidUtil.getFluidContained(heldItem);
        if (!isFluidInvalid(fluid)) {
            ICUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(ITLib.CHAT_INFO + "noGasAllowed"));
            return true;
        }
        return FluidUtil.interactWithFluidHandler(player, hand, tank);
    }
}
