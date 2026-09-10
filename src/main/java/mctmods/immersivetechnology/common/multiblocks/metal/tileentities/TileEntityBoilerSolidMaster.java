package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import mctmods.immersivetechnology.common.util.ITUtils;
import com.immersiveconvergence.api.capability.IHeatConsumer;
import com.immersiveconvergence.api.client.ICSoundHandler;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.IComparatorOverride;
import com.immersiveconvergence.api.network.BinaryTileSyncMessage;
import com.immersiveconvergence.api.network.IBinaryMessageReceiver;
import com.immersiveconvergence.api.particles.ParticleColoredSmoke;
import com.immersiveconvergence.api.particles.ParticleFlameCustom;
import com.immersiveconvergence.api.util.ICInventoryHandler;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.util.IICInventory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.api.crafting.BoilerSolidRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.util.ITSounds;
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
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

import java.util.Random;

public class TileEntityBoilerSolidMaster extends TileEntityBoilerSolidSlave implements IComparatorOverride, IICInventory, IBinaryMessageReceiver {
    private static double heatLossPerTick() { return Multiblocks.boilerSolid.boilerSolid_heat_lossPerTick; }

    private static double pilotHeat() { return Multiblocks.boilerSolid.boilerSolid_heat_pilot; }

    private static int pilotMultiplier() { return Multiblocks.boilerSolid.boilerSolid_pilot_fuelMultiplier; }

    private static double defaultHeatPerTick() { return Multiblocks.boilerSolid.boilerSolid_heat_defaultPerTick; }

    private static int burnTimeDivider() { return Multiblocks.boilerSolid.boilerSolid_burnTime_divider; }

    private static double defaultWorkingHeatLevel() { return Multiblocks.boilerHeat.workingLevel(); }

    public static int slotCount = 1;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    final IItemHandler inputHandler = new ICInventoryHandler(1, this, 0, new boolean[]{true}, new boolean[]{false});

    public double heatLevel = 0;
    public double heatPerTick = 0;
    public double targetHeat = defaultWorkingHeatLevel();
    public double workingHeatLevel = defaultWorkingHeatLevel();
    public int burnRemaining = 0;
    public int totalBurnTime = 0;
    public boolean pilotLit = false;
    public boolean isRunning = false;

