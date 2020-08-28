package mctmods.immersivetechnology.common.util.multiblock;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import mctmods.immersivetechnology.api.ITUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class ITMultiblock<T extends TileEntityMultiblockPart<T>> implements MultiblockHandler.IMultiblock {

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
    public byte[][][] collisionData;
    public ItemStack[][][] structureExport;
    public PoIJSONSchema[] pointsOfInterest;

    @Override
    public ItemStack[][][] getStructureManual() {
        return structureExport;
    }

    @Override
    public IngredientStack[] getTotalMaterials() {
        return materials;
    }

    @Override
    public boolean isBlockTrigger(IBlockState state) {
        return trigger.isEquals(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)));
    }

    public ITMultiblock(String structurePath, IBlockState master, IBlockState slave) {
        MultiblockJSONSchema data = MultiblockUtils.Load(structurePath);
        if(data == null) throw new IllegalArgumentException(String.format("Invalid or missing multiblock file %s", structurePath));

        this.uniqueName = data.uniqueName;
        this.masterBlockState = master;
        this.slaveBlockState = slave;
        this.width = data.width;
        this.length = data.length;
        this.height = data.height;
        this.pointsOfInterest = data.pointsOfInterest;
        this.masterX = data.master.x;
        this.masterY = data.master.y;
        this.masterZ = data.master.z;
        this.collisionData = data.AABB;
        this.structure = MultiblockUtils.GetStructure(data, width, length, height);
        this.materials = MultiblockUtils.GetMaterials(data);

        if(data.master.mod.equals("ore")) {
            trigger = new OreDictRef(data.master.name);
        } else {
            Item item = Item.getByNameOrId(data.master.mod + ":" + data.master.name);
            if(item == null) throw new IllegalArgumentException(String.format("Invalid item %s:%s",data.master.mod, data.master.name));
            trigger = new ItemStackRef(new ItemStack(item, 1, data.master.meta));
        }

        structureExport = MultiblockUtils.Convert(structure);
    }

    @Override
    public String getUniqueName() {
        return uniqueName;
    }

    @Override
    public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
        side = (side == EnumFacing.UP || side == EnumFacing.DOWN)? EnumFacing.fromAngle(player.rotationYaw) : side.getOpposite();

        boolean mirror = false;
        if(isInvalid(world, pos, side, mirror)) {
            mirror = true;
            if(isInvalid(world, pos, side, mirror)) return false;
        }
        BlockPos origin = pos.offset(side, -masterZ).offset(side.rotateY(), mirror ? width-1-masterX : -masterX).offset(EnumFacing.DOWN, masterY);
        ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(Lib.TOOL_HAMMER)?player.getHeldItemMainhand(): player.getHeldItemOffhand();
        if(MultiblockHandler.fireMultiblockFormationEventPost(player, this, pos, hammer).isCanceled()) return false;
        for(int h = 0; h < height; h++) {
            for(int l = 0; l < length; l++) {
                for(int w = 0; w < width; w++) {
                    int position = h * (width * length) + l * width + w;
                    if(collisionData[position] == null) continue;
                    BlockPos pos2 = ITUtils.LocalOffsetToWorldBlockPos(origin, mirror ? -w : w, h, l, side);
                    world.setBlockState(pos2, ITUtils.AreBlockPosIdentical(pos, pos2)? masterBlockState : slaveBlockState);
                    @SuppressWarnings("unchecked")
					T tile = (T)world.getTileEntity(pos2);
                    if(tile != null) {
                        tile.facing = side;
                        tile.formed = true;
                        tile.pos = position;
                        tile.offset = new int[] { pos2.getX() - pos.getX(), pos2.getY() - pos.getY(), pos2.getZ() - pos.getZ() };
                        tile.mirrored = mirror;
                        tile.markDirty();
                        world.addBlockEvent(pos2, slaveBlockState.getBlock(), 255, 0);
                    }
                }
            }
        }
        return true;
    }

    boolean isInvalid(World world, BlockPos pos, EnumFacing side, boolean mirror) {
        BlockPos origin = pos.offset(side, -masterZ).offset(side.rotateY(), mirror ? width-1-masterX : -masterX).offset(EnumFacing.DOWN, masterY);
        for(int h = 0; h < height; h++) {
            for(int l = 0; l < length; l++) {
                for(int w = 0; w < width; w++) {
                    if (structure[h][l][w] == AirRef.instance) continue;
                    BlockPos blockPos = ITUtils.LocalOffsetToWorldBlockPos(origin, mirror ? -w : w, h, l, side);
                    IBlockState state = world.getBlockState(blockPos);
                    if(!structure[h][l][w].isEquals(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)))) return true;
                }
            }
        }
        return false;
    }

    public boolean isPointOfInterest(EnumFacing accessSide, EnumFacing machineFacing, int position, String name) {
        for(PoIJSONSchema poi : pointsOfInterest) {
            if(    !poi.name.equals(name) ||
                    poi.position != position ||
                    poi.facing.LocalToGlobal(machineFacing) != accessSide) continue;
            return true;
        }
        return false;
    }
}
