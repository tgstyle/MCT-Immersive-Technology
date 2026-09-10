package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.blocks.ItemBlockITBase;
import mctmods.immersivetechnology.common.multiblocks.BlockITMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.*;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock1;

import com.immersiveconvergence.api.block.ICProperties;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;
import net.minecraftforge.common.property.Properties;

public class BlockMetalMultiblock1 extends BlockITMultiblock<BlockType_MetalMultiblock1> {
    public BlockMetalMultiblock1() {
        super("metal_multiblock1", Material.IRON, PropertyEnum.create("type", BlockType_MetalMultiblock1.class), ItemBlockITBase.class, ICProperties.DYNAMICRENDER, ICProperties.BOOLEANS[0], Properties.AnimationProperty, ICProperties.OBJ_TEXTURE_REMAP);
        setHardness(3.0F);
        setResistance(15.0F);
        setMetaBlockLayer(BlockType_MetalMultiblock1.BOILER_LIQUID.getMeta(), BlockRenderLayer.CUTOUT);
        setMetaBlockLayer(BlockType_MetalMultiblock1.BOILER_LIQUID_SLAVE.getMeta(), BlockRenderLayer.CUTOUT);
        setAllNotNormalBlock();
        lightOpacity = 0;
    }

    @Override public TileEntity createBasicTE(World worldIn, BlockType_MetalMultiblock1 type) {
        switch (type) {
            case GAS_TURBINE: { return new TileEntityGasTurbineMaster(); }
            case GAS_TURBINE_SLAVE: { return new TileEntityGasTurbineSlave(); }
            case HEAT_EXCHANGER: { return new TileEntityHeatExchangerMaster(); }
            case HEAT_EXCHANGER_SLAVE: { return new TileEntityHeatExchangerSlave(); }
            case HIGH_PRESSURE_STEAM_TURBINE: { return new TileEntityHighPressureSteamTurbineMaster(); }
            case HIGH_PRESSURE_STEAM_TURBINE_SLAVE: { return new TileEntityHighPressureSteamTurbineSlave(); }
            case ELECTROLYTIC_CRUCIBLE_BATTERY: { return new TileEntityElectrolyticCrucibleBatteryMaster(); }
            case ELECTROLYTIC_CRUCIBLE_BATTERY_SLAVE: { return new TileEntityElectrolyticCrucibleBatterySlave(); }
            case MELTING_CRUCIBLE: { return new TileEntityMeltingCrucibleMaster(); }
            case MELTING_CRUCIBLE_SLAVE: { return new TileEntityMeltingCrucibleSlave(); }
            case RADIATOR: { return new TileEntityRadiatorMaster(); }
            case RADIATOR_SLAVE: { return new TileEntityRadiatorSlave(); }
            case SOLAR_MELTER: { return new TileEntitySolarMelterMaster(); }
            case SOLAR_MELTER_SLAVE: { return new TileEntitySolarMelterSlave(); }
            case BOILER_LIQUID: { return new TileEntityBoilerLiquidMaster(); }
            case BOILER_LIQUID_SLAVE: { return new TileEntityBoilerLiquidSlave(); }
        }
        return null;
    }
}
