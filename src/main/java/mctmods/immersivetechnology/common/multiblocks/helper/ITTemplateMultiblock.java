package mctmods.immersivetechnology.common.multiblocks.helper;

import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import mctmods.immersivetechnology.core.ITServerConfig;
import mctmods.immersivetechnology.core.lib.ITLib;

import blusunrize.immersiveengineering.api.multiblocks.BlockMatcher;
import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityDummy;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;

import static net.minecraft.world.level.block.Mirror.FRONT_BACK;

public abstract class ITTemplateMultiblock extends TemplateMultiblock {
    public static final int DISASSEMBLE_QUEUE_SIZE = 8;
    public static final List<ITQueueProcessor> pendingQueues = new ArrayList<>();
    private final MultiblockRegistration<?> logic;
    private List<StructureBlockInfo> sortedStructureBlocks;
    private Map<BlockPos, BlockState> triggerStateMap;

    public record TriggerPoint(BlockPos cell, Rotation offset) {}

    public ITTemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size, MultiblockRegistration<?> logic) {
        super(loc, masterFromOrigin, triggerFromOrigin, size);
        this.logic = logic;
    }

    public ITTemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size, List<BlockMatcher.MatcherPredicate> additionalPredicates, MultiblockRegistration<?> logic) {
        super(loc, masterFromOrigin, triggerFromOrigin, size, additionalPredicates);
        this.logic = logic;
    }

    protected List<TriggerPoint> getTriggerPoints() { return List.of(new TriggerPoint(this.triggerFromOrigin, Rotation.NONE)); }

    protected List<Mirror> getMirrorsToTry() { return canBeMirrored() ? List.of(Mirror.NONE, FRONT_BACK) : List.of(Mirror.NONE); }

    private void ensureCaches(TemplateMultiblock.TemplateData data) {
        if (sortedStructureBlocks != null) { return; }
        List<StructureBlockInfo> nonAir = data.blocksWithoutAir();
        sortedStructureBlocks = new ArrayList<>(data.template().palettes.get(0).blocks());
        sortedStructureBlocks.sort(Comparator.comparingInt(info -> -info.pos().getY()));
        triggerStateMap = new HashMap<>();
        for (TriggerPoint trigger : getTriggerPoints()) {
            for (StructureBlockInfo info : nonAir) {
                if (info.pos().equals(trigger.cell())) {
                    triggerStateMap.put(trigger.cell(), info.state());
                    break;
                }
            }
        }
    }

    @Override public float getManualScale() { return 0; }

    @Override public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { }

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
        Mirror mirror = mirrored ? FRONT_BACK : Mirror.NONE;
        Rotation rot = DirectionUtils.getRotationBetweenFacings(Direction.NORTH, clickDirectionAtCreation);
        Preconditions.checkNotNull(rot);
        if (world instanceof ServerLevel serverLevel) {
            boolean templateMode = ITServerConfig.DISASSEMBLY_MODE.get() == ITServerConfig.DisassemblyMode.TEMPLATE_BLOCKS;
            boolean doTileDrops = serverLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS);
            BlockPos masterPos = withSettingsAndOffset(origin, masterFromOrigin, mirror, rot);
            ServerPlayer breakingPlayer = (ServerPlayer) serverLevel.getNearestPlayer(masterPos.getX() + 0.5, masterPos.getY() + 0.5, masterPos.getZ() + 0.5, -1.0, e -> true);
            boolean dropItems = doTileDrops;
            if (breakingPlayer != null && breakingPlayer.gameMode.getGameModeForPlayer() == GameType.CREATIVE) { dropItems = false; }
            ItemStack tool = breakingPlayer != null ? breakingPlayer.getMainHandItem() : ItemStack.EMPTY;
            ItemStack effectiveTool = tool.isEmpty() ? new ItemStack(Items.DIAMOND_PICKAXE) : tool;
            IMultiblockBEHelperMaster<?> masterHelper = null;
            BlockEntity masterBE = world.getBlockEntity(masterPos);
            if (masterBE instanceof IMultiblockBE<?> mbBE && mbBE.getHelper() instanceof IMultiblockBEHelperMaster<?> h) { masterHelper = h; }

            if (masterBE instanceof ITIMultiblockBEHelper itBE && itBE.it$isDisassembling()) { return; }
            if (masterHelper instanceof ITIMultiblockBEHelper itH && itH.it$isDisassembling()) { return; }
            if (masterBE == null) { return; }

            if (masterBE instanceof ITIMultiblockBEHelper itBE) {
                itBE.it$markDisassembling();
            } else if (masterHelper instanceof ITIMultiblockBEHelper itH) {
                itH.it$markDisassembling();
            }

            getTemplate(world);

            List<StructureBlockInfo> structure = sortedStructureBlocks;

            for (StructureBlockInfo info : structure) {
                BlockPos actualPos = withSettingsAndOffset(origin, info.pos(), mirror, rot);
                prepareBlockForDisassembly(serverLevel, actualPos);
            }

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

            List<ItemStack> allDrops = new ArrayList<>();
            List<BlockPos> toBreak = new ArrayList<>();

            LootParams.Builder baseLootBuilder = dropItems ? new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.TOOL, effectiveTool)
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, breakingPlayer)
                    .withOptionalParameter(LootContextParams.BLOCK_ENTITY, null) : null;

            if (templateMode) {
                for (StructureBlockInfo info : structure) {
                    BlockPos actualPos = withSettingsAndOffset(origin, info.pos(), mirror, rot);
                    BlockState stateAfterMirror = info.state().mirror(mirror);
                    BlockState template = stateAfterMirror.rotate(rot);
                    serverLevel.setBlockAndUpdate(actualPos, template);
                }
            } else {
                for (StructureBlockInfo info : structure) {
                    BlockPos actualPos = withSettingsAndOffset(origin, info.pos(), mirror, rot);
                    BlockState stateAfterMirror = info.state().mirror(mirror);
                    BlockState template = stateAfterMirror.rotate(rot);
                    toBreak.add(actualPos);
                    if (dropItems) {
                        LootParams.Builder params = baseLootBuilder.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(actualPos));
                        allDrops.addAll(template.getDrops(params));
                    }
                }
            }

            if (!templateMode && !toBreak.isEmpty()) { pendingQueues.add(new ITQueueProcessor(serverLevel, toBreak, breakingPlayer, dropItems, brokenPos, allDrops)); }
        }
    }

    protected void prepareBlockForDisassembly(Level world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof ITIMultiblockBEHelper itHelper) { itHelper.it$markDisassembling(); }
        else if (be instanceof IMultiblockBE<?> multiblockBE) {
            var helper = multiblockBE.getHelper();
            if (helper instanceof ITIMultiblockBEHelper itHelper) { itHelper.it$markDisassembling(); }
        }
        else if (be != null) { ITLib.IT_LOGGER.error("Expected multiblock BE at {}, got {}", pos, be); }
    }

    @SuppressWarnings("deprecation")
    @Override public boolean isBlockTrigger(BlockState state, Direction d, @Nonnull Level world) {
        getTemplate(world);
        Rotation baseRot = DirectionUtils.getRotationBetweenFacings(Direction.NORTH, d.getOpposite());
        if (baseRot == null) { return false; }
        for (TriggerPoint trigger : getTriggerPoints()) {
            BlockState baseTrigger = triggerStateMap.getOrDefault(trigger.cell(), getTemplate(world).triggerState());
            Rotation rot = baseRot.getRotated(trigger.offset());
            for (Mirror triedMirror : getMirrorsToTry()) {
                BlockState expected = baseTrigger.mirror(triedMirror).rotate(rot);
                if (BlockMatcher.matches(expected, state, null, null, additionalPredicates).isAllow()) { return true; }
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override public boolean createStructure(Level world, BlockPos pos, Direction side, net.minecraft.world.entity.player.Player player) {
        Rotation baseRot = DirectionUtils.getRotationBetweenFacings(Direction.NORTH, side.getOpposite());
        if (baseRot == null) { return false; }
        getTemplate(world);
        List<StructureTemplate.StructureBlockInfo> structure = getStructure(world);
        for (TriggerPoint trigger : getTriggerPoints()) {
            Rotation rot = baseRot.getRotated(trigger.offset());
            for (Mirror triedMirror : getMirrorsToTry()) {
                StructurePlaceSettings placeSettings = new StructurePlaceSettings().setMirror(triedMirror).setRotation(rot);
                BlockPos origin = pos.subtract(StructureTemplate.calculateRelativePosition(placeSettings, trigger.cell()));
                boolean allMatch = true;
                for (StructureBlockInfo info : structure) {
                    if (info.pos().equals(trigger.cell())) { continue; }
                    BlockPos here = origin.offset(StructureTemplate.calculateRelativePosition(placeSettings, info.pos()));
                    BlockState expected = info.state().mirror(triedMirror).rotate(rot);
                    BlockState inWorld = world.getBlockState(here);
                    if (!BlockMatcher.matches(expected, inWorld, world, here, additionalPredicates).isAllow()) {
                        allMatch = false;
                        break;
                    }
                }
                if (allMatch) {
                    Direction formSide = rot.rotate(Direction.NORTH).getOpposite();
                    if (!world.isClientSide) { form(world, origin, rot, triedMirror, formSide); }
                    return true;
                }
            }
        }
        return false;
    }

    protected void form(Level world, BlockPos origin, Rotation rot, Mirror mirrorForSettings, Direction side) {
        StructureTemplate template = getTemplate(world).template();
        StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rot).setMirror(mirrorForSettings);
        boolean mirrored = mirrorForSettings != Mirror.NONE;
        for (StructureBlockInfo info : template.palettes.get(0).blocks()) {
            BlockPos actualPos = origin.offset(StructureTemplate.calculateRelativePosition(settings, info.pos()));
            Vec3i offsetFromMaster = info.pos().subtract(masterFromOrigin);
            replaceStructureBlock(info, world, actualPos, mirrored, side, offsetFromMaster);
        }
    }

    @Override public Component getDisplayName() { return this.logic.block().get().getName(); }

    @Override public Block getBlock() { return this.logic.block().get(); }

    public Vec3i getSize(@Nullable Level world) { return this.size; }

    @Nonnull public TemplateMultiblock.TemplateData getTemplate(@Nullable Level world) {
        assert world != null;
        TemplateMultiblock.TemplateData result = super.getTemplate(world);
        Vec3i resultSize = result.template().getSize();
        Preconditions.checkState(resultSize.equals(this.size), "Wrong template size for multiblock %s, template size: %s", this.getTemplateLocation(), resultSize);
        ensureCaches(result);
        return result;
    }
}
