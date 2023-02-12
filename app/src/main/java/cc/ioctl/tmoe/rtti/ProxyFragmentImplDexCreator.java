package cc.ioctl.tmoe.rtti;

import android.content.Context;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableDexFile;
import org.jf.dexlib2.immutable.ImmutableField;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction10x;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction11x;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21c;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction22c;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction35c;
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableTypeReference;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import cc.ioctl.tmoe.BuildConfig;
import cc.ioctl.tmoe.util.dex.DexFieldDescriptor;
import cc.ioctl.tmoe.util.dex.DexMethodDescriptor;
import cc.ioctl.tmoe.util.HostInfo;

public class ProxyFragmentImplDexCreator {

    private static final int ACC_CONSTRUCTOR = 0x00010000;
    private static final String PROXY_FRAGMENT_IMPL_CLASS_NAME = "cc.ioctl.tmoe.dynamic.ProxyFragmentImpl";
    private static final String PROXY_FRAGMENT_IMPL_TYPE = "L" + PROXY_FRAGMENT_IMPL_CLASS_NAME.replace(".", "/") + ";";
    private static final String RTTI_HANDLER_TYPE = "L" + ProxyFragmentRttiHandler.class.getName().replace(".", "/") + ";";
    private static final String OBJECT_TYPE = "Ljava/lang/Object;";
    private static final String VIEW_GROUP_TYPE = "Landroid/view/ViewGroup;";

    private ProxyFragmentImplDexCreator() {
        throw new AssertionError("no instance for you!");
    }

