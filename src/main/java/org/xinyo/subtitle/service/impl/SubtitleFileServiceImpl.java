package org.xinyo.subtitle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xinyo.subtitle.entity.SubtitleFile;
import org.xinyo.subtitle.mapper.SubtitleFileMapper;
import org.xinyo.subtitle.service.SubtitleFileService;
import org.xinyo.subtitle.task.SubtitleFileThread;
import org.xinyo.subtitle.task.SubtitleFileThreadPool;

import java.util.List;

/**
 * @author CHENG
 */
@Service
public class SubtitleFileServiceImpl extends ServiceImpl<SubtitleFileMapper, SubtitleFile> implements SubtitleFileService {
    @Value("${custom.basePath}")
    private String basePath;

    @Override
    public List<SubtitleFile> listBySubtitleId(String subtitleId) {
        QueryWrapper<SubtitleFile> wrapper = new QueryWrapper<>();
        wrapper.eq("subtitle_id", subtitleId);
        wrapper.orderByAsc("file_index");
        List<SubtitleFile> list = super.list(wrapper);

        if (list == null || list.isEmpty()) {
            // 文件为空，重新生成
            SubtitleFileThreadPool.getInstance().submitTask(new SubtitleFileThread(subtitleId, basePath));
        }

        return list;
    }

}
