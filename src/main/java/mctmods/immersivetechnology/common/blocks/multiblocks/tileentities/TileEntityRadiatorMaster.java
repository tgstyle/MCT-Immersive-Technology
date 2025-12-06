package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.RadiatorRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.blocks.multiblocks.tileentitiesmultiblockpart.TileEntityITMultiblockPartRadiator;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class TileEntityRadiatorMaster extends TileEntityRadiatorSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver {
    private static final int inputTankSize = Multiblocks.radiator.radiator_input_tankSize;
    private static final int outputTankSize = Multiblocks.radiator.radiator_output_tankSize;
    private static final float speedMult = Multiblocks.radiator.radiator_speed_multiplier;

    public FluidTank[] tanks = new FluidTank[] {new ITFluidTank(inputTankSize, this), new ITFluidTank(outputTankSize, this)};

    public int recipeTimeRemaining = 0;
    private int clientUpdateCooldown = 20;
    private double radiationEfficiency = 0;

    public RadiatorRecipe lastRecipe;
    private RadiatorRecipe cachedRecipe;

    private PoICache input, output;
    private BlockPos soundOrigin, outputFront;
    private float soundVolume;
    private boolean isRunning;
    private int gracePeriod = 60;
    private double distanceSqToTE;
    private int playerDimension;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        recipeTimeRemaining = nbt.getInteger("recipeTimeRemaining");
        radiationEfficiency = nbt.getDouble("radiationEfficiency");
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setInteger("recipeTimeRemaining", recipeTimeRemaining);
        nbt.setDouble("radiationEfficiency", radiationEfficiency);
    }

    public void requestUpdate() {
        ByteBuf buffer = Unpooled.copyBoolean(true);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendToServer(new BinaryMessageTileSync(center, buffer));
    }

    public void notifyNearbyClients() {
        ByteBuf buffer = Unpooled.copyBoolean(isRunning);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendToAllAround(new BinaryMessageTileSync(center, buffer), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 40));
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {
        ByteBuf buffer = Unpooled.copyBoolean(isRunning);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendTo(new BinaryMessageTileSync(center, buffer), player);
    }

    @Override public void receiveMessageFromServer(ByteBuf message) { isRunning = message.readBoolean(); }

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
        if (this.mirrored) radiationEfficiency = checkLineEfficiency(-2) + checkLineEfficiency(2);
        else radiationEfficiency = checkRowEfficiency(-2) + checkRowEfficiency(2);
    }

    private double checkRowEfficiency(int offsetY) {
        double halfEfficiency = 0;
        BlockPos pos2 = this.getPos().offset(this.facing, 1).add(0, offsetY, 0);
        halfEfficiency += checkColumnEfficiency(pos2, this.facing.rotateY())/12.0;
        halfEfficiency += checkColumnEfficiency(pos2, this.facing.rotateYCCW())/12.0;
        pos2 = this.getPos().offset(this.facing, 3);
        halfEfficiency += checkColumnEfficiency(pos2, this.facing.rotateY())/12.0;
        halfEfficiency += checkColumnEfficiency(pos2, this.facing.rotateYCCW())/12.0;
        pos2 = this.getPos().offset(this.facing, 3);
        halfEfficiency += checkColumnEfficiency(pos2, this.facing.rotateY())/12.0;
        halfEfficiency += checkColumnEfficiency(pos2, this.facing.rotateYCCW())/12.0;
        return halfEfficiency;
    }

    private double checkLineEfficiency(int offsetX) {
        double halfEfficiency = 0;
        BlockPos pos2 = this.getPos().offset(this.facing, 1).offset(this.facing.rotateY(), offsetX);
        halfEfficiency += checkColumnEfficiency(pos2, EnumFacing.DOWN)/12.0;
        halfEfficiency += checkColumnEfficiency(pos2, EnumFacing.UP)/12.0;
        pos2 = this.getPos().offset(this.facing, 3);
        halfEfficiency += checkColumnEfficiency(pos2, EnumFacing.DOWN)/12.0;
        halfEfficiency += checkColumnEfficiency(pos2, EnumFacing.UP)/12.0;
        pos2 = this.getPos().offset(this.facing, 3);
        halfEfficiency += checkColumnEfficiency(pos2, EnumFacing.DOWN)/12.0;
        halfEfficiency += checkColumnEfficiency(pos2, EnumFacing.UP)/12.0;
        return halfEfficiency;
    }

    private double checkColumnEfficiency(BlockPos pos, EnumFacing facing) {
        double j = 1;
        for (int i = 1; i < 49; i++) {
            if (world.isAirBlock(pos.offset(facing, i))) continue;
            j = 1.0/((49 - i) * (49 - i));
            break;
        }
        return j;
    }

    private double getTotalRadiationEfficiency(int inputFluidTemperature) {
        if (world.provider.isNether()) return 0;
        return (ITCompatModule.isAdvancedRocketryLoaded ? AdvancedRocketryHelper.getRadiatorHeatTransferCoefficient(this.world, this.getPos(), inputFluidTemperature, radiationEfficiency) : radiationEfficiency);
    }

    private void pumpOutputOut() {
        if (output == null) InitializePoIs();
        if (tanks[1].getFluidAmount() == 0) return;
        IFluidHandler handler = FluidUtil.getFluidHandler(world, outputFront, output.facing.getOpposite());
        if (handler == null) return;
        FluidStack out = tanks[1].getFluid();
        int accepted = handler.fill(out, false);
        if (accepted == 0) return;
        assert out != null;
        int drained = handler.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        this.tanks[1].drain(drained, true);
    }

    public int[] getProcessedFluidAmounts(RadiatorRecipe recipe) {
        int inputToOutputRatio = recipe.fluidInput.amount/recipe.fluidOutput.amount;
        int outputFluidAmount = (int)(getTotalRadiationEfficiency(recipe.fluidInput.getFluid().getTemperature()) * recipe.fluidOutput.amount);
        int inputFluidAmount = inputToOutputRatio * outputFluidAmount;
        return new int[]{inputFluidAmount, outputFluidAmount};
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (soundOrigin == null) InitializePoIs();
        if (isRunning) { if (soundVolume < 1) soundVolume += 0.01f; }
        else { if (soundVolume > 0) soundVolume -= 0.01f; }
        if (soundVolume == 0) { ITSoundHandler.StopSound(soundOrigin); }
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float) player.getDistanceSq(soundOrigin.getX(), soundOrigin.getY(), soundOrigin.getZ()) / 8, 1);
            ITSounds.solarTower.PlayRepeating(soundOrigin, (2 * soundVolume) / attenuation, soundVolume);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        if (soundOrigin == null) InitializePoIs();
        ITSoundHandler.StopSound(soundOrigin);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        super.disassemble();
        if (soundOrigin == null) InitializePoIs();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundOrigin), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundOrigin.getX(), soundOrigin.getY(), soundOrigin.getZ(), 0));
    }

    public void efficientMarkDirty() { world.getChunk(this.getPos()).markDirty(); }

    private boolean recipeLogic() {
        boolean update = false;
        if (recipeTimeRemaining > 0) if (gainProgress()) update = true;
        else if (tanks[0].getFluid() != null) {
            if (lastRecipe == null || !tanks[0].getFluid().isFluidEqual(lastRecipe.fluidInput)) { cachedRecipe = RadiatorRecipe.findRecipe(tanks[0].getFluid()); }
            RadiatorRecipe recipe = lastRecipe = cachedRecipe;
            if (recipe != null && recipe.fluidInput.amount <= tanks[0].getFluidAmount() && recipe.fluidOutput.amount == tanks[1].fillInternal(recipe.fluidOutput, false)) {
                recipeTimeRemaining = (int) (recipe.getTotalProcessTime() / (speedMult));
                gainProgress();
                update = true;
            }
        }
        return update;
    }

    @Override public void TankContentsChanged() {
        cachedRecipe = null;
        this.markContainingBlockForUpdate(null);
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityRadiatorMaster master() {
        master = this;
        return this;
    }

    private void clientUpdate() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distSq = player.getDistanceSq(soundOrigin.getX(), soundOrigin.getY(), soundOrigin.getZ());
        if (getWorld().provider.getDimension() == player.dimension && distSq < 400 && (distanceSqToTE > 400 || playerDimension != player.dimension)) requestUpdate();
        distanceSqToTE = distSq;
        playerDimension = player.dimension;
        handleSounds();
    }

    private void serverUpdate() {
        if (world.getTotalWorldTime() % 600 == 0) checkReflectorEfficiency();
        boolean update = recipeLogic();
        if (tanks[1].getFluidAmount() > 0) pumpOutputOut();
        boolean wasRunning = isRunning;
        if (recipeTimeRemaining > 0) gracePeriod = 60;
        else if (gracePeriod > 0) gracePeriod--;
        isRunning = gracePeriod > 0;
        if (isRunning != wasRunning) notifyNearbyClients();
        clientUpdateCooldown--;
        if (clientUpdateCooldown <= 0) {
            notifyNearbyClients();
            clientUpdateCooldown = 20;
        }
        if (update) {
            efficientMarkDirty();
            this.markContainingBlockForUpdate(null);
        }
    }

    @Override public void update() {
        super.update();
        if (world.isRemote) { clientUpdate(); return; }
        serverUpdate();
    }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartRadiator.instance.pointsOfInterest) {
            switch (poi.name) {
                case "input": input = new PoICache(facing, poi, mirrored); break;
                case "output":
                    output = new PoICache(facing, poi, mirrored);
                    outputFront = getBlockPosForPos(output.position).offset(output.facing);
                    break;
                case "sound": soundOrigin = getBlockPosForPos(poi.position); break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(input.position));
        notifyNeighbor(getBlockPosForPos(output.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if (input == null) InitializePoIs();
        if (side == null) return tanks;
        if (input.isPoI(side, position)) return new FluidTank[] {tanks[0]};
        if (output.isPoI(side, position)) return new FluidTank[] {tanks[1]};
        return ITUtils.emptyIFluidTankList;
    }

    protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (input == null) InitializePoIs();
        if (input.isPoI(side, position)) {
            if (tanks[iTank].getFluidAmount() >= tanks[iTank].getCapacity()) return false;
            FluidStack current = tanks[iTank].getFluid();
            if (current == null) return RadiatorRecipe.findRecipeByFluid(resource.getFluid()) != null;
            return resource.getFluid() == current.getFluid();
        }
        return false;
    }

    protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (output == null) InitializePoIs();
        if (output.isPoI(side, position)) return tanks[1].getFluidAmount() > 0;
        return false;
    }
}
