package mctmods.immersivetechnology.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.Lists;
import net.minecraft.block.state.BlockFaceShape;
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
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
    protected EnumFacing extractDirection;
    protected int transferCooldown = -1;
    protected int transferTickrate = 8;
    protected float extension = -1.0F;
    private long lastUpdateTick = 0;

    private static final float PIXEL = 0.0625F;
    private static final float ARM_BASE_Z = 0.625F;
    private static final float ARM_EXTEND_MAX = 0.25F;
    private static final float ARM_Y_LOW = 0.375F;
    private static final float ARM_Y_HIGH = 1.0F;
    private static final float CURTAIN_Y_LOW = 0.1875F;
    private static final float CURTAIN_Y_HIGH = 0.625F;
    private static final float CURTAIN_Z = 0.09375F;
    private static final float CURTAIN_Z_MAX = 0.125F;
    private static final float PLATE_Y_LOW = 0.125F;
    private static final float PLATE_Y_HIGH = 0.75F;
    private static final float ITEM_SPAWN_Y = 0.1875F;
    private static final double ITEM_SPAWN_XZ_OFFSET = 0.5D;

    public ConveyorExtractAlternative() {
        this.extractDirection = EnumFacing.NORTH;
    }

    protected void initializeDirection(@Nullable TileEntity tile, EnumFacing facing) {
        if (tile != null) { this.extractDirection = facing.getOpposite(); }
    }

    protected float getExtensionIntoBlock(TileEntity tile) {
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
                    if (extend > ARM_EXTEND_MAX) extend = ARM_EXTEND_MAX;
                    float round = extend % PIXEL;
                    if (round < extend) extend = round + PIXEL;
                }
            }
        }
        return extend;
    }

    @SideOnly(Side.CLIENT)
    @Override public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, @Nullable TileEntity tile, EnumFacing facing) {
        initializeDirection(tile, facing);
        List<BakedQuad> model = super.modifyQuads(baseModel, tile, facing);

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

        model.addAll(ClientUtils.createBakedBox(new Vector3f(PIXEL, ARM_Y_LOW, ARM_BASE_Z), new Vector3f(PIXEL * 3, ARM_Y_HIGH, 1.0F), armMatrix, facing, casingTransformer, getCasingSprite, colour));
        model.addAll(ClientUtils.createBakedBox(new Vector3f(1.0F - PIXEL * 3, ARM_Y_LOW, ARM_BASE_Z), new Vector3f(1.0F - PIXEL, ARM_Y_HIGH, 1.0F), armMatrix, facing, casingTransformer, getCasingSprite, colour));
        model.addAll(ClientUtils.createBakedBox(new Vector3f(PIXEL * 3, 0.875F, ARM_BASE_Z), new Vector3f(1.0F - PIXEL * 3, ARM_Y_HIGH, 1.0F), armMatrix, facing, casingTransformer, getCasingSprite, colour));

        if (tile != null && extend > 0.0F) {
            TextureAtlasSprite texConveyor = ClientUtils.getSprite(isActive(tile) ? getActiveTexture() : getInactiveTexture());

            Function<EnumFacing, TextureAtlasSprite> getExtensionSprite = f -> f.getAxis() == EnumFacing.Axis.Y ? null : (f.getAxis() == EnumFacing.Axis.Z ? textureSteel : textureCasing);

            Vector3f[] vertices = {
                    new Vector3f(PIXEL, 0.0F, -extend),
                    new Vector3f(PIXEL, 0.0F, 0.0F),
                    new Vector3f(1.0F - PIXEL, 0.0F, 0.0F),
                    new Vector3f(1.0F - PIXEL, 0.0F, -extend)
            };

            model.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(armMatrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.DOWN, facing), texConveyor, new double[]{15.0, extend * 16.0, 1.0, 0.0}, colour, true));

            for (Vector3f vec : vertices) vec.setY(0.125F);
            model.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(armMatrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.UP, facing), texConveyor, new double[]{15.0, (1.0F - extend) * 16.0, 1.0, 16.0}, colour, false));

            model.addAll(ClientUtils.createBakedBox(new Vector3f(PIXEL, 0.25F, ARM_BASE_Z), new Vector3f(1.0F - PIXEL, 0.375F, ARM_BASE_Z + extend), armMatrix, facing, casingTransformer, getExtensionSprite, colour));
        }

        Vector3f[] vertices = {
                new Vector3f(0.8125F, 0.625F, 0.03125F),
                new Vector3f(0.8125F, 0.125F, 0.03125F),
                new Vector3f(0.1875F, 0.125F, 0.03125F),
                new Vector3f(0.1875F, 0.625F, 0.03125F)
        };

        model.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(armMatrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(EnumFacing.NORTH, facing), textureAssembler, new double[]{15.25, 13.25, 12.75, 15.25}, colour, false));

        for (Vector3f vec : vertices) vec.setZ(PIXEL);
        model.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(armMatrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(EnumFacing.SOUTH, facing), textureAssembler, new double[]{12.75, 13.25, 15.25, 15.25}, colour, true));

        for (int i = 0; i < 5; ++i) {
            float off = i * 0.125F;
            model.addAll(ClientUtils.createBakedBox(new Vector3f(0.203125F + off, CURTAIN_Y_LOW, CURTAIN_Z), new Vector3f(0.296875F + off, CURTAIN_Y_HIGH, CURTAIN_Z_MAX), armMatrix, facing, vertexTransformer, f -> textureCurtain, colour));
        }

        return model;
    }

    @Override public String getModelCacheKey(TileEntity tile, EnumFacing facing) {
        initializeDirection(tile, facing);
        String key = "immersivetech:extract_conveyor" +
                "f" + facing.ordinal() +
                "d" + getConveyorDirection().ordinal() +
                "a" + (isActive(tile) ? 1 : 0) +
                "w0" + (renderWall(tile, facing, 0) ? 1 : 0) +
                "w1" + (renderWall(tile, facing, 1) ? 1 : 0) +
                "c" + getDyeColour();
        EnumFacing effectiveDir = (tile == null) ? facing : this.extractDirection;
        key += "e" + effectiveDir.ordinal();
        key += "ex" + getExtensionIntoBlock(tile);
        return key;
    }

    @Override public boolean renderWall(TileEntity tile, EnumFacing facing, int wall) {
        initializeDirection(tile, facing);
        EnumFacing side = wall == 0 ? facing.rotateYCCW() : facing.rotateY();
        return side != this.extractDirection && super.renderWall(tile, facing, wall);
    }

    @Override public boolean isActive(TileEntity tile) {
        if (tile == null) { return true; }
        return runTimer > 0;
    }

    @Override public boolean isTicking(TileEntity tile) { return true; }

    @Override public void onUpdate(TileEntity tile, EnumFacing facing) {
        initializeDirection(tile, facing);
        if (!tile.getWorld().isRemote) {
            if (this.transferCooldown > 0) { --this.transferCooldown; }
            if (isPowered(tile) && this.transferCooldown <= 0) {
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
                                EntityItem entity = new EntityItem(world, tile.getPos().getX() + ITEM_SPAWN_XZ_OFFSET, tile.getPos().getY() + ITEM_SPAWN_Y, tile.getPos().getZ() + ITEM_SPAWN_XZ_OFFSET, extractItem);
                                entity.motionX = 0;
                                entity.motionY = 0;
                                entity.motionZ = 0;
                                world.spawnEntity(entity);
                                this.onItemDeployed(tile, entity, facing);
                                this.transferCooldown = this.transferTickrate;
                                runTimer = IDLE_TIME_TICKS;
                                tile.markDirty();
                                IBlockState state = world.getBlockState(tile.getPos());
                                world.notifyBlockUpdate(tile.getPos(), state, state, 3);
                                return;
                            }
                        }
                    }
                }
            }
        }
        if (runTimer > 0) {
            --runTimer;
            if (runTimer == 0 && !tile.getWorld().isRemote) {
                tile.markDirty();
                IBlockState state = tile.getWorld().getBlockState(tile.getPos());
                tile.getWorld().notifyBlockUpdate(tile.getPos(), state, state, 3);
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
        if (this.extension < 0.0F) { this.extension = getExtensionIntoBlock(tile); }
        switch (this.extractDirection) {
            case NORTH: list.add(new AxisAlignedBB(PIXEL, PLATE_Y_LOW, -this.extension, 1.0F - PIXEL, PLATE_Y_HIGH, 0.375F - this.extension)); break;
            case SOUTH: list.add(new AxisAlignedBB(PIXEL, PLATE_Y_LOW, 0.625F + this.extension, 1.0F - PIXEL, PLATE_Y_HIGH, 1.0F + this.extension)); break;
            case WEST: list.add(new AxisAlignedBB(-this.extension, PLATE_Y_LOW, PIXEL, 0.375F - this.extension, PLATE_Y_HIGH, 1.0F - PIXEL)); break;
            case EAST: list.add(new AxisAlignedBB(0.625F + this.extension, PLATE_Y_LOW, PIXEL, 1.0F + this.extension, PLATE_Y_HIGH, 1.0F - PIXEL)); break;
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

    @Override public void onEntityCollision(TileEntity tile, Entity entity, EnumFacing facing) {
        if (entity instanceof EntityItem) {
            runTimer = IDLE_TIME_TICKS;
            World world = tile.getWorld();
            if (!world.isRemote && world.getTotalWorldTime() - lastUpdateTick > 4) {
                tile.markDirty();
                IBlockState state = world.getBlockState(tile.getPos());
                world.notifyBlockUpdate(tile.getPos(), state, state, 3);
                lastUpdateTick = world.getTotalWorldTime();
            }
        }
        BlockPos pos = tile.getPos();
        ConveyorDirection conveyorDirection = getConveyorDirection();
        float heightLimit = conveyorDirection == ConveyorDirection.HORIZONTAL ? HORIZONTAL_HEIGHT_LIMIT : SLOPED_HEIGHT_LIMIT;
        double height = entity.posY - pos.getY();
        if (entity.isDead || height < 0D || height >= heightLimit || (entity instanceof EntityPlayer && entity.isSneaking())) { return; }

        Vec3d vec = getDirection(tile, entity, facing);
        entity.motionX = vec.x;
        entity.motionY = vec.y;
        entity.motionZ = vec.z;
        if (entity.fallDistance < MAX_FALL_RESET) { entity.fallDistance = 0.0F; }

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
            if (!(te instanceof IConveyorTile)) { ConveyorHandler.revertMagnetSupression(entity, (IConveyorTile)tile); }
        } else {
            ConveyorHandler.applyMagnetSupression(entity, (IConveyorTile)tile);
        }

        if (entity instanceof EntityItem && entity.ticksExisted > 1) {
            EntityItem item = (EntityItem)entity;
            if (!contact) { item.setNoDespawn(); }
            else { handleInsertion(tile, item, facing, conveyorDirection, distX, distZ); }
        }
    }
}