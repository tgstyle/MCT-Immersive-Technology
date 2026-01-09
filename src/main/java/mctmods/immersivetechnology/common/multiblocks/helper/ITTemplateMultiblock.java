package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.multiblocks.BlockMatcher;
import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityDummy;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import com.google.common.base.Preconditions;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.core.ITServerConfig;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public abstract class ITTemplateMultiblock extends TemplateMultiblock {
    public static final int DISASSEMBLE_QUEUE_SIZE = 8;
    private final MultiblockRegistration<?> logic;

    public static final List<ITQueueProcessor> pendingQueues = new ArrayList<>();

    public ITTemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size, MultiblockRegistration<?> logic) {
        super(loc, masterFromOrigin, triggerFromOrigin, size);
        this.logic = logic;
    }

    public ITTemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size, List<BlockMatcher.MatcherPredicate> additionalPredicates, MultiblockRegistration<?> logic) {
        super(loc, masterFromOrigin, triggerFromOrigin, size, additionalPredicates);
        this.logic = logic;
    }

    @Override public float getManualScale() { return 0; }

    @Override protected void replaceStructureBlock(StructureTemplate.StructureBlockInfo info, Level world, BlockPos actualPos, boolean mirrored, Direction clickDirection, Vec3i offsetFromMaster) {
        BlockState newState = this.logic.block().get().defaultBlockState();
        newState = newState.setValue(ITProperties.MULTIBLOCKSLAVE, !offsetFromMaster.equals(Vec3i.ZERO));
        if (newState.hasProperty(ITProperties.MIRRORED)) { newState = newState.setValue(ITProperties.MIRRORED, mirrored); }
        if (newState.hasProperty(ITProperties.FACING_HORIZONTAL)) { newState = newState.setValue(ITProperties.FACING_HORIZONTAL, clickDirection.getOpposite()); }
        if (newState.hasProperty(ITProperties.ACTIVE)) { newState = newState.setValue(ITProperties.ACTIVE, false); }
        BlockState oldState = world.getBlockState(actualPos);
        world.setBlock(actualPos, newState, 3);
        BlockEntity curr = world.getBlockEntity(actualPos);
        if (curr instanceof MultiblockBlockEntityDummy<?> dummy) { dummy.getHelper().setPositionInMB(info.pos()); }
        else if (!(curr instanceof MultiblockBlockEntityMaster)) { ITLib.IT_LOGGER.error("Expected MB TE at {} during placement", actualPos); }
        LevelChunk chunk = world.getChunkAt(actualPos);
        world.markAndNotifyBlock(actualPos, chunk, oldState, newState, 3, 512);
    }

    @SuppressWarnings("deprecation")
    @Override public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) {
        if (world.isClientSide) { return; }
        Mirror mirror = mirrored ? Mirror.FRONT_BACK : Mirror.NONE;
        Rotation rot = DirectionUtils.getRotationBetweenFacings(Direction.NORTH, clickDirectionAtCreation);
        Preconditions.checkNotNull(rot);
        List<ItemStack> allDrops = new ArrayList<>();
        Consumer<ItemStack> addToDrops = stack -> { if (!stack.isEmpty()) { allDrops.add(stack); } };
        if (world instanceof ServerLevel serverLevel) {
            boolean templateMode = ITServerConfig.DISASSEMBLY_MODE.get() == ITServerConfig.DisassemblyMode.TEMPLATE_BLOCKS;
            boolean doTileDrops = serverLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS);
            BlockPos masterPos = withSettingsAndOffset(origin, masterFromOrigin, mirror, rot);
            ServerPlayer breakingPlayer = (ServerPlayer) serverLevel.getNearestPlayer(masterPos.getX() + 0.5, masterPos.getY() + 0.5, masterPos.getZ() + 0.5, -1.0, e -> true);
            boolean dropItems = doTileDrops;
            if (breakingPlayer != null && breakingPlayer.gameMode.getGameModeForPlayer() == GameType.CREATIVE) { dropItems = false; }
            IMultiblockBEHelperMaster<?> masterHelper = null;
            BlockEntity masterBE = world.getBlockEntity(masterPos);
            if (masterBE instanceof IMultiblockBE<?> mbBE && mbBE.getHelper() instanceof IMultiblockBEHelperMaster<?> h) { masterHelper = h; }
            if (masterHelper != null && dropItems) { dropInventory(masterHelper, addToDrops); }
            for (StructureBlockInfo block : getStructure(world)) { prepareBlockForDisassembly(world, withSettingsAndOffset(origin, block.pos(), mirror, rot)); }
            List<StructureBlockInfo> structure = new ArrayList<>(getTemplate(world).template().palettes.get(0).blocks());
            structure.sort(Comparator.comparingInt(a -> -a.pos().getY()));
            BlockPos brokenPos = masterPos;
            if (breakingPlayer != null) {
                Vec3 eyePos = breakingPlayer.getEyePosition();
                Vec3 look = breakingPlayer.getViewVector(1.0F);
                double reach = breakingPlayer.getAttributeValue(ForgeMod.BLOCK_REACH.get());
                Vec3 end = eyePos.add(look.scale(reach + 2));
                ClipContext ctx = new ClipContext(eyePos, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, breakingPlayer);
                BlockHitResult hit = serverLevel.clip(ctx);
                if (hit.getType() == HitResult.Type.BLOCK) {
                    BlockPos hitPos = hit.getBlockPos();
                    for (StructureBlockInfo info : structure) {
                        BlockPos actual = withSettingsAndOffset(origin, info.pos(), mirror, rot);
                        if (actual.equals(hitPos)) {
                            brokenPos = actual;
                            break;
                        }
                    }
                }
                if (brokenPos.equals(masterPos)) {
                    double minDist = Double.MAX_VALUE;
                    BlockPos closest = null;
                    for (StructureBlockInfo info : structure) {
                        BlockPos actual = withSettingsAndOffset(origin, info.pos(), mirror, rot);
                        double dist = Vec3.atCenterOf(actual).distanceToSqr(eyePos);
                        if (dist < minDist) {
                            minDist = dist;
                            closest = actual;
                        }
                    }
                    if (closest != null) { brokenPos = closest; }
                }
            }
            ItemStack tool = breakingPlayer != null ? breakingPlayer.getMainHandItem() : ItemStack.EMPTY;
            List<AbstractMap.SimpleEntry<BlockPos, BlockState>> toBreak = new ArrayList<>();
            for (StructureBlockInfo info : structure) {
                BlockPos actualPos = withSettingsAndOffset(origin, info.pos(), mirror, rot);
                BlockState stateAfterMirror = info.state().mirror(mirror);
                BlockState template = stateAfterMirror.rotate(rot);
                if (templateMode) {
                    if (dropItems && actualPos.equals(brokenPos)) {
                        List<ItemStack> drops = Block.getDrops(template, serverLevel, actualPos, null, breakingPlayer, tool);
                        allDrops.addAll(drops);
                    }
                    if (!actualPos.equals(brokenPos)) { world.setBlockAndUpdate(actualPos, template); }
                } else {
                    if (dropItems) {
                        List<ItemStack> drops = Block.getDrops(template, serverLevel, actualPos, null, breakingPlayer, tool);
                        allDrops.addAll(drops);
                    }
                    if (!actualPos.equals(brokenPos)) { toBreak.add(new AbstractMap.SimpleEntry<>(actualPos, template)); }
                }
            }
            for (ItemStack s : allDrops) { ITUtils.dropStackAtPos(world, brokenPos, s); }
            if (!templateMode) { pendingQueues.add(new ITQueueProcessor(world, toBreak, breakingPlayer)); }
        }
    }

    private <S extends IMultiblockState> void dropInventory(IMultiblockBEHelperMaster<S> helper, Consumer<ItemStack> dropIt) {
        helper.getMultiblock().logic().dropExtraItems(helper.getState(), dropIt);
    }

    @Override public Component getDisplayName() { return this.logic.block().get().getName(); }

    @Override public Block getBlock() { return this.logic.block().get(); }

    public Vec3i getSize(@Nullable Level world) { return this.size; }

    @Override public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { }

    @Nonnull public TemplateMultiblock.TemplateData getTemplate(@Nullable Level world) {
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
