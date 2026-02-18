package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockPartBlock;
import mctmods.immersivetechnology.common.blocks.helper.ITBlockInterfaces;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ITMultiblockPartBlock<S extends IMultiblockState> extends MultiblockPartBlock<S> {
    public ITMultiblockPartBlock(Properties properties, MultiblockRegistration<S> multiblock) { super(properties, multiblock); }

    @Nonnull @Override public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof ITBlockInterfaces.IPlayerInteraction be) {
            Vec3 hitVec = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
            if (be.interact(hit.getDirection(), player, hand, player.getItemInHand(hand), (float) hitVec.x, (float) hitVec.y, (float) hitVec.z)) { return InteractionResult.sidedSuccess(level.isClientSide); }
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override public void playerWillDestroy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Player player) {
        if (level.isClientSide) { return; }
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof IMultiblockBE<?> be) {
            var helper = be.getHelper();
            if (((ITMultiblockBEHelper)helper).it$isDisassembling()) {
                super.playerWillDestroy(level, pos, state, player);
                return;
            }
            if (helper.getContext() != null && ((ITMultiblockBEHelper)helper).it$isAssembled() && !(player instanceof FakePlayer)) { helper.disassemble(); }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override public void onRemove(BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity te = level.getBlockEntity(pos);
            if (te instanceof IMultiblockBE<?> be) {
                var helper = be.getHelper();
                if (((ITMultiblockBEHelper)helper).it$isDisassembling()) {
                    super.onRemove(state, level, pos, newState, isMoving);
                    return;
                }
                if (helper.getContext() != null && ((ITMultiblockBEHelper)helper).it$isAssembled()) { helper.disassemble(); }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @SuppressWarnings("deprecation")
    @Override @Nonnull public List<ItemStack> getDrops(@Nonnull BlockState state, @Nonnull LootParams.Builder builder) {
        BlockEntity te = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (te instanceof IMultiblockBE<?> be) {
            var helper = be.getHelper();
            if (helper.getContext() != null || ((ITMultiblockBEHelper)helper).it$isDisassembling()) { return new ArrayList<>(); }
        }
        return super.getDrops(state, builder);
    }
}
