package ferro2000.immersivetech.common.blocks.metal.multiblocks;

import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1;
import blusunrize.immersiveengineering.common.util.Utils;
import ferro2000.immersivetech.common.ITContent;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntitySolarTower;
import ferro2000.immersivetech.common.blocks.metal.types.BlockType_MetalMultiblock;
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

public class MultiblockSolarTower implements IMultiblock {
	
	public static MultiblockSolarTower instance = new MultiblockSolarTower();
	
	static ItemStack[][][] structure = new ItemStack[7][3][3];
	static {
		for(int h=0;h<7;h++) {
			for(int l=0;l<3;l++) {
				for(int w=0;w<3;w++) {
					if(h==3 && ((l==1 && (w==0 || w==2)) || (w==1 && (l==0 || l==2)))) {
						continue;
					}
					
					if(h==0) {
						if(l==1 || (l==2 && w==1)) {
							structure[h][l][w]=new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.FLUID_PIPE.getMeta());
						}else {
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
						}
					}else if(h>0 && h<6){
						if(h==1){
							if(l==0 && w==1) {
								structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta());
							}else if(l==1 && w==1) {
								structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
							}else if((l==1 && (w==0 || w==2)) || (l==2 && w==1)) {
								structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal,1,BlockTypes_MetalsAll.STEEL.getMeta());
							}else {
								structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta());
							}
						}else {
							if(l==1 && w==1) {
								structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
							}else if((l==1 && (w==0 || w==2)) || (w==1 && (l==0 || l==2))) {
								structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal,1,BlockTypes_MetalsAll.STEEL.getMeta());
							}else {
								structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta());
							}
						}
					}else {
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
					}
				}
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	static ItemStack renderStack;

	@Override
	public String getUniqueName() {
		return "IT:SolarTower";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state) {
		return state.getBlock()==IEContent.blockMetalDecoration0 && 
				(state.getBlock().getMetaFromState(state)==BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta());
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
		side = side.getOpposite();
		if(side==EnumFacing.UP || side==EnumFacing.DOWN){
			side = EnumFacing.fromAngle(player.rotationYaw);
		}
		
		boolean bool = this.structureCheck(world, pos, side);
		if(!bool){
			return false;
		}
		
		for(int h=-1;h<=5;h++)
			for(int l=0;l<=2;l++)
				for(int w=-1;w<=1;w++)
				{
					if(h==2 && ((l==1 && (w==-1 || w==1)) || (w==0 && (l==0 || l==2)))) {
						//System.out.println("ERROR AT: h "+h+", l "+l+", w "+w);
						continue;
					}
					
					BlockPos pos2 = pos.offset(side, l).offset(side.rotateY(), w).add(0, h, 0);
					
					world.setBlockState(pos2, ITContent.blockMetalMultiblock.getStateFromMeta(BlockType_MetalMultiblock.SOLAR_TOWER.getMeta()));
					TileEntity curr = world.getTileEntity(pos2);
					if(curr instanceof TileEntitySolarTower){
						TileEntitySolarTower tile = (TileEntitySolarTower)curr;
						tile.facing=side;
						tile.formed=true;
						tile.pos = (h+1)*9 + l*3 + (w+1);
						tile.offset = new int[]{(side==EnumFacing.WEST?-l: side==EnumFacing.EAST?l: side==EnumFacing.NORTH?w: -w),h,(side==EnumFacing.NORTH?-l: side==EnumFacing.SOUTH?l: side==EnumFacing.EAST?w : -w)};
						
						tile.markDirty();
						world.addBlockEvent(pos2, ITContent.blockMetalMultiblock, 255, 0);
					}
				}
		return true;
	}
	
	boolean structureCheck(World world, BlockPos startPos, EnumFacing dir){
		for(int h=-1;h<=5;h++) {
			for(int l=0;l<=2;l++) {
				for(int w=-1;w<=1;w++) {
					if(h==2 && ((l==1 && (w==-1 || w==1)) || (w==0 && (l==0 || l==2)))) {
						//System.out.println("ERROR AT: h "+h+", l "+l+", w "+w);
						continue;
					}
					
					BlockPos pos = startPos.offset(dir, l).offset(dir.rotateY(), w).add(0, h, 0);
					
					if(h==-1) {
						if(l==1 || (l==2 && w==0)) {
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDevice1, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta())){
								//System.out.println("ERROR AT: h "+h+", l "+l+", w "+w);
								return false;
							}
						}else {
							if(!Utils.isOreBlockAt(world, pos, "scaffoldingSteel")){
								//System.out.println("ERROR AT: h "+h+", l "+l+", w "+w);
								return false;
							}
						}
					}else if(h==0) {
						if(l==1 && w==0) {
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta())){
								//System.out.println("ERROR AT: h "+h+", l "+l+", w "+w);
								return false;
							}
						}else if((l==1 && (w==-1 || w==1)) || (w==0 && l==2)) {
							if(!Utils.isOreBlockAt(world, pos, "blockSheetmetalSteel")){
								//System.out.println("ERROR AT: h "+h+", l "+l+", w "+w);
								return false;
							}
						}else if(l==0 && w==0){
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())){
								//System.out.println("ERROR AT: h "+h+", l "+l+", w "+w);
								return false;
							}
						}else {
							if(!Utils.isOreBlockAt(world, pos, "fenceSteel")){
								//System.out.println("ERROR AT: h "+h+", l "+l+", w "+w);
								return false;
							}
						}
					}else if(h>0 && h<5) {
						if((l==1 && (w==-1 || w==1)) || (w==0 && (l==0 || l==2))) {
							if(!Utils.isOreBlockAt(world, pos, "blockSheetmetalSteel")){
								//System.out.println("ERROR AT: h "+h+", l "+l+", w "+w);
								return false;
							}
						}else if(l==1 && w==0){
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta())){
								//System.out.println("ERROR AT: h "+h+", l "+l+", w "+w);
								return false;
							}
						}else {
							if(!Utils.isOreBlockAt(world, pos, "fenceSteel")){
								//System.out.println("ERROR AT: h "+h+", l "+l+", w "+w);
								return false;
							}
						}
					}else if(h==5) {
						if(!Utils.isOreBlockAt(world, pos, "scaffoldingSteel")){
							//System.out.println("ERROR AT: h "+h+", l "+l+", w "+w);
							return false;
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
			new IngredientStack("scaffoldingSteel", 14),
			new IngredientStack(new ItemStack(IEContent.blockMetalDevice1, 4, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 5, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta())),
			new IngredientStack("fenceSteel", 20),
			new IngredientStack("blockSheetmetalSteel", 16)
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
		if(renderStack==null)
			renderStack = new ItemStack(ITContent.blockMetalMultiblock,1,BlockType_MetalMultiblock.SOLAR_TOWER.getMeta());
		GlStateManager.translate(1.5, .5, .5);
		GlStateManager.rotate(-45, 0, 1, 0);
		GlStateManager.rotate(-20, 1, 0, 0);
		GlStateManager.scale(8,8,8);
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
	}

}
