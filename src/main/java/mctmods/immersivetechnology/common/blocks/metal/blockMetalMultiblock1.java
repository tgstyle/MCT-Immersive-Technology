package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import mctmods.immersivetechnology.common.blocks.BlockITMultiblock;
import mctmods.immersivetechnology.common.blocks.ItemBlockITBase;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.*;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalMultiblock1;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.property.Properties;

public class blockMetalMultiblock1 extends BlockITMultiblock<BlockType_MetalMultiblock1> {
    public blockMetalMultiblock1() {
        super("metal_multiblock1", Material.IRON, PropertyEnum.create("type", BlockType_MetalMultiblock1.class), ItemBlockITBase.class, IEProperties.DYNAMICRENDER, IEProperties.BOOLEANS[0], Properties.AnimationProperty, IEProperties.OBJ_TEXTURE_REMAP);
        setHardness(3.0F);
        setResistance(15.0F);
        this.setAllNotNormalBlock();
        lightOpacity = 0;
    }

    @Override
    public boolean useCustomStateMapper() {
        return true;
    }

    @Override
    public String getCustomStateMapping(int meta, boolean itemBlock) {
        if(BlockType_MetalMultiblock1.values()[meta].needsCustomState()) return BlockType_MetalMultiblock1.values()[meta].getCustomState();
        return null;
    }

    @Override
    public boolean allowHammerHarvest(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createBasicTE(World worldIn, BlockType_MetalMultiblock1 type) {
        switch(type) {
            case GAS_TURBINE:
                return new TileEntityGasTurbineMaster();
            case GAS_TURBINE_SLAVE:
                return new TileEntityGasTurbineSlave();
            case HEAT_EXCHANGER:
                return new TileEntityHeatExchangerMaster();
            case HEAT_EXCHANGER_SLAVE:
                return new TileEntityHeatExchangerSlave();
            case HIGH_PRESSURE_STEAM_TURBINE:
                return new TileEntityHighPressureSteamTurbineMaster();
            case HIGH_PRESSURE_STEAM_TURBINE_SLAVE:
                return new TileEntityHighPressureSteamTurbineSlave();
            case ELECTROLYTIC_CRUCIBLE_BATTERY:
                return new TileEntityElectrolyticCrucibleBatteryMaster();
            case ELECTROLYTIC_CRUCIBLE_BATTERY_SLAVE:
                return new TileEntityElectrolyticCrucibleBatterySlave();
            case MELTING_CRUCIBLE:
                return new TileEntityMeltingCrucibleMaster();
            case MELTING_CRUCIBLE_SLAVE:
                return new TileEntityMeltingCrucibleSlave();
            case RADIATOR:
                return new TileEntityRadiatorMaster();
            case RADIATOR_SLAVE:
                return new TileEntityRadiatorSlave();
        }
        return null;
    }

}