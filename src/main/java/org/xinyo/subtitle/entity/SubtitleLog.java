package org.xinyo.subtitle.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("subtitle_log")
public class SubtitleLog {
    @TableId
    private String subjectId;
    private LocalDateTime updateTime;

    public SubtitleLog(String subjectId) {
        this.subjectId = subjectId;
        this.updateTime = LocalDateTime.now();
    }
}
