package mctmods.immersivetechnology.core.proxy;

public class ClientProxySupplier {
    public static CommonProxy get() {
        try {
            return (CommonProxy) Class.forName("mctmods.immersivetechnology.core.proxy.ClientProxy").getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
