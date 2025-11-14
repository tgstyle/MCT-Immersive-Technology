package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.common.util.inventory.IDropInventory;
import mctmods.immersivetechnology.common.blocks.helper.ITBlockInterfaces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ITMultiblockBlockEntityMaster<State extends IMultiblockState> extends MultiblockBlockEntityMaster<State> implements ITBlockInterfaces.IPlayerInteraction, IDropInventory {
    public List<AbstractMap.SimpleEntry<BlockPos, BlockState>> disassembleQueue = null;

    public ITMultiblockBlockEntityMaster(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState, MultiblockRegistration<State> multiblock) { super(type, worldPosition, blockState, multiblock); }

    @Override
    public AABB getRenderBoundingBox() {
        IMultiblockContext<State> ctx = getHelper().getContext();
        BlockPos min = ctx.getLevel().toAbsolute(BlockPos.ZERO);
        Vec3i size = getHelper().getSize(ctx.getLevel().getRawLevel());
        BlockPos max = ctx.getLevel().toAbsolute(new BlockPos(size.getX() - 1, size.getY() - 1, size.getZ() - 1));
        return new AABB(min, max.offset(1, 1, 1)).inflate(1);
    }

    @Override
    public boolean interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
        IMultiblockContext<State> ctx = getHelper().getContext();
        BlockPos posInMultiblock = getHelper().getPositionInMB();
        Vec3 hitVec = new Vec3(hitX, hitY, hitZ);
        BlockHitResult absoluteHit = new BlockHitResult(hitVec, side, getBlockPos(), false);
        assert this.level != null;
        boolean isClient = this.level.isClientSide;
        InteractionResult result = InteractionResult.PASS;
        for (MultiblockRegistration.ExtraComponent<State, ?> extra : getHelper().getMultiblock().extraComponents()) {
            @SuppressWarnings("unchecked")
            IMultiblockComponent<State> component = (IMultiblockComponent<State>) extra.component();
            InteractionResult componentResult = component.click(ctx, posInMultiblock, player, hand, absoluteHit, isClient);
            if (componentResult.consumesAction()) {
                result = componentResult;
                break;
            }
        }
        return result.consumesAction();
    }

    @Override
    public Stream<ItemStack> getDroppedItems() {
        List<ItemStack> drops = new ArrayList<>();
        getHelper().getMultiblock().logic().dropExtraItems(getHelper().getState(), drops::add);
        return drops.stream();
    }
}
