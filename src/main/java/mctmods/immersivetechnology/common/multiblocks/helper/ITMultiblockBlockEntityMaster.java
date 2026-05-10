package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import mctmods.immersivetechnology.common.blocks.helper.ITBlockInterfaces;
import mctmods.immersivetechnology.common.blocks.helper.ITModelOffsetProvider;
import mctmods.immersivetechnology.core.util.inventory.IITDropInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.List;
import java.util.stream.Stream;

public class ITMultiblockBlockEntityMaster<State extends IMultiblockState> extends MultiblockBlockEntityMaster<State> implements ITBlockInterfaces.IPlayerInteraction, IITDropInventory, ITModelOffsetProvider {
    private final ITMultiblockBlockEntityCommon<State> common;

    public List<AbstractMap.SimpleEntry<BlockPos, BlockState>> disassembleQueue = null;

    public ITMultiblockBlockEntityMaster(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState, MultiblockRegistration<State> multiblock) {
        super(type, worldPosition, blockState, multiblock);
        this.common = new ITMultiblockBlockEntityCommon<>(multiblock, this::getHelper, this::getLevel);
    }

    @Override public boolean interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
        return common.interact(side, player, hand, heldItem, hitX, hitY, hitZ);
    }

    @Override public Stream<ItemStack> getDroppedItems() {
        return common.getDroppedItems();
    }

    @Override public BlockPos getModelOffset(BlockState state, Vec3i size) {
        return common.getModelOffset(state, size);
    }

    @Override @NotNull public ModelData getModelData() {
        return common.getModelData();
    }

    @Override public AABB getRenderBoundingBox() {
        IMultiblockContext<State> ctx = getHelper().getContext();
        BlockPos min = ctx.getLevel().toAbsolute(BlockPos.ZERO);
        Vec3i size = getHelper().getSize(ctx.getLevel().getRawLevel());
        BlockPos max = ctx.getLevel().toAbsolute(new BlockPos(size.getX() - 1, size.getY() - 1, size.getZ() - 1));
        return new AABB(min, max.offset(1, 1, 1)).inflate(1);
    }
}
