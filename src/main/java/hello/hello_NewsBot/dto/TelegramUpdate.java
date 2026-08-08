package hello.hello_NewsBot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

// 텔레그램이 웹훅으로 보내오는 최상위 JSON 구조 (Update 객체).
// 사용자가 봇과 상호작용할 때마다(메시지 전송 등) 이 형태로 우리 서버에 POST 된다.
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramUpdate {
    private TelegramMessage message; // 메시지 이벤트가 아닌 경우(예: 버튼 클릭) null 일 수 있음
}
