//
// Created by kinit on 2021-06-24.
//

#include "SharedBuffer.h"

#include <mutex>
#include <algorithm>
#include <cstring>
#include <malloc.h>

constexpr float LOAD_FACTOR = 0.75f;

class SharedBufferImpl {
private:
    mutable std::mutex mMutex;
    void *mBuffer = nullptr;
    size_t mSize = 0;
    size_t mActualSize = 0;
public:

    SharedBufferImpl() = default;

    ~SharedBufferImpl() noexcept {
        std::scoped_lock _(mMutex);
        if (mBuffer != nullptr) {
            free(mBuffer);
            mBuffer = nullptr;
            mSize = 0;
            mActualSize = 0;
        }
    }

    [[nodiscard]] void *get() const noexcept {
        std::scoped_lock _(mMutex);
        return mBuffer;
    }

    [[nodiscard]] size_t size() const noexcept {
        std::scoped_lock _(mMutex);
        return mSize;
    }

    void reset() noexcept {
        std::scoped_lock _(mMutex);
        if (mBuffer != nullptr) {
            free(mBuffer);
            mBuffer = nullptr;
            mSize = 0;
            mActualSize = 0;
        }
    }

    [[nodiscard]] bool resetSize(size_t size, bool keepContent) noexcept {
        std::scoped_lock _(mMutex);
        if (size == 0) {
            mSize = 0;
            return mBuffer != nullptr;
        }
        if (mBuffer != nullptr) {
            if (size * 2 < mActualSize || size > mActualSize) {
                size_t newSize = int(float(size) / LOAD_FACTOR);
                void *newBuffer = malloc(newSize);
                if (newBuffer == nullptr) {
                    return false;
                }
                if (keepContent) {
                    size_t cpSize = std::min(size, mSize);
                    memcpy(newBuffer, mBuffer, cpSize);
                    size_t left = newSize - cpSize;
                    if (left > 0) {
                        memset((char *) (newBuffer) + cpSize, 0, left);
                    }
                } else {
                    memset(newBuffer, 0, newSize);
                }
                // fill the old buffer with 0xCC before free for debugging purpose
                memset(mBuffer, 0xCC, mActualSize);
                free(mBuffer);
                mBuffer = newBuffer;
                mActualSize = newSize;
                mSize = size;
            } else {
                mSize = size;
            }
            return true;
        } else {
            mBuffer = malloc(size);
            if (mBuffer == nullptr) {
                return false;
            }
            memset(mBuffer, 0, size);
            mSize = mActualSize = size;
        }
        return true;
    }
};

SharedBuffer::SharedBuffer() = default;

SharedBuffer::SharedBuffer(const SharedBuffer &o) = default;

SharedBuffer &SharedBuffer::operator=(const SharedBuffer &o) = default;

SharedBuffer::~SharedBuffer() = default;

size_t SharedBuffer::size() const noexcept {
    const SharedBufferImpl *p = pImpl.get();
    if (p == nullptr) {
        return 0;
    } else {
        return p->size();
    }
}

void *SharedBuffer::get() const noexcept {
    const SharedBufferImpl *p = pImpl.get();
    if (p == nullptr) {
        return nullptr;
    } else {
        return p->get();
    }
}

SharedBuffer SharedBuffer::copy() const {
    SharedBuffer other;
    if (get() != nullptr && size() > 0) {
        other.ensureCapacity(size());
        memcpy(other.get(), get(), size());
    }
    return other;
}

bool SharedBuffer::ensureCapacity(size_t size, std::nothrow_t) noexcept {
    SharedBufferImpl *p = pImpl.get();
    if (p == nullptr) {
        p = new SharedBufferImpl();
        pImpl.reset(p);
        return p->resetSize(size, false);
    } else {
        if (p->size() < size) {
            return p->resetSize(size, true);
        } else {
            return true;
        }
    }
}

bool SharedBuffer::resetSize(size_t size, bool keepContent) noexcept {
    SharedBufferImpl *p = pImpl.get();
    if (p == nullptr) {
        p = new SharedBufferImpl();
        pImpl.reset(p);
        return p->resetSize(size, keepContent);
    } else {
        return p->resetSize(size, keepContent);
    }
}

void SharedBuffer::reset() noexcept {
    SharedBufferImpl *p = pImpl.get();
    if (p != nullptr) {
        p->reset();
    }
}

std::vector<uint8_t> SharedBuffer::toVector() const {
    const SharedBufferImpl *p = pImpl.get();
    if (p != nullptr && p->get() != nullptr) {
        return {reinterpret_cast<const uint8_t *>(p->get()),
                reinterpret_cast<const uint8_t *>(p->get()) + p->size()};
    } else {
        return {};
    }
}
