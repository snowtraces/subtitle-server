package org.xinyo.subtitle.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.google.common.base.Strings;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.xinyo.subtitle.entity.vo.SubtitleVO;
import org.xinyo.subtitle.util.SnowFlake;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@TableName("subtitle")
public class Subtitle {
    @TableId
    private String id;
    private String title;
    private String subjectId;
    private String language; // [其他][德][法][韩][日][英][繁][简]
    private Integer type; // 1.翻译 2.官方 3.听译 4.机翻
    private String version;
    private String fileName;
    private String sourceFileName;
    private LocalDateTime createTime;
    private String remark;

    private String source; // 来源
    private String sourceId;
    private String currentEpisode;
    private String currentSeason;

    private long downloadTimes;
    private Integer weight;

    public Subtitle(){}
    public Subtitle(SubtitleVO subtitleVO) {
        BeanUtils.copyProperties(subtitleVO, this);

        this.setId(String.valueOf(SnowFlake.getId()));
        this.createTime = LocalDateTime.now();
        this.language = generateLanguage(subtitleVO.getLanguage());
        this.type = generateType(subtitleVO.getType());
    }

    private Integer generateType(String source){
        switch (source) {
            case("字幕翻译"):
                return 1;
            case("官方译本"):
                return 2;
            case("听译版本"):
                return 3;
            case("机翻字幕"):
                return 4;
        }
        return null;
    }


    private String generateLanguage(String source) {
        if (Strings.isNullOrEmpty(source)) {
            return null;
        }

        String[] split = source.split(",");
        List<String> collect = Arrays.stream(split)
                .map(String::trim)
                .filter(s -> !Strings.isNullOrEmpty(s))
                .map(s -> s.replaceAll("[体文本国]", ""))
                .collect(Collectors.toList());

        int[] lang = new int[10];
        collect.forEach(s -> {
            int i = lang2int(s);
            lang[i] = 1;
        });

        String result = Arrays.stream(lang)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();

        return result;
    }

    private int lang2int(String lang) {
        switch (lang) {
            case("简"):
                return 9;
            case("繁"):
                return 8;
            case("英"):
                return 7;
            case("日"):
                return 6;
            case("韩"):
                return 5;
            case("法"):
                return 4;
            case("德"):
                return 3;
            default:
                return 0;
        }
    }

    public static void main(String[] args) {
        int[] t = new int[5];
        System.err.println(t[0]);
    }

}
