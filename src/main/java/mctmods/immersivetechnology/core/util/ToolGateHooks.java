package mctmods.immersivetechnology.core.util;

import mctmods.immersivetechnology.core.lib.Reference;

import com.immersiveconvergence.api.block.BlockToolGates;

public class ToolGateHooks {
    public static void install() {
        BlockToolGates.descFlavour = Reference.DESC_FLAVOUR;
        BlockToolGates.descInfo = Reference.DESC_INFO;
    }
}
