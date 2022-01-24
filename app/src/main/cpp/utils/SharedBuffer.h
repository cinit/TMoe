//
// Created by kinit on 2021-06-24.
//

#ifndef RPCPROTOCOL_SHAREDBUFFER_H
#define RPCPROTOCOL_SHAREDBUFFER_H

#include <cstddef>
#include <atomic>
#include <memory>
#include <cstdint>
#include <vector>

class SharedBufferImpl;

/**
 * Java-like Object
 */
class SharedBuffer {
public:
    SharedBuffer();

    SharedBuffer(const SharedBuffer &o);

    SharedBuffer &operator=(const SharedBuffer &o);

    ~SharedBuffer();

    [[nodiscard]] size_t size() const noexcept;

    [[nodiscard]] void *get() const noexcept;

    [[nodiscard]] SharedBuffer copy() const;

    template<class T>
    [[nodiscard]] const T *at(size_t s) const noexcept {
        if (s + sizeof(T) > size()) {
            return nullptr;
        } else {
            return (const T *) (((const char *) get()) + s);
        }
    }

    template<class T>
    [[nodiscard]] T *at(size_t s) noexcept {
        if (s + sizeof(T) > size()) {
            return nullptr;
        } else {
            return (T *) (((char *) get()) + s);
        }
    }

    [[nodiscard]] bool ensureCapacity(size_t size, std::nothrow_t) noexcept;

    inline void ensureCapacity(size_t size) {
        if (size != 0 && !ensureCapacity(size, std::nothrow)) {
            throw std::bad_alloc();
        }
    }

    [[nodiscard]] std::vector<uint8_t> toVector() const;

    [[nodiscard]] bool resetSize(size_t size, bool keepContent = true) noexcept;

    void reset() noexcept;

private:
    std::shared_ptr<SharedBufferImpl> pImpl;
};

#endif //RPCPROTOCOL_SHAREDBUFFER_H
