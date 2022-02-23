package mctmods.immersivetechnology.common.blocks.metal.multiblocks;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityRadiatorSlave;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalMultiblock1;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MultiblockRadiator implements IMultiblock {
	public static MultiblockRadiator instance = new MultiblockRadiator();

	static ItemStack[][][] structure = new ItemStack[7][9][1];
	static {
		for(int h = 0 ; h < 7 ; h ++) {
			for(int l = 0 ; l < 9 ; l ++) {
				for(int w = 0 ; w < 1 ; w ++) {
					if (h == 3 && w == 0) {
						if (l == 0 || l == 8) {
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
						} else {
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDevice1, 1, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta());
						}
					}else
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RADIATOR.getMeta());
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	static ItemStack renderStack;

	@Override
	public String getUniqueName() {
		return "IT:Radiator";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state) {
		return state.getBlock() == IEContent.blockMetalDecoration0 && (state.getBlock().getMetaFromState(state) == BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
		side = (side == EnumFacing.UP || side == EnumFacing.DOWN)? EnumFacing.fromAngle(player.rotationYaw) : side.getOpposite();
		IBlockState master = ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.RADIATOR.getMeta());
		IBlockState slave = ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.RADIATOR_SLAVE.getMeta());
		boolean mirror = false;
		if(!this.structureCheck(world, pos, side, mirror)) {
			mirror = true;
			if(!this.structureCheck(world, pos, side, mirror)) return false;
		}
		if(player != null) {
			ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(Lib.TOOL_HAMMER)?player.getHeldItemMainhand(): player.getHeldItemOffhand();
			if(MultiblockHandler.fireMultiblockFormationEventPost(player, this, pos, hammer).isCanceled()) return false;
		}
		for(int h = -3 ; h <= 3 ; h ++) {
			for(int l = 0 ; l < 9 ; l ++) {
				for(int w = 0 ; w <= 0 ; w ++) {
					int ww = mirror ? h : w;
					int hh = mirror ? w : h;
					BlockPos pos2 = pos.offset(side, l).offset(side.rotateY(), ww).add(0, hh, 0);
					int[] offset = new int[] {(side == EnumFacing.WEST ? - l : side == EnumFacing.EAST ? l : side == EnumFacing.NORTH ? ww : - ww), hh, (side == EnumFacing.NORTH ? - l : side == EnumFacing.SOUTH ? l : side == EnumFacing.EAST ? ww : - ww)};
					world.setBlockState(pos2, (hh == 0 && l == 0 && ww == 0)? master : slave);
					TileEntity curr = world.getTileEntity(pos2);
					if(curr instanceof TileEntityRadiatorSlave) {
						TileEntityRadiatorSlave tile = (TileEntityRadiatorSlave)curr;
						tile.facing = side;
						tile.formed = true;
						tile.pos = (h + 3) * 9 + l;
						tile.offset = offset;
						tile.mirrored = mirror;
						tile.markDirty();
						world.addBlockEvent(pos2, ITContent.blockMetalMultiblock1, 255, 0);
					}
				}
			}
		}
		return true;
	}

	boolean structureCheck(World world, BlockPos startPos, EnumFacing dir, boolean mirror) {
		for(int h = -3 ; h <= 3 ; h ++) {
			for (int l = 0; l < 9; l++) {
				for (int w = 0; w <= 0; w++) {
					int ww = mirror ? h : w;
					int hh = mirror ? w : h;

					BlockPos pos = startPos.offset(dir, l).offset(dir.rotateY(), ww).add(0, hh, 0);

					if (hh == 0 && ww == 0) {
						if (l == 0 || l == 8) {
							if (!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()))
								return false;
						} else {
							if (!Utils.isBlockAt(world, pos, IEContent.blockMetalDevice1, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta()))
								return false;
						}
					} else {
						if (!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RADIATOR.getMeta()))
							return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public ItemStack[][][] getStructureManual() {
		return structure;
	}

	static final IngredientStack[] materials = new IngredientStack[] {
		new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 54, BlockTypes_MetalDecoration0.RADIATOR.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDevice1, 7, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta())),
		new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 2, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()))
	};

	@Override
	public IngredientStack[] getTotalMaterials() {
		return materials;
	}

	@Override
	public boolean overwriteBlockRender(ItemStack stack, int iterator) {
		return false;
	}

	@Override
	public float getManualScale() {
		return 6;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure() {
		if(renderStack == null) renderStack = new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.RADIATOR.getMeta());
		GlStateManager.translate(0.1, 0.25, 0.125);
		GlStateManager.translate(1, 3.5, 2);
		GlStateManager.rotate(- 45, 0, 1, 0);
		GlStateManager.rotate(- 20, 1, 0, 0);
		GlStateManager.scale(8, 8, 8);
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
	}

}