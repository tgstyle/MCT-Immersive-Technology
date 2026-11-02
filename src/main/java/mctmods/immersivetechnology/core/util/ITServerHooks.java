package mctmods.immersivetechnology.core.util;

import com.immersiveconvergence.common.blocks.pipes.TileEntityFluidPipeAlternative;
import net.minecraftforge.fml.common.Loader;

public class ITServerHooks {
    public static void serverStopping() {
        if (Loader.isModLoaded("immersiveengineering")) { TileEntityFluidPipeAlternative.indirectConnections.clear(); }
    }
}
