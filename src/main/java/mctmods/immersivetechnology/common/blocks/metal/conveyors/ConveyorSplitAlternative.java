package mctmods.immersivetechnology.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelConveyor;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.List;

public class ConveyorSplitAlternative extends ConveyorBasicAlternative {
    private enum SplitMode { SPLIT, ALL_LEFT, ALL_RIGHT, STOP }
    private SplitMode mode = SplitMode.SPLIT;
    private EnumFacing nextOutput = null;
    private transient String nbtKeyCache = null;
    private int prevRedstone = 0;
    private long lastUpdateTick = 0;

    private static final ResourceLocation texture_on = new ResourceLocation("immersiveengineering", "blocks/conveyor_split");
    private static final ResourceLocation texture_off = new ResourceLocation("immersiveengineering", "blocks/conveyor_split_off");
    private static final ResourceLocation texture_casing = new ResourceLocation("immersiveengineering", "blocks/conveyor_split_wall");

    private static final double FORWARD_SPEED = 0.100D;
    private static final double SIDE_SPEED = 0.10D;
    private static final double INITIAL_OFFSET = 0.04D;

    private static final float HORIZONTAL_HEIGHT_LIMIT = 0.25F;
    private static final double CONTACT_DIST = 0.9D;
    private static final float MAX_FALL_RESET = 3.0F;

    public ConveyorSplitAlternative() {}

    private String getNBTKey(TileEntity tile) {
        if (nbtKeyCache == null) nbtKeyCache = "it_split_dir_" + Integer.toHexString(tile.getPos().hashCode());
        return nbtKeyCache;
    }

    @Override public ConveyorDirection getConveyorDirection() { return ConveyorDirection.HORIZONTAL; }

    @Override public boolean changeConveyorDirection() { return false; }

    @Override public boolean setConveyorDirection(ConveyorDirection dir) { return false; }

    @Override public void afterRotation(EnumFacing oldDir, EnumFacing newDir) {
        if (nextOutput != null) nextOutput = nextOutput == oldDir.rotateY() ? newDir.rotateY() : newDir.rotateYCCW();
        nbtKeyCache = null;
    }

    @Override public boolean isActive(TileEntity tile) {
        if (tile == null) return true;
        return runTimer > 0 && mode != SplitMode.STOP;
    }

    @Override public boolean isTicking(TileEntity tile) { return true; }

    @Override public void onUpdate(TileEntity tile, EnumFacing facing) {
        super.onUpdate(tile, facing);
        int currentRedstone = tile.getWorld().getRedstonePowerFromNeighbors(tile.getPos());
        if (currentRedstone > 0 && this.prevRedstone == 0) {
            this.mode = this.mode == SplitMode.SPLIT ? SplitMode.ALL_LEFT
                    : this.mode == SplitMode.ALL_LEFT ? SplitMode.ALL_RIGHT
                      : this.mode == SplitMode.ALL_RIGHT ? SplitMode.STOP
                        : SplitMode.SPLIT;
            tile.markDirty();
        }
        this.prevRedstone = currentRedstone;
    }

