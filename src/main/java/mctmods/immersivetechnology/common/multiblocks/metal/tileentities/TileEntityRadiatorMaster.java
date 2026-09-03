package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;

import com.immersiveconvergence.ImmersiveConvergence;
import com.immersiveconvergence.api.client.ICSoundHandler;
import com.immersiveconvergence.api.multiblock.PoICache;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.multiblock.TemplateMultiblock;
import com.immersiveconvergence.api.network.BinaryTileSyncMessage;
import com.immersiveconvergence.api.network.IBinaryMessageReceiver;
import com.immersiveconvergence.api.network.MessageStopSound;
import com.immersiveconvergence.api.util.ICFluidTank;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.api.crafting.RadiatorRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
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

public class TileEntityRadiatorMaster extends TileEntityRadiatorSlave implements ICFluidTank.TankListener, IBinaryMessageReceiver {

    protected long onlyLocalDissassembly = -1;

    private static int inputTankSize() { return Multiblocks.radiator.radiator_input_tankSize; }
    private static int outputTankSize() { return Multiblocks.radiator.radiator_output_tankSize; }
    private static float speedMult() { return Multiblocks.radiator.radiator_speed_multiplier; }

    public FluidTank[] tanks = new FluidTank[] {
            new ICFluidTank(inputTankSize(), this),
            new ICFluidTank(outputTankSize(), this)
    };

    public int processTimeRemaining = 0;
    public int processTimeTotal = 0;

    private RadiatorRecipe cachedRadiatorRecipe;
    private float soundVolume = 0f;
    private int soundGracePeriod = 0;
    private boolean isRunning = false;
    private double radiationEfficiency = 0;
    private int clientUpdateCooldown = 20;
    private double distanceSqToTE;
    private int playerDimension;
    public boolean redstoneControlInverted = false;
    private boolean needsPoIInit = true;
    private boolean needsNotify = false;

    protected PoICache fluidInputPos0, fluidOutputPos0, redstonePos0;
    private BlockPos soundPos0, fluidOutputTEPos0;

    public void efficientMarkDirty() {
        world.getChunk(getPos()).markDirty();
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        processTimeRemaining = nbt.getInteger("processTimeRemaining");
        processTimeTotal = nbt.getInteger("processTimeTotal");
        radiationEfficiency = nbt.getDouble("radiationEfficiency");
        redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
        isRunning = nbt.getBoolean("isRunning");
        soundGracePeriod = nbt.getInteger("soundGracePeriod");
        if (!descPacket) {
            if (nbt.hasKey("cachedRecipe")) cachedRadiatorRecipe = RadiatorRecipe.loadFromNBT(nbt.getCompoundTag("cachedRecipe"));
            else if (processTimeRemaining > 0 && tanks[0].getFluid() != null && tanks[0].getFluidAmount() > 0) {
                cachedRadiatorRecipe = RadiatorRecipe.findRecipe(tanks[0].getFluid());
            }
            if (cachedRadiatorRecipe == null && processTimeRemaining > 0) processTimeRemaining = 0;
        }
        if (!descPacket && formed) {
            needsPoIInit = true;
            needsNotify = true;
        }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setInteger("processTimeRemaining", processTimeRemaining);
        nbt.setInteger("processTimeTotal", processTimeTotal);
        nbt.setDouble("radiationEfficiency", radiationEfficiency);
        nbt.setBoolean("redstoneControlInverted", redstoneControlInverted);
        nbt.setBoolean("isRunning", isRunning);
        nbt.setInteger("soundGracePeriod", soundGracePeriod);
        if (!descPacket && cachedRadiatorRecipe != null) nbt.setTag("cachedRecipe", cachedRadiatorRecipe.writeToNBT(new NBTTagCompound()));
    }

