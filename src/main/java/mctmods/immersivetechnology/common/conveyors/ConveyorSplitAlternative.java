package mctmods.immersivetechnology.common.conveyors;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelConveyor;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.List;

public class ConveyorSplitAlternative extends ConveyorBasicAlternative {
    private EnumFacing outputFace = null;

    public static final ResourceLocation texture_on = new ResourceLocation("immersiveengineering", "blocks/conveyor_split");
    public static final ResourceLocation texture_off = new ResourceLocation("immersiveengineering", "blocks/conveyor_split_off");
    public static final ResourceLocation texture_casing = new ResourceLocation("immersiveengineering", "blocks/conveyor_split_wall");

    public ConveyorSplitAlternative() {}

    @Override public ConveyorDirection getConveyorDirection() { return ConveyorDirection.HORIZONTAL; }

    @Override public boolean changeConveyorDirection() { return false; }

    @Override public boolean setConveyorDirection(ConveyorDirection dir) { return false; }

    @Override public void afterRotation(EnumFacing oldDir, EnumFacing newDir) {
        outputFace = newDir.rotateY();
    }

    @Override public void onEntityCollision(TileEntity tile, Entity entity, EnumFacing facing) {
        if (!isActive(tile) || entity == null || entity.isDead) return;

        String nbtKey = "immersiveengineering:conveyorDir" + Integer.toHexString(tile.getPos().hashCode());

        if (!entity.getEntityData().hasKey(nbtKey)) {
            BlockPos pos = tile.getPos();
            double dx = entity.posX - (pos.getX() + 0.5D);
            double dz = entity.posZ - (pos.getZ() + 0.5D);
            double distCenterSq = dx * dx + dz * dz;

            if (distCenterSq < 0.35D) {
                if (outputFace == null) {
                    outputFace = facing.rotateY();
                }
                EnumFacing assignedDir = outputFace;
                entity.getEntityData().setInteger(nbtKey, assignedDir.ordinal());
                outputFace = assignedDir.getOpposite();
                tile.markDirty();
            }
        }

        super.onEntityCollision(tile, entity, facing);

        if (entity.getEntityData().hasKey(nbtKey)) {
            EnumFacing assignedDir = EnumFacing.values()[entity.getEntityData().getInteger(nbtKey)];
            BlockPos nextPos = tile.getPos().offset(assignedDir);
            double distNext = Math.abs((assignedDir.getAxis() == EnumFacing.Axis.Z ? nextPos.getZ() : nextPos.getX()) + 0.5D
                    - (assignedDir.getAxis() == EnumFacing.Axis.Z ? entity.posZ : entity.posX));

            if (distNext < 0.4D) {
                entity.getEntityData().removeTag(nbtKey);
            }
        }
    }

    @Override public void handleInsertion(TileEntity tile, EntityItem entity, EnumFacing facing, ConveyorDirection conDir, double distX, double distZ) {
        String nbtKey = "immersiveengineering:conveyorDir" + Integer.toHexString(tile.getPos().hashCode());
        if (entity.getEntityData().hasKey(nbtKey)) {
            EnumFacing redirect = EnumFacing.values()[entity.getEntityData().getInteger(nbtKey)];
            BlockPos nextPos = tile.getPos().offset(redirect);
            double distNext = Math.abs((redirect.getAxis() == EnumFacing.Axis.Z ? nextPos.getZ() : nextPos.getX()) + 0.5D
                    - (redirect.getAxis() == EnumFacing.Axis.Z ? entity.posZ : entity.posX));
            if (distNext < 0.7D) {
                super.handleInsertion(tile, entity, redirect, conDir, distX, distZ);
            }
        }
    }

    @Override public boolean renderWall(TileEntity tile, EnumFacing facing, int wall) { return false; }

    @Override public EnumFacing[] sigTransportDirections(TileEntity conveyorTile, EnumFacing facing) { return new EnumFacing[]{facing.rotateY(), facing.rotateYCCW()}; }

    @Override public Vec3d getDirection(TileEntity conveyorTile, Entity entity, EnumFacing facing) {
        String nbtKey = "immersiveengineering:conveyorDir" + Integer.toHexString(conveyorTile.getPos().hashCode());

        if (!entity.getEntityData().hasKey(nbtKey)) {
            return super.getDirection(conveyorTile, entity, facing);
        }

        double vBase = 1.15D;
        double vX = 0.1D * vBase * facing.getXOffset();
        double vY = entity.motionY;
        double vZ = 0.1D * vBase * facing.getZOffset();

        Vec3d baseVec = new Vec3d(vX, vY, vZ);
        Vec3d vec = baseVec;

        EnumFacing redirect = EnumFacing.byIndex(entity.getEntityData().getInteger(nbtKey));

        BlockPos wallPos = conveyorTile.getPos().offset(facing);
        double distNext = Math.abs((facing.getAxis() == EnumFacing.Axis.Z ? wallPos.getZ() : wallPos.getX()) + 0.5D
                - (facing.getAxis() == EnumFacing.Axis.Z ? entity.posZ : entity.posX));

        if (distNext < 1.33D) {
            double sideMove = Math.pow(1.0D + distNext, 0.1D) * 0.2D;

            if (distNext < 0.8D) {
                vec = new Vec3d(
                        facing.getAxis() == EnumFacing.Axis.X ? 0.0D : baseVec.x,
                        baseVec.y,
                        facing.getAxis() == EnumFacing.Axis.Z ? 0.0D : baseVec.z
                );
            }

            vec = vec.add(redirect.getXOffset() * sideMove, 0.0D, redirect.getZOffset() * sideMove);
        }

        return vec;
    }

    @Override public NBTTagCompound writeConveyorNBT() {
        return super.writeConveyorNBT();
    }

    @Override public void readConveyorNBT(NBTTagCompound nbt) {
        super.readConveyorNBT(nbt);
        outputFace = null;
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
