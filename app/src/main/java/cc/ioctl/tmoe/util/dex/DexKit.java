package cc.ioctl.tmoe.util.dex;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;

import cc.ioctl.tmoe.util.HostInfo;
import cc.ioctl.tmoe.util.Initiator;
import cc.ioctl.tmoe.util.Reflex;
import cc.ioctl.tmoe.util.Utils;
import cc.ioctl.tmoe.util.config.ConfigManager;
import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * I hadn't obfuscated the source code. I just don't want to name it, leaving it a()
 */
public class DexKit {

    static final String NO_SUCH_CLASS = "Lcc/ioctl/tmoe/util/dex/DexKit$NoSuchClass;";
    static final DexMethodDescriptor NO_SUCH_METHOD = new DexMethodDescriptor(NO_SUCH_CLASS, "a", "()V");

    // WARN: NEVER change the index!
    public static final int C_THEME = 1;
    // the last index
    public static final int DEOBF_NUM_C = 1;

    public static final int DEOBF_NUM_N = 0;

    /**
     * Run the dex deobfuscation.
     *
     * @param i the dex class index
     * @return true if the dex class is deobfuscated successfully.
     */
    public static boolean prepareFor(int i) {
        if (i / 10000 == 0) {
            return doFindClass(i) != null;
        } else {
            return doFindMethod(i) != null;
        }
    }

    /**
     * Test whether we should run the dex deobfuscation. Note that if a dex class is tried to deobfuscate before, but
     * failed, its failed result will be cached, which means that the same dex class will not be deobfuscated again.
     *
     * @param i the dex class index
     * @return true if the dex class is deobfuscated and cached(regardless of success or failure) or not.
     */
    public static boolean checkFor(int i) {
        if (i / 10000 == 0) {
            if (loadClassFromCache(i) != null) {
                return true;
            }
            DexMethodDescriptor desc = getMethodDescFromCache(i);
            return desc != null && NO_SUCH_CLASS.equals(desc.declaringClass);
        } else {
            DexMethodDescriptor desc = getMethodDescFromCache(i);
            return desc != null && NO_SUCH_CLASS.equals(desc.declaringClass);
        }
    }

    /**
     * Try to load the obfuscated class from deobfuscation cache. This method does not take much time and may be called
     * in main thread.
     *
     * @param i the dex class index
     * @return null if the dex class is not in deobfuscation cache, otherwise the target class object.
     */
    @Nullable
    public static Class<?> loadClassFromCache(int i) {
        Class<?> ret = Initiator.load(getOriginalClassNameForIndex(i));
        if (ret != null) {
            return ret;
        }
        DexMethodDescriptor m = getMethodDescFromCache(i);
        if (m == null) {
            return null;
        }
        return Initiator.load(m.declaringClass);
    }

    /**
     * Run the dex deobfuscation. This method may take a long time and should only be called in background thread.
     *
     * @param i the dex class index
     * @return the target class object, null if the dex class is not found.
     */
    @Nullable
    public static Class<?> doFindClass(int i) {
        Class<?> ret = Initiator.load(getOriginalClassNameForIndex(i));
        if (ret != null) {
            return ret;
        }
        DexMethodDescriptor m = getMethodDescFromCache(i);
        if (m == null) {
            m = doFindMethodDesc(i);
        }
        if (m == null) {
            return null;
        }
        return Initiator.load(m.declaringClass);
    }

    /**
     * Try to load the obfuscated method from deobfuscation cache. This method does not take much time and may be called
     * in main thread.
     *
     * @param i the dex method index
     * @return the target method descriptor, null if the target is not found.
     */
    @Nullable
    public static Method getMethodFromCache(int i) {
        if (i / 10000 == 0) {
            throw new IllegalStateException("Index " + i + " attempted to access method!");
        }
        DexMethodDescriptor m = getMethodDescFromCache(i);
        if (m == null) {
            return null;
        }
        if (m.name.equals("<init>") || m.name.equals("<clinit>")) {
            // TODO: support constructors
            Utils.logi("getMethodFromCache(" + i + ") methodName == " + m.name + " , return null");
            return null;
        }
        try {
            return m.getMethodInstance(Initiator.getHostClassLoader());
        } catch (NoSuchMethodException e) {
            Utils.loge(e);
            return null;
        }
    }

