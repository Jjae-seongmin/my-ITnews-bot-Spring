package hello.hello_NewsBot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

// 텔레그램 웹훅 JSON 중 "message" 부분.
// 사용자가 봇에게 보낸 메시지 하나(예: "/start abcd1234")를 담고 있다.
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramMessage {
    private String text;       // 사용자가 입력한 메시지 본문
    private TelegramChat chat; // 이 메시지를 보낸 채팅방(=사용자) 정보
}
