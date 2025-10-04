#pragma once
#include <CL/opencl.hpp>
#include <string>

class GpuMonitor {
public:
  static cl::Device device;
  static std::string getName();
  static long getVram();
};