    /**
     * Run the dex deobfuscation. This method may take a long time and should only be called in background thread.
     *
     * @param i the dex method index
     * @return target method descriptor, null if the target is not found.
     */
    @Nullable
    public static Method doFindMethod(int i) {
        if (i / 10000 == 0) {
            throw new IllegalStateException("Index " + i + " attempted to access method!");
        }
        DexMethodDescriptor m = doFindMethodDesc(i);
        if (m == null) {
            return null;
        }
        if (m.name.equals("<init>") || m.name.equals("<clinit>")) {
            Utils.logi("doFindMethod(" + i + ") methodName == " + m.name + " , return null");
            return null;
        }
        try {
            return m.getMethodInstance(Initiator.getHostClassLoader());
        } catch (NoSuchMethodException e) {
            Utils.loge(e);
            return null;
        }
    }

    /**
     * Try to load the obfuscated method from deobfuscation cache. This method does not take much time and may be called
     * in main thread.
     *
     * @param i the dex method index
     * @return the target method descriptor, null if the target is not in deobfuscation cache.
     */
    @Nullable
    public static DexMethodDescriptor getMethodDescFromCache(int i) {
        try {
            ConfigManager cache = ConfigManager.getCache();
            int currentHostVersionCode32 = HostInfo.getVersionCode();
            String memoirKey = getOriginalClassNameForIndex(i);
            int lastVersion = cache.getIntOrDefault("cache_" + memoirKey + "_code", 0);
            if (currentHostVersionCode32 != lastVersion) {
                return null;
            }
            String name = cache.getString("cache_" + memoirKey + "_method");
            if (name != null && name.length() > 0) {
                return new DexMethodDescriptor(name);
            }
            return null;
        } catch (Exception e) {
            Utils.loge(e);
            return null;
        }
    }

    /**
     * Run the dex deobfuscation. This method may take a long time and should only be called in background thread. Note
     * that if a method is not found, its failed state will be cached.
     *
     * @param i the dex method index
     * @return the target method descriptor, null if the target is not found.
     * @see #checkFor(int)
     */
    @Nullable
    public static DexMethodDescriptor doFindMethodDesc(int i) {
        DexMethodDescriptor ret = getMethodDescFromCache(i);
        if (ret != null) {
            return ret;
        }
        int currentHostVersionCode32 = HostInfo.getVersionCode();
        try {
            HashSet<DexMethodDescriptor> methods;
            String memoirKey = getOriginalClassNameForIndex(i);
            ConfigManager cache = ConfigManager.getCache();
            DexDeobfReport report = new DexDeobfReport();
            report.target = i;
            report.version = currentHostVersionCode32;
            methods = searchFromHostDexForIndex(i, report);
            if (methods == null || methods.size() == 0) {
                report.v("No method candidate found.");
                Utils.logi("Unable to deobf: " + getOriginalClassNameForIndex(i));
                // save failed state
                cache.putString("cache_" + memoirKey + "_method", NO_SUCH_METHOD.toString());
                cache.putInt("cache_" + memoirKey + "_code", currentHostVersionCode32);
                cache.save();
                return null;
            }
            report.v(methods.size() + " method(s) found: " + methods);
            if (methods.size() == 1) {
                ret = methods.iterator().next();
            } else {
                ret = verifyFoundCandidateForIndex(i, methods, report);
            }
            report.v("Final decision:" + (ret == null ? null : ret.toString()));
            cache.putString("deobf_log_" + memoirKey, report.toString());
            if (ret == null) {
                Utils.logi("Multiple classes candidates found, none satisfactory.");
                // save failed state
                cache.putString("cache_" + memoirKey + "_method", NO_SUCH_METHOD.toString());
                cache.putInt("cache_" + memoirKey + "_code", currentHostVersionCode32);
                cache.save();
                return null;
            }
            cache.putString("cache_" + memoirKey + "_method", ret.toString());
            cache.putInt("cache_" + memoirKey + "_code", currentHostVersionCode32);
            cache.save();
        } catch (Exception e) {
            Utils.loge(e);
        }
        return ret;
    }