    @Override public void onEntityCollision(TileEntity tile, Entity entity, EnumFacing facing) {
        if (mode == SplitMode.STOP) return;

        super.onEntityCollision(tile, entity, facing);

        World world = tile.getWorld();

        if (entity instanceof EntityItem) {
            if (!world.isRemote && world.getTotalWorldTime() - lastUpdateTick > 4) {
                tile.markDirty();
                IBlockState state = world.getBlockState(tile.getPos());
                world.notifyBlockUpdate(tile.getPos(), state, state, 3);
                lastUpdateTick = world.getTotalWorldTime();
            }
        }

        BlockPos pos = tile.getPos();
        double height = entity.posY - pos.getY();
        if (height < 0D || height >= HORIZONTAL_HEIGHT_LIMIT || (entity instanceof EntityPlayer && entity.isSneaking())) return;

        String nbtKey = getNBTKey(tile);
        boolean hasRedirect = entity.getEntityData().hasKey(nbtKey);

        if (!hasRedirect) {
            EnumFacing output;
            if (mode == SplitMode.SPLIT) {
                if (nextOutput == null) nextOutput = facing.rotateYCCW();
                output = nextOutput;
                nextOutput = nextOutput.getOpposite();
            } else if (mode == SplitMode.ALL_LEFT) {
                output = facing.rotateYCCW();
            } else {
                output = facing.rotateY();
            }
            entity.getEntityData().setInteger(nbtKey, output.ordinal());
            tile.markDirty();
        }

        Vec3d vec = getDirection(tile, entity, facing);
        entity.motionX = vec.x;
        entity.motionY = vec.y;
        entity.motionZ = vec.z;
        if (entity.fallDistance < MAX_FALL_RESET) entity.fallDistance = 0.0F;
        entity.onGround = false;

        double nextCenterX = pos.getX() + 0.5D + facing.getXOffset();
        double nextCenterZ = pos.getZ() + 0.5D + facing.getZOffset();
        double distX = Math.abs(nextCenterX - entity.posX);
        double distZ = Math.abs(nextCenterZ - entity.posZ);
        boolean contact = facing.getAxis() == EnumFacing.Axis.Z ? distZ < CONTACT_DIST : distX < CONTACT_DIST;

        if (contact) {
            TileEntity te = Utils.getExistingTileEntity(world, pos.offset(facing));
            if (!(te instanceof IConveyorTile)) ConveyorHandler.revertMagnetSupression(entity, (IConveyorTile)tile);
        } else {
            ConveyorHandler.applyMagnetSupression(entity, (IConveyorTile)tile);
        }

        if (entity instanceof EntityItem && entity.ticksExisted > 1) {
            EntityItem item = (EntityItem)entity;
            if (!contact) item.setNoDespawn();
            else handleInsertion(tile, item, facing, getConveyorDirection(), distX, distZ);
        }
    }

    @Override public void onItemDeployed(TileEntity tile, EntityItem entity, EnumFacing facing) {
        if (mode == SplitMode.STOP) return;

        runTimer = IDLE_TIME_TICKS;

        String nbtKey = getNBTKey(tile);
        if (!entity.getEntityData().hasKey(nbtKey)) {
            EnumFacing output;
            if (mode == SplitMode.SPLIT) {
                if (nextOutput == null) nextOutput = facing.rotateYCCW();
                output = nextOutput;
                nextOutput = nextOutput.getOpposite();
            } else if (mode == SplitMode.ALL_LEFT) {
                output = facing.rotateYCCW();
            } else {
                output = facing.rotateY();
            }
            entity.getEntityData().setInteger(nbtKey, output.ordinal());
            tile.markDirty();
        }

        Vec3d vec = getDirection(tile, entity, facing);
        entity.motionX = vec.x;
        entity.motionY = vec.y;
        entity.motionZ = vec.z;
    }

    @Override public void handleInsertion(TileEntity tile, EntityItem entity, EnumFacing facing, ConveyorDirection conDir, double distX, double distZ) {
        if (mode == SplitMode.STOP) return;

        String nbtKey = getNBTKey(tile);
        if (entity.getEntityData().hasKey(nbtKey)) {
            EnumFacing redirect = EnumFacing.values()[entity.getEntityData().getInteger(nbtKey)];
            super.handleInsertion(tile, entity, redirect, conDir, distX, distZ);
        } else {
            super.handleInsertion(tile, entity, facing, conDir, distX, distZ);
        }
    }

    @Override public boolean renderWall(TileEntity tile, EnumFacing facing, int wall) { return false; }

    @Override public EnumFacing[] sigTransportDirections(TileEntity conveyorTile, EnumFacing facing) {
        return new EnumFacing[]{facing.rotateY(), facing.rotateYCCW()};
    }

    @Override public Vec3d getDirection(TileEntity conveyorTile, Entity entity, EnumFacing facing) {
        if (mode == SplitMode.STOP) {
            return new Vec3d(0, entity.motionY, 0);
        }

        String nbtKey = getNBTKey(conveyorTile);
        if (entity.getEntityData().hasKey(nbtKey)) {
            EnumFacing redirect = EnumFacing.byIndex(entity.getEntityData().getInteger(nbtKey));

            double vX = FORWARD_SPEED * facing.getXOffset() + redirect.getXOffset() * SIDE_SPEED;
            double vZ = FORWARD_SPEED * facing.getZOffset() + redirect.getZOffset() * SIDE_SPEED;
            double vY = entity.motionY;

            vX += redirect.getXOffset() * INITIAL_OFFSET;
            vZ += redirect.getZOffset() * INITIAL_OFFSET;

            return new Vec3d(vX, vY, vZ);
        }
        return super.getDirection(conveyorTile, entity, facing);
    }

