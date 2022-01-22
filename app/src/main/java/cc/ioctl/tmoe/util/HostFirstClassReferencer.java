package cc.ioctl.tmoe.util;

import android.content.Context;

public class HostFirstClassReferencer extends ClassLoader {

    private static final ClassLoader BOOTSTRAP = Context.class.getClassLoader();
    private final ClassLoader mHostReferencer;

    public HostFirstClassReferencer() {
        super(BOOTSTRAP);
        mHostReferencer = Initiator.getHostClassLoader();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            assert BOOTSTRAP != null;
            return BOOTSTRAP.loadClass(name);
        } catch (ClassNotFoundException ignored) {
        }
        // try host class loader first
        try {
            return mHostReferencer.loadClass(name);
        } catch (ClassNotFoundException cnfe) {
            // try plugin class loader
            try {
                ClassLoader pluginClassLoader = HostFirstClassReferencer.class.getClassLoader();
                assert pluginClassLoader != null;
                return pluginClassLoader.loadClass(name);
            } catch (ClassNotFoundException ignored) {
            }
            // class not found
            throw cnfe;
        }
    }
}
