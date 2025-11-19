package mctmods.immersivetechnology.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Arrays;

public abstract class BlockITMultiblock<E extends Enum<E> & BlockITBase.IBlockEnum> extends BlockITTileProvider<E> {
    protected final boolean[] hasMultiblockTile;

    public BlockITMultiblock(String name, Material material, PropertyEnum<E> mainProperty, Class<? extends ItemBlockITBase> itemBlock, Object... additionalProperties) {
        super(name, material, mainProperty, itemBlock, combineProperties(additionalProperties, IEProperties.FACING_HORIZONTAL, IEProperties.MULTIBLOCKSLAVE));
        this.hasMultiblockTile = new boolean[this.enumValues.length];
        Arrays.fill(this.hasMultiblockTile, true);
    }

    @Override
    public @Nonnull IBlockState getActualState(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        state = super.getActualState(state, world, pos);
        return state;
    }

    @Override
    public void breakBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity instanceof TileEntityMultiblockPart && world.getGameRules().getBoolean("doTileDrops")) {
            TileEntityMultiblockPart<?> tile = (TileEntityMultiblockPart<?>)tileEntity;
            if(!tile.formed && tile.pos == -1 && !tile.getOriginalBlock().isEmpty()) world.spawnEntity(new EntityItem(world, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, tile.getOriginalBlock().copy()));
            if(tile.formed && tile instanceof IIEInventory) {
                IIEInventory master = (IIEInventory)tile.master();
                if(master != null && (!(master instanceof ITileDrop) || !((ITileDrop)master).preventInventoryDrop()) && master.getDroppedItems() != null) {
                    for(ItemStack s : master.getDroppedItems()) {
                        if(!s.isEmpty()) {
                            world.spawnEntity(new EntityItem(world, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, s.copy()));
                        }
                    }
                }
            }
        }
        if(tileEntity instanceof TileEntityMultiblockPart)
            ((TileEntityMultiblockPart<?>)tileEntity).disassemble();
        super.breakBlock(world, pos, state);
    }

    @Override
    public void getDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
        int meta = this.getMetaFromState(state);
        if(meta >= 0 && meta < this.hasMultiblockTile.length && !this.hasMultiblockTile[meta]) super.getDrops(drops, world, pos, state, fortune);
    }

    @Override
    public @Nonnull ItemStack getPickBlock(@Nonnull IBlockState state, @Nonnull RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
        ItemStack stack = getOriginalBlock(world, pos);

        if(!stack.isEmpty()) return stack;
        return super.getPickBlock(state, target, world, pos, player);
    }

    public ItemStack getOriginalBlock(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityMultiblockPart) return ((TileEntityMultiblockPart<?>)te).getOriginalBlock();
        return ItemStack.EMPTY;
    }

}
