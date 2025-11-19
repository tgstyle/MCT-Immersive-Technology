package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.common.Config.ITConfig.Settings;
import mctmods.immersivetechnology.common.util.ITIPipe;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

public class TileEntityFluidPumpAlternative extends blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPump implements ITickable, IBlockBounds, IHasDummyBlocks, IConfigurableSides, IFluidPipe, IIEInternalFluxHandler, IBlockOverlayText, IPlayerInteraction {
    boolean checkingArea = false;
    Fluid searchFluid = null;
    boolean fillFirstMode = true;
    ArrayDeque<BlockPos> openList = new ArrayDeque<>();
    ArrayList<BlockPos> closedList = new ArrayList<>();
    ArrayList<BlockPos> checked = new ArrayList<>();
    int rsPower = 0;

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        fillFirstMode = nbt.getBoolean("fillfirstmode");
        checkingArea = nbt.getBoolean("checkingArea");
        if (nbt.hasKey("searchFluid")) searchFluid = FluidRegistry.getFluid(nbt.getString("searchFluid")); else searchFluid = null;
        int closedSize = nbt.getInteger("closedListSize");
        closedList.clear();
        for (int i = 0; i < closedSize; i++) { closedList.add(new BlockPos(nbt.getInteger("closedX" + i), nbt.getInteger("closedY" + i), nbt.getInteger("closedZ" + i))); }
        super.readCustomNBT(nbt, descPacket);
    }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        nbt.setBoolean("fillfirstmode", fillFirstMode);
        nbt.setBoolean("checkingArea", checkingArea);
        if (searchFluid != null) nbt.setString("searchFluid", searchFluid.getName());
        int closedSize = closedList.size();
        nbt.setInteger("closedListSize", closedSize);
        for (int i = 0; i < closedSize; i++) {
            BlockPos pos = closedList.get(i);
            nbt.setInteger("closedX" + i, pos.getX());
            nbt.setInteger("closedY" + i, pos.getY());
            nbt.setInteger("closedZ" + i, pos.getZ());
        }
        super.writeCustomNBT(nbt, descPacket);
    }

    @Override
    public void update() {
        ApiUtils.checkForNeedlessTicking(this);
        if (dummy || world.isRemote) return;
        if (world.getTotalWorldTime() % 20 == 0) rsPower = getRSPower(getPos());
        if (tank.getFluidAmount() > 0) {
            assert tank.getFluid() != null;
            int i = outputFluid(tank.getFluid(), false);
            tank.drain(i, true);
        }
        if (rsPower > 0 || getRSPower(getPos().add(0, 1, 0)) > 0) {
            boolean hasInput = false;
            for (EnumFacing f : EnumFacing.values()) { if (sideConfig[f.ordinal()] != 1) { hasInput = true; break; } }
            if (!hasInput) return;
            for (EnumFacing f : EnumFacing.values()) {
                if (sideConfig[f.ordinal()] != 0) continue;
                BlockPos output = getPos().offset(f);
                TileEntity tile = Utils.getExistingTileEntity(world, output);
                if (tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, f.getOpposite())) {
                    IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, f.getOpposite());
                    if (handler == null) continue;
                    drainFromTank(handler);
                } else if (world.getTotalWorldTime()%20 == ((getPos().getX()^getPos().getZ())&19) && world.getBlockState(getPos().offset(f)).getBlock() == Blocks.WATER && IEConfig.Machines.pump_infiniteWater && tank.fill(new FluidStack(FluidRegistry.WATER, 1000), false) == 1000 && this.energyStorage.extractEnergy(IEConfig.Machines.pump_consumption, true) >= IEConfig.Machines.pump_consumption) {
                    int connectedSources = 0;
                    for (EnumFacing f2 : EnumFacing.HORIZONTALS) {
                        IBlockState waterState = world.getBlockState(getPos().offset(f).offset(f2));
                        if (waterState.getBlock() == Blocks.WATER && Blocks.WATER.getMetaFromState(waterState) == 0) connectedSources++;
                    }
                    if (connectedSources > 1) {
                        this.energyStorage.extractEnergy(IEConfig.Machines.pump_consumption, false);
                        this.tank.fill(new FluidStack(FluidRegistry.WATER, 1000), true);
                    }
                }
            }
            if (world.getTotalWorldTime()%40 == (((getPos().getX()^getPos().getZ()))%40 + 40)%40) {
                if (closedList.isEmpty()) prepareAreaCheck();
                else {
                    int target = closedList.size()-1;
                    BlockPos pos = closedList.get(target);
                    FluidStack fs = Utils.drainFluidBlock(world, pos, false);
                    if (fs == null) closedList.remove(target);
                    else {
                        FluidStack fsCopy = fs.copy();
                        if (fsCopy.tag != null) {
                            fsCopy.tag.removeTag("pressurized");
                            if (fsCopy.tag.isEmpty()) fsCopy.tag = null;
                        }
                        if (tank.fill(fsCopy, false) == fs.amount && this.energyStorage.extractEnergy(IEConfig.Machines.pump_consumption, true) >= IEConfig.Machines.pump_consumption) {
                            this.energyStorage.extractEnergy(IEConfig.Machines.pump_consumption, false);
                            fs = Utils.drainFluidBlock(world, pos, true);
                            if (IEConfig.Machines.pump_placeCobble && placeCobble) world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
                            if (fs.tag != null) {
                                fs.tag.removeTag("pressurized");
                                if (fs.tag.isEmpty()) fs.tag = null;
                            }
                            this.tank.fill(fs, true);
                            closedList.remove(target);
                        }
                    }
                }
            }
        }
        if (checkingArea) checkAreaTick();
    }

    public int getRSPower(BlockPos position) {
        int toReturn = 0;
        for (EnumFacing directions : EnumFacing.values()) { toReturn = Math.max(world.getRedstonePower(position.offset(directions, -1), directions), toReturn); }
        return toReturn;
    }

    public boolean canPressurize() {
        boolean hasTank = false;
        for (EnumFacing f : EnumFacing.values()) {
            if (sideConfig[f.ordinal()] != 1) continue;
            TileEntity tile = Utils.getExistingTileEntity(world, getPos().offset(f));
            if (tile == null || !tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, f.getOpposite())) continue;
            hasTank = true;
        }
        if (hasTank) return true;
        return energyStorage.extractEnergy(IEConfig.Machines.pump_consumption_accelerate, true) >= IEConfig.Machines.pump_consumption_accelerate;
    }

    public void drainFromTank(IFluidHandler handler) {
        int rate = Settings.experimental.pipe_transfer_rate;
        int pressurizedRate = Settings.experimental.pipe_pressurized_transfer_rate;
        boolean usePressurized = canPressurize();
        int maxDrain = usePressurized ? pressurizedRate : rate;
        FluidStack drain;
        int drainedAmount;
        if (!fillFirstMode) {
            drain = handler.drain(maxDrain, false);
            if (drain == null || drain.amount <= 0) return;
            int out = this.outputFluid(drain, false);
            handler.drain(out, true);
            drainedAmount = out;
        } else {
            drain = handler.drain(Math.min(tank.getCapacity() - tank.getFluidAmount(), maxDrain), false);
            if (drain == null || drain.amount <= 0 || !tank.canFillFluidType(drain)) return;
            FluidStack drainCopy = drain.copy();
            if (drainCopy.tag != null) {
                drainCopy.tag.removeTag("pressurized");
                if (drainCopy.tag.isEmpty()) drainCopy.tag = null;
            }
            drainedAmount = tank.fill(drainCopy, true);
            handler.drain(drainedAmount, true);
        }
        if (usePressurized) energyStorage.extractEnergy(IEConfig.Machines.pump_consumption_accelerate, false);
        energyStorage.extractEnergy((int)(drainedAmount / 1000f * IEConfig.Machines.pump_consumption), false);
    }

    public void prepareAreaCheck() {
        openList.clear();
        closedList.clear();
        checked.clear();
        for (EnumFacing f : EnumFacing.values()) { if (sideConfig[f.ordinal()] == 0) { openList.add(getPos().offset(f)); checkingArea = true; } }
    }

    public void checkAreaTick() {
        BlockPos next;
        final int closedListMax = 2048;
        int timeout = 0;
        while (timeout < 64 && closedList.size() < closedListMax && !openList.isEmpty()) {
            timeout++;
            next = openList.pollFirst();
            if (!checked.contains(next)) {
                Fluid fluid = Utils.getRelatedFluid(world, next);
                if (fluid != null && (fluid != FluidRegistry.WATER || !IEConfig.Machines.pump_infiniteWater) && (searchFluid == null || fluid == searchFluid)) {
                    if (searchFluid == null) searchFluid = fluid;
                    if (Utils.drainFluidBlock(world, next, false) != null) closedList.add(next);
                    for (EnumFacing f : EnumFacing.values()) {
                        BlockPos pos2 = next.offset(f);
                        fluid = Utils.getRelatedFluid(world, pos2);
                        if (!checked.contains(pos2) && !closedList.contains(pos2) && !openList.contains(pos2) && fluid != null && (fluid != FluidRegistry.WATER || !IEConfig.Machines.pump_infiniteWater) && (searchFluid == null || fluid == searchFluid)) openList.addLast(pos2);
                    }
                }
                checked.add(next);
            }
        }
        if (closedList.size() >= closedListMax || openList.isEmpty()) checkingArea = false;
    }

    public int outputFluid(@Nonnull FluidStack fs, boolean simulate) {
        int canAccept = fs.amount;
        if (canAccept <= 0) return 0;
        int accelPower = IEConfig.Machines.pump_consumption_accelerate;
        final int fluidForSort = canAccept;
        int sum = 0;
        HashMap<DirectionalFluidOutput, Integer> sorting = new HashMap<>();
        FluidStack insertResource = fs.copy();
        for (EnumFacing f : EnumFacing.values()) {
            if (sideConfig[f.ordinal()] != 1) continue;
            TileEntity tile = Utils.getExistingTileEntity(world, getPos().offset(f));
            if (tile == null || !tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, f.getOpposite())) continue;
            IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, f.getOpposite());
            insertResource.amount = fs.amount;
            if (tile instanceof ITIPipe && this.energyStorage.extractEnergy(accelPower, true) >= accelPower) {
                if (insertResource.tag == null) insertResource.tag = new NBTTagCompound();
                insertResource.tag.setBoolean("pressurized", true);
            } else {
                if (insertResource.tag != null) {
                    insertResource.tag.removeTag("pressurized");
                    if (insertResource.tag.isEmpty()) insertResource.tag = null;
                }
            }
            assert handler != null;
            int temp = handler.fill(insertResource, false);
            if (temp > 0) {
                sorting.put(new DirectionalFluidOutput(handler, tile, f), temp);
                sum += temp;
            }
        }
        if (sum > 0) {
            int f = 0;
            int i = 0;
            for (DirectionalFluidOutput output : sorting.keySet()) {
                float prio = sorting.get(output)/(float)sum;
                int amount = (int)(fluidForSort*prio);
                if (i++ == sorting.size()-1) amount = canAccept;
                insertResource.amount = amount;
                if (output.containingTile instanceof ITIPipe && this.energyStorage.extractEnergy(accelPower, true) >= accelPower) {
                    this.energyStorage.extractEnergy(accelPower, false);
                    if (insertResource.tag == null) insertResource.tag = new NBTTagCompound();
                    insertResource.tag.setBoolean("pressurized", true);
                } else {
                    if (insertResource.tag != null) {
                        insertResource.tag.removeTag("pressurized");
                        if (insertResource.tag.isEmpty()) insertResource.tag = null;
                    }
                }
                int r = output.output.fill(insertResource, !simulate);
                f += r;
                canAccept -= r;
                if (canAccept <= 0) break;
            }
            return f;
        }
        return 0;
    }

    public void flipFillMode(EntityPlayer player) {
        fillFirstMode = !fillFirstMode;
        ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(fillFirstMode? TranslationKey.CHAT_PUMP_FILL_FIRST_MODE.location : TranslationKey.CHAT_PUMP_PUSH_ONLY_MODE.location));
    }

    public TileEntityFluidPumpAlternative master() { if (!dummy) return this; TileEntity te = Utils.getExistingTileEntity(world, pos.down()); return te instanceof TileEntityFluidPumpAlternative?(TileEntityFluidPumpAlternative)te: null; }

    @Override
    public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        if (!Utils.isWirecutter(heldItem)) return false;
        master().flipFillMode(player);
        return true;
    }

    public static class DirectionalFluidOutput {
        IFluidHandler output;
        EnumFacing direction;
        TileEntity containingTile;

        public DirectionalFluidOutput(IFluidHandler output, TileEntity containingTile, EnumFacing direction) {
            this.output = output;
            this.direction = direction;
            this.containingTile = containingTile;
        }
    }
}
