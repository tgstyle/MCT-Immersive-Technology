package mctmods.immersivetechnology.common.items;

import com.immersiveconvergence.api.block.HammerUse;
import com.immersiveconvergence.api.multiblock.MultiblockRegistry;
import com.immersiveconvergence.core.lib.ICLib;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class FormationTool extends Item {
    public FormationTool() { super(new Properties()); }

    @Override public int getMaxStackSize(@Nonnull ItemStack stack) { return 1; }

    @Override @Nonnull public Component getName(@Nonnull ItemStack pStack) { return Component.translatable(this.getDescriptionId(pStack)); }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nonnull TooltipContext context, @Nonnull List<Component> tooltipComponents, @Nonnull TooltipFlag tooltipFlag) {
        addInfo(tooltipComponents, ICLib.DESC_INFO + "multiblocksAllowed", stack, "multiblockPermission");
        addInfo(tooltipComponents, ICLib.DESC_INFO + "multiblockForbidden", stack, "multiblockInterdiction");
    }

    private void addInfo(List<Component> list, String titleKey, ItemStack stack, String nbtKey) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (!tag.contains(nbtKey, Tag.TAG_LIST)) return;

        MutableComponent title = Component.translatable(titleKey);
        ListTag tagList = tag.getList(nbtKey, Tag.TAG_STRING);

        if (!Screen.hasShiftDown()) {
            list.add(title.append(" ").append(Component.translatable(ICLib.DESC_INFO + "holdShift")));
        } else {
            list.add(title);
            for (int i = 0; i < tagList.size(); i++) {
                ResourceLocation mbName = ResourceLocation.tryParse(tagList.getString(i));
                if (mbName == null) continue;
                Component displayName = MultiblockRegistry.displayName(mbName);
                if (displayName != null) { list.add(displayName.copy().withStyle(ChatFormatting.DARK_GRAY)); }
            }
        }
    }

    @Override @Nonnull public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        Direction side = context.getClickedFace();

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        List<ResourceLocation> permittedMultiblocks = null;
        List<ResourceLocation> interdictedMultiblocks = null;

        if (tag.contains("multiblockPermission", Tag.TAG_LIST)) {
            ListTag list = tag.getList("multiblockPermission", Tag.TAG_STRING);
            permittedMultiblocks = parseMultiblockNames(list, player, "permission");
            if (permittedMultiblocks == null) return InteractionResult.FAIL;
        }
        if (tag.contains("multiblockInterdiction", Tag.TAG_LIST)) {
            ListTag list = tag.getList("multiblockInterdiction", Tag.TAG_STRING);
            interdictedMultiblocks = parseMultiblockNames(list, player, "interdiction");
            if (interdictedMultiblocks == null) return InteractionResult.FAIL;
        }

        final Direction multiblockSide = (side.getAxis() == Direction.Axis.Y && player != null)
                ? Direction.fromYRot(player.getYRot()).getOpposite() : side;

        final List<ResourceLocation> permitted = permittedMultiblocks;
        final List<ResourceLocation> interdicted = interdictedMultiblocks;
        Predicate<ResourceLocation> allowed = name -> permitted != null ? permitted.contains(name) : interdicted == null || !interdicted.contains(name);
        if (MultiblockRegistry.formFirstMatching(world, pos, multiblockSide, player, stack, allowed)) { return InteractionResult.SUCCESS; }
        return HammerUse.onBlock(world, pos, side, player, context.getHand(), context.getClickLocation());
    }

    @Nullable
    private static List<ResourceLocation> parseMultiblockNames(ListTag data, @Nullable Player player, String prefix) {
        List<ResourceLocation> result = new ArrayList<>();
        for (int i = 0; i < data.size(); ++i) {
            String listEntry = data.getString(i);
            ResourceLocation asRL = ResourceLocation.tryParse(listEntry);
            if (asRL == null || MultiblockRegistry.displayName(asRL) == null) {
                if (player != null && !player.level().isClientSide) {
                    player.displayClientMessage(Component.literal("Invalid " + prefix + " entry: " + listEntry), false);
                }
                return null;
            }
            result.add(asRL);
        }
        return result;
    }

    @Override public boolean doesSneakBypassUse(@Nonnull ItemStack stack, @Nonnull LevelReader world, @Nonnull BlockPos pos, @Nonnull Player player) { return true; }

    @Override @Nonnull public InteractionResult interactLivingEntity(@Nonnull ItemStack stack, @Nonnull Player player, @Nonnull LivingEntity entity, @Nonnull InteractionHand hand) {
        return HammerUse.onEntity(player, entity);
    }

    @Override public boolean hasCraftingRemainingItem(@Nonnull ItemStack stack) { return true; }

    @Override @Nonnull public ItemStack getCraftingRemainingItem(@Nonnull ItemStack stack) {
        ItemStack container = stack.copy();
        int newDamage = container.getDamageValue() + 1;
        if (newDamage >= container.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        container.setDamageValue(newDamage);
        return container;
    }

    @Override public boolean isEnchantable(@Nonnull ItemStack stack) { return false; }

    @Override public int getEnchantmentValue(@Nonnull ItemStack stack) { return 0; }

    @Override public boolean isBookEnchantable(@Nonnull ItemStack stack, @Nonnull ItemStack book) { return false; }
}
