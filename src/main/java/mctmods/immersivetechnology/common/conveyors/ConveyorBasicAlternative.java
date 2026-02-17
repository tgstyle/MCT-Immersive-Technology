package mctmods.immersivetechnology.common.conveyors;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorAttachable;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelConveyor;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ConveyorBasicAlternative implements IConveyorBelt {
    private ConveyorDirection direction = ConveyorDirection.HORIZONTAL;
    private int dyeColour = -1;

    public ConveyorBasicAlternative() {}

    @SideOnly(Side.CLIENT)
    @Override public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, @Nullable TileEntity tile, EnumFacing facing) {
        if (tile == null) {
            EnumFacing itemFacing = facing.getOpposite();
            Matrix4 mat = new Matrix4(itemFacing);
            TextureAtlasSprite sprite = ClientUtils.getSprite(isActive(null) ? getActiveTexture() : getInactiveTexture());
            TextureAtlasSprite spriteColour = ClientUtils.getSprite(getColouredStripesTexture());
            boolean[] walls = {false, false};
            return ModelConveyor.getBaseConveyor(itemFacing, 0.875F, mat, ConveyorDirection.HORIZONTAL, sprite, walls, new boolean[]{true, false}, spriteColour, getDyeColour());
        }
        return baseModel;
    }

    @SuppressWarnings("unused")
    @SideOnly(Side.CLIENT)
    public Matrix4f modifyBaseRotationMatrix(Matrix4f matrix, TileEntity tile, EnumFacing facing) { return matrix; }

    @Override public boolean changeConveyorDirection() {
        direction = direction == ConveyorDirection.HORIZONTAL ? ConveyorDirection.UP : direction == ConveyorDirection.UP ? ConveyorDirection.DOWN : ConveyorDirection.HORIZONTAL;
        return true;
    }

    @Override public boolean setConveyorDirection(ConveyorDirection dir) {
        direction = dir;
        return true;
    }

    @Override public boolean isActive(TileEntity tile) {
        if (tile == null) { return true; }
        return tile.getWorld().getRedstonePowerFromNeighbors(tile.getPos()) <= 0;
    }

    @Override public boolean canBeDyed() { return true; }

    @Override public boolean setDyeColour(int colour) {
        if (colour == this.dyeColour) { return false; }
        this.dyeColour = colour;
        return true;
    }

    @Override public int getDyeColour() { return this.dyeColour; }

    @Override public ConveyorDirection getConveyorDirection() { return direction; }

    @Override public NBTTagCompound writeConveyorNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("direction", direction.ordinal());
        nbt.setInteger("dyeColour", dyeColour);
        return nbt;
    }

    @Override public void readConveyorNBT(NBTTagCompound nbt) {
        direction = ConveyorDirection.values()[nbt.getInteger("direction")];
        dyeColour = nbt.hasKey("dyeColour") ? nbt.getInteger("dyeColour") : -1;
    }

    static final ResourceLocation texture_on = new ResourceLocation("immersiveengineering", "blocks/conveyor");
    static final ResourceLocation texture_off = new ResourceLocation("immersiveengineering", "blocks/conveyor_off");

    @Override public ResourceLocation getActiveTexture() { return texture_on; }

    @Override public ResourceLocation getInactiveTexture() { return texture_off; }

    @Override public ResourceLocation getColouredStripesTexture() { return new ResourceLocation("immersiveengineering", "blocks/conveyor_colour"); }

    @Override public boolean renderWall(TileEntity tile, EnumFacing facing, int wall) {
        if (tile == null) { return true; }
        if (this.getConveyorDirection() != ConveyorDirection.HORIZONTAL) { return true; }
        EnumFacing side = wall == 0 ? facing.rotateYCCW() : facing.rotateY();
        BlockPos pos = tile.getPos().offset(side);
        TileEntity te = Utils.getExistingTileEntity(tile.getWorld(), pos);
        if (te instanceof IConveyorAttachable) {
            boolean b = false;
            for (EnumFacing f : ((IConveyorAttachable) te).sigOutputDirections()) {
                if (f == side.getOpposite()) { b = true; }
                else if (f == EnumFacing.UP) { b = false; }
            }
            return !b;
        } else {
            te = Utils.getExistingTileEntity(tile.getWorld(), pos.add(0, -1, 0));
            if (te instanceof IConveyorAttachable) {
                int b = 0;
                for (EnumFacing f : ((IConveyorAttachable) te).sigOutputDirections()) {
                    if (f == side.getOpposite()) { ++b; }
                    else if (f == EnumFacing.UP) { ++b; }
                }
                return b < 2;
            } else { return true; }
        }
    }

    @Override public String getModelCacheKey(TileEntity tile, EnumFacing facing) {
        String key = "immersiveengineering:conveyor";
        key += "f" + facing.ordinal();
        key += "d" + getConveyorDirection().ordinal();
        key += "a" + (isActive(tile) ? 1 : 0);
        key += "w0" + (renderWall(tile, facing, 0) ? 1 : 0);
        key += "w1" + (renderWall(tile, facing, 1) ? 1 : 0);
        key += "c" + getDyeColour();
        return key;
    }

    @Override public Vec3d getDirection(TileEntity conveyorTile, Entity entity, EnumFacing facing) {
        ConveyorDirection conveyorDirection = getConveyorDirection();
        BlockPos pos = conveyorTile.getPos();
        double vBase = 1.15D;
        double vX = 0.1D * vBase * facing.getXOffset();
        double vY = entity.motionY;
        double vZ = 0.1D * vBase * facing.getZOffset();
        if (conveyorDirection == ConveyorDirection.UP) { vY = 0.17D * vBase; }
        else if (conveyorDirection == ConveyorDirection.DOWN) { vY = -0.07D * vBase; }
        if (conveyorDirection != ConveyorDirection.HORIZONTAL) { entity.onGround = false; }
        if (facing != EnumFacing.WEST && facing != EnumFacing.EAST) {
            if (entity.posX > pos.getX() + 0.55D) { vX = -0.1D * vBase; }
            else if (entity.posX < pos.getX() + 0.45D) { vX = 0.1D * vBase; }
        } else {
            if (entity.posZ > pos.getZ() + 0.55D) { vZ = -0.1D * vBase; }
            else if (entity.posZ < pos.getZ() + 0.45D) { vZ = 0.1D * vBase; }
        }
        return new Vec3d(vX, vY, vZ);
    }

    @Override public void onEntityCollision(TileEntity tile, Entity entity, EnumFacing facing) {
        if (!isActive(tile)) { return; }
        BlockPos pos = tile.getPos();
        ConveyorDirection conveyorDirection = getConveyorDirection();
        float heightLimit = conveyorDirection == ConveyorDirection.HORIZONTAL ? 0.25F : 1.0F;
        double height = entity.posY - pos.getY();
        if (entity.isDead || height < 0D || height >= heightLimit || entity instanceof EntityPlayer && entity.isSneaking()) { return; }
        Vec3d vec = getDirection(tile, entity, facing);
        entity.motionX = vec.x;
        entity.motionY = vec.y;
        entity.motionZ = vec.z;
        if (entity.fallDistance < 3.0F) { entity.fallDistance = 0.0F; }
        double distX = Math.abs(pos.offset(facing).getX() + 0.5D - entity.posX);
        double distZ = Math.abs(pos.offset(facing).getZ() + 0.5D - entity.posZ);
        boolean contact = facing.getAxis() == Axis.Z ? distZ < 0.9D : distX < 0.9D;
        if (contact && conveyorDirection == ConveyorDirection.UP) {
            IBlockState state = tile.getWorld().getBlockState(pos.offset(facing).up());
            if (!state.isFullBlock()) {
                double move = 0.4D;
                entity.setPosition(entity.posX + move * facing.getXOffset(), entity.posY + move, entity.posZ + move * facing.getZOffset());
            }
        }
        if (!contact) { ConveyorHandler.applyMagnetSupression(entity, (IConveyorTile)tile); }
        else {
            BlockPos nextPos = pos.offset(facing);
            TileEntity te = Utils.getExistingTileEntity(tile.getWorld(), nextPos);
            if (!(te instanceof IConveyorTile)) { ConveyorHandler.revertMagnetSupression(entity, (IConveyorTile)tile); }
        }
        if (entity instanceof EntityItem && entity.ticksExisted > 1) {
            EntityItem item = (EntityItem)entity;
            if (!contact) { item.setNoDespawn(); }
            else { handleInsertion(tile, item, facing, conveyorDirection, distX, distZ); }
        }
    }

    @Override public void onItemDeployed(TileEntity tile, EntityItem entity, EnumFacing facing) {
        Vec3d vec = getDirection(tile, entity, facing);
        entity.motionX = vec.x;
        entity.motionY = vec.y;
        entity.motionZ = vec.z;
    }
}
