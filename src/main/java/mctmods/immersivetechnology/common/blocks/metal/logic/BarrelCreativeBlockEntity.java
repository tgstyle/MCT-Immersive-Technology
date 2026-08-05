package mctmods.immersivetechnology.common.blocks.metal.logic;

import mctmods.immersivetechnology.common.blocks.helper.BlockInterfaces;
import mctmods.immersivetechnology.core.ClientConfig;
import mctmods.immersivetechnology.core.CommonConfig;
import mctmods.immersivetechnology.core.registration.BlockEntities;
import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.util.Utils;

import blusunrize.immersiveengineering.api.IEApiDataComponents;
import com.mojang.datafixers.util.Unit;
import java.text.DecimalFormat;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BarrelCreativeBlockEntity extends OSDCommonBlockEntity implements BlockInterfaces.IBlockEntityDrop, BlockInterfaces.IPlayerInteraction, BlockInterfaces.IBlockOverlayText {
    private FluidStack selectedFluid = FluidStack.EMPTY;

    private static int creativeBarrelOutputAmount() { return CommonConfig.creativeBarrelOutputAmount; }

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0.###");

    public BarrelCreativeBlockEntity(BlockPos pos, BlockState state) { super(BlockEntities.BARREL_CREATIVE.get(), pos, state); }

    @Override public void tickServer() {
        if (selectedFluid.isEmpty() || level == null) { super.tickServer(); return; }
        FluidStack baseFs = pressurizedCopy(selectedFluid, creativeBarrelOutputAmount());
        long thisTickOutput = 0;
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = worldPosition.relative(dir);
            FluidStack fsToOffer = baseFs.copy();
            IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, neighborPos, dir.getOpposite());
            if (handler == null) { continue; }
            int accepted = handler.fill(fsToOffer, FluidAction.SIMULATE);
            if (accepted <= 0) { continue; }
            FluidStack toFill = Utils.copyFluidStackWithAmount(fsToOffer, accepted, false);
            int filled = handler.fill(toFill, FluidAction.EXECUTE);
            thisTickOutput += filled;
        }
        acceptedAmount += thisTickOutput;
        super.tickServer();
    }

    @Override public void readCustomNBT(@NotNull CompoundTag nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        if (nbt.contains("SelectedFluid")) {
            selectedFluid = FluidStack.parseOptional(level != null ? level.registryAccess() : HolderLookup.Provider.create(Stream.empty()), nbt.getCompound("SelectedFluid"));
        }
    }

    @Override public void writeCustomNBT(@NotNull CompoundTag nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        if (!selectedFluid.isEmpty()) {
            nbt.put("SelectedFluid", selectedFluid.save(level != null ? level.registryAccess() : HolderLookup.Provider.create(Stream.empty())));
        }
    }

    @SuppressWarnings("unused")
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return new IFluidHandler() {
            @Override public int getTanks() { return 1; }

            @Override @NotNull public FluidStack getFluidInTank(int tank) {
                return pressurizedCopy(selectedFluid, Integer.MAX_VALUE);
            }

            @Override public int getTankCapacity(int tank) { return Integer.MAX_VALUE; }

            @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return false; }

            @Override public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) { return 0; }

            @Override @NotNull public FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) {
                if (selectedFluid.isEmpty() || !FluidStack.isSameFluidSameComponents(selectedFluid, resource)) { return FluidStack.EMPTY; }
                return pressurizedCopy(selectedFluid, resource.getAmount());
            }

            @Override public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
                if (selectedFluid.isEmpty()) { return FluidStack.EMPTY; }
                return pressurizedCopy(selectedFluid, maxDrain);
            }
        };
    }

    @Override public boolean interact(@NotNull Direction side, @NotNull Player player, @NotNull InteractionHand hand, @NotNull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        FluidStack contained = FluidUtil.getFluidContained(heldItem).orElse(FluidStack.EMPTY);
        if (!contained.isEmpty()) {
            setOutputFluid(contained);
            if (level != null && !level.isClientSide) {
                SoundEvent sound = contained.getFluid().getFluidType().getSound(player, level, worldPosition, SoundActions.BUCKET_EMPTY);
                if (sound == null) {
                    @SuppressWarnings("deprecation")
                    boolean isLava = contained.getFluid().builtInRegistryHolder().is(FluidTags.LAVA);
                    sound = isLava ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
                }
                level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return true;
        } else if (player.isShiftKeyDown()) {
            setOutputFluid(FluidStack.EMPTY);
            return true;
        }
        IFluidHandler handler = getFluidHandler(null);
        return FluidUtil.interactWithFluidHandler(player, hand, handler);
    }

    @Override public TranslationKey text() { return TranslationKey.OVERLAY_OSD_BARREL_NORMAL_FIRST_LINE; }

    @Override public Component[] getOverlayText(@NotNull Player player, @NotNull HitResult rtr, boolean hammer) {
        if (rtr.getType() == HitResult.Type.MISS) { return null; }
        // Removed illegal packet send from render thread
        if (selectedFluid.isEmpty()) { return new Component[]{Component.translatable(TranslationKey.GUI_EMPTY.getLocation())}; }
        Component fluidName = selectedFluid.getHoverName();
        double rawValue = ClientConfig.perTickTrashCans ? (double)lastAcceptedAmount / 20.0 : lastAcceptedAmount;
        String value = NUMBER_FORMAT.format(rawValue);
        return new Component[]{Component.translatable(text().getLocation(), fluidName, value)};
    }

    @Override public void getBlockEntityDrop(@NotNull LootContext context, @NotNull Consumer<ItemStack> drop) {
        ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, level != null ? level.registryAccess() : HolderLookup.Provider.create(Stream.empty()));
        if (!tag.isEmpty()) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        drop.accept(stack);
    }

    @Override public void onBEPlaced(BlockPlaceContext ctx) { onBEPlaced(ctx.getItemInHand()); }

    public void setOutputFluid(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            this.selectedFluid = FluidStack.EMPTY;
        } else {
            this.selectedFluid = fluidStack.copy();
            this.selectedFluid.setAmount(1);
            this.selectedFluid.set(IEApiDataComponents.FLUID_PRESSURIZED.get(), Unit.INSTANCE);
        }
        setChanged();
    }

    @Override protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!selectedFluid.isEmpty()) {
            tag.put("SelectedFluid", selectedFluid.save(registries));
        }
    }

    @Override protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("SelectedFluid")) {
            selectedFluid = FluidStack.parseOptional(registries, tag.getCompound("SelectedFluid"));
        }
    }

    public void onBEPlaced(ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_DATA)) {
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData != null) {
                CompoundTag tag = customData.copyTag();
                if (tag.contains("SelectedFluid")) {
                    selectedFluid = FluidStack.parseOptional(level != null ? level.registryAccess() : HolderLookup.Provider.create(Stream.empty()), tag.getCompound("SelectedFluid"));
                }
            }
        }
    }

    private FluidStack pressurizedCopy(FluidStack src, int amt) {
        if (src.isEmpty()) { return FluidStack.EMPTY; }
        FluidStack fs = src.copyWithAmount(amt);
        fs.set(IEApiDataComponents.FLUID_PRESSURIZED.get(), Unit.INSTANCE);
        return fs;
    }
}
