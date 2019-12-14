package org.xinyo.subtitle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xinyo.subtitle.entity.SubtitleFile;

import java.util.List;

/**
 * @author CHENG
 */
public interface SubtitleFileService extends IService<SubtitleFile> {

    List<SubtitleFile> listBySubtitleId(String subtitleId);
}
