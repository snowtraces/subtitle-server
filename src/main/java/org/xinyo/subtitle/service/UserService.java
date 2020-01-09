package org.xinyo.subtitle.service;

import org.xinyo.subtitle.entity.PageParams;
import org.xinyo.subtitle.entity.auth.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.xinyo.subtitle.entity.vo.Resp;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author CHENG
 * @since 2020-01-03
 */
public interface UserService extends IService<User> {

    User getUser(User user);

    Resp addUser(User user);

    Resp list4Page(User user, PageParams pageParams);

    Resp changeStatus(User user);
}
