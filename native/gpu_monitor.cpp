#include <jni.h>

#include "GpuMonitor.hpp"

int main() { return 0; }

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_meioQuilo_showme_GpuMonitor_getName(JNIEnv *env, jclass clazz) {
	std::string name = GpuMonitor::getName();
  return env->NewStringUTF(name.c_str());
}

JNIEXPORT jdouble JNICALL
Java_com_meioQuilo_showme_GpuMonitor_getVram(JNIEnv *env, jclass clazz) {
	return GpuMonitor::getVram();
}

}
