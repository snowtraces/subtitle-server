package org.xinyo.subtitle;

import org.junit.Test;
import org.xinyo.subtitle.netty.init.ControllerInitializer;

public class PerformanceTest {

    @Test
    public void regexTime() {
        String path = "api/movie/10441575/";

        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            String shortPath = path.replaceAll("^(.*)/[^/]+/?", "$1");
            String value = path.replaceAll("^.*/([^/]+)/?", "$1");
        }

        long t2 = System.currentTimeMillis();
        System.err.println("t2 - t1: " + (t2 - t1));
    }

    @Test
    public void subStringTime() {
        String path = "api/movie/10441575/";

        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            int index = path.lastIndexOf("/");
            String shortPath = path.substring(0, index);
            String value = path.substring(index + 1);
        }

        long t2 = System.currentTimeMillis();
        System.err.println("t2 - t1: " + (t2 - t1));
    }
}
