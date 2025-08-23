package mctmods.immersivetechnology.mixin;

import net.minecraftforge.fml.relauncher.Side;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(net.minecraftforge.fml.common.network.NetworkRegistry.class)
public class MixinNetworkRegistry {
    @Redirect(method = "newChannel(Ljava/lang/String;[Lio/netty/channel/ChannelHandler;)Ljava/util/EnumMap;", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/relauncher/Side;values()[Lnet/minecraftforge/fml/relauncher/Side;"), remap = false)
    private Side[] redirectNewChannel1() { return new Side[]{Side.CLIENT, Side.SERVER}; }

    @Redirect(method = "newChannel(Lnet/minecraftforge/fml/common/ModContainer;Ljava/lang/String;[Lio/netty/channel/ChannelHandler;)Ljava/util/EnumMap;", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/relauncher/Side;values()[Lnet/minecraftforge/fml/relauncher/Side;"), remap = false)
    private Side[] redirectNewChannel2() { return new Side[]{Side.CLIENT, Side.SERVER}; }
}
