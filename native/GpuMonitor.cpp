#include "GpuMonitor.hpp"
#include <CL/cl.h>
#include <CL/cl_platform.h>
#include <CL/opencl.hpp>
#include <iostream>

cl::Device GpuMonitor::device;

std::string GpuMonitor::getName() {

  std::vector<cl::Platform> all_platforms;
  cl::Platform::get(&all_platforms);

  if (all_platforms.size() == 0) {
    std::cout << " No Platform found " << std::endl;
  }

  cl::Platform default_platform = all_platforms[0];

  std::vector<cl::Device> all_devices;
  default_platform.getDevices(CL_DEVICE_TYPE_ALL, &all_devices);
  if (all_devices.size() == 0) {
    std::cout << "No devices found" << std::endl;
  }

  std::vector<std::string> found;

  for (auto &device : all_devices) {

    std::string vendor = device.getInfo<CL_DEVICE_VENDOR>();

    std::string boardName;
    if (vendor.find("NVIDIA") != std::string::npos) {
      std::cout << "Found Nvidia" << std::endl;
      boardName = device.getInfo<CL_DEVICE_NAME>();
      found.push_back(boardName);
    } else if (vendor.find("AMD") != std::string::npos ||
               vendor.find("Advanced Micro Devices") != std::string::npos) {
      std::cout << "Found AMD" << std::endl;
      cl_int err = device.getInfo((cl_device_info)0x4038, &boardName);
      found.push_back(boardName);
    } else if (vendor.find("Intel") != std::string::npos) {
      std::cout << "Found Intel" << std::endl;
      boardName = device.getInfo<CL_DEVICE_NAME>();
      found.push_back(boardName);
    }
  }

  for (auto &item : found) {
    std::cout << item << std::endl;
  }

  GpuMonitor::device = all_devices[0];

  return found[0];
}

// this will get me gb i think...
long GpuMonitor::getVram() {
  cl_ulong memSize;
  std::cout << GpuMonitor::device.getInfo<CL_DEVICE_NAME>() << std::endl;
  memSize = GpuMonitor::device.getInfo<CL_DEVICE_GLOBAL_MEM_SIZE>();
  return memSize;
}
