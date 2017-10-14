package ferro2000.immersivetech.common.blocks.metal.multiblocks;

import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsIE;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDecoration;
import blusunrize.immersiveengineering.common.util.Utils;
import ferro2000.immersivetech.common.ITContent;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntitySolarReflector;
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

public class MultiblockSolarReflector implements IMultiblock{

	public static MultiblockSolarReflector instance = new MultiblockSolarReflector();
	
	static ItemStack[][][] structure = new ItemStack[5][1][3];
	static{
		for(int h=0;h<5;h++) {
			for(int w=0;w<3;w++) {
				if((h==0||h==1)&&w==1) {
					continue;
				}
				
				if(h<2) {
					structure[h][0][w] = new ItemStack(IEContent.blockWoodenDecoration,1, BlockTypes_WoodenDecoration.FENCE.getMeta());
				}else if(h==2||h==4) {
					if(w==1) {
						structure[h][0][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
					}else {
						structure[h][0][w] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
					}
				}else if(h==3) {
					if(w==1) {
						structure[h][0][w] = new ItemStack(IEContent.blockStorage,1,BlockTypes_MetalsIE.SILVER.getMeta());
					}else {
						structure[h][0][w] = new ItemStack(IEContent.blockWoodenDecoration,1, BlockTypes_WoodenDecoration.FENCE.getMeta());
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
		return Utils.compareToOreName(new ItemStack(state.getBlock(),1,state.getBlock().getMetaFromState(state)), "blockSilver");
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
		
		for(int h=-3;h<=1;h++)
				for(int w=-1;w<=1;w++)
				{
					if(h<-1 && w==0) {
						continue;
					}
					
					BlockPos pos2 = pos.offset(side.rotateY(), w).add(0, h, 0);
					
					world.setBlockState(pos2, ITContent.blockMetalMultiblock.getStateFromMeta(BlockType_MetalMultiblock.SOLAR_REFLECTOR.getMeta()));
					TileEntity curr = world.getTileEntity(pos2);
					if(curr instanceof TileEntitySolarReflector){
						TileEntitySolarReflector tile = (TileEntitySolarReflector)curr;
						tile.facing=side;
						tile.formed=true;
						tile.pos = (h+3)*3 + (w+1);
						tile.offset = new int[]{(side==EnumFacing.NORTH?w: side==EnumFacing.SOUTH?-w: 0),h,(side==EnumFacing.EAST?w : side==EnumFacing.WEST?-w: 0)};
						tile.markDirty();
						world.addBlockEvent(pos2, ITContent.blockMetalMultiblock, 255, 0);
					}
				}
		return true;
	}
	
	boolean structureCheck(World world, BlockPos startPos, EnumFacing dir){
		
		for(int h=-3;h<=1;h++) {
			for(int w=-1;w<=1;w++) {
				if(h<-1 && w==0) {
					continue;
				}
				
				BlockPos pos = startPos.offset(dir.rotateY(), w).add(0, h, 0);
				
				if(h<-1) {
					if(!Utils.isOreBlockAt(world, pos, "fenceTreatedWood")){
						//System.out.println("ERROR AT: h "+h+", l "+l+", w "+w);
						return false;
					}
				}else if(h==-1 || h==1) {
					if(w==0) {
						if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())){
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
					if(w==0) {
						if(!Utils.isOreBlockAt(world, pos, "blockSilver")){
							//System.out.println("ERROR AT: h "+h+", l "+l+", w "+w);
							return false;
						}
					}else {
						if(!Utils.isOreBlockAt(world, pos, "fenceTreatedWood")){
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
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 2, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())),
			new IngredientStack("scaffoldingSteel", 4),
			new IngredientStack("blockSilver", 1),
			new IngredientStack("fenceTreatedWood", 6)
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
		if(renderStack==null)
			renderStack = new ItemStack(ITContent.blockMetalMultiblock,1,BlockType_MetalMultiblock.SOLAR_REFLECTOR.getMeta());
		GlStateManager.translate(1.5, 2.5, .5);
		GlStateManager.rotate(-45, 0, 1, 0);
		GlStateManager.rotate(-20, 1, 0, 0);
		GlStateManager.scale(8,8,8);
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
	}

}