    private float soundVolume = 0f;
    private int oldComparatorOutput = 0;
    private int tickCountdown = 5;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        heatLevel = nbt.getDouble("heatLevel");
        heatPerTick = nbt.getDouble("heatPerTick");
        targetHeat = nbt.getDouble("targetHeat");
        if (nbt.hasKey("workingHeatLevel")) { workingHeatLevel = nbt.getDouble("workingHeatLevel"); }
        burnRemaining = nbt.getInteger("burnRemaining");
        totalBurnTime = nbt.getInteger("totalBurnTime");
        pilotLit = nbt.getBoolean("pilotLit");
        oldComparatorOutput = nbt.getInteger("oldComparatorOutput");
        if (!descPacket) { inventory = ICUtils.readInventory(nbt.getTagList("inventory", 10), slotCount); }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setDouble("heatLevel", heatLevel);
        nbt.setDouble("heatPerTick", heatPerTick);
        nbt.setDouble("targetHeat", targetHeat);
        nbt.setDouble("workingHeatLevel", workingHeatLevel);
        nbt.setInteger("burnRemaining", burnRemaining);
        nbt.setInteger("totalBurnTime", totalBurnTime);
        nbt.setBoolean("pilotLit", pilotLit);
        nbt.setInteger("oldComparatorOutput", oldComparatorOutput);
        if (!descPacket) { nbt.setTag("inventory", ICUtils.writeInventory(inventory)); }
    }

    public boolean isHeatOutputPoI(BlockPos position) {
        return poi("heat_output0").position.equals(position);
    }

    public boolean tryIgnite(BlockPos posInMultiblock, EntityPlayer player, ItemStack heldItem) {
        if (!formed) return false;
        if (!isPoIPosition("ignition0", posInMultiblock)) return false;
        boolean torch = Block.getBlockFromItem(heldItem.getItem()) == Blocks.TORCH;
        boolean flintAndSteel = heldItem.getItem() == Items.FLINT_AND_STEEL;
        if (!torch && !flintAndSteel) return false;
        if (pilotLit) return false;
        if (findBurnTime(inventory.get(0)) <= 0) return false;
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

    private int findBurnTime(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        ItemStack single = stack.copy();
        single.setCount(1);
        int burnTime = ForgeEventFactory.getItemBurnTime(single);
        if (burnTime <= 0 && BoilerSolidRecipe.findFuel(single) != null) { burnTime = 200; }
        return burnTime;
    }

    private void notifyNearbyClients() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeDouble(heatLevel);
        buf.writeDouble(workingHeatLevel);
        buf.writeInt(burnRemaining);
        buf.writeInt(totalBurnTime);
        buf.writeBoolean(pilotLit);
        buf.writeBoolean(isRunning);
        BinaryTileSyncMessage.sendToAllTracking(world, getPos(), buf);
    }

    @Override public void receiveMessageFromServer(ByteBuf message) {
        heatLevel = message.readDouble();
        workingHeatLevel = message.readDouble();
        burnRemaining = message.readInt();
        totalBurnTime = message.readInt();
        pilotLit = message.readBoolean();
        boolean wasRunning = isRunning;
        isRunning = message.readBoolean();
        if (isRunning != wasRunning) { world.markBlockRangeForRenderUpdate(getPos().add(-4, -4, -4), getPos().add(4, 4, 4)); }
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {}

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        BlockPos soundPos = poiWorldPos("sound0");
        float targetSoundLevel = heatLevel > 0 ? (float)(heatLevel / workingHeatLevel) : 0f;
        if (soundVolume < targetSoundLevel) { soundVolume = Math.min(soundVolume + 0.01f, targetSoundLevel); }
        else if (soundVolume > targetSoundLevel) { soundVolume = Math.max(soundVolume - 0.01f, targetSoundLevel); }
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        float attenuation = Math.max((float)player.getDistanceSq(soundPos.getX() + 0.5, soundPos.getY() + 0.5, soundPos.getZ() + 0.5) / 8, 1);
        float volume = (2 * soundVolume) / attenuation;
        if (soundVolume <= 0f || volume <= 0.01f) { ICSoundHandler.stopSound(soundPos); }
        else { ITSounds.boilerSolid.PlayRepeating(soundPos, volume, soundVolume); }
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
        int previousBurn = burnRemaining;
        boolean canCombust = true;
        if (ITCompatModule.isAdvancedRocketryLoaded) { canCombust = AdvancedRocketryHelper.isAtmosphereSuitableForCombustion(world, poiWorldPos("exhaust0")); }
        boolean fullMode = isFullMode();
        if (!pilotLit || !canCombust) {
            heatLevel = Math.max(heatLevel - heatLossPerTick(), 0);
            burnRemaining = 0;
            totalBurnTime = 0;
            workingHeatLevel = defaultWorkingHeatLevel();
        }
        else if (burnRemaining > 0) {
            boolean consumeThisTick = fullMode || (world.getTotalWorldTime() % pilotMultiplier() == 0);
            if (consumeThisTick) { burnRemaining--; }
            if (fullMode) {
                if (heatLevel < targetHeat) { heatLevel = Math.min(heatLevel + heatPerTick, targetHeat); }
                else { heatLevel = Math.max(heatLevel - heatLossPerTick(), targetHeat); }
            }
            else { heatLevel = Math.max(heatLevel - heatLossPerTick(), pilotHeat()); }
        }
        else {
            totalBurnTime = 0;
            ItemStack fuelStack = inventory.get(0);
            BoilerSolidRecipe recipe = fuelStack.isEmpty() ? null : BoilerSolidRecipe.findFuel(fuelStack);
            ItemStack single = fuelStack.copy();
            if (!single.isEmpty()) single.setCount(1);
            int burnTimePerItem = single.isEmpty() ? 0 : ForgeEventFactory.getItemBurnTime(single);
            double newHeatPerTick = defaultHeatPerTick();
            double newTargetHeat = defaultWorkingHeatLevel();
            int consumeAmount = 1;
            if (recipe != null) {
                newHeatPerTick = recipe.heatPerTick;
                newTargetHeat = recipe.targetHeat;
                consumeAmount = recipe.itemInput.inputSize;
                if (burnTimePerItem <= 0) { burnTimePerItem = 200; }
            }
            if (burnTimePerItem <= 0 || fuelStack.getCount() < consumeAmount) {
                pilotLit = false;
                heatLevel = Math.max(heatLevel - heatLossPerTick(), 0);
                workingHeatLevel = defaultWorkingHeatLevel();
            }
            else {
                fuelStack.shrink(consumeAmount);
                if (fuelStack.getCount() <= 0) inventory.set(0, ItemStack.EMPTY);
                burnRemaining = (burnTimePerItem * consumeAmount) / burnTimeDivider();
                totalBurnTime = burnRemaining;
                heatPerTick = newHeatPerTick;
                targetHeat = newTargetHeat;
                workingHeatLevel = newTargetHeat;
            }
        }
        return previousHeat != heatLevel || previousPilot != pilotLit || previousWorking != workingHeatLevel || previousBurn != burnRemaining;
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityBoilerSolidMaster master() { return this; }

    @Override public TileEntity getGuiMaster() { return this; }

    @Override public int getComparatorInputOverride() { return isComparatorPos() ? comparatorValue() : 0; }

    public int comparatorValue() { return workingHeatLevel > 0 ? (int)Math.min(15, 15 * (heatLevel / workingHeatLevel)) : 0; }

    @Override @Nonnull public NonNullList<ItemStack> getInventory() { return inventory; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return findBurnTime(stack) > 0; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) { markDirty(); markContainingBlockForUpdate(null); }

    @Override @Nonnull public NonNullList<ItemStack> getDroppedItems() { return inventory; }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return ITUtils.EMPTY_INT_ARRAY; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return ITUtils.EMPTY_INT_ARRAY; }
}
