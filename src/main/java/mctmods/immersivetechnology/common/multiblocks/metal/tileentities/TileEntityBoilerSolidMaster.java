package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import com.immersiveconvergence.api.capability.IHeatConsumer;
import com.immersiveconvergence.api.multiblock.PoICache;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.particles.ParticleFlameCustom;
import com.immersiveconvergence.api.particles.ParticleCampfireSmoke;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.crafting.BoilerSolidRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartBoilerSolid;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
import mctmods.immersivetechnology.common.util.compat.advancedrocketry.AdvancedRocketryHelper;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.IBinaryMessageReceiver;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;

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
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TileEntityBoilerSolidMaster extends TileEntityBoilerSolidSlave implements IComparatorOverride, IIEInventory, IBinaryMessageReceiver {

    private static double heatLossPerTick() { return Multiblocks.boilerSolid.boilerSolid_heat_lossPerTick; }
    private static double pilotHeat() { return Multiblocks.boilerSolid.boilerSolid_heat_pilot; }
    private static int pilotMultiplier() { return Multiblocks.boilerSolid.boilerSolid_pilot_fuelMultiplier; }
    private static double defaultHeatPerTick() { return Multiblocks.boilerSolid.boilerSolid_heat_defaultPerTick; }
    private static int burnTimeDivider() { return Multiblocks.boilerSolid.boilerSolid_burnTime_divider; }
    private static double defaultWorkingHeatLevel() { return Multiblocks.boilerHeat.boiler_heat_workingLevel; }

    public static int slotCount = 1;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    final IItemHandler inputHandler = new IEInventoryHandler(1, this, 0, new boolean[]{true}, new boolean[]{false});

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

    private boolean needsPoIInit = false;
    private boolean needsNotify = false;

    protected PoICache itemInputPos0, heatOutputPos0, redstonePos0;
    private BlockPos heatConsumerTEPos0, soundPos0, exhaustPos0;
    private List<BlockPos> ignitionPositions;

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
        if (!descPacket) { inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount); }
        if (formed && !descPacket) {
            needsPoIInit = true;
            needsNotify = true;
        }
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
        if (!descPacket) { nbt.setTag("inventory", Utils.writeInventory(inventory)); }
    }

    void InitializePoIs() {
        List<BlockPos> ignition = new ArrayList<>();
        for (PoIJSONSchema poi : TileEntityITMultiblockPartBoilerSolid.instance.pointsOfInterest) {
            switch (poi.name) {
                case "item_input0":
                    itemInputPos0 = new PoICache(facing, poi, mirrored);
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

    public boolean tryIgnite(BlockPos posInMultiblock, EntityPlayer player, ItemStack heldItem) {
        if (!formed) return false;
        if (ignitionPositions == null) InitializePoIs();
        if (!ignitionPositions.contains(posInMultiblock)) return false;
        boolean torch = Block.getBlockFromItem(heldItem.getItem()) == Blocks.TORCH;
        boolean flintAndSteel = heldItem.getItem() == Items.FLINT_AND_STEEL;
        if (!torch && !flintAndSteel) return false;
        if (pilotLit) return true;
        if (findBurnTime(inventory.get(0)) <= 0) return true;
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

    private int findBurnTime(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        ItemStack single = stack.copy();
        single.setCount(1);
        int burnTime = ForgeEventFactory.getItemBurnTime(single);
        if (burnTime <= 0 && BoilerSolidRecipe.findFuel(single) != null) { burnTime = 200; }
        return burnTime;
    }

    private void notifyIONeighbors() {
        if (itemInputPos0 != null) world.notifyNeighborsOfStateChange(getBlockPosForPos(itemInputPos0.position), getBlockType(), true);
        if (redstonePos0 != null) world.updateComparatorOutputLevel(getBlockPosForPos(redstonePos0.position), getBlockType());
    }

    private void notifyNearbyClients() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeDouble(heatLevel);
        buf.writeDouble(workingHeatLevel);
        buf.writeInt(burnRemaining);
        buf.writeInt(totalBurnTime);
        buf.writeBoolean(pilotLit);
        buf.writeBoolean(isRunning);
        BinaryMessageTileSync.sendToAllTracking(world, getPos(), buf);
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
        if (soundPos0 == null) InitializePoIs();
        float targetSoundLevel = heatLevel > 0 ? (float)(heatLevel / workingHeatLevel) : 0f;
        if (soundVolume < targetSoundLevel) { soundVolume = Math.min(soundVolume + 0.01f, targetSoundLevel); }
        else if (soundVolume > targetSoundLevel) { soundVolume = Math.max(soundVolume - 0.01f, targetSoundLevel); }
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        float attenuation = Math.max((float)player.getDistanceSq(soundPos0.getX() + 0.5, soundPos0.getY() + 0.5, soundPos0.getZ() + 0.5) / 8, 1);
        float volume = (2 * soundVolume) / attenuation;
        if (soundVolume <= 0f || volume <= 0.01f) { ITSoundHandler.StopSound(soundPos0); }
        else { ITSounds.boilerSolid.PlayRepeating(soundPos0, volume, soundVolume); }
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
            ParticleCampfireSmoke cloud = new ParticleCampfireSmoke(world,
                    exhaustPos0.getX() + 0.5,
                    exhaustPos0.getY() + 1.25,
                    exhaustPos0.getZ() + 0.5,
                    (rand.nextDouble() - 0.5) * 0.0125, 0.05, (rand.nextDouble() - 0.5) * 0.0125);
            cloud.setRBGColorF(0.2f, 0.2f, 0.2f);
            Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        if (soundPos0 != null) ITSoundHandler.StopSound(soundPos0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        if (soundPos0 == null) InitializePoIs();
        if (soundPos0 != null) {
            ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
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
        if (needsPoIInit || itemInputPos0 == null || heatOutputPos0 == null || redstonePos0 == null || soundPos0 == null || exhaustPos0 == null || ignitionPositions == null) {
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
        int previousBurn = burnRemaining;
        boolean canCombust = true;
        if (ITCompatModule.isAdvancedRocketryLoaded) { canCombust = AdvancedRocketryHelper.isAtmosphereSuitableForCombustion(world, exhaustPos0); }
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

    @Override public int getComparatorInputOverride() { return workingHeatLevel > 0 ? (int)Math.min(15, 15 * (heatLevel / workingHeatLevel)) : 0; }

    @Override @Nonnull public NonNullList<ItemStack> getInventory() { return inventory; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return findBurnTime(stack) > 0; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) { markDirty(); markContainingBlockForUpdate(null); }

    @Override @Nonnull public NonNullList<ItemStack> getDroppedItems() { return inventory; }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstonePos0 == null) InitializePoIs();
        return new int[]{toFlatIndex(redstonePos0.position)};
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }
}
