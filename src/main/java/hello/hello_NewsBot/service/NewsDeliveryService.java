package hello.hello_NewsBot.service;

import hello.hello_NewsBot.domain.Article;
import hello.hello_NewsBot.domain.SentArticle;
import hello.hello_NewsBot.repository.SentArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsDeliveryService {

    private final NewsFetchService newsFetchService;
    private final TelegramService telegramService;
    private final SentArticleRepository sentArticleRepository;

    public void deliverDailyNews() {
        // source, title, link
        List<Article> fetched = newsFetchService.fetchAllArticles();

        List<Article> newArticles = fetched.stream()
                .filter(a -> !sentArticleRepository.existsById(a.getLink())) // 중복검사
                .toList();

        if (newArticles.isEmpty()) {
            log.info("새 기사가 없습니다. 발송 생략.");
            return;
        }

        telegramService.send(buildMessage(newArticles));

        // 발송 성공 후에 기록(DB 저장) (Python 버전과 동일한 순서 원칙)
        newArticles.forEach(a ->
                sentArticleRepository.save(new SentArticle(a.getLink(), LocalDateTime.now()))
        );

        log.info("{}건 발송 완료", newArticles.size());
    }

    private String buildMessage(List<Article> articles) {
        StringBuilder sb = new StringBuilder("📰 <b>오늘의 IT 뉴스</b>\n\n");
        for (Article a : articles) {
            String safeTitle = HtmlUtils.htmlEscape(a.getTitle());
            sb.append("• [").append(a.getSource()).append("] ")
                    .append("<a href=\"").append(a.getLink()).append("\">")
                    .append(safeTitle).append("</a>\n");
        }
        return sb.toString();
    }
}