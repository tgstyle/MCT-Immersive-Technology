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
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.IBinaryMessageReceiver;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class TileEntityRadiatorMaster extends TileEntityRadiatorSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver {

    private static final int inputTankSize = Multiblocks.radiator.radiator_input_tankSize;
    private static final int outputTankSize = Multiblocks.radiator.radiator_output_tankSize;
    private static final float speedMult = Multiblocks.radiator.radiator_speed_multiplier;

    public FluidTank[] tanks = new FluidTank[] {new ITFluidTank(inputTankSize, this), new ITFluidTank(outputTankSize, this)};
    public int recipeTimeRemaining = 0;
    public int recipeTimeTotal = 0;
    public RadiatorRecipe lastRecipe;
    private RadiatorRecipe cachedRecipe;
    private double radiationEfficiency = 0;
    private int clientUpdateCooldown = 20;
    protected PoICache fluidInput0, fluidOutput0, redstone0;
    private BlockPos soundPos0, fluidOutputPos0;
    private float soundVolume;
    private boolean isRunning;
    private int gracePeriod = 0;
    private double distanceSqToTE;
    private int playerDimension;
    public Optional<Boolean> computerOn = Optional.empty();
    public boolean redstoneControlInverted = false;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        recipeTimeRemaining = nbt.getInteger("recipeTimeRemaining");
        recipeTimeTotal = nbt.getInteger("recipeTimeTotal");
        radiationEfficiency = nbt.getDouble("radiationEfficiency");
        redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setInteger("recipeTimeRemaining", recipeTimeRemaining);
        nbt.setInteger("recipeTimeTotal", recipeTimeTotal);
        nbt.setDouble("radiationEfficiency", radiationEfficiency);
        nbt.setBoolean("redstoneControlInverted", redstoneControlInverted);
    }

    public void handleSounds() {
        if (isRunning) { if (soundVolume < 1) soundVolume += 0.01f; }
        else { if (soundVolume > 0) soundVolume -= 0.01f; }
        if (soundVolume == 0) { ITSoundHandler.StopSound(soundPos0); }
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos0.getX(), soundPos0.getY(), soundPos0.getZ()) / 8, 1);
            ITSounds.solarTower.PlayRepeating(soundPos0, (2 * soundVolume) / attenuation, soundVolume);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        if (soundPos0 != null) { ITSoundHandler.StopSound(soundPos0); }
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        if (soundPos0 == null) { InitializePoIs(); }
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
        if (lastRecipe == null) { recipeTimeRemaining = 0; return true; }
        recipeTimeRemaining--;
        if (recipeTimeRemaining == 0) {
            int[] fluidAmounts = getProcessedFluidAmounts(lastRecipe);
            tanks[0].drain(fluidAmounts[0], true);
            tanks[1].fillInternal(new FluidStack(lastRecipe.fluidOutput.getFluid(), fluidAmounts[1]), true);
            markContainingBlockForUpdate(null);
            return true;
        }
        return false;
    }

    private void checkReflectorEfficiency() {
        if (mirrored) { radiationEfficiency = checkLineEfficiency(-2) + checkLineEfficiency(2); }
        else { radiationEfficiency = checkRowEfficiency(-2) + checkRowEfficiency(2); }
    }

    private double checkRowEfficiency(int offsetY) {
        double halfEfficiency = 0;
        BlockPos pos2 = getPos().offset(facing, 1).add(0, offsetY, 0);
        halfEfficiency += checkColumnEfficiency(pos2, facing.rotateY()) / 12.0;
        halfEfficiency += checkColumnEfficiency(pos2, facing.rotateYCCW()) / 12.0;
        pos2 = getPos().offset(facing, 3).add(0, offsetY, 0);
        halfEfficiency += checkColumnEfficiency(pos2, facing.rotateY()) / 12.0;
        halfEfficiency += checkColumnEfficiency(pos2, facing.rotateYCCW()) / 12.0;
        pos2 = getPos().offset(facing, 5).add(0, offsetY, 0);
        halfEfficiency += checkColumnEfficiency(pos2, facing.rotateY()) / 12.0;
        halfEfficiency += checkColumnEfficiency(pos2, facing.rotateYCCW()) / 12.0;
        return halfEfficiency;
    }

    private double checkLineEfficiency(int offsetX) {
        double halfEfficiency = 0;
        BlockPos pos2 = getPos().offset(facing, 1).offset(facing.rotateY(), offsetX);
        halfEfficiency += checkColumnEfficiency(pos2, EnumFacing.DOWN) / 12.0;
        halfEfficiency += checkColumnEfficiency(pos2, EnumFacing.UP) / 12.0;
        pos2 = getPos().offset(facing, 3).offset(facing.rotateY(), offsetX);
        halfEfficiency += checkColumnEfficiency(pos2, EnumFacing.DOWN) / 12.0;
        halfEfficiency += checkColumnEfficiency(pos2, EnumFacing.UP) / 12.0;
        pos2 = getPos().offset(facing, 5).offset(facing.rotateY(), offsetX);
        halfEfficiency += checkColumnEfficiency(pos2, EnumFacing.DOWN) / 12.0;
        halfEfficiency += checkColumnEfficiency(pos2, EnumFacing.UP) / 12.0;
        return halfEfficiency;
    }

    private double checkColumnEfficiency(BlockPos pos, EnumFacing facing) {
        double j = 1;
        for (int i = 1; i < 49; i++) {
            if (world.isAirBlock(pos.offset(facing, i))) { continue; }
            j = 1.0 / ((49 - i) * (49 - i));
            break;
        }
        return j;
    }

    private double getTotalRadiationEfficiency(int inputFluidTemperature) {
        if (world.provider.isNether()) { return 0; }
        return ITCompatModule.isAdvancedRocketryLoaded ? AdvancedRocketryHelper.getRadiatorHeatTransferCoefficient(world, getPos(), inputFluidTemperature, radiationEfficiency) : radiationEfficiency;
    }

    private void pumpOutputOut() {
        if (tanks[1].getFluidAmount() == 0) { return; }
        IFluidHandler handler = FluidUtil.getFluidHandler(world, fluidOutputPos0, fluidOutput0.facing.getOpposite());
        if (handler == null) { return; }
        FluidStack out = tanks[1].getFluid();
        int accepted = handler.fill(out, false);
        if (accepted == 0) { return; }
        assert out != null;
        int drained = handler.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        tanks[1].drain(drained, true);
    }

    public int[] getProcessedFluidAmounts(RadiatorRecipe recipe) {
        double eff = getTotalRadiationEfficiency(recipe.fluidInput.getFluid().getTemperature());
        int inputToOutputRatio = recipe.fluidInput.amount / recipe.fluidOutput.amount;
        int outputFluidAmount = (int)(eff * recipe.fluidOutput.amount);
        int inputFluidAmount = inputToOutputRatio * outputFluidAmount;
        return new int[] {inputFluidAmount, outputFluidAmount};
    }

    private boolean recipeLogic() {
        if (isRSDisabled()) { return false; }
        boolean update = false;
        if (recipeTimeRemaining > 0) {
            if (gainProgress()) { update = true; }
        } else {
            recipeTimeTotal = 0;
            if (tanks[0].getFluid() != null) {
                if (lastRecipe == null || !tanks[0].getFluid().isFluidEqual(lastRecipe.fluidInput)) { cachedRecipe = RadiatorRecipe.findRecipe(tanks[0].getFluid()); }
                RadiatorRecipe recipe = lastRecipe = cachedRecipe;
                if (recipe != null) {
                    boolean inputOk = recipe.fluidInput.amount <= tanks[0].getFluidAmount();
                    int fill = tanks[1].fillInternal(recipe.fluidOutput, false);
                    boolean outputOk = recipe.fluidOutput.amount == fill;
                    if (inputOk && outputOk) {
                        recipeTimeRemaining = (int)(recipe.getTotalProcessTime() / speedMult);
                        recipeTimeTotal = recipeTimeRemaining;
                        gainProgress();
                        update = true;
                    }
                }
            }
        }
        return update;
    }

    private void clientUpdate() {
        if (soundPos0 == null) { InitializePoIs(); }
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distSq = player.getDistanceSq(soundPos0.getX(), soundPos0.getY(), soundPos0.getZ());
        if (world.provider.getDimension() == player.dimension && distSq < 400 && (distanceSqToTE > 400 || playerDimension != player.dimension)) { requestUpdate(); }
        distanceSqToTE = distSq;
        playerDimension = player.dimension;
        handleSounds();
    }

    @Override public void update() {
        if (formed && fluidInput0 == null) { InitializePoIs(); }
        super.update();
        if (world.isRemote) { clientUpdate(); return; }
        if (world.getTotalWorldTime() % 600 == 0) { checkReflectorEfficiency(); }
        boolean update = recipeLogic();
        pumpOutputOut();
        boolean wasRunning = isRunning;
        boolean active = recipeTimeRemaining > 0 && !isRSDisabled();
        if (active) { gracePeriod = 60; }
        else if (gracePeriod > 0) { gracePeriod--; }
        isRunning = gracePeriod > 0;
        if (isRunning != wasRunning) { notifyNearbyClients(); }
        clientUpdateCooldown--;
        if (clientUpdateCooldown <= 0) {
            notifyNearbyClients();
            clientUpdateCooldown = 20;
        }
        if (update) {
            efficientMarkDirty();
            markContainingBlockForUpdate(null);
        }
    }

    @Override public void TankContentsChanged() {
        cachedRecipe = null;
        markContainingBlockForUpdate(null);
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityRadiatorMaster master() { return this; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        if (!formed) { return ITUtils.emptyIFluidTankList; }
        if (redstone0 == null) { InitializePoIs(); }
        if (side == null) { return tanks; }
        if (fluidInput0.isPoI(side, position)) { return new IFluidTank[] {tanks[0]}; }
        if (fluidOutput0.isPoI(side, position)) { return new IFluidTank[] {tanks[1]}; }
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (!formed || redstone0 == null) { InitializePoIs(); }
        if (!fluidInput0.isPoI(side, position)) { return false; }
        if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) { return false; }
        FluidStack current = tanks[0].getFluid();
        if (current == null) { return RadiatorRecipe.findRecipeByFluid(resource.getFluid()) != null; }
        return resource.isFluidEqual(current);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (!formed || redstone0 == null) { InitializePoIs(); }
        return fluidOutput0.isPoI(side, position) && tanks[1].getFluidAmount() > 0;
    }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) { return new int[0]; }
        if (redstone0 == null) { InitializePoIs(); }
        return new int[] {redstone0.position};
    }

    @Override public boolean isRSDisabled() {
        if (computerOn.isPresent()) { return !computerOn.get(); }
        int[] rsPositions = getRedstonePos();
        if (rsPositions.length < 1) { return false; }
        for (int rsPos : rsPositions) {
            TileEntity tile = getTileForPos(rsPos);
            if (tile != null) {
                int power = world.getRedstonePowerFromNeighbors(tile.getPos());
                boolean b = power > 0;
                return redstoneControlInverted != b;
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
        if (!world.isRemote) { notifyIONeighbors(); }
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(fluidInput0.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput0.position));
        notifyNeighbor(getBlockPosForPos(redstone0.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }

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
            if (master == null) return -1;
            for (int i = 0; i < master.tanks.length; i++) if (master.tanks[i] == tank) return i;
            return -1;
        }

        @Override public IFluidTankProperties[] getTankProperties() {
            IFluidTankProperties[] info = new IFluidTankProperties[tanks.length];
            for (int i = 0; i < tanks.length; i++) {
                FluidStack fs = tanks[i].getFluid();
                int idx = getTankIndex(tanks[i]);
                boolean canFill = idx == 0;
                boolean canDrain = idx == 1;
                info[i] = new FluidTankProperties(fs != null ? Utils.copyFluidStackWithAmount(fs, fs.amount, false) : null, tanks[i].getCapacity(), canFill, canDrain);
            }
            return info;
        }

        @Override public int fill(FluidStack resource, boolean doFill) {
            if (resource == null || resource.amount <= 0 || tanks.length == 0 || master == null) return 0;
            int filled = 0;
            for (IFluidTank tank : tanks) {
                int idx = getTankIndex(tank);
                if (idx == -1) continue;
                if (master.canFillTankFrom(idx, side, resource, position)) {
                    FluidStack copy = Utils.copyFluidStackWithAmount(resource, resource.amount - filled, false);
                    if (copy.amount <= 0) break;
                    int f = tank.fill(copy, doFill);
                    filled += f;
                    if (doFill && f > 0) master.TankContentsChanged();
                }
            }
            return filled;
        }

        @Override public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.amount <= 0 || tanks.length == 0 || master == null) return null;
            for (IFluidTank tank : tanks) {
                int idx = getTankIndex(tank);
                if (idx == -1) continue;
                if (master.canDrainTankFrom(idx, side, position)) {
                    FluidStack tankFluid = tank.getFluid();
                    if (tankFluid != null && tankFluid.isFluidEqual(resource)) {
                        int drainAmt = Math.min(resource.amount, tankFluid.amount);
                        FluidStack drained = tank.drain(drainAmt, doDrain);
                        if (drained != null && drained.amount > 0 && doDrain) master.TankContentsChanged();
                        return drained;
                    }
                }
            }
            return null;
        }

        @Override public FluidStack drain(int maxDrain, boolean doDrain) {
            if (maxDrain <= 0 || tanks.length == 0 || master == null) return null;
            for (IFluidTank tank : tanks) {
                int idx = getTankIndex(tank);
                if (idx == -1) continue;
                if (master.canDrainTankFrom(idx, side, position)) {
                    FluidStack drained = tank.drain(maxDrain, doDrain);
                    if (drained != null && drained.amount > 0 && doDrain) master.TankContentsChanged();
                    return drained;
                }
            }
            return null;
        }
    }
}
