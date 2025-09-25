package mctmods.immersivetechnology.common.blocks.multiblocks.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.AbstractMap;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ITQueueProcessor {
    private final Level level;
    private final List<AbstractMap.SimpleEntry<BlockPos, BlockState>> queue;
    private final Set<ChunkPos> affectedChunks = new HashSet<>();
    private boolean sorted = false;

    public ITQueueProcessor(Level level, List<AbstractMap.SimpleEntry<BlockPos, BlockState>> queue) {
        this.level = level;
        this.queue = queue;
        for (AbstractMap.SimpleEntry<BlockPos, BlockState> entry : queue) {
            BlockPos pos = entry.getKey();
            ChunkPos center = new ChunkPos(pos);
            for (int dx = -8; dx <= 8; dx++) for (int dz = -8; dz <= 8; dz++) { affectedChunks.add(new ChunkPos(center.x + dx, center.z + dz)); }
        }
    }

    public void tick() {
        if (!sorted) { queue.sort(Comparator.comparingInt(e -> -e.getKey().getY())); sorted = true; }
        int blocksPerTick = ITTemplateMultiblock.DISASSEMBLE_QUEUE_SIZE;
        if (level instanceof ServerLevel serverLevel) {
            ThreadedLevelLightEngine lightEngine = serverLevel.getChunkSource().getLightEngine();
            for (int i = 0; i < blocksPerTick && !queue.isEmpty(); ++i) {
                AbstractMap.SimpleEntry<BlockPos, BlockState> entry = queue.remove(0);
                BlockPos breakPos = entry.getKey();
                BlockState template = entry.getValue();
                level.setBlock(breakPos, template, 3);
                level.destroyBlock(breakPos, false);
            }
            if (queue.isEmpty()) {
                int minSection = lightEngine.getMinLightSection();
                int maxSection = lightEngine.getMaxLightSection();
                for (ChunkPos chunk : affectedChunks) {
                    lightEngine.setLightEnabled(chunk, false);
                    for (int y = minSection; y <= maxSection; y++) {
                        lightEngine.queueSectionData(LightLayer.SKY, SectionPos.of(chunk, y), null);
                        lightEngine.updateSectionStatus(SectionPos.of(chunk, y), false);
                    }
                }
                for (ChunkPos chunk : affectedChunks) {
                    lightEngine.setLightEnabled(chunk, true);
                    lightEngine.propagateLightSources(chunk);
                }
                while (lightEngine.hasLightWork()) { lightEngine.runLightUpdates(); }
                ChunkMap chunkMap = serverLevel.getChunkSource().chunkMap;
                for (ChunkPos chunk : affectedChunks) {
                    LevelChunk levelChunk = serverLevel.getChunk(chunk.x, chunk.z);
                    levelChunk.setLightCorrect(false);
                    levelChunk.setUnsaved(true);
                    List<ServerPlayer> players = chunkMap.getPlayers(chunk, false);
                    players.forEach(p -> p.connection.send(new ClientboundForgetLevelChunkPacket(chunk.x, chunk.z)));
                    BitSet skyMask = new BitSet();
                    BitSet blockMask = new BitSet();
                    for (int y = minSection; y <= maxSection; y++) {
                        int index = y - minSection;
                        skyMask.set(index);
                        blockMask.set(index);
                    }
                    ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(levelChunk, lightEngine, skyMask, blockMask);
                    players.forEach(p -> p.connection.send(packet));
                    ClientboundLightUpdatePacket lightPacket = new ClientboundLightUpdatePacket(chunk, lightEngine, skyMask, blockMask);
                    players.forEach(p -> p.connection.send(lightPacket));
                    ChunkHolder holder = chunkMap.getUpdatingChunkIfPresent(chunk.toLong());
                    if (holder != null) { holder.broadcastChanges(levelChunk); }
                }
                affectedChunks.clear();
            }
        }
    }

    public boolean isEmpty() { return queue.isEmpty(); }
}
