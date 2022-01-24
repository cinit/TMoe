#include <cstdint>

#include "lsp_native_api.h"
#include "utils/log/AndroidLog.h"

#include "NativeInit.h"

static constexpr auto LOG_TAG = "NativeInit";

namespace tmoe::core {

struct NativeHookHandle {
    int (*hookFunction)(void *func, void *replace, void **backup);
};

static NativeHookHandle nativeHookHandle = {};

void handleLoadLibrary(const char *name, void *handle) {
    // TODO: implement this function
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
