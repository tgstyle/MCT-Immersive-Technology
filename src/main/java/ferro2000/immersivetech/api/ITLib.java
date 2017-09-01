package ferro2000.immersivetech.api;

import net.minecraft.util.EnumFacing;

public class ITLib {
	
	//GUI ID
	public static final int GUIID_Base_Tile = 0;
	public static final int GUIID_Distiller = GUIID_Base_Tile + 0;
	public static final int GUIID_Solar_Tower = GUIID_Base_Tile + 1;
	public static final int GUIID_Boiler = GUIID_Base_Tile + 2;
	
	
	/*	
	 * 			|
	 * 			A
	 * 			|
	 * 		 +--------+
	 * 		 |		  |< D >
	 * 		 |		  |
	 * 		 |		  |
	 * 	< C >|		  |
	 * 		 +--------+
	 * 				|
	 * 				B	
	 * 				|	  
	 * */
	
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

}
