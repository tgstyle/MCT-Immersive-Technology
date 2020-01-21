package ferro2000.immersivetech.common.blocks.stone.multiblocks;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.util.Utils;

import ferro2000.immersivetech.common.ITContent;
import ferro2000.immersivetech.common.blocks.stone.tileentities.TileEntityCokeOvenAdvanced;
import ferro2000.immersivetech.common.blocks.stone.types.BlockType_StoneDecoration;
import ferro2000.immersivetech.common.blocks.stone.types.BlockType_StoneMultiblock;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MultiblockCokeOvenAdvanced implements IMultiblock {
	public static MultiblockCokeOvenAdvanced instance = new MultiblockCokeOvenAdvanced();

	static ItemStack[][][] structure = new ItemStack[4][3][3];
	static {
		for(int h = 0 ; h < 4 ; h ++) {
			for(int l = 0 ; l < 3 ; l ++) {
				for(int w = 0 ; w < 3 ; w ++) {
					if(h == 3 && w == 1 && l == 1) {
						structure[h][l][w]=new ItemStack(Blocks.HOPPER);
					} else if(h < 3) {
						if(l == 2) {
							structure[h][l][w]=new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.STEEL.getMeta());
						} else {
							structure[h][l][w]=new ItemStack(ITContent.blockStoneDecoration, 1, BlockType_StoneDecoration.COKEBRICK_REINFORCED.getMeta());
						}
					}
				}
			}
		}
	}

	@Override
	public String getUniqueName() {
		return "IT : CokeOvenAdvanced";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state) {
		return state.getBlock() == ITContent.blockStoneDecoration && (state.getBlock().getMetaFromState(state) == BlockType_StoneDecoration.COKEBRICK_REINFORCED.getMeta());
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
		EnumFacing f = EnumFacing.fromAngle(player.rotationYaw);
		side = side.getOpposite();
		if(side == EnumFacing.UP || side == EnumFacing.DOWN) side = f;
		if(!structureCheck(world, pos, side)) return false;
		IBlockState state = ITContent.blockStoneMultiblock.getStateFromMeta(BlockType_StoneMultiblock.COKE_OVEN_ADVANCED.getMeta());
		state = state.withProperty(IEProperties.FACING_HORIZONTAL, f.getOpposite());
		for(int h = - 1 ; h <= 2 ; h ++) {
			for(int l = 0 ; l <= 2 ; l ++) {
				for(int w = - 1 ; w <= 1 ;w ++) {
					if(h != 2 || (w == 0 && l == 1)) {
						BlockPos pos2 = pos.offset(side, l).offset(side.rotateY(), w).add(0, h, 0);
						world.setBlockState(pos2, ITContent.blockStoneMultiblock.getStateFromMeta(BlockType_StoneMultiblock.COKE_OVEN_ADVANCED.getMeta()));
						TileEntity curr = world.getTileEntity(pos2);
						if(curr instanceof TileEntityCokeOvenAdvanced) {
							TileEntityCokeOvenAdvanced tile = (TileEntityCokeOvenAdvanced)curr;
							tile.facing=side;
							tile.formed=true;
							tile.pos = (h + 1) * 9 + l * 3 + (w + 1);
							tile.offset = new int[] {(side == EnumFacing.WEST ? - l : side == EnumFacing.EAST ? l : side == EnumFacing.NORTH ? w : - w), h, (side == EnumFacing.NORTH ? - l : side == EnumFacing.SOUTH ? l : side == EnumFacing.EAST ? w : - w)};
							tile.markDirty();
							world.addBlockEvent(pos2, ITContent.blockMetalMultiblock, 255, 0);
						}
					}
				}
			}
		}
		return true;
	}
	boolean structureCheck(World world, BlockPos startPos, EnumFacing dir) {
		for(int h = - 1 ; h <= 2 ; h ++) {
			for(int l = 0 ; l <= 2 ; l ++) {
				for(int w = - 1 ; w <=1 ;w ++) {
					if(!(h != 2 || (l == 1 && w == 0))) continue;
					BlockPos pos = startPos.offset(dir, l).offset(dir.rotateY(), w).add(0, h, 0);
					if(h == 2) {
						if(!Utils.isBlockAt(world, pos, Blocks.HOPPER, - 1)) return false;
					} else {
						if(l == 2) {
							if(!Utils.isOreBlockAt(world, pos, "blockSheetmetalSteel")) return false;
						} else {
							if(!Utils.isBlockAt(world, pos, ITContent.blockStoneDecoration, BlockType_StoneDecoration.COKEBRICK_REINFORCED.getMeta())) return false;
						}
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
		new IngredientStack(new ItemStack(ITContent.blockStoneDecoration, 18, BlockType_StoneDecoration.COKEBRICK_REINFORCED.getMeta())), 
		ApiUtils.createIngredientStack(Blocks.HOPPER), 
		new IngredientStack("blockSheetmetalSteel", 9)
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
		return 14;
	}

	@Override
	public boolean canRenderFormedStructure() {
		return true;
	}

	static ItemStack renderStack = ItemStack.EMPTY;

	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure() {
		if(renderStack.isEmpty()) renderStack = new ItemStack(ITContent.blockStoneMultiblock, 1, BlockType_StoneMultiblock.COKE_OVEN_ADVANCED.getMeta());
		GlStateManager.translate(.5, 1.5, 1.5);
		GlStateManager.rotate(- 45, 0, 1, 0);
		GlStateManager.rotate(- 20, 1, 0, 0);
		GlStateManager.scale(4, 4, 4);
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
	}

}