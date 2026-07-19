package mctmods.immersivetechnology.common.blocks.helper;

import blusunrize.immersiveengineering.api.IEProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import java.util.Map;

public class ModProperties {
    public static final DirectionProperty FACING_ALL;
    public static final DirectionProperty FACING_HORIZONTAL;
    public static final DirectionProperty FACING_TOP_DOWN;
    public static final BooleanProperty MULTIBLOCKSLAVE;
    public static final BooleanProperty ACTIVE;
    public static final BooleanProperty MIRRORED;
    public static final IntegerProperty INT_16;
    public static final IntegerProperty INT_32;
    static {
        FACING_ALL = IEProperties.FACING_ALL;
        FACING_HORIZONTAL = IEProperties.FACING_HORIZONTAL;
        FACING_TOP_DOWN = IEProperties.FACING_TOP_DOWN;
        MULTIBLOCKSLAVE = IEProperties.MULTIBLOCKSLAVE;
        MIRRORED = IEProperties.MIRRORED;
        ACTIVE = BooleanProperty.create("active");
        INT_16 = IntegerProperty.create("int_16", 0, 15);
        INT_32 = IntegerProperty.create("int_32", 0, 31);
    }
    public static class Model {
        public static final ModelProperty<Map<Direction, Enums.IOSideConfig>> SIDECONFIG = new ModelProperty<>();
        public static final ModelProperty<BlockPos> SUBMODEL_OFFSET = new ModelProperty<>();
    }
}
