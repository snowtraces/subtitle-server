package org.xinyo.subtitle.entity.douban;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("douban_update_log")
public class UpdateLog {
    @TableId
    private String subjectId;
    private LocalDateTime baseUpdateTime;
    private LocalDateTime posterUpdateTime;
}
