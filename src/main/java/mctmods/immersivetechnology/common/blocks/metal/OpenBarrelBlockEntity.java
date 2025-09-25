package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableMap;
import mctmods.immersivetechnology.common.blocks.helper.ITBlockInterfaces;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.EnumMap;
import java.util.Random;
import java.util.function.Consumer;

public class OpenBarrelBlockEntity extends IEBaseBlockEntity implements IEServerTickableBE, ITBlockInterfaces.IBlockOverlayText, ITBlockInterfaces.IPlayerInteraction, ITBlockInterfaces.IBlockEntityDrop, ITBlockInterfaces.IComparatorOverride, ITBlockInterfaces.IPlacementInteraction {
    private static final int tankSize = 12000;
    private static final int transferSpeed = 40;

    public final ITMarkableFluidTank tank = new ITMarkableFluidTank(tankSize, v -> setChanged());
    public EnumMap<Direction, IOSideConfig> sideConfig = new EnumMap<>(ImmutableMap.of(Direction.DOWN, IOSideConfig.OUTPUT, Direction.UP, IOSideConfig.INPUT));
    protected int sleep = 0;

    private static final Random RANDOM = new Random();

    private final LazyOptional<IFluidHandler> nonsidedHandler = LazyOptional.of(() -> new SidedFluidHandler(this, null));
    private final LazyOptional<IFluidHandler> upHandler = LazyOptional.of(() -> new SidedFluidHandler(this, Direction.UP));
    private final LazyOptional<IFluidHandler> downHandler = LazyOptional.of(() -> new SidedFluidHandler(this, Direction.DOWN));

    public OpenBarrelBlockEntity(BlockPos pos, BlockState state) { super(ITBlockEntities.OPEN_BARREL.get(), pos, state); }

    @Override
    public void tickServer() {
        boolean update = false;
        if (level == null || level.isClientSide) return;
        if (tank.getFluidAmount() < tank.getCapacity() && RANDOM.nextInt(20) == 0) {
            FluidStack fs = tank.getFluid();
            if (fs.isEmpty() || fs.getFluid() == Fluids.WATER) {
                Biome biome = level.getBiome(worldPosition).value();
                float temp = biome.getBaseTemperature();
                if (level.isRainingAt(worldPosition.above()) && level.canSeeSky(worldPosition.above()) && temp > 0.05F && temp < 2.0F) {
                    int amount = level.isThundering() ? 200 : 100;
                    tank.fill(new FluidStack(Fluids.WATER, amount), IFluidHandler.FluidAction.EXECUTE);
                    update = true;
                } else if (temp >= 2.0F) {
                    tank.drain(Math.min(100, tank.getFluidAmount()), IFluidHandler.FluidAction.EXECUTE);
                    update = true;
                }
            }
        }
        if (tank.getFluidAmount() > 0 && sideConfig.get(Direction.DOWN) == IOSideConfig.OUTPUT) {
            Direction face = Direction.DOWN;
            IFluidHandler output = CapabilityReference.forNeighbor(this, ForgeCapabilities.FLUID_HANDLER, face).getNullable();
            if (output != null) {
                if (sleep == 0) {
                    FluidStack simulatedDrain = tank.drain(Math.min(transferSpeed, tank.getFluidAmount()), IFluidHandler.FluidAction.SIMULATE);
                    if (!simulatedDrain.isEmpty()) {
                        int accepted = output.fill(simulatedDrain, IFluidHandler.FluidAction.SIMULATE);
                        if (accepted > 0) {
                            FluidStack drained = tank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
                            output.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                            update = true; sleep = 0;
                        } else { sleep = 20; }
                    } else { sleep = 20; }
                } else { sleep--; }
            }
        }
        if (update) { setChanged(); markContainingBlockForUpdate(null); }
    }

    @Override
    public void readCustomNBT(@NotNull CompoundTag nbt, boolean descPacket) {
        sideConfig.clear();
        int[] sideCfgArray = nbt.getIntArray("sideConfig");
        if (sideCfgArray.length >= 2) {
            sideConfig.put(Direction.DOWN, IOSideConfig.VALUES[sideCfgArray[0]]);
            sideConfig.put(Direction.UP, IOSideConfig.VALUES[sideCfgArray[1]]);
        } else {
            sideConfig.put(Direction.DOWN, IOSideConfig.OUTPUT);
            sideConfig.put(Direction.UP, IOSideConfig.INPUT);
        }
        tank.readFromNBT(nbt.getCompound("tank"));
    }

