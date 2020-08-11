/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import mctmods.immersivetechnology.common.Config;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityFluidPipeAlternative;
import mctmods.immersivetechnology.common.util.IPipe;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;

import java.util.Arrays;

public class BlockMetalDevice1 extends BlockIETileProvider<BlockTypes_MetalDevice1> {
	public BlockMetalDevice1() {
		super("metal_device1", Material.IRON, PropertyEnum.create("type", BlockTypes_MetalDevice1.class), ItemBlockIEBase.class, IEProperties.FACING_ALL, IEProperties.MULTIBLOCKSLAVE, IEProperties.BOOLEANS[0], Properties.AnimationProperty, IOBJModelCallback.PROPERTY, IEProperties.OBJ_TEXTURE_REMAP);
		this.setHardness(3.0F);
		this.setResistance(15.0F);
		lightOpacity = 0;
		this.setMetaBlockLayer(BlockTypes_MetalDevice1.CHARGING_STATION.getMeta(), BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
		this.setMetaBlockLayer(BlockTypes_MetalDevice1.FLUID_PIPE.getMeta(), BlockRenderLayer.CUTOUT);
		this.setMetaBlockLayer(BlockTypes_MetalDevice1.SAMPLE_DRILL.getMeta(), BlockRenderLayer.CUTOUT);
		this.setMetaBlockLayer(BlockTypes_MetalDevice1.FLOODLIGHT.getMeta(), BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
		this.setMetaBlockLayer(BlockTypes_MetalDevice1.ELECTRIC_LANTERN.getMeta(), BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
		this.setMetaBlockLayer(BlockTypes_MetalDevice1.FURNACE_HEATER.getMeta(), BlockRenderLayer.CUTOUT);
		this.setMetaLightOpacity(BlockTypes_MetalDevice1.FURNACE_HEATER.getMeta(), 255);
		this.setMetaLightOpacity(BlockTypes_MetalDevice1.DYNAMO.getMeta(), 255);
		this.setMetaLightOpacity(BlockTypes_MetalDevice1.THERMOELECTRIC_GEN.getMeta(), 255);
		this.setNotNormalBlock(BlockTypes_MetalDevice1.BLAST_FURNACE_PREHEATER.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.ELECTRIC_LANTERN.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.CHARGING_STATION.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.FLUID_PIPE.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.SAMPLE_DRILL.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.TESLA_COIL.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.FLOODLIGHT.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.TURRET_CHEM.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.TURRET_GUN.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.BELLJAR.getMeta());
		this.setMetaMobilityFlag(BlockTypes_MetalDevice1.BLAST_FURNACE_PREHEATER.getMeta(), EnumPushReaction.BLOCK);
		this.setMetaMobilityFlag(BlockTypes_MetalDevice1.SAMPLE_DRILL.getMeta(), EnumPushReaction.BLOCK);
		this.setMetaMobilityFlag(BlockTypes_MetalDevice1.TESLA_COIL.getMeta(), EnumPushReaction.BLOCK);
		this.setMetaMobilityFlag(BlockTypes_MetalDevice1.TURRET_CHEM.getMeta(), EnumPushReaction.BLOCK);
		this.setMetaMobilityFlag(BlockTypes_MetalDevice1.TURRET_GUN.getMeta(), EnumPushReaction.BLOCK);
		this.setMetaMobilityFlag(BlockTypes_MetalDevice1.BELLJAR.getMeta(), EnumPushReaction.BLOCK);
	}

	@Override
	public boolean useCustomStateMapper() {
		return true;
	}

	@Override
	public String getCustomStateMapping(int meta, boolean itemBlock) {
		if(BlockTypes_MetalDevice1.values()[meta]==BlockTypes_MetalDevice1.ELECTRIC_LANTERN) return "lantern";
		else if(BlockTypes_MetalDevice1.values()[meta]==BlockTypes_MetalDevice1.CHARGING_STATION) return "charging_station";
		else if(BlockTypes_MetalDevice1.values()[meta]==BlockTypes_MetalDevice1.FLUID_PIPE) return "pipe";
		else if(BlockTypes_MetalDevice1.values()[meta]==BlockTypes_MetalDevice1.SAMPLE_DRILL) return "core_drill";
		else if(BlockTypes_MetalDevice1.values()[meta]==BlockTypes_MetalDevice1.FLOODLIGHT) return "floodlight";
		else if(BlockTypes_MetalDevice1.values()[meta]==BlockTypes_MetalDevice1.BELLJAR) return "belljar";
		//else if(BlockTypes_MetalDevice1.values()[meta]==BlockTypes_MetalDevice1.TESLA_COIL) return "teslaCoil";
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected BlockStateContainer createBlockState() {
		BlockStateContainer base = super.createBlockState();
		IUnlistedProperty[] unlisted = (base instanceof ExtendedBlockState)?((ExtendedBlockState)base).getUnlistedProperties().toArray(new IUnlistedProperty[0]): new IUnlistedProperty[0];
		unlisted = Arrays.copyOf(unlisted, unlisted.length+1);
		unlisted[unlisted.length-1] = IEProperties.CONNECTIONS;
		return new ExtendedBlockState(this, base.getProperties().toArray(new IProperty[0]), unlisted);
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		state = super.getExtendedState(state, world, pos);
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof TileEntityImmersiveConnectable&&state instanceof IExtendedBlockState) state = ((IExtendedBlockState)state).withProperty(IEProperties.CONNECTIONS, ((TileEntityImmersiveConnectable)tile).genConnBlockstate());
		if(tile instanceof TileEntityElectricLantern) state = state.withProperty(IEProperties.BOOLEANS[0], ((TileEntityElectricLantern)tile).active);
		if(tile instanceof TileEntityFloodlight) state = state.withProperty(IEProperties.BOOLEANS[0], ((TileEntityFloodlight)tile).active);
		return state;
	}

	@Override
	public boolean canIEBlockBePlaced(World world, BlockPos pos, IBlockState newState, EnumFacing side, float hitX, float hitY, float hitZ, EntityPlayer player, ItemStack stack) {
		if(stack.getItemDamage()==BlockTypes_MetalDevice1.BLAST_FURNACE_PREHEATER.getMeta()||stack.getItemDamage()==BlockTypes_MetalDevice1.SAMPLE_DRILL.getMeta()||stack.getItemDamage()==BlockTypes_MetalDevice1.BELLJAR.getMeta()) {
			for(int hh = 1; hh <= 2; hh++) {
				BlockPos pos2 = pos.add(0, hh, 0);
				if(world.isOutsideBuildHeight(pos2)||!world.getBlockState(pos2).getBlock().isReplaceable(world, pos2)) return false;
			}
		} else if(stack.getItemDamage()==BlockTypes_MetalDevice1.TESLA_COIL.getMeta()) {
			BlockPos newPos = pos.offset(side);
			return !world.isOutsideBuildHeight(newPos)&&world.getBlockState(newPos).getBlock().isReplaceable(world, newPos);
		} else if(stack.getItemDamage()==BlockTypes_MetalDevice1.TURRET_CHEM.getMeta()||stack.getItemDamage()==BlockTypes_MetalDevice1.TURRET_GUN.getMeta()) {
			BlockPos newPos = pos.up();
			return !world.isOutsideBuildHeight(newPos)&&world.getBlockState(newPos).getBlock().isReplaceable(world, newPos);
		}
		return true;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof TileEntityTeslaCoil) return !((TileEntityTeslaCoil)tile).dummy;
		else if(tile instanceof TileEntityTurret) return !((TileEntityTurret)tile).dummy;
		else if(tile instanceof TileEntityFluidPipe) return !((TileEntityFluidPipe)tile).pipeCover.isEmpty();
		else if(tile instanceof TileEntityElectricLantern||tile instanceof TileEntityChargingStation||tile instanceof TileEntityFloodlight) return side==EnumFacing.DOWN;
		return true;
	}

	public TileEntity pipeImplementation() {
		if(Config.ITConfig.Experimental.replace_pipe_algorithm) {
			return new mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityFluidPipeAlternative();
		} else {
			return new mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityFluidPipe();
		}
	}


	@Override
	public TileEntity createBasicTE(World world, BlockTypes_MetalDevice1 type) {
		switch(type) {
			case BLAST_FURNACE_PREHEATER:
				return new TileEntityBlastFurnacePreheater();
			case FURNACE_HEATER:
				return new TileEntityFurnaceHeater();
			case DYNAMO:
				return new TileEntityDynamo();
			case THERMOELECTRIC_GEN:
				return new TileEntityThermoelectricGen();
			case ELECTRIC_LANTERN:
				return new TileEntityElectricLantern();
			case CHARGING_STATION:
				return new TileEntityChargingStation();
			case FLUID_PIPE:
				return pipeImplementation();
			case SAMPLE_DRILL:
				return new TileEntitySampleDrill();
			case TESLA_COIL:
				return new TileEntityTeslaCoil();
			case FLOODLIGHT:
				return new TileEntityFloodlight();
			case TURRET_CHEM:
				return new TileEntityTurretChem();
			case TURRET_GUN:
				return new TileEntityTurretGun();
			case BELLJAR:
				return new TileEntityBelljar();
		default:
			break;
		}
		return null;
	}

	@Override
	public boolean allowHammerHarvest(IBlockState state) {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		if(!Config.ITConfig.Experimental.replace_pipe_algorithm)
			mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityFluidPipe.indirectConnections.clear();
		return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		super.neighborChanged(state, world, pos, block, fromPos);
		if(!Config.ITConfig.Experimental.replace_pipe_algorithm && world.getBlockState(pos).getValue(property)==BlockTypes_MetalDevice1.FLUID_PIPE)
			mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityFluidPipe.indirectConnections.clear();
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if(state.getValue(property)==BlockTypes_MetalDevice1.FLUID_PIPE) {
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof IPipe) {
				IPipe here = (IPipe)te;
				for(int i = 0; i < 6; i++) {
					if(here.getSideConfig()[i]==-1) {
						EnumFacing f = EnumFacing.VALUES[i];
						TileEntity there = world.getTileEntity(pos.offset(f));
						if(there instanceof IPipe) ((IPipe)there).toggleSide(f.getOpposite().ordinal());
					}
				}
			}
			if(te instanceof TileEntityFluidPipeAlternative) {
				for(EnumFacing neighborDirection : EnumFacing.values()) {
					TileEntity neighbor = world.getTileEntity(pos.offset(neighborDirection));
					if(!(neighbor instanceof TileEntityFluidPipeAlternative)) continue;
					((TileEntityFluidPipeAlternative)neighbor).neighborPipeRemoved(neighborDirection.getOpposite());
				}
			}
		}
		super.breakBlock(world, pos, state);
	}

}