package mctmods.immersivetechnology.common.multiblocks.helper;

import mctmods.immersivetechnology.core.util.ITFakePlayerUtil;
import mctmods.immersivetechnology.core.util.ITUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ITQueueProcessor {
    private static final Comparator<BlockPos> Y_DESC_COMPARATOR = Comparator.comparingInt(pos -> -pos.getY());

    private final ServerLevel level;
    private final Deque<BlockPos> queue = new ArrayDeque<>();
    private final @Nullable ServerPlayer owner;
    private final boolean dropItems;
    private final BlockPos dropAt;
    private final List<ItemStack> allDrops;
    private FakePlayer fakePlayer;

    private final Set<ChunkPos> affectedChunks = new HashSet<>();
    private boolean chunksMarked = false;

    public ITQueueProcessor(ServerLevel level, List<BlockPos> toBreak, @Nullable ServerPlayer owner, boolean dropItems, BlockPos dropAt, List<ItemStack> allDrops, @Nullable BlockPos masterPos) {
        this.level = level;
        List<BlockPos> sorted = new ArrayList<>(toBreak);
        sorted.sort(Y_DESC_COMPARATOR);
        if (masterPos != null && sorted.remove(masterPos)) { sorted.addFirst(masterPos); }
        this.queue.addAll(sorted);
        this.owner = owner;
        this.dropItems = dropItems;
        this.dropAt = dropAt;
        this.allDrops = allDrops;

        for (BlockPos pos : toBreak) {
            ChunkPos cp = new ChunkPos(pos);
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) { affectedChunks.add(new ChunkPos(cp.x + dx, cp.z + dz)); }
            }
        }
    }

    public void tick() {
        if (!chunksMarked) {
            markChunksForLightUpdate();
            chunksMarked = true;
        }

        if (queue.isEmpty()) {
            if (dropItems && !allDrops.isEmpty()) { for (ItemStack s : allDrops) { ITUtils.dropStackAtPos(level, dropAt, s); } }
            allDrops.clear();

            doFinalLightingRefresh();
            return;
        }

        if (fakePlayer == null) { fakePlayer = ITFakePlayerUtil.getFakePlayer(level, owner); }

        BlockPos batchPos = queue.peek();
        if (batchPos != null) {
            fakePlayer.setPos(batchPos.getX() + 0.5, batchPos.getY() + 1, batchPos.getZ() + 0.5);
            fakePlayer.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(batchPos));
        }

        int blocksPerTick = ITTemplateMultiblock.DISASSEMBLE_QUEUE_SIZE;
        for (int i = 0; i < blocksPerTick && !queue.isEmpty(); ++i) {
            BlockPos pos = queue.poll();
            fakePlayer.gameMode.destroyBlock(pos);
        }
    }

    private void markChunksForLightUpdate() {
        for (ChunkPos chunk : affectedChunks) {
            LevelChunk levelChunk = level.getChunk(chunk.x, chunk.z);
            levelChunk.setLightCorrect(false);
            levelChunk.setUnsaved(true);
        }
    }

    private void doFinalLightingRefresh() {
        ChunkMap chunkMap = level.getChunkSource().chunkMap;
        ThreadedLevelLightEngine lightEngine = level.getChunkSource().getLightEngine();

        for (ChunkPos chunk : affectedChunks) {
            LevelChunk levelChunk = level.getChunk(chunk.x, chunk.z);
            levelChunk.setLightCorrect(false);
            levelChunk.setUnsaved(true);

            List<ServerPlayer> players = chunkMap.getPlayers(chunk, false);
            players.forEach(p -> p.connection.send(new ClientboundForgetLevelChunkPacket(chunk)));

            ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(levelChunk, lightEngine, null, null);
            players.forEach(p -> p.connection.send(packet));

            ChunkHolder holder = chunkMap.getVisibleChunkIfPresent(chunk.toLong());
            if (holder != null) { holder.broadcastChanges(levelChunk); }
        }

        affectedChunks.clear();
        fakePlayer = null;
    }

    public boolean isEmpty() { return queue.isEmpty() && allDrops.isEmpty(); }
}
