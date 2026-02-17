package mctmods.immersivetechnology.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelConveyor;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ConveyorExtractCoveredAlternative extends ConveyorExtractAlternative {
    private ItemStack cover = ItemStack.EMPTY;
    private long lastUpdateTick = 0;

    private static final AxisAlignedBB TOP_BOX = new AxisAlignedBB(0, 0.75, 0, 1, 1, 1);

    private static final float PIXEL = 0.0625F;
    private static final float ARM_Y_LOW = 0.375F;
    private static final float ARM_Y_HIGH = 1.0F;
    private static final float ARM_BASE_Z = 0.625F;
    private static final float CURTAIN_Y_LOW = 0.1875F;
    private static final float CURTAIN_Y_HIGH = 0.625F;
    private static final float CURTAIN_Z = 0.09375F;
    private static final float CURTAIN_Z_MAX = 0.125F;
    private static final float PLATE_THICKNESS = 0.125F;

    @SideOnly(Side.CLIENT)
    @Override public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, @Nullable TileEntity tile, EnumFacing facing) {
        EnumFacing renderFacing = tile == null ? facing.getOpposite() : facing;
        Matrix4 mat = new Matrix4(renderFacing);
        ConveyorDirection dir = ConveyorDirection.HORIZONTAL;
        TextureAtlasSprite sprite = ClientUtils.getSprite(isActive(tile) ? getActiveTexture() : getInactiveTexture());
        TextureAtlasSprite spriteColour = ClientUtils.getSprite(getColouredStripesTexture());
        boolean w0 = tile == null || renderWall(tile, facing, 0);
        boolean w1 = tile == null || renderWall(tile, facing, 1);

        List<BakedQuad> model = ModelConveyor.getBaseConveyor(renderFacing, 1.0F, mat, dir, sprite, new boolean[]{w0, w1}, new boolean[]{true, true}, spriteColour, getDyeColour());

        if (tile != null) initializeDirection(tile, facing);
        EnumFacing armDirection = (tile == null) ? facing : this.extractDirection;

        TextureAtlasSprite textureSteel = ClientUtils.getSprite(new ResourceLocation("immersiveengineering", "blocks/storage_steel"));
        TextureAtlasSprite textureCasing = ClientUtils.getSprite(new ResourceLocation("immersiveengineering", "blocks/wooden_device_turntable_bottom"));
        TextureAtlasSprite textureCurtain = ClientUtils.getSprite(new ResourceLocation("immersiveengineering", "blocks/cloth_device_stripcurtain"));
        TextureAtlasSprite textureAssembler = ClientUtils.getSprite(new ResourceLocation("immersiveengineering", "blocks/metal_multiblock_assembler"));

        float[] colour = {1.0F, 1.0F, 1.0F, 1.0F};
        Matrix4 armMatrix = new Matrix4(armDirection);

        float extend = getExtensionIntoBlock(tile);
        this.extension = extend;

        Function<EnumFacing, TextureAtlasSprite> getCasingSprite = f -> f.getAxis() == EnumFacing.Axis.Z ? textureSteel : textureCasing;
        Function<Vector3f[], Vector3f[]> vertexTransformer = vertices -> {
            if (extend == 0.0F) return vertices;
            Vector3f[] ret = new Vector3f[vertices.length];
            for (int i = 0; i < ret.length; ++i) {
                ret[i] = new Vector3f(vertices[i].x, vertices[i].y, vertices[i].z - extend);
            }
            return ret;
        };
        Function<Vector3f[], Vector3f[]> casingTransformer = vertices -> {
            Vector3f[] ret = new Vector3f[vertices.length];
            for (int i = 0; i < ret.length; ++i) {
                ret[i] = new Vector3f(vertices[i].x, vertices[i].y - 0.25F, vertices[i].z - 0.625F - extend);
            }
            return ret;
        };

        model.addAll(ClientUtils.createBakedBox(new Vector3f(PIXEL, ARM_Y_LOW, ARM_BASE_Z), new Vector3f(PIXEL * 3, ARM_Y_HIGH, 1.0F), armMatrix, renderFacing, casingTransformer, getCasingSprite, colour));
        model.addAll(ClientUtils.createBakedBox(new Vector3f(1.0F - PIXEL * 3, ARM_Y_LOW, ARM_BASE_Z), new Vector3f(1.0F - PIXEL, ARM_Y_HIGH, 1.0F), armMatrix, renderFacing, casingTransformer, getCasingSprite, colour));
        model.addAll(ClientUtils.createBakedBox(new Vector3f(PIXEL * 3, 0.875F, ARM_BASE_Z), new Vector3f(1.0F - PIXEL * 3, ARM_Y_HIGH, 1.0F), armMatrix, renderFacing, casingTransformer, getCasingSprite, colour));

        if (tile != null && extend > 0.0F) {
            TextureAtlasSprite texConveyor = ClientUtils.getSprite(isActive(tile) ? getActiveTexture() : getInactiveTexture());

            Function<EnumFacing, TextureAtlasSprite> getExtensionSprite = f -> f.getAxis() == EnumFacing.Axis.Y ? null : (f.getAxis() == EnumFacing.Axis.Z ? textureSteel : textureCasing);

            Vector3f[] vertices = {
                    new Vector3f(PIXEL, 0.0F, -extend),
                    new Vector3f(PIXEL, 0.0F, 0.0F),
                    new Vector3f(1.0F - PIXEL, 0.0F, 0.0F),
                    new Vector3f(1.0F - PIXEL, 0.0F, -extend)
            };

            model.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(armMatrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.DOWN, renderFacing), texConveyor, new double[]{15.0, extend * 16.0, 1.0, 0.0}, colour, true));

            for (Vector3f vec : vertices) vec.setY(PLATE_THICKNESS);
            model.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(armMatrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.UP, renderFacing), texConveyor, new double[]{15.0, (1.0F - extend) * 16.0, 1.0, 16.0}, colour, false));

            model.addAll(ClientUtils.createBakedBox(new Vector3f(PIXEL, 0.25F, ARM_BASE_Z), new Vector3f(1.0F - PIXEL, 0.375F, ARM_BASE_Z + extend), armMatrix, renderFacing, casingTransformer, getExtensionSprite, colour));
        }

        Vector3f[] vertices = {
                new Vector3f(0.8125F, 0.625F, 0.03125F),
                new Vector3f(0.8125F, 0.125F, 0.03125F),
                new Vector3f(0.1875F, 0.125F, 0.03125F),
                new Vector3f(0.1875F, 0.625F, 0.03125F)
        };

        model.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(armMatrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(EnumFacing.NORTH, renderFacing), textureAssembler, new double[]{15.25, 13.25, 12.75, 15.25}, colour, false));

        for (Vector3f vec : vertices) vec.setZ(PIXEL);
        model.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(armMatrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(EnumFacing.SOUTH, renderFacing), textureAssembler, new double[]{12.75, 13.25, 15.25, 15.25}, colour, true));

        for (int i = 0; i < 5; ++i) {
            float off = i * 0.125F;
            model.addAll(ClientUtils.createBakedBox(new Vector3f(0.203125F + off, CURTAIN_Y_LOW, CURTAIN_Z), new Vector3f(0.296875F + off, CURTAIN_Y_HIGH, CURTAIN_Z_MAX), armMatrix, renderFacing, vertexTransformer, f -> textureCurtain, colour));
        }

        ConveyorCoveredHelper.addCoverToQuads(model, renderFacing, () -> cover, dir, new boolean[]{w0, w1});
        return model;
    }

    @Override public String getModelCacheKey(TileEntity tile, EnumFacing facing) {
        initializeDirection(tile, facing);
        String key = "immersivetech:extract_covered_conveyor" +
                "f" + facing.ordinal() +
                "d" + getConveyorDirection().ordinal() +
                "a" + (isActive(tile) ? 1 : 0) +
                "w0" + (renderWall(tile, facing, 0) ? 1 : 0) +
                "w1" + (renderWall(tile, facing, 1) ? 1 : 0) +
                "c" + getDyeColour();
        EnumFacing effectiveDir = (tile == null) ? facing : this.extractDirection;
        key += "e" + effectiveDir.ordinal();
        key += "ex" + getExtensionIntoBlock(tile);
        if (!cover.isEmpty()) key += "s" + cover.getItem().getRegistryName() + cover.getMetadata();
        return key;
    }

    @Override public boolean playerInteraction(TileEntity tile, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, EnumFacing side) {
        if (super.playerInteraction(tile, player, hand, heldItem, hitX, hitY, hitZ, side)) return true;
        return ConveyorCoveredHelper.handleCoverInteraction(tile, player, hand, heldItem, () -> cover, stack -> cover = stack);
    }

    @Override public List<AxisAlignedBB> getColisionBoxes(TileEntity tile, EnumFacing facing) {
        List<AxisAlignedBB> list = new ArrayList<>(super.getColisionBoxes(tile, facing));
        list.add(TOP_BOX);
        return list;
    }

    @Override public List<AxisAlignedBB> getSelectionBoxes(TileEntity tile, EnumFacing facing) {
        return Lists.newArrayList(net.minecraft.block.Block.FULL_BLOCK_AABB);
    }

    @Override public NBTTagCompound writeConveyorNBT() {
        NBTTagCompound nbt = super.writeConveyorNBT();
        if (!cover.isEmpty()) nbt.setTag("cover", cover.writeToNBT(new NBTTagCompound()));
        return nbt;
    }

    @Override public void readConveyorNBT(NBTTagCompound nbt) {
        super.readConveyorNBT(nbt);
        cover = nbt.hasKey("cover") ? new ItemStack(nbt.getCompoundTag("cover")) : ItemStack.EMPTY;
    }

    @Override public void onEntityCollision(TileEntity tile, Entity entity, EnumFacing facing) {
        int oldRun = runTimer;
        runTimer = IDLE_TIME_TICKS;
        if (oldRun <= 0 && tile.getWorld().isRemote) {
            tile.getWorld().markBlockRangeForRenderUpdate(tile.getPos(), tile.getPos());
        }

        World world = tile.getWorld();
        if (!world.isRemote && world.getTotalWorldTime() - lastUpdateTick > 4) {
            tile.markDirty();
            IBlockState state = world.getBlockState(tile.getPos());
            world.notifyBlockUpdate(tile.getPos(), state, state, 3);
            lastUpdateTick = world.getTotalWorldTime();
        }

        double height = entity.posY - tile.getPos().getY();
        if (entity instanceof EntityItem) {
            ((EntityItem)entity).setPickupDelay(10);
            if (height >= 0.75) return;
        }
        super.onEntityCollision(tile, entity, facing);
    }

    @Override public void onItemDeployed(TileEntity tile, EntityItem entity, EnumFacing facing) {
        entity.setPickupDelay(10);
        ConveyorHandler.applyMagnetSupression(entity, (IConveyorTile)tile);
    }
}
