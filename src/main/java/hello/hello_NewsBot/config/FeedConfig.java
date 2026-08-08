package hello.hello_NewsBot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

// yml 의 리스트를 자바 객체로 매핑
@Component
@ConfigurationProperties(prefix = "news") // 얘가 가져옴.
@Getter
@Setter
public class FeedConfig {

    private List<Feed> feeds; // yml 의 전체피드
    private int countPerFeed;

    @Getter
    @Setter
    public static class Feed {
        private String name;
        private String url;
    }
}