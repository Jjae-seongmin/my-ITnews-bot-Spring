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
    private String chatId;
}