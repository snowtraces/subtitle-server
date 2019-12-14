package org.xinyo.subtitle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 字幕文件详情
 * @author CHENG
 */
@Data
@TableName("subtitle_file")
public class SubtitleFile {
    private String subtitleId;
    private Integer fileIndex;
    private String fileName;
    private String fileSize;
    private String content;
}
