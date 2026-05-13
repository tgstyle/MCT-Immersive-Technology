package mctmods.immersivetechnology.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ConveyorDropAlternative extends ConveyorBasicAlternative {
    public static final ResourceLocation texture_on = new ResourceLocation("immersiveengineering", "blocks/conveyor_dropper");
    public static final ResourceLocation texture_off = new ResourceLocation("immersiveengineering", "blocks/conveyor_dropper_off");

    @Override public ResourceLocation getActiveTexture() { return texture_on; }

    @Override public ResourceLocation getInactiveTexture() { return texture_off; }

    @Override public String getModelCacheKey(TileEntity tile, EnumFacing facing) {
        return "immersivetech:drop_conveyor" +
                "f" + facing.ordinal() +
                "a" + (isActive(tile) ? 1 : 0) +
                "c" + getDyeColour();
    }

    @Override public boolean changeConveyorDirection() {
        return false;
    }

    @Override public boolean setConveyorDirection(ConveyorDirection dir) {
        if (dir != ConveyorDirection.HORIZONTAL) return false;
        return super.setConveyorDirection(dir);
    }

    @Override public boolean playerInteraction(TileEntity tile, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, EnumFacing side) {
        if (Utils.isHammer(heldItem) && player.isSneaking()) {
            if (!tile.getWorld().isRemote) {
                tile.markDirty();
                IBlockState state = tile.getWorld().getBlockState(tile.getPos());
                tile.getWorld().notifyBlockUpdate(tile.getPos(), state, state, 3);
            }
            return true;
        }
        return false;
    }

    @Override public void handleInsertion(TileEntity tile, EntityItem entity, EnumFacing facing, ConveyorHandler.ConveyorDirection conDir, double distX, double distZ) {
        BlockPos posDown = tile.getPos().down();
        TileEntity inventoryTile = tile.getWorld().getTileEntity(posDown);
        boolean contact = Math.abs((facing.getAxis() == EnumFacing.Axis.Z ? tile.getPos().getZ() : tile.getPos().getX()) + 0.5 -
                (facing.getAxis() == EnumFacing.Axis.Z ? entity.posZ : entity.posX)) < 0.2;

        if (contact && inventoryTile != null && !(inventoryTile instanceof IConveyorTile)) {
            if (!tile.getWorld().isRemote) {
                ItemStack stack = entity.getItem();
                if (!stack.isEmpty()) {
                    ItemStack ret = ApiUtils.insertStackIntoInventory(inventoryTile, stack, EnumFacing.UP);
                    if (ret.isEmpty()) {
                        entity.setDead();
                    } else if (ret.getCount() < stack.getCount()) {
                        entity.setItem(ret);
                    }
                }
            }
        } else if (contact && isEmptySpace(tile.getWorld(), posDown, inventoryTile)) {
            entity.motionX = 0;
            entity.motionZ = 0;
            entity.setPosition(tile.getPos().getX() + 0.5, tile.getPos().getY() - 0.5, tile.getPos().getZ() + 0.5);
            if (inventoryTile == null) {
                ConveyorHandler.revertMagnetSupression(entity, (IConveyorTile) tile);
            }
        } else {
            super.handleInsertion(tile, entity, facing, conDir, distX, distZ);
        }
    }

    private boolean isEmptySpace(World world, BlockPos pos, TileEntity tile) {
        if (world.isAirBlock(pos)) return true;
        if (tile instanceof IConveyorTile) return true;
        IBlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof BlockTrapDoor && state.getValue(BlockTrapDoor.OPEN);
    }
}
