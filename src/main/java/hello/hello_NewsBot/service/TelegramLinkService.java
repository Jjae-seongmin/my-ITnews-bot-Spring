package hello.hello_NewsBot.service;

import hello.hello_NewsBot.domain.User;
import hello.hello_NewsBot.dto.TelegramUpdate;
import hello.hello_NewsBot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

// 웹 <-> 텔레그램 계정 연결을 담당하는 서비스.
// 1) 웹에서 연결을 시작하면 임시 linkCode 를 가진 User 를 만들고 (startLink)
// 2) 사용자가 텔레그램에서 "/start <linkCode>" 를 보내면 그 코드로 User 를 찾아 chatId 를 채워 넣는다 (handleIncomingMessage)
@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramLinkService {

    private final UserRepository userRepository;

    // 웹사이트에서 "텔레그램으로 시작하기"를 눌렀을 때 호출.
    // 아직 텔레그램과 연결되지 않은 User 를 미리 만들어두고 linkCode 를 발급한다.
    public User startLink() {
        User user = new User();
        user.setLinkCode(UUID.randomUUID().toString());
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    // 텔레그램 웹훅으로 들어온 메시지를 처리.
    // "/start <linkCode>" 형태인 경우에만 연결을 시도하고, 그 외 메시지는 무시한다.
    public void handleIncomingMessage(TelegramUpdate update) {
        if (update.getMessage() == null || update.getMessage().getChat() == null) {
            return; // 메시지가 아닌 이벤트(버튼 클릭 등)는 처리하지 않음
        }

        String text = update.getMessage().getText();
        if (text == null || !text.startsWith("/start")) {
            return;
        }

        String linkCode = text.replace("/start", "").trim();
        if (linkCode.isEmpty()) {
            return;
        }

        userRepository.findByLinkCode(linkCode).ifPresentOrElse(user -> {
            Long chatId = update.getMessage().getChat().getId();
            user.setTelegramChatId(String.valueOf(chatId));
            userRepository.save(user);
            log.info("사용자(id={}) 텔레그램 연결 완료 (chatId={})", user.getId(), chatId);
        }, () -> log.warn("알 수 없는 연결 코드로 /start 요청이 들어옴: {}", linkCode));
    }
}
