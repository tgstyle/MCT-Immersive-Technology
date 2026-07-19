package mctmods.immersivetechnology.common.multiblocks.helper;

import mctmods.immersivetechnology.common.blocks.helper.BlockInterfaces;
import mctmods.immersivetechnology.core.util.Utils;
import mctmods.immersivetechnology.core.util.inventory.IDropInventory;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockPartBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ModMultiblockPartBlock<S extends IMultiblockState> extends MultiblockPartBlock<S> {
    private final MultiblockRegistration<S> multiblockRef;

    public ModMultiblockPartBlock(Properties properties, MultiblockRegistration<S> multiblock) { super(properties, multiblock); this.multiblockRef = multiblock; }

    @Override public int getLightBlock(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos) { return 0; }

    @Override public boolean propagatesSkylightDown(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos) { return true; }

    @Override public boolean isLadder(@Nonnull BlockState state, @Nonnull LevelReader level, @Nonnull BlockPos pos, @Nullable LivingEntity entity) {
        if (level.getBlockEntity(pos) instanceof IMultiblockBE<?> be && multiblockRef.logic() instanceof BlockInterfaces.ILadderPositionProvider ladderLogic) {
            BlockPos posInMB = be.getHelper().getPositionInMB();
            return posInMB != null && ladderLogic.isLadderPos(posInMB);
        }
        return false;
    }

    @Override public void entityInside(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (entity instanceof LivingEntity && isLadder(state, level, pos, (LivingEntity) entity)) { Utils.applyLadderLogic(entity); }
    }

    @Nonnull @Override
    public ItemInteractionResult useItemOn(@Nonnull ItemStack stack, @Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof BlockInterfaces.IPlayerInteraction be) {
            Vec3 hitVec = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
            if (be.interact(hit.getDirection(), player, hand, stack, (float) hitVec.x, (float) hitVec.y, (float) hitVec.z)) {
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override @Nonnull public BlockState playerWillDestroy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Player player) {
        if (!level.isClientSide) {
            BlockEntity te = level.getBlockEntity(pos);
            if (te instanceof IMultiblockBE<?> be) {
                var helper = be.getHelper();
                if (((IDisassemblingAware) helper).it$isDisassembling()) {
                    if (te instanceof IDropInventory dropInv) {
                        dropInv.getDroppedItems().forEach(stack -> {
                            if (!stack.isEmpty()) {
                                ItemEntity item = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
                                level.addFreshEntity(item);
                            }
                        });
                    }
                    if (!(player instanceof FakePlayer)) {
                        ModTemplateMultiblock.currentlyBreakingPos = pos.immutable();
                        ModTemplateMultiblock.sneakBreaking = player.isShiftKeyDown();
                        try { return super.playerWillDestroy(level, pos, state, player); }
                        finally { ModTemplateMultiblock.currentlyBreakingPos = null; }
                    }
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override public void onRemove(BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, BlockState newState, boolean isMoving) {
        try {
            if (!level.isClientSide && state.getBlock() != newState.getBlock()) {
                BlockEntity te = level.getBlockEntity(pos);
                if (te instanceof IMultiblockBE<?> be && ((IDisassemblingAware) be.getHelper()).it$isDisassembling()) {
                    ModTemplateMultiblock.currentlyBreakingPos = pos.immutable();
                    try { super.onRemove(state, level, pos, newState, isMoving); }
                    finally { ModTemplateMultiblock.currentlyBreakingPos = null; }
                    return;
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
        finally { ModTemplateMultiblock.sneakBreaking = false; }
    }

    @Override @Nonnull
    public List<ItemStack> getDrops(@Nonnull BlockState state, @Nonnull LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, builder));
        BlockEntity te = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (te instanceof IDropInventory dropInv) {
            dropInv.getDroppedItems().forEach(drops::add);
        }
        return drops;
    }
}
