#include <unistd.h>
#include <dlfcn.h>
#include <jni.h>

#include <optional>

#include "utils/log/AndroidLog.h"
#include "utils/FileMemMap.h"
#include "utils/elfsym/ProcessView.h"
#include "utils/elfsym/ElfView.h"
#include "NativeInit.h"

static constexpr auto LOG_TAG = "TMoeJNI";

extern "C" __attribute__((visibility("default")))
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    logging::android::init();
    JNIEnv *env = nullptr;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    LOGD("JNI_OnLoad");
    return JNI_VERSION_1_6;
}

/*
 * Class:     cc_ioctl_tmoe_startup_NativeLoader
 * Method:    getpagesize
 * Signature: ()I
 */
extern "C" JNIEXPORT jint JNICALL Java_cc_ioctl_tmoe_startup_NativeLoader_getpagesize
        (JNIEnv *, jclass) {
    return getpagesize();
}

static void *pFileLogLogsEnabled = nullptr;

static void *lookupFileLogLogsEnabled() {
    void *p = pFileLogLogsEnabled;
    if (p != nullptr) {
        return p;
    }
    // prefer dlfcn if available
    if (auto* handle = GetLoadedTMessageLibHandle()) {
        p = dlsym(handle, "LOGS_ENABLED");
        if (p != nullptr) {
            LOGD("LOGS_ENABLED: %p", p);
            pFileLogLogsEnabled = p;
            return p;
        }
    }
    // lookup
    elfsym::ProcessView self;
    int err = self.readProcess(getpid());
    if (err != 0) {
        LOGE("readProcess self failed, err: %d", err);
    }
    std::optional<elfsym::ProcessView::Module> libtmsg;
    for (const auto &module: self.getModules()) {
        if (module.name.find("libtmessages.") != std::string::npos) {
            libtmsg = module;
            break;
        }
    }
    if (!libtmsg.has_value()) {
        LOGE("libtmessages.*.so not found");
        return nullptr;
    }
    std::string soanme = libtmsg->name;
    std::string path = libtmsg->path;
    uintptr_t baseAddress = libtmsg->baseAddress;
    LOGD("libtmsg: %s, baseAddress: %p", soanme.c_str(), (void *) baseAddress);
    FileMemMap fileMemMap;
    err = fileMemMap.mapFilePath(path.c_str());
    if (err != 0) {
        LOGE("mapFilePath failed, err: %d", err);
        return nullptr;
    }
    elfsym::ElfView elfView;
    elfView.attachFileMemMapping(Rva(fileMemMap.getAddress(), fileMemMap.getLength()));
    int offset = elfView.getSymbolAddress("LOGS_ENABLED");
    if (offset == 0) {
        LOGE("getSymbolAddress LOGS_ENABLED failed");
        return nullptr;
    }
    uintptr_t addr = baseAddress + offset;
    LOGD("LOGS_ENABLED: %p", (void *) addr);
    pFileLogLogsEnabled = reinterpret_cast<void *>(addr);
    return pFileLogLogsEnabled;
}

extern "C" JNIEXPORT jint JNICALL
Java_cc_ioctl_tmoe_hook_func_TgnetLogController_getCurrentTgnetLogStatus(JNIEnv *env, jclass clazz) {
    void *p = lookupFileLogLogsEnabled();
    if (p == nullptr) {
        LOGE("getCurrentTgnetLogStatus: lookupFileLogLogsEnabled failed");
        return -1;
    }
    const auto *p8 = (uint8_t *) p;
    return jint(*p8);
}

extern "C" JNIEXPORT jint JNICALL
Java_cc_ioctl_tmoe_hook_func_TgnetLogController_setCurrentTgnetLogStatus(JNIEnv *env, jclass clazz, jint status) {
    void *p = lookupFileLogLogsEnabled();
    if (p == nullptr) {
        LOGE("setCurrentTgnetLogStatus: lookupFileLogLogsEnabled failed");
        return -1;
    }
    auto *p8 = (uint8_t *) p;
    uint8_t prev = *p8;
    *p8 = uint8_t(status);
    LOGD("setCurrentTgnetLogStatus: %d -> %d", prev, *p8);
    return jint(prev);
}