    public static File createProxyFragmentImplDex(Class<?> baseFragmentClass, Class<?> actionBar, Class<?> actionBarLayout)
            throws ReflectiveOperationException, IOException {
        String sigBaseFragmentClass = DexFieldDescriptor.getTypeSig(baseFragmentClass);
        String sigActionBar = DexFieldDescriptor.getTypeSig(actionBar);
        String sigActionBarLayout = DexFieldDescriptor.getTypeSig(actionBarLayout);
        String hostAppVersionName = HostInfo.getVersionName();
        String pluginVersionName = BuildConfig.VERSION_NAME;
        String hashCode = String.valueOf(Objects.hash(baseFragmentClass, actionBar, actionBarLayout,
                hostAppVersionName, pluginVersionName, HostInfo.getVersionCode(), BuildConfig.VERSION_CODE, BuildConfig.BUILD_UUID));
        String dexName = "ProxyFragmentImpl_" + hostAppVersionName + "_" + pluginVersionName + "_" + hashCode + ".dex";
        Context ctx = HostInfo.getApplication();
        File dexTmpDir = new File(ctx.getFilesDir(), "tmoe_dyn_dex");
        if (!dexTmpDir.exists()) {
            dexTmpDir.mkdirs();
        }
        // remove old dex if exists
        File[] oldDexFileNames = dexTmpDir.listFiles();
        if (oldDexFileNames != null) {
            for (File f : oldDexFileNames) {
                if ((f.getName().startsWith("ProxyFragmentImpl_") && f.getName().endsWith(".dex"))
                        && !f.getName().equals(dexName)) {
                    f.delete();
                }
            }
        }
        // check if dex file exists
        File dexFile = new File(dexTmpDir, dexName);
        if (dexFile.exists() && dexFile.length() > 0) {
            return dexFile;
        }
        // create dex file if not exists
        // find all exported methods in ProxyFragmentRttiHandler
        ArrayList<DexMethodDescriptor> exportedMethods = new ArrayList<>();
        for (Method m : ProxyFragmentRttiHandler.class.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers())
                    && m.getName().endsWith("$dispatcher")) {
                String originalMethodName = m.getName().substring(0, m.getName().length() - "$dispatcher".length());
                DexMethodDescriptor md = new DexMethodDescriptor(m);
                exportedMethods.add(new DexMethodDescriptor(md.declaringClass, originalMethodName, md.signature));
            }
        }
        // find all imported methods in IProxyFragmentObject
        ArrayList<DexMethodDescriptor> importedSuperMethods = new ArrayList<>();
        ArrayList<DexMethodDescriptor> missingImportedSuperMethods = new ArrayList<>();
        for (Method m : IProxyFragmentObject.class.getDeclaredMethods()) {
            if (m.getName().endsWith("$super")) {
                boolean found = false;
                int argCount = m.getParameterTypes().length;
                // find corresponding method in host BaseFragment class
                String originalMethodName = m.getName().substring(0, m.getName().length() - "$super".length());
                for (Method hostMethod : baseFragmentClass.getDeclaredMethods()) {
                    if (hostMethod.getName().equals(originalMethodName) && hostMethod.getParameterTypes().length == argCount) {
                        // use original method name
                        importedSuperMethods.add(new DexMethodDescriptor(hostMethod));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    missingImportedSuperMethods.add(new DexMethodDescriptor(m));
                }
            }
        }
        if (missingImportedSuperMethods.size() > 0) {
            throw new RuntimeException("Missing imported methods in host " + baseFragmentClass
                    + ": " + Arrays.toString(missingImportedSuperMethods.toArray()));
        }
        String sigBaseFragment = DexFieldDescriptor.getTypeSig(baseFragmentClass);
        // create super method accessor
        ArrayList<ImmutableMethod> superMethodAccessors = new ArrayList<>();
        for (DexMethodDescriptor md : importedSuperMethods) {
            ImmutableMethod method = generateProxySuperStubMethod(sigBaseFragment, md, actionBar, actionBarLayout);
            superMethodAccessors.add(method);
        }
        // create proxy method accessor
        ArrayList<ImmutableMethod> proxyMethodAccessors = new ArrayList<>();
        for (DexMethodDescriptor md : exportedMethods) {
            ImmutableMethod method = generateProxyDispatcherMethod(sigBaseFragment, md, actionBar, actionBarLayout);
            proxyMethodAccessors.add(method);
        }
        ArrayList<ImmutableMethod> proxyConstructors = generateProxyClassConstructor(sigBaseFragment);
        ArrayList<org.jf.dexlib2.iface.Method> declaredMethods = new ArrayList<>();
        declaredMethods.addAll(superMethodAccessors);
        declaredMethods.addAll(proxyMethodAccessors);
        declaredMethods.addAll(proxyConstructors);
        // proxy class definition
        // package cc.ioctl.tmoe.dynamic;
        // public class ProxyFragmentImpl extends BaseFragment implements IProxyFragmentObject {
        //     private final cc.ioctl.tmoe.rtti.ProxyFragmentRttiHandler h;
        //     public ProxyFragmentImpl(ProxyFragmentRttiHandler handler, Bundle args) {
        //         super(args);
        //         this.h = handler;
        //     }
        // }
        // create proxy class
        ImmutableField handlerField = new ImmutableField(PROXY_FRAGMENT_IMPL_TYPE, "h",
                DexFieldDescriptor.getTypeSig(ProxyFragmentRttiHandler.class), Modifier.PRIVATE, null, null, null);
        ImmutableClassDef proxyFragmentImplClassDef = new ImmutableClassDef(PROXY_FRAGMENT_IMPL_TYPE, Modifier.PUBLIC,
                sigBaseFragment, Collections.singletonList(DexMethodDescriptor.getTypeSig(IProxyFragmentObject.class)),
                "ProxyFragmentImpl.dexlib2", null, Collections.singletonList(handlerField), declaredMethods);
        // create proxy dex
        ImmutableDexFile proxyDex = new ImmutableDexFile(Opcodes.forDexVersion(35), Collections.singletonList(proxyFragmentImplClassDef));
        // create proxy class file
        dexFile.createNewFile();
        DexFileFactory.writeDexFile(dexFile.getAbsolutePath(), proxyDex);
        dexFile.setReadOnly();
        return dexFile;
    }

    private static ArrayList<ImmutableMethod> generateProxyClassConstructor(String hostBaseFragmentTypeSig) {
        // smali for constructor
        // .method public constructor <init>(Lcc/ioctl/tmoe/rtti/ProxyFragmentRttiHandler;Landroid/os/Bundle;)V
        //     .registers 3
        //     .parameter "handler"
        //     .parameter "args"
        //     .prologue
        //     invoke-direct {p0, p2}, LBaseFragment;-><init>(Landroid/os/Bundle;)V
        //     iput-object p1, p0, Lcc/ioctl/tmoe/dynamic/ProxyFragmentImpl;->h:Lcc/ioctl/tmoe/rtti/ProxyFragmentRttiHandler;
        //     return-void
        // .end method
        ArrayList<ImmutableMethod> constructors = new ArrayList<>();
        ArrayList<Instruction> insCtor = new ArrayList<>();
        // invoke-direct {p0, p2}, LBaseFragment;-><init>(Landroid/os/Bundle;)V
        insCtor.add(new ImmutableInstruction35c(Opcode.INVOKE_DIRECT, 2, 0, 2, 0, 0, 0,
                referenceMethod(hostBaseFragmentTypeSig, "<init>", "(Landroid/os/Bundle;)V")));
        // iput-object p1, p0, LProxyFragmentImpl;->h:Lcc/ioctl/tmoe/rtti/ProxyFragmentRttiHandler;
        insCtor.add(new ImmutableInstruction22c(Opcode.IPUT_OBJECT, 1, 0,
                referenceField(PROXY_FRAGMENT_IMPL_TYPE, "h", RTTI_HANDLER_TYPE)));
        // return-void
        insCtor.add(new ImmutableInstruction10x(Opcode.RETURN_VOID));
        ImmutableMethodImplementation ctorMethodImpl = new ImmutableMethodImplementation(3, insCtor, null, null);
        ImmutableMethod ctorMethod = new ImmutableMethod(PROXY_FRAGMENT_IMPL_TYPE, "<init>", Arrays.asList(
                new ImmutableMethodParameter(RTTI_HANDLER_TYPE, null, "handler"),
                new ImmutableMethodParameter("Landroid/os/Bundle;", null, "args")),
                "V", Modifier.PUBLIC | ACC_CONSTRUCTOR, null, null, ctorMethodImpl);
        constructors.add(ctorMethod);
        return constructors;
    }

    private static ImmutableMethod generateProxySuperStubMethod(String hostBaseFragmentTypeSig, DexMethodDescriptor targetMethodDescriptor,
                                                                Class<?> kActionBar, Class<?> kActionBarLayout) {
        String methodName = targetMethodDescriptor.name;
        String generatedMethodName = methodName + "$super";
        List<String> parameterTypeList = targetMethodDescriptor.getParameterTypes();
        String parameterTypeSig = targetMethodDescriptor.signature;
        String returnType = targetMethodDescriptor.getReturnType();
        int parameterCount = parameterTypeList.size();
        // replace host obfuscated class name with ViewGroup or Object
        String returnTypeMitigated = returnType.replace(DexMethodDescriptor.getTypeSig(kActionBar), VIEW_GROUP_TYPE)
                .replace(DexMethodDescriptor.getTypeSig(kActionBarLayout), VIEW_GROUP_TYPE)
                .replace(hostBaseFragmentTypeSig, OBJECT_TYPE);
        String parameterTypeSigMitigated = parameterTypeSig.replace(DexMethodDescriptor.getTypeSig(kActionBar), VIEW_GROUP_TYPE)
                .replace(DexMethodDescriptor.getTypeSig(kActionBarLayout), VIEW_GROUP_TYPE)
                .replace(hostBaseFragmentTypeSig, OBJECT_TYPE);
        List<String> parameterTypeListMitigated = new ArrayList<>();
        for (String parameterType : parameterTypeList) {
            parameterTypeListMitigated.add(parameterType.replace(DexMethodDescriptor.getTypeSig(kActionBar), VIEW_GROUP_TYPE)
                    .replace(DexMethodDescriptor.getTypeSig(kActionBarLayout), VIEW_GROUP_TYPE)
                    .replace(hostBaseFragmentTypeSig, OBJECT_TYPE));
        }
        // smali for method, no local variable
        // .method public ${generatedMethodName}(${parameterTypeSigMitigated})${returnTypeMitigated}
        // .registers 5
        // .prologue
        // invoke-super {p0, p1, p2, p3}, ${hostBaseFragmentTypeSig}->${methodName}(${parameterTypeSig})${returnType}
        // #if returnType == "V"
        // return-void
        // #elseif returnType.startsWith("L") || returnType.startsWith("[")
        // move-result-object v0
        // return-object v0
        // #else
        // move-result v0
        // return v0
        // #endif
        // .end method
        List<Instruction> insSuperStub = new ArrayList<>();
        // check cast if the parameter type is mitigated, make ART verifier happy
        for (int i = 0; i < parameterCount; i++) {
            String parameterTypeMitigated = parameterTypeListMitigated.get(i);
            String parameterType = parameterTypeList.get(i);
            if (!parameterTypeMitigated.equals(parameterType)) {
                ImmutableTypeReference typeRef = new ImmutableTypeReference(parameterType);
                // p0 is this, p1 is the first parameter, p2 is the second parameter, p3 is the third parameter
                insSuperStub.add(new ImmutableInstruction21c(Opcode.CHECK_CAST, i + 1, typeRef));
            }
        }
        insSuperStub.add(new ImmutableInstruction35c(Opcode.INVOKE_SUPER, parameterCount + 1, 0, 1, 2, 3, 4,
                referenceMethod(hostBaseFragmentTypeSig, methodName, parameterTypeSig)));
        if (returnType.equals("V")) {
            insSuperStub.add(new ImmutableInstruction10x(Opcode.RETURN_VOID));
        } else if (returnType.startsWith("L") || returnType.startsWith("[")) {
            insSuperStub.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 0));
            insSuperStub.add(new ImmutableInstruction11x(Opcode.RETURN_OBJECT, 0));
        } else {
            insSuperStub.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT, 0));
            insSuperStub.add(new ImmutableInstruction11x(Opcode.RETURN, 0));
        }
        ArrayList<ImmutableMethodParameter> parameters = new ArrayList<>();
        for (int i = 0; i < parameterCount; i++) {
            parameters.add(new ImmutableMethodParameter(parameterTypeListMitigated.get(i), null, "p" + (i + 1)));
        }
        ImmutableMethodImplementation methodImpl = new ImmutableMethodImplementation(parameterCount + 1, insSuperStub, null, null);
        return new ImmutableMethod(PROXY_FRAGMENT_IMPL_TYPE, generatedMethodName, parameters,
                returnTypeMitigated, Modifier.PUBLIC, null, null, methodImpl);
    }

    private static ImmutableMethod generateProxyDispatcherMethod(String hostBaseFragmentTypeSig, DexMethodDescriptor targetMethodDescriptor,
                                                                 Class<?> kActionBar, Class<?> kActionBarLayout) {
        String methodName = targetMethodDescriptor.name;
        String dispatchMethodName = methodName + "$dispatcher";
        List<String> parameterTypeList = targetMethodDescriptor.getParameterTypes();
        String parameterTypeSig = targetMethodDescriptor.signature;
        String returnType = targetMethodDescriptor.getReturnType();
        int parameterCount = parameterTypeList.size(); // including this
        // replace host obfuscatable class name with ViewGroup or Object
        String returnTypeMitigated = returnType.replace(DexMethodDescriptor.getTypeSig(kActionBar), VIEW_GROUP_TYPE)
                .replace(DexMethodDescriptor.getTypeSig(kActionBarLayout), VIEW_GROUP_TYPE)
                .replace(hostBaseFragmentTypeSig, OBJECT_TYPE);
        String parameterTypeSigMitigated = parameterTypeSig.replace(DexMethodDescriptor.getTypeSig(kActionBar), VIEW_GROUP_TYPE)
                .replace(DexMethodDescriptor.getTypeSig(kActionBarLayout), VIEW_GROUP_TYPE)
                .replace(hostBaseFragmentTypeSig, OBJECT_TYPE);
        List<String> parameterTypeListMitigated = new ArrayList<>();
        for (String parameterType : parameterTypeList) {
            parameterTypeListMitigated.add(parameterType.replace(DexMethodDescriptor.getTypeSig(kActionBar), VIEW_GROUP_TYPE)
                    .replace(DexMethodDescriptor.getTypeSig(kActionBarLayout), VIEW_GROUP_TYPE)
                    .replace(hostBaseFragmentTypeSig, OBJECT_TYPE));
        }
        // smali for super stub method
        // .method public ${methodName}(${parameterTypeSig})${returnType}
        // .locals 1
        // .prologue
        // iget-object v0, p0, Lcc/ioctl/tmoe/dynamic/ProxyFragmentImpl;->h:Lcc/ioctl/tmoe/rtti/ProxyFragmentRttiHandler;
        // invoke-virtual {v0, p0, p1, p2}, Lcc/ioctl/tmoe/rtti/ProxyFragmentRttiHandler;->${dispatchMethodName}(${parameterTypeSigMitigated})${returnTypeMitigated}
        // #if returnTypeMitigated == "V"
        // return-void
        // #elseif returnTypeMitigated.startsWith("L") || returnTypeMitigated.startsWith("[")
        // move-result-object v0
        // return-object v0
        // #else
        // move-result v0
        // return v0
        // #endif
        // .end method
        ArrayList<ImmutableInstruction> insStub = new ArrayList<>();
        int localVarCount = 1;
        insStub.add(new ImmutableInstruction22c(Opcode.IGET_OBJECT, 0, localVarCount + 0,
                referenceField(PROXY_FRAGMENT_IMPL_TYPE, "h", RTTI_HANDLER_TYPE)));
        insStub.add(new ImmutableInstruction35c(Opcode.INVOKE_VIRTUAL, parameterCount + 1,
                0, localVarCount + 1, localVarCount + 2, localVarCount + 3, localVarCount + 4,
                referenceMethod(RTTI_HANDLER_TYPE, dispatchMethodName, parameterTypeSigMitigated)));
        if (returnTypeMitigated.equals("V")) {
            insStub.add(new ImmutableInstruction10x(Opcode.RETURN_VOID));
        } else if (returnTypeMitigated.startsWith("L") || returnTypeMitigated.startsWith("[")) {
            insStub.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 0));
            insStub.add(new ImmutableInstruction11x(Opcode.RETURN_OBJECT, 0));
            // check cast if the return type is mitigated, make ART verifier happy
            // result object is in v0
            if (!returnTypeMitigated.equals(returnType)) {
                insStub.add(new ImmutableInstruction21c(Opcode.CHECK_CAST, 0, new ImmutableTypeReference(returnType)));
            }
        } else {
            insStub.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT, 0));
            insStub.add(new ImmutableInstruction11x(Opcode.RETURN, 0));
        }
        ArrayList<ImmutableMethodParameter> parameters = new ArrayList<>();
        for (int i = 0; i < parameterCount; i++) {
            parameters.add(new ImmutableMethodParameter(parameterTypeList.get(i), null, "p" + (i + 1)));
        }
        ImmutableMethodImplementation methodImpl = new ImmutableMethodImplementation(localVarCount + parameterCount + 1, insStub, null, null);
        return new ImmutableMethod(PROXY_FRAGMENT_IMPL_TYPE, methodName, parameters, returnType,
                Modifier.PUBLIC, null, null, methodImpl);
    }

    private static ImmutableMethodReference referenceMethod(String declaringClass, String name, String descriptor) {
        return referenceMethod(new DexMethodDescriptor(declaringClass, name, descriptor));
    }

    private static ImmutableMethodReference referenceMethod(DexMethodDescriptor md) {
        return new ImmutableMethodReference(md.declaringClass, md.name, md.getParameterTypes(), md.getReturnType());
    }

    public static ImmutableFieldReference referenceField(String declaringClass, String name, String type) {
        return new ImmutableFieldReference(declaringClass, name, type);
    }
}
