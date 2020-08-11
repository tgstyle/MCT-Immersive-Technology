package mctmods.immersivetechnology.common.blocks.metal.multiblocks;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDecoration;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntitySteelSheetmetalTankSlave;
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

public class MultiblockSteelSheetmetalTank implements IMultiblock {
	public static MultiblockSteelSheetmetalTank instance = new MultiblockSteelSheetmetalTank();

	static ItemStack[][][] structure = new ItemStack[5][3][3];
	static {
		for(int h = 0; h < 5; h ++) {
			for(int l = 0; l < 3; l ++) {
				for(int w = 0; w < 3; w ++) {
					if(h == 0) {
						if((l == 0 || l == 2) && (w == 0 || w == 2)) {
							structure[h][l][w] = new ItemStack(IEContent.blockWoodenDecoration, 1, BlockTypes_WoodenDecoration.FENCE.getMeta());
						} else if(l == 1 && w == 1) {
							structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.STEEL.getMeta());
						}
					} else if(h < 1 || h > 3 || w != 1 || l != 1) {
						structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.STEEL.getMeta());
					}
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	static ItemStack renderStack;

	@Override
	public String getUniqueName() {
		return "IT:SteelSheetmetalTank";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state) {
		return Utils.compareToOreName(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)), "blockSheetmetalSteel");
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
		side = (side == EnumFacing.UP || side == EnumFacing.DOWN)? EnumFacing.fromAngle(player.rotationYaw) : side.getOpposite();
		pos = pos.offset(side).down();

		if(!(Utils.isOreBlockAt(world, pos.offset(side, - 1).offset(side.rotateY()), "fenceTreatedWood") && Utils.isOreBlockAt(world, pos.offset(side, - 1).offset(side.rotateYCCW()), "fenceTreatedWood"))) {
			for(int i = 0; i < 4; i ++) {
				if(Utils.isOreBlockAt(world, pos.add(0, - i, 0).offset(side, - 1).offset(side.rotateY()), "fenceTreatedWood") && Utils.isOreBlockAt(world, pos.add(0, - i, 0).offset(side, - 1).offset(side.rotateYCCW()), "fenceTreatedWood")) {
					pos = pos.add(0, - i, 0);
					break;
				}
			}
		}
		for(int h = 0; h <= 4; h ++) {
			for(int xx = - 1; xx <= 1; xx ++) {
				for(int zz = - 1; zz <= 1; zz ++) {
					if(h == 0) {
						if(Math.abs(xx) == 1 && Math.abs(zz) == 1) {
							if(!Utils.isOreBlockAt(world, pos.add(xx, h, zz), "fenceTreatedWood")) return false;
						} else if(xx == 0 && zz == 0) {
							if(!Utils.isOreBlockAt(world, pos.add(xx, h, zz), "blockSheetmetalSteel")) return false;
						}
					} else {
						if(h < 4 && xx == 0 && zz == 0) {
							if(!world.isAirBlock(pos.add(xx, h, zz))) return false;
						} else if(!Utils.isOreBlockAt(world, pos.add(xx, h, zz), "blockSheetmetalSteel")) return false;
					}
				}
			}
		}
		if(player != null) {
			ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(Lib.TOOL_HAMMER)?player.getHeldItemMainhand(): player.getHeldItemOffhand();
			if(MultiblockHandler.fireMultiblockFormationEventPost(player, this, pos, hammer).isCanceled()) return false;
		}

		createStructureInternal(world, pos, side);
		return true;
	}

	private void createStructureInternal(World world, BlockPos pos, EnumFacing side) {
		IBlockState master = ITContent.blockMetalMultiblock.getStateFromMeta(BlockType_MetalMultiblock.STEEL_TANK.getMeta());
		IBlockState slave = ITContent.blockMetalMultiblock.getStateFromMeta(BlockType_MetalMultiblock.STEEL_TANK_SLAVE.getMeta());
		for(int h = 0; h <= 4; h ++) {
			for(int l = - 1; l <= 1; l ++) {
				for(int w = - 1; w <= 1; w ++) {
					if(h == 0 && !((l == 0 && w == 0) || (Math.abs(l) == 1 && Math.abs(w) == 1))) continue;
					if(h > 0 && h < 4 && l == 0 && w == 0) continue;
					int xx = side == EnumFacing.EAST?l : side == EnumFacing.WEST? - l : side == EnumFacing.NORTH? - w : w;
					int zz = side == EnumFacing.NORTH?l : side == EnumFacing.SOUTH? - l : side == EnumFacing.EAST?w : - w;
					world.setBlockState(pos.add(xx, h, zz), (xx == 0 && h == 0 && zz == 0)? master : slave);
					BlockPos pos2 = pos.add(xx, h, zz);
					TileEntity curr = world.getTileEntity(pos2);
					if(curr instanceof TileEntitySteelSheetmetalTankSlave) {
						TileEntitySteelSheetmetalTankSlave currTank = (TileEntitySteelSheetmetalTankSlave)curr;
						currTank.offset = new int[]{xx, h, zz};
						currTank.pos = h * 9 + (l + 1) * 3 + (w + 1);
						currTank.formed = true;
						currTank.offset = new int[]{xx, h, zz};
						currTank.markDirty();
						world.addBlockEvent(pos2, ITContent.blockMetalMultiblock, 255, 0);
					}
				}
			}
		}
	}

	@Override
	public ItemStack[][][] getStructureManual()	{
		return structure;
	}

	static final IngredientStack[] materials = new IngredientStack[] {
		new IngredientStack("fenceTreatedWood", 4), 
		new IngredientStack("blockSheetmetalSteel", 34)
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
		return 10;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure() {
		if(renderStack.isEmpty()) renderStack = new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.STEEL_TANK.getMeta());
		GlStateManager.translate(1.875, 1.75, 1.125);
		GlStateManager.rotate(- 45, 0, 1, 0);
		GlStateManager.rotate(- 20, 1, 0, 0);
		GlStateManager.scale(5.5, 5.5, 5.5);
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
	}

}