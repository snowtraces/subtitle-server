package org.xinyo.subtitle.netty.init;

import java.util.ArrayList;
import java.util.List;

public class BaseInitializer {
    private final List<Class> controllerList = new ArrayList<>();

    public void addController(Class clazz) {
        controllerList.add(clazz);
    }

    public void init() {
        ControllerInitializer controllerInitializer = new ControllerInitializer();
        controllerInitializer.add(controllerList);
        controllerInitializer.init();
    }
}
