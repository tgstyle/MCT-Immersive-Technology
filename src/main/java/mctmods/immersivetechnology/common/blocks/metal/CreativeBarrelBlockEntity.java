package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeBlockEntity;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public class CreativeBarrelBlockEntity extends IEBaseBlockEntity implements IEBlockInterfaces.IBlockEntityDrop, IEBlockInterfaces.IPlayerInteraction, IEBlockInterfaces.IBlockOverlayText, IEServerTickableBE {
    private FluidStack selectedFluid = FluidStack.EMPTY;
    private int lastOutputAmount = 0;

    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> new IFluidHandler() {
        @Override
        public int getTanks() { return 1; }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            if (selectedFluid.isEmpty()) { return FluidStack.EMPTY; }
            return new FluidStack(selectedFluid.getFluid(), Integer.MAX_VALUE);
        }

        @Override
        public int getTankCapacity(int tank) { return Integer.MAX_VALUE; }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return false; }

        @Override
        public int fill(FluidStack resource, FluidAction action) { return 0; }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (!selectedFluid.isEmpty() && resource.getFluid() == selectedFluid.getFluid()) { return new FluidStack(selectedFluid.getFluid(), resource.getAmount()); }
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (!selectedFluid.isEmpty()) { return new FluidStack(selectedFluid.getFluid(), maxDrain); }
            return FluidStack.EMPTY;
        }
    });

    public CreativeBarrelBlockEntity(BlockPos pos, BlockState state) { super(ITBlockEntities.CREATIVE_BARREL.get(), pos, state); }

    public void setOutputFluid(FluidStack fluidStack) {
        this.selectedFluid = fluidStack.isEmpty() ? FluidStack.EMPTY : new FluidStack(fluidStack.getFluid(), 1);
        setChanged();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        if (!selectedFluid.isEmpty()) { tag.putString("SelectedFluid", Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(selectedFluid.getFluid())).toString()); }
        tag.putInt("lastOutputAmount", lastOutputAmount);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("SelectedFluid")) {
            ResourceLocation fluidId = ResourceLocation.parse(tag.getString("SelectedFluid"));
            if (ForgeRegistries.FLUIDS.containsKey(fluidId)) { this.selectedFluid = new FluidStack(Objects.requireNonNull(ForgeRegistries.FLUIDS.getValue(fluidId)), 1); }
        }
        lastOutputAmount = tag.getInt("lastOutputAmount");
    }

    @Override
    public void readCustomNBT(@NotNull CompoundTag nbt, boolean descPacket) {
        if (nbt.contains("SelectedFluid")) {
            ResourceLocation fluidId = ResourceLocation.parse(nbt.getString("SelectedFluid"));
            if (ForgeRegistries.FLUIDS.containsKey(fluidId)) { this.selectedFluid = new FluidStack(Objects.requireNonNull(ForgeRegistries.FLUIDS.getValue(fluidId)), 1); }
        }
        lastOutputAmount = nbt.getInt("lastOutputAmount");
    }

    @Override
    public void writeCustomNBT(@NotNull CompoundTag nbt, boolean descPacket) {
        if (!selectedFluid.isEmpty()) { nbt.putString("SelectedFluid", Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(selectedFluid.getFluid())).toString()); }
        nbt.putInt("lastOutputAmount", lastOutputAmount);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) { return fluidHandler.cast(); }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandler.invalidate();
    }

    @Override
    public void getBlockEntityDrop(@NotNull LootContext context, @NotNull Consumer<ItemStack> drop) {
        ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        if (!tag.isEmpty()) { stack.setTag(tag); }
        drop.accept(stack);
    }

    @Override
    public void onBEPlaced(BlockPlaceContext ctx) { onBEPlaced(ctx.getItemInHand()); }

    public void onBEPlaced(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            assert tag != null;
            if (tag.contains("SelectedFluid")) {
                ResourceLocation fluidId = ResourceLocation.parse(tag.getString("SelectedFluid"));
                if (ForgeRegistries.FLUIDS.containsKey(fluidId)) { this.selectedFluid = new FluidStack(Objects.requireNonNull(ForgeRegistries.FLUIDS.getValue(fluidId)), 1); }
            }
        }
    }

    @Override
    public boolean interact(@NotNull Direction side, @NotNull Player player, @NotNull InteractionHand hand, @NotNull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        FluidStack contained = FluidUtil.getFluidContained(heldItem).orElse(FluidStack.EMPTY);
        if (!contained.isEmpty()) {
            setOutputFluid(contained);
            if (level != null && !level.isClientSide) {
                SoundEvent sound = contained.getFluid().getFluidType().getSound(player, level, worldPosition, SoundActions.BUCKET_EMPTY);
                if (sound == null) { sound = ForgeRegistries.FLUIDS.getHolder(contained.getFluid()).map(holder -> holder.is(FluidTags.LAVA)).orElse(false) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY; }
                level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return true;
        } else if (player.isShiftKeyDown()) {
            setOutputFluid(FluidStack.EMPTY);
            return true;
        }
        return FluidUtil.interactWithFluidHandler(player, hand, fluidHandler.orElseThrow(RuntimeException::new));
    }

    @Override
    public void tickServer() {
        if (!selectedFluid.isEmpty()) {
            int[] outputThisTick = {0}; // Use array to allow modification in lambda
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = worldPosition.relative(dir);
                Object neighbor = Utils.getExistingTileEntity(level, neighborPos);
                boolean isPipe = neighbor instanceof FluidPipeBlockEntity;
                FluidStack toPush = new FluidStack(selectedFluid.getFluid(), Integer.MAX_VALUE);
                if (isPipe) {
                    CompoundTag tag = new CompoundTag();
                    tag.putBoolean("pressurized", true);
                    toPush.setTag(tag);
                }
                FluidUtil.getFluidHandler(level, neighborPos, dir.getOpposite()).ifPresent(handler -> {
                    int accepted = handler.fill(toPush, FluidAction.SIMULATE);
                    if (accepted > 0) {
                        int filled = handler.fill(new FluidStack(selectedFluid.getFluid(), accepted, toPush.getTag()), FluidAction.EXECUTE);
                        outputThisTick[0] += filled;
                    }
                });
            }
            if (outputThisTick[0] != lastOutputAmount) {
                lastOutputAmount = outputThisTick[0];
                setChanged();
            }
        }
    }

    @Override
    public Component[] getOverlayText(@NotNull Player player, HitResult rtr, boolean hammer) {
        if (rtr.getType() == HitResult.Type.MISS) { return null; }
        if (selectedFluid.isEmpty()) { return new Component[]{Component.translatable(TranslationKey.GUI_EMPTY.location)}; }
        Component fluidName = new FluidStack(selectedFluid.getFluid(), 1).getDisplayName();
        return new Component[]{Component.translatable(TranslationKey.OVERLAY_OSD_BARREL_NORMAL_FIRST_LINE.location, fluidName, lastOutputAmount)};
    }

    @Override
    public boolean useNixieFont(@NotNull Player player, @NotNull HitResult mop) { return false; }
}
