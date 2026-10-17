package mctmods.immersivetechnology.core.util;

import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.util.RelativeBlockFace;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class EnergyPush {
    private EnergyPush() {}

    public static int distributeFluxProper(List<IEnergyStorage> storages, int amount) {
        List<Pair<IEnergyStorage, Integer>> pairs = storages.stream()
                .filter(Objects::nonNull)
                .map(storage -> Pair.of(storage, storage.receiveEnergy(amount, true)))
                .sorted(Comparator.comparingInt(Pair::getSecond))
                .toList();
        int remaining = amount;
        int remainingOutputs = pairs.size();
        for (Pair<IEnergyStorage, Integer> pair : pairs) {
            if (remaining <= 0) { break; }
            int possibleOutput = (int) Math.ceil((double) remaining / remainingOutputs);
            remaining -= pair.getFirst().receiveEnergy(possibleOutput, false);
            remainingOutputs--;
        }
        return amount - remaining;
    }

    public static void addConnectedHandlers(List<IEnergyStorage> connected, IMultiblockContext<?> ctx, Level level, List<BlockPos> pois, RelativeBlockFace facing) {
        for (BlockPos pos : pois) {
            Direction side = ctx.getLevel().toAbsolute(facing);
            if (side == null) { continue; }
            IEnergyStorage handler = level.getCapability(Capabilities.EnergyStorage.BLOCK, ctx.getLevel().toAbsolute(pos).relative(side), side.getOpposite());
            if (handler != null) { connected.add(handler); }
        }
    }
}
