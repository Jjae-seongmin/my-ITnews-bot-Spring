package hello.hello_NewsBot.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SAXBuilder;
import com.rometools.rome.io.SyndFeedInput;
import hello.hello_NewsBot.config.FeedConfig;
import hello.hello_NewsBot.domain.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdom2.Document;
import org.springframework.stereotype.Service;
import org.jdom2.input.sax.XMLReaders;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// fetch 해오기.
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsFetchService {

    private final FeedConfig feedConfig;

    // 예외처리
    public List<Article> fetchAllArticles() {
        List<Article> articles = new ArrayList<>();

        for (FeedConfig.Feed feed : feedConfig.getFeeds()) {
            try {
                articles.addAll(fetchFromSingleFeed(feed));
            } catch (Exception e) {
                log.warn("[{}] 피드를 가져오지 못했습니다: {}", feed.getName(), e.getMessage());
            }
        }
        return articles;
    }

    private List<Article> fetchFromSingleFeed(FeedConfig.Feed feed) throws Exception {
        List<Article> result = new ArrayList<>();

//        URL url = new URL(feed.getUrl());
//        SyndFeedInput input = new SyndFeedInput();
//        SyndFeed syndFeed = input.build(new XmlReader(url));

        SyndFeed syndFeed = parseFeed(feed.getUrl()); // feedparser.parse(url)

        List<SyndEntry> entries = syndFeed.getEntries(); // 'source', 'title', 'link'
        int limit = Math.min(entries.size(), feedConfig.getCountPerFeed());

        for (int i = 0; i < limit; i++) {
            SyndEntry entry = entries.get(i);
            result.add(new Article(  // 'source', 'title', 'link'
                    feed.getName(),
                    entry.getTitle().trim(),
                    entry.getLink()
            ));
        }
        return result;
    }

    /**
     * DOCTYPE 선언이 포함된 RSS(전자신문 등)를 읽기 위해
     */

    /**
     * 일부 국내 언론사(전자신문 등)의 RSS는 DOCTYPE 선언 문법이 표준을 살짝 벗어나
     * (publicId-systemId 사이 공백 누락 등) 그대로 파싱하면 실패한다.
     * RSS 파싱에 DOCTYPE 정보 자체는 필요 없으므로, 파싱 전에 통째로 제거한다.
     */
    private SyndFeed parseFeed(String urlString) throws Exception {
        String xml = fetchRawXml(urlString);

        String cleaned = xml.replaceFirst("(?is)<!DOCTYPE[^>]*>", "");

        SAXBuilder saxBuilder = new SAXBuilder(XMLReaders.NONVALIDATING);
        saxBuilder.setFeature("http://xml.org/sax/features/external-general-entities", false);
        saxBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        Document document = saxBuilder.build(new StringReader(cleaned));
        SyndFeedInput input = new SyndFeedInput();
        return input.build(document);
    }

    private String fetchRawXml(String urlString) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (InputStream inputStream = new URL(urlString).openStream();
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }
}

