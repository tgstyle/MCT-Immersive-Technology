package mctmods.immersivetechnology.common.items;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import mctmods.immersivetechnology.common.blocks.helper.BlockInterfaces;
import mctmods.immersivetechnology.core.util.Advancements;
import mctmods.immersivetechnology.core.util.RotationUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
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

    @Override public int getMaxStackSize(ItemStack stack) { return 1; }

    @Override @NotNull public Component getName(@NotNull ItemStack pStack) { return Component.translatable(this.getDescriptionId(pStack)); }

    @Override public void appendHoverText(@NotNull ItemStack stack, @Nullable Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        addInfo(tooltip, Lib.DESC_INFO + "multiblocksAllowed", stack, "multiblockPermission");
        addInfo(tooltip, Lib.DESC_INFO + "multiblockForbidden", stack, "multiblockInterdiction");
    }

    private void addInfo(List<Component> list, String titleKey, ItemStack stack, String nbtKey) {
        if (!ItemNBTHelper.hasKey(stack, nbtKey, Tag.TAG_LIST)) { return; }
        MutableComponent title = Component.translatable(titleKey);
        ListTag tagList = stack.getOrCreateTag().getList(nbtKey, Tag.TAG_STRING);
        if (!Screen.hasShiftDown()) { list.add(title.append(" ").append(Component.translatable(Lib.DESC_INFO + "holdShift"))); }
        else {
            list.add(title);
            for (int i = 0; i < tagList.size(); i++) {
                ResourceLocation mbName = ResourceLocation.tryParse(tagList.getString(i));
                if (mbName == null) { continue; }
                MultiblockHandler.IMultiblock multiblock = MultiblockHandler.getByUniqueName(mbName);
                if (multiblock != null) { list.add(TextUtils.applyFormat(multiblock.getDisplayName(), ChatFormatting.DARK_GRAY)); }
            }
        }
    }

    @Override public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        Direction side = context.getClickedFace();
        List<ResourceLocation> permittedMultiblocks = null;
        List<ResourceLocation> interdictedMultiblocks = null;
        if (ItemNBTHelper.hasKey(stack, "multiblockPermission")) {
            ListTag list = stack.getOrCreateTag().getList("multiblockPermission", Tag.TAG_STRING);
            permittedMultiblocks = parseMultiblockNames(list, player, "permission");
            if (permittedMultiblocks == null) { return InteractionResult.FAIL; }
        }
        if (ItemNBTHelper.hasKey(stack, "multiblockInterdiction")) {
            ListTag list = stack.getOrCreateTag().getList("multiblockInterdiction", Tag.TAG_STRING);
            interdictedMultiblocks = parseMultiblockNames(list, player, "interdiction");
            if (interdictedMultiblocks == null) { return InteractionResult.FAIL; }
        }
        final Direction multiblockSide;
        if (side.getAxis() == Direction.Axis.Y && player != null) { multiblockSide = Direction.fromYRot(player.getYRot()).getOpposite(); }
        else { multiblockSide = side; }
        for (MultiblockHandler.IMultiblock mb : MultiblockHandler.getMultiblocks()) {
            if (mb.isBlockTrigger(world.getBlockState(pos), multiblockSide, world)) {
                boolean isAllowed;
                if (permittedMultiblocks != null) { isAllowed = permittedMultiblocks.contains(mb.getUniqueName()); }
                else if (interdictedMultiblocks != null) { isAllowed = !interdictedMultiblocks.contains(mb.getUniqueName()); }
                else { isAllowed = true; }
                if (!isAllowed) { continue; }
                if (MultiblockHandler.postMultiblockFormationEvent(player, mb, pos, stack).isCanceled()) { continue; }
                boolean formed = mb.createStructure(world, pos, multiblockSide, player);
                if (formed) {
                    if (player instanceof ServerPlayer sPlayer) { Advancements.TRIGGER_MULTIBLOCK.trigger(sPlayer, mb, stack); }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof BlockInterfaces.IConfigurableSides sideConfig) {
            Direction activeSide = ((player != null) && player.isShiftKeyDown()) ? side.getOpposite() : side;
            assert player != null;
            if (sideConfig.toggleSide(activeSide, player)) { return InteractionResult.SUCCESS; }
            else { return InteractionResult.FAIL; }
        }
        else {
            boolean rotate = !(tile instanceof BlockInterfaces.IDirectionalBE) && !(tile instanceof BlockInterfaces.IHammerInteraction);
            if (!rotate && tile instanceof BlockInterfaces.IDirectionalBE dirBE) {
                assert player != null;
                rotate = dirBE.canHammerRotate(side, context.getClickLocation().subtract(Vec3.atLowerCornerOf(pos)), player); }
            if (rotate && RotationUtil.rotateBlock(world, pos, player != null && (player.isShiftKeyDown() != side.equals(Direction.DOWN)))) { return InteractionResult.SUCCESS; }
            else if (!rotate && tile instanceof BlockInterfaces.IHammerInteraction hammerInteraction) {
                assert player != null;
                if (hammerInteraction.hammerUseSide(side, player, context.getHand(), context.getClickLocation())) { return InteractionResult.SUCCESS; }
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable private static List<ResourceLocation> parseMultiblockNames(ListTag data, @Nullable Player player, String prefix) {
        List<ResourceLocation> result = new ArrayList<>();
        for (int i = 0; i < data.size(); ++i) {
            String listEntry = data.getString(i);
            ResourceLocation asRL = ResourceLocation.tryParse(listEntry);
            if (asRL == null || MultiblockHandler.getByUniqueName(asRL) == null) {
                if (player != null && !player.getCommandSenderWorld().isClientSide) { player.displayClientMessage(Component.literal("Invalid " + prefix + " entry: " + listEntry), false); }
                return null;
            }
            result.add(asRL);
        }
        return result;
    }

    @Override public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) { return true; }

    @Override @NotNull public InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
        if (!player.level().isClientSide && RotationUtil.rotateEntity(entity)) { return InteractionResult.SUCCESS; }
        return InteractionResult.PASS;
    }

    @Override public boolean hasCraftingRemainingItem(@NotNull ItemStack stack) { return true; }

    @Override @NotNull public ItemStack getCraftingRemainingItem(@NotNull ItemStack stack) {
        ItemStack container = stack.copy();
        if (container.hurt(1, ApiUtils.RANDOM_SOURCE, null)) { return ItemStack.EMPTY; }
        else { return container; }
    }

    @Override public boolean isEnchantable(@NotNull ItemStack stack) { return false; }

    @Override public int getEnchantmentValue(ItemStack stack) { return 0; }

    @Override public boolean isBookEnchantable(ItemStack stack, ItemStack book) { return false; }

    @Override public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) { return false; }
}
