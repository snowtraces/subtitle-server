package org.xinyo.subtitle.service.impl;

import org.xinyo.subtitle.entity.auth.Api;
import org.xinyo.subtitle.mapper.ApiMapper;
import org.xinyo.subtitle.service.ApiService;
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
public class ApiServiceImpl extends ServiceImpl<ApiMapper, Api> implements ApiService {

}
