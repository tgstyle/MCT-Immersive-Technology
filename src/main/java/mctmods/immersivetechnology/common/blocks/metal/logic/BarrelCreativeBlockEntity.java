package mctmods.immersivetechnology.common.blocks.metal.logic;

import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeBlockEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.common.network.ITOSDRequestMessage;
import mctmods.immersivetechnology.common.network.ITPacketHandler;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.core.ITClientConfig;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;

public class BarrelCreativeBlockEntity extends OSDCommonBlockEntity implements IEBlockInterfaces.IBlockEntityDrop, IEBlockInterfaces.IPlayerInteraction, IEBlockInterfaces.IBlockOverlayText {
    private FluidStack selectedFluid = FluidStack.EMPTY;

    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> new IFluidHandler() {
        @Override
        public int getTanks() { return 1; }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            if (selectedFluid.isEmpty()) { return FluidStack.EMPTY; }
            return new FluidStack(selectedFluid, Integer.MAX_VALUE);
        }

        @Override
        public int getTankCapacity(int tank) { return Integer.MAX_VALUE; }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return false; }

        @Override
        public int fill(FluidStack resource, FluidAction action) { return 0; }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (selectedFluid.isEmpty() || !selectedFluid.isFluidEqual(resource)) { return FluidStack.EMPTY; }
            return new FluidStack(selectedFluid, resource.getAmount());
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (selectedFluid.isEmpty()) { return FluidStack.EMPTY; }
            return new FluidStack(selectedFluid, maxDrain);
        }
    });

    public BarrelCreativeBlockEntity(BlockPos pos, BlockState state) { super(ITBlockEntities.BARREL_CREATIVE.get(), pos, state); }

    @Override
    public void tickServer() {
        if (!selectedFluid.isEmpty()) {
            long thisTickOutput = 0;
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = worldPosition.relative(dir);
                assert level != null;
                BlockEntity neighbor = level.getBlockEntity(neighborPos);
                boolean isPipe = neighbor instanceof FluidPipeBlockEntity;
                FluidStack fs = selectedFluid.copy();
                fs.setAmount(Integer.MAX_VALUE);
                boolean hadTag = fs.hasTag() && fs.getTag().contains(IFluidPipe.NBT_PRESSURIZED);
                if (isPipe && !hadTag) { fs.getOrCreateTag().putBoolean(IFluidPipe.NBT_PRESSURIZED, true); }
                LazyOptional<IFluidHandler> cap = FluidUtil.getFluidHandler(level, neighborPos, dir.getOpposite());
                if (!cap.isPresent()) { continue; }
                IFluidHandler handler = cap.orElseThrow(AssertionError::new);
                int accepted = handler.fill(fs, FluidAction.SIMULATE);
                if (!hadTag) { fs.removeChildTag(IFluidPipe.NBT_PRESSURIZED); }
                if (accepted <= 0) { continue; }
                FluidStack toFill = Utils.copyFluidStackWithAmount(fs, accepted, false);
                if (isPipe) { toFill.getOrCreateTag().putBoolean(IFluidPipe.NBT_PRESSURIZED, true); }
                int filled = handler.fill(toFill, FluidAction.EXECUTE);
                thisTickOutput += filled;
            }
            acceptedAmount += thisTickOutput;
        }
        super.tickServer();
    }

    @Override
    public void readCustomNBT(@NotNull CompoundTag nbt, boolean descPacket) {
        if (nbt.contains("SelectedFluid")) {
            selectedFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("SelectedFluid"));
            if (selectedFluid == null) selectedFluid = FluidStack.EMPTY;
        }
    }

    @Override
    public void writeCustomNBT(@NotNull CompoundTag nbt, boolean descPacket) {
        if (!selectedFluid.isEmpty()) { nbt.put("SelectedFluid", selectedFluid.writeToNBT(new CompoundTag())); }
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) { return fluidHandler.cast(); }
        return super.getCapability(cap, side);
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
    public TranslationKey text() { return TranslationKey.OVERLAY_OSD_BARREL_NORMAL_FIRST_LINE; }

    @Override
    public Component[] getOverlayText(@NotNull Player player, @NotNull HitResult rtr, boolean hammer) {
        if (rtr.getType() == HitResult.Type.MISS) { return null; }
        assert level != null;
        if (level.isClientSide && requestCooldown == 0) {
            ITPacketHandler.sendToServer(new ITOSDRequestMessage(worldPosition));
            requestCooldown = 20;
        }
        if (selectedFluid.isEmpty()) { return new Component[]{Component.translatable(TranslationKey.GUI_EMPTY.text())}; }
        Component fluidName = selectedFluid.getDisplayName();
        float value = ITClientConfig.perTickTrashCans.get() ? (float)lastAcceptedAmount / 20 : lastAcceptedAmount;
        return new Component[]{Component.translatable(text().text(), fluidName, value)};
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

    public void setOutputFluid(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            this.selectedFluid = FluidStack.EMPTY;
        } else {
            this.selectedFluid = fluidStack.copy();
            this.selectedFluid.setAmount(1);
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        if (!selectedFluid.isEmpty()) { tag.put("SelectedFluid", selectedFluid.writeToNBT(new CompoundTag())); }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("SelectedFluid")) {
            selectedFluid = FluidStack.loadFluidStackFromNBT(tag.getCompound("SelectedFluid"));
            if (selectedFluid == null) selectedFluid = FluidStack.EMPTY;
        }
    }

    public void onBEPlaced(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            assert tag != null;
            if (tag.contains("SelectedFluid")) {
                selectedFluid = FluidStack.loadFluidStackFromNBT(tag.getCompound("SelectedFluid"));
                if (selectedFluid == null) selectedFluid = FluidStack.EMPTY;
            }
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandler.invalidate();
    }
}
