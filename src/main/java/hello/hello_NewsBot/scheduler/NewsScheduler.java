package hello.hello_NewsBot.scheduler;

import hello.hello_NewsBot.service.NewsDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsScheduler {

    private final NewsDeliveryService newsDeliveryService;

    // 초 분 시 일 월 요일 순서. 매일 오전 8시 (서버의 시스템 시간대 기준)
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    public void sendDailyNews() {
        log.info("=== 스케줄 실행: 뉴스 발송 시작 ===");
        newsDeliveryService.deliverDailyNews();
    }
}