package mctmods.immersivetechnology.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import mctmods.immersivetechnology.common.blocks.helper.ITIBlockInterfaces;
import mctmods.immersivetechnology.core.util.ITAdvancements;
import mctmods.immersivetechnology.core.util.ITRotationUtil;
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
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FormationTool extends Item {
    public FormationTool() { super(new Properties()); }

    @Override public int getMaxStackSize(@NotNull ItemStack stack) { return 1; }

    @Override @NotNull public Component getName(@NotNull ItemStack pStack) { return Component.translatable(this.getDescriptionId(pStack)); }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        addInfo(tooltipComponents, Lib.DESC_INFO + "multiblocksAllowed", stack, "multiblockPermission");
        addInfo(tooltipComponents, Lib.DESC_INFO + "multiblockForbidden", stack, "multiblockInterdiction");
    }

    private void addInfo(List<Component> list, String titleKey, ItemStack stack, String nbtKey) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (!tag.contains(nbtKey, Tag.TAG_LIST)) return;

        MutableComponent title = Component.translatable(titleKey);
        ListTag tagList = tag.getList(nbtKey, Tag.TAG_STRING);

        if (!Screen.hasShiftDown()) {
            list.add(title.append(" ").append(Component.translatable(Lib.DESC_INFO + "holdShift")));
        } else {
            list.add(title);
            for (int i = 0; i < tagList.size(); i++) {
                ResourceLocation mbName = ResourceLocation.tryParse(tagList.getString(i));
                if (mbName == null) continue;
                MultiblockHandler.IMultiblock multiblock = MultiblockHandler.getByUniqueName(mbName);
                if (multiblock != null) {
                    list.add(TextUtils.applyFormat(multiblock.getDisplayName(), ChatFormatting.DARK_GRAY));
                }
            }
        }
    }

    @Override @NotNull public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
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

        for (MultiblockHandler.IMultiblock mb : MultiblockHandler.getMultiblocks()) {
            if (mb.isBlockTrigger(world.getBlockState(pos), multiblockSide, world)) {
                boolean isAllowed = true;
                if (permittedMultiblocks != null) isAllowed = permittedMultiblocks.contains(mb.getUniqueName());
                else if (interdictedMultiblocks != null) isAllowed = !interdictedMultiblocks.contains(mb.getUniqueName());

                if (!isAllowed) continue;
                if (MultiblockHandler.postMultiblockFormationEvent(player, mb, pos, stack).isCanceled()) continue;

                boolean formed = mb.createStructure(world, pos, multiblockSide, player);
                if (formed) {
                    if (player instanceof ServerPlayer sPlayer) {
                        ITAdvancements.TRIGGER_MULTIBLOCK.trigger(sPlayer, mb, stack);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }

        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ITIBlockInterfaces.IConfigurableSides sideConfig) {
            Direction activeSide = (player != null && player.isShiftKeyDown()) ? side.getOpposite() : side;
            if (sideConfig.toggleSide(activeSide, player)) return InteractionResult.SUCCESS;
            return InteractionResult.FAIL;
        } else {
            boolean rotate = !(tile instanceof ITIBlockInterfaces.IDirectionalBE) && !(tile instanceof ITIBlockInterfaces.IHammerInteraction);
            if (!rotate && tile instanceof ITIBlockInterfaces.IDirectionalBE dirBE) {
                rotate = dirBE.canHammerRotate(side, context.getClickLocation().subtract(Vec3.atLowerCornerOf(pos)), player);
            }
            if (rotate && ITRotationUtil.rotateBlock(world, pos, player != null && (player.isShiftKeyDown() != side.equals(Direction.DOWN)))) {
                return InteractionResult.SUCCESS;
            } else if (!rotate && tile instanceof ITIBlockInterfaces.IHammerInteraction hammerInteraction) {
                if (hammerInteraction.hammerUseSide(side, player, context.getHand(), context.getClickLocation())) {
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable
    private static List<ResourceLocation> parseMultiblockNames(ListTag data, @Nullable Player player, String prefix) {
        List<ResourceLocation> result = new ArrayList<>();
        for (int i = 0; i < data.size(); ++i) {
            String listEntry = data.getString(i);
            ResourceLocation asRL = ResourceLocation.tryParse(listEntry);
            if (asRL == null || MultiblockHandler.getByUniqueName(asRL) == null) {
                if (player != null && !player.level().isClientSide) {
                    player.displayClientMessage(Component.literal("Invalid " + prefix + " entry: " + listEntry), false);
                }
                return null;
            }
            result.add(asRL);
        }
        return result;
    }

    @Override public boolean doesSneakBypassUse(@NotNull ItemStack stack, @NotNull LevelReader world, @NotNull BlockPos pos, @NotNull Player player) { return true; }

    @Override @NotNull public InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
        if (!player.level().isClientSide && ITRotationUtil.rotateEntity(entity)) return InteractionResult.SUCCESS;
        return InteractionResult.PASS;
    }

    @Override public boolean hasCraftingRemainingItem(@NotNull ItemStack stack) { return true; }

    @Override @NotNull public ItemStack getCraftingRemainingItem(@NotNull ItemStack stack) {
        ItemStack container = stack.copy();
        int newDamage = container.getDamageValue() + 1;
        if (newDamage >= container.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        container.setDamageValue(newDamage);
        return container;
    }

    @Override public boolean isEnchantable(@NotNull ItemStack stack) { return false; }

    @Override public int getEnchantmentValue(@NotNull ItemStack stack) { return 0; }

    @Override public boolean isBookEnchantable(@NotNull ItemStack stack, @NotNull ItemStack book) { return false; }
}
