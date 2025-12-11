package mctmods.immersivetechnology.mixin;

import mctmods.immersivetechnology.core.MCTMixin;
import mctmods.immersivetechnology.core.MCTMixinConfig;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(World.class)
public abstract class MixinMCTWorldCMEFix {
    @Shadow(remap = false)
    private boolean processingLoadedTiles;

    @Shadow(remap = false) @Final
    private List<TileEntity> addedTileEntityList;

    @Shadow(remap = false) @Final
    public List<TileEntity> loadedTileEntityList;

    @Shadow(remap = false) @Final
    public List<TileEntity> tickableTileEntities;

    @Shadow(remap = false)
    public abstract void notifyBlockUpdate(net.minecraft.util.math.BlockPos pos, IBlockState oldState, IBlockState newState, int flags);

    @Inject(method = "addTileEntities(Ljava/util/Collection;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void injectAddTileEntities(Collection<TileEntity> collection, CallbackInfo ci) {
        if (!MCTMixinConfig.mixinSettings.enableWorldMixin) { return; }
        ci.cancel();
        World world = (World)(Object)this;
        List<TileEntity> toAdd = new ArrayList<>(collection);
        if (MCTMixinConfig.mixinSettings.enableAdditionsLogging) {
            MCTMixin.LOGGER.info("Adding {} TEs (delayed): {}", toAdd.size(), processingLoadedTiles ? "delayed" : "immediate");
        }
        if (processingLoadedTiles) {
            for (TileEntity tile : toAdd) {
                if (tile.getWorld() != world) { tile.setWorld(world); }
                int sizeBefore = addedTileEntityList.size();
                addedTileEntityList.add(tile);
                int sizeAfter = addedTileEntityList.size();
                if (MCTMixinConfig.mixinSettings.enablePotentialsLogging && sizeAfter > sizeBefore + 1) { MCTMixin.LOGGER.warn("Potential CME in delayed add: {} at {}", tile.getClass().getName(), tile.getPos()); }
            }
        } else {
            for (TileEntity tile : toAdd) {
                if (tile.getWorld() != world) { tile.setWorld(world); }
                int sizeBefore = loadedTileEntityList.size();
                loadedTileEntityList.add(tile);
                if (tile instanceof ITickable) { tickableTileEntities.add(tile); }
                Chunk chunk = world.getChunk(tile.getPos());
                chunk.addTileEntity(tile.getPos(), tile);
                tile.onLoad();
                if (world.isRemote) {
                    IBlockState state = world.getBlockState(tile.getPos());
                    notifyBlockUpdate(tile.getPos(), state, state, 2);
                }
                int sizeAfter = loadedTileEntityList.size();
                if (MCTMixinConfig.mixinSettings.enablePotentialsLogging && sizeAfter > sizeBefore + 1) { MCTMixin.LOGGER.warn("Potential CME detected: {} at {}", tile.getClass().getName(), tile.getPos()); }
            }
        }
    }
}
