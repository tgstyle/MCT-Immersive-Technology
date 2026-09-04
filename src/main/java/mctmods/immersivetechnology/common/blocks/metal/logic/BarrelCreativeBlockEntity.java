package mctmods.immersivetechnology.common.blocks.metal.logic;

import com.immersiveconvergence.api.block.BlockInterfaces;
import mctmods.immersivetechnology.core.CommonConfig;
import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.registration.BlockEntities;

import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import java.util.function.Consumer;
import com.immersiveconvergence.api.util.ICFluidUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BarrelCreativeBlockEntity extends OSDCommonBlockEntity implements BlockInterfaces.IBlockEntityDrop, BlockInterfaces.IPlayerInteraction, BlockInterfaces.IBlockOverlayText {
    private FluidStack selectedFluid = FluidStack.EMPTY;

    private static int creativeBarrelOutputAmount() { return CommonConfig.creativeBarrelOutputAmount; }

    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> new IFluidHandler() {
        @Override public int getTanks() { return 1; }

        @Override @Nonnull public FluidStack getFluidInTank(int tank) {
            if (selectedFluid.isEmpty()) { return FluidStack.EMPTY; }
            return new FluidStack(selectedFluid, Integer.MAX_VALUE);
        }

        @Override public int getTankCapacity(int tank) { return Integer.MAX_VALUE; }

        @Override public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return false; }

        @Override public int fill(FluidStack resource, FluidAction action) { return 0; }

        @Override @Nonnull public FluidStack drain(FluidStack resource, FluidAction action) {
            if (selectedFluid.isEmpty() || !selectedFluid.isFluidEqual(resource)) { return FluidStack.EMPTY; }
            if (action.execute()) { acceptedAmount += resource.getAmount(); }
            return new FluidStack(selectedFluid, resource.getAmount());
        }

        @Override public @Nonnull FluidStack drain(int maxDrain, FluidAction action) {
            if (selectedFluid.isEmpty()) { return FluidStack.EMPTY; }
            if (action.execute()) { acceptedAmount += maxDrain; }
            return new FluidStack(selectedFluid, maxDrain);
        }
    });

    public BarrelCreativeBlockEntity(BlockPos pos, BlockState state) { super(BlockEntities.BARREL_CREATIVE.get(), pos, state); }

    @Override public void tickServer() {
        if (selectedFluid.isEmpty() || level == null) {
            super.tickServer();
            return;
        }

        FluidStack baseFs = selectedFluid.copy();
        baseFs.setAmount(creativeBarrelOutputAmount());
        boolean hadTag = baseFs.hasTag() && baseFs.getTag().contains(IFluidPipe.NBT_PRESSURIZED);
        if (hadTag) { baseFs.removeChildTag(IFluidPipe.NBT_PRESSURIZED); }

        long thisTickOutput = 0;
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = worldPosition.relative(dir);
            BlockEntity neighbor = level.getBlockEntity(neighborPos);
            boolean isPipe = neighbor instanceof FluidPipeBlockEntity;

            FluidStack fsToOffer = baseFs.copy();
            if (isPipe) { fsToOffer.getOrCreateTag().putBoolean(IFluidPipe.NBT_PRESSURIZED, true); }

            LazyOptional<IFluidHandler> cap = FluidUtil.getFluidHandler(level, neighborPos, dir.getOpposite());
            if (!cap.isPresent()) { continue; }
            IFluidHandler handler = cap.orElseThrow(AssertionError::new);

            int accepted = handler.fill(fsToOffer, FluidAction.SIMULATE);
            if (accepted <= 0) { continue; }

            FluidStack toFill = ICFluidUtils.copyFluidStackWithAmount(fsToOffer, accepted, false);
            int filled = handler.fill(toFill, FluidAction.EXECUTE);
            thisTickOutput += filled;
        }
        acceptedAmount += thisTickOutput;

        super.tickServer();
    }

    @Override public void readCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket) {
        if (nbt.contains("SelectedFluid")) {
            selectedFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("SelectedFluid"));
            if (selectedFluid == null) { selectedFluid = FluidStack.EMPTY; }
        }
    }

    @Override public void writeCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket) {
        if (!selectedFluid.isEmpty()) { nbt.put("SelectedFluid", selectedFluid.writeToNBT(new CompoundTag())); }
    }

    @Override public <T> @Nonnull LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) { return fluidHandler.cast(); }
        return super.getCapability(cap, side);
    }

    @Override public boolean interact(@Nonnull Direction side, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
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

    @Override public TranslationKey text() { return TranslationKey.OVERLAY_OSD_BARREL_NORMAL_FIRST_LINE; }

    @Override public Component[] getOverlayText(@Nonnull Player player, @Nonnull HitResult rtr, boolean hammer) {
        if (rtr.getType() == HitResult.Type.MISS) { return null; }
        requestOverlaySync();
        if (selectedFluid.isEmpty()) { return new Component[]{Component.translatable(TranslationKey.GUI_EMPTY.getLocation())}; }
        return new Component[]{Component.translatable(text().getLocation(), selectedFluid.getDisplayName(), formattedAmount())};
    }

    @Override public void getBlockEntityDrop(@Nonnull LootContext context, @Nonnull Consumer<ItemStack> drop) {
        ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        if (!tag.isEmpty()) { stack.setTag(tag); }
        drop.accept(stack);
    }

    @Override public void onBEPlaced(BlockPlaceContext ctx) { onBEPlaced(ctx.getItemInHand()); }

    public void setOutputFluid(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            this.selectedFluid = FluidStack.EMPTY;
        } else {
            this.selectedFluid = fluidStack.copy();
            this.selectedFluid.setAmount(1);
        }
        setChanged();
    }

    @Override protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        if (!selectedFluid.isEmpty()) { tag.put("SelectedFluid", selectedFluid.writeToNBT(new CompoundTag())); }
    }

    @Override public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("SelectedFluid")) {
            selectedFluid = FluidStack.loadFluidStackFromNBT(tag.getCompound("SelectedFluid"));
            if (selectedFluid == null) { selectedFluid = FluidStack.EMPTY; }
        }
    }

    public void onBEPlaced(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("SelectedFluid")) {
                selectedFluid = FluidStack.loadFluidStackFromNBT(tag.getCompound("SelectedFluid"));
                if (selectedFluid == null) { selectedFluid = FluidStack.EMPTY; }
            }
        }
    }

    @Override public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandler.invalidate();
    }
}
