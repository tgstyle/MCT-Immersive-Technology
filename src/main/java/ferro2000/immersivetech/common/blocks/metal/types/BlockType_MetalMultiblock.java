package ferro2000.immersivetech.common.blocks.metal.types;

import java.util.Locale;

import ferro2000.immersivetech.common.blocks.BlockITBase;
import net.minecraft.util.IStringSerializable;

public enum BlockType_MetalMultiblock implements IStringSerializable, BlockITBase.IBlockEnum{
	
	DISTILLER(true),
	SOLAR_TOWER(false),
	SOLAR_REFLECTOR(false),
	STEAM_TURBINE(true);
	
	private boolean needsCustomState;
	BlockType_MetalMultiblock(boolean needsCustomState){
		this.needsCustomState = needsCustomState;
	}

	@Override
	public int getMeta() {
		return ordinal();
	}

	@Override
	public boolean listForCreative() {
		return false;
	}

	@Override
	public String getName() {
		return this.toString().toLowerCase(Locale.ENGLISH);
	}
	
	public boolean needsCustomState()
	{
		return this.needsCustomState;
	}
	public String getCustomState()
	{
		String[] split = getName().split("_");
		String s = split[0].toLowerCase(Locale.ENGLISH);
		for(int i=1; i<split.length; i++)
			s+=split[i].substring(0,1).toUpperCase(Locale.ENGLISH)+split[i].substring(1).toLowerCase(Locale.ENGLISH);
		return s;
	}

}
