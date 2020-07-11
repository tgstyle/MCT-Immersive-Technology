package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import mctmods.immersivetechnology.common.blocks.BlockITTileProvider;
import mctmods.immersivetechnology.common.blocks.ItemBlockITBase;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityCokeOvenPreheater;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalDevice;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.Properties;

public class BlockMetalDevice extends BlockITTileProvider<BlockType_MetalDevice> {
	public BlockMetalDevice() {
		super("metal_device", Material.IRON, PropertyEnum.create("type", BlockType_MetalDevice.class), ItemBlockITBase.class, IEProperties.FACING_ALL, IEProperties.MULTIBLOCKSLAVE, IEProperties.BOOLEANS[0], Properties.AnimationProperty, IOBJModelCallback.PROPERTY, IEProperties.OBJ_TEXTURE_REMAP);
		this.setHardness(3.0F);
		this.setResistance(15.0F);
		lightOpacity = 0;
		this.setAllNotNormalBlock();
	}

	@Override
	public boolean useCustomStateMapper() {
		return true;
	}

	@Override
	public String getCustomStateMapping(int meta, boolean itemBlock) {
		if(BlockType_MetalDevice.values()[meta] == BlockType_MetalDevice.COKE_OVEN_PREHEATER) {
			return "coke_oven_preheater";
		}
		return null;
	}

	@Override
	public boolean canITBlockBePlaced(World world, BlockPos pos, IBlockState newState, EnumFacing side, float hitX, float hitY, float hitZ, EntityPlayer player, ItemStack stack) {
		if(stack.getItemDamage() == BlockType_MetalDevice.COKE_OVEN_PREHEATER.getMeta()) {
			EnumFacing f = EnumFacing.fromAngle(player.rotationYaw);
			if(f.getAxis() == Axis.Z) {
				return world.getBlockState(pos.add(1, 0, 0)).getBlock().isReplaceable(world, pos.add(1, 0, 0)) && world.getBlockState(pos.add(-1, 0, 0)).getBlock().isReplaceable(world, pos.add(-1, 0, 0));
			} else {
				return world.getBlockState(pos.add(0, 0, 1)).getBlock().isReplaceable(world, pos.add(0, 0, 1)) && world.getBlockState(pos.add(0, 0, -1)).getBlock().isReplaceable(world, pos.add(0, 0, -1));
			}
		}
		return true;
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		state = super.getExtendedState(state, world, pos);
		return state;
	}

	@Override
	public boolean allowHammerHarvest(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createBasicTE(World worldIn, BlockType_MetalDevice type) {
		switch(type) {
		case COKE_OVEN_PREHEATER:
			return new TileEntityCokeOvenPreheater();
		default:
			break;
		}
		return null;
	}

}