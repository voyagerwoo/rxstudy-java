package com.voyager.rxstudy.e11;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@EnableAsync
@Slf4j
public class E11Application {
	@RestController
	static class Controller {
		static final String URL1 = "http://localhost:7071/service?req={req}";
		static final String URL2 = "http://localhost:7071/service2?req={req}";
		AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));

		@Autowired MyService myService;

		@GetMapping("/rest")
		public DeferredResult<String> rest(int  idx) throws Exception {
			DeferredResult<String> dr = new DeferredResult<>();

			toCompletableFuture(rt.getForEntity(URL1, String.class, "Hello" + idx))
//					.thenApply(s -> {if(1==1) throw new RuntimeException();return s;})
					.thenCompose(s -> toCompletableFuture(rt.getForEntity(URL2, String.class, s.getBody())))
					.thenApplyAsync(s -> {log.info("apply async" +s);return myService.work(s.getBody());})
					.thenAccept(s -> dr.setResult(s))
					.exceptionally(e -> {dr.setErrorResult(e); return null;});
			return dr;
		}
	}

	public static <T> CompletableFuture<T> toCompletableFuture(ListenableFuture<T> listenableFuture) {
		CompletableFuture<T> completableFuture = new CompletableFuture<T>();
		listenableFuture.addCallback(
				completableFuture::complete,
				completableFuture::completeExceptionally);
		return completableFuture;
	}

	@Service
	static class MyService {
		public String work(String req) {
			return req + "/asyncwork";
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(E11Application.class, args);
	}
}
