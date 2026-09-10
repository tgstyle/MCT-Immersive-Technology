package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.capability.IHeatConsumer;
import com.immersiveconvergence.api.client.ICSoundHandler;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.IComparatorOverride;
import com.immersiveconvergence.api.network.BinaryTileSyncMessage;
import com.immersiveconvergence.api.network.IBinaryMessageReceiver;
import com.immersiveconvergence.api.particles.ParticleColoredSmoke;
import com.immersiveconvergence.api.particles.ParticleFlameCustom;
import com.immersiveconvergence.api.util.ICFluidTank;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.util.IICInventory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.api.crafting.BoilerLiquidRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Objects;
import java.util.Random;

public class TileEntityBoilerLiquidMaster extends TileEntityBoilerLiquidSlave implements ICFluidTank.TankListener, IComparatorOverride, IICInventory, IBinaryMessageReceiver {
    private static int fuelTankSize() { return Multiblocks.boilerLiquid.boilerLiquid_fuel_tankSize; }

    private static double heatLossPerTick() { return Multiblocks.boilerLiquid.boilerLiquid_heat_lossPerTick; }

    private static double pilotHeat() { return Multiblocks.boilerLiquid.boilerLiquid_heat_pilot; }

    private static double defaultWorkingHeatLevel() { return Multiblocks.boilerHeat.workingLevel(); }

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

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        heatLevel = nbt.getDouble("heatLevel");
        targetHeat = nbt.getDouble("targetHeat");
        if (nbt.hasKey("workingHeatLevel")) { workingHeatLevel = nbt.getDouble("workingHeatLevel"); }
        pilotLit = nbt.getBoolean("pilotLit");
        oldComparatorOutput = nbt.getInteger("oldComparatorOutput");
        if (!descPacket) { inventory = ICUtils.readInventory(nbt.getTagList("inventory", 10), slotCount); }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setDouble("heatLevel", heatLevel);
        nbt.setDouble("targetHeat", targetHeat);
        nbt.setDouble("workingHeatLevel", workingHeatLevel);
        nbt.setBoolean("pilotLit", pilotLit);
        nbt.setInteger("oldComparatorOutput", oldComparatorOutput);
        if (!descPacket) { nbt.setTag("inventory", ICUtils.writeInventory(inventory)); }
    }

    public boolean isHeatOutputPoI(BlockPos position) {
        return poi("heat_output0").position.equals(position);
    }

    public void applyLegacyBoiler(NBTTagCompound legacyNbt, double scaledHeat) {
        tanks[0].readFromNBT(legacyNbt.getCompoundTag("tank0"));
        heatLevel = scaledHeat;
        pilotLit = legacyNbt.getInteger("fuelBurnRemaining") > 0 || scaledHeat > 0;
        redstoneControlInverted = legacyNbt.getBoolean("redstoneControlInverted");
        NonNullList<ItemStack> legacyInventory = ICUtils.readInventory(legacyNbt.getTagList("inventory", 10), 6);
        inventory.set(0, legacyInventory.get(0));
        inventory.set(1, legacyInventory.get(1));
    }

    public boolean tryIgnite(BlockPos posInMultiblock, EntityPlayer player, ItemStack heldItem) {
        if (!formed) return false;
        if (!isPoIPosition("ignition0", posInMultiblock)) return false;
        boolean torch = Block.getBlockFromItem(heldItem.getItem()) == Blocks.TORCH;
        boolean flintAndSteel = heldItem.getItem() == Items.FLINT_AND_STEEL;
        if (!torch && !flintAndSteel) return false;
        if (pilotLit) return false;
        if (tanks[0].getFluidAmount() <= 0 || BoilerLiquidRecipe.findFuel(tanks[0].getFluid()) == null) return false;
        if (!world.isRemote) {
            pilotLit = true;
            heatLevel = pilotHeat();
            world.playSound(null, getPos(), ITSounds.gasIgnite, SoundCategory.BLOCKS, 0.5f, 1.0f);
            if (torch) { heldItem.shrink(1); }
            else { heldItem.damageItem(1, player); }
            notifyNearbyClients();
            markContainingBlockForUpdate(null);
            world.markChunkDirty(getPos(), this);
        }
        return true;
    }

    private void notifyNearbyClients() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeDouble(heatLevel);
        buf.writeDouble(workingHeatLevel);
        buf.writeBoolean(pilotLit);
        buf.writeBoolean(isRunning);
        BinaryTileSyncMessage.sendToAllTracking(world, getPos(), buf);
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
        BlockPos soundPos = poiWorldPos("sound0");
        boolean pilotOnly = pilotLit && heatLevel <= pilotHeat();
        float targetSoundLevel = heatLevel > 0 ? (float)((pilotOnly ? pilotHeat() : heatLevel) / workingHeatLevel) : 0f;
        if (soundVolume < targetSoundLevel) { soundVolume = Math.min(soundVolume + 0.01f, targetSoundLevel); }
        else if (soundVolume > targetSoundLevel) { soundVolume = Math.max(soundVolume - 0.01f, targetSoundLevel); }
        if (pilotOnly != wasPilotOnly) {
            ICSoundHandler.stopSound(soundPos);
            wasPilotOnly = pilotOnly;
        }
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        float attenuation = Math.max((float)player.getDistanceSq(soundPos.getX() + 0.5, soundPos.getY() + 0.5, soundPos.getZ() + 0.5) / 8, 1);
        float volume = (2 * soundVolume) / attenuation;
        if (soundVolume <= 0f || volume <= 0.01f) { ICSoundHandler.stopSound(soundPos); }
        else if (pilotOnly) { ITSounds.pilot.PlayRepeating(soundPos, volume, soundVolume); }
        else { ITSounds.boilerLiquid.PlayRepeating(soundPos, volume, soundVolume); }
    }

    @SideOnly(Side.CLIENT)
    public void spawnParticles() {
        BlockPos exhaustPos = poiWorldPos("exhaust0");
        Random rand = world.rand;
        int lessParticleSetting = Minecraft.getMinecraft().gameSettings.particleSetting;
        if (lessParticleSetting == 2 || (lessParticleSetting == 1 && rand.nextInt(3) == 0)) return;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distanceLimit = 64;
        if (exhaustPos.distanceSq(player.posX, player.posY, player.posZ) > distanceLimit * distanceLimit) return;
        if (pilotLit) {
            Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleFlameCustom(world,
                    exhaustPos.getX() + 0.5, exhaustPos.getY() + 0.1, exhaustPos.getZ() + 0.5,
                    rand.nextFloat() * 0.0625f - 0.03125f, 0.0625f, rand.nextFloat() * 0.0625f - 0.03125f));
        }
        if (pilotLit && heatLevel > pilotHeat() && !isRSDisabled() && consumerHasWater()) {
            ParticleColoredSmoke cloud = new ParticleColoredSmoke(world,
                    exhaustPos.getX() + 0.5,
                    exhaustPos.getY() + 1.25,
                    exhaustPos.getZ() + 0.5,
                    0, 0.125, 0, ITConfig.Client.particles.colored_smoke_height);
            cloud.setRBGColorF(0.2f, 0.2f, 0.2f);
            Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
        }
    }

    @SideOnly(Side.CLIENT)
    private boolean consumerHasWater() {
        BlockPos heatOutputFront = poiFrontPos("heat_output0");
        TileEntity te = world.getTileEntity(heatOutputFront);
        if (!(te instanceof TileEntityBoilerTankSlave)) { return false; }
        TileEntityBoilerTankMaster tank = ((TileEntityBoilerTankSlave)te).master();
        return tank != null && tank.tanks[0].getFluidAmount() > 0;
    }

    @Override public void disassemble() {
        if (!world.isRemote) {
            for (ItemStack stack : inventory) if (!stack.isEmpty()) ICUtils.dropStackAtPos(world, getPos(), stack);
            inventory.clear();
        }
        super.disassemble();
    }

    @Override public void update() {
        super.update();
        if (!formed) return;
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
        int comp = comparatorValue();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            notifyComparators();
        }
        if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
        else if (changed) { throttledBlockUpdate(); }
    }

    private boolean hasWater() {
        TileEntity te = world.getTileEntity(poiFrontPos("heat_output0"));
        return te instanceof IHeatConsumer && ((IHeatConsumer)te).getFluidAmount() > 0;
    }

    private boolean isFullMode() { return !isRSDisabled() && hasWater(); }

    private boolean heatLogic() {
        double previousHeat = heatLevel;
        boolean previousPilot = pilotLit;
        double previousWorking = workingHeatLevel;
        boolean canCombust = true;
        if (ITCompatModule.isAdvancedRocketryLoaded) { canCombust = AdvancedRocketryHelper.isAtmosphereSuitableForCombustion(world, poiWorldPos("exhaust0")); }
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
                BoilerLiquidRecipe fuel = cachedFuelRecipe;
                targetHeat = fuel.targetHeat;
                workingHeatLevel = targetHeat;
                if (isFullMode()) {
                    int drainAmount = fuel.fluidInput.amount;
                    FluidStack drained = tanks[0].drain(drainAmount, true);
                    if (drained != null && drained.amount == drainAmount) {
                        if (heatLevel < targetHeat) { heatLevel = Math.min(heatLevel + fuel.heatPerTick, targetHeat); }
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
        ItemStack empty = ICUtils.drainFluidContainer(tanks[0], inventory.get(0), inventory.get(1), null);
        if (prev != tanks[0].getFluidAmount()) {
            if (!inventory.get(1).isEmpty() && OreDictionary.itemMatches(inventory.get(1), empty, true)) inventory.get(1).grow(empty.getCount());
            else if (inventory.get(1).isEmpty()) inventory.set(1, empty.copy());
            inventory.get(0).shrink(1);
            if (inventory.get(0).getCount() <= 0) inventory.set(0, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityBoilerLiquidMaster master() { return this; }

    @Override public TileEntity getGuiMaster() { return this; }

    @Override public void TankContentsChanged() {
        if (tanks[0].getFluidAmount() == 0) { cachedFuelRecipe = null; }
        requestClientSync();
    }

    @Override public int getComparatorInputOverride() { return isComparatorPos() ? comparatorValue() : 0; }

    public int comparatorValue() { return workingHeatLevel > 0 ? (int)Math.min(15, 15 * (heatLevel / workingHeatLevel)) : 0; }

    @Override @Nonnull public NonNullList<ItemStack> getInventory() { return inventory; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) { markDirty(); markContainingBlockForUpdate(null); }

    @Override @Nonnull public NonNullList<ItemStack> getDroppedItems() { return inventory; }

    @Override @Nonnull public net.minecraftforge.fluids.IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, BlockPos position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (side == null) return tanks;
        if (isPoI("fluid_input0", side, position)) return new net.minecraftforge.fluids.IFluidTank[]{tanks[0]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        if (iTank == 0 && isPoI("fluid_input0", side, position)) {
            if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
            if (tanks[0].getFluid() == null) { return true; }
            return resource.isFluidEqual(tanks[0].getFluid());
        }
        return false;
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return ITUtils.EMPTY_INT_ARRAY; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return ITUtils.EMPTY_INT_ARRAY; }
}
