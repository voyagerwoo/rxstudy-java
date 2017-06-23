package vw.rxstudy.e09;

import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

@SpringBootApplication
@EnableAsync
public class E09Application {
	@RestController
	static class Controller {
		// RestTemplate -> AsyncRestTemplate + DeferredResult -> AsyncRestTemplate with Netty\
		// 의존성을 가진 여러서비스에 동시에 통신
		AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));

		@Autowired MyService myService;

		@GetMapping("/rest")
		public DeferredResult<String> rest(int  idx) throws Exception {
			DeferredResult<String> dr = new DeferredResult<>();
			ListenableFuture<ResponseEntity<String>> lf1 = rt.getForEntity("http://localhost:7071/service?req={req}", String.class, "Hello" + idx);

			lf1.addCallback(s-> {
				ListenableFuture<ResponseEntity<String>> lf2 = rt.getForEntity("http://localhost:7071/service2?req={req}", String.class, "Hello" + s.getBody());
				lf2.addCallback(s2-> {
					ListenableFuture<String> lf3 = myService.work(s2.getBody());
					lf3.addCallback(s3-> {
						dr.setResult(s3);
					}, e->dr.setErrorResult(e.getMessage()));
				}, e-> dr.setErrorResult(e.getMessage()));
			}, e-> dr.setErrorResult(e.getMessage()));
			return dr;
		}
	}

	@Service
	static class MyService {
		@Async
		public ListenableFuture<String> work(String req) {
			return new AsyncResult<>(req + "/asyncwork");
		}
	}

	@Bean
	ThreadPoolTaskExecutor myThreadPool() {
		ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
		te.setCorePoolSize(1);
		te.setMaxPoolSize(1);
		te.initialize();
		return te;
	}

	public static void main(String[] args) {
		SpringApplication.run(E09Application.class, args);
	}
}
