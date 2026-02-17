package mctmods.immersivetechnology.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelConveyor;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.renderer.block.model.BakedQuad;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ConveyorCoveredAlternative extends ConveyorBasicAlternative {
    private ItemStack cover = ItemStack.EMPTY;

    private static final AxisAlignedBB TOP_BOX = new AxisAlignedBB(0, 0.75, 0, 1, 1, 1);

    @SideOnly(Side.CLIENT)
    @Override public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, @Nullable TileEntity tile, EnumFacing facing) {
        EnumFacing renderFacing = tile == null ? facing.getOpposite() : facing;
        Matrix4 mat = new Matrix4(renderFacing);
        ConveyorDirection dir = tile == null ? ConveyorDirection.HORIZONTAL : getConveyorDirection();
        TextureAtlasSprite sprite = ClientUtils.getSprite(isActive(tile) ? getActiveTexture() : getInactiveTexture());
        TextureAtlasSprite spriteColour = ClientUtils.getSprite(getColouredStripesTexture());
        boolean w0 = tile == null || renderWall(tile, facing, 0);
        boolean w1 = tile == null || renderWall(tile, facing, 1);
        boolean[] corners = {true, true};

        List<BakedQuad> model = ModelConveyor.getBaseConveyor(renderFacing, 1.0F, mat, dir, sprite, new boolean[]{w0, w1}, corners, spriteColour, getDyeColour());
        ConveyorCoveredHelper.addCoverToQuads(model, renderFacing, () -> cover, dir, new boolean[]{w0, w1});
        return model;
    }

    @Override public String getModelCacheKey(TileEntity tile, EnumFacing facing) {
        String key = "immersivetech:covered_conveyor" +
                "f" + facing.ordinal() +
                "d" + getConveyorDirection().ordinal() +
                "a" + (isActive(tile) ? 1 : 0) +
                "w0" + (renderWall(tile, facing, 0) ? 1 : 0) +
                "w1" + (renderWall(tile, facing, 1) ? 1 : 0) +
                "c" + getDyeColour();
        if (!cover.isEmpty()) key += "s" + cover.getItem().getRegistryName() + cover.getMetadata();
        return key;
    }

    @Override public boolean playerInteraction(TileEntity tile, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, EnumFacing side) {
        return ConveyorCoveredHelper.handleCoverInteraction(tile, player, hand, heldItem, () -> cover, stack -> cover = stack);
    }

    private List<AxisAlignedBB> getSlopedCoverBoxes(EnumFacing facing, boolean isCollision) {
        boolean up = getConveyorDirection() == ConveyorDirection.UP;
        double minX = (facing == EnumFacing.WEST && !up) || (facing == EnumFacing.EAST && up) ? 0.5 : 0;
        double maxX = (facing == EnumFacing.WEST && up) || (facing == EnumFacing.EAST && !up) ? 0.5 : 1;
        double minZ = (facing == EnumFacing.NORTH && !up) || (facing == EnumFacing.SOUTH && up) ? 0.5 : 0;
        double maxZ = (facing == EnumFacing.NORTH && up) || (facing == EnumFacing.SOUTH && !up) ? 0.5 : 1;

        List<AxisAlignedBB> list = new ArrayList<>(1);
        if (isCollision) {
            list.add(new AxisAlignedBB(minX, 0.75, minZ, maxX, 1.0, maxZ));
        } else {
            list.add(new AxisAlignedBB(minX, 0.5, minZ, maxX, 2, maxZ));
            list.add(new AxisAlignedBB(minX, 0, minZ, maxX, 0.5, maxZ));
        }
        return list;
    }

    @Override public List<AxisAlignedBB> getColisionBoxes(TileEntity tile, EnumFacing facing) {
        List<AxisAlignedBB> list = new ArrayList<>(super.getColisionBoxes(tile, facing));
        if (getConveyorDirection() == ConveyorDirection.HORIZONTAL) list.add(TOP_BOX);
        else list.addAll(getSlopedCoverBoxes(facing, true));
        return list;
    }

    @Override public List<AxisAlignedBB> getSelectionBoxes(TileEntity tile, EnumFacing facing) {
        if (getConveyorDirection() == ConveyorDirection.HORIZONTAL) return com.google.common.collect.Lists.newArrayList(net.minecraft.block.Block.FULL_BLOCK_AABB);
        return getSlopedCoverBoxes(facing, false);
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
        super.onEntityCollision(tile, entity, facing);
        if (entity instanceof EntityItem) ((EntityItem)entity).setPickupDelay(10);
    }

    @Override public void onItemDeployed(TileEntity tile, EntityItem entity, EnumFacing facing) {
        entity.setPickupDelay(10);
    }
}
