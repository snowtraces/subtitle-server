package org.xinyo.subtitle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xinyo.subtitle.entity.auth.User;
import org.xinyo.subtitle.entity.vo.Resp;
import org.xinyo.subtitle.mapper.UserMapper;
import org.xinyo.subtitle.service.UserService;
import org.xinyo.subtitle.util.EncryptUtils;
import org.xinyo.subtitle.util.SnowFlake;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author CHENG
 * @since 2020-01-03
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User getUser(User user) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("name", user.getName());
        wrapper.eq("password", EncryptUtils.baseEnc(user.getPassword()));

        return super.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Resp addUser(User user) {
        boolean nameAvailable = checkNameAvailable(user.getName());
        if (!nameAvailable) {
            return Resp.failure("用户名已存在，请更换后重新尝试");
        }

        String password = user.getPassword();
        user.setId(String.valueOf(SnowFlake.getId()));
        user.setPassword(EncryptUtils.baseEnc(password));
        user.setStatus(0);

        super.save(user);
        return Resp.success(null, "注册成功，请前去登录");
    }

    private boolean checkNameAvailable(String name) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);

        List<User> list = super.list(wrapper);
        return list == null || list.size() == 0;
    }
}
