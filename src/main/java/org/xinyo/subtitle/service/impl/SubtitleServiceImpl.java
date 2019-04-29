package org.xinyo.subtitle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Strings;
import org.springframework.stereotype.Service;
import org.xinyo.subtitle.entity.SRTSubtitleUnit;
import org.xinyo.subtitle.entity.Subtitle;
import org.xinyo.subtitle.mapper.SubtitleMapper;
import org.xinyo.subtitle.service.SubtitleService;

import java.util.ArrayList;
import java.util.List;

@Service
public class SubtitleServiceImpl extends ServiceImpl<SubtitleMapper, Subtitle> implements SubtitleService {
    String numberRegex = "^\\d+$";
    String timeRegex = "(^\\d{2}:\\d{2}:\\d{2},\\d{3})[^0-9]+(\\d{2}:\\d{2}:\\d{2},\\d{3}$)";

    @Override
    public List<SRTSubtitleUnit> readSubtitle(List<String> lines) {

        List<SRTSubtitleUnit> list = new ArrayList<>();

        boolean isBegin = true;
        SRTSubtitleUnit unit = null;
        for (String line : lines) {

            // 1. 新建单元
            if (isBegin && !Strings.isNullOrEmpty(line)) {
                unit = new SRTSubtitleUnit();
            }

            line = line.trim();
            // 2. 存入数据
            if ((line.matches(numberRegex) || line.startsWith("\uFEFF")) && isBegin) {
                // a. 数字
                unit.setNumber(Integer.valueOf(line.replaceAll("\uFEFF", "")));
            } else if (line.matches(timeRegex)) {
                // b. 时间
                int[] time = buildTime(line);
                unit.setStart(time[0]);
                unit.setEnd(time[1]);
            } else {
                // 3. 文本
                if (!Strings.isNullOrEmpty(line)) {
                    unit.getText().add(line);
                }
            }

            if (Strings.isNullOrEmpty(line)) {
                list.add(unit);
                isBegin = true;
            } else {
                isBegin = false;
            }
        }

        return list;
    }

    @Override
    public boolean add(Subtitle subtitle) {
        return super.save(subtitle);
    }

    @Override
    public List<Subtitle> getBySubjectId(String id) {
        QueryWrapper<Subtitle> wrapper = new QueryWrapper<>();
        wrapper.eq("subject_id", id);
        return super.list(wrapper);
    }

    @Override
    public List<Subtitle> listBySubjectId(String id) {
        QueryWrapper<Subtitle> wrapper = new QueryWrapper<>();
        wrapper.eq("subject_id", id);

        return super.list(wrapper);
    }

    @Override
    public List<Subtitle> listAll() {
        QueryWrapper<Subtitle> wrapper = new QueryWrapper<>();
        wrapper.select("source_id", "subject_id");
        return super.list(wrapper);
    }

    @Override
    public String getSubjectIdById(String subtitleId) {
        QueryWrapper<Subtitle> wrapper = new QueryWrapper<>();
        wrapper.select("subject_id");
        wrapper.eq("id", subtitleId);
        wrapper.last("limit 1");

        Subtitle one = super.getOne(wrapper);

        return one == null ? null : one.getSubjectId();
    }

    private int[] buildTime(String line){

        line = line.replaceAll(timeRegex, "$1#$2");
        String[] split = line.split("#");
        String startTime = split[0];
        String endTime = split[1];

        int start = calcTime(startTime);
        int end = calcTime(endTime);

        return new int[]{start, end};
    }

    private int calcTime(String time){
        String[] split = time.split("[:,]");
        int hour = Integer.parseInt(split[0]);
        int min = Integer.parseInt(split[1]);
        int sec = Integer.parseInt(split[2]);
        int mill = Integer.parseInt(split[3]);

        int t = hour * 60 * 60 * 1000 + min * 60 * 1000 + sec * 1000 + mill ;
        return t;
    }
}
