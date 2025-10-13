#include "GpuMonitor.hpp"
#include <CL/cl.h>
#include <CL/cl_platform.h>
#include <CL/opencl.hpp>
#include <iostream>

GpuMonitor::GpuMonitor() : initialized(false) {}

bool GpuMonitor::init() {
    std::vector<cl::Platform> platforms;
    cl::Platform::get(&platforms);

    if (platforms.empty()) {
        std::cout << "No OpenCL platform found" << std::endl;
        return false;
    }

    platform = platforms[0];
    std::vector<cl::Device> devices;
    platform.getDevices(CL_DEVICE_TYPE_ALL, &devices);

    if (devices.empty()) {
        std::cout << "No OpenCL devices found" << std::endl;
        return false;
    }

    device = devices[0];
    initialized = true;
    return true;
}

std::string GpuMonitor::getName() {
    if (!initialized && !init()) {
        return "GPU Not Found";
    }

    std::string vendor = device.getInfo<CL_DEVICE_VENDOR>();
    std::string boardName;

    if (vendor.find("NVIDIA") != std::string::npos) {
        std::cout << "Found Nvidia" << std::endl;
        boardName = device.getInfo<CL_DEVICE_NAME>();
    } 
    else if (vendor.find("AMD") != std::string::npos || 
             vendor.find("Advanced Micro Devices") != std::string::npos) {
        std::cout << "Found AMD" << std::endl;
        cl_int err = device.getInfo((cl_device_info)0x4038, &boardName);
        if (err != CL_SUCCESS) {
            boardName = device.getInfo<CL_DEVICE_NAME>();
        }
    } 
    else if (vendor.find("Intel") != std::string::npos) {
        std::cout << "Found Intel" << std::endl;
        boardName = device.getInfo<CL_DEVICE_NAME>();
    }

    return boardName;
}

double GpuMonitor::getVram() {
    if (!initialized && !init()) {
        return 0.0;
    }

    cl_ulong memSize;
    
    // AMD-specific extension
    cl_int err = device.getInfo(CL_DEVICE_GLOBAL_FREE_MEMORY_AMD, &memSize);
    
    if (err != CL_SUCCESS) {
        std::cout << "AMD-specific query failed, falling back to standard query." << std::endl;
        memSize = device.getInfo<CL_DEVICE_GLOBAL_MEM_SIZE>() / 1024;
    }
    
    std::cout << "VRAM (bytes): " << memSize << std::endl;

    return static_cast<double>(memSize) / (1024.0 * 1024.0); 
}
