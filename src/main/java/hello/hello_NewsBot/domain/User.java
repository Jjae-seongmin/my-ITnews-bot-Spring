package hello.hello_NewsBot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// 서비스를 이용하는 한 명의 사용자를 나타내는 엔티티.
// 앞으로 만들 "피드 구독", "발송 이력" 등은 전부 이 User 를 기준으로 연결된다.
// 테이블명을 "users"로 지정 — 클래스명 그대로(user)를 쓰면 H2 등 대부분의 DB에서 예약어와 충돌한다.
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 사용자 고유 번호 (DB가 자동 발급)

    private String linkCode;
    // 텔레그램 연결 전에 발급하는 임시 코드.
    // 사용자가 "t.me/봇이름?start=이코드" 링크를 눌러 봇에게 /start 를 보내면,
    // 이 코드를 통해 "웹에서 방문한 그 사람"과 "텔레그램에서 말 건 그 사람"을 매칭한다.

    private String telegramChatId;
    // 텔레그램 연결이 완료되면 채워지는 값 (연결 전에는 null).
    // 앞으로 뉴스를 보낼 때 이 chatId 를 수신자로 사용한다.

    private LocalDateTime createdAt; // 사용자가 처음 생성된 시각 (가입 시각)
}
