package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.blocks.ItemBlockITBase;
import mctmods.immersivetechnology.common.multiblocks.BlockITMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityAlternatorMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityAlternatorSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerTankMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerTankSlave;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityCoolingTowerMaster;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityCoolingTowerSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityDistillerMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityDistillerSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarReflectorMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarReflectorSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarTowerMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarTowerSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySteamTurbineMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySteamTurbineSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySteelSheetmetalTankMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySteelSheetmetalTankSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock;
import com.immersiveconvergence.api.block.ICProperties;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;
import net.minecraftforge.common.property.Properties;

public class BlockMetalMultiblock extends BlockITMultiblock<BlockType_MetalMultiblock> {
    public BlockMetalMultiblock() {
        super("metal_multiblock", Material.IRON, PropertyEnum.create("type", BlockType_MetalMultiblock.class), ItemBlockITBase.class, ICProperties.DYNAMICRENDER, ICProperties.BOOLEANS[0], Properties.AnimationProperty, ICProperties.OBJ_TEXTURE_REMAP);
        setHardness(3.0F);
        setResistance(15.0F);
        setMetaBlockLayer(BlockType_MetalMultiblock.STEEL_TANK.getMeta(), BlockRenderLayer.CUTOUT);
        setMetaBlockLayer(BlockType_MetalMultiblock.BOILER_TANK.getMeta(), BlockRenderLayer.CUTOUT);
        setMetaBlockLayer(BlockType_MetalMultiblock.BOILER_TANK_SLAVE.getMeta(), BlockRenderLayer.CUTOUT);
        setAllNotNormalBlock();
        lightOpacity = 0;
    }

    @Override public TileEntity createBasicTE(World worldIn, BlockType_MetalMultiblock type) {
        switch (type) {
            case ALTERNATOR: return new TileEntityAlternatorMaster();
            case ALTERNATOR_SLAVE: return new TileEntityAlternatorSlave();
            case BOILER_TANK: return new TileEntityBoilerTankMaster();
            case BOILER_TANK_SLAVE: return new TileEntityBoilerTankSlave();
            case DISTILLER: return new TileEntityDistillerMaster();
            case DISTILLER_SLAVE: return new TileEntityDistillerSlave();
            case SOLAR_REFLECTOR: return new TileEntitySolarReflectorMaster();
            case SOLAR_REFLECTOR_SLAVE: return new TileEntitySolarReflectorSlave();
            case SOLAR_TOWER: return new TileEntitySolarTowerMaster();
            case SOLAR_TOWER_SLAVE: return new TileEntitySolarTowerSlave();
            case STEAM_TURBINE: return new TileEntitySteamTurbineMaster();
            case STEAM_TURBINE_SLAVE: return new TileEntitySteamTurbineSlave();
            case STEEL_TANK: return new TileEntitySteelSheetmetalTankMaster();
            case STEEL_TANK_SLAVE: return new TileEntitySteelSheetmetalTankSlave();
            case COOLING_TOWER: return new TileEntityCoolingTowerMaster();
            case COOLING_TOWER_SLAVE: return new TileEntityCoolingTowerSlave();
        }
        return null;
    }
}
