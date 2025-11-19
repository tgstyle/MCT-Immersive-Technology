package mctmods.immersivetechnology.common.multiblocks.helper;

import mctmods.immersivetechnology.common.util.ITFakePlayerUtil;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ITQueueProcessor {
    private final Level level;
    private final List<AbstractMap.SimpleEntry<BlockPos, BlockState>> queue = new ArrayList<>();
    private final List<BlockPos> pendingBreaks = new ArrayList<>();
    private final @Nullable ServerPlayer owner;
    private FakePlayer fakePlayer;
    private final Set<ChunkPos> affectedChunks = new HashSet<>();
    private boolean chunksMarked = false;

    public ITQueueProcessor(Level level, List<AbstractMap.SimpleEntry<BlockPos, BlockState>> sourceQueue, @Nullable ServerPlayer owner) {
        this.level = level;
        this.owner = owner;
        queue.addAll(sourceQueue);
        queue.sort(Comparator.comparingInt(e -> -e.getKey().getY()));

        for (var entry : sourceQueue) {
            ChunkPos cp = new ChunkPos(entry.getKey());
            for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) { affectedChunks.add(new ChunkPos(cp.x + dx, cp.z + dz)); }
        }
    }

    public void tick() {
        if (!(level instanceof ServerLevel serverLevel)) { return; }

        if (!chunksMarked) {
            for (ChunkPos chunk : affectedChunks) {
                LevelChunk levelChunk = serverLevel.getChunk(chunk.x, chunk.z);
                levelChunk.setLightCorrect(false);
                levelChunk.setUnsaved(true);
            }
            chunksMarked = true;
        }

        if (queue.isEmpty() && pendingBreaks.isEmpty()) {
            if (fakePlayer != null) {
                ChunkMap chunkMap = serverLevel.getChunkSource().chunkMap;
                ThreadedLevelLightEngine lightEngine = serverLevel.getChunkSource().getLightEngine();
                for (ChunkPos chunk : affectedChunks) {
                    LevelChunk levelChunk = serverLevel.getChunk(chunk.x, chunk.z);
                    levelChunk.setLightCorrect(false);
                    levelChunk.setUnsaved(true);
                    List<ServerPlayer> players = chunkMap.getPlayers(chunk, false);
                    players.forEach(p -> p.connection.send(new ClientboundForgetLevelChunkPacket(chunk.x, chunk.z)));
                    ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(levelChunk, lightEngine, null, null);
                    players.forEach(p -> p.connection.send(packet));
                    ChunkHolder holder = chunkMap.getUpdatingChunkIfPresent(chunk.toLong());
                    if (holder != null) { holder.broadcastChanges(levelChunk); }
                }
                affectedChunks.clear();
                fakePlayer = null;
            }
            return;
        }

        if (fakePlayer == null) { fakePlayer = ITFakePlayerUtil.getFakePlayer(serverLevel, owner); }

        int blocksPerTick = ITTemplateMultiblock.DISASSEMBLE_QUEUE_SIZE;

        for (int i = 0; i < blocksPerTick && !pendingBreaks.isEmpty(); ++i) {
            BlockPos pos = pendingBreaks.remove(0);
            fakePlayer.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
            fakePlayer.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(pos));
            fakePlayer.gameMode.destroyBlock(pos);
        }

        for (int i = 0; i < blocksPerTick && !queue.isEmpty(); ++i) {
            var entry = queue.remove(0);
            BlockPos pos = entry.getKey();
            BlockState template = entry.getValue();
            level.setBlock(pos, template, 3);
            pendingBreaks.add(pos);
        }
    }

    public boolean isEmpty() { return queue.isEmpty() && pendingBreaks.isEmpty(); }
}
