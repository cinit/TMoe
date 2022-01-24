//
// Created by kinit on 2021-11-16.
//
#include <charconv>

#include "TextUtils.h"

bool utils::parseInt32(int *result, std::string_view str, int base) {
    auto ret = std::from_chars(str.data(), str.data() + str.size(), *result, base);
    return int(ret.ec) == 0;
}

bool utils::parseUInt32(uint32_t *result, std::string_view str, int base) {
    auto ret = std::from_chars(str.data(), str.data() + str.size(), *result, base);
    return int(ret.ec) == 0;
}

bool utils::parseInt64(int64_t *result, std::string_view str, int base) {
    auto ret = std::from_chars(str.data(), str.data() + str.size(), *result, base);
    return int(ret.ec) == 0;
}

bool utils::parseUInt64(uint64_t *result, std::string_view str, int base) {
    auto ret = std::from_chars(str.data(), str.data() + str.size(), *result, base);
    return int(ret.ec) == 0;
}

std::vector<std::string> utils::splitString(const std::string &str, const std::string &splits) {
    std::vector<std::string> res;
    if (str.empty()) {
        return res;
    }
    std::string strs = str + splits;
    size_t pos = strs.find(splits);
    size_t step = splits.size();
    while (pos != std::string::npos) {
        std::string temp = strs.substr(0, pos);
        res.push_back(temp);
        strs = strs.substr(pos + step, strs.size());
        pos = strs.find(splits);
    }
    return res;
}
