//
// Created by kinit on 2021-06-13.
//

#ifndef RPCPROTOCOL_HASHMAP_H
#define RPCPROTOCOL_HASHMAP_H

#include <memory>
#include <utility>
#include <set>
#include <unordered_map>

template<typename K, typename V, typename Hash = std::hash<K>, typename Pred = std::equal_to<K>>
class HashMap {
public:
    class Entry {
    public:
        Entry(const K &k, const V &v) : key(k), value(v) {}

        Entry(const Entry &) = delete;

        Entry &operator=(const Entry &) = delete;

        [[nodiscard]] K getKey() const {
            return key;
        }

        [[nodiscard]] V *getValue() const {
            return &value;
        }

        template<typename... Args>
        void setValue(Args &&...args) const {
            value = V(std::forward<Args>(args)...);
        }

        void setValue(V &v) const {
            value = v;
        }

    private:
        const K key;
        mutable V value;
    };

private:
    std::unordered_map<K, std::shared_ptr<Entry>, Hash, Pred> backend;
public:
    HashMap() = default;

    template<typename AnyMap>
    explicit HashMap(const AnyMap &map) {
        const auto entries = map.entrySet();
        for (const auto &entry: entries) {
            backend.insert_or_assign(entry->getKey(), std::make_shared<Entry>
                    (entry->getKey(), *entry->getValue()));
        }
    }

    [[nodiscard]] std::set<std::shared_ptr<Entry>> entrySet() const {
        std::set<std::shared_ptr<Entry>> copy = std::set<std::shared_ptr<Entry>>();
        for (const auto &entry: backend) {
            copy.emplace(entry.second);
        }
        return copy;
    }

    [[nodiscard]] size_t size() const {
        return backend.size();
    }

    [[nodiscard]] bool isEmpty() const {
        return size() == 0;
    }

    [[nodiscard]] bool containsKey(const K &key) const {
        return backend.find(key) != backend.end();
    }

    [[nodiscard]] V *get(const K &key) const {
        auto p = backend.find(key);
        if (p != backend.end()) {
            return p->second.get()->getValue();
        } else {
            return nullptr;
        }
    }

    template<typename... Args>
    void put(const K &key, Args &&...args) {
        auto entry = std::make_shared<Entry>(key, V(std::forward<Args>(args)...));
        auto result = backend.insert_or_assign(key, entry);
        backend.insert_or_assign(key, std::shared_ptr<Entry>(entry));
    }

    template<typename... Args>
    bool putIfAbsent(const K &key, Args &&...args) {
        auto entry = std::make_shared<Entry>(key, V(std::forward<Args>(args)...));
        auto result = backend.try_emplace(key, entry);
        return result.second;
    }

    bool remove(const K &key) {
        return backend.erase(key) != 0;
    }

    void clear() {
        backend.clear();
    }

    template<typename AnyMap>
    void putAll(const AnyMap &map) {
        const auto entries = map.entrySet();
        for (const auto &entry: entries) {
            backend.insert_or_assign(entry->getKey(), std::make_shared<Entry>
                    (entry->getKey(), *entry->getValue()));
        }
    }
};

#endif //RPCPROTOCOL_HASHMAP_H
