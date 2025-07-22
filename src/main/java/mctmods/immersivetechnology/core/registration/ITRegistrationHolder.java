/*
 * tgstyle
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package mctmods.immersivetechnology.core.registration;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistrationBuilder;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.ComparatorManager;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockItem;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.MultiblockBEType;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.NonMirrorableWithActiveBlock;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.helper.ITMultiblockBuilder;
import mctmods.immersivetechnology.core.lib.ResourceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("all")
public class ITRegistrationHolder {
    private static final List<Consumer<IEventBus>> MOD_BUS_CALLBACKS = new ArrayList<>();

    public static void initialize() {
        ITMultiblockProvider.init();
        ITMenuTypes.REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static <T extends BlockEntity & IEBlockInterfaces.IGeneralMultiblock> MultiblockBEType<T> makeMultiblock(String name, MultiblockBEType.BEWithTypeConstructor<T> make, Supplier<? extends Block> block) { return new MultiblockBEType<>(name, ITBlockEntities.REGISTER, make, block, state -> state.hasProperty(IEProperties.MULTIBLOCKSLAVE)&&!state.getValue(IEProperties.MULTIBLOCKSLAVE)); }

    public static Supplier<List<? extends Item>> supplyDeferredItems() { return ITItems::getITItems; }

    public static Supplier<List<? extends Fluid>> supplyDeferredFluids() { return ITFluids::getITFluids; }

    public static Supplier<List<? extends Block>> supplyDeferredBlocks() { return ITBlocks::getITBlocks; }

    public static void addRegistersToEventBus(final IEventBus eventBus) {
        ITBlockEntities.init(eventBus);
        ITBlocks.init(eventBus);
        ITItems.init(eventBus);
        ITFluids.REGISTER.register(eventBus);
        ITFluids.TYPE_REGISTER.register(eventBus);
        ITCreativeTabs.REGISTER.register(eventBus);

        MOD_BUS_CALLBACKS.forEach(e -> e.accept(eventBus));
    }

    public static List<Item> getITItems() { return ITItems.getITItems(); }

    public static <S extends IMultiblockState> ITMultiblockBuilder<S> stone(IMultiblockLogic<S> logic, String name, boolean solid) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(2, 20);
        if (!solid) { properties.noOcclusion(); }
        return new ITMultiblockBuilder<>(logic, name).notMirrored().customBlock(ITBlocks.REGISTER, ITItems.REGISTER, r -> new NonMirrorableWithActiveBlock<>(properties, r), MultiblockItem::new).defaultBEs(ITBlockEntities.REGISTER);
    }

    public static <S extends IMultiblockState> ITMultiblockBuilder<S> metal(IMultiblockLogic<S> logic, String name) { return new ITMultiblockBuilder<>(logic, name).defaultBEs(ITBlockEntities.REGISTER).defaultBlock(ITBlocks.REGISTER, ITItems.REGISTER, IEBlocks.METAL_PROPERTIES_NO_OCCLUSION.get()); }

    protected static class MultiblockBuilder<S extends IMultiblockState> extends MultiblockRegistrationBuilder<S, MultiblockBuilder<S>> {
        public MultiblockBuilder(IMultiblockLogic<S> logic, String name) { super(logic, ResourceUtils.it(name)); }

        public MultiblockBuilder<S> redstone(IMultiblockComponent.StateWrapper<S, RedstoneControl.RSState> getState, BlockPos... positions) {
            redstoneAware();
            return selfWrappingComponent(new RedstoneControl<>(getState, positions));
        }

        public MultiblockBuilder<S> comparator(ComparatorManager<S> comparator) {
            withComparator();
            return super.selfWrappingComponent(comparator);
        }

        @Override
        protected MultiblockBuilder<S> self() { return this; }
    }
    public static ResourceLocation getRegistryNameOf(Block block) { return BuiltInRegistries.BLOCK.getKey(block); }
}
