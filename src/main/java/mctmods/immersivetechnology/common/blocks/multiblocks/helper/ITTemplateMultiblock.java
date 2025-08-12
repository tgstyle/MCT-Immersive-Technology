package mctmods.immersivetechnology.common.blocks.multiblocks.helper;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityDummy;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.base.Preconditions;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public abstract class ITTemplateMultiblock extends TemplateMultiblock {
    private final MultiblockRegistration<?> logic;

    public ITTemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size, MultiblockRegistration<?> logic) {
        super(loc, masterFromOrigin, triggerFromOrigin, size);
        this.logic = logic;
    }

    @Override
    public boolean createStructure(Level world, BlockPos pos, Direction side, Player player) { return super.createStructure(world, pos, side, player); }

    @Override
    public float getManualScale() { return 0; }

    @Override
    protected void replaceStructureBlock(StructureTemplate.StructureBlockInfo info, Level world, BlockPos actualPos, boolean mirrored, Direction clickDirection, Vec3i offsetFromMaster) {
        BlockState newState = this.logic.block().get().defaultBlockState();
        newState = newState.setValue(IEProperties.MULTIBLOCKSLAVE, !offsetFromMaster.equals(Vec3i.ZERO));
        if (newState.hasProperty(IEProperties.MIRRORED)) { newState = newState.setValue(IEProperties.MIRRORED, mirrored); }
        if (newState.hasProperty(IEProperties.FACING_HORIZONTAL)) { newState = newState.setValue(IEProperties.FACING_HORIZONTAL, clickDirection.getOpposite()); }
        BlockState oldState = world.getBlockState(actualPos);
        world.setBlock(actualPos, newState, 0);
        BlockEntity curr = world.getBlockEntity(actualPos);
        if (curr instanceof MultiblockBlockEntityDummy<?> dummy) { dummy.getHelper().setPositionInMB(info.pos()); }
        else if (!(curr instanceof MultiblockBlockEntityMaster)) { ITLib.IT_LOGGER.error("Expected MB TE at {} during placement", actualPos); }
        LevelChunk chunk = world.getChunkAt(actualPos);
        world.markAndNotifyBlock(actualPos, chunk, oldState, newState, 3, 512);
    }

    @Override
    public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) {
        if (world.isClientSide) { return; }
        Mirror mirror = mirrored ? Mirror.FRONT_BACK : Mirror.NONE;
        Rotation rot = DirectionUtils.getRotationBetweenFacings(Direction.NORTH, clickDirectionAtCreation);
        Preconditions.checkNotNull(rot);
        List<ItemStack> allDrops = new ArrayList<>();
        Consumer<ItemStack> addToDrops = stack -> { if (!stack.isEmpty()) { allDrops.add(stack); } };
        if (world instanceof ServerLevel serverLevel) {
            BlockPos masterPos = withSettingsAndOffset(origin, masterFromOrigin, mirror, rot);
            Player breakingPlayer = serverLevel.getNearestPlayer(masterPos.getX() + 0.5, masterPos.getY() + 0.5, masterPos.getZ() + 0.5, -1.0, e -> true);
            IMultiblockBEHelperMaster<?> masterHelper = null;
            BlockEntity masterBE = world.getBlockEntity(masterPos);
            if (masterBE instanceof IMultiblockBE<?> mbBE && mbBE.getHelper() instanceof IMultiblockBEHelperMaster<?> h) { masterHelper = h; }
            if (masterHelper != null) { dropInventory(masterHelper, addToDrops); }
            for (StructureBlockInfo block : getStructure(world)) { prepareBlockForDisassembly(world, withSettingsAndOffset(origin, block.pos(), mirror, rot)); }
            List<StructureTemplate.StructureBlockInfo> structure = new ArrayList<>(getStructure(world));
            structure.sort(Comparator.comparingInt(a -> -a.pos().getY()));
            for (StructureTemplate.StructureBlockInfo info : structure) {
                BlockPos actualPos = withSettingsAndOffset(origin, info.pos(), mirror, rot);
                BlockState stateAfterMirror = info.state().mirror(mirror);
                BlockState templateState = stateAfterMirror.rotate(serverLevel, actualPos, rot);
                world.setBlockAndUpdate(actualPos, templateState);
                List<ItemStack> drops;
                if (breakingPlayer != null) { drops = Block.getDrops(templateState, serverLevel, actualPos, null, breakingPlayer, breakingPlayer.getMainHandItem()); }
                else { drops = Block.getDrops(templateState, serverLevel, actualPos, null); }
                world.destroyBlock(actualPos, false);
                for (ItemStack s : drops) { addToDrops.accept(s); }
            }
            BlockPos dropPos = origin;
            if (breakingPlayer != null) {
                BlockPos playerPos = breakingPlayer.blockPosition();
                double minDist = Double.MAX_VALUE;
                BlockPos closest = null;
                for (StructureTemplate.StructureBlockInfo info : structure) {
                    BlockPos actual = withSettingsAndOffset(origin, info.pos(), mirror, rot);
                    double dist = actual.distSqr(playerPos);
                    if (dist < minDist) {
                        minDist = dist;
                        closest = actual;
                    }
                }
                if (closest != null) { dropPos = closest; }
            }
            for (ItemStack s : allDrops) { Utils.dropStackAtPos(world, dropPos, s); }
        }
    }

    private <S extends IMultiblockState> void dropInventory(IMultiblockBEHelperMaster<S> helper, Consumer<ItemStack> dropIt) {
        helper.getMultiblock().logic().dropExtraItems(helper.getState(), dropIt);
    }

    @Override
    public Component getDisplayName() { return this.logic.block().get().getName(); }

    @Override
    public Block getBlock() { return this.logic.block().get(); }

    public Vec3i getSize(@Nullable Level world) { return this.size; }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { }

    @Nonnull
    public TemplateMultiblock.TemplateData getTemplate(@Nullable Level world) {
        assert world != null;
        TemplateMultiblock.TemplateData result = super.getTemplate(world);
        Vec3i resultSize = result.template().getSize();
        Preconditions.checkState(resultSize.equals(this.size), "Wrong template size for multiblock %s, template size: %s", this.getTemplateLocation(), resultSize);
        return result;
    }

    protected void prepareBlockForDisassembly(Level world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof IMultiblockBE<?> multiblockBE) { multiblockBE.getHelper().markDisassembling(); }
        else if (be != null) { ITLib.IT_LOGGER.error("Expected multiblock BE at {}, got {}", pos, be); }
    }
}
