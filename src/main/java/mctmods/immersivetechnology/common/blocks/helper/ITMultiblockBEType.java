package mctmods.immersivetechnology.common.blocks.helper;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.types.Type;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ITMultiblockBEType<T extends BlockEntity & ITBlockInterfaces.IGeneralMultiblock> implements BiFunction<BlockPos, BlockState, T> {
    private final RegistryObject<BlockEntityType<T>> master;
    private final RegistryObject<BlockEntityType<T>> dummy;
    private final Predicate<BlockState> isMaster;

    public ITMultiblockBEType(String name, DeferredRegister<BlockEntityType<?>> register, ITMultiblockBEType.BEWithTypeConstructor<T> make, Supplier<? extends Block> block, Predicate<BlockState> isMaster) {
        this.isMaster = isMaster;
        this.master = register.register(name + "_master", makeType(make, block));
        this.dummy = register.register(name + "_dummy", makeType(make, block));
    }

    @Nullable
    public T apply(BlockPos pos, BlockState state) {
        return (T) (this.isMaster.test(state) ? ((BlockEntityType)this.master.get()).create(pos, state) : ((BlockEntityType)this.dummy.get()).create(pos, state));
    }

    public static <T extends BlockEntity> Supplier<BlockEntityType<T>> makeType(ITMultiblockBEType.BEWithTypeConstructor<T> create, Supplier<? extends Block> valid) {
        return () -> {
            Mutable<BlockEntityType<T>> typeMutable = new MutableObject();
            BlockEntityType<T> type = new BlockEntityType((pos, state) -> {
                return create.create((BlockEntityType)typeMutable.getValue(), pos, state);
            }, ImmutableSet.of((Block)valid.get()), (Type)null);
            typeMutable.setValue(type);
            return type;
        };
    }

    public BlockEntityType<T> master() {
        return (BlockEntityType)this.master.get();
    }

    public BlockEntityType<T> dummy() {
        return (BlockEntityType)this.dummy.get();
    }

    public RegistryObject<BlockEntityType<T>> dummyHolder() {
        return this.dummy;
    }

    public RegistryObject<BlockEntityType<T>> masterHolder() {
        return this.master;
    }

    public interface BEWithTypeConstructor<T extends BlockEntity> {
        T create(BlockEntityType<T> var1, BlockPos var2, BlockState var3);
    }
}