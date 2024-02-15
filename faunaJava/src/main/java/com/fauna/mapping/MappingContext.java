package com.fauna.mapping;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MappingContext {

    private final Map<Type, MappingInfo> _cache = new ConcurrentHashMap<>();

    public MappingContext() {
    }

    public MappingInfo getInfo(Type ty) {
        synchronized (_cache) {
            if (_cache.containsKey(ty)) {
                return _cache.get(ty);
            }
        }

        MappingInfo info = new MappingInfo(this, ty);
        add(ty, info);
        return info;
    }

    void add(Type ty, MappingInfo info) {
        synchronized (_cache) {
            _cache.put(ty, info);
        }
    }
}
