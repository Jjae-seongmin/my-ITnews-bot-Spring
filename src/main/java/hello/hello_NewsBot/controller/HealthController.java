package hello.hello_NewsBot.controller;

import hello.hello_NewsBot.service.NewsDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final NewsDeliveryService newsDeliveryService;

    @GetMapping("/")
    public String health() {
        return "OK";
    }

    @GetMapping("/trigger")
    public void triggerTest() {
        newsDeliveryService.deliverDailyNews();
    }
}