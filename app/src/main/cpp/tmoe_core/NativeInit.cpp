#include <cstdint>
#include <string>
#include <unistd.h>
#include <dlfcn.h>
#include <sys/mman.h>

#include "lsp_native_api.h"
#include "utils/log/AndroidLog.h"
#include "utils/elfsym/ProcessView.h"
#include "utils/elfsym/ElfView.h"
#include "utils/FileMemMap.h"

#include "NativeInit.h"

static constexpr auto LOG_TAG = "NativeInit";

void patchAntiXposed();

namespace tmoe::core {

struct NativeHookHandle {
    int (*hookFunction)(void *func, void *replace, void **backup);
};

static NativeHookHandle nativeHookHandle = {};

void handleLoadLibrary(const char *name, void *handle) {
    if (name == nullptr || handle == nullptr) {
        return;
    }
    std::string tmpName(name);
    std::string soname = tmpName.substr(tmpName.find_last_of('/') == std::string::npos ?
                                        0 : tmpName.find_last_of('/') + 1);
    // from Pigeongram >= 8.4.4, it has anti Xposed feature
    if (soname.find("libneko.") == std::string::npos) {
        return;
    }
    LOGD("found neko library: %s handle = %p", name, handle);
    patchAntiXposed();
}

NativeOnModuleLoaded initNativeHook(const NativeAPIEntries *entries) {
    nativeHookHandle.hookFunction = entries->hookFunc;
    return &handleLoadLibrary;
}
}

extern "C" void *tmoe_core_NativeInit(const void *entries) {
    logging::android::init();
    LOGD("NativeInit, entries: %p", entries);
    return reinterpret_cast<void *>(tmoe::core::initNativeHook(static_cast<const NativeAPIEntries *>(entries)));
}

int (*original_system_property_get)(const char *, char *) = nullptr;

int makeXposedDetectorHappy = 0;

int fake_system_property_get(const char *name, char *value) {
    if (original_system_property_get == nullptr) {
        LOGE("we are in fake_system_property_get, but original_system_property_get is nullptr");
        // panic
        return -1;
    }
    int ret = original_system_property_get(name, value);
    if (strcmp(name, "ro.build.version.sdk") == 0 && makeXposedDetectorHappy > 0) {
        strcpy(value, "20");
        makeXposedDetectorHappy--;
    }
    LOGD("fake_system_property_get, name: %s, value: %s", name, value);
    return ret;
}

void patchAntiXposed() {
    elfsym::ProcessView processView;
    int err = processView.readProcess(getpid());
    if (err != 0) {
        LOGE("ProcessView.readProcess self failed: %d", err);
        return;
    }
    uintptr_t libnekoBase = 0;
    std::string modulePath;
    for (const auto &m: processView.getModules()) {
        if (m.name.find("libneko.") != std::string::npos) {
            libnekoBase = (uintptr_t) m.baseAddress;
            modulePath = m.path;
            LOGD("%s base: %p", m.name.c_str(), (void *) m.baseAddress);
            break;
        }
    }
    if (libnekoBase == 0) {
        LOGE("libneko.*.so not found, dump modules: ");
        for (const auto &m: processView.getModules()) {
            LOGE("%p %s %s", (void *) m.baseAddress, m.name.c_str(), m.path.c_str());
        }
        LOGE("end dump modules");
        return;
    }
    FileMemMap libnekoMap;
    err = libnekoMap.mapFilePath(modulePath.c_str(), true, 0);
    if (err != 0) {
        LOGE("mapFilePath failed: %d, path: %s", err, modulePath.c_str());
        return;
    }
    elfsym::ElfView elfView;
    elfView.attachFileMemMapping({libnekoMap.getAddress(), libnekoMap.getLength()});
    auto offsets = elfView.getExtSymGotRelVirtAddr("__system_property_get");
    if (offsets.empty()) {
        LOGE("__system_property_get not found");
        return;
    }
    {
        void *hLibc = dlopen("libc.so", RTLD_NOW | RTLD_NOLOAD);
        if (hLibc == nullptr) {
            LOGE("dlopen libc.so failed: %s", dlerror());
            return;
        }
        original_system_property_get = reinterpret_cast<int (*)(const char *, char *)>(
                dlsym(hLibc, "__system_property_get"));
        dlclose(hLibc);
        if (original_system_property_get == nullptr) {
            LOGE("dlsym __system_property_get failed: %s", dlerror());
            return;
        }
    }
    uintptr_t systemPropertyGet = libnekoBase + (uintptr_t) offsets[0];
    // hook __system_property_get
    err = mprotect(reinterpret_cast<void *>(systemPropertyGet & ~0xfff), 0x1000, PROT_READ | PROT_WRITE);
    if (err != 0) {
        LOGE("mprotect failed: %d, addr: %p", err, reinterpret_cast<void *>(systemPropertyGet & ~0xfff));
        return;
    }
    void **p = reinterpret_cast<void **>(systemPropertyGet);
    *p = reinterpret_cast<void *>(&fake_system_property_get);
    mprotect(reinterpret_cast<void *>(systemPropertyGet & ~0xfff), 0x1000, PROT_READ);
    makeXposedDetectorHappy = 2;
    LOGD("hook __system_property_get success, orig addr: %p", (void *) systemPropertyGet);
}
