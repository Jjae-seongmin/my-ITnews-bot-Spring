package hello.hello_NewsBot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

// sent.json 구조
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SentArticle {

    @Id
    private String link;   // 링크를 PK로 사용 (기사마다 고유하므로)

    private LocalDateTime sentAt;
}