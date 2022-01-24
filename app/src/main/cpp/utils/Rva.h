//
// Created by kinit on 2021-09-30.
//

#ifndef RPCPROTOCOL_RVA_H
#define RPCPROTOCOL_RVA_H

#include "cstddef"

class Rva {
public:
    const void *address = nullptr;
    size_t length = 0;

    Rva() = default;

    inline Rva(const void *addr, size_t len) : address(addr), length(len) {}

    template<class T>
    inline T *at(size_t offset) noexcept {
        if (offset + sizeof(T) > length) {
            return nullptr;
        } else {
            return (T *) (((char *) address) + offset);
        }
    }

    template<class T>
    inline const T *at(size_t offset) const noexcept {
        if (offset + sizeof(T) > length) {
            return nullptr;
        } else {
            return (const T *) (((const char *) address) + offset);
        }
    }

    template<class T>
    inline bool access(size_t offset) const noexcept {
        if (offset + sizeof(T) > length) {
            return false;
        } else {
            return true;
        }
    }
};

#endif //RPCPROTOCOL_RVA_H
