package mctmods.immersivetechnology.mixin;

import mctmods.immersivetechnology.core.MCTMixinConfig;

import net.minecraftforge.fml.relauncher.Side;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(net.minecraftforge.fml.common.network.NetworkRegistry.class)
public class MixinMCTNetworkRegistry {
    @Redirect(method = "newChannel(Ljava/lang/String;[Lio/netty/channel/ChannelHandler;)Ljava/util/EnumMap;", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/relauncher/Side;values()[Lnet/minecraftforge/fml/relauncher/Side;"), remap = false)
    private Side[] redirectNewChannel1() { return MCTMixinConfig.mixinSettings.enableDevSided ? new Side[]{Side.CLIENT, Side.SERVER} : Side.values(); }

    @Redirect(method = "newChannel(Lnet/minecraftforge/fml/common/ModContainer;Ljava/lang/String;[Lio/netty/channel/ChannelHandler;)Ljava/util/EnumMap;", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/relauncher/Side;values()[Lnet/minecraftforge/fml/relauncher/Side;"), remap = false)
    private Side[] redirectNewChannel2() { return MCTMixinConfig.mixinSettings.enableDevSided ? new Side[]{Side.CLIENT, Side.SERVER} : Side.values(); }
}
