package cc.ioctl.tmoe.startup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import com.tencent.mmkv.MMKV;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import cc.ioctl.tmoe.BuildConfig;
import de.robv.android.xposed.XposedBridge;

public class NativeLoader {

    private static final String LIB_NAME = "tmoe";
    private static final String SO_NAME = "lib" + LIB_NAME + ".so";

    private static void registerNativeLibEntry(String soTailingName) {
        if (soTailingName == null || soTailingName.length() == 0) {
            return;
        }
        try {
            Class<?> xp = Class.forName("de.robv.android.xposed.XposedBridge");
            try {
                xp.getClassLoader()
                        .loadClass("org.lsposed.lspd.nativebridge.NativeAPI")
                        .getMethod("recordNativeEntrypoint", String.class)
                        .invoke(null, soTailingName);
            } catch (ClassNotFoundException ignored) {
                // not LSPosed, ignore
            } catch (NoSuchMethodException | IllegalArgumentException
                    | InvocationTargetException | IllegalAccessException e) {
                XposedBridge.log(e);
            }
        } catch (ClassNotFoundException e) {
            // not in host process, ignore
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("UnsafeDynamicallyLoadedCode")
    public static void loadAllSharedLibraries(Context ctx) {
        try {
            getpagesize();
            return;
        } catch (UnsatisfiedLinkError ignored) {
        }
        try {
            Class.forName("de.robv.android.xposed.XposedBridge");
            // in host process
            try {
                String modulePath = HookEntry.getModulePath();
                // try direct memory map
                System.load(modulePath + "!/lib/" + Build.CPU_ABI + "/libmmkv.so");
                System.load(modulePath + "!/lib/" + Build.CPU_ABI + "/libtmoe.so");
            } catch (UnsatisfiedLinkError e1) {
                // direct memory map load failed, extract and dlopen
                File libmmkv = extractNativeLibrary(ctx, "mmkv");
                File libtmoe = extractNativeLibrary(ctx, "tmoe");
                registerNativeLibEntry(libtmoe.getName());
                System.load(libmmkv.getAbsolutePath());
                System.load(libtmoe.getAbsolutePath());
            }
        } catch (ClassNotFoundException e) {
            // not in host process, ignore
            System.loadLibrary("mmkv");
            System.loadLibrary("tmoe");
        }
        getpagesize();
        File mmkvDir = new File(ctx.getFilesDir(), "tmoe_mmkv");
        if (!mmkvDir.exists()) {
            mmkvDir.mkdirs();
        }
        MMKV.initialize(mmkvDir.getAbsolutePath(), s -> {
            // nop, libmmkv.so should be already loaded
        });
        // Telegram only has one process, so we may use SINGLE_PROCESS_MODE
        MMKV.mmkvWithID("global_config", MMKV.SINGLE_PROCESS_MODE);
        MMKV.mmkvWithID("global_cache", MMKV.SINGLE_PROCESS_MODE);
    }

    /**
     * Extract or update native library into "tmoe_dyn_lib" dir
     *
     * @param libraryName library name without "lib" or ".so", eg. "mmkv"
     */
    private static File extractNativeLibrary(Context ctx, String libraryName) {
        String abi = Build.CPU_ABI;
        String soName = "lib" + libraryName + ".so." + BuildConfig.VERSION_CODE + "." + abi;
        File dir = new File(ctx.getFilesDir(), "tmoe_dyn_lib");
        if (!dir.isDirectory()) {
            if (dir.isFile()) {
                dir.delete();
            }
            dir.mkdir();
        }
        File soFile = new File(dir, soName);
        if (!soFile.exists()) {
            InputStream in = NativeLoader.class.getClassLoader()
                    .getResourceAsStream("lib/" + abi + "/lib" + libraryName + ".so");
            if (in == null) {
                throw new UnsatisfiedLinkError("Unsupported ABI: " + abi);
            }
            //clean up old files
            for (String name : dir.list()) {
                if (name.startsWith("lib" + libraryName + "_")
                        || name.startsWith("lib" + libraryName + ".so")) {
                    new File(dir, name).delete();
                }
            }
            try {
                // extract so file
                soFile.createNewFile();
                FileOutputStream fout = new FileOutputStream(soFile);
                byte[] buf = new byte[1024];
                int i;
                while ((i = in.read(buf)) > 0) {
                    fout.write(buf, 0, i);
                }
                in.close();
                fout.flush();
                fout.close();
            } catch (IOException ioe) {
                // this should rarely happen
                throw new RuntimeException(ioe);
            }
        }
        return soFile;
    }

    public static boolean isNativeLibraryLoaded() {
        try {
            getpagesize();
            return true;
        } catch (UnsatisfiedLinkError ignored) {
            return false;
        }
    }

    /**
     * For test purpose only
     *
     * @return the page size of the device
     */
    private static native int getpagesize();
}
