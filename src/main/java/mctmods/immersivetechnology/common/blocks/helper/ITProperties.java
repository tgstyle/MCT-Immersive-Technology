package mctmods.immersivetechnology.common.blocks.helper;

import blusunrize.immersiveengineering.api.utils.DirectionUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.client.model.data.ModelProperty;

public class ITProperties {
    public static final DirectionProperty FACING_ALL;
    public static final DirectionProperty FACING_HORIZONTAL;
    public static final DirectionProperty FACING_TOP_DOWN;
    public static final BooleanProperty MULTIBLOCKSLAVE;
    public static final BooleanProperty ACTIVE;
    public static final BooleanProperty MIRRORED;
    public static final IntegerProperty INT_16;
    public static final IntegerProperty INT_32;

    static {
        FACING_ALL = DirectionProperty.create("facing", DirectionUtils.VALUES);
        FACING_HORIZONTAL = DirectionProperty.create("facing", Plane.HORIZONTAL);
        FACING_TOP_DOWN = DirectionProperty.create("facing", Direction.UP, Direction.DOWN);
        MULTIBLOCKSLAVE = BooleanProperty.create("multiblockslave");
        ACTIVE = BooleanProperty.create("active");
        MIRRORED = BooleanProperty.create("mirrored");
        INT_16 = IntegerProperty.create("int_16", 0, 15);
        INT_32 = IntegerProperty.create("int_32", 0, 31);
    }

    public static class Model {
        public static final ModelProperty<BlockPos> SUBMODEL_OFFSET = new ModelProperty<>();
    }
}
