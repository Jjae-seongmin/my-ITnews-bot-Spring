package hello.hello_NewsBot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// yml 의 "telegram" 을 자바 객체로 매핑
@Component
@ConfigurationProperties(prefix = "telegram")
@Getter
@Setter
public class TelegramConfig {
    private String botToken;
    private String botUsername; // 딥링크(t.me/봇이름?start=코드)를 만들 때 사용하는 봇 아이디(@ 제외)
}