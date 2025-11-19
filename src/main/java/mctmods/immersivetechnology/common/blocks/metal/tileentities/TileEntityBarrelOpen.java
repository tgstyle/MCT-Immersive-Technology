package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.common.Config.ITConfig.Blocks;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.Random;

public class TileEntityBarrelOpen extends TileEntityBarrelSteel implements IPlayerInteraction {
    private static final int tankSize = Blocks.barrels.barrel_open_tankSize;
    private static final int transferSpeed = Blocks.barrels.barrel_open_transferSpeed;

    private int lastRandom = 0;
    private int sleep = 0;

    private static final Random RANDOM = new Random();

    public TileEntityBarrelOpen() {}

    @Override
    public void createTank() { tank = new ITFluidTank(tankSize, this); }

    @Override
    public void update() {
        super.update();
        if (world.isRemote) { return; }
        if (tank.getFluidAmount() != tank.getCapacity()) {
            int random = 1 + RANDOM.nextInt(100);
            if (random == lastRandom) {
                if (tank.getFluid() == null || tank.getFluid().getFluid() == FluidRegistry.WATER) {
                    float temp = world.getBiomeProvider().getTemperatureAtHeight(world.getBiome(pos).getTemperature(pos), pos.getY());
                    if (world.isRaining() && world.canSeeSky(pos) && temp > 0.05F && temp < 2.0F) {
                        int amount = 100;
                        if (world.isThundering()) { amount = 200; }
                        tank.fill(new FluidStack(FluidRegistry.WATER, amount), true);
                    } else if (temp >= 2.0F) { tank.drain(Math.min(100, tank.getFluidAmount()), true); }
                }
            }
            lastRandom = random;
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
                        FluidStack accepted = Utils.copyFluidStackWithAmount(tank.getFluid(), Math.min(transferSpeed, tank.getFluidAmount()), false);
                        assert accepted != null;
                        accepted.amount = output.fill(Utils.copyFluidStackWithAmount(accepted, accepted.amount, true), false);
                        if (accepted.amount > 0) {
                            int drained = output.fill(Utils.copyFluidStackWithAmount(accepted, accepted.amount, false), true);
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
    public boolean toggleSide(int side, @Nonnull EntityPlayer p) { return false; }

    @Override
    public boolean isFluidInvalid(FluidStack fluid) { return fluid != null && fluid.getFluid() != null && !fluid.getFluid().isGaseous(fluid); }

    @Override
    public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        FluidStack fluid = FluidUtil.getFluidContained(heldItem);
        if (!isFluidInvalid(fluid)) {
            ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(Lib.CHAT_INFO + "noGasAllowed"));
            return true;
        }
        return FluidUtil.interactWithFluidHandler(player, hand, tank);
    }
}
