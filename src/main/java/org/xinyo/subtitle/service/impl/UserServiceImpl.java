package org.xinyo.subtitle.service.impl;

import org.xinyo.subtitle.entity.auth.User;
import org.xinyo.subtitle.mapper.UserMapper;
import org.xinyo.subtitle.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author CHENG
 * @since 2020-01-03
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