    @Override
    public void writeCustomNBT(@NotNull CompoundTag nbt, boolean descPacket) {
        int[] sideCfgArray = new int[2];
        sideCfgArray[0] = sideConfig.getOrDefault(Direction.DOWN, IOSideConfig.OUTPUT).ordinal();
        sideCfgArray[1] = sideConfig.getOrDefault(Direction.UP, IOSideConfig.INPUT).ordinal();
        nbt.putIntArray("sideConfig", sideCfgArray);
        nbt.put("tank", tank.writeToNBT(new CompoundTag()));
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            if (facing == null) return nonsidedHandler.cast();
            if (facing.getAxis() != Direction.Axis.Y) return super.getCapability(capability, facing);
            return (facing == Direction.UP ? upHandler : downHandler).cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        nonsidedHandler.invalidate();
        upHandler.invalidate();
        downHandler.invalidate();
    }

    @Override
    public boolean interact(@NotNull Direction side, @NotNull Player player, @NotNull InteractionHand hand, @NotNull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        FluidStack contained = FluidUtil.getFluidContained(heldItem).orElse(FluidStack.EMPTY);
        if (!isFluidValid(contained)) { player.displayClientMessage(Component.translatable(TranslationKey.NO_GAS_ALLOWED.text()), false); return true; }
        if (FluidUtil.interactWithFluidHandler(player, hand, tank)) { setChanged(); markContainingBlockForUpdate(null); return true; }
        return false;
    }

    @Override
    public Component[] getOverlayText(@NotNull Player player, @NotNull HitResult rtr, boolean hammer) {
        if (rtr.getType() == HitResult.Type.MISS) return null;
        if (Utils.isFluidRelatedItemStack(player.getItemInHand(InteractionHand.MAIN_HAND))) {
            FluidStack fs = tank.getFluid();
            if (fs.isEmpty()) return new Component[]{Component.translatable(TranslationKey.GUI_EMPTY.text())};
            return new Component[]{Component.literal(TranslationKey.OVERLAY_OSD_BARREL_NORMAL_FIRST_LINE.format(fs.getDisplayName().getString(), fs.getAmount()))};
        }
        return new Component[0];
    }

    @Override
    public boolean useNixieFont(@NotNull Player player, @NotNull HitResult mop) { return false; }

    @Override
    public int getComparatorInputOverride() { return (15 * tank.getFluidAmount()) / tank.getCapacity(); }

    @Override
    public void getBlockEntityDrop(@NotNull LootContext context, @NotNull Consumer<ItemStack> drop) {
        ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
        CompoundTag tag = new CompoundTag();
        writeTank(tag, true);
        if (!tag.isEmpty()) stack.setTag(tag);
        drop.accept(stack);
    }

    @Override
    public void onBEPlaced(BlockPlaceContext ctx) { if (ctx.getItemInHand().hasTag()) readTank(ctx.getItemInHand().getOrCreateTag()); }

    public boolean isFluidValid(@NotNull FluidStack fluid) { return !fluid.isEmpty() && fluid.getFluid().getFluidType().getDensity(fluid) >= 0; }

    public void writeTank(CompoundTag nbt, boolean toItem) {
        boolean write = tank.getFluidAmount() > 0;
        CompoundTag tankTag = tank.writeToNBT(new CompoundTag());
        if (!toItem || write) nbt.put("tank", tankTag);
    }

    public void readTank(CompoundTag nbt) { tank.readFromNBT(nbt.getCompound("tank")); }

    @Override
    public boolean triggerEvent(int id, int arg) { if (id == 0) { markContainingBlockForUpdate(null); return true; } return false; }

    public static class SidedFluidHandler implements IFluidHandler {
        OpenBarrelBlockEntity barrel;
        @Nullable Direction facing;

        public SidedFluidHandler(OpenBarrelBlockEntity barrel, @Nullable Direction facing) { this.barrel = barrel; this.facing = facing; }

        @Override
        public int getTanks() { return barrel.tank.getTanks(); }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) { return barrel.tank.getFluidInTank(tank); }

        @Override
        public int getTankCapacity(int tank) { return barrel.tank.getTankCapacity(tank); }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return barrel.isFluidValid(stack); }

        @Override
        public int fill(FluidStack resource, FluidAction action) { if (resource.isEmpty() || (facing != null && barrel.sideConfig.get(facing) != IOSideConfig.INPUT)) return 0; return barrel.tank.fill(resource, action); }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) { if (resource.isEmpty() || (facing != null && barrel.sideConfig.get(facing) != IOSideConfig.OUTPUT)) return FluidStack.EMPTY; return barrel.tank.drain(resource, action); }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) { if (facing != null && barrel.sideConfig.get(facing) != IOSideConfig.OUTPUT) return FluidStack.EMPTY; return barrel.tank.drain(maxDrain, action); }
    }
}
