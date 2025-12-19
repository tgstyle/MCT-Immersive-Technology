package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.client.ClientUtils;

import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityHighPressureSteamTurbineSlave;
import mctmods.immersivetechnology.common.multiblocks.types.BlockType_MetalMultiblock1;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.HighPressureSteamTurbineShape;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblockPart;
import mctmods.immersivetechnology.common.util.ITLogger;
import mctmods.immersivetechnology.common.util.multiblock.*;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityITMultiblockPartHighPressureSteamTurbine extends TileEntityITMultiblockPart<TileEntityHighPressureSteamTurbineSlave> implements MultiblockHandler.IMultiblock {
    public static TileEntityITMultiblockPartHighPressureSteamTurbine instance = new TileEntityITMultiblockPartHighPressureSteamTurbine();

    public TileEntityITMultiblockPartHighPressureSteamTurbine() {
        super(ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.HIGH_PRESSURE_STEAM_TURBINE.getMeta()),
                ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.HIGH_PRESSURE_STEAM_TURBINE_SLAVE.getMeta()));
        MultiblockJSONSchema data = HighPressureSteamTurbineShape.DATA;
        if (data == null) { ITLogger.error("No data for high_pressure_steam_turbine"); return; }
        boolean useTurbineMaterial = Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_turbine_material;
        String blockOreName = useTurbineMaterial && OreDictionary.doesOreNameExist("blockTungsten") ? "blockTungsten" : "blockNickel";
        String sheetmetalOreName = useTurbineMaterial && OreDictionary.doesOreNameExist("blockSheetmetalTungsten") ? "blockSheetmetalTungsten" : "blockSheetmetalNickel";
        boolean useBlockOre = OreDictionary.doesOreNameExist(blockOreName);
        boolean useSheetmetalOre = OreDictionary.doesOreNameExist(sheetmetalOreName);
        for (BlockJSONSchema p : data.palette) {
            if (p.character == 'B' && useBlockOre) { p.mod = "ore"; p.name = blockOreName; p.meta = 0; }
            else if (p.character == 'M' && useSheetmetalOre) { p.mod = "ore"; p.name = sheetmetalOreName; p.meta = 0; }
        }
        if ("immersiveengineering".equals(data.master.mod) && "storage".equals(data.master.name) && data.master.meta == 4 && useBlockOre) {
            data.master.mod = "ore";
            data.master.name = blockOreName;
            data.master.meta = 0;
        }
        this.uniqueName = data.uniqueName;
        this.width = data.width;
        this.height = data.height;
        this.length = data.length;
        this.pointsOfInterest = data.pointsOfInterest != null ? data.pointsOfInterest : new PoIJSONSchema[0];
        this.masterX = data.master.x;
        this.masterY = data.master.y;
        this.masterZ = data.master.z;
        this.structure = MultiblockUtils.GetStructure(data, width, length, height);
        this.materials = MultiblockUtils.GetMaterials(data);
        this.structureExport = MultiblockUtils.Convert(this.structure);
        if (data.master.mod.equals("ore")) { this.trigger = new OreDictRef(data.master.name); }
        else {
            Item item = Item.getByNameOrId(data.master.mod + ":" + data.master.name);
            if (item == null) throw new IllegalArgumentException(String.format("Invalid item %s:%s", data.master.mod, data.master.name));
            this.trigger = new ItemStackRef(new ItemStack(item, 1, data.master.meta));
        }
    }

    @Override public boolean overwriteBlockRender(ItemStack stack, int iterator) { return false; }

    @Override public float getManualScale() { return 8; }

    @Override public boolean canRenderFormedStructure() { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderFormedStructure() {
        if (renderStack == null) renderStack = new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.HIGH_PRESSURE_STEAM_TURBINE.getMeta());
        GlStateManager.translate(0.3, 0.1, 0);
        GlStateManager.translate(2.4, 2, 3.2);
        GlStateManager.rotate(-45, 0, 1, 0);
        GlStateManager.rotate(-20, 1, 0, 0);
        GlStateManager.scale(8.7, 8.7, 8.7);
        GlStateManager.disableCull();
        ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
        GlStateManager.enableCull();
    }

    @SideOnly(Side.CLIENT)
    static ItemStack renderStack;
}
