package mctmods.immersivetechnology.common.multiblocks.helper;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mctmods.immersivetechnology.common.blocks.helper.ITIBlockInterfaces;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

public class ITMultiblockBEType<T extends BlockEntity & ITIBlockInterfaces.IGeneralMultiblock> implements BiFunction<BlockPos, BlockState, T> {
    private final Supplier<BlockEntityType<T>> master;
    private final Supplier<BlockEntityType<T>> dummy;
    private final Predicate<BlockState> isMaster;

    public ITMultiblockBEType(String name, DeferredRegister<BlockEntityType<?>> register, IBEWithTypeConstructor<T> make, Supplier<? extends Block> block, Predicate<BlockState> isMaster) {
        this.isMaster = isMaster;
        this.master = register.register(name + "_master", makeType(make, block));
        this.dummy = register.register(name + "_dummy", makeType(make, block));
    }

    @Override public T apply(BlockPos pos, BlockState state) {
        return this.isMaster.test(state) ? this.master.get().create(pos, state) : this.dummy.get().create(pos, state);
    }

    public static <T extends BlockEntity> Supplier<BlockEntityType<T>> makeType(IBEWithTypeConstructor<T> create, Supplier<? extends Block> valid) {
        return () -> {
            Mutable<BlockEntityType<T>> typeMutable = new MutableObject<>();
            @SuppressWarnings("ConstantConditions")
            BlockEntityType<T> type = BlockEntityType.Builder.of((pos, state) -> create.create(typeMutable.getValue(), pos, state), valid.get()).build(null);
            typeMutable.setValue(type);
            return type;
        };
    }

    public BlockEntityType<T> master() { return this.master.get(); }

    public BlockEntityType<T> dummy() { return this.dummy.get(); }

    public interface IBEWithTypeConstructor<T extends BlockEntity> {
        T create(BlockEntityType<T> type, BlockPos pos, BlockState state);
    }
}
