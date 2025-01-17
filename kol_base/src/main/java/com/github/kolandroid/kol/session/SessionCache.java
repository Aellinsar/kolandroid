package com.github.kolandroid.kol.session;

import com.github.kolandroid.kol.connection.ServerReply;
import com.github.kolandroid.kol.session.data.FightActionBarData;
import com.github.kolandroid.kol.session.data.PwdData;
import com.github.kolandroid.kol.util.Callback;
import com.github.kolandroid.kol.util.Logger;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionCache {
    // All cache items
    private final Map<Class<?>, CacheItem> cache;

    // Current session for the cache
    private final Session session;

    /**
     * Create a new cache for information within the provided session.
     *
     * @param session Session to cache information within
     */
    public SessionCache(Session session) {
        this.session = session;
        this.cache = new ConcurrentHashMap<>();

        init(Session.class, new BasicCacheItem<>(session));
        init(PwdData.class, new BasicCacheItem<PwdData>(null));
        init(FightActionBarData.class, new LiveCacheItem<FightActionBarData>(session) {
            @Override
            protected FightActionBarData process(ServerReply reply) {
                return FightActionBarData.create(reply);
            }

            @Override
            protected void computeUrl(SessionCache cache, final Callback<String> callback, Callback<Void> failure) {
                cache.access(PwdData.class, new Callback<PwdData>() {
                    @Override
                    public void execute(PwdData item) {
                        callback.execute("actionbar.php?action=fetch&d=" + System.currentTimeMillis() + "&pwd=" + item.getPwd());
                    }
                }, failure);
            }

            @Override
            Class[] dependencies() {
                return new Class[]{PwdData.class};
            }
        });
    }

    /**
     * Initialize a line of the cache.
     *
     * @param id        The class identifier for the cache line.
     * @param container The container backing the cache line.
     * @param <E>       The type of item to store in the new cache line.
     */
    private <E extends Serializable> void init(Class<E> id, CacheItem<E> container) {
        cache.put(id, container);
        for (Class dependency : container.dependencies()) {
            if (cache.containsKey(dependency)) {
                cache.get(dependency).addDependent(container);
            } else {
                Logger.log("SessionCache", "Container for " + id + " attempted to declare dependency for " + dependency + ", but this was not yet stored in the cache");
            }
        }
    }

    /**
     * Attempt to access the stored item, either using the stored copy or recomputing if needed.
     *
     * @param id       Which cache line to access
     * @param callback Callback to call when the stored item is located
     * @param failure  Callback to call when we are unable to find or compute this item
     */
    public <E extends Serializable> void access(Class<E> id, Callback<E> callback, Callback<Void> failure) {
        if (cache.containsKey(id)) {
            CacheItem<E> cached = (CacheItem<E>) cache.get(id);
            cached.access(this, callback, failure);
        } else {
            failure.execute(null);
        }
    }

    /**
     * Explicitly change the stored version of a cache line.
     *
     * @param id    Which cache line to fill
     * @param value The new version of the item to store in the cache line
     */
    public <E extends Serializable> void put(Class<E> id, E value) {
        if (cache.containsKey(id)) {
            CacheItem<E> cached = (CacheItem<E>) cache.get(id);
            cached.fill(value);
        }
    }


    /**
     * Explicitly clear a cache line.
     *
     * @param id Which cache line to clear
     */
    public <E extends Serializable> void clear(Class<E> id) {
        if (cache.containsKey(id)) {
            CacheItem<E> cached = (CacheItem<E>) cache.get(id);
            cached.markDirty();
        }
    }

}
