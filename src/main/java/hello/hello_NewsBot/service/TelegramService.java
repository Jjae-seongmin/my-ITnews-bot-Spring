package hello.hello_NewsBot.service;

import hello.hello_NewsBot.config.TelegramConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramService {

    private final TelegramConfig telegramConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    public void send(String text) {
        String url = "https://api.telegram.org/bot" + telegramConfig.getBotToken() + "/sendMessage";

        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", telegramConfig.getChatId());
        body.put("text", text);
        body.put("parse_mode", "HTML");
        body.put("disable_web_page_preview", true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.postForObject(url, request, String.class); //requests.post

        log.info("텔레그램 발송 완료");
    }
}