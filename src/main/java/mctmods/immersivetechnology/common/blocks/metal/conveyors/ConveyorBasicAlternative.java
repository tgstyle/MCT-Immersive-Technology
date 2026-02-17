package mctmods.immersivetechnology.common.blocks.metal.conveyors;

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
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ConveyorBasicAlternative implements IConveyorBelt {
    protected ConveyorDirection direction = ConveyorDirection.HORIZONTAL;
    protected int dyeColour = -1;
    protected int runTimer = 0;
    protected long lastUpdateTick = 0;
    protected long lastActivationTick = 0;

    private static final ResourceLocation TEXTURE_ON = new ResourceLocation("immersiveengineering", "blocks/conveyor");
    private static final ResourceLocation TEXTURE_OFF = new ResourceLocation("immersiveengineering", "blocks/conveyor_off");
    private static final ResourceLocation TEXTURE_COLOURED = new ResourceLocation("immersiveengineering", "blocks/conveyor_colour");

    private static final double BASE_SPEED = 1.15D;
    private static final double LATERAL_SPEED = 0.1D * BASE_SPEED;
    private static final double UP_SPEED = 0.17D * BASE_SPEED;
    private static final double DOWN_SPEED = -0.07D * BASE_SPEED;
    private static final double CENTER_HIGH = 0.55D;
    private static final double CENTER_LOW = 0.45D;
    protected static final float HORIZONTAL_HEIGHT_LIMIT = 0.25F;
    protected static final float SLOPED_HEIGHT_LIMIT = 1.0F;
    protected static final double CONTACT_DIST = 0.9D;
    protected static final double UP_PUSH = 0.4D;
    protected static final float MAX_FALL_RESET = 3.0F;

    protected static final int IDLE_TIME_TICKS = 40;
    private static final int ACTIVATION_CHECK_INTERVAL = 10;

    public ConveyorBasicAlternative() {}

    @SideOnly(Side.CLIENT)
    @Override public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, @Nullable TileEntity tile, EnumFacing facing) {
        if (tile == null) {
            EnumFacing itemFacing = facing.getOpposite();
            Matrix4 mat = new Matrix4(itemFacing);
            TextureAtlasSprite sprite = ClientUtils.getSprite(isActive(null) ? getActiveTexture() : getInactiveTexture());
            TextureAtlasSprite spriteColour = ClientUtils.getSprite(getColouredStripesTexture());
            boolean[] walls = {true, true};
            return ModelConveyor.getBaseConveyor(itemFacing, 0.875F, mat, ConveyorDirection.HORIZONTAL, sprite, walls, new boolean[]{true, false}, spriteColour, getDyeColour());
        }
        return baseModel;
    }

    @SuppressWarnings("unused")
    @SideOnly(Side.CLIENT)
    public Matrix4 modifyBaseRotationMatrix(Matrix4 matrix, TileEntity tile, EnumFacing facing) { return matrix; }

    @Override public boolean changeConveyorDirection() {
        direction = direction == ConveyorDirection.HORIZONTAL ? ConveyorDirection.UP : direction == ConveyorDirection.UP ? ConveyorDirection.DOWN : ConveyorDirection.HORIZONTAL;
        return true;
    }

    @Override public boolean setConveyorDirection(ConveyorDirection dir) {
        direction = dir;
        return true;
    }

    protected boolean isPowered(TileEntity tile) {
        return tile.getWorld().getRedstonePowerFromNeighbors(tile.getPos()) <= 0;
    }

    @Override public boolean isActive(TileEntity tile) {
        if (tile == null) return true;
        return isPowered(tile) && runTimer > 0;
    }

    @Override public boolean isTicking(TileEntity tile) { return runTimer > 0; }

    @Override public void onUpdate(TileEntity tile, EnumFacing facing) {
        if (runTimer <= 0) return;
        --runTimer;
        if (runTimer != 0) return;

        if (!tile.getWorld().isRemote) {
            tile.markDirty();
            IBlockState state = tile.getWorld().getBlockState(tile.getPos());
            tile.getWorld().notifyBlockUpdate(tile.getPos(), state, state, 3);
        } else {
            tile.getWorld().markBlockRangeForRenderUpdate(tile.getPos(), tile.getPos());
        }
    }

    @Override public boolean canBeDyed() { return true; }

    @Override public boolean setDyeColour(int colour) {
        if (colour == this.dyeColour) return false;
        this.dyeColour = colour;
        return true;
    }

    @Override public int getDyeColour() { return this.dyeColour; }

    @Override public ConveyorDirection getConveyorDirection() { return direction; }

    @Override public NBTTagCompound writeConveyorNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("direction", direction.ordinal());
        nbt.setInteger("dyeColour", dyeColour);
        nbt.setInteger("runTimer", runTimer);
        return nbt;
    }

    @Override public void readConveyorNBT(NBTTagCompound nbt) {
        direction = ConveyorDirection.values()[nbt.getInteger("direction")];
        dyeColour = nbt.hasKey("dyeColour") ? nbt.getInteger("dyeColour") : -1;
        runTimer = nbt.getInteger("runTimer");
        lastActivationTick = 0;
    }

    @Override public ResourceLocation getActiveTexture() { return TEXTURE_ON; }

    @Override public ResourceLocation getInactiveTexture() { return TEXTURE_OFF; }

    @Override public ResourceLocation getColouredStripesTexture() { return TEXTURE_COLOURED; }

    @Override public boolean renderWall(TileEntity tile, EnumFacing facing, int wall) {
        if (tile == null) return true;
        if (this.getConveyorDirection() != ConveyorDirection.HORIZONTAL) return true;

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
        }

        te = Utils.getExistingTileEntity(tile.getWorld(), pos.add(0, -1, 0));
        if (te instanceof IConveyorAttachable) {
            int b = 0;
            for (EnumFacing f : ((IConveyorAttachable) te).sigOutputDirections()) {
                if (f == side.getOpposite()) ++b;
                else if (f == EnumFacing.UP) ++b;
            }
            return b < 2;
        }
        return true;
    }

    @Override public String getModelCacheKey(TileEntity tile, EnumFacing facing) {
        return "immersiveengineering:conveyor" +
                "f" + facing.ordinal() +
                "d" + getConveyorDirection().ordinal() +
                "a" + (isActive(tile) ? 1 : 0) +
                "w0" + (renderWall(tile, facing, 0) ? 1 : 0) +
                "w1" + (renderWall(tile, facing, 1) ? 1 : 0) +
                "c" + getDyeColour();
    }

    @Override public Vec3d getDirection(TileEntity conveyorTile, Entity entity, EnumFacing facing) {
        ConveyorDirection conveyorDirection = getConveyorDirection();
        BlockPos pos = conveyorTile.getPos();
        double vX = LATERAL_SPEED * facing.getXOffset();
        double vY = entity.motionY;
        double vZ = LATERAL_SPEED * facing.getZOffset();

        if (conveyorDirection == ConveyorDirection.UP) vY = UP_SPEED;
        else if (conveyorDirection == ConveyorDirection.DOWN) vY = DOWN_SPEED;
        if (conveyorDirection != ConveyorDirection.HORIZONTAL) entity.onGround = false;

        if (facing.getAxis() == Axis.X) {
            if (entity.posZ > pos.getZ() + CENTER_HIGH) vZ = -LATERAL_SPEED;
            else if (entity.posZ < pos.getZ() + CENTER_LOW) vZ = LATERAL_SPEED;
        } else {
            if (entity.posX > pos.getX() + CENTER_HIGH) vX = -LATERAL_SPEED;
            else if (entity.posX < pos.getX() + CENTER_LOW) vX = LATERAL_SPEED;
        }
        return new Vec3d(vX, vY, vZ);
    }

    @Override public void onEntityCollision(TileEntity tile, Entity entity, EnumFacing facing) {
        if (!isPowered(tile)) return;

        World world = tile.getWorld();
        long now = world.getTotalWorldTime();

        if (runTimer <= 0 && now - lastActivationTick >= ACTIVATION_CHECK_INTERVAL) {
            lastActivationTick = now;
            runTimer = IDLE_TIME_TICKS;

            if (!world.isRemote) {
                tile.markDirty();
                IBlockState state = world.getBlockState(tile.getPos());
                world.notifyBlockUpdate(tile.getPos(), state, state, 3);
            } else {
                world.markBlockRangeForRenderUpdate(tile.getPos(), tile.getPos());
            }
        }

        BlockPos pos = tile.getPos();
        ConveyorDirection conveyorDirection = getConveyorDirection();
        float heightLimit = conveyorDirection == ConveyorDirection.HORIZONTAL ? HORIZONTAL_HEIGHT_LIMIT : SLOPED_HEIGHT_LIMIT;
        double height = entity.posY - pos.getY();
        if (entity.isDead || height < 0D || height >= heightLimit || (entity instanceof EntityPlayer && entity.isSneaking())) return;

        Vec3d vec = getDirection(tile, entity, facing);
        entity.motionX = vec.x;
        entity.motionY = vec.y;
        entity.motionZ = vec.z;
        if (entity.fallDistance < MAX_FALL_RESET) entity.fallDistance = 0.0F;

        int offsetX = facing.getXOffset();
        int offsetZ = facing.getZOffset();
        double nextCenterX = pos.getX() + offsetX + 0.5D;
        double nextCenterZ = pos.getZ() + offsetZ + 0.5D;
        double distX = Math.abs(nextCenterX - entity.posX);
        double distZ = Math.abs(nextCenterZ - entity.posZ);
        boolean contact = facing.getAxis() == Axis.Z ? distZ < CONTACT_DIST : distX < CONTACT_DIST;

        if (contact) {
            if (conveyorDirection == ConveyorDirection.UP) {
                IBlockState state = tile.getWorld().getBlockState(new BlockPos(pos.getX() + offsetX, pos.getY() + 1, pos.getZ() + offsetZ));
                if (!state.isFullBlock()) {
                    double move = UP_PUSH;
                    entity.setPosition(entity.posX + move * offsetX, entity.posY + move, entity.posZ + move * offsetZ);
                }
            }
            BlockPos nextPos = new BlockPos(pos.getX() + offsetX, pos.getY(), pos.getZ() + offsetZ);
            TileEntity te = Utils.getExistingTileEntity(tile.getWorld(), nextPos);
            if (!(te instanceof IConveyorTile)) ConveyorHandler.revertMagnetSupression(entity, (IConveyorTile)tile);
        } else {
            ConveyorHandler.applyMagnetSupression(entity, (IConveyorTile)tile);
        }

        if (entity instanceof EntityItem && entity.ticksExisted > 1) {
            EntityItem item = (EntityItem)entity;
            if (!contact) item.setNoDespawn();
            else handleInsertion(tile, item, facing, conveyorDirection, distX, distZ);
        }
    }

    @Override public void onItemDeployed(TileEntity tile, EntityItem entity, EnumFacing facing) {
        Vec3d vec = getDirection(tile, entity, facing);
        entity.motionX = vec.x;
        entity.motionY = vec.y;
        entity.motionZ = vec.z;
    }
}
