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
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityBoiler;
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

public class MultiblockBoiler implements IMultiblock{

	public static MultiblockBoiler instance = new MultiblockBoiler();
		
	static ItemStack[][][] structure = new ItemStack[3][3][5];
	static {
		for(int h=0;h<3;h++) {
			for(int l=0;l<3;l++) {
				for(int w=0;w<5;w++) {
					
					if(w==4 && l==0 && h==2) {
						continue;
					}
					
					if(h==0) {
						if(l==1) {
							structure[h][l][w]=new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.FLUID_PIPE.getMeta());
						}else {
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
						}
					}else if(h==1) {
						if(w<4) {
							structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal,1, BlockTypes_MetalsAll.IRON.getMeta());
						}else if(l==0 && w==4) {
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta());
						}else {
							structure[h][l][w]=new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
						}
					}else if(h==2){
						if(w<4) {
							if(l==1) {
								structure[h][l][w]=new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.FLUID_PIPE.getMeta());
							}else {
								structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal,1, BlockTypes_MetalsAll.IRON.getMeta());
							}
						}else {
							structure[h][l][w]=new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
						}
					}
					
				}
			}
		}
	}
	
	@Override
	public String getUniqueName() {
		return "IT:Boiler";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state) {
		return state.getBlock()==IEContent.blockSheetmetal && 
				(state.getBlock().getMetaFromState(state)==BlockTypes_MetalsAll.IRON.getMeta());
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
		side = side.getOpposite();
		if(side==EnumFacing.UP || side==EnumFacing.DOWN){
			side = EnumFacing.fromAngle(player.rotationYaw);
		}
		
		boolean mirror = false;
		boolean bool = this.structureCheck(world, pos, side, mirror);
		if(!bool){
			mirror = true;
			bool = this.structureCheck(world, pos, side, mirror);
		}
		if(!bool){
			return false;
		}
		
		for(int h=-1;h<=1;h++)
			for(int l=0;l<=2;l++)
				for(int w=-2;w<=2;w++)
				{
					if(w==2 && l==0 && h==1) {
						continue;
					}
					
					int ww = mirror?-w:w;
					BlockPos pos2 = pos.offset(side, l).offset(side.rotateY(), ww).add(0, h, 0);
					
					world.setBlockState(pos2, ITContent.blockMetalMultiblock.getStateFromMeta(BlockType_MetalMultiblock.BOILER.getMeta()));
					TileEntity curr = world.getTileEntity(pos2);
					if(curr instanceof TileEntityBoiler){
						TileEntityBoiler tile = (TileEntityBoiler)curr;
						tile.facing=side;
						tile.formed=true;
						tile.pos = (h+1)*15 + l*5 + (w+2);
						tile.offset = new int[]{(side==EnumFacing.WEST?-l: side==EnumFacing.EAST?l: side==EnumFacing.NORTH?ww: -ww),h,(side==EnumFacing.NORTH?-l: side==EnumFacing.SOUTH?l: side==EnumFacing.EAST?ww : -ww)};
						tile.mirrored=mirror;
						tile.markDirty();
						world.addBlockEvent(pos2, ITContent.blockMetalMultiblock, 255, 0);
					}
				}
		return true;
	}
	
	boolean structureCheck(World world, BlockPos startPos, EnumFacing dir, boolean mirror){
		for(int h=-1;h<2;h++){
			for(int l=0;l<=2;l++){
				for(int w=-2;w<=2;w++){
					if(w==2 && l==0 && h==1) {
						continue;
					}
					
					int ww = mirror?-w:w;
					BlockPos pos = startPos.offset(dir, l).offset(dir.rotateY(), ww).add(0, h, 0);
					
					if(h==-1) {
						if(l==1) {
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
						if(w<2) {
							if(!Utils.isOreBlockAt(world, pos, "blockSheetmetalIron")){
								//System.out.println("ERROR AT: h "+h+", l "+l+", w "+w);
								return false;
							}
						}else if(l==0 && w==2) {
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())){
								//System.out.println("ERROR AT: h "+h+", l "+l+", w "+w);
								return false;
							}
						}else {
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta())){
								//System.out.println("ERROR AT: h "+h+", l "+l+", w "+w);
								return false;
							}
						}
					}else {
						if(w<2) {
							if(l==1) {
								if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDevice1, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta())){
									//System.out.println("ERROR AT: h "+h+", l "+l+", w "+w);
									return false;
								}
							}else {
								if(!Utils.isOreBlockAt(world, pos, "blockSheetmetalIron")){
									//System.out.println("ERROR AT: h "+h+", l "+l+", w "+w);
									return false;
								}
							}
						}else {
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta())){
								//System.out.println("ERROR AT: h "+h+", l "+l+", w "+w);
								return false;
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

	static final IngredientStack[] materials = new IngredientStack[]{
			new IngredientStack("scaffoldingSteel", 10),
			new IngredientStack(new ItemStack(IEContent.blockMetalDevice1, 9, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 4, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta())),
			new IngredientStack("blockSheetmetalIron", 20)};
	
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
		return 12;
	}

	@Override
	public boolean canRenderFormedStructure() {
		return true;
	}

	@Override
	public void renderFormedStructure() {
		if(renderStack==null)
			renderStack = new ItemStack(ITContent.blockMetalMultiblock,1,BlockType_MetalMultiblock.BOILER.getMeta());
		GlStateManager.translate(1.5, 1, 1.5);
		GlStateManager.rotate(-45, 0, 1, 0);
		GlStateManager.rotate(-20, 1, 0, 0);
		GlStateManager.scale(4, 4, 4);
		GlStateManager.disableCull();
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.enableCull();
	}
	
	@SideOnly(Side.CLIENT)
	static ItemStack renderStack;

}
