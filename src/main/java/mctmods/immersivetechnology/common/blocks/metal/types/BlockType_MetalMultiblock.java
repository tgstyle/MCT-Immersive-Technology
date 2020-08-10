package mctmods.immersivetechnology.common.blocks.metal.types;

import mctmods.immersivetechnology.common.blocks.BlockITBase;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockType_MetalMultiblock implements IStringSerializable, BlockITBase.IBlockEnum {
	DISTILLER(true),
	SOLAR_TOWER(false),
	SOLAR_REFLECTOR(false), 
	STEAM_TURBINE(true),
	BOILER(true),
	ALTERNATOR(false),
	DISTILLER_SLAVE(true),
	SOLAR_TOWER_SLAVE(false),
	STEAM_TURBINE_SLAVE(true),
	BOILER_SLAVE(true),
	ALTERNATOR_SLAVE(false),
	SOLAR_REFLECTOR_SLAVE(false),
	STEEL_TANK(false),
	STEEL_TANK_SLAVE(false),
	COOLING_TOWER(false),
	COOLING_TOWER_SLAVE(false);

	private boolean needsCustomState;
	BlockType_MetalMultiblock(boolean needsCustomState) {
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

	public boolean needsCustomState() {
		return this.needsCustomState;
	}
	public String getCustomState() {
		return getName().toLowerCase();
	}

}