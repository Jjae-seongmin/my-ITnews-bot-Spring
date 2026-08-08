package hello.hello_NewsBot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

// 텔레그램 웹훅 JSON 중 "message.chat" 부분.
// 여기서 뽑아내는 id 가 곧 User.telegramChatId 로 저장될 값이다.
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) // 텔레그램이 실제로는 훨씬 많은 필드를 보내지만 필요한 것만 매핑
public class TelegramChat {
    private Long id;
}
