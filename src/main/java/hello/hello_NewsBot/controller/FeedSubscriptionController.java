package hello.hello_NewsBot.controller;

import hello.hello_NewsBot.config.FeedConfig;
import hello.hello_NewsBot.domain.User;
import hello.hello_NewsBot.repository.UserRepository;
import hello.hello_NewsBot.service.FeedSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 언론사 후보 목록 조회 및 사용자별 구독 설정을 다루는 엔드포인트.
// 지금은 로그인/세션이 없어 userId 를 경로로 직접 받는다 — 인증 붙이면 세션에서 꺼내는 방식으로 바뀔 부분.
@RestController
@RequiredArgsConstructor
public class FeedSubscriptionController {

    private final FeedSubscriptionService feedSubscriptionService;
    private final UserRepository userRepository;

    // 선택 가능한 언론사 후보 전체 (yml 기반)
    @GetMapping("/feeds")
    public List<FeedConfig.Feed> listAvailableFeeds() {
        return feedSubscriptionService.listAvailableFeeds();
    }

    // 특정 유저가 현재 구독 중인 언론사 이름 목록
    @GetMapping("/users/{userId}/feeds")
    public List<String> getMySubscriptions(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자: " + userId));
        return feedSubscriptionService.getSubscribedFeedNames(user);
    }

    // 구독 목록을 통째로 교체 (예: ["전자신문 IT", "AI타임스"])
    @PutMapping("/users/{userId}/feeds")
    public void updateMySubscriptions(@PathVariable Long userId, @RequestBody List<String> feedNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자: " + userId));
        feedSubscriptionService.updateSubscriptions(user, feedNames);
    }
}
