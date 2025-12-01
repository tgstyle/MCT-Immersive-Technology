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

    private final PoICache[] energyInputs = new PoICache[3];
    private PoICache redstone, input0;
    private final PoICache[] outputs = new PoICache[4];
    private BlockPos soundOrigin;
    private final BlockPos[] outputFronts = new BlockPos[4];

    private float soundVolume;
    private int clientUpdateCooldown = 20;
    private double distanceToTE = 0;
    private int playerDimension;
    private boolean isRunning;
    private boolean notify;

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
        tanks[3].readFromNBT(nbt.getCompoundTag("tank3"));
    }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
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

    @Override
    public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {
        ByteBuf buffer = Unpooled.copyBoolean(isRunning);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendTo(new BinaryMessageTileSync(center, buffer), player);
    }

    @Override
    public void receiveMessageFromServer(ByteBuf message) { isRunning = message.readBoolean(); }

    public void handleSounds() {
        if (isRunning) { if (soundVolume < 1) soundVolume += 0.02f; }
        else { if (soundVolume > 0) soundVolume -= 0.02f; }
        if (soundVolume == 0) ITSoundHandler.StopSound(soundOrigin);
        else {
            float attenuation = Math.max((float) distanceToTE / 16f, 1);
            ITSounds.gasTurbineArc.PlayRepeating(soundOrigin, soundVolume / attenuation, 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onChunkUnload() { ITSoundHandler.StopSound(soundOrigin); super.onChunkUnload(); }

    @Override
    public void disassemble() {
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundOrigin), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundOrigin.getX(), soundOrigin.getY(), soundOrigin.getZ(), 0));
        super.disassemble();
    }

    private void clientUpdate() {
        if (input0 == null) {
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

    @Override
    public void update() {
        super.update();
        if (world.isRemote) { clientUpdate(); return; }
        serverUpdate();
    }

    public void efficientMarkDirty() { world.getChunk(this.getPos()).markDirty(); }

    @Override
    public void TankContentsChanged() { cachedRecipe = null; this.markContainingBlockForUpdate(null); }

    @Override
    public boolean isDummy() { return false; }

    @Override
    public TileEntityElectrolyticCrucibleBatteryMaster master() { master = this; return this; }

    private void InitializePoIs() {
        int energyIndex = 0, outputIndex = 0;
        for (PoIJSONSchema poi : TileEntityITMultiblockPartElectrolyticCrucibleBattery.instance.pointsOfInterest) {
            switch (poi.name) {
                case "redstone": redstone = new PoICache(facing, poi, mirrored); break;
                case "sound": soundOrigin = getBlockPosForPos(poi.position); break;
                case "input0": input0 = new PoICache(facing, poi, mirrored); break;
                default:
                    if (poi.name.startsWith("energy")) {
                        energyInputs[energyIndex] = new PoICache(facing, poi, mirrored);
                        energyIndex++;
                    } else if (poi.name.startsWith("output")) {
                        outputs[outputIndex] = new PoICache(facing, poi, mirrored);
                        outputFronts[outputIndex] = getBlockPosForPos(outputs[outputIndex].position).offset(outputs[outputIndex].facing);
                        outputIndex++;
                    }
                    break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(redstone.position));
        notifyNeighbor(getBlockPosForPos(input0.position));
        for (PoICache p : energyInputs) notifyNeighbor(getBlockPosForPos(p.position));
        for (PoICache p : outputs) notifyNeighbor(getBlockPosForPos(p.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override
    public @Nonnull int[] getRedstonePos() {
        if (redstone == null) InitializePoIs();
        return new int[]{redstone.position};
    }

    @Override
    public @Nonnull int[] getEnergyPos() {
        if (energyInputs[0] == null) InitializePoIs();
        return new int[]{energyInputs[0].position, energyInputs[1].position, energyInputs[2].position};
    }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, int position) {
        if (energyInputs[0] == null) InitializePoIs();
        if (facing == null) return false;
        for (PoICache p : energyInputs) if (p.isPoI(facing, position)) return true;
        return false;
    }

    @Override
    public void onProcessFinish(@Nonnull MultiblockProcess<ElectrolyticCrucibleBatteryRecipe> process) {
        tanks[1].fill(process.recipe.fluidOutput0, true);
        if (process.recipe.fluidOutput1 != null) tanks[2].fill(process.recipe.fluidOutput1, true);
        if (process.recipe.fluidOutput2 != null) tanks[3].fill(process.recipe.fluidOutput2, true);
        if (process.recipe.itemOutput != null && !process.recipe.itemOutput.isEmpty()) {
            TileEntity inventoryTile = world.getTileEntity(outputFronts[3]);
            ItemStack output = Utils.insertStackIntoInventory(inventoryTile, process.recipe.itemOutput.copy(), outputs[3].facing.getOpposite());
            if (output != null && !output.isEmpty()) Utils.dropStackAtPos(world, outputFronts[3], output, outputs[3].facing);
        }
    }

    @Override
    protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if (input0 == null) InitializePoIs();
        if (side == null) return tanks;
        if (input0.isPoI(side, position)) return new FluidTank[]{tanks[0]};
        if (outputs[0].isPoI(side, position)) return new FluidTank[]{tanks[1]};
        if (outputs[1].isPoI(side, position)) return new FluidTank[]{tanks[2]};
        if (outputs[2].isPoI(side, position)) return new FluidTank[]{tanks[3]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override
    protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (input0.isPoI(side, position)) {
            if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
            if (tanks[0].getFluid() == null) return ElectrolyticCrucibleBatteryRecipe.findRecipeFluid(resource.getFluid()) != null;
            return resource.getFluid() == tanks[0].getFluid().getFluid();
        }
        return false;
    }

    @Override
    protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (outputs[0].isPoI(side, position)) return tanks[1].getFluidAmount() > 0;
        if (outputs[1].isPoI(side, position)) return tanks[2].getFluidAmount() > 0;
        if (outputs[2].isPoI(side, position)) return tanks[3].getFluidAmount() > 0;
        return false;
    }

    private void pumpOutputOut() {
        if (input0 == null) InitializePoIs();
        IFluidHandler output;
        for (int i = 0; i < 3; i++) {
            if (tanks[i + 1].getFluidAmount() > 0 && (output = FluidUtil.getFluidHandler(world, outputFronts[i], outputs[i].facing.getOpposite())) != null) {
                FluidStack out = tanks[i + 1].getFluid();
                int accepted = output.fill(out, false);
                if (accepted == 0) continue;
                assert out != null;
                int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                this.tanks[i + 1].drain(drained, true);
            }
        }
    }
}
