package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.api.crafting.MeltingCrucibleRecipe;
import mctmods.immersivetechnology.common.Config;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockMeltingCrucible;
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

    private static final int outputTankSize = Config.ITConfig.Machines.MeltingCrucible.meltingCrucible_output_tankSize;

    public FluidTank[] tanks = new FluidTank[] {
            new ITFluidTank(outputTankSize, this)
    };

    public static int slotCount = 1;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);

    IItemHandler insertionHandler = new IEInventoryHandler(slotCount, this, 0, new boolean[]{true}, new boolean[1]);

    MeltingCrucibleRecipe recipe;

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
    }

    @Override
    public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {
        ByteBuf buffer = Unpooled.copyBoolean(isRunning);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendTo(new BinaryMessageTileSync(center, buffer), player);
    }

    @Override
    public void receiveMessageFromServer(ByteBuf message) {
        isRunning = message.readBoolean();
    }

    private float soundVolume;
    private int clientUpdateCooldown = 20;
    private double distanceToTE = 0;
    private int playerDimension;
    private boolean isRunning;
    private boolean notify;

    public void handleSounds() {
        if(isRunning) {
            if(soundVolume < 1) soundVolume += 0.02f;
        } else {
            if(soundVolume > 0) soundVolume -= 0.02f;
        }
        if(soundVolume == 0) ITSoundHandler.StopSound(soundOrigin);
        else {
            float attenuation = Math.max((float) distanceToTE / 16f, 1);
            ITSounds.heatExchanger.PlayRepeating(soundOrigin, soundVolume / attenuation, 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onChunkUnload() {
        ITSoundHandler.StopSound(soundOrigin);
        super.onChunkUnload();
    }

    @Override
    public void disassemble() {
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundOrigin), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundOrigin.getX(), soundOrigin.getY(), soundOrigin.getZ(), 0));
        super.disassemble();
    }

    private void clientUpdate() {
        if(input0 == null) {
            InitializePoIs();
            requestUpdate();
        }
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double currentDistance = player.getDistanceSq(soundOrigin.getX(), soundOrigin.getY(), soundOrigin.getZ());
        if (getWorld().provider.getDimension() == player.dimension && currentDistance < 400 &&
                (distanceToTE > 400 || playerDimension != player.dimension)) requestUpdate();
        distanceToTE = currentDistance;
        playerDimension = player.dimension;
        handleSounds();
    }

    private void serverUpdate() {
        pumpOutputOut();
        boolean update = false;
        if(processQueue.size() < this.getProcessQueueMaxLength()) {
            if(!inventory.get(0).isEmpty()) {
                recipe = MeltingCrucibleRecipe.findRecipe(inventory.get(0));
                if(recipe != null && tanks[0].fill(recipe.fluidOutput, false) == recipe.fluidOutput.amount) {
                    MultiblockProcessInMachine<MeltingCrucibleRecipe> process =
                            new MultiblockProcessInMachine<>(recipe,0);
                    if(this.addProcessToQueue(process, true)) {
                        this.addProcessToQueue(process, false);
                        update = true;
                    }
                }
            }
        }

        if (tickedProcesses > 0) {
            if (!isRunning) {
                isRunning = true;
                notifyNearbyClients();
            }
        } else if (isRunning) {
            isRunning = false;
            notifyNearbyClients();
        }

        if (clientUpdateCooldown > 0) clientUpdateCooldown--;
        if (notify) notifyNearbyClients();
        if (update) {
            efficientMarkDirty();
            this.markContainingBlockForUpdate(null);
        }
    }

    @Override
    public void update() {
        super.update();
        if(world.isRemote) {
            clientUpdate();
            return;
        }
        serverUpdate();
    }

    public void efficientMarkDirty() { // !!!!!!! only use it within update() function !!!!!!!
        world.getChunkFromBlockCoords(this.getPos()).markDirty();
    }

    @Override
    public void TankContentsChanged() {
        this.markContainingBlockForUpdate(null);
    }

    @Override
    public void onProcessFinish(@Nonnull MultiblockProcess<MeltingCrucibleRecipe> process) {
        //inventory.get(0).shrink(process.recipe.itemInput.inputSize);
        tanks[0].fill(process.recipe.fluidOutput, true);
    }

    @Override
    public boolean isDummy() {
        return false;
    }

    @Override
    public TileEntityMeltingCrucibleMaster master() {
        master = this;
        return this;
    }

    private PoICache input0, output0;
    private BlockPos soundOrigin, output0Front;
    private int redstonePos, energyPos;

    private void InitializePoIs() {
        for(PoIJSONSchema poi : MultiblockMeltingCrucible.instance.pointsOfInterest) {
            switch (poi.name) {
                case "redstone":
                    redstonePos = poi.position;
                    break;
                case "energy":
                    energyPos = poi.position;
                    break;
                case "input0":
                    input0 = new PoICache(facing, poi, mirrored);
                    break;
                case "output0":
                    output0 = new PoICache(facing, poi, mirrored);
                    output0Front = getBlockPosForPos(output0.position).offset(output0.facing);
                    break;
                case "sound":
                    soundOrigin = getBlockPosForPos(poi.position);
                    break;
            }
        }
    }

    public IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if(input0 == null) InitializePoIs();
        if(side == null) return tanks;
        else if(output0.isPoI(side, position)) return new FluidTank[] {tanks[0]};
        return ITUtils.emptyIFluidTankList;
    }

    public boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource, int position) { return false; }

    public boolean canDrainTankFrom(int iTank, EnumFacing side, int position) {
        if(input0 == null) InitializePoIs();
        if(output0.isPoI(side, position)) {
            return tanks[0].getFluidAmount() > 0;
        }
        return false;
    }

    private void pumpOutputOut() {
        if(input0 == null) InitializePoIs();
        IFluidHandler output;
        if(tanks[0].getFluidAmount() > 0 && (output = FluidUtil.getFluidHandler(world, output0Front, output0.facing.getOpposite())) != null) {
            FluidStack out = tanks[0].getFluid();
            int accepted = output.fill(out, false);
            if(accepted == 0) return;
            int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
            this.tanks[0].drain(drained, true);
        }
    }

    @Nonnull
    @Override
    public int[] getRedstonePos() {
        if(input0 == null) InitializePoIs();
        return new int[] { redstonePos };
    }

    @Nonnull
    @Override
    public int[] getEnergyPos() {
        if(input0 == null) InitializePoIs();
        return new int[] { energyPos };
    }
}
