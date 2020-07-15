package com.github.sofiman.inventory.impl;

import com.github.sofiman.inventory.api.models.Database;

import java.util.HashMap;
import java.util.Map;

abstract class CachedModel<T> {

    protected Database database;
    private Map<byte[], CacheEntry<T>> cache;

    public CachedModel(){
        this.cache = new HashMap<>();
    }

    protected T get(byte[] id){
        CacheEntry<T> entry = cache.get(id);
        if(entry == null) return null;
        return entry.unpack();
    }

    protected void put(byte[] id, T data, long expireInMillis){
        cache.put(id, new CacheEntry<>(data, expireInMillis));
    }

    protected void remove(byte[] id){
        cache.remove(id);
    }

    protected void clearDataCache(){
        cache.clear();
    }

    private static class CacheEntry<T> {

        private T data;
        private long expire;

        private CacheEntry(T data, long expire){
            this.data = data;
            this.expire = System.currentTimeMillis() + expire;
        }

        private T unpack(){
            if(System.currentTimeMillis() > expire){
                return null;
            }
            return data;
        }
    }
}
