package mctmods.immersivetechnology.common.blocks.multiblocks.tileentitiesmultiblockpart;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.client.ClientUtils;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.MeltingCrucibleShape;
import mctmods.immersivetechnology.common.blocks.multiblocks.tileentities.TileEntityMeltingCrucibleSlave;
import mctmods.immersivetechnology.common.blocks.multiblocks.types.BlockType_MetalMultiblock1;
import mctmods.immersivetechnology.common.tileentities.TileEntityITMultiblockPart;
import mctmods.immersivetechnology.common.util.ITLogger;
import mctmods.immersivetechnology.common.util.multiblock.*;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityITMultiblockPartMeltingCrucible extends TileEntityITMultiblockPart<TileEntityMeltingCrucibleSlave> implements MultiblockHandler.IMultiblock {
    public static TileEntityITMultiblockPartMeltingCrucible instance = new TileEntityITMultiblockPartMeltingCrucible();

    @SideOnly(Side.CLIENT)
    static ItemStack renderStack;

    public TileEntityITMultiblockPartMeltingCrucible() {
        super(ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.MELTING_CRUCIBLE.getMeta()),
                ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.MELTING_CRUCIBLE_SLAVE.getMeta()));
        MultiblockJSONSchema data = MeltingCrucibleShape.DATA;
        if (data == null) { ITLogger.error("No data for melting_crucible"); return; }
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

    @Override public float getManualScale() { return 12; }

    @Override public boolean canRenderFormedStructure() { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderFormedStructure() {
        if (renderStack == null) renderStack = new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.MELTING_CRUCIBLE.getMeta());
        GlStateManager.translate(1.5, 1.5, 1.5);
        GlStateManager.rotate(-45, 0, 1, 0);
        GlStateManager.rotate(-20, 1, 0, 0);
        GlStateManager.scale(3.5, 3.5, 3.5);
        GlStateManager.disableCull();
        ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
        GlStateManager.enableCull();
    }
}
