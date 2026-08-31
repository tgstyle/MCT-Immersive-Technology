package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import com.immersiveconvergence.ImmersiveConvergence;
import com.immersiveconvergence.api.capability.IHeatConsumer;
import com.immersiveconvergence.api.client.ICSoundHandler;
import com.immersiveconvergence.api.multiblock.PoICache;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.network.BinaryMessageTileSync;
import com.immersiveconvergence.api.network.IBinaryMessageReceiver;
import com.immersiveconvergence.api.network.MessageStopSound;
import com.immersiveconvergence.api.particles.ParticleColoredSmoke;
import com.immersiveconvergence.api.particles.ParticleFlameCustom;
import com.immersiveconvergence.api.util.ICFluidTank;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.crafting.BoilerLiquidRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartBoilerLiquid;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
import mctmods.immersivetechnology.common.util.compat.advancedrocketry.AdvancedRocketryHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class TileEntityBoilerLiquidMaster extends TileEntityBoilerLiquidSlave implements ICFluidTank.TankListener, IComparatorOverride, IIEInventory, IBinaryMessageReceiver {

    private static int fuelTankSize() { return Multiblocks.boilerLiquid.boilerLiquid_fuel_tankSize; }
    private static double heatLossPerTick() { return Multiblocks.boilerLiquid.boilerLiquid_heat_lossPerTick; }
    private static double pilotHeat() { return Multiblocks.boilerLiquid.boilerLiquid_heat_pilot; }
    private static double defaultWorkingHeatLevel() { return Multiblocks.boilerHeat.boiler_heat_workingLevel; }

    public FluidTank[] tanks = new FluidTank[] { new ICFluidTank(fuelTankSize(), this) };

    public static int slotCount = 2;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);

    public double heatLevel = 0;
    public double targetHeat = defaultWorkingHeatLevel();
    public double workingHeatLevel = defaultWorkingHeatLevel();
    public boolean pilotLit = false;
    public boolean isRunning = false;

    private float soundVolume = 0f;
    private boolean wasPilotOnly = false;
    public BoilerLiquidRecipe cachedFuelRecipe;
    private int oldComparatorOutput = 0;
    private int tickCountdown = 5;

    private boolean needsPoIInit = false;
    private boolean needsNotify = false;

    protected PoICache fluidInputPos0, heatOutputPos0, redstonePos0;
    private BlockPos heatConsumerTEPos0, soundPos0, exhaustPos0;
    private List<BlockPos> ignitionPositions;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        heatLevel = nbt.getDouble("heatLevel");
        targetHeat = nbt.getDouble("targetHeat");
        if (nbt.hasKey("workingHeatLevel")) { workingHeatLevel = nbt.getDouble("workingHeatLevel"); }
        pilotLit = nbt.getBoolean("pilotLit");
        oldComparatorOutput = nbt.getInteger("oldComparatorOutput");
        if (!descPacket) { inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount); }
        if (formed && !descPacket) {
            needsPoIInit = true;
            needsNotify = true;
        }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setDouble("heatLevel", heatLevel);
        nbt.setDouble("targetHeat", targetHeat);
        nbt.setDouble("workingHeatLevel", workingHeatLevel);
        nbt.setBoolean("pilotLit", pilotLit);
        nbt.setInteger("oldComparatorOutput", oldComparatorOutput);
        if (!descPacket) { nbt.setTag("inventory", Utils.writeInventory(inventory)); }
    }

    void InitializePoIs() {
        List<BlockPos> ignition = new ArrayList<>();
        for (PoIJSONSchema poi : TileEntityITMultiblockPartBoilerLiquid.instance.pointsOfInterest) {
            switch (poi.name) {
                case "fluid_input0":
                    fluidInputPos0 = new PoICache(facing, poi, mirrored);
                    break;
                case "heat_output0":
                    heatOutputPos0 = new PoICache(facing, poi, mirrored);
                    heatConsumerTEPos0 = getBlockPosForPos(heatOutputPos0.position).offset(heatOutputPos0.facing);
                    break;
                case "redstone0":
                    redstonePos0 = new PoICache(facing, poi, mirrored);
                    break;
                case "sound0":
                    soundPos0 = getBlockPosForPos(poi.position);
                    break;
                case "exhaust0":
                    exhaustPos0 = getBlockPosForPos(poi.position);
                    break;
                case "ignition0":
                    ignition.add(poi.position);
                    break;
            }
        }
        ignitionPositions = ignition;
    }

    public boolean isHeatOutputPoI(BlockPos position) {
        if (heatOutputPos0 == null) InitializePoIs();
        return heatOutputPos0.position.equals(position);
    }

    public void applyLegacyBoiler(NBTTagCompound legacyNbt, double scaledHeat) {
        tanks[0].readFromNBT(legacyNbt.getCompoundTag("tank0"));
        heatLevel = scaledHeat;
        pilotLit = legacyNbt.getInteger("fuelBurnRemaining") > 0 || scaledHeat > 0;
        redstoneControlInverted = legacyNbt.getBoolean("redstoneControlInverted");
        NonNullList<ItemStack> legacyInventory = Utils.readInventory(legacyNbt.getTagList("inventory", 10), 6);
        inventory.set(0, legacyInventory.get(0));
        inventory.set(1, legacyInventory.get(1));
    }

    public boolean tryIgnite(BlockPos posInMultiblock, EntityPlayer player, ItemStack heldItem) {
        if (!formed) return false;
        if (ignitionPositions == null) InitializePoIs();
        if (!ignitionPositions.contains(posInMultiblock)) return false;
        boolean torch = Block.getBlockFromItem(heldItem.getItem()) == Blocks.TORCH;
        boolean flintAndSteel = heldItem.getItem() == Items.FLINT_AND_STEEL;
        if (!torch && !flintAndSteel) return false;
        if (pilotLit) return true;
        if (tanks[0].getFluidAmount() <= 0 || BoilerLiquidRecipe.findFuel(tanks[0].getFluid()) == null) return true;
        if (!world.isRemote) {
            pilotLit = true;
            heatLevel = Math.max(heatLevel, pilotHeat());
            world.playSound(null, getPos(), ITSounds.gasIgnite, SoundCategory.BLOCKS, 0.5f, 1.0f);
            if (torch) { heldItem.shrink(1); }
            else { heldItem.damageItem(1, player); }
            notifyNearbyClients();
            markContainingBlockForUpdate(null);
            world.markChunkDirty(getPos(), this);
        }
        return true;
    }

    private void notifyIONeighbors() {
        if (fluidInputPos0 != null) world.notifyNeighborsOfStateChange(getBlockPosForPos(fluidInputPos0.position), getBlockType(), true);
        if (redstonePos0 != null) world.updateComparatorOutputLevel(getBlockPosForPos(redstonePos0.position), getBlockType());
    }

    private void notifyNearbyClients() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeDouble(heatLevel);
        buf.writeDouble(workingHeatLevel);
        buf.writeBoolean(pilotLit);
        buf.writeBoolean(isRunning);
        BinaryMessageTileSync.sendToAllTracking(world, getPos(), buf);
    }

    @Override public void receiveMessageFromServer(ByteBuf message) {
        heatLevel = message.readDouble();
        workingHeatLevel = message.readDouble();
        pilotLit = message.readBoolean();
        isRunning = message.readBoolean();
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {}

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (soundPos0 == null) InitializePoIs();
        boolean pilotOnly = pilotLit && heatLevel <= pilotHeat();
        float targetSoundLevel = heatLevel > 0 ? (float)((pilotOnly ? pilotHeat() : heatLevel) / workingHeatLevel) : 0f;
        if (soundVolume < targetSoundLevel) { soundVolume = Math.min(soundVolume + 0.01f, targetSoundLevel); }
        else if (soundVolume > targetSoundLevel) { soundVolume = Math.max(soundVolume - 0.01f, targetSoundLevel); }
        if (pilotOnly != wasPilotOnly) {
            ICSoundHandler.stopSound(soundPos0);
            wasPilotOnly = pilotOnly;
        }
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        float attenuation = Math.max((float)player.getDistanceSq(soundPos0.getX() + 0.5, soundPos0.getY() + 0.5, soundPos0.getZ() + 0.5) / 8, 1);
        float volume = (2 * soundVolume) / attenuation;
        if (soundVolume <= 0f || volume <= 0.01f) { ICSoundHandler.stopSound(soundPos0); }
        else if (pilotOnly) { ITSounds.pilot.PlayRepeating(soundPos0, volume, soundVolume); }
        else { ITSounds.boilerLiquid.PlayRepeating(soundPos0, volume, soundVolume); }
    }

    @SideOnly(Side.CLIENT)
    public void spawnParticles() {
        if (exhaustPos0 == null) InitializePoIs();
        Random rand = new Random();
        int lessParticleSetting = Minecraft.getMinecraft().gameSettings.particleSetting;
        if (lessParticleSetting == 2 || (lessParticleSetting == 1 && rand.nextInt(3) == 0)) return;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distanceLimit = 64;
        if (exhaustPos0.distanceSq(player.posX, player.posY, player.posZ) > distanceLimit * distanceLimit) return;
        if (pilotLit) {
            Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleFlameCustom(world,
                    exhaustPos0.getX() + 0.5, exhaustPos0.getY() + 0.1, exhaustPos0.getZ() + 0.5,
                    rand.nextFloat() * 0.0625f - 0.03125f, 0.0625f, rand.nextFloat() * 0.0625f - 0.03125f));
        }
        if (pilotLit && heatLevel > pilotHeat()) {
            ParticleColoredSmoke cloud = new ParticleColoredSmoke(world,
                    exhaustPos0.getX() + 0.5,
                    exhaustPos0.getY() + 1.25,
                    exhaustPos0.getZ() + 0.5,
                    0, 0.125, 0, ITConfig.client.particles.colored_smoke_height);
            cloud.setRBGColorF(0.2f, 0.2f, 0.2f);
            Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        if (soundPos0 != null) ICSoundHandler.stopSound(soundPos0);
        if (exhaustPos0 != null) ICSoundHandler.stopSound(exhaustPos0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        if (soundPos0 == null) InitializePoIs();
        if (soundPos0 != null) {
            ImmersiveConvergence.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
        }
        if (exhaustPos0 != null) {
            ImmersiveConvergence.packetHandler.sendToAllTracking(new MessageStopSound(exhaustPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), exhaustPos0.getX(), exhaustPos0.getY(), exhaustPos0.getZ(), 0));
        }
        if (!world.isRemote) {
            for (ItemStack stack : inventory) if (!stack.isEmpty()) Utils.dropStackAtPos(world, getPos(), stack);
            inventory.clear();
        }
        super.disassemble();
    }

    @Override public void update() {
        super.update();
        if (!formed) return;
        if (needsPoIInit || fluidInputPos0 == null || heatOutputPos0 == null || redstonePos0 == null || soundPos0 == null || exhaustPos0 == null || ignitionPositions == null) {
            InitializePoIs();
            needsPoIInit = false;
        }
        if (needsNotify) {
            notifyIONeighbors();
            needsNotify = false;
        }
        if (world.isRemote) {
            handleSounds();
            spawnParticles();
            return;
        }
        boolean changed = heatLogic();
        if (fuelTankLogic()) changed = true;

        boolean wasRunning = isRunning;
        isRunning = pilotLit && heatLevel >= workingHeatLevel && isFullMode();

        if (changed || isRunning != wasRunning) {
            if (tickCountdown-- <= 0) {
                notifyNearbyClients();
                tickCountdown = 5;
            }
            world.markChunkDirty(getPos(), this);
        }
        int comp = getComparatorInputOverride();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            if (redstonePos0 != null) world.updateComparatorOutputLevel(getBlockPosForPos(redstonePos0.position), getBlockType());
        }
        if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
        else if (changed) { throttledBlockUpdate(); }
    }

    private boolean hasWater() {
        TileEntity te = world.getTileEntity(heatConsumerTEPos0);
        return te instanceof IHeatConsumer && ((IHeatConsumer)te).getFluidAmount() > 0;
    }

    private boolean isFullMode() { return !isRSDisabled() && hasWater(); }

    private boolean heatLogic() {
        double previousHeat = heatLevel;
        boolean previousPilot = pilotLit;
        double previousWorking = workingHeatLevel;
        boolean canCombust = true;
        if (ITCompatModule.isAdvancedRocketryLoaded) { canCombust = AdvancedRocketryHelper.isAtmosphereSuitableForCombustion(world, exhaustPos0); }
        if (tanks[0].getFluidAmount() <= 0) { pilotLit = false; }
        if (!pilotLit || !canCombust) {
            heatLevel = Math.max(heatLevel - heatLossPerTick(), 0);
            workingHeatLevel = defaultWorkingHeatLevel();
        }
        else {
            cachedFuelRecipe = (cachedFuelRecipe != null && Objects.requireNonNull(tanks[0].getFluid()).isFluidEqual(cachedFuelRecipe.fluidInput)) ? cachedFuelRecipe : BoilerLiquidRecipe.findFuel(tanks[0].getFluid());
            if (cachedFuelRecipe == null) {
                pilotLit = false;
                heatLevel = Math.max(heatLevel - heatLossPerTick(), 0);
                workingHeatLevel = defaultWorkingHeatLevel();
            }
            else {
                targetHeat = cachedFuelRecipe.targetHeat;
                workingHeatLevel = targetHeat;
                if (isFullMode()) {
                    int drainAmount = cachedFuelRecipe.fluidInput.amount;
                    FluidStack drained = tanks[0].drain(drainAmount, true);
                    if (drained != null && drained.amount == drainAmount) {
                        if (heatLevel < targetHeat) { heatLevel = Math.min(heatLevel + cachedFuelRecipe.heatPerTick, targetHeat); }
                        else { heatLevel = Math.max(heatLevel - heatLossPerTick(), targetHeat); }
                    }
                    else { pilotBurn(); }
                }
                else { pilotBurn(); }
            }
        }
        return previousHeat != heatLevel || previousPilot != pilotLit || previousWorking != workingHeatLevel;
    }

    private void pilotBurn() {
        FluidStack drained = tanks[0].drain(1, true);
        if (drained != null && drained.amount >= 1) { heatLevel = Math.max(heatLevel - heatLossPerTick(), pilotHeat()); }
        else {
            pilotLit = false;
            heatLevel = Math.max(heatLevel - heatLossPerTick(), 0);
        }
    }

    private boolean fuelTankLogic() {
        int prev = tanks[0].getFluidAmount();
        ItemStack empty = Utils.drainFluidContainer(tanks[0], inventory.get(0), inventory.get(1), null);
        if (prev != tanks[0].getFluidAmount()) {
            if (!inventory.get(1).isEmpty() && OreDictionary.itemMatches(inventory.get(1), empty, true)) inventory.get(1).grow(empty.getCount());
            else if (inventory.get(1).isEmpty()) inventory.set(1, empty.copy());
            inventory.get(0).shrink(1);
            if (inventory.get(0).getCount() <= 0) inventory.set(0, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            if (fluidInputPos0 == null) InitializePoIs();
            if (fluidInputPos0.isPoI(facing, posInMultiblock())) return true;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            if (fluidInputPos0.isPoI(facing, posInMultiblock())) { return (T)new BoilerLiquidFluidHandler(this, facing, posInMultiblock()); }
        }
        return super.getCapability(capability, facing);
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityBoilerLiquidMaster master() { return this; }

    @Override public TileEntity getGuiMaster() { return this; }

    @Override public void TankContentsChanged() {
        if (tanks[0].getFluidAmount() == 0) { cachedFuelRecipe = null; }
        markContainingBlockForUpdate(null);
    }

    @Override public int getComparatorInputOverride() { return workingHeatLevel > 0 ? (int)Math.min(15, 15 * (heatLevel / workingHeatLevel)) : 0; }

    @Override @Nonnull public NonNullList<ItemStack> getInventory() { return inventory; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) { markDirty(); markContainingBlockForUpdate(null); }

    @Override @Nonnull public NonNullList<ItemStack> getDroppedItems() { return inventory; }

    @Override @Nonnull public net.minecraftforge.fluids.IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, BlockPos position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (fluidInputPos0 == null) InitializePoIs();
        if (side == null) return tanks;
        if (fluidInputPos0.isPoI(side, position)) return new net.minecraftforge.fluids.IFluidTank[]{tanks[0]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        if (fluidInputPos0 == null) InitializePoIs();
        if (iTank == 0 && fluidInputPos0.isPoI(side, position)) {
            if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
            if (tanks[0].getFluid() == null) { return true; }
            return resource.isFluidEqual(tanks[0].getFluid());
        }
        return false;
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

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstonePos0 == null) InitializePoIs();
        return new int[]{toFlatIndex(redstonePos0.position)};
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }

    public static class BoilerLiquidFluidHandler implements IFluidHandler {
        private final TileEntityBoilerLiquidMaster master;
        private final EnumFacing side;
        private final BlockPos position;

        public BoilerLiquidFluidHandler(TileEntityBoilerLiquidMaster master, EnumFacing side, BlockPos position) {
            this.master = master;
            this.side = side;
            this.position = position;
        }

        @Override public IFluidTankProperties[] getTankProperties() {
            return new IFluidTankProperties[]{new FluidTankProperties(master.tanks[0].getFluid(), master.tanks[0].getCapacity(), true, false)};
        }

        @Override public int fill(FluidStack resource, boolean doFill) {
            if (resource == null) return 0;
            if (!master.canFillTankFrom(0, side, resource, position)) return 0;
            int filled = master.tanks[0].fill(resource, doFill);
            if (doFill && filled > 0) master.TankContentsChanged();
            return filled;
        }

        @Override public FluidStack drain(FluidStack resource, boolean doDrain) { return null; }

        @Override public FluidStack drain(int maxDrain, boolean doDrain) { return null; }
    }
}
