package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import mctmods.immersivetechnology.common.blocks.helper.BlockInterfaces;
import com.immersiveconvergence.api.client.split.IModelOffsetProvider;
import com.immersiveconvergence.api.client.split.SplitModelProperties;
import mctmods.immersivetechnology.common.blocks.helper.ModProperties;
import mctmods.immersivetechnology.core.util.inventory.IDropInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MultiblockBlockEntityCommon<State extends IMultiblockState> implements BlockInterfaces.IPlayerInteraction, IDropInventory, IModelOffsetProvider {
    private final MultiblockRegistration<State> multiblock;
    private final Supplier<IMultiblockBEHelper<State>> helperSupplier;
    private final Supplier<Level> levelSupplier;

    public MultiblockBlockEntityCommon(MultiblockRegistration<State> multiblock, Supplier<IMultiblockBEHelper<State>> helperSupplier, Supplier<Level> levelSupplier) {
        this.multiblock = multiblock;
        this.helperSupplier = helperSupplier;
        this.levelSupplier = levelSupplier;
    }

    @Override public boolean interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
        IMultiblockContext<State> ctx = helperSupplier.get().getContext();
        BlockPos posInMultiblock = helperSupplier.get().getPositionInMB();
        Vec3 hitVec = new Vec3(hitX, hitY, hitZ);
        BlockHitResult absoluteHit = new BlockHitResult(hitVec, side, BlockPos.ZERO, false);
        boolean isClient = levelSupplier.get().isClientSide;
        InteractionResult result = InteractionResult.PASS;
        for (MultiblockRegistration.ExtraComponent<State, ?> extra : multiblock.extraComponents()) {
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

    @Override public Stream<ItemStack> getDroppedItems() {
        List<ItemStack> drops = new ArrayList<>();
        State state = helperSupplier.get().getState();
        if (state != null) { multiblock.logic().dropExtraItems(state, drops::add); }
        return drops.stream();
    }

    @Override public BlockPos getModelOffset(BlockState state, Vec3i size) {
        BlockPos posInMB = helperSupplier.get().getPositionInMB();
        BlockPos masterOffset = multiblock.masterPosInMB();
        BlockPos offset = posInMB;
        if (state != null && state.hasProperty(ModProperties.MIRRORED) && state.getValue(ModProperties.MIRRORED)) {
            Level level = levelSupplier.get();
            if (level != null) {
                Vec3i realSize = helperSupplier.get().getSize(level);
                if (realSize != null) {
                    int x = realSize.getX() - 1 - posInMB.getX();
                    offset = new BlockPos(x, posInMB.getY(), posInMB.getZ());
                }
            }
        }
        offset = offset.subtract(masterOffset);
        return offset;
    }

    public ModelData getModelData() {
        BlockPos offset = getModelOffset(null, Vec3i.ZERO);
        if (offset != null) { return ModelData.builder().with(SplitModelProperties.SUBMODEL_OFFSET, offset).build(); }
        return ModelData.EMPTY;
    }
}
