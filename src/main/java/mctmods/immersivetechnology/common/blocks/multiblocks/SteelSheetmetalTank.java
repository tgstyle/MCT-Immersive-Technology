package mctmods.immersivetechnology.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.SteelSheetmetalTankShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import java.util.function.Consumer;

public class SteelSheetmetalTank extends ITTemplateMultiblock {
    public static final SteelSheetmetalTank INSTANCE = new SteelSheetmetalTank();

    public SteelSheetmetalTank() { super(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "multiblocks/steel_sheetmetal_tank"), SteelSheetmetalTankShape.MASTER_POS, SteelSheetmetalTankShape.TRIGGER_POS, new BlockPos(SteelSheetmetalTankShape.WIDTH, SteelSheetmetalTankShape.HEIGHT, SteelSheetmetalTankShape.LENGTH), ITMultiblockProvider.STEEL_SHEETMETAL_TANK); }

    @Override
    public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) { super.disassemble(world, origin, mirrored, clickDirectionAtCreation); }

    @Override
    public float getManualScale() { return SteelSheetmetalTankShape.MANUAL_SCALE; }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, SteelSheetmetalTankShape.CLIENT_OFFSET.getX(), SteelSheetmetalTankShape.CLIENT_OFFSET.getY(), SteelSheetmetalTankShape.CLIENT_OFFSET.getZ())); }
}
