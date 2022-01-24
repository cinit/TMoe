//
// Created by kinit on 2021-11-28.
//

#include <iostream>
#include <fstream>
#include <unistd.h>
#include <dirent.h>
#include <string>
#include <array>
#include <fcntl.h>
#include <cstring>
#include <algorithm>
#include <sys/utsname.h>

#include "utils/log/Log.h"
#include "TextUtils.h"

#include "ProcessUtils.h"

namespace utils {

static const char *const LOG_TAG = "ProcessUtils";

std::vector<ProcessInfo> getRunningProcessInfo() {
    std::vector<ProcessInfo> runningProcesses;
    std::string procPath = "/proc/";
    DIR *dir = opendir(procPath.c_str());
    if (dir == nullptr) {
        LOGE("Failed to open %s", procPath.c_str());
        return {};
    }
    struct dirent *entry;
    while ((entry = readdir(dir)) != nullptr) {
        if (entry->d_type != DT_DIR) {
            continue;
        }
        std::string pidStr = entry->d_name;
        if (!std::all_of(pidStr.begin(), pidStr.end(), ::isdigit)) {
            continue;
        }
        int pid = std::stoi(pidStr);
        std::string procExePath = procPath + pidStr + "/exe";
        char exe[256];
        ssize_t len = readlink(procExePath.c_str(), exe, sizeof(exe) - 1);
        if (len < 0) {
            continue;
        }
        exe[len] = '\0';
        // get process uid
        int uid = -1;
        std::string procStatusPath = procPath + pidStr + "/status";
        std::ifstream procStatusFile(procStatusPath);
        if (procStatusFile.is_open()) {
            std::string line;
            while (std::getline(procStatusFile, line)) {
                if (line.find("Uid:") == 0) {
                    std::string uidStr = line.substr(5);
                    uid = std::stoi(uidStr);
                    break;
                }
            }
            procStatusFile.close();
        } else {
            uid = -1;
        }
        std::string procCmdlinePath = procPath + pidStr + "/cmdline";
        std::vector<uint8_t> cmdlineBytes;
        if (int cmdlineFd = open(procCmdlinePath.c_str(), O_RDONLY); cmdlineFd >= 0) {
            std::array<uint8_t, 256> buf = {};
            ssize_t n;
            while ((n = read(cmdlineFd, buf.data(), buf.size())) > 0) {
                cmdlineBytes.insert(cmdlineBytes.end(), buf.begin(), buf.begin() + n);
            }
            close(cmdlineFd);
        }
        ProcessInfo processInfo = {};
        processInfo.pid = pid;
        processInfo.uid = uid;
        processInfo.exe = exe;
        // cmdline is split by '\0', remove the last '\0' if it exists
        if (!cmdlineBytes.empty() && cmdlineBytes.back() == '\0') {
            cmdlineBytes.pop_back();
        }
        if (!cmdlineBytes.empty()) {
            processInfo.cmdline = utils::splitString(std::string(
                    reinterpret_cast<const char *>(cmdlineBytes.data()), cmdlineBytes.size()), std::string("\0", 1));
            processInfo.argv0 = processInfo.cmdline.front();
        }
        if (!processInfo.exe.empty()) {
            processInfo.name = processInfo.exe.substr(processInfo.exe.find_last_of('/') + 1);
        }
        runningProcesses.push_back(processInfo);
    }
    closedir(dir);
    return runningProcesses;
}

bool getProcessInfo(int pid, ProcessInfo &info) {
    std::string procPath = "/proc/";
    std::string pidStr = std::to_string(pid);
    // get process uid
    int uid = -1;
    std::string procStatusPath = procPath + pidStr + "/status";
    std::ifstream procStatusFile(procStatusPath);
    if (procStatusFile.is_open()) {
        std::string line;
        while (std::getline(procStatusFile, line)) {
            if (line.find("Uid:") == 0) {
                std::string uidStr = line.substr(5);
                uid = std::stoi(uidStr);
                break;
            }
        }
        procStatusFile.close();
    } else {
        return false;
    }
    std::string procCmdlinePath = procPath + pidStr + "/cmdline";
    std::vector<uint8_t> cmdlineBytes;
    if (int cmdlineFd = open(procCmdlinePath.c_str(), O_RDONLY); cmdlineFd >= 0) {
        std::array<uint8_t, 256> buf = {};
        ssize_t n;
        while ((n = read(cmdlineFd, buf.data(), buf.size())) > 0) {
            cmdlineBytes.insert(cmdlineBytes.end(), buf.begin(), buf.begin() + n);
        }
        close(cmdlineFd);
    }
    // cmdline is split by '\0', remove the last '\0' if it exists
    if (!cmdlineBytes.empty() && cmdlineBytes.back() == '\0') {
        cmdlineBytes.pop_back();
    }
    if (!cmdlineBytes.empty()) {
        info.cmdline = utils::splitString(std::string(
                reinterpret_cast<const char *>(cmdlineBytes.data()), cmdlineBytes.size()), std::string("\0", 1));
        info.argv0 = info.cmdline.front();
    }
    info.pid = pid;
    info.uid = uid;
    // get process exe path
    std::string procExePath = procPath + pidStr + "/exe";
    char exe[256];
    ssize_t len = readlink(procExePath.c_str(), exe, sizeof(exe) - 1);
    if (len < 0) {
        return false;
    }
    exe[len] = '\0';
    info.exe = exe;
    if (!info.exe.empty()) {
        info.name = info.exe.substr(info.exe.find_last_of('/') + 1);
    }
    return true;
}

int getKernelArchitecture() noexcept {
    struct utsname uts = {};
    if (uname(&uts) != 0) {
        return -errno;
    }
    if (strcmp(uts.machine, "x86_64") == 0) {
        return Architecture::ARCH_X86_64;
    } else if (strcmp(uts.machine, "x86") == 0) {
        return Architecture::ARCH_X86;
    } else if (strcmp(uts.machine, "arm") == 0) {
        return Architecture::ARCH_ARM;
    } else if (strcmp(uts.machine, "aarch64") == 0) {
        return Architecture::ARCH_AARCH64;
    }
    return -ENOTSUP;
}

int getCurrentProcessArchitecture() noexcept {
#if defined(__aarch64__) || defined(__arm64__) || defined (_M_ARM64)
    return Architecture::ARCH_AARCH64;
#elif defined(__arm__) || defined(_M_ARM)
    return Architecture::ARCH_ARM;
#elif defined(__x86_64__) || defined(__amd64__)
    return Architecture::ARCH_X86_64;
#elif defined(__x86__) || defined(__i386__) || defined(_M_IX86)
    return Architecture::ARCH_X86;
#else
#error "Unsupported architecture"
#endif
}

}
