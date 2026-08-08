package hello.hello_NewsBot.controller;

import hello.hello_NewsBot.dto.TelegramUpdate;
import hello.hello_NewsBot.service.TelegramLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// 텔레그램이 사용자와의 상호작용(메시지 전송 등)을 대신 전달해주는 웹훅 수신 엔드포인트.
// 이 URL 을 텔레그램 서버에 등록(setWebhook)해두어야 실제로 호출이 들어온다.
@RestController
@RequiredArgsConstructor
public class TelegramWebhookController {

    private final TelegramLinkService telegramLinkService;

    @PostMapping("/telegram/webhook")
    public void receiveUpdate(@RequestBody TelegramUpdate update) {
        telegramLinkService.handleIncomingMessage(update);
    }
}
