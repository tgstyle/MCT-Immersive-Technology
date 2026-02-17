package mctmods.immersivetechnology.common.conveyors;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ConveyorDropAlternative extends ConveyorBasicAlternative {
    public static final ResourceLocation texture_on = new ResourceLocation("immersiveengineering", "blocks/conveyor_dropper");
    public static final ResourceLocation texture_off = new ResourceLocation("immersiveengineering", "blocks/conveyor_dropper_off");

    @Override public ResourceLocation getActiveTexture() { return texture_on; }

    @Override public ResourceLocation getInactiveTexture() { return texture_off; }

    @Override public String getModelCacheKey(TileEntity tile, EnumFacing facing) {
        String key = "immersivetech:drop_conveyor";
        key += "f" + facing.ordinal();
        key += "a" + (isActive(tile) ? 1 : 0);
        key += "c" + getDyeColour();
        return key;
    }

    @Override public void handleInsertion(TileEntity tile, EntityItem entity, EnumFacing facing, ConveyorHandler.ConveyorDirection conDir, double distX, double distZ) {
        BlockPos posDown = tile.getPos().down();
        TileEntity inventoryTile = tile.getWorld().getTileEntity(posDown);
        boolean contact = Math.abs(facing.getAxis() == EnumFacing.Axis.Z ? tile.getPos().getZ() + 0.5 - entity.posZ : tile.getPos().getX() + 0.5 - entity.posX) < 0.2;
        if (contact && inventoryTile != null && !(inventoryTile instanceof IConveyorTile)) {
            if (!tile.getWorld().isRemote) {
                ItemStack stack = entity.getItem();
                if (!stack.isEmpty()) {
                    ItemStack ret = ApiUtils.insertStackIntoInventory(inventoryTile, stack, EnumFacing.UP);
                    if (ret.isEmpty()) { entity.setDead(); }
                    else if (ret.getCount() < stack.getCount()) { entity.setItem(ret); }
                }
            }
        } else if (contact && isEmptySpace(tile.getWorld(), posDown, inventoryTile)) {
            entity.motionX = 0;
            entity.motionZ = 0;
            entity.setPosition(tile.getPos().getX() + 0.5, tile.getPos().getY() - 0.5, tile.getPos().getZ() + 0.5);
            if (inventoryTile == null) { ConveyorHandler.revertMagnetSupression(entity, (IConveyorTile)tile); }
        } else { super.handleInsertion(tile, entity, facing, conDir, distX, distZ); }
    }

    private boolean isEmptySpace(World world, BlockPos pos, TileEntity tile) {
        if (world.isAirBlock(pos)) { return true; }
        if (tile instanceof IConveyorTile) { return true; }
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockTrapDoor) { return state.getValue(BlockTrapDoor.OPEN); }
        return false;
    }

}