    @Override public String getModelCacheKey(TileEntity tile, EnumFacing facing) {
        return "immersivetech:split_conveyor" +
                "f" + facing.ordinal() +
                "d" + getConveyorDirection().ordinal() +
                "a" + (isActive(tile) ? 1 : 0) +
                "c" + getDyeColour();
    }

    @Override public NBTTagCompound writeConveyorNBT() {
        NBTTagCompound nbt = super.writeConveyorNBT();
        if (nextOutput != null) nbt.setInteger("nextOutput", nextOutput.ordinal());
        nbt.setInteger("mode", mode.ordinal());
        nbt.setInteger("prevRedstone", prevRedstone);
        return nbt;
    }

    @Override public void readConveyorNBT(NBTTagCompound nbt) {
        super.readConveyorNBT(nbt);
        nextOutput = nbt.hasKey("nextOutput") ? EnumFacing.values()[nbt.getInteger("nextOutput")] : null;
        mode = SplitMode.values()[nbt.getInteger("mode")];
        prevRedstone = nbt.getInteger("prevRedstone");
        nbtKeyCache = null;
    }

    @Override public ResourceLocation getActiveTexture() { return texture_on; }

    @Override public ResourceLocation getInactiveTexture() { return texture_off; }

    @SideOnly(Side.CLIENT)
    @Override public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, @Nullable TileEntity tile, EnumFacing facing) {
        TextureAtlasSprite tex_casing0 = ClientUtils.getSprite(texture_casing);
        Matrix4 matrix = new Matrix4(facing);
        float[] colour = new float[]{1.0F, 1.0F, 1.0F, 1.0F};

        Vector3f[] vertices = new Vector3f[]{new Vector3f(0.0625F, 0.1875F, 0.0F), new Vector3f(0.0625F, 0.1875F, 1.0F),
                new Vector3f(0.9375F, 0.1875F, 1.0F), new Vector3f(0.9375F, 0.1875F, 0.0F)};
        baseModel.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices),
                EnumFacing.UP, tex_casing0, new double[]{1.0D, 16.0D, 15.0D, 0.0D}, colour, false));

        vertices = new Vector3f[]{new Vector3f(0.0625F, 0.0F, 0.0F), new Vector3f(0.0625F, 0.1875F, 0.0F),
                new Vector3f(0.9375F, 0.1875F, 0.0F), new Vector3f(0.9375F, 0.0F, 0.0F)};
        baseModel.set(15, ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices),
                facing, ClientUtils.getSprite(ModelConveyor.rl_casing[1]), new double[]{1.0D, 16.0D, 15.0D, 13.0D}, colour, false));

        vertices = new Vector3f[]{new Vector3f(0.0625F, 0.125F, 0.0F), new Vector3f(0.0625F, 0.1875F, 0.0F),
                new Vector3f(0.9375F, 0.1875F, 0.0F), new Vector3f(0.9375F, 0.125F, 0.0F)};
        Vector3f[] vertices2 = new Vector3f[]{new Vector3f(0.5F, 0.125F, 0.0F), new Vector3f(0.5F, 0.125F, 0.5F),
                new Vector3f(0.5F, 0.1875F, 0.5F), new Vector3f(0.5F, 0.1875F, 0.0F)};
        Vector3f[] vertices3 = new Vector3f[]{new Vector3f(0.5F, 0.125F, 0.0F), new Vector3f(0.5F, 0.125F, 0.5F),
                new Vector3f(0.5F, 0.1875F, 0.5F), new Vector3f(0.5F, 0.1875F, 0.0F)};

        for (int i = 0; i < 8; ++i) {
            for (int iv = 0; iv < vertices.length; ++iv) {
                vertices[iv].setZ((i + 1) * 0.0625F);
                vertices2[iv].setX(vertices2[iv].getX() + 0.0625F);
                vertices3[iv].setX(vertices3[iv].getX() - 0.0625F);
            }
            double v = 16.0D - i;
            baseModel.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices),
                    facing, tex_casing0, new double[]{1.0D, v - 1.0D, 15.0D, v}, colour, true));
            if (i < 7) {
                double u = 8.0D - i;
                baseModel.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices2),
                        facing, tex_casing0, new double[]{u - 1.0D, 16.0D, u, 8.0D}, colour, true));
                baseModel.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices3),
                        facing, tex_casing0, new double[]{u - 1.0D, 16.0D, u, 8.0D}, colour, false));
            }
        }
        return baseModel;
    }
}
