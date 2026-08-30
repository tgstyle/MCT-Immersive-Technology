package mctmods.immersivetechnology.common.multiblocks;

import com.immersiveconvergence.api.client.split.SplitModelProperties;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import mctmods.immersivetechnology.common.blocks.BlockITBase;
import mctmods.immersivetechnology.common.shared.BlockITTileProvider;
import mctmods.immersivetechnology.common.blocks.ItemBlockITBase;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblock;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

import java.util.Arrays;

public abstract class BlockITMultiblock<E extends Enum<E> & BlockITBase.IBlockEnum> extends BlockITTileProvider<E> {

    protected final boolean[] hasMultiblockTile;

    public BlockITMultiblock(String name, Material material, PropertyEnum<E> mainProperty, Class<? extends ItemBlockITBase> itemBlock, Object... additionalProperties) {
        super(name, material, mainProperty, itemBlock, appendSplitProperty(combineProperties(additionalProperties)));
        this.hasMultiblockTile = new boolean[this.enumValues.length];
        Arrays.fill(this.hasMultiblockTile, true);
    }

    private static Object[] appendSplitProperty(Object[] properties) {
        Object[] array = new Object[properties.length + 1];
        System.arraycopy(properties, 0, array, 0, properties.length);
        array[properties.length] = SplitModelProperties.SUBMODEL_OFFSET;
        return array;
    }

    @Override @Nonnull public IBlockState getActualState(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) { return super.getActualState(state, world, pos); }

    @Override @Nonnull public IBlockState getExtendedState(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        state = super.getExtendedState(state, world, pos);
        if (state instanceof IExtendedBlockState) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityMultiblockPart && ((TileEntityMultiblockPart<?>)te).formed) {
                int[] offset = ((TileEntityMultiblockPart<?>)te).offset;
                state = ((IExtendedBlockState)state).withProperty(SplitModelProperties.SUBMODEL_OFFSET, new BlockPos(offset[0], offset[1], offset[2]));
            }
        }
        return state;
    }

    @Override public boolean removedByPlayer(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest) {
        if (!willHarvest) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityITMultiblock) {
                TileEntityITMultiblock<?, ?, ?> tile = (TileEntityITMultiblock<?, ?, ?>) te;
                tile.shouldDropOriginal = false;
                tile.shouldDropInventory = false;
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override public void breakBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityMultiblockPart) {
            TileEntityITMultiblock<?, ?, ?> tile = (TileEntityITMultiblock<?, ?, ?>) tileEntity;
            if (tile.formed && tile.shouldDropInventory) {
                IIEInventory master = tile.master();
                if (master != null && (!(master instanceof ITileDrop) || !((ITileDrop) master).preventInventoryDrop()) && master.getDroppedItems() != null) {
                    for (ItemStack s : master.getDroppedItems()) if (!s.isEmpty()) world.spawnEntity(new EntityItem(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, s.copy()));
                }
            }
            if (world.getGameRules().getBoolean("doTileDrops") && tile.shouldDropOriginal) if (!tile.formed && tile.pos == -1 && !tile.getOriginalBlock().isEmpty()) world.spawnEntity(new EntityItem(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, tile.getOriginalBlock().copy()));
        }
        if (tileEntity instanceof TileEntityMultiblockPart) ((TileEntityMultiblockPart<?>) tileEntity).disassemble();
        super.breakBlock(world, pos, state);
    }

    @Override public void getDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
        int meta = this.getMetaFromState(state);
        if (meta >= 0 && meta < this.hasMultiblockTile.length && !this.hasMultiblockTile[meta]) super.getDrops(drops, world, pos, state, fortune);
    }

    @Override @Nonnull public ItemStack getPickBlock(@Nonnull IBlockState state, @Nonnull RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
        ItemStack stack = getOriginalBlock(world, pos);
        if (!stack.isEmpty()) return stack;
        return super.getPickBlock(state, target, world, pos, player);
    }

    public ItemStack getOriginalBlock(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityMultiblockPart) return ((TileEntityMultiblockPart<?>) te).getOriginalBlock();
        return ItemStack.EMPTY;
    }

    @Override @Nonnull public AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        TileEntity te = source.getTileEntity(pos);
        if (te instanceof IBlockBounds) {
            float[] bounds = ((IBlockBounds) te).getBlockBounds();
            return new AxisAlignedBB(bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]).offset(pos);
        }
        return FULL_BLOCK_AABB;
    }

    @SuppressWarnings("deprecation")
    @SideOnly(Side.CLIENT)
    @Override @Nonnull public AxisAlignedBB getSelectedBoundingBox(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos) { return getBoundingBox(state, world, pos); }

    @Override @Nonnull public String getCustomStateMapping(int meta, boolean itemBlock) {
        if (!itemBlock && enumValues[meta].name().toLowerCase(java.util.Locale.US).endsWith("_slave")) return "multiblockSlave";
        return "";
    }

    @Override
    public boolean onBlockActivated(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) {
        return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }
}
