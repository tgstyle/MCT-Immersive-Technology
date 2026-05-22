package mctmods.immersivetechnology.core.registration;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockItem;
import blusunrize.immersiveengineering.common.blocks.MultiblockBEType;
import mctmods.immersivetechnology.common.blocks.helper.ITBlockInterfaces;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITMultiblockBuilder;
import mctmods.immersivetechnology.common.multiblocks.helper.ITNonMirrorableWithActiveBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class ITMultiblockRegistry {
    public static <T extends BlockEntity & ITBlockInterfaces.IGeneralMultiblock> MultiblockBEType<T> makeMultiblock(String name, MultiblockBEType.BEWithTypeConstructor<T> make, Supplier<? extends Block> block) { return new MultiblockBEType<>(name, ITBlockEntities.REGISTER, make, block, state -> state.hasProperty(ITProperties.MULTIBLOCKSLAVE) && !state.getValue(ITProperties.MULTIBLOCKSLAVE)); }

    public static Supplier<List<? extends Item>> supplyDeferredItems() { return ITItems::getITItems; }

    public static Supplier<List<? extends Fluid>> supplyDeferredFluids() { return ITFluids::getITFluids; }

    public static Supplier<List<? extends Block>> supplyDeferredBlocks() { return ITBlocks::getITBlocks; }

    public static List<Item> getITItems() { return ITItems.getITItems(); }

    public static <S extends IMultiblockState> ITMultiblockBuilder<S> stone(IMultiblockLogic<S> logic, String name, boolean solid) { BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(2, 20); if (!solid) properties.noOcclusion(); return new ITMultiblockBuilder<>(logic, name).notMirrored().customBlock(ITBlocks.REGISTER, ITItems.REGISTER, r -> new ITNonMirrorableWithActiveBlock<>(properties, r), MultiblockItem::new).defaultBEs(ITBlockEntities.REGISTER); }

    public static <S extends IMultiblockState> ITMultiblockBuilder<S> metal(IMultiblockLogic<S> logic, String name) { return new ITMultiblockBuilder<>(logic, name).defaultBEs(ITBlockEntities.REGISTER).defaultBlock(ITBlocks.REGISTER, ITItems.REGISTER, ITBlocks.METAL_PROPERTIES_NO_OCCLUSION.get()); }
}
