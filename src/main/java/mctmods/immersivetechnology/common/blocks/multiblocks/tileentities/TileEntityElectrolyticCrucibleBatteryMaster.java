package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.ElectrolyticCrucibleBatteryRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.blocks.multiblocks.tileentitiesmultiblockpart.TileEntityITMultiblockPartElectrolyticCrucibleBattery;
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
import net.minecraft.tileentity.TileEntity;
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
import javax.annotation.Nullable;

public class TileEntityElectrolyticCrucibleBatteryMaster extends TileEntityElectrolyticCrucibleBatterySlave implements ITFluidTank.TankListener, IBinaryMessageReceiver {
    private static final int inputTankSize = Multiblocks.electrolyticCrucibleBattery.electrolyticCrucibleBattery_input_tankSize;
    private static final int outputTankSize = Multiblocks.electrolyticCrucibleBattery.electrolyticCrucibleBattery_output_tankSize;

    public FluidTank[] tanks = new FluidTank[] {new ITFluidTank(inputTankSize, this), new ITFluidTank(outputTankSize, this), new ITFluidTank(outputTankSize, this), new ITFluidTank(outputTankSize, this)};

    ElectrolyticCrucibleBatteryRecipe recipe;
    private ElectrolyticCrucibleBatteryRecipe cachedRecipe;

    private PoICache energyInput0, energyInput1, energyInput2, redstone0, input0, output0, output1, output2, itemOutput0;
    private BlockPos soundPos0;
    private BlockPos outputFront0, outputFront1, outputFront2, itemOutputFront0;

