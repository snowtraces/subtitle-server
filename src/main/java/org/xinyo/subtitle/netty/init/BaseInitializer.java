package org.xinyo.subtitle.netty.init;

import java.util.ArrayList;
import java.util.List;

public class BaseInitializer {
    List<Class> controllerList = new ArrayList<>();

    public BaseInitializer addController(Class clazz) {
        controllerList.add(clazz);
        return this;
    }

    public void init() {
        ControllerInitializer controllerInitializer = new ControllerInitializer();
        controllerInitializer.add(controllerList).init();
    }
}
