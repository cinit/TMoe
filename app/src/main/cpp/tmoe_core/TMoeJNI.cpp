#include <sys/user.h>
#include <unistd.h>
#include <jni.h>

extern "C" __attribute__((visibility("default")))
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = nullptr;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return JNI_ERR;
    }
    return JNI_VERSION_1_4;
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
