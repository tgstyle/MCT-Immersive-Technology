package mctmods.immersivetechnology.common.multiblocks.helper;

import mctmods.immersivetechnology.core.util.ITFakePlayerUtil;
import mctmods.immersivetechnology.core.util.ITUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ITQueueProcessor {
    private final ServerLevel level;
    private final List<BlockPos> queue = new ArrayList<>();
    private final @Nullable ServerPlayer owner;
    private final boolean dropItems;
    private final BlockPos dropAt;
    private final List<ItemStack> allDrops;
    private FakePlayer fakePlayer;

    public ITQueueProcessor(ServerLevel level, List<BlockPos> toBreak, @Nullable ServerPlayer owner, boolean dropItems, BlockPos dropAt, List<ItemStack> allDrops) {
        this.level = level;
        this.queue.addAll(toBreak);
        this.queue.sort(Comparator.comparingInt(pos -> -pos.getY()));
        this.owner = owner;
        this.dropItems = dropItems;
        this.dropAt = dropAt;
        this.allDrops = allDrops;
    }

    public void tick() {
        if (queue.isEmpty()) {
            if (dropItems && !allDrops.isEmpty()) {
                for (ItemStack s : allDrops) {
                    ITUtils.dropStackAtPos(level, dropAt, s);
                }
            }
            allDrops.clear();
            return;
        }

        if (fakePlayer == null) {
            fakePlayer = ITFakePlayerUtil.getFakePlayer(level, owner);
        }

        int blocksPerTick = ITTemplateMultiblock.DISASSEMBLE_QUEUE_SIZE;
        for (int i = 0; i < blocksPerTick && !queue.isEmpty(); ++i) {
            BlockPos pos = queue.remove(0);
            fakePlayer.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
            fakePlayer.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(pos));
            fakePlayer.gameMode.destroyBlock(pos);
        }
    }

    public boolean isEmpty() { return queue.isEmpty() && allDrops.isEmpty(); }
}
