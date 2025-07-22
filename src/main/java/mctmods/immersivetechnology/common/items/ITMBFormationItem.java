package mctmods.immersivetechnology.common.items;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.advancements.IEAdvancements;
import mctmods.immersivetechnology.common.blocks.multiblocks.ITTemplateMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import mctmods.immersivetechnology.core.lib.ITLib; // Add this import for MODID

public class ITMBFormationItem extends Item {
    public ITMBFormationItem() {
        super(new Item.Properties());
    }

    @Override
    public int getMaxStackSize(ItemStack stack) { return 1; }

    @Override
    public @NotNull Component getName(@NotNull ItemStack pStack) { return Component.translatable(this.getDescriptionId(pStack)); }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
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
            if (mb instanceof ITTemplateMultiblock) {
                if (mb.isBlockTrigger(world.getBlockState(pos), multiblockSide, world)) {
                    boolean isAllowed;
                    if (permittedMultiblocks != null) { isAllowed = permittedMultiblocks.contains(mb.getUniqueName()); }
                    else if (interdictedMultiblocks != null) { isAllowed = !interdictedMultiblocks.contains(mb.getUniqueName()); }
                    else { isAllowed = true; }
                    if (!isAllowed) { continue; }
                    if (MultiblockHandler.postMultiblockFormationEvent(player, mb, pos, stack).isCanceled()) { continue; }
                    if (mb.createStructure(world, pos, multiblockSide, player)) {
                        if (player instanceof ServerPlayer sPlayer) { IEAdvancements.TRIGGER_MULTIBLOCK.trigger(sPlayer, mb, stack); }
                        assert player != null;
                        stack.hurtAndBreak(1, player, (p) -> { });
                        return InteractionResult.SUCCESS;
                    }
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
                if (player != null && !player.getCommandSenderWorld().isClientSide) { player.displayClientMessage(Component.literal("Invalid " + prefix + " entry: " + listEntry), false); }
                return null;
            }
            result.add(asRL);
        }
        return result;
    }
}
