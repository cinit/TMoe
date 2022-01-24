// SPDX-License-Identifier: MIT
//
// Created by kinit on 2021-10-05.
//

#ifndef RPCPROTOCOL_LOG_H
#define RPCPROTOCOL_LOG_H

#include <cstdarg>
#include <cstdio>
#include <cstring>
#include <malloc.h>

class Log {
public:
    enum class Level {
        UNKNOWN = 0,
        VERBOSE = 2,
        DEBUG = 3,
        INFO = 4,
        WARN = 5,
        ERROR = 6
    };
    using LogHandler = void (*)(Level level, const char *tag, const char *msg);
private:
    static volatile LogHandler mHandler;
public:
    static void format(Level level, const char *tag, const char *fmt, ...)
    __attribute__ ((__format__ (__printf__, 3, 4))) {
        va_list varg1;
        va_list varg2;
        LogHandler h = mHandler;
        if (h == nullptr || fmt == nullptr) {
            return;
        }
        va_start(varg1, fmt);
        va_copy(varg2, varg1);
        int size = vsnprintf(nullptr, 0, fmt, varg1) + 4;
        va_end(varg1);
        if (size <= 0) {
            return;
        }
        void *buffer = malloc(size);
        if (buffer == nullptr) {
            return;
        }
        va_start(varg2, fmt);
        vsnprintf((char *) buffer, size, fmt, varg2);
        va_end(varg2);
        h(level, tag, static_cast<const char *>(buffer));
        free(buffer);
    }

    static void logBuffer(Level level, const char *tag, const char *msg) {
        LogHandler h = mHandler;
        if (h == nullptr) {
            return;
        }
        h(level, tag, msg);
    }

    static inline LogHandler getLogHandler() noexcept {
        return mHandler;
    }

    static inline void setLogHandler(LogHandler h) noexcept {
        mHandler = h;
    }
};

#define LOGE(...)  Log::format(Log::Level::ERROR, LOG_TAG, __VA_ARGS__)
#define LOGW(...)  Log::format(Log::Level::WARN, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  Log::format(Log::Level::INFO, LOG_TAG, __VA_ARGS__)
#define LOGD(...)  Log::format(Log::Level::DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGV(...)  Log::format(Log::Level::VERBOSE, LOG_TAG, __VA_ARGS__)

#endif //RPCPROTOCOL_LOG_H
