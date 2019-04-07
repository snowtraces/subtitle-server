package org.xinyo.subtitle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.mapper.SubjectMapper;
import org.xinyo.subtitle.service.SubjectServcie;

@Service
public class SubjectServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements SubjectServcie {

    @Override
    public Subject getById(String id) {
        return super.getById(id);
    }
}
