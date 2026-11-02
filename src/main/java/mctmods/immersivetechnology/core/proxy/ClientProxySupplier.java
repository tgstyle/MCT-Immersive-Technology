package mctmods.immersivetechnology.core.proxy;

import net.neoforged.fml.loading.FMLLoader;

public class ClientProxySupplier {
    public static CommonProxy forDist() { return FMLLoader.getDist().isClient() ? get() : new CommonProxy(); }

    public static CommonProxy get() {
        try {
            return (CommonProxy) Class.forName("mctmods.immersivetechnology.core.proxy.ClientProxy").getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
