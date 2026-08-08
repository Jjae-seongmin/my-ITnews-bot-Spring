package hello.hello_NewsBot.service;

import hello.hello_NewsBot.config.FeedConfig;
import hello.hello_NewsBot.domain.User;
import hello.hello_NewsBot.domain.UserFeedSubscription;
import hello.hello_NewsBot.repository.UserFeedSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 사용자의 피드 구독을 관리하는 서비스.
// "고를 수 있는 언론사 목록"은 FeedConfig(yml) 를 그대로 보여주고,
// "실제로 고른 것"만 UserFeedSubscription 으로 저장한다.
@Service
@RequiredArgsConstructor
public class FeedSubscriptionService {

    private final FeedConfig feedConfig;
    private final UserFeedSubscriptionRepository subscriptionRepository;

    // 웹 화면에서 선택 가능한 언론사 후보 목록을 그대로 반환
    public List<FeedConfig.Feed> listAvailableFeeds() {
        return feedConfig.getFeeds();
    }

    // 사용자가 선택한 언론사 이름 목록으로 구독을 통째로 교체한다.
    // (기존 구독을 전부 지우고 새로 저장 — "부분 추가/삭제"보다 단순하고 프론트 구현이 쉬움)
    @Transactional
    public void updateSubscriptions(User user, List<String> feedNames) {
        subscriptionRepository.deleteByUser(user);
        feedNames.forEach(feedName ->
                subscriptionRepository.save(new UserFeedSubscription(null, user, feedName))
        );
    }

    // 특정 유저가 구독 중인 언론사 이름 목록 조회
    public List<String> getSubscribedFeedNames(User user) {
        return subscriptionRepository.findByUser(user).stream()
                .map(UserFeedSubscription::getFeedName)
                .toList();
    }
}
