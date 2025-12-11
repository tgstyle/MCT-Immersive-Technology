package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.MeltingCrucibleRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartMeltingCrucible;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.multiblock.PoICache;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.IBinaryMessageReceiver;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class TileEntityMeltingCrucibleMaster extends TileEntityMeltingCrucibleSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver {
    private static final int outputTankSize = Multiblocks.meltingCrucible.meltingCrucible_output_tankSize;
    public FluidTank[] tanks = new FluidTank[] { new ITFluidTank(outputTankSize, this) };
    public static int slotCount = 1;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    IItemHandler insertionHandler = new IEInventoryHandler(slotCount, this, 0, new boolean[]{true}, new boolean[]{false});
    MeltingCrucibleRecipe recipe;
    private MeltingCrucibleRecipe cachedRecipe;
    PoICache itemInput0;
    PoICache fluidOutput0;
    private BlockPos soundPos0, fluidOutputFront0;
    private PoICache redstone0;
    private PoICache energyInput0;
    private float soundVolume;
    private float targetVolume;
    private int clientUpdateCooldown = 20;
    private double distanceSqToTE;
    private int playerDimension;
    private boolean isRunning;
    private boolean notify;
    private int gracePeriod = 60;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("inventory", Utils.writeInventory(inventory));
    }

    public void requestUpdate() { BinaryMessageTileSync.sendToServer(getPos(), Unpooled.copyBoolean(true)); }

    public void notifyNearbyClients() {
        if (clientUpdateCooldown > 0) { notify = true; return; }
        clientUpdateCooldown = 20;
        ImmersiveTechnology.packetHandler.sendToAllAround(new BinaryMessageTileSync(getPos(), Unpooled.copyBoolean(isRunning)), new NetworkRegistry.TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 40));
        notify = false;
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) { BinaryMessageTileSync.sendToPlayer(player, getPos(), Unpooled.copyBoolean(isRunning)); }

    @Override public void receiveMessageFromServer(ByteBuf message) { targetVolume = message.readBoolean() ? 1f : 0f; }

    public void handleSounds() {
        if (soundVolume < targetVolume) { soundVolume = Math.min(soundVolume + 0.02f, targetVolume); }
        else if (soundVolume > targetVolume) { soundVolume = Math.max(soundVolume - 0.02f, targetVolume); }
        if (soundVolume == 0) { ITSoundHandler.StopSound(soundPos0); }
        else {
            float attenuation = Math.max((float) Math.sqrt(distanceSqToTE) / 8, 1);
            ITSounds.heatExchanger.PlayRepeating(soundPos0, (10 * soundVolume) / attenuation, 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        ITSoundHandler.StopSound(soundPos0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        if (soundPos0 == null) InitializePoIs();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
        super.disassemble();
    }

    private void clientUpdate() {
        if (soundPos0 == null) InitializePoIs();
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double currentDistance = player.getDistanceSq(soundPos0.getX(), soundPos0.getY(), soundPos0.getZ());
        if (getWorld().provider.getDimension() == player.dimension && currentDistance < 400 && (distanceSqToTE > 400 || playerDimension != player.dimension)) { requestUpdate(); }
        distanceSqToTE = currentDistance;
        playerDimension = player.dimension;
        handleSounds();
    }

    private void serverUpdate() {
        pumpOutputOut();
        boolean update = false;
        if (processQueue.size() < this.getProcessQueueMaxLength()) {
            if (!inventory.get(0).isEmpty()) {
                if (cachedRecipe == null) cachedRecipe = MeltingCrucibleRecipe.findRecipe(inventory.get(0));
                recipe = cachedRecipe;
                if (recipe != null && inventory.get(0).getCount() >= recipe.itemInput.inputSize && tanks[0].fill(recipe.fluidOutput, false) == recipe.fluidOutput.amount) {
                    MultiblockProcessInMachine<MeltingCrucibleRecipe> process = new MultiblockProcessInMachine<>(recipe, 0);
                    if (this.addProcessToQueue(process, true)) {
                        this.addProcessToQueue(process, false);
                        inventory.get(0).shrink(recipe.itemInput.inputSize);
                        update = true;
                    }
                }
            } else if (cachedRecipe != null) cachedRecipe = null;
        }
        boolean wasRunning = isRunning;
        if (tickedProcesses > 0) gracePeriod = 60;
        else if (gracePeriod > 0) gracePeriod--;
        isRunning = gracePeriod > 0;
        if (isRunning != wasRunning) notifyNearbyClients();
        clientUpdateCooldown--;
        if (notify) notifyNearbyClients();
        if (update) {
            efficientMarkDirty();
            this.markContainingBlockForUpdate(null);
        }
    }

    @Override public void update() {
        if (formed && redstone0 == null) InitializePoIs();
        super.update();
        if (world.isRemote) {
            clientUpdate();
            return;
        }
        serverUpdate();
    }

    public void efficientMarkDirty() { world.getChunk(this.getPos()).markDirty(); }

    @Override public void TankContentsChanged() { this.markContainingBlockForUpdate(null); }

    @Override public void onProcessFinish(@Nonnull MultiblockProcess<MeltingCrucibleRecipe> process) { tanks[0].fill(process.recipe.fluidOutput, true); }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityMeltingCrucibleMaster master() { return this; }

    void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartMeltingCrucible.instance.pointsOfInterest) {
            switch (poi.name) {
                case "redstone": redstone0 = new PoICache(facing, poi, mirrored); break;
                case "energy_input": energyInput0 = new PoICache(facing, poi, mirrored); break;
                case "item_input": itemInput0 = new PoICache(facing, poi, mirrored); break;
                case "fluid_output":
                    fluidOutput0 = new PoICache(facing, poi, mirrored);
                    fluidOutputFront0 = getBlockPosForPos(fluidOutput0.position).offset(fluidOutput0.facing);
                    break;
                case "sound": soundPos0 = getBlockPosForPos(poi.position); break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(itemInput0.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput0.position));
        notifyNeighbor(getBlockPosForPos(redstone0.position));
        notifyNeighbor(getBlockPosForPos(energyInput0.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if (side == null) return new IFluidTank[] {tanks[0]};
        else if (fluidOutput0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        return ITUtils.emptyIFluidTankList;
    }

    protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) { return false; }

    protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) { return fluidOutput0.isPoI(side, position); }

    private void pumpOutputOut() {
        if (tanks[0].getFluidAmount() == 0) return;
        if (fluidOutput0 == null) InitializePoIs();
        IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputFront0, fluidOutput0.facing.getOpposite());
        if (output == null) return;
        FluidStack out = tanks[0].getFluid();
        int accepted = output.fill(out, false);
        if (accepted == 0) return;
        assert out != null;
        int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        this.tanks[0].drain(drained, true);
    }

    @Override public @Nonnull int[] getRedstonePos() {
        if (redstone0 == null) InitializePoIs();
        return new int[] {redstone0.position};
    }

    @Override public @Nonnull int[] getEnergyPos() {
        if (energyInput0 == null) InitializePoIs();
        return new int[] {energyInput0.position};
    }
}
