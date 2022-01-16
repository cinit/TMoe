#ifndef TMOE_VERSION
#error Please define macro TMOE_VERSION in CMakeList.txt
#endif

#include <stdio.h>
#include <unistd.h>

__attribute__((used, section("TMOE_VERSION"), visibility("default")))
const char g_tmoe_version[] = TMOE_VERSION;

#if defined(__aarch64__) || defined(__x86_64__)
const char so_interp[] __attribute__((used, section(".interp")))= "/system/bin/linker64";
#elif defined(__i386__) || defined(__arm__)
const char so_interp[] __attribute__((used, section(".interp"))) = "/system/bin/linker";
#else
#error Unknown Arch
#endif

__attribute__((used, noreturn, section(".entry_init")))
void __libtmoe_main(void) {
    printf("TMoe libtmoe.so version " TMOE_VERSION ".\n"
           "Copyright (C) 2019-2022 xenonhydride@gmail.com\n"
           "This software is distributed in the hope that it will be useful,\n"
           "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
           "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\n");
    _exit(0);
}

#include "NativeInit.h"

__attribute__(( visibility("default")))
void *native_init(const void *entries) {
    return tmoe_core_NativeInit(entries);
}
