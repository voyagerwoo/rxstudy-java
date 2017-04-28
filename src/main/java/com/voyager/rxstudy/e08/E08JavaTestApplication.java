package com.voyager.rxstudy.e08;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootApplication
@EnableAsync
public class E08JavaTestApplication {
	@Component
	public static class MyService {
		@Async("tp")
		ListenableFuture<String> hello() throws InterruptedException {
			log.info("hello()");
			TimeUnit.SECONDS.sleep(1);
			return new AsyncResult<>("Hello");
		}
	}

	@Bean
	ThreadPoolTaskExecutor tp () {
		ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
		te.setCorePoolSize(10);
		te.setQueueCapacity(50);
		te.setMaxPoolSize(100);
		te.setThreadNamePrefix("myThread");
		te.initialize();

		return te;
	}

	public static void main(String[] args) {
		try (ConfigurableApplicationContext c = SpringApplication.run(E08JavaTestApplication.class, args)) {}
	}

	@Autowired
	MyService myService;

	@Bean
	ApplicationRunner run() {
		return args -> {
			log.info("run()");
			ListenableFuture<String> f = myService.hello();
			f.addCallback(s -> log.info("success callback : " +s), e-> log.error("error", e));
			log.info("exit");
		};
	}
}