    /**
     * Get the original class name for a class index.
     *
     * @param i The class index.
     * @return The original class name.
     */
    private static String getOriginalClassNameForIndex(int i) {
        String ret;
        switch (i) {
            case C_THEME:
                ret = "org.telegram.ui.ActionBar.Theme";
                break;
            default:
                ret = null;
        }
        if (ret != null) {
            return ret.replace("/", ".");
        }
        throw new IndexOutOfBoundsException("No class index for " + i + ", max = " + DEOBF_NUM_C);
    }

    /**
     * Get the keywords of the obfuscated class.
     *
     * @param i The class index.
     * @return The keywords of the class index.
     */
    private static byte[][] getByteCodeTagForIndex(int i) {
        switch (i) {
            case C_THEME:
                return new byte[][]{
                        new byte[]{0x19, 0x6C, 0x69, 0x67, 0x68, 0x74, 0x20, 0x73, 0x65, 0x6E, 0x73, 0x6F,
                                0x72, 0x20, 0x75, 0x6E, 0x72, 0x65}};
        }
        throw new IndexOutOfBoundsException("No class index for " + i + ", max = " + DEOBF_NUM_C);
    }

    /**
     * Get the dex index where the target dex class belongs to. Note that this dex index is only used as a hint.
     *
     * @param i the dex index
     * @return the dex indexes where the target class belongs to
     */
    private static int[] getDexIndexForIndex(int i) {
        switch (i) {
            case C_THEME:
                return new int[]{1};
        }
        throw new IndexOutOfBoundsException("No class index for " + i + ",max = " + DEOBF_NUM_C);
    }

    private static DexMethodDescriptor verifyFoundCandidateForIndex(int i, HashSet<DexMethodDescriptor> candidateDexMethods,
                                                                    DexDeobfReport report) {
        switch (i) {
            case C_THEME: {
                if (candidateDexMethods.size() != 1) {
                    report.v("C_THEME: found more than one method, aborting");
                    return null;
                } else {
                    return candidateDexMethods.iterator().next();
                }
            }
        }
        return null;
    }

    /**
     * What is the method originally designed to do?
     * I have already forgotten what it does.
     *
     * @param i the index of the target class or method
     * @return keep it return false if it works, otherwise return true
     */
    private static boolean shouldCheckFoundResult(int i) {
        return false;
    }

    @Nullable
    private static HashSet<DexMethodDescriptor> searchFromHostDexForIndex(int i, DexDeobfReport rep) {
        ClassLoader loader = Initiator.getHostClassLoader();
        int record = 0;
        int[] qf = getDexIndexForIndex(i);
        byte[][] keys = getByteCodeTagForIndex(i);
        boolean check = shouldCheckFoundResult(i);
        if (qf != null) {
            for (int dexi : qf) {
                record |= 1 << dexi;
                try {
                    for (byte[] k : keys) {
                        HashSet<DexMethodDescriptor> ret = findMethodsByConstString(k, dexi,
                                loader);
                        if (ret != null && ret.size() > 0) {
                            if (check) {
                                DexMethodDescriptor m = verifyFoundCandidateForIndex(i, ret, rep);
                                if (m != null) {
                                    ret.clear();
                                    ret.add(m);
                                    return ret;
                                }
                            } else {
                                return ret;
                            }
                        }
                    }
                } catch (FileNotFoundException ignored) {
                }
            }
        }
        int dexi = 1;
        while (true) {
            if ((record & (1 << dexi)) != 0) {
                dexi++;
                continue;
            }
            try {
                for (byte[] k : keys) {
                    HashSet<DexMethodDescriptor> ret = findMethodsByConstString(k, dexi, loader);
                    if (ret != null && ret.size() > 0) {
                        if (check) {
                            DexMethodDescriptor m = verifyFoundCandidateForIndex(i, ret, rep);
                            if (m != null) {
                                ret.clear();
                                ret.add(m);
                                return ret;
                            }
                        } else {
                            return ret;
                        }
                    }
                }
            } catch (FileNotFoundException ignored) {
                return null;
            }
            dexi++;
        }
    }

