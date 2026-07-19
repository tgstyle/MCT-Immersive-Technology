package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistrationBuilder;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.ComparatorManager;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockPartBlock;
import com.google.common.base.Preconditions;
import mctmods.immersivetechnology.common.multiblocks.gui.helper.MultiblockGui;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.DeferredRegister;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.function.Function;
import java.util.function.Supplier;

public class MultiblockBuilder<S extends IMultiblockState> extends MultiblockRegistrationBuilder<S, MultiblockBuilder<S>> {
    private Supplier<MultiblockRegistration<S>> regSupplier = () -> { throw new IllegalStateException("Accessed multiblock registration too early"); };

    public MultiblockBuilder(IMultiblockLogic<S> logic, String name) { super(logic, Reference.rl(name)); }

    public MultiblockBuilder<S> gui(MenuTypes.MultiblockContainer<S, ?> menu) { return component(new MultiblockGui<>(menu)); }

    public MultiblockBuilder<S> redstone(IMultiblockComponent.StateWrapper<S, RedstoneControl.RSState> getState, BlockPos... positions) { redstoneAware(); return selfWrappingComponent(new RedstoneControl<>(getState, positions)); }

    @SuppressWarnings("ConstantConditions") public MultiblockBuilder<S> customBEs(DeferredRegister<BlockEntityType<?>> register) {
        try {
            ResourceLocation rl = ObfuscationReflectionHelper.getPrivateValue(MultiblockRegistrationBuilder.class, this, "name");
            Supplier<? extends Block> blockSup = ObfuscationReflectionHelper.getPrivateValue(MultiblockRegistrationBuilder.class, this, "block");

            Supplier<BlockEntityType<?>> masterSup = register.register(rl.getPath() + "_master", () -> {
                Mutable<BlockEntityType<?>> resultBox = new MutableObject<>();
                resultBox.setValue(BlockEntityType.Builder.of((pos, state) -> new ModMultiblockBlockEntityMaster<>(resultBox.getValue(), pos, state, regSupplier.get()), blockSup.get()).build(null));
                return resultBox.getValue();
            });
            Supplier<BlockEntityType<?>> dummySup = register.register(rl.getPath() + "_dummy", () -> {
                Mutable<BlockEntityType<?>> resultBox = new MutableObject<>();
                resultBox.setValue(BlockEntityType.Builder.of((pos, state) -> new ModMultiblockBlockEntityDummy<>(resultBox.getValue(), pos, state, regSupplier.get()), blockSup.get()).build(null));
                return resultBox.getValue();
            });

            ObfuscationReflectionHelper.setPrivateValue(MultiblockRegistrationBuilder.class, this, masterSup, "masterBE");
            ObfuscationReflectionHelper.setPrivateValue(MultiblockRegistrationBuilder.class, this, dummySup, "dummyBE");
        } catch (Exception e) { throw new RuntimeException(e); }
        return this;
    }

    @Override public MultiblockBuilder<S> customBlock(DeferredRegister<Block> register, DeferredRegister<Item> blockItemRegister, Function<MultiblockRegistration<S>, ? extends MultiblockPartBlock<S>> make, Function<Block, Item> makeItem) { super.customBlock(register, blockItemRegister, make, makeItem); return this; }

    @Override public MultiblockBuilder<S> defaultBlock(DeferredRegister<Block> register, DeferredRegister<Item> blockItemRegister, BlockBehaviour.Properties properties) { super.defaultBlock(register, blockItemRegister, properties); return this; }

    @Override public <CS, C extends IMultiblockComponent<CS> & IMultiblockComponent.StateWrapper<S, CS>> MultiblockBuilder<S> selfWrappingComponent(C extraComponent) { Preconditions.checkArgument(!(extraComponent instanceof ComparatorManager<?>)); return super.selfWrappingComponent(extraComponent); }

    @Override protected MultiblockBuilder<S> self() { return this; }

    @Override public MultiblockRegistration<S> build() { MultiblockRegistration<S> reg = super.build(); regSupplier = () -> reg; return reg; }
}
