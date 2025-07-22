package mctmods.immersivetechnology.common.blocks.multiblocks.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FullblockShape extends GenericShape {

    public static FullblockShape GETTER = new FullblockShape();

    @NotNull
    @Override
    protected List<AABB> getShape(BlockPos posInMultiblock) {
        List<AABB> main = new ArrayList<>();
        main.add(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
        return main;
    }
}
