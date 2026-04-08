package mctmods.immersivetechnology.mixin;

import net.minecraftforge.fml.relauncher.Side;

import net.minecraft.launchwrapper.Launch;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(net.minecraftforge.fml.common.network.NetworkRegistry.class)
public class MixinMCTNetworkRegistry {
    // Only active in deobf/dev environment
    @Redirect(method = "newChannel(Ljava/lang/String;[Lio/netty/channel/ChannelHandler;)Ljava/util/EnumMap;", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/relauncher/Side;values()[Lnet/minecraftforge/fml/relauncher/Side;"), remap = false)
    private Side[] redirectNewChannel1() {
        Object isDeobf = Launch.blackboard.get("fml.deobfuscatedEnvironment");
        boolean applyFix = (isDeobf instanceof Boolean && (Boolean) isDeobf);
        if (applyFix) { return new Side[]{Side.CLIENT, Side.SERVER}; }
        return Side.values();
    }

    @Redirect(method = "newChannel(Lnet/minecraftforge/fml/common/ModContainer;Ljava/lang/String;[Lio/netty/channel/ChannelHandler;)Ljava/util/EnumMap;", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/relauncher/Side;values()[Lnet/minecraftforge/fml/relauncher/Side;"), remap = false)
    private Side[] redirectNewChannel2() {
        Object isDeobf = Launch.blackboard.get("fml.deobfuscatedEnvironment");
        boolean applyFix = (isDeobf instanceof Boolean && (Boolean) isDeobf);
        if (applyFix) { return new Side[]{Side.CLIENT, Side.SERVER}; }
        return Side.values();
    }
}
