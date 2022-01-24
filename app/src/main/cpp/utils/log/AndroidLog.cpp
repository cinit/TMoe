//
// Created by kinit on 2022-01-24.
//

#include "AndroidLog.h"

extern "C" void __android_log_print(int level, const char *tag, const char *fmt, ...);

namespace logging::android {

static void android_log_handler(Log::Level level, const char *tag, const char *msg) {
#ifdef __ANDROID__
    __android_log_print(static_cast<int>(level), "TMoe", "%s: %s", tag, msg);
#endif
}

static bool sInitialized = false;

void init() {
    if (!sInitialized) {
        Log::setLogHandler(&android_log_handler);
        sInitialized = true;
    }
}

}
