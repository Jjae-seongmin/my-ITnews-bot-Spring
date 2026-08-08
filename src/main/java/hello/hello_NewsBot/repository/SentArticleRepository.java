package hello.hello_NewsBot.repository;

import hello.hello_NewsBot.domain.SentArticle;
import hello.hello_NewsBot.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

// 유저별 발송 이력을 조회/저장하는 통로.
public interface SentArticleRepository extends JpaRepository<SentArticle, Long> {

    // 이 유저에게 이 링크가 이미 발송됐는지 — 발송 전 중복검사에 사용
    boolean existsByUserAndLink(User user, String link);
}
