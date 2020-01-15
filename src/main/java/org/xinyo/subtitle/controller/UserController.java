package org.xinyo.subtitle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xinyo.subtitle.entity.PageParams;
import org.xinyo.subtitle.entity.auth.User;
import org.xinyo.subtitle.entity.vo.Resp;
import org.xinyo.subtitle.netty.annotation.RestMapping;
import org.xinyo.subtitle.service.UserService;

/**
 * @author CHENG
 */
@Component
public class UserController {

    @Autowired
    private UserService userService;

    @RestMapping("/api/user/list")
    public Object login(User user, PageParams pageParams) {
        Resp resp = userService.list4Page(user, pageParams);
        return resp;
    }

    @RestMapping("/api/user/changeStatus")
    public Object changeStatus(User user) {
        return userService.changeStatus(user);
    }

}
