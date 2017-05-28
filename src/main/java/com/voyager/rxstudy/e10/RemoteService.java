package com.voyager.rxstudy.e10;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class RemoteService {
	@RestController
	static class RemoteController {
		@GetMapping("/service")
		public String service(String req) throws Exception {
			TimeUnit.SECONDS.sleep(2);
//			throw new RuntimeException();
			return req + "/service1";
		}

		@GetMapping("/service2")
		public String service2(String req) throws Exception {
			TimeUnit.SECONDS.sleep(2);
			return req + "/service2";
		}
	}

	public static void main(String[] args) {
		System.setProperty("server.port", "7071");
		System.setProperty("server.tomcat.max-threads", "1000");
		SpringApplication.run(RemoteService.class, args);
	}
}