    @Nullable
    public static byte[] getClassDeclaringDex(String klass, @Nullable int[] qf) {
        ClassLoader loader = Initiator.getHostClassLoader();
        int record = 0;
        if (qf != null) {
            for (int dexi : qf) {
                record |= 1 << dexi;
                try {
                    String name;
                    byte[] buf = new byte[4096];
                    byte[] content;
                    if (dexi == 1) {
                        name = "classes.dex";
                    } else {
                        name = "classes" + dexi + ".dex";
                    }
                    HashSet<URL> urls = new HashSet<>(3);
                    try {
                        Enumeration<URL> eu;
                        eu = (Enumeration<URL>) Reflex.invokeVirtual(loader, "findResources", name, String.class);
                        if (eu != null) {
                            while (eu.hasMoreElements()) {
                                urls.add(eu.nextElement());
                            }
                        }
                    } catch (Throwable e) {
                        Utils.loge(e);
                    }
                    if (!loader.getClass().equals(PathClassLoader.class) && !loader.getClass()
                            .equals(DexClassLoader.class)
                            && loader.getParent() != null) {
                        try {
                            Enumeration<URL> eu;
                            eu = (Enumeration<URL>) Reflex.invokeVirtual(loader.getParent(), "findResources", name, String.class);
                            if (eu != null) {
                                while (eu.hasMoreElements()) {
                                    urls.add(eu.nextElement());
                                }
                            }
                        } catch (Throwable e) {
                            Utils.loge(e);
                        }
                    }
                    if (urls.size() == 0) {
                        throw new FileNotFoundException(name);
                    }
                    InputStream in;
                    try {
                        for (URL url : urls) {
                            in = url.openStream();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            int ii;
                            while ((ii = in.read(buf)) != -1) {
                                baos.write(buf, 0, ii);
                            }
                            in.close();
                            content = baos.toByteArray();
                            if (DexFlow.hasClassInDex(content, klass)) {
                                return content;
                            }
                        }
                    } catch (IOException e) {
                        Utils.loge(e);
                        return null;
                    }
                } catch (FileNotFoundException ignored) {
                }
            }
        }
        int dexi = 1;
        while (true) {
            if ((record & (1 << dexi)) != 0) {
                dexi++;
                continue;
            }
            try {
                String name;
                byte[] buf = new byte[4096];
                byte[] content;
                if (dexi == 1) {
                    name = "classes.dex";
                } else {
                    name = "classes" + dexi + ".dex";
                }
                HashSet<URL> urls = new HashSet<>(3);
                try {
                    Enumeration<URL> eu;
                    eu = (Enumeration<URL>) Reflex.invokeVirtual(loader, "findResources", name,
                            String.class);
                    if (eu != null) {
                        while (eu.hasMoreElements()) {
                            urls.add(eu.nextElement());
                        }
                    }
                } catch (Throwable e) {
                    Utils.loge(e);
                }
                if (!loader.getClass().equals(PathClassLoader.class) && !loader.getClass()
                        .equals(DexClassLoader.class)
                        && loader.getParent() != null) {
                    try {
                        Enumeration<URL> eu;
                        eu = (Enumeration<URL>) Reflex.invokeVirtual(loader.getParent(), "findResources", name, String.class);
                        if (eu != null) {
                            while (eu.hasMoreElements()) {
                                urls.add(eu.nextElement());
                            }
                        }
                    } catch (Throwable e) {
                        Utils.loge(e);
                    }
                }
                if (urls.size() == 0) {
                    throw new FileNotFoundException(name);
                }
                InputStream in;
                try {
                    for (URL url : urls) {
                        in = url.openStream();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        int ii;
                        while ((ii = in.read(buf)) != -1) {
                            baos.write(buf, 0, ii);
                        }
                        in.close();
                        content = baos.toByteArray();
                        if (DexFlow.hasClassInDex(content, klass)) {
                            return content;
                        }
                    }
                } catch (IOException e) {
                    Utils.loge(e);
                    return null;
                }
            } catch (FileNotFoundException ignored) {
                return null;
            }
            dexi++;
        }
    }

