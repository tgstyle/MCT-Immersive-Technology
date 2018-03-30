package ferro2000.immersivetech.common.blocks.stone.types;

import java.util.Locale;

import ferro2000.immersivetech.common.blocks.BlockITBase;
import net.minecraft.util.IStringSerializable;

public enum BlockType_StoneDecoration implements IStringSerializable, BlockITBase.IBlockEnum
{
	COKEBRICK_REINFORCED;
	
	@Override
	public String getName()
	{
		return this.toString().toLowerCase(Locale.ENGLISH);
	}
	@Override
	public int getMeta()
	{
		return ordinal();
	}
	@Override
	public boolean listForCreative()
	{
		return true;
	}
}
