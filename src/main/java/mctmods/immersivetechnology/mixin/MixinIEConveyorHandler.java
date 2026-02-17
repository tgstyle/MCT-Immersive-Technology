package mctmods.immersivetechnology.mixin;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import com.google.common.collect.Maps;
import mctmods.immersivetechnology.common.blocks.metal.conveyors.*;
import mctmods.immersivetechnology.core.MCTMixin;
import mctmods.immersivetechnology.core.MCTMixinConfig;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = ConveyorHandler.class, remap = false)
public abstract class MixinIEConveyorHandler {

    @Unique private static final Map<String, Class<? extends IConveyorBelt>> REPLACEMENT_CLASSES = Maps.newHashMap();

    static {
        REPLACEMENT_CLASSES.put("conveyor", ConveyorBasicAlternative.class);
        REPLACEMENT_CLASSES.put("uncontrolled", ConveyorUncontrolledAlternative.class);
        REPLACEMENT_CLASSES.put("splitter", ConveyorSplitAlternative.class);
        REPLACEMENT_CLASSES.put("covered", ConveyorCoveredAlternative.class);
        REPLACEMENT_CLASSES.put("dropper", ConveyorDropAlternative.class);
        REPLACEMENT_CLASSES.put("droppercovered", ConveyorDropCoveredAlternative.class);
        REPLACEMENT_CLASSES.put("extract", ConveyorExtractAlternative.class);
        REPLACEMENT_CLASSES.put("extractcovered", ConveyorExtractCoveredAlternative.class);
        REPLACEMENT_CLASSES.put("vertical", ConveyorVerticalAlternative.class);
        REPLACEMENT_CLASSES.put("verticalcovered", ConveyorVerticalCoveredAlternative.class);
    }

    @Inject(
            method = "getConveyor(Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/tileentity/TileEntity;)Lblusunrize/immersiveengineering/api/tool/ConveyorHandler$IConveyorBelt;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void injectGetConveyor(ResourceLocation key, TileEntity tile, CallbackInfoReturnable<IConveyorBelt> cir) {
        if (!MCTMixinConfig.mixinSettings.replace_IE_conveyors) return;
        if (key == null || !"immersiveengineering".equals(key.getNamespace())) return;

        String path = key.getPath();
        Class<? extends IConveyorBelt> clazz = REPLACEMENT_CLASSES.get(path);

        if (clazz != null) {
            try {
                IConveyorBelt freshInstance = clazz.newInstance();
                MCTMixin.LOGGER.debug("IT created fresh conveyor instance: {}", key);
                cir.setReturnValue(freshInstance);
            } catch (Exception e) {
                MCTMixin.LOGGER.error("Failed to instantiate conveyor replacement for {}", key, e);
            }
        }
    }
}
