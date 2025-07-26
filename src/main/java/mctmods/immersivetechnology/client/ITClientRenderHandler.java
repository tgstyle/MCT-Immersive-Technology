package mctmods.immersivetechnology.client;

import mctmods.immersivetechnology.common.blocks.helper.ITBlockType;
import mctmods.immersivetechnology.common.items.helper.ITFlagItem;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import mctmods.immersivetechnology.core.registration.ITFluids;
import mctmods.immersivetechnology.core.registration.ITItems;
import mctmods.immersivetechnology.core.registration.ITParticles;
import mctmods.immersivetechnology.client.particles.ColoredSmokeParticleProvider;
import mctmods.immersivetechnology.client.renderer.OpenBarrelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "immersivetechnology", bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ITClientRenderHandler implements net.minecraft.client.color.item.ItemColor, net.minecraft.client.color.block.BlockColor {
    @net.minecraftforge.api.distmarker.OnlyIn(Dist.CLIENT)
    private static Map<RenderTypeSkeleton, RenderType> renderTypes;

    private static final Map<Block, RenderTypeSkeleton> mapping = new HashMap<>();

    public static ITClientRenderHandler INSTANCE = new ITClientRenderHandler();

    @net.minecraftforge.api.distmarker.OnlyIn(Dist.CLIENT)
    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> BlockEntityRenderers.register(ITBlockEntities.OPEN_BARREL.get(), OpenBarrelRenderer::new));
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) { event.registerSpriteSet(ITParticles.COLORED_SMOKE.get(), ColoredSmokeParticleProvider::new); }

    @SubscribeEvent
    public static void onItemColor(RegisterColorHandlersEvent.Item event) {
        for (RegistryObject<? extends Item> holder : ITItems.getItemRegistryMap().values()) {
            Item i = holder.get();
            if (i instanceof ITFlagItem) { event.getItemColors().register(INSTANCE, i); }
        }

        for (ITFluids.FluidEntry entry : ITFluids.ALL_ENTRIES) {
            final int tint = entry.tintColor();
            event.getItemColors().register((stack, index) -> { if (index == 1) { return tint; } return -1; }, entry.bucket().get());
        }
    }

    @SubscribeEvent
    public static void onBlockColor(RegisterColorHandlersEvent.Block event) {
        for (ITFluids.FluidEntry entry : ITFluids.ALL_ENTRIES) {
            final int tint = entry.tintColor();
            event.getBlockColors().register((state, level, pos, index) -> tint, entry.block().get());
        }
    }

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter getter, @Nullable BlockPos pos, int index) {
        if (state.getBlock() instanceof ITBlockType type) { return type.getColor(index); }
        return 0xffffff;
    }

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if (stack.getItem() instanceof ITFlagItem type) { return type.getColor(tintIndex); }
        return 0xffffff;
    }

    @OnlyIn(Dist.CLIENT)
    public static void setRenderType(Block block, RenderTypeSkeleton skeleton) { setRenderTypeClient(block, skeleton); }

    @OnlyIn(Dist.CLIENT)
    private static void setRenderTypeClient(Block block, RenderTypeSkeleton skeleton) { resolveRenderTypes(); mapping.put(block, skeleton); }

    @OnlyIn(Dist.CLIENT)
    private static void resolveRenderTypes() {
        if (renderTypes == null) {
            renderTypes = new HashMap<>();
            renderTypes.put(RenderTypeSkeleton.SOLID, RenderType.solid());
            renderTypes.put(RenderTypeSkeleton.CUTOUT, RenderType.cutout());
            renderTypes.put(RenderTypeSkeleton.CUTOUT_MIPPED, RenderType.cutoutMipped());
            renderTypes.put(RenderTypeSkeleton.TRANSLUCENT, RenderType.translucent());
        }
    }

    public enum RenderTypeSkeleton {
        SOLID,
        CUTOUT,
        CUTOUT_MIPPED,
        TRANSLUCENT
    }
}
