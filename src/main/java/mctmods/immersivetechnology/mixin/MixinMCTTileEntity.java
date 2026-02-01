package mctmods.immersivetechnology.mixin;

import mctmods.immersivetechnology.core.MCTMixinConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mixin(TileEntity.class)
public abstract class MixinMCTTileEntity {

    /**
     * @author tgstyle
     * @reason Redirect error logging to stderr for debugging early loading issues
     */
    @SuppressWarnings("all") @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private static void create(World p_190200_0_, NBTTagCompound p_190200_1_, CallbackInfoReturnable<TileEntity> cir) {
        TileEntity tileentity = null;
        String s = p_190200_1_.getString("id");
        Class<? extends TileEntity> oclass = null;
        Logger LOGGER = null;

        try {
            Field regField = TileEntity.class.getDeclaredField("REGISTRY");
            regField.setAccessible(true);
            Object regObj = regField.get(null);
            @SuppressWarnings("unchecked")
            RegistryNamespaced<ResourceLocation, Class<? extends TileEntity>> REGISTRY = (RegistryNamespaced<ResourceLocation, Class<? extends TileEntity>>) regObj;

            Field logField = TileEntity.class.getDeclaredField("LOGGER");
            logField.setAccessible(true);
            LOGGER = (Logger) logField.get(null);

            oclass = REGISTRY.getObject(new ResourceLocation(s));
            if (oclass != null) {
                tileentity = oclass.newInstance();
            }
        } catch (Throwable throwable1) {
            if (MCTMixinConfig.mixinSettings.enableErrorLoggingRedirect) {
                System.err.printf("Failed to create block entity %s\n", s);
                throwable1.printStackTrace(System.err);
                System.err.printf("A TileEntity %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author\n", s, oclass == null ? null : oclass.getName());
                throwable1.printStackTrace(System.err);
            } else {
                assert LOGGER != null;
                LOGGER.error("Failed to create block entity {}", s, throwable1);
                FMLLog.log.error("A TileEntity {}({}) has thrown an exception during loading, its state cannot be restored. Report this to the mod author", s, oclass == null ? null : oclass.getName(), throwable1);
            }
        }

        if (tileentity != null) {
            Throwable loadThrowable = null;
            try {
                Method setWorldCreate = TileEntity.class.getDeclaredMethod("setWorldCreate", World.class);
                setWorldCreate.setAccessible(true);
                setWorldCreate.invoke(tileentity, p_190200_0_);
                tileentity.readFromNBT(p_190200_1_);
            } catch (Throwable throwable) {
                loadThrowable = throwable;
            }
            if (loadThrowable != null) {
                if (MCTMixinConfig.mixinSettings.enableErrorLoggingRedirect) {
                    System.err.printf("Failed to load data for block entity %s\n", s);
                    loadThrowable.printStackTrace(System.err);
                    System.err.printf("A TileEntity %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author\n", s, oclass.getName());
                    loadThrowable.printStackTrace(System.err);
                } else {
                    LOGGER.error("Failed to load data for block entity {}", s, loadThrowable);
                    FMLLog.log.error("A TileEntity {}({}) has thrown an exception during loading, its state cannot be restored. Report this to the mod author", s, oclass.getName(), loadThrowable);
                }
                tileentity = null;
            }
        } else {
            if (LOGGER != null) LOGGER.warn("Skipping BlockEntity with id {}", s);
        }
        cir.setReturnValue(tileentity);
    }
}
