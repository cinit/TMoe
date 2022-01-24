// SPDX-License-Identifier: MIT
//
// Created by kinit on 2021-10-25.
//

#ifndef NCI_HOST_NATIVES_PROCESSVIEW_H
#define NCI_HOST_NATIVES_PROCESSVIEW_H

#include <cstdint>
#include <vector>
#include <string>

namespace elfsym {

class ProcessView {
public:
    class Module {
    public:
        std::string name;
        std::string path;
        uint64_t baseAddress;
    };

    [[nodiscard]] int readProcess(int pid);

    [[nodiscard]] int getPointerSize() const noexcept;

    [[nodiscard]] int getArchitecture() const noexcept;

    [[nodiscard]] bool isValid() const noexcept;

    [[nodiscard]] std::vector<Module> getModules() const;

private:
    int mPointerSize = 0;
    int mArchitecture = 0;
    std::vector<Module> mProcessModules;
};

}

#endif //NCI_HOST_NATIVES_PROCESSVIEW_H
