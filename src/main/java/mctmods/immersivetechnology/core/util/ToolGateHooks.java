package mctmods.immersivetechnology.core.util;

import mctmods.immersivetechnology.common.items.FormationTool;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.ModTags;

import com.immersiveconvergence.api.block.BlockToolGates;
import com.immersiveconvergence.api.multiblock.ClearTank;

public class ToolGateHooks {
    public static void install() {
        ClearTank.additionalTool = stack -> stack.getItem() instanceof FormationTool;
        BlockToolGates.isFormationTool = stack -> stack.is(ModTags.formationTools);
        BlockToolGates.isScrewdriver = stack -> stack.is(ModTags.screwdrivers);
        BlockToolGates.descFlavour = Reference.DESC_FLAVOUR;
        BlockToolGates.descInfo = Reference.DESC_INFO;
    }
}
