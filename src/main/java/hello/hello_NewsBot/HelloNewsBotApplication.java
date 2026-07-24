package hello.hello_NewsBot;

import hello.hello_NewsBot.service.NewsDeliveryService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HelloNewsBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelloNewsBotApplication.class, args);
	}
}