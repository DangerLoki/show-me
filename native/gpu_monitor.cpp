#include <jni.h>
#include "GpuMonitor.hpp"

static GpuMonitor monitor;

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_meioQuilo_showme_GpuMonitor_getName(JNIEnv *env, jclass clazz) {
    std::string name = monitor.getName();
    return env->NewStringUTF(name.c_str());
}

JNIEXPORT jdouble JNICALL
Java_com_meioQuilo_showme_GpuMonitor_getVram(JNIEnv *env, jclass clazz) {
    return monitor.getVram();
}

}
