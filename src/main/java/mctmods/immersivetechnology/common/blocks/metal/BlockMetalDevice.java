package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;

import mctmods.immersivetechnology.common.blocks.ItemBlockITBase;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityAdvancedCokeOvenBaseheater;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityHeatCreative;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityRotorCreative;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalDevice;
import mctmods.immersivetechnology.common.shared.BlockITTileProvider;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nonnull;

public class BlockMetalDevice extends BlockITTileProvider<BlockType_MetalDevice> {

    public BlockMetalDevice() {
        super("metal_device", Material.IRON, PropertyEnum.create("type", BlockType_MetalDevice.class), ItemBlockITBase.class, IEProperties.FACING_ALL, IEProperties.MULTIBLOCKSLAVE, IEProperties.BOOLEANS[0], IEProperties.DYNAMICRENDER, IEProperties.TILEENTITY_PASSTHROUGH);
        this.setHardness(3.0F);
        this.setResistance(15.0F);
        lightOpacity = 0;
        this.setAllNotNormalBlock();
    }

    @Override public boolean useCustomStateMapper() { return true; }

    @Override @Nonnull public String getCustomStateMapping(int meta, boolean itemBlock) {
        switch (BlockType_MetalDevice.values()[meta]) {
            case ADVANCED_COKE_OVEN_BASEHEATER: { return "advanced_coke_oven_baseheater"; }
            case ROTOR_CREATIVE: { return "rotor_creative"; }
            case HEAT_CREATIVE: { return "heat_creative"; }
        }
        return "";
    }

    @Override public boolean canITBlockBePlaced(World world, BlockPos pos, IBlockState newState, EnumFacing side, float hitX, float hitY, float hitZ, EntityPlayer player, ItemStack stack) {
        if (stack.getItemDamage() == BlockType_MetalDevice.ADVANCED_COKE_OVEN_BASEHEATER.getMeta()) {
            EnumFacing f = EnumFacing.fromAngle(player.rotationYaw);
            if (f.getAxis() == Axis.Z) {
                return world.getBlockState(pos.add(1, 0, 0)).getBlock().isReplaceable(world, pos.add(1, 0, 0))
                        && world.getBlockState(pos.add(-1, 0, 0)).getBlock().isReplaceable(world, pos.add(-1, 0, 0));
            }
            return world.getBlockState(pos.add(0, 0, 1)).getBlock().isReplaceable(world, pos.add(0, 0, 1))
                    && world.getBlockState(pos.add(0, 0, -1)).getBlock().isReplaceable(world, pos.add(0, 0, -1));
        }
        return true;
    }

    @Override @Nonnull public IBlockState getActualState(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        state = super.getActualState(state, world, pos);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityAdvancedCokeOvenBaseheater) {
            TileEntityAdvancedCokeOvenBaseheater heater = (TileEntityAdvancedCokeOvenBaseheater) te;
            state = state.withProperty(IEProperties.BOOLEANS[0], heater.getIsActive());
            state = state.withProperty(IEProperties.MULTIBLOCKSLAVE, heater.dummy);
        }
        return state;
    }

    @Override @Nonnull public IBlockState getExtendedState(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        state = super.getExtendedState(state, world, pos);
        if (state instanceof IExtendedBlockState) {
            IExtendedBlockState extended = (IExtendedBlockState) state;
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityAdvancedCokeOvenBaseheater) { extended = extended.withProperty(IEProperties.TILEENTITY_PASSTHROUGH, te); }
            return extended;
        }
        return state;
    }

    @Override public boolean allowHammerHarvest(IBlockState state) { return true; }

    @Override @Nonnull public AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        TileEntity te = source.getTileEntity(pos);
        if (te instanceof ITBlockInterfaces.IBlockBounds) {
            float[] bounds = ((ITBlockInterfaces.IBlockBounds)te).getBlockBounds();
            return new AxisAlignedBB(bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
        }
        return super.getBoundingBox(state, source, pos);
    }

    @Override public TileEntity createBasicTE(World worldIn, BlockType_MetalDevice type) {
        switch (type) {
            case ADVANCED_COKE_OVEN_BASEHEATER: { return new TileEntityAdvancedCokeOvenBaseheater(); }
            case ROTOR_CREATIVE: { return new TileEntityRotorCreative(); }
            case HEAT_CREATIVE: { return new TileEntityHeatCreative(); }
        }
        return null;
    }
}
