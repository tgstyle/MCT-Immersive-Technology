// main/java/mctmods/immersivetechnology/mixin/MixinMCTWorldCMEFix.java

package mctmods.immersivetechnology.mixin;

import mctmods.immersivetechnology.core.MCTMixin;
import mctmods.immersivetechnology.core.MCTMixinConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(World.class)
public abstract class MixinMCTWorldCMEFix {
    @Shadow private boolean processingLoadedTiles;

    @Shadow @Final private List<TileEntity> addedTileEntityList;

    @Shadow @Final public List<TileEntity> loadedTileEntityList;

    @Shadow @Final public List<TileEntity> tickableTileEntities;

    @Shadow @Final public boolean isRemote;

    @Inject(method = "addTileEntities(Ljava/util/Collection;)V", at = @At("HEAD"), cancellable = true)
    private void injectAddTileEntities(Collection<TileEntity> collection, CallbackInfo ci) {
        if (!MCTMixinConfig.mixinSettings.enableWorldMixin) { return; }
        ci.cancel();
        World world = (World)(Object)this;
        List<TileEntity> toAdd = new ArrayList<>(collection);
        if (MCTMixinConfig.mixinSettings.enableAdditionsLogging && !toAdd.isEmpty()) {
            String mode = processingLoadedTiles ? "delayed" : "immediate";
            MCTMixin.LOGGER.debug("Adding {} TEs ({} add)", toAdd.size(), mode);
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
                if (isRemote) {
                    IBlockState state = world.getBlockState(tile.getPos());
                    try {
                        Method m = World.class.getMethod("notifyBlockUpdate", BlockPos.class, IBlockState.class, IBlockState.class, int.class);
                        m.invoke(world, tile.getPos(), state, state, 3);
                    } catch (Throwable ignored) {}
                }
                int sizeAfter = loadedTileEntityList.size();
                if (MCTMixinConfig.mixinSettings.enablePotentialsLogging && sizeAfter > sizeBefore + 1) { MCTMixin.LOGGER.warn("Potential CME detected: {} at {}", tile.getClass().getName(), tile.getPos()); }
            }
        }
    }
}
