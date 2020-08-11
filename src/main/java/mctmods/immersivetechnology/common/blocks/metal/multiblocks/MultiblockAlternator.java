package mctmods.immersivetechnology.common.blocks.metal.multiblocks;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsIE;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice0;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityAlternatorSlave;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalMultiblock;
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

public class MultiblockAlternator implements IMultiblock {
	public static MultiblockAlternator instance = new MultiblockAlternator();

	static ItemStack[][][] structure = new ItemStack[3][4][3];
	static {
		for(int h = 0 ; h < 3 ; h ++) {
			for(int l = 0 ; l < 4 ; l ++) {
				for(int w = 0 ; w < 3 ; w ++) {
					if(l == 1 && h == 2) continue;
					if(h == 1 && w == 1 && l>1) {
						structure[h][l][w] = new ItemStack(IEContent.blockStorage, 1, BlockTypes_MetalsIE.STEEL.getMeta());
					} else if(l == 3) {
						structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.STEEL.getMeta());
					} else if(l == 2) {
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.GENERATOR.getMeta());
					} else if((l == 0 && w == 1) || (l == 1 && w == 1 && h == 1)) {
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
					} else if(l == 1) {
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDevice0, 1, BlockTypes_MetalDevice0.CAPACITOR_HV.getMeta());
					} else {
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.COIL_HV.getMeta());
					}
				}
			}
		}
	}

	@Override
	public String getUniqueName() {
		return "IT:Alternator";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state) {
		return state.getBlock() == IEContent.blockMetalDecoration0 && (state.getBlock().getMetaFromState(state) == BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
		side = (side == EnumFacing.UP || side == EnumFacing.DOWN)? EnumFacing.fromAngle(player.rotationYaw) : side.getOpposite();
		IBlockState master = ITContent.blockMetalMultiblock.getStateFromMeta(BlockType_MetalMultiblock.ALTERNATOR.getMeta());
		IBlockState slave = ITContent.blockMetalMultiblock.getStateFromMeta(BlockType_MetalMultiblock.ALTERNATOR_SLAVE.getMeta());
		if(!this.structureCheck(world, pos, side)) return false;
		if(player != null) {
			ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(Lib.TOOL_HAMMER)?player.getHeldItemMainhand(): player.getHeldItemOffhand();
			if(MultiblockHandler.fireMultiblockFormationEventPost(player, this, pos, hammer).isCanceled()) return false;
		}
		for(int h = - 1 ; h <= 1 ; h ++) {
			for(int l = 0 ; l <= 3 ; l ++) {
				for(int w = - 1 ; w <= 1 ; w ++) {
					if(l == 1 && h == 1) continue;
					BlockPos pos2 = pos.offset(side, l).offset(side.rotateY(), w).add(0, h, 0);
					int[] offset = new int[] {(side == EnumFacing.WEST ? - l : side == EnumFacing.EAST ? l : side == EnumFacing.NORTH ? w : - w), h, (side == EnumFacing.NORTH ? - l : side == EnumFacing.SOUTH ? l : side == EnumFacing.EAST ? w : - w)};
					world.setBlockState(pos2, (offset[0]==0&&offset[1]==0&&offset[2]==0)? master : slave);
					TileEntity curr = world.getTileEntity(pos2);
					if(curr instanceof TileEntityAlternatorSlave) {
						TileEntityAlternatorSlave tile = (TileEntityAlternatorSlave)curr;
						tile.facing = side;
						tile.formed = true;
						tile.pos = (h + 1) * 12 + l * 3 + (w + 1);
						tile.offset = offset;
						tile.markDirty();
						world.addBlockEvent(pos2, ITContent.blockMetalMultiblock, 255, 0);
					}
				}
			}
		}
		return true;
	}

	private boolean structureCheck(World world, BlockPos startPos, EnumFacing dir) {
		for(int h = - 1 ; h <= 1 ; h ++) {
			for(int l = 0 ; l <= 3 ; l ++) {
				for(int w = - 1 ; w <= 1 ; w ++) {
					if(l == 1 && h == 1) continue;
					BlockPos pos = startPos.offset(dir, l).offset(dir.rotateY(), w).add(0, h, 0);
					if(h == 0 && w == 0 && l > 1) {
						if(!Utils.isOreBlockAt(world, pos, "blockSteel")) return false;
					} else if(l == 3) {
						if(!Utils.isOreBlockAt(world, pos, "blockSheetmetalSteel")) return false;
					} else if(l == 2) {
						if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.GENERATOR.getMeta())) return false;
					} else if((l == 0 && w == 0) || (l == 1 && w == 0 && h == 0)) {
						if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta())) return false;
					} else if(l == 1) {
						if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDevice0, BlockTypes_MetalDevice0.CAPACITOR_HV.getMeta())) return false;
					} else {
						if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.COIL_HV.getMeta())) return false;
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
		new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 4, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta())), 
		new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 8, BlockTypes_MetalDecoration0.GENERATOR.getMeta())), 
		new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 6, BlockTypes_MetalDecoration0.COIL_HV.getMeta())), 
		new IngredientStack(new ItemStack(IEContent.blockMetalDevice0, 5, BlockTypes_MetalDevice0.CAPACITOR_HV.getMeta())), 
		new IngredientStack("blockSheetmetalSteel", 8), 
		new IngredientStack("blockSteel", 2)
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
		return 11;
	}

	@Override
	public boolean canRenderFormedStructure() {
		return true;
	}

	@Override
	public void renderFormedStructure() {
		if(renderStack == null) renderStack = new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.ALTERNATOR.getMeta());
		GlStateManager.translate(.5, 1.5, 1.5);
		GlStateManager.rotate(- 45, 0, 1, 0);
		GlStateManager.rotate(- 20, 1, 0, 0);
		GlStateManager.scale(4, 4, 4);
		GlStateManager.disableCull();
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.enableCull();
	}

	@SideOnly(Side.CLIENT)
	static ItemStack renderStack;

}