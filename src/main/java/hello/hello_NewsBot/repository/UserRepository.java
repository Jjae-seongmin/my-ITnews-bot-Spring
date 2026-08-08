package hello.hello_NewsBot.repository;

import hello.hello_NewsBot.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// User 엔티티를 DB에서 조회/저장하는 통로.
public interface UserRepository extends JpaRepository<User, Long> {

    // 텔레그램 /start 메시지로 들어온 linkCode 로 어떤 User 가 연결을 시도하는 중인지 찾을 때 사용.
    Optional<User> findByLinkCode(String linkCode);

    // 텔레그램 연결까지 끝난 사용자만 조회 — 스케줄러가 발송 대상을 정할 때 사용.
    List<User> findByTelegramChatIdIsNotNull();
}
