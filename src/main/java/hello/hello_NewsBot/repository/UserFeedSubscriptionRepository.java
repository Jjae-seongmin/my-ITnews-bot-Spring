package hello.hello_NewsBot.repository;

import hello.hello_NewsBot.domain.User;
import hello.hello_NewsBot.domain.UserFeedSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// UserFeedSubscription(구독 매핑)을 DB에서 조회/저장하는 통로.
public interface UserFeedSubscriptionRepository extends JpaRepository<UserFeedSubscription, Long> {

    // 특정 유저가 구독 중인 피드 목록 조회 (스케줄러가 발송 대상 피드를 정할 때 사용)
    List<UserFeedSubscription> findByUser(User user);

    // 구독 설정을 갱신할 때, 기존 구독을 한 번에 지우기 위해 사용
    void deleteByUser(User user);
}
