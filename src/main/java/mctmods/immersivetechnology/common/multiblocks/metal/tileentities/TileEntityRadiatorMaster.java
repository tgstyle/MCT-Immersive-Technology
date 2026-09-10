package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.ImmersiveConvergence;
import com.immersiveconvergence.api.client.ICSoundHandler;
import com.immersiveconvergence.api.multiblock.QueueProcessor;
import com.immersiveconvergence.api.multiblock.TemplateMultiblock;
import com.immersiveconvergence.api.network.BinaryTileSyncMessage;
import com.immersiveconvergence.api.network.IBinaryMessageReceiver;
import com.immersiveconvergence.api.network.MessageStopSound;
import com.immersiveconvergence.api.util.ICFluidTank;
import com.immersiveconvergence.api.util.ICUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.api.crafting.RadiatorRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.process.RadiatorProcess;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartRadiator;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
import mctmods.immersivetechnology.common.util.compat.advancedrocketry.AdvancedRocketryHelper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TileEntityRadiatorMaster extends TileEntityRadiatorSlave implements ICFluidTank.TankListener, IBinaryMessageReceiver {
    private static int inputTankSize() { return Multiblocks.radiator.radiator_input_tankSize; }

    private static int outputTankSize() { return Multiblocks.radiator.radiator_output_tankSize; }

    private static float speedMult() { return Multiblocks.radiator.radiator_speed_multiplier; }

    public FluidTank[] tanks = new FluidTank[] {
            new ICFluidTank(inputTankSize(), this),
            new ICFluidTank(outputTankSize(), this)
    };

    public final List<RadiatorProcess> processQueue = new ArrayList<>();

    private static final int MAX_PROCESSES = 2;

    private int oldComparatorOutput = -1;
    private RadiatorRecipe cachedRadiatorRecipe;
    private float soundVolume = 0f;
    private int soundGracePeriod = 0;
    private boolean isRunning = false;
    private double radiationEfficiency = 0;
    private int clientUpdateCooldown = 20;
    private double distanceSqToTE;
    private int playerDimension;
    public boolean redstoneControlInverted = false;

    public void efficientMarkDirty() {
        world.getChunk(getPos()).markDirty();
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        processQueue.clear();
        NBTTagList queue = nbt.getTagList("processQueue", 10);
        for (int i = 0; i < queue.tagCount(); i++) {
            RadiatorProcess process = RadiatorProcess.readFromNBT(queue.getCompoundTagAt(i));
            if (process != null) { processQueue.add(process); }
        }
        radiationEfficiency = nbt.getDouble("radiationEfficiency");
        redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
        isRunning = nbt.getBoolean("isRunning");
        soundGracePeriod = nbt.getInteger("soundGracePeriod");
        if (!descPacket && nbt.hasKey("cachedRecipe")) { cachedRadiatorRecipe = RadiatorRecipe.loadFromNBT(nbt.getCompoundTag("cachedRecipe")); }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        NBTTagList queue = new NBTTagList();
        for (RadiatorProcess process : processQueue) { queue.appendTag(process.writeToNBT()); }
        nbt.setTag("processQueue", queue);
        nbt.setDouble("radiationEfficiency", radiationEfficiency);
        nbt.setBoolean("redstoneControlInverted", redstoneControlInverted);
        nbt.setBoolean("isRunning", isRunning);
        nbt.setInteger("soundGracePeriod", soundGracePeriod);
        if (!descPacket && cachedRadiatorRecipe != null) nbt.setTag("cachedRecipe", cachedRadiatorRecipe.writeToNBT(new NBTTagCompound()));
    }

    @Override public void update() {
        if (!formed) return;
        if (world.isRemote) {
            clientUpdate();
            return;
        }
        super.update();

        boolean update = false;
        double oldEff = radiationEfficiency;
        if (radiationEfficiency == 0 || world.getTotalWorldTime() % 600 == Math.abs(getPos().hashCode()) % 600) checkReflectorEfficiency();
        if (radiationEfficiency != oldEff) update = true;

        update |= recipeLogic();
        if (pumpOutputOut()) update = true;

        boolean wasRunning = isRunning;
        boolean active = !processQueue.isEmpty() && !isRSDisabled();
        if (active) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;
        isRunning = soundGracePeriod > 0;

        if (isRunning != wasRunning) notifyNearbyClients();

        int comp = comparatorValue();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            notifyComparators();
        }

        clientUpdateCooldown--;
        if (clientUpdateCooldown <= 0) {
            notifyNearbyClients();
            clientUpdateCooldown = 20;
        }

        if (update || isRunning != wasRunning) {
            efficientMarkDirty();
            if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
            else { throttledBlockUpdate(); }
        }
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        BlockPos soundPos = poiWorldPos("sound0");
        float targetSoundLevel = isRunning ? 1f : 0f;
        if (soundVolume < targetSoundLevel) { soundVolume = Math.min(soundVolume + 0.01f, targetSoundLevel); }
        else if (soundVolume > targetSoundLevel) { soundVolume = Math.max(soundVolume - 0.01f, targetSoundLevel); }
        if (soundVolume == 0) ICSoundHandler.stopSound(soundPos);
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            double distance = Math.sqrt(player.getDistanceSq(soundPos.getX() + .5, soundPos.getY() + .5, soundPos.getZ() + .5));
            ITSounds.solarTower.PlayRepeating(soundPos, soundVolume * (float)Math.max(1 - distance / 16, 0), 1f);
        }
    }

    public void disassemble() {
        disassemble(getPos());
    }

    public void disassemble(@Nullable BlockPos triggerPos) {
        if (world.isRemote || QueueProcessor.isDisassembling(world, getPos())) { return; }
        if (triggerPos == null) { triggerPos = getPos(); }

        long time = world.getTotalWorldTime();
        if (time == onlyLocalDisassembly) { return; }

        BlockPos soundPos = poiWorldPos("sound0");
        ImmersiveConvergence.packetHandler.sendToAllTracking(new MessageStopSound(soundPos), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos.getX(), soundPos.getY(), soundPos.getZ(), 0));

        tanks[0].setFluid(null);
        tanks[1].setFluid(null);
        processQueue.clear();
        cachedRadiatorRecipe = null;
        radiationEfficiency = 0;
        isRunning = false;

        TileEntity trigger = world.getTileEntity(triggerPos);
        TileEntityRadiatorSlave broken = trigger instanceof TileEntityRadiatorSlave ? (TileEntityRadiatorSlave)trigger : this;
        if (QueueProcessor.handleDisassembly(broken, structurePositions(), broken.shouldDropOriginal) != QueueProcessor.Result.FALLBACK) {
            efficientMarkDirty();
            return;
        }

        onlyLocalDisassembly = time;
        revertAllPositions(time, triggerPos);

        efficientMarkDirty();
    }

    private List<BlockPos> structurePositions() {
        int eff_width = mirrored ? TileEntityITMultiblockPartRadiator.instance.height : TileEntityITMultiblockPartRadiator.instance.width;
        int eff_height = mirrored ? TileEntityITMultiblockPartRadiator.instance.width : TileEntityITMultiblockPartRadiator.instance.height;

        int masterX = TileEntityITMultiblockPartRadiator.instance.masterX;
        int masterY = TileEntityITMultiblockPartRadiator.instance.masterY;
        int masterZ = TileEntityITMultiblockPartRadiator.instance.masterZ;

        int eff_masterX = mirrored ? masterY : masterX;
        int eff_masterY = mirrored ? masterX : masterY;

        BlockPos origin = getPos()
                .offset(facing, -masterZ)
                .offset(facing.rotateY(), -eff_masterX)
                .offset(EnumFacing.DOWN, eff_masterY);

        List<BlockPos> positions = new ArrayList<>(eff_height * eff_width * TileEntityITMultiblockPartRadiator.instance.length);
        for (int eff_h = 0; eff_h < eff_height; eff_h++) {
            for (int l = 0; l < TileEntityITMultiblockPartRadiator.instance.length; l++) {
                for (int eff_w = 0; eff_w < eff_width; eff_w++) {
                    positions.add(TemplateMultiblock.localToWorld(origin, mirrored ? -eff_w : eff_w, eff_h, l, facing, mirrored));
                }
            }
        }
        return positions;
    }

    private void revertAllPositions(long time, BlockPos triggerPos) {
        List<BlockPos> positions = new ArrayList<>();
        List<IBlockState> states = new ArrayList<>();
        List<ItemStack> drops = new ArrayList<>();

        for (BlockPos pos2 : structurePositions()) {
            TileEntity te = world.getTileEntity(pos2);
            if (te instanceof TileEntityRadiatorSlave) {
                TileEntityRadiatorSlave part = (TileEntityRadiatorSlave) te;
                if (time != part.onlyLocalDisassembly) {
                    ItemStack originalStack = part.getOriginalBlock();
                    IBlockState originalState = ICUtils.getStateFromItemStack(originalStack);
                    if (originalState != null) {
                        positions.add(pos2);
                        states.add(originalState);
                        drops.add(originalStack.copy());
                    }
                    part.formed = false;
                    part.onlyLocalDisassembly = time;
                }
            }
        }

        final double maxDistSq = 400;
        EntityPlayer closestPlayer = null;
        double minDistSq = maxDistSq;
        for (EntityPlayer p : world.playerEntities) {
            double dSq = p.getDistanceSq(triggerPos.getX() + 0.5D, triggerPos.getY() + 0.5D, triggerPos.getZ() + 0.5D);
            if (dSq < minDistSq) {
                minDistSq = dSq;
                closestPlayer = p;
            }
        }
        boolean creativeBreak = closestPlayer != null && closestPlayer.capabilities.isCreativeMode;

        for (int i = 0; i < positions.size(); i++) {
            BlockPos pos = positions.get(i);
            IBlockState state = states.get(i);
            ItemStack drop = drops.get(i);

            world.removeTileEntity(pos);

            if (pos.equals(triggerPos)) {
                if (!creativeBreak && !drop.isEmpty()) {
                    float f = 0.7F;
                    double dx = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
                    double dy = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
                    double dz = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
                    EntityItem entityitem = new EntityItem(world, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, drop);
                    entityitem.setPickupDelay(10);
                    world.spawnEntity(entityitem);
                }
                world.playEvent(2001, pos, Block.getStateId(state));
            } else {
                world.setBlockState(pos, state, 3);
            }
        }
    }

    public void requestUpdate() {
        BinaryTileSyncMessage.sendToServer(getPos(), Unpooled.copyBoolean(true));
    }

    public void notifyNearbyClients() {
        BinaryTileSyncMessage.sendToAllTracking(world, getPos(), Unpooled.copyBoolean(isRunning));
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {
        BinaryTileSyncMessage.sendToPlayer(player, getPos(), Unpooled.copyBoolean(isRunning));
    }

    @Override public void receiveMessageFromServer(ByteBuf message) {
        isRunning = message.readBoolean();
    }

    private void checkReflectorEfficiency() {
        double reflectorFactor = Multiblocks.radiator.radiator_reflector_factor;
        if (reflectorFactor <= 0) {
            radiationEfficiency = 1;
            return;
        }
        double raw = mirrored ? checkLineEfficiency(-2) + checkLineEfficiency(2) : checkRowEfficiency(-2) + checkRowEfficiency(2);
        radiationEfficiency = Math.max(1 - (1 - raw) * reflectorFactor, 0);
    }

    private double checkRowEfficiency(int offsetY) {
        double half = 0;
        BlockPos p = getPos().offset(facing, 1).add(0, offsetY, 0);
        half += checkColumnEfficiency(p, facing.rotateY()) / 12.0;
        half += checkColumnEfficiency(p, facing.rotateYCCW()) / 12.0;
        p = getPos().offset(facing, 4).add(0, offsetY, 0);
        half += checkColumnEfficiency(p, facing.rotateY()) / 12.0;
        half += checkColumnEfficiency(p, facing.rotateYCCW()) / 12.0;
        p = getPos().offset(facing, 7).add(0, offsetY, 0);
        half += checkColumnEfficiency(p, facing.rotateY()) / 12.0;
        half += checkColumnEfficiency(p, facing.rotateYCCW()) / 12.0;
        return half;
    }

    private double checkLineEfficiency(int offsetX) {
        double half = 0;
        BlockPos p = getPos().offset(facing, 1).offset(facing.rotateY(), offsetX);
        half += checkColumnEfficiency(p, EnumFacing.DOWN) / 12.0;
        half += checkColumnEfficiency(p, EnumFacing.UP) / 12.0;
        p = getPos().offset(facing, 4).offset(facing.rotateY(), offsetX);
        half += checkColumnEfficiency(p, EnumFacing.DOWN) / 12.0;
        half += checkColumnEfficiency(p, EnumFacing.UP) / 12.0;
        p = getPos().offset(facing, 7).offset(facing.rotateY(), offsetX);
        half += checkColumnEfficiency(p, EnumFacing.DOWN) / 12.0;
        half += checkColumnEfficiency(p, EnumFacing.UP) / 12.0;
        return half;
    }

    private double checkColumnEfficiency(BlockPos pos, EnumFacing dir) {
        for (int i = 1; i < 25; i++) {
            if (!world.isAirBlock(pos.offset(dir, i))) return 1.0 / ((25 - i) * (25 - i));
        }
        return 1;
    }

    private boolean pumpOutputOut() {
        if (tanks[1].getFluidAmount() == 0) return false;
        IFluidHandler handler = FluidUtil.getFluidHandler(world, poiFrontPos("fluid_output0"), poi("fluid_output0").facing.getOpposite());
        if (handler == null) return false;
        FluidStack out = tanks[1].getFluid();
        if (out == null) return false;
        int accepted = handler.fill(out, false);
        if (accepted <= 0) return false;
        int drained = handler.fill(ICUtils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        tanks[1].drain(drained, true);
        return drained > 0;
    }

    private double getTotalRadiationEfficiency(int temp) {
        double tempFactorNether = Multiblocks.radiator.radiator_biome_temp_factor;
        if (tempFactorNether > 0 && world.provider.isNether()) return 0;
        double eff = ITCompatModule.isAdvancedRocketryLoaded ? AdvancedRocketryHelper.getRadiatorHeatTransferCoefficient(world, getPos(), temp, radiationEfficiency) : radiationEfficiency;
        double tempFactor = Multiblocks.radiator.radiator_biome_temp_factor;
        if (tempFactor > 0) { eff *= 1.0 - (world.getBiome(getPos()).getDefaultTemperature() - 0.8) * tempFactor; }
        double humidityFactor = Multiblocks.radiator.radiator_biome_humidity_factor;
        if (humidityFactor > 0) { eff += 0.075 * humidityFactor * -((world.getBiome(getPos()).getRainfall() - 0.5) / 0.5); }
        return Math.max(eff, 0);
    }

    private boolean recipeLogic() {
        boolean update = false;
        FluidStack current = tanks[0].getFluid();
        double speed = current == null ? 0 : speedMult() * getTotalRadiationEfficiency(current.getFluid().getTemperature());
        for (int i = processQueue.size() - 1; i >= 0; i--) {
            RadiatorProcess process = processQueue.get(i);
            process.tick(tanks[0], tanks[1], speed);
            if (process.isComplete()) { processQueue.remove(i); }
            update = true;
        }
        if (!isRSDisabled() && speed > 0 && processQueue.size() < MAX_PROCESSES) {
            FluidStack input = tanks[0].getFluid();
            if (input != null && input.amount > 0) {
                RadiatorRecipe recipe = cachedRadiatorRecipe;
                if (recipe == null || !input.isFluidEqual(recipe.fluidInput)) { recipe = RadiatorRecipe.findRecipe(input); }
                if (recipe != null) {
                    boolean inputOk = recipe.fluidInput.amount <= input.amount;
                    boolean outputOk = true;
                    if (recipe.fluidOutput != null) {
                        outputOk = tanks[1].fillInternal(recipe.fluidOutput.copy(), false) == recipe.fluidOutput.amount;
                    }
                    if (inputOk && outputOk) {
                        cachedRadiatorRecipe = recipe;
                        processQueue.add(new RadiatorProcess(recipe));
                        update = true;
                    }
                }
            }
        }
        return update;
    }

    @SideOnly(Side.CLIENT)
    private void clientUpdate() {
        BlockPos soundPos = poiWorldPos("sound0");
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distSq = player.getDistanceSq(soundPos.getX() + .5, soundPos.getY() + .5, soundPos.getZ() + .5);
        if (world.provider.getDimension() == player.dimension && distSq < 400 && (distanceSqToTE > 400 || playerDimension != player.dimension)) requestUpdate();
        distanceSqToTE = distSq;
        playerDimension = player.dimension;
        handleSounds();
    }

    @Override public void TankContentsChanged() {
        if (processQueue.isEmpty()) { cachedRadiatorRecipe = null; }
        efficientMarkDirty();
        requestClientSync();
    }

    @Override public int getComparatorInputOverride() { return isComparatorPos() ? comparatorValue() : 0; }

    public int comparatorValue() {
        if (!formed || processQueue.isEmpty()) return 0;
        RadiatorProcess process = processQueue.get(0);
        if (process.getTotalProcessTime() <= 0) return 0;
        return 15 * process.getTicksProcessed() / process.getTotalProcessTime();
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return ITUtils.EMPTY_INT_ARRAY; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return ITUtils.EMPTY_INT_ARRAY; }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityRadiatorMaster master() { return this; }

    public double getRadiationEfficiency() { return radiationEfficiency; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, BlockPos position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (side == null) return tanks;
        if (isPoI("fluid_input0", side, position)) return tankView(0, tanks[0]);
        if (isPoI("fluid_output0", side, position)) return tankView(1, tanks[1]);
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        if (!isPoI("fluid_input0", side, position)) return false;
        if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
        FluidStack current = tanks[0].getFluid();
        if (current == null) { return true; }
        return resource.isFluidEqual(current);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        return isPoI("fluid_output0", side, position) && tanks[1].getFluidAmount() > 0;
    }
}
