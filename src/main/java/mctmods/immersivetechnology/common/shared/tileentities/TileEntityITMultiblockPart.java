package mctmods.immersivetechnology.common.shared.tileentities;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;

import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.multiblock.ShapeData;
import com.immersiveconvergence.api.multiblock.TemplateMultiblock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class TileEntityITMultiblockPart<T extends TileEntityMultiblockPart<T>> extends TemplateMultiblock {
    public IBlockState masterBlockState;
    public IBlockState slaveBlockState;
    public int height;
    public int length;
    public int width;
    public int masterX, masterY, masterZ;
    public PoIJSONSchema[] pointsOfInterest;

    public TileEntityITMultiblockPart(String uniqueName, ShapeData shape, IBlockState master, IBlockState slave) {
        super(uniqueName, shape);
        this.masterBlockState = master;
        this.slaveBlockState = slave;
        this.width = shape.width;
        this.height = shape.height;
        this.length = shape.length;
        this.masterX = shape.masterPos.getX();
        this.masterY = shape.masterPos.getY();
        this.masterZ = shape.masterPos.getZ();
        this.pointsOfInterest = shape.data != null && shape.data.pointsOfInterest != null ? shape.data.pointsOfInterest : new PoIJSONSchema[0];
    }

    @Override protected void replaceStructureBlock(World world, BlockPos worldPos, BlockPos masterWorldPos, int position, boolean mirrored, EnumFacing side) {
        boolean isMaster = worldPos.equals(masterWorldPos);
        IBlockState placed = (isMaster ? masterBlockState : slaveBlockState).withProperty(IEProperties.FACING_HORIZONTAL, side).withProperty(IEProperties.MULTIBLOCKSLAVE, !isMaster);
        world.setBlockState(worldPos, placed, 2);
        @SuppressWarnings("unchecked")
        T tile = (T) world.getTileEntity(worldPos);
        if (tile != null) {
            tile.facing = side;
            tile.formed = true;
            tile.pos = position;
            tile.offset = new int[]{worldPos.getX() - masterWorldPos.getX(), worldPos.getY() - masterWorldPos.getY(), worldPos.getZ() - masterWorldPos.getZ()};
            tile.mirrored = mirrored;
            tile.markDirty();
            world.notifyBlockUpdate(worldPos, placed, placed, 2);
            world.addBlockEvent(worldPos, slaveBlockState.getBlock(), 255, 0);
        }
    }
}
