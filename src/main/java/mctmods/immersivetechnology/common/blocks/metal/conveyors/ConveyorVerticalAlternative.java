package mctmods.immersivetechnology.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelConveyor;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Matrix4f;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ConveyorVerticalAlternative extends ConveyorBasicAlternative {
    public static final AxisAlignedBB[] verticalBounds = {
            new AxisAlignedBB(0, 0, 0, 1, 1, 0.125),
            new AxisAlignedBB(0, 0, 0.875, 1, 1, 1),
            new AxisAlignedBB(0, 0, 0, 0.125, 1, 1),
            new AxisAlignedBB(0.875, 0, 0, 1, 1, 1)
    };

    public static final ResourceLocation texture_on = new ResourceLocation("immersiveengineering", "blocks/conveyor_vertical");
    public static final ResourceLocation texture_off = new ResourceLocation("immersiveengineering", "blocks/conveyor_vertical_off");

    @Override public boolean renderWall(TileEntity tile, EnumFacing facing, int wall) { return true; }

    @Override public boolean changeConveyorDirection() { return false; }

    @Override public boolean setConveyorDirection(ConveyorDirection dir) { return false; }

    @Override public String getModelCacheKey(TileEntity tile, EnumFacing facing) {
        return "immersiveengineering:vertical" +
                "f" + facing.ordinal() +
                "a" + (isActive(tile) ? 1 : 0) +
                "b" + (renderBottomBelt(tile, facing) ? "1" + (renderBottomWall(tile, facing, 0) ? "1" : "0") + (renderBottomWall(tile, facing, 1) ? "1" : "0") : "000") +
                "c" + getDyeColour() +
                "_it";
    }

    protected boolean renderBottomBelt(TileEntity tile, EnumFacing facing) {
        TileEntity te = tile.getWorld().getTileEntity(tile.getPos().down());
        if (te instanceof IConveyorTile && ((IConveyorTile)te).getConveyorSubtype() != null) {
            for (EnumFacing f : ((IConveyorTile)te).getConveyorSubtype().sigTransportDirections(te, ((IConveyorTile)te).getFacing())) {
                if (f == EnumFacing.UP) return false;
            }
        }
        for (EnumFacing f : EnumFacing.HORIZONTALS) {
            if (f != facing && isInwardConveyor(tile, f)) return true;
        }
        return false;
    }

    protected boolean isInwardConveyor(TileEntity tile, EnumFacing f) {
        TileEntity te = tile.getWorld().getTileEntity(tile.getPos().offset(f));
        if (te instanceof IConveyorTile) {
            ConveyorHandler.IConveyorBelt sub = ((IConveyorTile)te).getConveyorSubtype();
            if (sub != null) {
                for (EnumFacing f2 : sub.sigTransportDirections(te, ((IConveyorTile)te).getFacing())) {
                    if (f2 == EnumFacing.UP) break;
                    if (f == f2.getOpposite()) return true;
                }
            }
        }
        te = tile.getWorld().getTileEntity(tile.getPos().down().offset(f));
        if (te instanceof IConveyorTile) {
            ConveyorHandler.IConveyorBelt sub = ((IConveyorTile)te).getConveyorSubtype();
            if (sub != null) {
                int b = 0;
                for (EnumFacing f2 : sub.sigTransportDirections(te, ((IConveyorTile)te).getFacing())) {
                    if (f == f2.getOpposite()) ++b;
                    else if (f2 == EnumFacing.UP) ++b;
                    if (b == 2) return true;
                }
            }
        }
        return false;
    }

    protected boolean renderBottomWall(TileEntity tile, EnumFacing facing, int wall) { return super.renderWall(tile, facing, wall); }

    @Override public EnumFacing[] sigTransportDirections(TileEntity conveyorTile, EnumFacing facing) { return new EnumFacing[]{EnumFacing.UP, facing}; }

    @Override public Vec3d getDirection(TileEntity conveyorTile, Entity entity, EnumFacing facing) {
        BlockPos posWall = conveyorTile.getPos().offset(facing);
        double d = 0.625 + entity.width;
        double distToWall = Math.abs((facing.getAxis() == EnumFacing.Axis.Z ? posWall.getZ() : posWall.getX()) + 0.5 - (facing.getAxis() == EnumFacing.Axis.Z ? entity.posZ : entity.posX));
        if (distToWall > d) return super.getDirection(conveyorTile, entity, facing);

        double vBase = entity instanceof EntityLivingBase ? 1.5 : 1.15;
        double distY = Math.abs(conveyorTile.getPos().up().getY() + 0.5 - entity.posY);
        boolean contact = distY < 0.9;
        double vX = entity.motionX;
        double vY = 0.1 * vBase;
        double vZ = entity.motionZ;
        if (entity.motionY < 0) vY += entity.motionY * 0.9;
        if (!(entity instanceof EntityPlayer)) {
            vX = 0.05 * facing.getXOffset();
            vZ = 0.05 * facing.getZOffset();
            if (facing.getAxis() == EnumFacing.Axis.X) {
                if (entity.posZ > conveyorTile.getPos().getZ() + 0.65) vZ = -0.1 * vBase;
                else if (entity.posZ < conveyorTile.getPos().getZ() + 0.35) vZ = 0.1 * vBase;
            } else {
                if (entity.posX > conveyorTile.getPos().getX() + 0.65) vX = -0.1 * vBase;
                else if (entity.posX < conveyorTile.getPos().getX() + 0.35) vX = 0.1 * vBase;
            }
        }
        BlockPos upForward = conveyorTile.getPos().up();
        if (contact && !(Utils.getExistingTileEntity(conveyorTile.getWorld(), upForward) instanceof IConveyorTile)) vY *= 2.25;
        return new Vec3d(vX, vY, vZ);
    }

    @Override public void onEntityCollision(TileEntity tile, Entity entity, EnumFacing facing) {
        if (!isPowered(tile)) return;

        super.onEntityCollision(tile, entity, facing); // throttled activation + normal movement

        BlockPos posWall = tile.getPos().offset(facing);
        double d = 0.625 + entity.width;
        double distToWall = Math.abs((facing.getAxis() == EnumFacing.Axis.Z ? posWall.getZ() : posWall.getX()) + 0.5 - (facing.getAxis() == EnumFacing.Axis.Z ? entity.posZ : entity.posX));
        if (distToWall > d) return;

        if (!entity.isDead && (!(entity instanceof EntityPlayer) || !entity.isSneaking())) {
            double distY = Math.abs(tile.getPos().up().getY() + 0.5 - entity.posY);
            boolean contact = distY < 0.9;
            entity.onGround = false;
            if (entity.fallDistance < 3.0F) entity.fallDistance = 0.0F;
            else entity.fallDistance *= 0.9F;
            Vec3d vec = getDirection(tile, entity, facing);
            entity.motionX = vec.x;
            entity.motionY = vec.y;
            entity.motionZ = vec.z;
            if (!contact) ConveyorHandler.applyMagnetSupression(entity, (IConveyorTile)tile);
            else {
                BlockPos posTop = tile.getPos().up();
                if (!(tile.getWorld().getTileEntity(posTop) instanceof IConveyorTile) && (!tile.getWorld().isAirBlock(posTop) || !(tile.getWorld().getTileEntity(posTop.offset(facing)) instanceof IConveyorTile))) {
                    ConveyorHandler.revertMagnetSupression(entity, (IConveyorTile)tile);
                }
            }
            if (entity instanceof EntityItem) {
                EntityItem item = (EntityItem)entity;
                if (!contact) item.setNoDespawn();
                else {
                    TileEntity inventoryTile = tile.getWorld().getTileEntity(tile.getPos().up());
                    if (!tile.getWorld().isRemote && inventoryTile != null && !(inventoryTile instanceof IConveyorTile)) {
                        ItemStack stack = item.getItem();
                        if (!stack.isEmpty()) {
                            ItemStack ret = Utils.insertStackIntoInventory(inventoryTile, stack, EnumFacing.DOWN);
                            if (ret.isEmpty()) entity.setDead();
                            else if (ret.getCount() < stack.getCount()) item.setItem(ret);
                        }
                    }
                }
            }
        }
    }

    @Override public List<AxisAlignedBB> getSelectionBoxes(TileEntity tile, EnumFacing facing) {
        List<AxisAlignedBB> list = new ArrayList<>();
        if (facing.ordinal() > 1) list.add(verticalBounds[facing.ordinal() - 2]);
        if (renderBottomBelt(tile, facing) || list.isEmpty()) list.add(conveyorBounds);
        return list;
    }

    @Override public List<AxisAlignedBB> getColisionBoxes(TileEntity tile, EnumFacing facing) { return getSelectionBoxes(tile, facing); }

    @SideOnly(Side.CLIENT)
    @Override public Matrix4f modifyBaseRotationMatrix(Matrix4f matrix, TileEntity tile, EnumFacing facing) {
        return new Matrix4(matrix).translate(0.0, 1.0, 0.0).rotate(Math.PI / 2.0, 1.0, 0.0, 0.0).toMatrix4f();
    }

    @Override public ResourceLocation getActiveTexture() { return texture_on; }

    @Override public ResourceLocation getInactiveTexture() { return texture_off; }

    @SideOnly(Side.CLIENT)
    @Override public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, @Nullable TileEntity tile, EnumFacing facing) {
        if (tile != null && renderBottomBelt(tile, facing)) {
            TextureAtlasSprite sprite = ClientUtils.getSprite(isActive(tile) ? texture_on : texture_off);
            TextureAtlasSprite spriteColour = ClientUtils.getSprite(getColouredStripesTexture());
            boolean[] walls = {renderBottomWall(tile, facing, 0), renderBottomWall(tile, facing, 1)};
            baseModel.addAll(ModelConveyor.getBaseConveyor(facing, 0.875F, new Matrix4(facing), ConveyorDirection.HORIZONTAL, sprite, walls, new boolean[]{true, false}, spriteColour, getDyeColour()));
        }
        return baseModel;
    }
}
