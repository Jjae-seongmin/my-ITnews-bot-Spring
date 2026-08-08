package hello.hello_NewsBot.controller;

import hello.hello_NewsBot.config.TelegramConfig;
import hello.hello_NewsBot.domain.User;
import hello.hello_NewsBot.dto.LinkStartResponse;
import hello.hello_NewsBot.dto.LinkStatusResponse;
import hello.hello_NewsBot.repository.UserRepository;
import hello.hello_NewsBot.service.TelegramLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 사용자가 웹사이트에서 텔레그램 연결을 "시작"하고, 연결 완료 여부를 확인하는 진입점.
@RestController
@RequiredArgsConstructor
public class LinkController {

    private final TelegramLinkService telegramLinkService;
    private final TelegramConfig telegramConfig;
    private final UserRepository userRepository;

    // "텔레그램으로 시작하기" 버튼을 누르면 호출.
    // User 를 미리 만들고 linkCode 를 발급한 뒤, 봇 채팅방으로 바로 이동하는 딥링크를 함께 내려준다.
    @PostMapping("/link/start")
    public LinkStartResponse startLink() {
        User user = telegramLinkService.startLink();
        String telegramUrl = "https://t.me/" + telegramConfig.getBotUsername() + "?start=" + user.getLinkCode();
        return new LinkStartResponse(user.getLinkCode(), telegramUrl);
    }

    // 프론트가 "아직 연결 안 됐나?"를 주기적으로 확인(폴링)할 때 호출.
    // 사용자가 텔레그램 앱에서 /start 를 언제 누를지 서버는 알 수 없으므로,
    // 웹훅(TelegramWebhookController)이 telegramChatId 를 채워 넣을 때까지 프론트가 이 값을 반복 조회한다.
    @GetMapping("/link/status")
    public LinkStatusResponse checkStatus(@RequestParam String linkCode) {
        return userRepository.findByLinkCode(linkCode)
                .map(user -> new LinkStatusResponse(
                        user.getTelegramChatId() != null,
                        user.getTelegramChatId() != null ? user.getId() : null))
                .orElse(new LinkStatusResponse(false, null));
    }
}
