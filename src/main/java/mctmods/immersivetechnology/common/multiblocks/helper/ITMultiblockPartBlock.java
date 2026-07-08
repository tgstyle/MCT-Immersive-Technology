package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockPartBlock;
import mctmods.immersivetechnology.common.blocks.helper.ITIBlockInterfaces;
import mctmods.immersivetechnology.core.util.inventory.ITIDropInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ITMultiblockPartBlock<S extends IMultiblockState> extends MultiblockPartBlock<S> {
    public ITMultiblockPartBlock(Properties properties, MultiblockRegistration<S> multiblock) { super(properties, multiblock); }

    @Nonnull @Override
    public ItemInteractionResult useItemOn(@Nonnull ItemStack stack, @Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof ITIBlockInterfaces.IPlayerInteraction be) {
            Vec3 hitVec = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
            if (be.interact(hit.getDirection(), player, hand, stack, (float) hitVec.x, (float) hitVec.y, (float) hitVec.z)) {
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override @Nonnull
    public BlockState playerWillDestroy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Player player) {
        if (!level.isClientSide) {
            BlockEntity te = level.getBlockEntity(pos);
            if (te instanceof IMultiblockBE<?> be) {
                var helper = be.getHelper();
                if (!((ITIMultiblockBEHelper)helper).it$isDisassembling()) {
                    if (te instanceof ITIDropInventory dropInv) {
                        dropInv.getDroppedItems().forEach(stack -> {
                            if (!stack.isEmpty()) {
                                ItemEntity item = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
                                level.addFreshEntity(item);
                            }
                        });
                    }
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override @Nonnull
    public List<ItemStack> getDrops(@Nonnull BlockState state, @Nonnull LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, builder));
        BlockEntity te = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (te instanceof ITIDropInventory dropInv) {
            dropInv.getDroppedItems().forEach(drops::add);
        }
        return drops;
    }
}
