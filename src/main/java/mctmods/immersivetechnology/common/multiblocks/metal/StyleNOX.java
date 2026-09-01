package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.StyleNOXShape;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import net.minecraft.core.BlockPos;

public class StyleNOX extends MachineTemplateMultiblock {
    public static final StyleNOX INSTANCE = new StyleNOX();

    public StyleNOX() { super(Reference.rl("multiblocks/stylenox"), StyleNOXShape.MASTER_POS, StyleNOXShape.TRIGGER_POS,
            new BlockPos(StyleNOXShape.WIDTH, StyleNOXShape.HEIGHT, StyleNOXShape.LENGTH),
            StyleNOXShape.CLIENT_OFFSET, StyleNOXShape.MANUAL_SCALE, MultiblockRegistry.STYLENOX); }
}
