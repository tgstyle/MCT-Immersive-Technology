package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperDummy;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockPartBlock;
import mctmods.immersivetechnology.common.blocks.helper.ITBlockInterfaces;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class ITMultiblockPartBlock<S extends IMultiblockState> extends MultiblockPartBlock<S> {
    public ITMultiblockPartBlock(Properties properties, MultiblockRegistration<S> multiblock) { super(properties, multiblock); }

    @Nonnull
    @Override
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof ITBlockInterfaces.IPlayerInteraction be) {
            Vec3 hitVec = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
            if (be.interact(hit.getDirection(), player, hand, player.getItemInHand(hand), (float) hitVec.x, (float) hitVec.y, (float) hitVec.z)) { return InteractionResult.sidedSuccess(level.isClientSide); }
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public float getDestroyProgress(@NotNull BlockState state, @NotNull Player player, @NotNull BlockGetter worldIn, @NotNull BlockPos pos) {
        if (!(worldIn instanceof Level level)) { return super.getDestroyProgress(state, player, worldIn, pos); }
        BlockEntity te = level.getBlockEntity(pos);
        if (!(te instanceof IMultiblockBE<?> mbe)) { return super.getDestroyProgress(state, player, worldIn, pos); }
        IMultiblockBEHelper<?> helper = mbe.getHelper();
        if (helper instanceof IMultiblockBEHelperDummy<?> dummy) {
            IMultiblockLevel mbLevel = Objects.requireNonNull(helper.getContext()).getLevel();
            BlockPos relPos = dummy.getPositionInMB();
            BlockPos offset = mbLevel.getOrientation().getAbsoluteOffset(relPos);
            BlockPos masterPos = pos.subtract(offset);
            BlockState masterState = level.getBlockState(masterPos);
            return masterState.getDestroyProgress(player, worldIn, masterPos);
        }
        return super.getDestroyProgress(state, player, worldIn, pos);
    }
}
