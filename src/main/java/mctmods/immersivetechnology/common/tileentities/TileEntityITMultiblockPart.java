package mctmods.immersivetechnology.common.tileentities;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.ITLogger;

import mctmods.immersivetechnology.common.util.multiblock.*;
import mctmods.immersivetechnology.common.util.shapes.BooleanOp;
import mctmods.immersivetechnology.common.util.shapes.VoxelShape;
import mctmods.immersivetechnology.common.util.shapes.Shapes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public abstract class TileEntityITMultiblockPart<T extends TileEntityMultiblockPart<T>> implements MultiblockHandler.IMultiblock {
    public String uniqueName;
    public IBlockState masterBlockState;
    public IBlockState slaveBlockState;
    public int height;
    public int length;
    public int width;
    public int masterX, masterY, masterZ;
    public IRefComparable trigger;
    public IRefComparable[][][] structure;
    public IngredientStack[] materials;
    public ItemStack[][][] structureExport;
    public PoIJSONSchema[] pointsOfInterest;
    public VoxelShape[] shapes;

    @Override public ItemStack[][][] getStructureManual() { return structureExport; }

    @Override public IngredientStack[] getTotalMaterials() { return materials; }

    @Override public boolean isBlockTrigger(IBlockState state) { return trigger.isEquals(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state))); }

    protected TileEntityITMultiblockPart(IBlockState master, IBlockState slave) {
        this.masterBlockState = master;
        this.slaveBlockState = slave;
        this.pointsOfInterest = new PoIJSONSchema[0];
    }

    public TileEntityITMultiblockPart(String structurePath, IBlockState master, IBlockState slave) {
        this.masterBlockState = master;
        this.slaveBlockState = slave;
        try {
            MultiblockJSONSchema data = MultiblockUtils.Load(structurePath);
            if (data == null) { ITLogger.error("Missing multiblock JSON: " + structurePath); return; }
            this.uniqueName = data.uniqueName;
            this.width = data.width;
            this.length = data.length;
            this.height = data.height;
            this.pointsOfInterest = data.pointsOfInterest != null ? data.pointsOfInterest : new PoIJSONSchema[0];
            this.masterX = data.master.x;
            this.masterY = data.master.y;
            this.masterZ = data.master.z;
            this.structure = MultiblockUtils.GetStructure(data, width, length, height);
            this.materials = MultiblockUtils.GetMaterials(data);
            this.structureExport = MultiblockUtils.Convert(this.structure);
            if (data.master.mod.equals("ore")) { trigger = new OreDictRef(data.master.name); }
            else {
                Item item = Item.getByNameOrId(data.master.mod + ":" + data.master.name);
                if (item == null) throw new IllegalArgumentException(String.format("Invalid item %s:%s", data.master.mod, data.master.name));
                trigger = new ItemStackRef(new ItemStack(item, 1, data.master.meta));
            }
            VoxelShape[] shapesArray = new VoxelShape[width * length * height];
            if (data.shapeAABB != null) {
                int expectedSize = width * length * height;
                if (data.shapeAABB.size() > 0 && data.shapeAABB.size() != expectedSize) { ITLogger.warn("ShapeAABB size mismatch for " + structurePath + ": expected " + expectedSize + ", got " + data.shapeAABB.size()); }
                for (int i = 0; i < Math.min(data.shapeAABB.size(), shapesArray.length); i++) {
                    JsonElement el = data.shapeAABB.get(i);
                    if (el.isJsonNull()) { shapesArray[i] = Shapes.create(0,0,0,1,1,1); continue; }
                    if (!el.isJsonArray()) { shapesArray[i] = Shapes.create(0,0,0,1,1,1); continue; }
                    JsonArray sub = el.getAsJsonArray();
                    if (sub.size() == 0) { shapesArray[i] = Shapes.create(0,0,0,1,1,1); continue; }
                    VoxelShape shape = Shapes.empty();
                    for (JsonElement b : sub) {
                        JsonArray box = b.getAsJsonArray();
                        double minX = box.get(0).getAsDouble();
                        double minY = box.get(1).getAsDouble();
                        double minZ = box.get(2).getAsDouble();
                        double maxX = box.get(3).getAsDouble();
                        double maxY = box.get(4).getAsDouble();
                        double maxZ = box.get(5).getAsDouble();
                        shape = Shapes.joinUnoptimized(shape, Shapes.box(minX, minY, minZ, maxX, maxY, maxZ), BooleanOp.OR);
                    }
                    shape = shape.optimize();
                    if (shape.isEmpty() && sub.size() > 0) { ITLogger.error("Shape became empty after optimize for index " + i + " in " + structurePath + " despite " + sub.size() + " boxes"); }
                    shapesArray[i] = shape;
                }
                for (int i = data.shapeAABB.size(); i < shapesArray.length; i++) { shapesArray[i] = Shapes.create(0,0,0,1,1,1); }
            } else {
                for (int i = 0; i < shapesArray.length; i++) { shapesArray[i] = Shapes.create(0,0,0,1,1,1); }
            }
            this.shapes = shapesArray;
            ITLogger.info("Loaded multiblock " + uniqueName + " with shapeAABB size " + (data.shapeAABB != null ? data.shapeAABB.size() : "null") + ", POI count " + pointsOfInterest.length);
        } catch (Exception e) { ITLogger.error("Error loading multiblock " + structurePath, e); }
    }

    @Override public String getUniqueName() { return uniqueName; }

    @Override public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
        side = (side == EnumFacing.UP || side == EnumFacing.DOWN)? EnumFacing.fromAngle(player.rotationYaw) : side.getOpposite();
        boolean mirror = false;
        if (isInvalid(world, pos, side, false)) { mirror = true; if (isInvalid(world, pos, side, true)) return false; }
        BlockPos origin = pos.offset(side, -masterZ).offset(side.rotateY(), mirror ? width-1-masterX : -masterX).offset(EnumFacing.DOWN, masterY);
        BlockPos masterPos = ITUtils.LocalOffsetToWorldBlockPos(origin, mirror ? (width - 1 - masterX) : masterX, masterY, masterZ, side);
        ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(Lib.TOOL_HAMMER)?player.getHeldItemMainhand(): player.getHeldItemOffhand();
        if (MultiblockHandler.fireMultiblockFormationEventPost(player, this, pos, hammer).isCanceled()) return false;
        IBlockState masterState = masterBlockState.withProperty(IEProperties.FACING_HORIZONTAL, side).withProperty(IEProperties.MULTIBLOCKSLAVE, false);
        IBlockState slaveState = slaveBlockState.withProperty(IEProperties.FACING_HORIZONTAL, side).withProperty(IEProperties.MULTIBLOCKSLAVE, true);
        for (int h = 0; h < height; h++) for (int l = 0; l < length; l++) for (int w = 0; w < width; w++) {
            if (structure[h][l][w] == AirRef.instance) continue;
            int position = h * (width * length) + l * width + w;
            BlockPos pos2 = ITUtils.LocalOffsetToWorldBlockPos(origin, mirror ? (width - 1 - w) : w, h, l, side);
            world.setBlockState(pos2, pos2.equals(masterPos) ? masterState : slaveState);
            @SuppressWarnings("unchecked")
            T tile = (T)world.getTileEntity(pos2);
            if (tile != null) {
                tile.facing = side;
                tile.formed = true;
                tile.pos = position;
                tile.offset = new int[] { pos2.getX() - masterPos.getX(), pos2.getY() - masterPos.getY(), pos2.getZ() - masterPos.getZ() };
                tile.mirrored = mirror;
                tile.markDirty();
                tile.markContainingBlockForUpdate(null);
                world.addBlockEvent(pos2, slaveBlockState.getBlock(), 255, 0);
                if (!world.isRemote) {
                    SPacketUpdateTileEntity packet = tile.getUpdatePacket();
                    for (EntityPlayer p : world.playerEntities) {
                        if (p instanceof EntityPlayerMP && p.getDistanceSq(pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5) < 1024.0D) ((EntityPlayerMP) p).connection.sendPacket(packet);
                    }
                }
            }
        }
        return true;
    }

    boolean isInvalid(World world, BlockPos pos, EnumFacing side, boolean mirror) {
        BlockPos origin = pos.offset(side, -masterZ).offset(side.rotateY(), mirror ? width-1-masterX : -masterX).offset(EnumFacing.DOWN, masterY);
        for (int h = 0; h < height; h++) for (int l = 0; l < length; l++) for (int w = 0; w < width; w++) {
            if (structure[h][l][w] == AirRef.instance) continue;
            BlockPos blockPos = ITUtils.LocalOffsetToWorldBlockPos(origin, mirror ? (width - 1 - w) : w, h, l, side);
            IBlockState state = world.getBlockState(blockPos);
            if (!structure[h][l][w].isEquals(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)))) return true;
        }
        return false;
    }
}
