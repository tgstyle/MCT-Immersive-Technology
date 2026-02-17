package mctmods.immersivetechnology.common.conveyors;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.Lists;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ConveyorExtractAlternative extends ConveyorBasicAlternative {
    EnumFacing extractDirection;
    private int transferCooldown = -1;
    private int transferTickrate = 8;
    float extension = -1.0F;

    public ConveyorExtractAlternative() { this.extractDirection = EnumFacing.NORTH; }

    void initializeDirection(@Nullable TileEntity tile, EnumFacing facing) {
        if (tile != null) { this.extractDirection = facing.getOpposite(); }
    }

    @SideOnly(Side.CLIENT)
    @Override public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, @Nullable TileEntity tile, EnumFacing facing) {
        initializeDirection(tile, facing);
        List<BakedQuad> model = super.modifyQuads(baseModel, tile, facing);
        EnumFacing armDirection = (tile == null) ? facing : this.extractDirection;
        TextureAtlasSprite texture_steel = ClientUtils.getSprite(new ResourceLocation("immersiveengineering", "blocks/storage_steel"));
        TextureAtlasSprite texture_casing = ClientUtils.getSprite(new ResourceLocation("immersiveengineering", "blocks/wooden_device_turntable_bottom"));
        TextureAtlasSprite texture_curtain = ClientUtils.getSprite(new ResourceLocation("immersiveengineering", "blocks/cloth_device_stripcurtain"));
        TextureAtlasSprite texture_assembler = ClientUtils.getSprite(new ResourceLocation("immersiveengineering", "blocks/metal_multiblock_assembler"));
        float[] colour = {1.0F, 1.0F, 1.0F, 1.0F};
        Matrix4 armMatrix = new Matrix4(armDirection);
        float extend = this.getExtensionIntoBlock(tile);
        this.extension = extend;
        Function<EnumFacing, TextureAtlasSprite> getCasingSprite = (f) -> f.getAxis() == EnumFacing.Axis.Z ? texture_steel : texture_casing;
        Function<Vector3f[], Vector3f[]> vertexTransformer = (vertices) -> {
            if (extend == 0.0F) return vertices;
            Vector3f[] ret = new Vector3f[vertices.length];
            for (int i = 0; i < ret.length; ++i) { ret[i] = new Vector3f(vertices[i].x, vertices[i].y, vertices[i].z - extend); }
            return ret;
        };
        Function<Vector3f[], Vector3f[]> casingTransformer = (vertices) -> {
            Vector3f[] ret = new Vector3f[vertices.length];
            for (int i = 0; i < ret.length; ++i) { ret[i] = new Vector3f(vertices[i].x, vertices[i].y - 0.25F, vertices[i].z - 0.625F - extend); }
            return ret;
        };
        model.addAll(ClientUtils.createBakedBox(new Vector3f(0.0625F, 0.375F, 0.625F), new Vector3f(0.1875F, 1.0F, 1.0F), armMatrix, facing, casingTransformer, getCasingSprite, colour));
        model.addAll(ClientUtils.createBakedBox(new Vector3f(0.8125F, 0.375F, 0.625F), new Vector3f(0.9375F, 1.0F, 1.0F), armMatrix, facing, casingTransformer, getCasingSprite, colour));
        model.addAll(ClientUtils.createBakedBox(new Vector3f(0.1875F, 0.875F, 0.625F), new Vector3f(0.8125F, 1.0F, 1.0F), armMatrix, facing, casingTransformer, getCasingSprite, colour));
        if (tile != null && extend > 0.0F) {
            TextureAtlasSprite tex_conveyor = ClientUtils.getSprite(this.isActive(tile) ? ConveyorBasicAlternative.texture_on : ConveyorBasicAlternative.texture_off);
            Function<EnumFacing, TextureAtlasSprite> getExtensionSprite = (f) -> f.getAxis() == EnumFacing.Axis.Y ? null : (f.getAxis() == EnumFacing.Axis.Z ? texture_steel : texture_casing);
            Vector3f[] vertices = {new Vector3f(0.0625F, 0.0F, -extend), new Vector3f(0.0625F, 0.0F, 0.0F), new Vector3f(0.9375F, 0.0F, 0.0F), new Vector3f(0.9375F, 0.0F, -extend)};
            model.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(armMatrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.DOWN, facing), tex_conveyor, new double[]{15.0, extend * 16.0, 1.0, 0.0}, colour, true));
            for (Vector3f vec : vertices) vec.setY(0.125F);
            model.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(armMatrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.UP, facing), tex_conveyor, new double[]{15.0, (1.0F - extend) * 16.0, 1.0, 16.0}, colour, false));
            model.addAll(ClientUtils.createBakedBox(new Vector3f(0.0625F, 0.25F, 0.625F), new Vector3f(0.9375F, 0.375F, 0.625F + extend), armMatrix, facing, casingTransformer, getExtensionSprite, colour));
        }
        Vector3f[] vertices = {new Vector3f(0.8125F, 0.625F, 0.03125F), new Vector3f(0.8125F, 0.125F, 0.03125F), new Vector3f(0.1875F, 0.125F, 0.03125F), new Vector3f(0.1875F, 0.625F, 0.03125F)};
        model.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(armMatrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(EnumFacing.NORTH, facing), texture_assembler, new double[]{15.25, 13.25, 12.75, 15.25}, colour, false));
        for (Vector3f vec : vertices) vec.setZ(0.0625F);
        model.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(armMatrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(EnumFacing.SOUTH, facing), texture_assembler, new double[]{12.75, 13.25, 15.25, 15.25}, colour, true));
        for (int i = 0; i < 5; ++i) {
            float off = i * 0.125F;
            model.addAll(ClientUtils.createBakedBox(new Vector3f(0.203125F + off, 0.1875F, 0.09375F), new Vector3f(0.296875F + off, 0.625F, 0.125F), armMatrix, facing, vertexTransformer, (f) -> texture_curtain, colour));
        }
        return model;
    }

    @Override public String getModelCacheKey(TileEntity tile, EnumFacing facing) {
        initializeDirection(tile, facing);
        String key = "immersivetech:extract_conveyor";
        key += "f" + facing.ordinal();
        key += "d" + getConveyorDirection().ordinal();
        key += "a" + (isActive(tile) ? 1 : 0);
        key += "w0" + (renderWall(tile, facing, 0) ? 1 : 0);
        key += "w1" + (renderWall(tile, facing, 1) ? 1 : 0);
        key += "c" + getDyeColour();
        EnumFacing effectiveDir = (tile == null) ? facing : this.extractDirection;
        key += "e" + effectiveDir.ordinal();
        key += "ex" + this.getExtensionIntoBlock(tile);
        return key;
    }

    @Override public boolean renderWall(TileEntity tile, EnumFacing facing, int wall) {
        initializeDirection(tile, facing);
        EnumFacing side = wall == 0 ? facing.rotateYCCW() : facing.rotateY();
        return side != this.extractDirection && super.renderWall(tile, facing, wall);
    }

    float getExtensionIntoBlock(TileEntity tile) {
        float extend = 0.0F;
        if (tile != null && tile.hasWorld()) {
            World world = tile.getWorld();
            BlockPos neighbour = tile.getPos().offset(this.extractDirection);
            if (!world.isAirBlock(neighbour)) {
                IBlockState connected = world.getBlockState(neighbour);
                TileEntity connectedTile = world.getTileEntity(neighbour);
                if (connectedTile != null && connectedTile.hasCapability(net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.extractDirection.getOpposite()) && connected.getBlockFaceShape(world, neighbour, this.extractDirection.getOpposite()) != BlockFaceShape.SOLID) {
                    AxisAlignedBB aabb = connected.getBoundingBox(world, neighbour);
                    switch (this.extractDirection) {
                        case NORTH: extend = (float)(1.0 - aabb.maxZ); break;
                        case SOUTH: extend = (float)aabb.minZ; break;
                        case WEST: extend = (float)(1.0 - aabb.maxX); break;
                        case EAST: extend = (float)aabb.minX; break;
                    }
                    if (extend > 0.25F) extend = 0.25F;
                    float round = extend % 0.0625F;
                    if (round < extend) extend = round + 0.0625F;
                }
            }
        }
        return extend;
    }

    @Override public boolean isActive(TileEntity tile) { return true; }

    private boolean isPowered(TileEntity tile) { return tile.getWorld().getRedstonePowerFromNeighbors(tile.getPos()) > 0; }

    @Override public boolean isTicking(TileEntity tile) { return true; }

    @Override public void onUpdate(TileEntity tile, EnumFacing facing) {
        initializeDirection(tile, facing);
        if (!tile.getWorld().isRemote) {
            if (this.transferCooldown > 0) { --this.transferCooldown; }
            if (!this.isPowered(tile) && this.transferCooldown <= 0) {
                World world = tile.getWorld();
                BlockPos neighbour = tile.getPos().offset(this.extractDirection);
                if (!world.isAirBlock(neighbour)) {
                    TileEntity neighbourTile = world.getTileEntity(neighbour);
                    if (neighbourTile != null && neighbourTile.hasCapability(net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.extractDirection.getOpposite())) {
                        net.minecraftforge.items.IItemHandler itemHandler = neighbourTile.getCapability(net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.extractDirection.getOpposite());
                        for (int i = 0; i < Objects.requireNonNull(itemHandler).getSlots(); ++i) {
                            ItemStack extractItem = itemHandler.extractItem(i, 1, true);
                            if (!extractItem.isEmpty()) {
                                extractItem = itemHandler.extractItem(i, 1, false);
                                EntityItem entity = new EntityItem(world, tile.getPos().getX() + 0.5, tile.getPos().getY() + 0.1875, tile.getPos().getZ() + 0.5, extractItem);
                                entity.motionX = 0;
                                entity.motionY = 0;
                                entity.motionZ = 0;
                                world.spawnEntity(entity);
                                this.onItemDeployed(tile, entity, facing);
                                this.transferCooldown = this.transferTickrate;
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override public boolean playerInteraction(TileEntity tile, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, EnumFacing side) {
        initializeDirection(tile, side);
        if (Utils.isHammer(heldItem) && player.isSneaking()) {
            EnumFacing dir = this.extractDirection.rotateY();
            if (dir == ((ConveyorHandler.IConveyorTile)tile).getFacing()) { dir = dir.rotateY(); }
            this.extractDirection = dir;
            return true;
        } else if (Utils.isWirecutter(heldItem)) {
            if (this.transferTickrate == 4) { this.transferTickrate = 8; }
            else if (this.transferTickrate == 8) { this.transferTickrate = 16; }
            else if (this.transferTickrate == 16) { this.transferTickrate = 20; }
            else if (this.transferTickrate == 20) { this.transferTickrate = 4; }
            player.sendStatusMessage(new TextComponentTranslation("chat.immersiveengineering.info.tickrate", this.transferTickrate), true);
            return true;
        }
        return false;
    }

    @Override public List<AxisAlignedBB> getColisionBoxes(TileEntity tile, EnumFacing facing) {
        initializeDirection(tile, facing);
        return Lists.newArrayList(conveyorBounds);
    }

    @Override public List<AxisAlignedBB> getSelectionBoxes(TileEntity tile, EnumFacing facing) {
        initializeDirection(tile, facing);
        List<AxisAlignedBB> list = Lists.newArrayList(conveyorBounds);
        if (this.extension < 0.0F) { this.extension = this.getExtensionIntoBlock(tile); }
        switch (this.extractDirection) {
            case NORTH: list.add(new AxisAlignedBB(0.0625, 0.125, -this.extension, 0.9375, 0.75, 0.375 - this.extension)); break;
            case SOUTH: list.add(new AxisAlignedBB(0.0625, 0.125, 0.625 + this.extension, 0.9375, 0.75, 1.0 + this.extension)); break;
            case WEST: list.add(new AxisAlignedBB(-this.extension, 0.125, 0.0625, 0.375 - this.extension, 0.75, 0.9375)); break;
            case EAST: list.add(new AxisAlignedBB(0.625 + this.extension, 0.125, 0.0625, 1.0 + this.extension, 0.75, 0.9375)); break;
        }
        return list;
    }

    @Override public NBTTagCompound writeConveyorNBT() {
        NBTTagCompound nbt = super.writeConveyorNBT();
        nbt.setInteger("extractDirection", this.extractDirection.ordinal());
        nbt.setInteger("transferCooldown", this.transferCooldown);
        nbt.setInteger("transferTickrate", this.transferTickrate);
        return nbt;
    }

    @Override public void readConveyorNBT(NBTTagCompound nbt) {
        super.readConveyorNBT(nbt);
        this.extractDirection = EnumFacing.values()[nbt.getInteger("extractDirection")];
        this.transferCooldown = nbt.getInteger("transferCooldown");
        this.transferTickrate = nbt.getInteger("transferTickrate");
    }
}
