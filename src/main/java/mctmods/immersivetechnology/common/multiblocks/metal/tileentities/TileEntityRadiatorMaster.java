package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.crafting.RadiatorRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartRadiator;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
import mctmods.immersivetechnology.common.util.compat.advancedrocketry.AdvancedRocketryHelper;
import mctmods.immersivetechnology.common.util.multiblock.PoICache;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.IBinaryMessageReceiver;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class TileEntityRadiatorMaster extends TileEntityRadiatorSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver {

    private static final int inputTankSize = Multiblocks.radiator.radiator_input_tankSize;
    private static final int outputTankSize = Multiblocks.radiator.radiator_output_tankSize;
    private static final float speedMult = Multiblocks.radiator.radiator_speed_multiplier;

    public FluidTank[] tanks = new FluidTank[] {
            new ITFluidTank(inputTankSize, this),
            new ITFluidTank(outputTankSize, this)
    };

    private RadiatorRecipe cachedRadiatorRecipe;

    public int processTimeRemaining = 0;
    public int processTimeTotal = 0;
    private double radiationEfficiency = 0;
    private int clientUpdateCooldown = 20;
    protected PoICache fluidInput0, fluidOutput0, redstone0;
    private BlockPos soundPos0, fluidOutputPos0;
    private float soundVolume = 0f;
    private int soundGracePeriod = 60;
    private boolean isRunning;
    private double distanceSqToTE;
    private int playerDimension;
    public Optional<Boolean> computerOn = Optional.empty();
    public boolean redstoneControlInverted = false;
    private boolean needsPoIInit = true;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        processTimeRemaining = nbt.getInteger("processTimeRemaining");
        processTimeTotal = nbt.getInteger("processTimeTotal");
        radiationEfficiency = nbt.getDouble("radiationEfficiency");
        redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
        if (!descPacket) {
            if (nbt.hasKey("cachedRecipe")) cachedRadiatorRecipe = RadiatorRecipe.loadFromNBT(nbt.getCompoundTag("cachedRecipe"));
            else if (processTimeRemaining > 0 && tanks[0].getFluid() != null && tanks[0].getFluidAmount() > 0) {
                cachedRadiatorRecipe = RadiatorRecipe.findRecipe(tanks[0].getFluid());
            }
            if (cachedRadiatorRecipe == null && processTimeRemaining > 0) processTimeRemaining = 0;
        }
        if (!descPacket && formed) needsPoIInit = true;
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setInteger("processTimeRemaining", processTimeRemaining);
        nbt.setInteger("processTimeTotal", processTimeTotal);
        nbt.setDouble("radiationEfficiency", radiationEfficiency);
        nbt.setBoolean("redstoneControlInverted", redstoneControlInverted);
        if (!descPacket && cachedRadiatorRecipe != null) nbt.setTag("cachedRecipe", cachedRadiatorRecipe.writeToNBT(new NBTTagCompound()));
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (soundPos0 == null) InitializePoIs();
        float targetSoundLevel = isRunning ? 1f : 0f;
        if (soundVolume < targetSoundLevel) {
            soundVolume = Math.min(soundVolume + 0.01f, targetSoundLevel);
            soundGracePeriod = 60;
        } else if (soundVolume > targetSoundLevel) {
            if (soundGracePeriod > 0) soundGracePeriod--;
            else soundVolume = Math.max(soundVolume - 0.01f, targetSoundLevel);
        }
        if (soundVolume == 0) ITSoundHandler.StopSound(soundPos0);
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos0.getX() + .5, soundPos0.getY() + .5, soundPos0.getZ() + .5) / 8, 1);
            ITSounds.solarTower.PlayRepeating(soundPos0, (2 * soundVolume) / attenuation, soundVolume);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        if (soundPos0 != null) ITSoundHandler.StopSound(soundPos0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        if (soundPos0 == null) InitializePoIs();
        if (soundPos0 != null) {
            ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
        }
        super.disassemble();
    }

    public void requestUpdate() { BinaryMessageTileSync.sendToServer(getPos(), Unpooled.copyBoolean(true)); }

    public void notifyNearbyClients() { BinaryMessageTileSync.sendToAllTracking(world, getPos(), Unpooled.copyBoolean(isRunning)); }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) { BinaryMessageTileSync.sendToPlayer(player, getPos(), Unpooled.copyBoolean(isRunning)); }

    @Override public void receiveMessageFromServer(ByteBuf message) { isRunning = message.readBoolean(); }

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    private boolean gainProgress() {
        processTimeRemaining--;
        if (processTimeRemaining > 0) return false;
        processTimeRemaining = 0;
        if (cachedRadiatorRecipe != null) {
            int[] fluidAmounts = getProcessedFluidAmounts(cachedRadiatorRecipe);
            FluidStack outputStack = cachedRadiatorRecipe.fluidOutput;
            Fluid fluid = (outputStack != null) ? outputStack.getFluid() : null;
            int outputAmount = fluidAmounts[1];
            tanks[0].drain(fluidAmounts[0], true);
            if (fluid != null && outputAmount > 0) {
                FluidStack out = new FluidStack(fluid, outputAmount);
                tanks[1].fillInternal(out, true);
            }
        }
        cachedRadiatorRecipe = null;
        return true;
    }

    private void checkReflectorEfficiency() {
        if (mirrored) radiationEfficiency = checkLineEfficiency(-2) + checkLineEfficiency(2);
        else radiationEfficiency = checkRowEfficiency(-2) + checkRowEfficiency(2);
    }

    private double checkRowEfficiency(int offsetY) {
        double half = 0;
        BlockPos p = getPos().offset(facing, 1).add(0, offsetY, 0);
        half += checkColumnEfficiency(p, facing.rotateY()) / 12.0;
        half += checkColumnEfficiency(p, facing.rotateYCCW()) / 12.0;
        p = getPos().offset(facing, 3).add(0, offsetY, 0);
        half += checkColumnEfficiency(p, facing.rotateY()) / 12.0;
        half += checkColumnEfficiency(p, facing.rotateYCCW()) / 12.0;
        p = getPos().offset(facing, 5).add(0, offsetY, 0);
        half += checkColumnEfficiency(p, facing.rotateY()) / 12.0;
        half += checkColumnEfficiency(p, facing.rotateYCCW()) / 12.0;
        return half;
    }

    private double checkLineEfficiency(int offsetX) {
        double half = 0;
        BlockPos p = getPos().offset(facing, 1).offset(facing.rotateY(), offsetX);
        half += checkColumnEfficiency(p, EnumFacing.DOWN) / 12.0;
        half += checkColumnEfficiency(p, EnumFacing.UP) / 12.0;
        p = getPos().offset(facing, 3).offset(facing.rotateY(), offsetX);
        half += checkColumnEfficiency(p, EnumFacing.DOWN) / 12.0;
        half += checkColumnEfficiency(p, EnumFacing.UP) / 12.0;
        p = getPos().offset(facing, 5).offset(facing.rotateY(), offsetX);
        half += checkColumnEfficiency(p, EnumFacing.DOWN) / 12.0;
        half += checkColumnEfficiency(p, EnumFacing.UP) / 12.0;
        return half;
    }

    private double checkColumnEfficiency(BlockPos pos, EnumFacing dir) {
        for (int i = 1; i < 49; i++) {
            if (!world.isAirBlock(pos.offset(dir, i))) return 1.0 / ((49 - i) * (49 - i));
        }
        return 1;
    }

    private double getTotalRadiationEfficiency(int temp) {
        if (world.provider.isNether()) return 0;
        return ITCompatModule.isAdvancedRocketryLoaded ? AdvancedRocketryHelper.getRadiatorHeatTransferCoefficient(world, getPos(), temp, radiationEfficiency) : radiationEfficiency;
    }

    private boolean pumpOutputOut() {
        if (tanks[1].getFluidAmount() == 0) return false;
        IFluidHandler handler = FluidUtil.getFluidHandler(world, fluidOutputPos0, fluidOutput0.facing.getOpposite());
        if (handler == null) return false;
        FluidStack out = tanks[1].getFluid();
        if (out == null) return false;
        int accepted = handler.fill(out, false);
        if (accepted <= 0) return false;
        int drained = handler.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        tanks[1].drain(drained, true);
        return drained > 0;
    }

    public int[] getProcessedFluidAmounts(RadiatorRecipe recipe) {
        if (recipe == null || recipe.fluidInput == null || recipe.fluidInput.getFluid() == null) return new int[]{0, 0};
        int baseInput = recipe.fluidInput.amount;
        if (recipe.fluidOutput == null || recipe.fluidOutput.getFluid() == null) return new int[]{baseInput, 0};
        int baseOutput = recipe.fluidOutput.amount;
        double eff = getTotalRadiationEfficiency(recipe.fluidInput.getFluid().getTemperature());
        int output = (int)(eff * baseOutput);
        int input = (baseInput * output) / baseOutput;
        return new int[]{input, output};
    }

    private boolean recipeLogic() {
        if (isRSDisabled()) return false;
        boolean update = false;
        if (processTimeRemaining > 0) {
            if (gainProgress()) update = true;
        } else {
            processTimeTotal = 0;
            FluidStack input = tanks[0].getFluid();
            if (input != null && input.amount > 0) {
                RadiatorRecipe recipe = RadiatorRecipe.findRecipe(input);
                if (recipe != null) {
                    int[] amounts = getProcessedFluidAmounts(recipe);
                    boolean inputOk = amounts[0] <= input.amount;
                    boolean outputOk = true;
                    if (recipe.fluidOutput != null) {
                        FluidStack sim = recipe.fluidOutput.copy();
                        sim.amount = amounts[1];
                        if (sim.getFluid() != null) {
                            outputOk = tanks[1].fillInternal(sim, false) == sim.amount;
                        } else {
                            outputOk = false;
                        }
                    }
                    if (inputOk && outputOk) {
                        cachedRadiatorRecipe = recipe;
                        processTimeRemaining = (int)(recipe.getTotalProcessTime() / speedMult);
                        processTimeTotal = processTimeRemaining;
                        update = true;
                    }
                }
            }
        }
        return update;
    }

    @SideOnly(Side.CLIENT)
    private void clientUpdate() {
        if (soundPos0 == null) InitializePoIs();
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distSq = player.getDistanceSq(soundPos0.getX() + .5, soundPos0.getY() + .5, soundPos0.getZ() + .5);
        if (world.provider.getDimension() == player.dimension && distSq < 400 && (distanceSqToTE > 400 || playerDimension != player.dimension)) requestUpdate();
        distanceSqToTE = distSq;
        playerDimension = player.dimension;
        handleSounds();
    }

    @Override public void update() {
        if (world.isRemote) {
            clientUpdate();
            return;
        }
        if (formed && (needsPoIInit || fluidInput0 == null)) {
            InitializePoIs();
            needsPoIInit = false;
        }
        super.update();
        if (!formed) return;

        boolean update = false;
        double oldEff = radiationEfficiency;
        if (radiationEfficiency == 0 || world.getTotalWorldTime() % 600 == 0) checkReflectorEfficiency();
        if (radiationEfficiency != oldEff) update = true;

        update |= recipeLogic();
        if (pumpOutputOut()) update = true;

        boolean wasRunning = isRunning;
        boolean active = processTimeRemaining > 0 && !isRSDisabled();
        if (active) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;
        isRunning = soundGracePeriod > 0;

        if (isRunning != wasRunning) notifyNearbyClients();

        clientUpdateCooldown--;
        if (clientUpdateCooldown <= 0) {
            notifyNearbyClients();
            clientUpdateCooldown = 20;
        }

        if (update || isRunning != wasRunning) {
            efficientMarkDirty();
            markContainingBlockForUpdate(null);
        }
    }

    @Override public void TankContentsChanged() {
        cachedRadiatorRecipe = null;
        markContainingBlockForUpdate(null);
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityRadiatorMaster master() { return this; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (redstone0 == null) InitializePoIs();
        if (side == null) return tanks;
        if (fluidInput0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        if (fluidOutput0.isPoI(side, position)) return new IFluidTank[] {tanks[1]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (!formed || redstone0 == null) InitializePoIs();
        if (!fluidInput0.isPoI(side, position)) return false;
        if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
        FluidStack current = tanks[0].getFluid();
        if (current == null) return RadiatorRecipe.findRecipeByFluid(resource.getFluid()) != null;
        return resource.isFluidEqual(current);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (!formed || redstone0 == null) InitializePoIs();
        return fluidOutput0.isPoI(side, position) && tanks[1].getFluidAmount() > 0;
    }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstone0 == null) InitializePoIs();
        return new int[] {redstone0.position};
    }

    @Override public boolean isRSDisabled() {
        if (computerOn.isPresent()) return !computerOn.get();
        int[] rs = getRedstonePos();
        if (rs.length < 1) return false;
        for (int p : rs) {
            TileEntity te = getTileForPos(p);
            if (te != null) {
                int power = world.getRedstonePowerFromNeighbors(te.getPos());
                return redstoneControlInverted != (power > 0);
            }
        }
        return false;
    }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartRadiator.instance.pointsOfInterest) {
            switch (poi.name) {
                case "fluid_input0":
                    fluidInput0 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_output0":
                    fluidOutput0 = new PoICache(facing, poi, mirrored);
                    fluidOutputPos0 = getBlockPosForPos(fluidOutput0.position).offset(fluidOutput0.facing);
                    break;
                case "redstone0":
                    redstone0 = new PoICache(facing, poi, mirrored);
                    break;
                case "sound0":
                    soundPos0 = getBlockPosForPos(poi.position);
                    break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(fluidInput0.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput0.position));
        notifyNeighbor(getBlockPosForPos(redstone0.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }

    @Override public int getComparatorInputOverride() {
        if (!formed || processTimeTotal <= 0) return 0;
        return 15 * (processTimeTotal - processTimeRemaining) / processTimeTotal;
    }

    static class RadiatorFluidHandler implements IFluidHandler {
        private final IFluidTank[] tanks;
        private final TileEntityRadiatorMaster master;
        private final EnumFacing side;
        private final int position;

        RadiatorFluidHandler(IFluidTank[] accessibleTanks, TileEntityRadiatorMaster master, EnumFacing side, int position) {
            this.tanks = accessibleTanks;
            this.master = master;
            this.side = side;
            this.position = position;
        }

        private int getTankIndex(IFluidTank tank) {
            for (int i = 0; i < master.tanks.length; i++) if (master.tanks[i] == tank) return i;
            return -1;
        }

        @Override public IFluidTankProperties[] getTankProperties() {
            List<IFluidTankProperties> list = new ArrayList<>();
            for (IFluidTank tank : tanks) {
                int idx = getTankIndex(tank);
                boolean fill = idx == 0;
                boolean drain = idx == 1;
                list.add(new FluidTankProperties(tank.getFluid(), tank.getCapacity(), fill, drain));
            }
            return list.toArray(new IFluidTankProperties[0]);
        }

        @Override public int fill(FluidStack resource, boolean doFill) {
            if (resource == null) return 0;
            resource = resource.copy();
            int filled = 0;
            for (IFluidTank tank : tanks) {
                int idx = getTankIndex(tank);
                if (idx != -1 && master.canFillTankFrom(idx, side, resource, position)) {
                    int f = tank.fill(resource, doFill);
                    filled += f;
                    resource.amount -= f;
                    if (doFill && f > 0) master.TankContentsChanged();
                    if (resource.amount <= 0) return filled;
                }
            }
            return filled;
        }

        @Override public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null) return null;
            resource = resource.copy();
            FluidStack drained = null;
            for (IFluidTank tank : tanks) {
                int idx = getTankIndex(tank);
                if (idx != -1 && master.canDrainTankFrom(idx, side, position)) {
                    FluidStack tf = tank.getFluid();
                    if (tf != null && tf.isFluidEqual(resource)) {
                        int amt = Math.min(resource.amount, tf.amount);
                        FluidStack d = tank.drain(amt, doDrain);
                        if (d != null) {
                            if (drained == null) drained = d.copy();
                            else drained.amount += d.amount;
                            if (doDrain && d.amount > 0) master.TankContentsChanged();
                            if (resource.amount <= d.amount) return drained;
                            resource.amount -= d.amount;
                        }
                    }
                }
            }
            return drained;
        }

        @Override public FluidStack drain(int maxDrain, boolean doDrain) {
            int remaining = maxDrain;
            FluidStack drained = null;
            for (IFluidTank tank : tanks) {
                int idx = getTankIndex(tank);
                if (idx != -1 && master.canDrainTankFrom(idx, side, position)) {
                    FluidStack d = tank.drain(remaining, doDrain);
                    if (d != null) {
                        if (drained == null) drained = d.copy();
                        else if (drained.isFluidEqual(d)) drained.amount += d.amount;
                        remaining -= d.amount;
                        if (doDrain && d.amount > 0) master.TankContentsChanged();
                        if (remaining <= 0) return drained;
                    }
                }
            }
            return drained;
        }
    }
}