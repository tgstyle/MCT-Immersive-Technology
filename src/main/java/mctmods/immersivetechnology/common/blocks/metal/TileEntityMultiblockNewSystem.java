package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public abstract class TileEntityMultiblockNewSystem<T extends TileEntityMultiblockNewSystem<T, R, M>, R extends IMultiblockRecipe, M extends T> extends TileEntityMultiblockMetal<T,R> {

    public TileEntityMultiblockNewSystem(MultiblockHandler.IMultiblock mutliblockInstance, int[] structureDimensions, int energyCapacity, boolean redstoneControl) {
        super(mutliblockInstance, structureDimensions, energyCapacity, redstoneControl);
    }

    @Nullable
    @Override
    public T getTileForPos(int targetPos) {
        BlockPos target = getBlockPosForPos(targetPos);
        TileEntity tile = Utils.getExistingTileEntity(world, target);
        if(tile instanceof TileEntityMultiblockNewSystem && tile.getClass().isInstance(this))
            return (T)tile;
        return null;
    }
}