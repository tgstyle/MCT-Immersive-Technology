package mctmods.immersivetechnology.core.registration;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockItem;
import mctmods.immersivetechnology.common.blocks.helper.BlockInterfaces;
import mctmods.immersivetechnology.common.blocks.helper.ModProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.MultiblockBEType;
import mctmods.immersivetechnology.common.multiblocks.helper.MultiblockBuilder;
import mctmods.immersivetechnology.common.multiblocks.helper.MultiblockPartBlockNonMirrorActiveBlock;
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
public class MultiblockProvider {
    public static <T extends BlockEntity & BlockInterfaces.IGeneralMultiblock> MultiblockBEType<T> makeMultiblock(String name, MultiblockBEType.IBEWithTypeConstructor<T> make, Supplier<? extends Block> block) { return new MultiblockBEType<>(name, BlockEntities.REGISTER, make, block, state -> state.hasProperty(ModProperties.MULTIBLOCKSLAVE) && !state.getValue(ModProperties.MULTIBLOCKSLAVE)); }

    public static Supplier<List<? extends Item>> supplyDeferredItems() { return ModItems::getITItems; }

    public static Supplier<List<? extends Fluid>> supplyDeferredFluids() { return ModFluids::getITFluids; }

    public static Supplier<List<? extends Block>> supplyDeferredBlocks() { return ModBlocks::getITBlocks; }

    public static List<Item> getITItems() { return ModItems.getITItems(); }

    public static <S extends IMultiblockState> MultiblockBuilder<S> stone(IMultiblockLogic<S> logic, String name, boolean solid) { BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(2, 20); if (!solid) properties.noOcclusion(); return new MultiblockBuilder<>(logic, name).notMirrored().customBlock(ModBlocks.REGISTER, ModItems.REGISTER, r -> new MultiblockPartBlockNonMirrorActiveBlock<>(properties, r), MultiblockItem::new).defaultBEs(BlockEntities.REGISTER); }

    public static <S extends IMultiblockState> MultiblockBuilder<S> metal(IMultiblockLogic<S> logic, String name) { return new MultiblockBuilder<>(logic, name).defaultBEs(BlockEntities.REGISTER).defaultBlock(ModBlocks.REGISTER, ModItems.REGISTER, ModBlocks.METAL_PROPERTIES_NO_OCCLUSION.get()); }
}
