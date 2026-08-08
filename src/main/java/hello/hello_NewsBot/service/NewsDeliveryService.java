package hello.hello_NewsBot.service;

import hello.hello_NewsBot.config.FeedConfig;
import hello.hello_NewsBot.domain.Article;
import hello.hello_NewsBot.domain.SentArticle;
import hello.hello_NewsBot.domain.User;
import hello.hello_NewsBot.repository.SentArticleRepository;
import hello.hello_NewsBot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// 전체 발송 흐름을 조율.
// 예전엔 "고정 피드 목록 전체 -> 한 곳(chat-id)으로 발송"이었지만,
// 이제는 "텔레그램 연결이 끝난 유저마다 -> 각자 구독한 피드만 -> 각자의 chatId로" 발송한다.
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsDeliveryService {

    private final NewsFetchService newsFetchService;
    private final TelegramService telegramService;
    private final SentArticleRepository sentArticleRepository;
    private final UserRepository userRepository;
    private final FeedSubscriptionService feedSubscriptionService;
    private final FeedConfig feedConfig;

    public void deliverDailyNews() {
        List<User> connectedUsers = userRepository.findByTelegramChatIdIsNotNull();
        if (connectedUsers.isEmpty()) {
            log.info("텔레그램 연결이 완료된 사용자가 없습니다. 발송 생략.");
            return;
        }

        // 피드 이름 -> 설정(url 등) 을 빠르게 찾기 위한 조회용 맵
        Map<String, FeedConfig.Feed> feedsByName = new HashMap<>();
        feedConfig.getFeeds().forEach(feed -> feedsByName.put(feed.getName(), feed));

        // 같은 피드를 여러 유저가 구독할 수 있으므로, 피드별로 한 번만 가져오도록 캐싱
        Map<String, List<Article>> articleCache = new HashMap<>();

        int sentUserCount = 0;
        for (User user : connectedUsers) {
            sentUserCount += deliverToUser(user, feedsByName, articleCache) ? 1 : 0;
        }

        log.info("{}명에게 발송 완료", sentUserCount);
    }

    // 한 명의 유저에게: 구독 피드 필터링 -> 새 기사만 추리기 -> 발송 -> 이력 저장
    private boolean deliverToUser(User user, Map<String, FeedConfig.Feed> feedsByName,
                                   Map<String, List<Article>> articleCache) {
        List<String> subscribedFeedNames = feedSubscriptionService.getSubscribedFeedNames(user);
        if (subscribedFeedNames.isEmpty()) {
            return false; // 구독한 언론사가 하나도 없으면 보낼 게 없음
        }

        List<Article> candidates = subscribedFeedNames.stream()
                .map(feedsByName::get)
                .filter(Objects::nonNull) // yml 에서 이미 삭제된 피드를 구독 중인 경우 무시
                .flatMap(feed -> articleCache
                        .computeIfAbsent(feed.getName(), name -> newsFetchService.fetchArticles(feed))
                        .stream())
                .toList();

        List<Article> newArticles = candidates.stream()
                .filter(a -> !sentArticleRepository.existsByUserAndLink(user, a.getLink())) // 이 유저 기준 중복검사
                .toList();

        if (newArticles.isEmpty()) {
            return false;
        }

        telegramService.send(user.getTelegramChatId(), buildMessage(newArticles));

        // 발송 성공 후에만 기록 (실패 시 다음 실행에서 재시도되도록)
        newArticles.forEach(a ->
                sentArticleRepository.save(new SentArticle(null, user, a.getLink(), LocalDateTime.now()))
        );

        return true;
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