    private static ArrayList<Integer> searchForKeyByteCodeTag(byte[] buf, byte[] target) {
        ArrayList<Integer> rets = new ArrayList<>();
        int[] ret = new int[1];
        ret[0] = DexFlow.arrayIndexOf(buf, target, 0, buf.length);
        ret[0] = DexFlow.arrayIndexOf(buf, DexFlow.int2u4le(ret[0]), 0, buf.length);
        int strIdx = (ret[0] - DexFlow.readLe32(buf, 0x3c)) / 4;
        if (strIdx > 0xFFFF) {
            target = DexFlow.int2u4le(strIdx);
        } else {
            target = DexFlow.int2u2le(strIdx);
        }
        int off = 0;
        while (true) {
            off = DexFlow.arrayIndexOf(buf, target, off + 1, buf.length);
            if (off == -1) {
                break;
            }
            if (buf[off - 2] == (byte) 26/*Opcodes.OP_CONST_STRING*/
                    || buf[off - 2] == (byte) 27)/* Opcodes.OP_CONST_STRING_JUMBO*/ {
                ret[0] = off - 2;
                int opcodeOffset = ret[0];
                if (buf[off - 2] == (byte) 27 && strIdx < 0x10000) {
                    if (DexFlow.readLe32(buf, opcodeOffset + 2) != strIdx) {
                        continue;
                    }
                }
                rets.add(opcodeOffset);
            }
        }
        return rets;
    }

    /**
     * get ALL the possible class names
     *
     * @param key    the pattern
     * @param i      C_XXXX
     * @param loader to get dex file
     * @return ["abc","ab"]
     * @throws FileNotFoundException apk has no classesN.dex
     */
    private static HashSet<DexMethodDescriptor> findMethodsByConstString(byte[] key, int i,
                                                                         ClassLoader loader) throws FileNotFoundException {
        String name;
        byte[] buf = new byte[4096];
        byte[] content;
        if (i == 1) {
            name = "classes.dex";
        } else {
            name = "classes" + i + ".dex";
        }
        HashSet<URL> urls = new HashSet<>(3);
        try {
            Enumeration<URL> eu;
            eu = (Enumeration<URL>) Reflex.invokeVirtual(loader, "findResources", name, String.class);
            if (eu != null) {
                while (eu.hasMoreElements()) {
                    urls.add(eu.nextElement());
                }
            }
        } catch (Throwable e) {
            Utils.loge(e);
        }
        if (!loader.getClass().equals(PathClassLoader.class) && !loader.getClass()
                .equals(DexClassLoader.class)
                && loader.getParent() != null) {
            try {
                Enumeration<URL> eu;
                eu = (Enumeration<URL>) Reflex.invokeVirtual(loader.getParent(), "findResources", name, String.class);
                if (eu != null) {
                    while (eu.hasMoreElements()) {
                        urls.add(eu.nextElement());
                    }
                }
            } catch (Throwable e) {
                Utils.loge(e);
            }
        }
        //log("dex" + i + ":" + url);
        if (urls.size() == 0) {
            throw new FileNotFoundException(name);
        }
        InputStream in;
        try {
            HashSet<DexMethodDescriptor> rets = new HashSet<>();
            for (URL url : urls) {
                in = url.openStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int ii;
                while ((ii = in.read(buf)) != -1) {
                    baos.write(buf, 0, ii);
                }
                in.close();
                content = baos.toByteArray();
                ArrayList<Integer> opcodeOffsets = searchForKeyByteCodeTag(content, key);
                for (int j = 0; j < opcodeOffsets.size(); j++) {
                    try {
                        DexMethodDescriptor desc = DexFlow.getDexMethodByOpOffset(content, opcodeOffsets.get(j), true);
                        if (desc != null) {
                            rets.add(desc);
                        }
                    } catch (InternalError ignored) {
                    }
                }
            }
            return rets;
        } catch (IOException e) {
            Utils.loge(e);
            return null;
        }
    }

    public static class DexDeobfReport {
        int target;
        int version;
        String result;
        String log;
        long time;

        public DexDeobfReport() {
            time = System.currentTimeMillis();
        }

        public void v(String str) {
            if (log == null) {
                log = str + "\n";
            } else {
                log = log + str + "\n";
            }
        }

        @NonNull
        @Override
        public String toString() {
            return "Deobf target: " + target + '\n' +
                    "Time: " + time + '\n' +
                    "Version code: " + version + '\n' +
                    "Result: " + result + '\n' + log;
        }
    }
}
