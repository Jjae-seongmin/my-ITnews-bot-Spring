package hello.hello_NewsBot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// "/link/status" 응답 — 프론트가 "텔레그램 연결이 끝났는지"를 주기적으로 물어볼 때 사용.
// 웹훅은 비동기로 들어오기 때문에(사용자가 언제 텔레그램에서 /start 를 누를지 알 수 없음),
// 프론트에서 이 값을 폴링해서 connected 가 true 가 되면 다음 화면(피드 선택)으로 넘어간다.
@Getter
@AllArgsConstructor
public class LinkStatusResponse {
    private boolean connected;
    private Long userId; // 연결이 끝났을 때만 의미 있는 값 (연결 전엔 null)
}
