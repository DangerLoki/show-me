#pragma once
#include <CL/opencl.hpp>
#include <CL/cl_ext.h>
#include <string>

class GpuMonitor {
private:
    cl::Device device;
    cl::Platform platform;
    bool initialized;

public:
    GpuMonitor();
    bool init();
    std::string getName();
    double getVram();
};
