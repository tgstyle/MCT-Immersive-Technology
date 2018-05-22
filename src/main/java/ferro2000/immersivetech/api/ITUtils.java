package ferro2000.immersivetech.api;

import ferro2000.immersivetech.api.client.MechanicalEnergyAnimation;
import ferro2000.immersivetech.api.energy.MechanicalEnergy;
import ferro2000.immersivetech.common.blocks.ITBlockInterface.IMechanicalEnergy;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityAlternator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ITUtils {
	
	public static double[] smartBoundingBox(double A, double B, double C, double D, double minY, double maxY, EnumFacing fl, EnumFacing fw) {
		
		double[] boundingArray = new double[6];
		
		boundingArray[0] = fl==EnumFacing.WEST? A : fl==EnumFacing.EAST? B : fw==EnumFacing.EAST? C : D;
		boundingArray[1] = minY;
		boundingArray[2] = fl==EnumFacing.NORTH? A : fl==EnumFacing.SOUTH? B : fw==EnumFacing.SOUTH? C : D;
		boundingArray[3] = fl==EnumFacing.EAST? 1-A : fl==EnumFacing.WEST? 1-B : fw==EnumFacing.EAST? 1-D : 1-C;
		boundingArray[4] = maxY;
		boundingArray[5] = fl==EnumFacing.SOUTH? 1-A : fl==EnumFacing.NORTH? 1-B : fw==EnumFacing.SOUTH? 1-D : 1-C;
		
		return boundingArray;
		
	}
	
	public static boolean checkMechanicalEnergyTransmitter(World world, BlockPos startPos) {
		
		TileEntity tile = world.getTileEntity(startPos);
		
		if(tile instanceof IMechanicalEnergy) {
			
			if(((IMechanicalEnergy) tile).isMechanicalEnergyReceiver()) {
				
				EnumFacing inputFacing = ((IMechanicalEnergy) tile).getMechanicalEnergyInputFacing();
				BlockPos pos = startPos.offset(inputFacing, ((IMechanicalEnergy) tile).inputToCenterDistance()+1);
				TileEntity tileTransmitter = world.getTileEntity(pos);
				
				if(tileTransmitter instanceof IMechanicalEnergy && ((IMechanicalEnergy) tileTransmitter).isMechanicalEnergyTransmitter() && (((IMechanicalEnergy) tileTransmitter).getMechanicalEnergyOutputFacing() == inputFacing.getOpposite())) {
					
					return true;
					
				}
				
			}
			
		}
		
		return false;
				
	}
	
	public static boolean checkMechanicalEnergyReceiver(World world, BlockPos startPos) {
		
		TileEntity tile = world.getTileEntity(startPos);
		
		if(tile instanceof IMechanicalEnergy) {
			
			if(((IMechanicalEnergy) tile).isMechanicalEnergyTransmitter()) {
				
				EnumFacing outputFacing = ((IMechanicalEnergy) tile).getMechanicalEnergyOutputFacing();
				BlockPos pos = startPos.offset(outputFacing, ((IMechanicalEnergy) tile).outputToCenterDistance()+1);
				TileEntity tileReceiver = world.getTileEntity(pos);
				
				if(tileReceiver instanceof IMechanicalEnergy && ((IMechanicalEnergy) tileReceiver).isMechanicalEnergyReceiver() && ((IMechanicalEnergy) tileReceiver).getMechanicalEnergyInputFacing() == outputFacing.getOpposite()) {
					
					return true;
					
				}
				
			}
			
		}
		
		return false;
		
	}
	
	public static MechanicalEnergy getMechanicalEnergy(World world, BlockPos startPos) {
		
		TileEntity tile = world.getTileEntity(startPos);
		EnumFacing inputFacing = ((IMechanicalEnergy) tile).getMechanicalEnergyInputFacing();
		BlockPos pos = startPos.offset(inputFacing, ((IMechanicalEnergy) tile).inputToCenterDistance()+1);
		TileEntity tileInfo = world.getTileEntity(pos);
		TileEntity tileTransmitter = world.getTileEntity(pos.offset(inputFacing, ((IMechanicalEnergy) tileInfo).outputToCenterDistance()));
		
		if(tileTransmitter instanceof IMechanicalEnergy) {
			return ((IMechanicalEnergy) tileTransmitter).getEnergy();
		}else {
			return new MechanicalEnergy(0,0,0);
		}
		
	}
	
	public static boolean checkAlternatorStatus(World world, BlockPos startPos) {
		
		TileEntity tile = world.getTileEntity(startPos);
		EnumFacing outputFacing = ((IMechanicalEnergy) tile).getMechanicalEnergyOutputFacing();
		BlockPos pos = startPos.offset(outputFacing, ((IMechanicalEnergy) tile).outputToCenterDistance()+1);
		TileEntity tileInfo = world.getTileEntity(pos);
		TileEntity tileReceiver = world.getTileEntity(pos.offset(outputFacing, ((IMechanicalEnergy) tileInfo).inputToCenterDistance()));
		
		if(tileReceiver instanceof TileEntityAlternator) {
			
			if(((TileEntityAlternator) tileReceiver).canRunMechanicalEnergy()){
				
				return true;
				
			}
			
		}
		
		return false;
		
	}
		
	public static MechanicalEnergyAnimation setRotationAngle(MechanicalEnergyAnimation animation, boolean active) {
		
		if(active || animation.getAnimationFadeIn()>0 || animation.getAnimationFadeOut()>0) {
			
			int fadeIn = animation.getAnimationFadeIn();
			int fadeOut = animation.getAnimationFadeOut();
			float base = 18f;
			float step = active? base : 0;
			float rotation = animation.getAnimationRotation();
			
			if(animation.getAnimationFadeIn()>0) {
				
				step -= (animation.getAnimationFadeIn()/80f) * base;
				fadeIn--;
				
			}
			
			if(animation.getAnimationFadeOut()>0) {
				
				step += (animation.getAnimationFadeOut()/80f) * base;
				fadeOut--;
				
			}
			
			rotation += step;
			rotation %= 360;
			
			return new MechanicalEnergyAnimation(fadeIn, fadeOut, rotation, step);
			
		}
		
		return new MechanicalEnergyAnimation(0,0,animation.getAnimationRotation(),0);
		
	}

	public static MechanicalEnergyAnimation getMechanicalEnergyAnimation(World world, BlockPos startPos) {
		
		TileEntity tile = world.getTileEntity(startPos);
		EnumFacing inputFacing = ((IMechanicalEnergy) tile).getMechanicalEnergyInputFacing();
		BlockPos pos = startPos.offset(inputFacing, ((IMechanicalEnergy) tile).inputToCenterDistance()+1);
		TileEntity tileInfo = world.getTileEntity(pos);
		TileEntity tileTransmitter = world.getTileEntity(pos.offset(inputFacing, ((IMechanicalEnergy) tileInfo).outputToCenterDistance()));
		
		if(tileTransmitter instanceof IMechanicalEnergy) {
			return ((IMechanicalEnergy) tileTransmitter).getAnimation();
		}else {
			return new MechanicalEnergyAnimation();
		}
		
	}
	
	public static EnumFacing getInputFacing(World world, BlockPos startPos) {
		
		TileEntity tile = world.getTileEntity(startPos);
		TileEntity tileTransmitter;
		BlockPos pos;
		
		for(EnumFacing f: EnumFacing.HORIZONTALS) {
			
			pos = startPos.offset(f,1);
			tileTransmitter = world.getTileEntity(pos);
			
			if(tileTransmitter instanceof IMechanicalEnergy) {
				
				if(((IMechanicalEnergy) tileTransmitter).isMechanicalEnergyTransmitter() && ((IMechanicalEnergy) tileTransmitter).getMechanicalEnergyOutputFacing()==f.getOpposite()) {
					
					return f;
					
				}
				
			}
			
		}
		
		return null;
		
	}
	
}
