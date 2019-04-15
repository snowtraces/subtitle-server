package org.xinyo.subtitle.service.impl;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xinyo.subtitle.entity.Subtitle;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.entity.vo.SubtitleVO;
import org.xinyo.subtitle.service.SpiderService;
import org.xinyo.subtitle.service.SubtitleService;
import org.xinyo.subtitle.util.FileUtils;
import org.xinyo.subtitle.util.RequestUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SpiderServiceImpl implements SpiderService {
    private static final String SUBTITLE_SEARCH_PATH = "http://subhd.com/search0/%s";
    private static final String SUBTITLE_PATH = "http://subhd.com/ar0/%s";

    @Autowired
    private SubtitleService subtitleService;

    @Override
    public void doCrawl(Subject subject) {

        // 1. 搜索字幕
        String searchPath = null;
        try {
            searchPath = String.format(SUBTITLE_SEARCH_PATH, URLEncoder.encode(subject.getTitle(), "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String movieText = RequestUtils.requestText(searchPath);

        Matcher matcher = Pattern.compile("<h4><a href=\"/ar0/(\\d+)\"").matcher(movieText);
        List<String> subList = new ArrayList<>();
        while (matcher.find()) {
            System.err.println(matcher.group());
            subList.add(matcher.group(1));
        }
        subList = subList.stream().distinct().collect(Collectors.toList());
        if (subList.size() == 0) {
            return;
        }

        // 2. 请求字幕页面
        List<SubtitleVO> list = new ArrayList<>();
        for (String subtitleId : subList) {
            String subPath = String.format(SUBTITLE_PATH, subtitleId);
            String subText = RequestUtils.requestText(subPath);

            SubtitleVO subtitleVO = new SubtitleVO();
            subtitleVO.setId(subtitleId);
            subtitleVO.setTitle(subject.getTitle());
            subtitleVO.setSubjectId(subject.getId());
            subtitleVO.setToken(extraAttr(subText, "dtoken=\"", "\""));

            Pattern compile = Pattern.compile("<div class=\"b\">字幕信息</div>[^`]*?</div>");
            Matcher infoMatcher = compile.matcher(subText);
            if (infoMatcher.find()) {
                String info = infoMatcher.group();
                subtitleVO.setLanguage(extraAttr(info, "语言：", "<br>"));
                subtitleVO.setType(extraAttr(info, "来源：", "<br>"));
                subtitleVO.setVersion(extraAttr(info, "字幕版本：", "<br>"));
            }

            System.err.println(subtitleVO);
            list.add(subtitleVO);
            break;
        }

        // 3. 请求字幕地址
        for (SubtitleVO subtitleVO : list) {
            String token = subtitleVO.getToken();
            String id = subtitleVO.getId();

            String url = "http://subhd.com/ajax/down_ajax";

            List<String> params = new ArrayList<>();
            params.add("sub_id:" + id );
            params.add("dtoken:" + token );

            List<String> headers = new ArrayList<>();
            headers.add("Host:subhd.com");
            headers.add("Origin:http://subhd.com");

            String s = RequestUtils.requestText(url, params, headers); // {"success":true,"url":"http:\/\/dl1.subhd.com\/sub\/2016\/05\/146418042012852.zip"}

            String downloadPath = s.replaceAll("^.*\"url\":\"([^\"]+)\"}$", "$1").replaceAll("\\\\", "");

            System.err.println("下载地址：" + downloadPath);

            if (Strings.isNullOrEmpty(s)) {
                return;
            }

            // 4. 下载保存文件
            String bathPath = "/Users/CHENG/CODE/Projects/subtitle-angular/src/assets/subtitles";
            List<String> idPath = FileUtils.separateString(subject.getId(), 1, 5);

            String path = FileUtils.createPosterPath(bathPath, idPath);
            RequestUtils.fetchBinary(downloadPath, path);

            // 5. 入库
            Subtitle subtitle = new Subtitle(subtitleVO);
            String fileName = downloadPath.substring(downloadPath.lastIndexOf("/") + 1);
            subtitle.setFileName(fileName);

            subtitleService.add(subtitle);
        }
    }

    private String extraAttr(String source, String start, String end) {
        Matcher matcher = Pattern.compile(start + "(.*?)" + end).matcher(source);
        if(matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static void main(String[] args) {
        SpiderServiceImpl spiderService = new SpiderServiceImpl();
        Subject subject = new Subject();
        subject.setTitle("杀死比尔");
        subject.setId("1291580");
        spiderService.doCrawl(subject);
    }
}
