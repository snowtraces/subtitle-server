package org.xinyo.subtitle.controller;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xinyo.subtitle.entity.auth.User;
import org.xinyo.subtitle.entity.vo.Resp;
import org.xinyo.subtitle.netty.annotation.RestMapping;
import org.xinyo.subtitle.service.UserService;
import org.xinyo.subtitle.util.TokenCache;

import java.util.UUID;

/**
 * @author CHENG
 */
@Component
public class AuthController {

    @Autowired
    private UserService userService;

    @RestMapping("/api/login")
    public Object login(User user) {
        // 1. 参数验证
        if (user == null
                || Strings.isNullOrEmpty(user.getName())
                || Strings.isNullOrEmpty(user.getPassword())) {
            return Resp.failure("参数不能为空");
        }

        // 2. 查询用户
        User existUser = userService.getUser(user);
        if (existUser == null) {
            return Resp.failure("账号或密码错误");
        } else {
            String token = UUID.randomUUID().toString();
            TokenCache.setCache(token, existUser);
            return Resp.success(token, "登录成功");
        }
    }

    @RestMapping("/api/autoLogin")
    public Object autoLogin(String token) {
        if (Strings.isNullOrEmpty(token)) {
            return Resp.failure("");
        }

        User cacheUser = TokenCache.getCache(token);
        if (cacheUser != null) {
            return Resp.success(token, "登录成功");
        }

        return Resp.failure("");
    }

    @RestMapping("/api/signUp")
    public Object sigUp(User user) {
        // 1. 参数验证
        if (user == null
                || Strings.isNullOrEmpty(user.getName())
                || Strings.isNullOrEmpty(user.getPassword())) {
            return Resp.failure("参数不能为空");
        }

        // 2. 新增用户
        return userService.addUser(user);
    }

}
