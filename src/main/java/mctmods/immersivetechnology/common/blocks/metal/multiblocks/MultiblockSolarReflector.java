package mctmods.immersivetechnology.common.blocks.metal.multiblocks;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsIE;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDecoration;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntitySolarReflectorSlave;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalMultiblock;
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

public class MultiblockSolarReflector implements IMultiblock {
	public static MultiblockSolarReflector instance = new MultiblockSolarReflector();

	static ItemStack[][][] structure = new ItemStack[3][3][3];
	static {
		for(int h = 0 ; h < 3 ; h ++) {
			for (int l = 0; l < 3; l++) {
				for (int w = 0; w < 3; w++) {
					if (h == 1 && w == 1 && (l == 0 || l == 2)) continue;
					if (h == 0) {
						if ((w == 0 || w == 2) && (l == 0 || l == 2)) {
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
						} else if (w == 1 && l == 1) {
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
						} else {
							structure[h][l][w] = new ItemStack(IEContent.blockStoneDecoration, 1, BlockTypes_StoneDecoration.CONCRETE.getMeta());
						}
					} else if (h == 1) {
						if (l == 0 || l == 2) {
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
						} else {
							structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsIE.SILVER.getMeta());
						}
					} else if (h == 2) {
						if(l == 1) {
							structure[h][l][w] = new ItemStack(Blocks.GLASS);
						} else {
							structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsIE.SILVER.getMeta());
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
		return "IT:SolarReflector";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state) {
		return Utils.compareToOreName(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)), "blockSheetmetalSilver");
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
		side = (side == EnumFacing.UP || side == EnumFacing.DOWN)? EnumFacing.fromAngle(player.rotationYaw) : side.getOpposite();
		IBlockState master = ITContent.blockMetalMultiblock.getStateFromMeta(BlockType_MetalMultiblock.SOLAR_REFLECTOR.getMeta());
		IBlockState slave = ITContent.blockMetalMultiblock.getStateFromMeta(BlockType_MetalMultiblock.SOLAR_REFLECTOR_SLAVE.getMeta());
		if(!this.structureCheck(world, pos, side)) return false;
		if(player != null) {
			ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(Lib.TOOL_HAMMER)?player.getHeldItemMainhand(): player.getHeldItemOffhand();
			if(MultiblockHandler.fireMultiblockFormationEventPost(player, this, pos, hammer).isCanceled()) return false;
		}
		for(int h = - 1 ; h <= 1 ; h ++) {
			for(int l = - 1 ; l <= 1 ; l ++) {
				for (int w = -1; w <= 1; w++) {
					if (h == 0 && w == 0 && (l == -1 || l == 1)) continue;
					BlockPos pos2 = pos.offset(side.rotateY(), w).offset(side, l).add(0, h, 0);
					world.setBlockState(pos2, (h == 0 && l == 0 && w == 0) ? master : slave);
					TileEntity curr = world.getTileEntity(pos2);
					if (curr instanceof TileEntitySolarReflectorSlave) {
						TileEntitySolarReflectorSlave tile = (TileEntitySolarReflectorSlave) curr;
						tile.facing = side;
						tile.formed = true;
						tile.pos = (h+1)*9+(l+1)*3+(w+1);
						tile.offset = new int[]{(side == EnumFacing.WEST ? -l : side == EnumFacing.EAST ? l: side == EnumFacing.NORTH ? w : -w), h, (side == EnumFacing.NORTH ? -l : side == EnumFacing.SOUTH ? l : side == EnumFacing.EAST ? w : -w)};
						tile.markDirty();
						world.addBlockEvent(pos2, ITContent.blockMetalMultiblock, 255, 0);
					}
				}
			}
		}
		return true;
	}

	boolean structureCheck(World world, BlockPos startPos, EnumFacing dir) {
		for(int h = - 1 ; h <= 1 ; h ++) {
			for(int l = - 1 ; l <= 1 ; l ++) {
				for (int w = -1; w <= 1; w++) {
					if (h == 0 && w == 0 && (l == -1 || l == 1)) continue;

					BlockPos pos = startPos.offset(dir, l).offset(dir.rotateY(), w).add(0, h, 0);
					if (h == -1) {
						if ((w == -1 || w == 1) && (l == -1 || l == 1)) {
							if(!Utils.isOreBlockAt(world, pos, "scaffoldingSteel")) return false;
						} else if (w == 0 && l == 0) {
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())) return false;
						} else {
							if(!Utils.isBlockAt(world, pos, IEContent.blockStoneDecoration, BlockTypes_StoneDecoration.CONCRETE.getMeta())) return false;
						}
					} else if (h == 0) {
						if (l == -1 || l == 1) {
							if(!Utils.isOreBlockAt(world, pos, "scaffoldingSteel")) return false;
						} else {
							if(!Utils.isOreBlockAt(world, pos, "blockSheetmetalSilver")) return false;
						}
					} else if (h == 1) {
						if(l == 0) {
							if (!Utils.isOreBlockAt(world, pos, "blockGlassColorless")) return false;
						} else {
							if (!Utils.isOreBlockAt(world, pos, "blockSheetmetalSilver")) return false;
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
		new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())),
		new IngredientStack(new ItemStack(IEContent.blockStoneDecoration, 4, BlockTypes_StoneDecoration.CONCRETE.getMeta())),
		new IngredientStack("scaffoldingSteel", 8),
		new IngredientStack("blockSheetmetalSilver", 9),
			new IngredientStack("blockGlassColorless", 9)
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

	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure() {
		if(renderStack == null) renderStack = new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.SOLAR_REFLECTOR.getMeta());
		GlStateManager.translate(1.5, 2.5, .5);
		GlStateManager.rotate(- 45, 0, 1, 0);
		GlStateManager.rotate(- 20, 1, 0, 0);
		GlStateManager.scale(8, 8, 8);
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
	}

}