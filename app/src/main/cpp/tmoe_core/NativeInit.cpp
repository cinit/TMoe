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

namespace tmoe::core {

struct NativeHookHandle {
    int (*hookFunction)(void *func, void *replace, void **backup);
};

static NativeHookHandle nativeHookHandle = {};

static void* gLoadedTMessageLibHandle = nullptr;

void handleLoadLibrary(const char* name, void* handle) {
    if (name == nullptr || handle == nullptr) {
        return;
    }
    std::string tmpName(name);
    std::string soname = tmpName.substr(tmpName.find_last_of('/') == std::string::npos ?
                                        0 : tmpName.find_last_of('/') + 1);
    if (soname.find("libtmessages.") != std::string::npos) {
        gLoadedTMessageLibHandle = handle;
    }
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

extern "C" void* GetLoadedTMessageLibHandle() {
    return ::tmoe::core::gLoadedTMessageLibHandle;
}
