package org.xinyo.subtitle.util;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.xinyo.subtitle.entity.auth.User;

import java.util.concurrent.TimeUnit;

/**
 * @author CHENG
 */
public class TokenCache {

    private static Cache<String, User> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(7, TimeUnit.DAYS)
            .maximumSize(1000)
            .build();

    public static User getCache(String token) {
        return cache.getIfPresent(token);
    }

    public static void setCache(String token, User user) {
        cache.put(token, user);
    }


}
