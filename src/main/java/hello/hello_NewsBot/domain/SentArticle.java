package hello.hello_NewsBot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// "어떤 User 에게 어떤 기사가 이미 발송되었는지"를 기록.
// 예전에는 link 하나가 PK라서 전역으로 한 번 보낸 기사는 다시 안 보냈지만,
// 이제는 (user, link) 조합으로 유저마다 독립적인 발송 이력을 가진다.
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "link"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SentArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // 이 기사를 받은 사용자

    private String link; // 기사 링크 (같은 유저 안에서는 unique 제약으로 중복이 막힘)

    private LocalDateTime sentAt;
}
