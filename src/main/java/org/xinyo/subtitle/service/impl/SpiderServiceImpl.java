package org.xinyo.subtitle.service.impl;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xinyo.subtitle.entity.Subtitle;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.entity.vo.SubtitleVO;
import org.xinyo.subtitle.service.SpiderService;
import org.xinyo.subtitle.service.SubtitleService;
import org.xinyo.subtitle.util.BloomFilterUtils;
import org.xinyo.subtitle.util.FileUtils;
import org.xinyo.subtitle.util.RequestUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SpiderServiceImpl implements SpiderService {
    private static final String SUBTITLE_SEARCH_PATH = "http://subhd.com/search0/%s";
    private static final String SUBTITLE_PATH = "http://subhd.com/ar0/%s";
    private static final String MOVIE_PATH = "http://subhd.com/do0/%s";

    @Autowired
    private SubtitleService subtitleService;

    @Override
    public void doCrawl(Subject subject) {

        // 1. 搜索字幕
        List<String> subList = getSubList(subject);
        if (subList.size() == 0) {
            return;
        }

        for (String subtitleId : subList) {
            // 2. 请求字幕页面
            String subPath = String.format(SUBTITLE_PATH, subtitleId);
            String subText = RequestUtils.requestText(subPath);

            if (Strings.isNullOrEmpty(subText)) {
                subText = RequestUtils.requestText(subPath);
                if (Strings.isNullOrEmpty(subText)) {
                    continue;
                }
            }

            SubtitleVO subtitleVO = new SubtitleVO();
            subtitleVO.setSourceId(subtitleId);

            String title = extraAttr(subText, "<h2><div[^<]*</div>", "</h2>");
            if (Strings.isNullOrEmpty(title)) {
                title = extraAttr(subText, "<h1><div[^<]*</div>", "</h1>");
            }
            if (Strings.isNullOrEmpty(title)) {
                title = null;
            }
            subtitleVO.setTitle(title);
            subtitleVO.setSource("subhd");
            subtitleVO.setCurrentSeason(extraAttr(subText, "<div class=\"tvlabel\">S", "E\\d+</div>"));
            subtitleVO.setCurrentEpisode(extraAttr(subText, "<div class=\"tvlabel\">S\\d+E", "</div>"));

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

        // 3. 请求字幕地址
            String token = subtitleVO.getToken();
            String id = subtitleVO.getSourceId();

            String url = "http://subhd.com/ajax/down_ajax";

            List<String> params = new ArrayList<>();
            params.add("sub_id:" + id );
            params.add("dtoken:" + token );

            List<String> headers = new ArrayList<>();
            headers.add("Host:subhd.com");
            headers.add("Origin:http://subhd.com");

            String s = RequestUtils.requestText(url, params, headers); // {"success":true,"url":"http:\/\/dl1.subhd.com\/sub\/2016\/05\/146418042012852.zip"}

            if (Strings.isNullOrEmpty(s)) {
                // 自动重试一次
                s = RequestUtils.requestText(url, params, headers);
                if (Strings.isNullOrEmpty(s)) {
                    continue;
                }
            }

            HashMap<String, String> downloadMap = new Gson().fromJson(s, HashMap.class);
            String downloadPath = downloadMap.get("url");

            System.err.println("下载地址：" + downloadPath);


            // 4. 下载保存文件
            String bathPath = "/Users/CHENG/CODE/Projects/subtitle-angular/src/assets/subtitles";
            List<String> idPath = FileUtils.separateString(subject.getId(), 1, 5);

            String path = FileUtils.createPosterPath(bathPath, idPath);
            boolean isDownload = RequestUtils.fetchBinary(downloadPath, path);
            if (!isDownload) {
                isDownload = RequestUtils.fetchBinary(downloadPath, path);
                if (!isDownload) {
                    System.err.println("字幕下载失败……");
                    continue;
                }
            }

            // 5. 入库
            Subtitle subtitle = new Subtitle(subtitleVO);
            String fileName = downloadPath.substring(downloadPath.lastIndexOf("/") + 1);
            try {
                fileName = URLEncoder.encode(fileName, "utf8").replaceAll("\\+", "%20");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            subtitle.setFileName(fileName);

            subtitleService.add(subtitle);
        }
    }

    /**
     * 查询字幕id列表
     */
    private List<String> getSubList(Subject subject) {
        List<String> subList = new ArrayList<>();

        // 1. 直接访问
        String moviePath = String.format(MOVIE_PATH, subject.getId());
        String movieText = RequestUtils.requestText(moviePath);

        if (!Strings.isNullOrEmpty(movieText)) {
            Matcher matcher = Pattern.compile("=\"dt_edition\"><a href=\"/ar0/(\\d+)\"").matcher(movieText);
            while (matcher.find()) {
                subList.add(matcher.group(1));
            }
        }

        if (subList.size() < 10) {
            // 2. 添加搜索数据
            String searchPath = null;
            String keyword = subject.getTitle().length() < 5
                    ? (subject.getTitle() + " " + subject.getYear()) : subject.getTitle();
            try {
                searchPath = String.format(SUBTITLE_SEARCH_PATH,
                        URLEncoder.encode(keyword, "utf8").replaceAll("\\+", "%20"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            movieText = RequestUtils.requestText(searchPath);

            if (Strings.isNullOrEmpty(movieText)) {
                movieText = RequestUtils.requestText(searchPath);
                if (Strings.isNullOrEmpty(movieText)) {
                    return subList;
                }
            }

            Matcher matcher = Pattern.compile("=\"d_title\"><a href=\"/ar0/(\\d+)\"").matcher(movieText);
            while (matcher.find()) {
                subList.add(matcher.group(1));
            }
        }

        // 3. 去重
        subList = subList.stream()
                .distinct()
                .filter(s -> !BloomFilterUtils.mightContainSubtitle(s))
                .collect(Collectors.toList());
        subList.forEach(s -> {
            BloomFilterUtils.pushSubtitle(s);
            System.err.println("待下载字幕id：" + s);
        });

        return subList;
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
