package mctmods.immersivetechnology.common.multiblocks.helper;

import mctmods.immersivetechnology.common.blocks.helper.ModProperties;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.util.Utils;

import blusunrize.immersiveengineering.api.multiblocks.BlockMatcher;
import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;

import static net.minecraft.world.level.block.Mirror.FRONT_BACK;

public abstract class ModTemplateMultiblock extends TemplateMultiblock {
    public static final int DISASSEMBLE_QUEUE_SIZE = 8;
    public static final List<QueueProcessor> pendingQueues = new ArrayList<>();
    public static final Set<BlockPos> activeDisassemblies = new HashSet<>();
    public static BlockPos currentlyBreakingPos = null;
    public static boolean sneakBreaking = false;

    public record TriggerPoint(BlockPos cell, Rotation offset) {}

    private List<StructureBlockInfo> sortedStructureBlocks;
    private Map<BlockPos, BlockState> triggerStateMap;
    private MultiblockRegistration<?> multiblockRegistration;
    private final BlockPos clientOffset;
    private final float manualScale;

    public ModTemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size, BlockPos clientOffset, float manualScale, MultiblockRegistration<?> logic) {
        super(loc, masterFromOrigin, triggerFromOrigin, size);
        this.clientOffset = clientOffset;
        this.manualScale = manualScale;
        this.multiblockRegistration = logic;
    }

    public ModTemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size, BlockPos clientOffset, float manualScale, List<BlockMatcher.MatcherPredicate> additionalPredicates, MultiblockRegistration<?> logic) {
        super(loc, masterFromOrigin, triggerFromOrigin, size, additionalPredicates);
        this.clientOffset = clientOffset;
        this.manualScale = manualScale;
        this.multiblockRegistration = logic;
    }

    public ModTemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size, BlockPos clientOffset, float manualScale) {
        super(loc, masterFromOrigin, triggerFromOrigin, size);
        this.clientOffset = clientOffset;
        this.manualScale = manualScale;
    }

    public ModTemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size, BlockPos clientOffset, float manualScale, List<BlockMatcher.MatcherPredicate> additionalPredicates) {
        super(loc, masterFromOrigin, triggerFromOrigin, size, additionalPredicates);
        this.clientOffset = clientOffset;
        this.manualScale = manualScale;
    }

    @SuppressWarnings("unused")
    public void setLogic(MultiblockRegistration<?> logic) { this.multiblockRegistration = logic; }

    protected List<TriggerPoint> getTriggerPoints() { return List.of(new TriggerPoint(this.triggerFromOrigin, Rotation.NONE)); }

    protected List<Mirror> getMirrorsToTry() { return canBeMirrored() ? List.of(Mirror.NONE, FRONT_BACK) : List.of(Mirror.NONE); }

    private void ensureCaches(TemplateData data) {
        if (sortedStructureBlocks != null) { return; }
        List<StructureBlockInfo> nonAir = data.blocksWithoutAir();
        sortedStructureBlocks = new ArrayList<>(nonAir);
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

    @SuppressWarnings("deprecation")
    private static BlockState rotate(BlockState state, Rotation rotation) { return state.rotate(rotation); }

    @Override public float getManualScale() { return manualScale; }

    @Override public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ClientMultiblockProperties(this, clientOffset.getX(), clientOffset.getY(), clientOffset.getZ())); }

    @Override public boolean canBeMirrored() {
        if (multiblockRegistration != null) { return multiblockRegistration.mirrorable(); }
        return super.canBeMirrored();
    }

    @Override protected void replaceStructureBlock(StructureTemplate.StructureBlockInfo info, Level world, BlockPos actualPos, boolean mirrored, Direction clickDirection, Vec3i offsetFromMaster) {
        BlockState newState = this.getBlock().defaultBlockState();
        if (newState.hasProperty(ModProperties.MULTIBLOCKSLAVE)) { newState = newState.setValue(ModProperties.MULTIBLOCKSLAVE, !offsetFromMaster.equals(Vec3i.ZERO)); }
        if (newState.hasProperty(ModProperties.MIRRORED)) { newState = newState.setValue(ModProperties.MIRRORED, mirrored); }
        if (newState.hasProperty(ModProperties.FACING_HORIZONTAL)) { newState = newState.setValue(ModProperties.FACING_HORIZONTAL, clickDirection.getOpposite()); }
        if (newState.hasProperty(ModProperties.ACTIVE)) { newState = newState.setValue(ModProperties.ACTIVE, false); }
        world.setBlock(actualPos, newState, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        BlockEntity curr = world.getBlockEntity(actualPos);
        if (curr instanceof MultiblockBlockEntityDummy<?> dummy) { dummy.getHelper().setPositionInMB(info.pos()); }
        else if (!(curr instanceof MultiblockBlockEntityMaster)) { Reference.IT_LOGGER.error("Expected MB TE at {} during placement", actualPos); }
    }

    @Override
    public boolean isBlockTrigger(BlockState state, Direction d, @Nonnull Level world) {
        getTemplate(world);
        Rotation baseRot = DirectionUtils.getRotationBetweenFacings(Direction.NORTH, d.getOpposite());
        if (baseRot == null) { return false; }
        for (TriggerPoint trigger : getTriggerPoints()) {
            BlockState baseTrigger = triggerStateMap.getOrDefault(trigger.cell(), getTemplate(world).triggerState());
            Rotation rot = baseRot.getRotated(trigger.offset());
            for (Mirror triedMirror : getMirrorsToTry()) {
                BlockState expected = rotate(baseTrigger.mirror(triedMirror), rot);
                if (BlockMatcher.matches(expected, state, null, null, additionalPredicates).isAllow()) { return true; }
            }
        }
        return false;
    }

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
                    BlockState expected = rotate(info.state().mirror(triedMirror), rot);
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
        getTemplate(world);
        StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rot).setMirror(mirrorForSettings);
        boolean mirrored = mirrorForSettings != Mirror.NONE;
        Set<BlockPos> placedPositions = new HashSet<>();
        for (StructureBlockInfo info : sortedStructureBlocks) {
            BlockPos actualPos = origin.offset(StructureTemplate.calculateRelativePosition(settings, info.pos()));
            Vec3i offsetFromMaster = info.pos().subtract(masterFromOrigin);
            replaceStructureBlock(info, world, actualPos, mirrored, side, offsetFromMaster);
            placedPositions.add(actualPos);
        }
        Block block = getBlock();
        for (BlockPos placedPos : placedPositions) {
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = placedPos.relative(dir);
                if (!placedPositions.contains(neighborPos)) { world.neighborChanged(neighborPos, block, placedPos); }
            }
        }
    }

    @Override public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) {
        if (world.isClientSide) { return; }
        Mirror mirror = mirrored ? FRONT_BACK : Mirror.NONE;
        Rotation rot = DirectionUtils.getRotationBetweenFacings(Direction.NORTH, clickDirectionAtCreation);
        Preconditions.checkNotNull(rot);
        if (world instanceof ServerLevel serverLevel) {
            BlockPos initiatedAt = currentlyBreakingPos;
            boolean templateMode = sneakBreaking || ServerConfig.DISASSEMBLY_MODE.get() == ServerConfig.DisassemblyMode.TEMPLATE_BLOCKS;
            boolean doTileDrops = serverLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS);
            BlockPos masterPos = withSettingsAndOffset(origin, masterFromOrigin, mirror, rot);
            ServerPlayer breakingPlayer = (ServerPlayer) serverLevel.getNearestPlayer(masterPos.getX() + 0.5, masterPos.getY() + 0.5, masterPos.getZ() + 0.5, -1.0, e -> true);
            boolean dropItems = doTileDrops;
            if (breakingPlayer != null && breakingPlayer.gameMode.getGameModeForPlayer() == GameType.CREATIVE) { dropItems = false; }
            ItemStack tool = breakingPlayer != null ? breakingPlayer.getMainHandItem() : ItemStack.EMPTY;
            ItemStack effectiveTool = tool.isEmpty() ? new ItemStack(Items.DIAMOND_PICKAXE) : tool;
            if (!activeDisassemblies.add(masterPos.immutable())) { return; }
            BlockEntity masterBE = world.getBlockEntity(masterPos);
            if (masterBE instanceof IMultiblockBE<?> mbBE && mbBE.getHelper() instanceof IDisassemblingAware itH) { itH.it$markDisassembling(); }
            getTemplate(world);
            List<StructureBlockInfo> structure = sortedStructureBlocks;
            for (StructureBlockInfo info : structure) {
                BlockPos actualPos = withSettingsAndOffset(origin, info.pos(), mirror, rot);
                prepareBlockForDisassembly(serverLevel, actualPos);
            }
            BlockPos brokenPos = initiatedAt != null ? initiatedAt : masterPos;
            if (initiatedAt == null && breakingPlayer != null) {
                Vec3 eyePos = breakingPlayer.getEyePosition();
                Vec3 look = breakingPlayer.getViewVector(1.0F);
                double reach = breakingPlayer.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
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
            LootParams.Builder baseLootBuilder = dropItems ? new LootParams.Builder(serverLevel).withParameter(LootContextParams.TOOL, effectiveTool).withOptionalParameter(LootContextParams.THIS_ENTITY, breakingPlayer) : null;
            if (templateMode) {
                BlockState brokenTemplate = null;
                for (StructureBlockInfo info : structure) {
                    BlockPos actualPos = withSettingsAndOffset(origin, info.pos(), mirror, rot);
                    BlockState stateAfterMirror = info.state().mirror(mirror);
                    BlockState template = rotate(stateAfterMirror, rot);
                    if (actualPos.equals(brokenPos)) { brokenTemplate = template; }
                    serverLevel.setBlockAndUpdate(actualPos, template);
                }
                if (initiatedAt != null && brokenTemplate != null && !brokenTemplate.isAir()) {
                    if (dropItems) {
                        BlockEntity brokenBE = serverLevel.getBlockEntity(brokenPos);
                        LootParams.Builder params = baseLootBuilder.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(brokenPos)).withOptionalParameter(LootContextParams.BLOCK_ENTITY, brokenBE);
                        try { for (ItemStack s : brokenTemplate.getDrops(params)) { Utils.dropStackAtPos(serverLevel, brokenPos, s); } }
                        catch (Exception e) { Utils.dropStackAtPos(serverLevel, brokenPos, new ItemStack(brokenTemplate.getBlock())); }
                    }
                    serverLevel.removeBlock(brokenPos, false);
                }
            }
            else {
                for (StructureBlockInfo info : structure) {
                    BlockPos actualPos = withSettingsAndOffset(origin, info.pos(), mirror, rot);
                    BlockState stateAfterMirror = info.state().mirror(mirror);
                    BlockState template = rotate(stateAfterMirror, rot);
                    toBreak.add(actualPos);
                    if (dropItems && !template.isAir()) {
                        BlockEntity templateBE = null;
                        if (template.hasBlockEntity() && template.getBlock() instanceof EntityBlock entityBlock) {
                            try { templateBE = entityBlock.newBlockEntity(actualPos, template); }
                            catch (Exception ignored) { }
                        }
                        LootParams.Builder params = baseLootBuilder.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(actualPos)).withOptionalParameter(LootContextParams.BLOCK_ENTITY, templateBE);
                        try { allDrops.addAll(template.getDrops(params)); }
                        catch (Exception e) { allDrops.add(new ItemStack(template.getBlock())); }
                    }
                }
            }
            if (templateMode || toBreak.isEmpty()) { activeDisassemblies.remove(masterPos); }
            else { pendingQueues.add(new QueueProcessor(serverLevel, toBreak, breakingPlayer, dropItems, brokenPos, allDrops, masterPos)); }
        }
    }

    @Override protected void prepareBlockForDisassembly(Level world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be == null) { return; }
        boolean marked = false;
        if (be instanceof IDisassemblingAware itBE) {
            itBE.it$markDisassembling();
            marked = true;
        }
        if (be instanceof IMultiblockBE<?> multiblockBE && multiblockBE.getHelper() instanceof IDisassemblingAware itHelper) {
            itHelper.it$markDisassembling();
            marked = true;
        }
        if (!marked) { Reference.IT_LOGGER.error("Expected multiblock BE at {}, got {}", pos, be); }
    }

    @Override public Component getDisplayName() { return this.getBlock().getName(); }

    @Override public Block getBlock() {
        if (multiblockRegistration != null && multiblockRegistration.block() != null) { return multiblockRegistration.block().get(); }
        return super.getBlock();
    }

    public Vec3i getSize(@Nullable Level world) { return this.size; }

    @Nonnull @Override public TemplateData getTemplate(@Nullable Level world) {
        ResourceLocation loc = this.getTemplateLocation();
        try {
            TemplateData result = super.getTemplate(world);
            Vec3i resultSize = result.template().getSize();
            Preconditions.checkState(resultSize.equals(this.size), "Wrong template size for multiblock %s, template size: %s", loc, resultSize);
            ensureCaches(result);
            return result;
        } catch (Exception e) {
            Reference.IT_LOGGER.error("getTemplate FAILED for loc: {} (world={})", loc, world.dimension().location(), e);
            throw e;
        }
    }
}
