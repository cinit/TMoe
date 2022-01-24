//
// Created by kinit on 2021-11-16.
//

#ifndef NCI_HOST_NATIVES_TEXTUTILS_H
#define NCI_HOST_NATIVES_TEXTUTILS_H

#include <cstdint>
#include <vector>
#include <string>
#include <string_view>

namespace utils {

bool parseInt32(int *result, std::string_view str, int base = 10);

bool parseUInt32(uint32_t *result, std::string_view str, int base = 10);

bool parseInt64(int64_t *result, std::string_view str, int base = 10);

bool parseUInt64(uint64_t *result, std::string_view str, int base = 10);

std::vector<std::string> splitString(const std::string &str, const std::string &splits);

}

#endif //NCI_HOST_NATIVES_TEXTUTILS_H