    private float soundVolume;
    private int clientUpdateCooldown = 20;
    private double distanceToTE = 0;
    private int playerDimension;
    private boolean isRunning;
    private boolean notify;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
        tanks[3].readFromNBT(nbt.getCompoundTag("tank3"));
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank2", tanks[2].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank3", tanks[3].writeToNBT(new NBTTagCompound()));
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

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {
        ByteBuf buffer = Unpooled.copyBoolean(isRunning);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendTo(new BinaryMessageTileSync(center, buffer), player);
    }

    @Override public void receiveMessageFromServer(ByteBuf message) { isRunning = message.readBoolean(); }

    public void handleSounds() {
        if (isRunning) { if (soundVolume < 1) soundVolume += 0.02f; }
        else { if (soundVolume > 0) soundVolume -= 0.02f; }
        if (soundVolume == 0) ITSoundHandler.StopSound(soundPos0);
        else {
            float attenuation = Math.max((float) distanceToTE / 16f, 1);
            ITSounds.gasTurbineArc.PlayRepeating(soundPos0, soundVolume / attenuation, 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() { ITSoundHandler.StopSound(soundPos0); super.onChunkUnload(); }

    @Override public void disassemble() {
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
        super.disassemble();
    }

    private void clientUpdate() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double currentDistance = player.getDistanceSq(soundPos0.getX(), soundPos0.getY(), soundPos0.getZ());
        if (getWorld().provider.getDimension() == player.dimension && currentDistance < 400 &&
                (distanceToTE > 400 || playerDimension != player.dimension)) requestUpdate();
        distanceToTE = currentDistance;
        playerDimension = player.dimension;
        handleSounds();
    }

    private void serverUpdate() {
        pumpOutputOut();
        boolean update = false;
        if (processQueue.size() < this.getProcessQueueMaxLength()) {
            if (tanks[0].getFluidAmount() > 0) {
                if (cachedRecipe == null) cachedRecipe = ElectrolyticCrucibleBatteryRecipe.findRecipe(tanks[0].getFluid());
                recipe = cachedRecipe;
                if (recipe != null && tanks[1].fill(recipe.fluidOutput0, false) == recipe.fluidOutput0.amount &&
                        (recipe.fluidOutput1 == null || tanks[2].fill(recipe.fluidOutput1, false) == recipe.fluidOutput1.amount) &&
                        (recipe.fluidOutput2 == null || tanks[3].fill(recipe.fluidOutput2, false) == recipe.fluidOutput2.amount)) {
                    @SuppressWarnings("unchecked")
                    MultiblockProcessInMachine<ElectrolyticCrucibleBatteryRecipe> process = new MultiblockProcessInMachine<>(recipe).setInputTanks(0);
                    if (this.addProcessToQueue(process, true)) {
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

    @Override public void update() {
        if (formed && redstone0 == null) InitializePoIs();
        super.update();
        if (world.isRemote) { clientUpdate(); return; }
        serverUpdate();
    }

    public void efficientMarkDirty() { world.getChunk(this.getPos()).markDirty(); }

    @Override public void TankContentsChanged() { cachedRecipe = null; this.markContainingBlockForUpdate(null); }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityElectrolyticCrucibleBatteryMaster master() { master = this; return this; }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartElectrolyticCrucibleBattery.instance.pointsOfInterest) {
            switch (poi.name) {
                case "redstone0": redstone0 = new PoICache(this.facing, poi, this.mirrored); break;
                case "sound": soundPos0 = getBlockPosForPos(poi.position); break;
                case "input0": input0 = new PoICache(this.facing, poi, this.mirrored); break;
                case "energy_input0": energyInput0 = new PoICache(this.facing, poi, this.mirrored); break;
                case "energy_input1": energyInput1 = new PoICache(this.facing, poi, this.mirrored); break;
                case "energy_input2": energyInput2 = new PoICache(this.facing, poi, this.mirrored); break;
                case "output0":
                    output0 = new PoICache(this.facing, poi, this.mirrored);
                    outputFront0 = getBlockPosForPos(output0.position).offset(output0.facing);
                    break;
                case "output1":
                    output1 = new PoICache(this.facing, poi, this.mirrored);
                    outputFront1 = getBlockPosForPos(output1.position).offset(output1.facing);
                    break;
                case "output2":
                    output2 = new PoICache(this.facing, poi, this.mirrored);
                    outputFront2 = getBlockPosForPos(output2.position).offset(output2.facing);
                    break;
                case "item_output0":
                    itemOutput0 = new PoICache(this.facing, poi, this.mirrored);
                    itemOutputFront0 = getBlockPosForPos(itemOutput0.position).offset(itemOutput0.facing);
                    break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(redstone0.position));
        notifyNeighbor(getBlockPosForPos(input0.position));
        notifyNeighbor(getBlockPosForPos(energyInput0.position));
        notifyNeighbor(getBlockPosForPos(energyInput1.position));
        notifyNeighbor(getBlockPosForPos(energyInput2.position));
        notifyNeighbor(getBlockPosForPos(output0.position));
        notifyNeighbor(getBlockPosForPos(output1.position));
        notifyNeighbor(getBlockPosForPos(output2.position));
        notifyNeighbor(getBlockPosForPos(itemOutput0.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override public @Nonnull int[] getRedstonePos() {
        if (!formed) return new int[0];
        return new int[]{redstone0.position};
    }

    @Override public @Nonnull int[] getEnergyPos() {
        if (!formed) return new int[0];
        return new int[]{energyInput0.position, energyInput1.position, energyInput2.position};
    }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, int position) {
        if (!formed) return false;
        if (facing == null) return false;
        if (energyInput0.isPoI(facing, position)) return true;
        if (energyInput1.isPoI(facing, position)) return true;
        return energyInput2.isPoI(facing, position);
    }

    @Override public void onProcessFinish(@Nonnull MultiblockProcess<ElectrolyticCrucibleBatteryRecipe> process) {
        tanks[1].fill(process.recipe.fluidOutput0, true);
        if (process.recipe.fluidOutput1 != null) tanks[2].fill(process.recipe.fluidOutput1, true);
        if (process.recipe.fluidOutput2 != null) tanks[3].fill(process.recipe.fluidOutput2, true);
        if (process.recipe.itemOutput != null && !process.recipe.itemOutput.isEmpty()) {
            TileEntity inventoryTile = world.getTileEntity(itemOutputFront0);
            ItemStack output = Utils.insertStackIntoInventory(inventoryTile, process.recipe.itemOutput.copy(), itemOutput0.facing.getOpposite());
            if (output != null && !output.isEmpty()) Utils.dropStackAtPos(world, itemOutputFront0, output, itemOutput0.facing);
        }
    }

    @Override protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (side == null) return tanks;
        if (input0.isPoI(side, position)) return new FluidTank[]{tanks[0]};
        if (output0.isPoI(side, position)) return new FluidTank[]{tanks[1]};
        if (output1.isPoI(side, position)) return new FluidTank[]{tanks[2]};
        if (output2.isPoI(side, position)) return new FluidTank[]{tanks[3]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (!input0.isPoI(side, position)) return false;
        if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
        if (tanks[0].getFluid() == null) return ElectrolyticCrucibleBatteryRecipe.findRecipeFluid(resource.getFluid()) != null;
        return resource.getFluid() == tanks[0].getFluid().getFluid();
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (output0.isPoI(side, position)) return tanks[1].getFluidAmount() > 0;
        if (output1.isPoI(side, position)) return tanks[2].getFluidAmount() > 0;
        if (output2.isPoI(side, position)) return tanks[3].getFluidAmount() > 0;
        return false;
    }

    private void pumpOutputOut() {
        IFluidHandler output;
        if (tanks[1].getFluidAmount() > 0) {
            output = FluidUtil.getFluidHandler(world, outputFront0, output0.facing.getOpposite());
            if (output != null) {
                FluidStack out = tanks[1].getFluid();
                int accepted = output.fill(out, false);
                if (accepted > 0) {
                    assert out != null;
                    int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                    this.tanks[1].drain(drained, true);
                }
            }
        }
        if (tanks[2].getFluidAmount() > 0) {
            output = FluidUtil.getFluidHandler(world, outputFront1, output1.facing.getOpposite());
            if (output != null) {
                FluidStack out = tanks[2].getFluid();
                int accepted = output.fill(out, false);
                if (accepted > 0) {
                    assert out != null;
                    int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                    this.tanks[2].drain(drained, true);
                }
            }
        }
        if (tanks[3].getFluidAmount() > 0) {
            output = FluidUtil.getFluidHandler(world, outputFront2, output2.facing.getOpposite());
            if (output != null) {
                FluidStack out = tanks[3].getFluid();
                int accepted = output.fill(out, false);
                if (accepted > 0) {
                    assert out != null;
                    int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                    this.tanks[3].drain(drained, true);
                }
            }
        }
    }
}
