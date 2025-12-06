package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.MeltingCrucibleRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.blocks.multiblocks.tileentitiesmultiblockpart.TileEntityITMultiblockPartMeltingCrucible;
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

    public FluidTank[] tanks = new FluidTank[] {new ITFluidTank(outputTankSize, this)};

    public static int slotCount = 1;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);

    IItemHandler insertionHandler = new IEInventoryHandler(slotCount, this, 0, new boolean[] {true}, new boolean[]{false});

    MeltingCrucibleRecipe recipe;
    private MeltingCrucibleRecipe cachedRecipe;

    private PoICache output0;
    PoICache input;
    private BlockPos soundOrigin, output0Front;
    private int redstonePos = -1, energyPos = -1;
    private float soundVolume;
    private float targetVolume;
    private int clientUpdateCooldown = 20;
    private double distanceSqToTE;
    private int playerDimension;
    private boolean isRunning;
    private boolean notify;
    private int gracePeriod = 60;

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
    }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("inventory", Utils.writeInventory(inventory));
    }

    public void requestUpdate() {
        ByteBuf buffer = Unpooled.copyBoolean(true);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendToServer(new BinaryMessageTileSync(center, buffer));
    }

    public void notifyNearbyClients() {
        if (clientUpdateCooldown > 0) {
            notify = true;
            return;
        }
        clientUpdateCooldown = 20;
        ByteBuf buffer = Unpooled.copyBoolean(isRunning);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendToAllAround(new BinaryMessageTileSync(center, buffer), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 40));
        notify = false;
    }

    @Override
    public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {
        ByteBuf buffer = Unpooled.copyBoolean(isRunning);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendTo(new BinaryMessageTileSync(center, buffer), player);
    }

    @Override
    public void receiveMessageFromServer(ByteBuf message) { targetVolume = message.readBoolean() ? 1f : 0f; }

    public void handleSounds() {
        if (soundOrigin == null) InitializePoIs();
        if (soundVolume < targetVolume) { soundVolume = Math.min(soundVolume + 0.02f, targetVolume); }
        else if (soundVolume > targetVolume) { soundVolume = Math.max(soundVolume - 0.02f, targetVolume); }
        if (soundVolume == 0) { ITSoundHandler.StopSound(soundOrigin); }
        else {
            float attenuation = Math.max((float) Math.sqrt(distanceSqToTE) / 8, 1);
            ITSounds.heatExchanger.PlayRepeating(soundOrigin, (10 * soundVolume) / attenuation, 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onChunkUnload() {
        if (soundOrigin == null) InitializePoIs();
        ITSoundHandler.StopSound(soundOrigin);
        super.onChunkUnload();
    }

    @Override
    public void disassemble() {
        if (soundOrigin == null) InitializePoIs();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundOrigin), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundOrigin.getX(), soundOrigin.getY(), soundOrigin.getZ(), 0));
        super.disassemble();
    }

    private void clientUpdate() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double currentDistance = player.getDistanceSq(soundOrigin.getX(), soundOrigin.getY(), soundOrigin.getZ());
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

    @Override
    public void update() {
        super.update();
        if (world.isRemote) {
            clientUpdate();
            return;
        }
        serverUpdate();
    }

    public void efficientMarkDirty() { world.getChunk(this.getPos()).markDirty(); }

    @Override
    public void TankContentsChanged() { this.markContainingBlockForUpdate(null); }

    @Override
    public void onProcessFinish(@Nonnull MultiblockProcess<MeltingCrucibleRecipe> process) { tanks[0].fill(process.recipe.fluidOutput, true); }

    @Override
    public boolean isDummy() { return false; }

    @Override
    public TileEntityMeltingCrucibleMaster master() {
        master = this;
        return this;
    }

    void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartMeltingCrucible.instance.pointsOfInterest) {
            switch (poi.name) {
                case "redstone": redstonePos = poi.position; break;
                case "energy": energyPos = poi.position; break;
                case "output":
                    output0 = new PoICache(facing, poi, mirrored);
                    output0Front = getBlockPosForPos(output0.position).offset(output0.facing);
                    break;
                case "input": input = new PoICache(facing, poi, mirrored); break;
                case "sound": soundOrigin = getBlockPosForPos(poi.position); break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(output0.position));
        notifyNeighbor(getBlockPosForPos(redstonePos));
        notifyNeighbor(getBlockPosForPos(energyPos));
        notifyNeighbor(getBlockPosForPos(input.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if (output0 == null) InitializePoIs();
        if (side == null) return tanks;
        else if (output0.isPoI(side, position)) return new FluidTank[] {tanks[0]};
        return ITUtils.emptyIFluidTankList;
    }

    protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) { return false; }

    protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (output0 == null) InitializePoIs();
        if (output0.isPoI(side, position)) return tanks[0].getFluidAmount() > 0;
        return false;
    }

    private void pumpOutputOut() {
        if (output0 == null) InitializePoIs();
        if (tanks[0].getFluidAmount() == 0) return;
        IFluidHandler output;
        if ((output = FluidUtil.getFluidHandler(world, output0Front, output0.facing.getOpposite())) != null) {
            FluidStack out = tanks[0].getFluid();
            int accepted = output.fill(out, false);
            if (accepted == 0) return;
            assert out != null;
            int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
            this.tanks[0].drain(drained, true);
        }
    }

    @Override
    public @Nonnull int[] getRedstonePos() {
        if (redstonePos == -1) InitializePoIs();
        return new int[] {redstonePos};
    }

    @Override
    public @Nonnull int[] getEnergyPos() {
        if (energyPos == -1) InitializePoIs();
        return new int[] {energyPos};
    }
}
