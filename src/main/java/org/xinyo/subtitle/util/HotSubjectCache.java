package org.xinyo.subtitle.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.service.SubjectService;

import javax.activation.UnsupportedDataTypeException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HotSubjectCache {

    private SubjectService subjectService = SpringContextHolder.getBean(SubjectService.class);

    private static class HotSubjectCacheHolder {
        private static final HotSubjectCache INSTANCE = new HotSubjectCache();
    }

    private HotSubjectCache() {
    }

    public static final HotSubjectCache getInstance() {
        return HotSubjectCacheHolder.INSTANCE;
    }

    private volatile int duration = 10;
    private volatile TimeUnit timeUnit = TimeUnit.MINUTES;

    private volatile LoadingCache<Long, List<Subject>> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(this.duration, this.timeUnit)
            .build(new CacheLoader<Long, List<Subject>>() {
                @Override
                public List<Subject> load(Long o) {
                    return new ArrayList<>();
                }
            });


    public List<Subject> getHotList() {
        try {
            long key = calcCurrKey();
            List<Subject> subjects = cache.get(key);
            if (subjects.size() == 0) {
                synchronized (cache) {
                    subjects = cache.get(key);
                    if (subjects.size() == 0) {
                        subjects = subjectService.listHot();
                        cache.put(key, subjects);
                    }
                }
            }
            return subjects;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private long calcCurrKey() throws UnsupportedDataTypeException {
        long countKey;
        switch (timeUnit) {
            case SECONDS:
                countKey = System.currentTimeMillis() / duration / 1000;
                break;
            case MINUTES:
                countKey = System.currentTimeMillis() / duration / 1000 / 60;
                break;
            case HOURS:
                countKey = System.currentTimeMillis() / duration / 1000 / 60 / 60;
                break;
            case DAYS:
                countKey = System.currentTimeMillis() / duration / 1000 / 60 / 60 / 24;
                break;
            default:
                throw new UnsupportedDataTypeException();
        }
        return countKey;
    }


    public static void main(String[] args) throws InterruptedException {

    }

}
