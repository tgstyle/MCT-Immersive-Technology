package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.blocks.ItemBlockITBase;
import mctmods.immersivetechnology.common.multiblocks.BlockITMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerSolidMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerSolidSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock2;

import com.immersiveconvergence.api.block.ICProperties;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;
import net.minecraftforge.common.property.Properties;

public class BlockMetalMultiblock2 extends BlockITMultiblock<BlockType_MetalMultiblock2> {
    public BlockMetalMultiblock2() {
        super("metal_multiblock2", Material.IRON, PropertyEnum.create("type", BlockType_MetalMultiblock2.class), ItemBlockITBase.class, ICProperties.DYNAMICRENDER, ICProperties.BOOLEANS[0], ICProperties.BOOLEANS[1], Properties.AnimationProperty, ICProperties.OBJ_TEXTURE_REMAP);
        setHardness(3.0F);
        setResistance(15.0F);
        setMetaBlockLayer(BlockType_MetalMultiblock2.BOILER_SOLID.getMeta(), BlockRenderLayer.CUTOUT);
        setMetaBlockLayer(BlockType_MetalMultiblock2.BOILER_SOLID_SLAVE.getMeta(), BlockRenderLayer.CUTOUT);
        setAllNotNormalBlock();
        lightOpacity = 0;
    }

    @Override public TileEntity createBasicTE(World worldIn, BlockType_MetalMultiblock2 type) {
        switch (type) {
            case BOILER_SOLID: { return new TileEntityBoilerSolidMaster(); }
            case BOILER_SOLID_SLAVE: { return new TileEntityBoilerSolidSlave(); }
        }
        return null;
    }
}
