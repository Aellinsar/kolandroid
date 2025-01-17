package com.github.kolandroid.kol.android.view;

import android.app.Application;

import com.github.kolandroid.kol.data.DataCache;
import com.github.kolandroid.kol.data.RawItem;
import com.github.kolandroid.kol.data.RawSkill;
import com.github.kolandroid.kol.gamehandler.DataContext;
import com.github.kolandroid.kol.session.Session;
import com.github.kolandroid.kol.session.SessionCache;
import com.github.kolandroid.kol.util.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KolApplication extends Application implements DataContext {
    private final AndroidRawCache<RawSkill> skillsCache = new AndroidRawCache<RawSkill>("skillcache.txt", "skilloverridecache.txt") {
        @Override
        public RawSkill parse(String cacheLine) {
            return RawSkill.parse(cacheLine);
        }
    };

    private final AndroidRawCache<RawItem> itemsCache = new AndroidRawCache<RawItem>("itemcache.txt", "itemoverridecache.txt") {
        @Override
        public RawItem parse(String cacheLine) {
            return RawItem.parse(cacheLine);
        }
    };

    private final Map<String, SessionCache> sessionCache = new ConcurrentHashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();

        new Thread() {
            @Override
            public void run() {
                skillsCache.load(KolApplication.this);
                itemsCache.load(KolApplication.this);
            }
        }.start();
    }

    @Override
    public DataCache<String, RawSkill> getSkillCache() {
        return skillsCache;
    }

    @Override
    public DataCache<String, RawItem> getItemCache() {
        return itemsCache;
    }

    @Override
    public SessionCache getSessionCache(Session session) {
        String key = session.getCookie("PHPSESSID", "");
        if (!sessionCache.containsKey(key)) {
            synchronized (this) {
                if (!sessionCache.containsKey(key)) {
                    Logger.log("KolApplication", "Creating new SessionCache for session: " + session);
                    sessionCache.put(key, new SessionCache(session));
                }
            }
        }

        return sessionCache.get(key);
    }
}