    @Override public void update() {
        if (!formed) return;
        if (needsPoIInit || fluidInputPos0 == null || fluidOutputPos0 == null || redstonePos0 == null || soundPos0 == null) {
            InitializePoIs();
            needsPoIInit = false;
        }
        if (needsNotify) {
            notifyIONeighbors();
            needsNotify = false;
        }
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
            if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
            else { throttledBlockUpdate(); }
        }
    }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartRadiator.instance.pointsOfInterest) {
            switch (poi.name) {
                case "fluid_input0":
                    fluidInputPos0 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_output0":
                    fluidOutputPos0 = new PoICache(facing, poi, mirrored);
                    fluidOutputTEPos0 = getBlockPosForPos(fluidOutputPos0.position).offset(fluidOutputPos0.facing);
                    break;
                case "redstone0":
                    redstonePos0 = new PoICache(facing, poi, mirrored);
                    break;
                case "sound0":
                    soundPos0 = getBlockPosForPos(poi.position);
                    break;
            }
        }
    }

    private void notifyIONeighbors() {
        BlockPos pos;
        pos = getBlockPosForPos(fluidInputPos0.position);
        world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), true);
        pos = getBlockPosForPos(fluidOutputPos0.position);
        world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), true);
        pos = getBlockPosForPos(redstonePos0.position);
        world.updateComparatorOutputLevel(pos, world.getBlockState(pos).getBlock());
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        float targetSoundLevel = isRunning ? 1f : 0f;
        if (soundVolume < targetSoundLevel) { soundVolume = Math.min(soundVolume + 0.01f, targetSoundLevel); }
        else if (soundVolume > targetSoundLevel) { soundVolume = Math.max(soundVolume - 0.01f, targetSoundLevel); }
        if (soundVolume == 0) ICSoundHandler.stopSound(soundPos0);
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos0.getX() + .5, soundPos0.getY() + .5, soundPos0.getZ() + .5) / 8, 1);
            ITSounds.solarTower.PlayRepeating(soundPos0, (2 * soundVolume) / attenuation, soundVolume);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        if (soundPos0 != null) ICSoundHandler.stopSound(soundPos0);
        super.onChunkUnload();
    }

    public void disassemble() {
        disassemble(getPos());
    }

    public void disassemble(@Nullable BlockPos triggerPos) {
        if (world.isRemote) return;

        if (triggerPos == null) triggerPos = getPos();

        long time = world.getTotalWorldTime();
        if (time == onlyLocalDissassembly) return;
        onlyLocalDissassembly = time;

        if (soundPos0 != null) {
            ImmersiveConvergence.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
            soundPos0 = null;
        }

        tanks[0].setFluid(null);
        tanks[1].setFluid(null);
        processTimeRemaining = 0;
        processTimeTotal = 0;
        cachedRadiatorRecipe = null;
        radiationEfficiency = 0;
        isRunning = false;

        revertAllPositions(time, triggerPos);

        efficientMarkDirty();
    }

    private void revertAllPositions(long time, BlockPos triggerPos) {
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

        List<BlockPos> positions = new ArrayList<>();
        List<IBlockState> states = new ArrayList<>();
        List<ItemStack> drops = new ArrayList<>();

        for (int eff_h = 0; eff_h < eff_height; eff_h++) {
            for (int l = 0; l < TileEntityITMultiblockPartRadiator.instance.length; l++) {
                for (int eff_w = 0; eff_w < eff_width; eff_w++) {
                    BlockPos pos2 = TemplateMultiblock.localToWorld(
                            origin,
                            mirrored ? -eff_w : eff_w,
                            eff_h,
                            l,
                            facing,
                            mirrored
                    );

                    TileEntity te = world.getTileEntity(pos2);
                    if (te instanceof TileEntityRadiatorSlave) {
                        TileEntityRadiatorSlave part = (TileEntityRadiatorSlave) te;
                        if (time != part.onlyLocalDissassembly) {
                            ItemStack originalStack = part.getOriginalBlock();
                            IBlockState originalState = Utils.getStateFromItemStack(originalStack);
                            if (originalState != null) {
                                positions.add(pos2);
                                states.add(originalState);
                                drops.add(originalStack.copy());
                            }
                            part.formed = false;
                            part.onlyLocalDissassembly = time;
                        }
                    }
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
        for (int i = 1; i < 25; i++) {
            if (!world.isAirBlock(pos.offset(dir, i))) return 1.0 / ((25 - i) * (25 - i));
        }
        return 1;
    }

    private boolean pumpOutputOut() {
        if (tanks[1].getFluidAmount() == 0) return false;
        IFluidHandler handler = FluidUtil.getFluidHandler(world, fluidOutputTEPos0, fluidOutputPos0.facing.getOpposite());
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

    private double getTotalRadiationEfficiency(int temp) {
        if (world.provider.isNether()) return 0;
        double eff = ITCompatModule.isAdvancedRocketryLoaded ? AdvancedRocketryHelper.getRadiatorHeatTransferCoefficient(world, getPos(), temp, radiationEfficiency) : radiationEfficiency;
        double tempFactor = Multiblocks.radiator.radiator_biome_temp_factor;
        if (tempFactor > 0) { eff *= 1.0 - (world.getBiome(getPos()).getDefaultTemperature() - 0.8) * tempFactor; }
        double humidityFactor = Multiblocks.radiator.radiator_biome_humidity_factor;
        if (humidityFactor > 0) { eff += 0.075 * humidityFactor * -((world.getBiome(getPos()).getRainfall() - 0.5) / 0.5); }
        return Math.max(eff, 0);
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
                RadiatorRecipe recipe = cachedRadiatorRecipe;
                if (recipe == null || !input.isFluidEqual(recipe.fluidInput)) { recipe = RadiatorRecipe.findRecipe(input); }
                if (recipe != null) {
                    int[] amounts = getProcessedFluidAmounts(recipe);
                    boolean inputOk = amounts[0] <= input.amount;
                    boolean outputOk = true;
                    if (recipe.fluidOutput != null) {
                        FluidStack sim = recipe.fluidOutput.copy();
                        sim.amount = amounts[1];
                        outputOk = tanks[1].fillInternal(sim, false) == sim.amount;
                    }
                    if (inputOk && outputOk) {
                        cachedRadiatorRecipe = recipe;
                        processTimeRemaining = (int)(recipe.getTotalProcessTime() / speedMult());
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

    @Override public void TankContentsChanged() {
        if (processTimeRemaining == 0) { cachedRadiatorRecipe = null; }
        efficientMarkDirty();
        markContainingBlockForUpdate(null);
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

    @Override public int getComparatorInputOverride() {
        if (!formed || processTimeTotal <= 0) return 0;
        return 15 * (processTimeTotal - processTimeRemaining) / processTimeTotal;
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityRadiatorMaster master() { return this; }

    public double getRadiationEfficiency() { return radiationEfficiency; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, BlockPos position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (redstonePos0 == null) InitializePoIs();
        if (side == null) return tanks;
        if (fluidInputPos0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        if (fluidOutputPos0.isPoI(side, position)) return new IFluidTank[] {tanks[1]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        if (!formed || redstonePos0 == null) InitializePoIs();
        if (!fluidInputPos0.isPoI(side, position)) return false;
        if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
        FluidStack current = tanks[0].getFluid();
        if (current == null) { return true; }
        return resource.isFluidEqual(current);
    }

    @Override protected boolean isInputFluidPoI(BlockPos position) {
        if (fluidInputPos0 == null) { InitializePoIs(); }
        return fluidInputPos0.position.equals(position);
    }

    @Override protected int clearInputTanks() {
        tanks[0].drain(Integer.MAX_VALUE, true);
        TankContentsChanged();
        return 1;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        if (!formed || redstonePos0 == null) InitializePoIs();
        return fluidOutputPos0.isPoI(side, position) && tanks[1].getFluidAmount() > 0;
    }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstonePos0 == null) InitializePoIs();
        return new int[]{toFlatIndex(redstonePos0.position)};
    }

    static class RadiatorFluidHandler implements IFluidHandler {
        private final IFluidTank[] tanks;
        private final TileEntityRadiatorMaster master;
        private final EnumFacing side;
        private final BlockPos position;

        RadiatorFluidHandler(IFluidTank[] accessibleTanks, TileEntityRadiatorMaster master, EnumFacing side, BlockPos position) {
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
                        if (remaining <= 0) return drained;
                    }
                }
            }
            return drained;
        }
    }
}
