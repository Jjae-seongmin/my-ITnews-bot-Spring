package hello.hello_NewsBot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// "어떤 User 가 어떤 언론사(피드)를 구독하는지"를 나타내는 매핑 엔티티.
// 언론사 목록 자체는 여전히 application.yml(FeedConfig)의 후보 목록을 사용하고,
// 여기서는 그 후보 중 사용자가 고른 것의 이름만 저장한다 (User 와 다대일 관계).
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserFeedSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // 이 구독의 주인

    private String feedName; // FeedConfig.Feed.name 과 매칭되는 언론사 이름 (예: "전자신문 IT")
}
