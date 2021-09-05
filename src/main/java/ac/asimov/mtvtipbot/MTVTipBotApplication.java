package ac.asimov.mtvtipbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.concurrent.Executor;

@SpringBootApplication(scanBasePackages = { "ac.asimov.mtvtipbot", "ac.asimov.mtvtipbot.*" })
@EnableScheduling
@EnableAsync
public class MTVTipBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(MTVTipBotApplication.class, args);
	}

	@Bean
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(4);
		executor.setMaxPoolSize(8);
		executor.setQueueCapacity(5000);
		executor.setThreadNamePrefix("MTV-TipBot-");
		executor.initialize();
		return executor;
	}

}
