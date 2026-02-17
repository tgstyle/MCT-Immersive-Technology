package mctmods.immersivetechnology.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelConveyor;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.vecmath.Matrix4f;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class ConveyorVerticalCoveredAlternative extends ConveyorVerticalAlternative {
    private ItemStack cover = ItemStack.EMPTY;

    private static final List<AxisAlignedBB> selectionBoxes = Collections.singletonList(net.minecraft.block.Block.FULL_BLOCK_AABB);

    private static final AxisAlignedBB[] topBounds = {
            new AxisAlignedBB(0, 0, 0.75, 1, 1, 1),
            new AxisAlignedBB(0, 0, 0, 1, 1, 0.25),
            new AxisAlignedBB(0.75, 0, 0, 1, 1, 1),
            new AxisAlignedBB(0, 0, 0, 0.25, 1, 1)
    };

    private static final AxisAlignedBB[] topBoundsCorner = {
            new AxisAlignedBB(0, 0.75, 0.75, 1, 1, 1),
            new AxisAlignedBB(0, 0.75, 0, 1, 1, 0.25),
            new AxisAlignedBB(0.75, 0.75, 0, 1, 1, 1),
            new AxisAlignedBB(0, 0.75, 0, 0.25, 1, 1)
    };

    @Override public String getModelCacheKey(TileEntity tile, EnumFacing facing) {
        return "immersiveengineering:verticalcovered" +
                "f" + facing.ordinal() +
                "a" + (isActive(tile) ? 1 : 0) +
                "b" + (renderBottomBelt(tile, facing) ? "1" + (isInwardConveyor(tile, facing.getOpposite()) ? "1" : "0") + (renderBottomWall(tile, facing, 0) ? "1" : "0") + (renderBottomWall(tile, facing, 1) ? "1" : "0") : "0000") +
                "c" + getDyeColour() +
                (!cover.isEmpty() ? "s" + cover.getItem().getRegistryName() + cover.getMetadata() : "") +
                "_it";
    }

    @Override public void onEntityCollision(TileEntity tile, Entity entity, EnumFacing facing) {
        super.onEntityCollision(tile, entity, facing);
        if (entity instanceof EntityItem) ((EntityItem)entity).setPickupDelay(10);
    }

    @Override public void onItemDeployed(TileEntity tile, EntityItem entity, EnumFacing facing) {
        entity.setPickupDelay(10);
    }

    @Override public boolean playerInteraction(TileEntity tile, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, EnumFacing side) {
        return ConveyorCoveredHelper.handleCoverInteraction(tile, player, hand, heldItem, () -> cover, stack -> cover = stack);
    }

    @Override public List<AxisAlignedBB> getSelectionBoxes(TileEntity tile, EnumFacing facing) {
        return selectionBoxes;
    }

    @Override public List<AxisAlignedBB> getColisionBoxes(TileEntity tile, EnumFacing facing) {
        List<AxisAlignedBB> list = new ArrayList<>();
        boolean bottom = renderBottomBelt(tile, facing);
        if (facing.ordinal() > 1) {
            list.add(verticalBounds[facing.ordinal() - 2]);
            list.add(bottom ? topBoundsCorner[facing.ordinal() - 2] : topBounds[facing.ordinal() - 2]);
        }
        if (bottom || list.isEmpty()) list.add(conveyorBounds);
        return list;
    }

    @SuppressWarnings("deprecation")
    @SideOnly(Side.CLIENT)
    @Override public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, @Nullable TileEntity tile, EnumFacing facing) {
        boolean renderBottom = tile != null && renderBottomBelt(tile, facing);
        if (renderBottom) {
            TextureAtlasSprite sprite = ClientUtils.getSprite(isActive(tile) ? texture_on : texture_off);
            TextureAtlasSprite spriteColour = ClientUtils.getSprite(getColouredStripesTexture());
            boolean[] walls = {renderBottomWall(tile, facing, 0), renderBottomWall(tile, facing, 1)};
            baseModel.addAll(ModelConveyor.getBaseConveyor(facing, 0.875F, new Matrix4(facing), ConveyorDirection.HORIZONTAL, sprite, walls, new boolean[]{true, false}, spriteColour, getDyeColour()));
        }

        ItemStack coverStack = cover.isEmpty() ? ConveyorCoveredHelper.defaultCover : cover;
        Block b = Block.getBlockFromItem(coverStack.getItem());
        IBlockState state = b.getStateFromMeta(coverStack.getMetadata());

        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
        TextureAtlasSprite particle = model.getParticleTexture();
        HashMap<EnumFacing, TextureAtlasSprite> sprites = new HashMap<>();
        for (EnumFacing f : EnumFacing.VALUES) {
            for (BakedQuad q : model.getQuads(state, f, 0L)) {
                if (q != null) sprites.put(f, q.getSprite());
            }
        }
        for (BakedQuad q : model.getQuads(state, null, 0L)) {
            if (q != null) sprites.put(q.getFace(), q.getSprite());
        }

        Function<EnumFacing, TextureAtlasSprite> getSprite = fx -> sprites.getOrDefault(fx, particle);
        float[] colour = {1.0F, 1.0F, 1.0F, 1.0F};
        Matrix4 matrix = new Matrix4(facing);

        if (!renderBottom) {
            baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, 0, 0.75F), new Vector3f(1, 1, 1), matrix, facing, getSprite, colour));
            baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, 0, 0.1875F), new Vector3f(0.0625F, 1, 0.75F), matrix, facing, getSprite, colour));
            baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0.9375F, 0, 0.1875F), new Vector3f(1, 1, 0.75F), matrix, facing, getSprite, colour));
        } else {
            boolean straightInput = isInwardConveyor(tile, facing.getOpposite());
            baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, 0.9375F, 0.75F), new Vector3f(1, 1, 1), matrix, facing, getSprite, colour));
            if (!straightInput) {
                baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, 0.1875F, 0.9375F), new Vector3f(1, 1, 1), matrix, facing, getSprite, colour));
            } else {
                baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, 0.75F, 0.9375F), new Vector3f(1, 1, 1), matrix, facing, getSprite, colour));
                baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, 0.1875F, 0.9375F), new Vector3f(0.0625F, 0.75F, 1), matrix, facing, getSprite, colour));
                baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0.9375F, 0.1875F, 0.9375F), new Vector3f(1, 0.75F, 1), matrix, facing, getSprite, colour));
            }

            boolean[] walls = {renderBottomWall(tile, facing, 0), renderBottomWall(tile, facing, 1)};
            if (walls[0]) {
                baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, 0.1875F, 0.1875F), new Vector3f(0.0625F, 1, 0.9375F), matrix, facing, getSprite, colour));
            } else {
                baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, 0.75F, 0.1875F), new Vector3f(0.0625F, 1, 0.9375F), matrix, facing, getSprite, colour));
            }
            if (walls[1]) {
                baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0.9375F, 0.1875F, 0.1875F), new Vector3f(1, 1, 0.9375F), matrix, facing, getSprite, colour));
            } else {
                baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0.9375F, 0.75F, 0.1875F), new Vector3f(1, 1, 0.9375F), matrix, facing, getSprite, colour));
            }
        }
        return baseModel;
    }

    @SideOnly(Side.CLIENT)
    @Override public Matrix4f modifyBaseRotationMatrix(Matrix4f matrix, TileEntity tile, EnumFacing facing) {
        return new Matrix4(matrix).translate(0.0, 1.0, 0.0).rotate(Math.PI / 2.0, 1.0, 0.0, 0.0).toMatrix4f();
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
}
