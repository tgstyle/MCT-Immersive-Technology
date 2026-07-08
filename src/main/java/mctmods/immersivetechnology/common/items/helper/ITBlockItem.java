package mctmods.immersivetechnology.common.items.helper;

import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockPartBlock;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import mctmods.immersivetechnology.common.blocks.helper.ITIBaseBlock;
import mctmods.immersivetechnology.common.blocks.helper.ITIBlock;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ITBlockItem extends BlockItem {
    public ITBlockItem(Block b, Item.Properties props) { super(b, props); }

    public ITBlockItem(Block b) { this(b, new Item.Properties()); }

    @Override @NotNull public String getDescriptionId(@NotNull ItemStack stack) { return getBlock().getDescriptionId(); }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        if (getBlock() instanceof ITIBlock ieBlock && ieBlock.hasFlavour()) {
            String flavourKey = ITLib.DESC_FLAVOUR + ieBlock.getNameForFlavour();
            tooltipComponents.add(TextUtils.applyFormat(Component.translatable(flavourKey), ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (tag.contains(EnergyHelper.ENERGY_KEY)) {
            tooltipComponents.add(TextUtils.applyFormat(Component.translatable(ITLib.DESC_INFO + "energyStored", tag.getInt(EnergyHelper.ENERGY_KEY)), ChatFormatting.GRAY));
        }
        if (tag.contains("tank")) {
            CompoundTag tankTag = tag.getCompound("tank");
            Level level = context.level();
            FluidStack fs = level != null ? FluidStack.parseOptional(level.registryAccess(), tankTag) : FluidStack.EMPTY;
            if (!fs.isEmpty()) {
                tooltipComponents.add(TextUtils.applyFormat(Component.translatable(ITLib.DESC_INFO + "fluidStored", fs.getHoverName(), fs.getAmount()), ChatFormatting.GRAY));
            }
        }
    }

    @Override protected boolean placeBlock(@NotNull BlockPlaceContext context, @NotNull BlockState newState) {
        if (getBlock() instanceof MultiblockPartBlock) { return false; }
        Block b = newState.getBlock();
        if (b instanceof ITIBaseBlock ieBlock) {
            if (!ieBlock.canIEBlockBePlaced(newState, context)) return false;
            boolean ret = super.placeBlock(context, newState);
            if (ret) ieBlock.onIEBlockPlacedBy(context, newState);
            return ret;
        } else return super.placeBlock(context, newState);
    }

    @Override protected boolean updateCustomBlockEntityTag(@NotNull BlockPos pos, @NotNull Level worldIn, @Nullable Player player, @NotNull ItemStack stack, BlockState state) {
        if (!state.hasProperty(ITProperties.MULTIBLOCKSLAVE)) return super.updateCustomBlockEntityTag(pos, worldIn, player, stack, state);
        else return false;
    }

    @Override @Nonnull public Optional<TooltipComponent> getTooltipImage(@Nonnull ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("Items")) {
            ListTag list = tag.getList("Items", 10);
            NonNullList<ItemStack> items = NonNullList.create();
            for (Tag e : list) {
                if (e instanceof CompoundTag ct) {
                    ItemStack s = ItemStack.CODEC.parse(NbtOps.INSTANCE, ct)
                            .result()
                            .orElse(ItemStack.EMPTY);
                    if (!s.isEmpty()) items.add(s);
                }
            }
            BundleContents contents = new BundleContents(items);
            return Optional.of(new BundleTooltip(contents));
        }
        return super.getTooltipImage(stack);
    }

    @Override public boolean canFitInsideContainerItems() { return !(getBlock() instanceof ITIBaseBlock ieBlock) || ieBlock.fitsIntoContainer(); }
}
