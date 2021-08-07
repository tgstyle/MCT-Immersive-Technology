package mctmods.immersivetechnology.common.blocks.metal.multiblocks;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDecoration;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntitySolarMelterSlave;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalBarrel;
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

public class MultiblockSolarMelter implements IMultiblock {
	public static MultiblockSolarMelter instance = new MultiblockSolarMelter();

	static ItemStack[][][] structure = new ItemStack[21][3][3];
	static {
		for(int h = 0 ; h < 21 ; h ++) {
			for(int l = 0 ; l < 3 ; l ++) {
				for(int w = 0 ; w < 3 ; w ++) {
					if ((h > 3 && h < 18 && l == 1 && w == 1) || ((h == 8 || h == 9 || h == 10 || h == 11 || h == 16 || h == 17) && (w == 1 || l == 1)) || (h == 19 && w != l && (w == 1 || l == 1))) continue;
					if (h == 0) {
						structure[h][l][w] = new ItemStack(IEContent.blockStoneDecoration, 1, BlockTypes_StoneDecoration.CONCRETE.getMeta());
					} else if(h == 1) {
						if(w == 0 && l == 1) {
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta());
						} else if (w != 1 && (l != 1)) {
							structure[h][l][w] = new ItemStack(IEContent.blockStoneDecoration, 1, BlockTypes_StoneDecoration.HEMPCRETE.getMeta());
						} else {
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
						}
					} else if (h < 18) {
						if (h == 2 && (l == 1 || w == 1)) {
							if (l == 1 & w == 1) {
								structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
							} else
								structure[h][l][w] = new ItemStack(ITContent.blockMetalBarrel, 1, BlockType_MetalBarrel.BARREL_STEEL.getMeta());
						} else if (h == 3 && (l == 1 || w == 1)){
							if (l == 1 & w == 1) {
								structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.SILVER.getMeta());
							} else
							    structure[h][l][w] = new ItemStack(IEContent.blockSheetmetalSlabs, 1, BlockTypes_MetalsAll.STEEL.getMeta());
						} else {
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta());
						}
					} else {
						if((w == 0 || w == 2) && (l != 1)) {
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
						} else if(((w == 1 && (l != 1)) || (w == 0 || w == 2))) {
							structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.STEEL.getMeta());
						} else {
							if (h == 18 || h == 19) {
								structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.SILVER.getMeta());
							} else {
								structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
							}
						}
					}
				}
			}
		}
	}



	@SideOnly(Side.CLIENT)
	static ItemStack renderStack;

	@Override
	public String getUniqueName() {
		return "IT:SolarMelter";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state) {
		return state.getBlock() == IEContent.blockSheetmetal && (state.getBlock().getMetaFromState(state) == BlockTypes_MetalsAll.SILVER.getMeta());
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
		side = (side == EnumFacing.UP || side == EnumFacing.DOWN)? EnumFacing.fromAngle(player.rotationYaw) : side.getOpposite();
		IBlockState master = ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.SOLAR_MELTER.getMeta());
		IBlockState slave = ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.SOLAR_MELTER_SLAVE.getMeta());
		boolean mirror = false;
		if(!this.structureCheck(world, pos, side, mirror)) {
			mirror = true;
			if(!this.structureCheck(world, pos, side, mirror)) return false;
		}
		if(player != null) {
			ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(Lib.TOOL_HAMMER)?player.getHeldItemMainhand(): player.getHeldItemOffhand();
			if(MultiblockHandler.fireMultiblockFormationEventPost(player, this, pos, hammer).isCanceled()) return false;
		}
		for(int h = -3 ; h <= 17 ; h ++) {
			for(int l = -1 ; l <= 1 ; l ++) {
				for(int w = -1 ; w <= 1 ; w ++) {
					if ((h > 0 && h < 15 && l == 0 && w == 0) || ((h == 5 || h == 6 || h == 7 || h == 8 || h == 13 || h == 14) && (w == 0 || l == 0)) || (h == 16 && w != l && (w == 0 || l == 0))) continue;
					int ww = mirror ? - w : w;
					BlockPos pos2 = pos.offset(side, l).offset(side.rotateY(), ww).add(0, h, 0);
					int[] offset = new int[] {(side == EnumFacing.WEST ? - l : side == EnumFacing.EAST ? l : side == EnumFacing.NORTH ? ww : - ww), h, (side == EnumFacing.NORTH ? - l : side == EnumFacing.SOUTH ? l : side == EnumFacing.EAST ? ww : - ww)};
					world.setBlockState(pos2, (h == 0 && l == 0 && w == 0) ? master : slave);
					TileEntity curr = world.getTileEntity(pos2);
					if(curr instanceof TileEntitySolarMelterSlave) {
						TileEntitySolarMelterSlave tile = (TileEntitySolarMelterSlave)curr;
						tile.facing = side;
						tile.formed = true;
						tile.pos = (h + 3) * 9 + (l + 1) * 3 + (w + 1);
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
		for(int h = -3 ; h <= 17 ; h ++) {
			for(int l = -1 ; l <= 1 ; l ++) {
				for(int w = -1 ; w <= 1 ; w ++) {
					if ((h > 0 && h < 15 && l == 0 && w == 0) || ((h == 5 || h == 6 || h == 7 || h == 8 || h == 13 || h == 14) && (w == 0 || l == 0)) || (h == 16 && w != l && (w == 0 || l == 0))) continue;

					int ww = mirror ? -w : w;
					BlockPos pos = startPos.offset(dir, l).offset(dir.rotateY(), w).add(0, h, 0);

					if (h == -3) {
						if(!Utils.isBlockAt(world, pos, IEContent.blockStoneDecoration, BlockTypes_StoneDecoration.CONCRETE.getMeta())) return false;
					} else if(h == -2) {
						if(ww == -1 && l == 0) {
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())) return false;
						} else if ((ww == -1 || ww == 1) && (l != 0)) {
							if(!Utils.isBlockAt(world, pos, IEContent.blockStoneDecoration, BlockTypes_StoneDecoration.HEMPCRETE.getMeta())) return false;
						} else {
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())) return false;
						}
					} else if (h < 15) {
						if (h == -1 && (l == 0 || ww == 0)) {
							if (l == 0 & ww == 0) {
								if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())) return false;
							} else
								if(!Utils.isBlockAt(world, pos, ITContent.blockMetalBarrel, BlockType_MetalBarrel.BARREL_STEEL.getMeta())) return false;
						} else if (h == 0 && (l == 0 || w == 0)) {
							if (l == 0 & ww == 0) {
								if(!Utils.isBlockAt(world, pos, IEContent.blockSheetmetal, BlockTypes_MetalsAll.SILVER.getMeta())) return false;
							} else
							    if(!Utils.isBlockAt(world, pos, IEContent.blockSheetmetalSlabs, BlockTypes_MetalsAll.STEEL.getMeta())) return false;
						} else if(((ww != 0) && (l != 0)) || (( h == 1 || h == 2 || h == 3 || h == 4 || h == 9 || h == 10 || h == 11 || h == 12))) {
							if(!Utils.isOreBlockAt(world, pos, "fenceSteel")) return false;
						}
					} else {
						if((ww != 0) && (l != 0)) {
							if(!Utils.isOreBlockAt(world, pos, "scaffoldingSteel")) return false;
						} else if(ww != l) {
							if(!Utils.isOreBlockAt(world, pos, "blockSheetmetalSteel")) return false;
						} else {
							if (h == 15 || h == 16) {
								if(!Utils.isBlockAt(world, pos, IEContent.blockSheetmetal, BlockTypes_MetalsAll.SILVER.getMeta())) return false;
							} else {
								if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta())) return false;
							}
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
		new IngredientStack("scaffoldingSteel", 12),
		new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())), 
		new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta())),
		new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 5, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())),
		new IngredientStack(new ItemStack(IEContent.blockSheetmetal, 3, BlockTypes_MetalsAll.SILVER.getMeta())),
		new IngredientStack(new ItemStack(IEContent.blockSheetmetalSlabs, 4, BlockTypes_MetalsAll.STEEL.getMeta())),
		new IngredientStack(new ItemStack(ITContent.blockMetalBarrel, 4, BlockType_MetalBarrel.BARREL_STEEL.getMeta())),
		new IngredientStack(new ItemStack(IEContent.blockStoneDecoration, 9, BlockTypes_StoneDecoration.CONCRETE.getMeta())),
		new IngredientStack(new ItemStack(IEContent.blockStoneDecoration, 4, BlockTypes_StoneDecoration.HEMPCRETE.getMeta())),
		new IngredientStack("fenceSteel", 80),
		new IngredientStack("blockSheetmetalSteel", 8)
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
		return 4;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure() {
		if(renderStack == null) renderStack = new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.SOLAR_MELTER.getMeta());
		GlStateManager.translate(0.1, 0.25, 0.125);
		GlStateManager.translate(1, 3.5, 2);
		GlStateManager.rotate(- 45, 0, 1, 0);
		GlStateManager.rotate(- 20, 1, 0, 0);
		GlStateManager.scale(8, 8, 8);
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
	}

}