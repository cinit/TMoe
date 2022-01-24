//
// Created by kinit on 2021-11-28.
//

#ifndef NCI_HOST_NATIVES_PROCESSUTILS_H
#define NCI_HOST_NATIVES_PROCESSUTILS_H

#include <string>
#include <vector>

namespace utils {

struct ProcessInfo {
    int pid;
    int uid;
    std::string name;
    std::string exe;
    std::vector<std::string> cmdline;
    std::string argv0;
};

// get all running process info
std::vector<ProcessInfo> getRunningProcessInfo();

/**
 * Get the process info of the specified process.
 * @param pid the pid of the process.
 * @param info the process info.
 * @return true if the process info is found, false otherwise.
 */
bool getProcessInfo(int pid, ProcessInfo &info);

class Architecture {
public:
    static constexpr int ARCH_X86 = 3;
    static constexpr int ARCH_X86_64 = 62;
    static constexpr int ARCH_ARM = 40;
    static constexpr int ARCH_AARCH64 = 183;
};

/**
 * Get the kernel architecture of the current Linux system.
 * Note that this may not always be the same as the current process architecture.
 * Do not use '/proc/stat', Android's SELinux policy does not allow reading that.
 * Currently, this function only supports x86, x86_64, and ARM architectures.
 * The result is from the uname system call.
 * @return positive on success, -errno on failure.
 */
int getKernelArchitecture() noexcept;

int getCurrentProcessArchitecture() noexcept;

}

#endif //NCI_HOST_NATIVES_PROCESSUTILS_H
