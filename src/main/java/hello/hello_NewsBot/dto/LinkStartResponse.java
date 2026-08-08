package hello.hello_NewsBot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// "/link/start" 응답으로 프론트에 내려주는 정보.
// telegramUrl 을 그대로 버튼 링크로 쓰면 사용자가 눌렀을 때 봇 채팅방으로 이동하며 /start 가 자동 전송된다.
@Getter
@AllArgsConstructor
public class LinkStartResponse {
    private String linkCode;
    private String telegramUrl;
}
