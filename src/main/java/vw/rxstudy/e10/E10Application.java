package vw.rxstudy.e10;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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

import java.util.function.Consumer;
import java.util.function.Function;

@SpringBootApplication
@EnableAsync
public class E10Application {
	@RestController
	static class Controller {
		static final String URL1 = "http://localhost:7071/service?req={req}";
		static final String URL2 = "http://localhost:7071/service2?req={req}";
		// RestTemplate -> AsyncRestTemplate + DeferredResult -> AsyncRestTemplate with Netty\
		// 의존성을 가진 여러서비스에 동시에 통신
		AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));

		@Autowired MyService myService;

		@GetMapping("/rest")
		public DeferredResult<String> rest(int  idx) throws Exception {
			DeferredResult<String> dr = new DeferredResult<>();

			Completion.from(rt.getForEntity(URL1, String.class, "Hello" + idx))
				.andApply(s -> rt.getForEntity(URL2, String.class, "Hello" + s.getBody()))
                .andApply(s -> myService.work(s.getBody()))
                .andError(e -> dr.setErrorResult(e))
				.andAccept(s -> dr.setResult(s));

//			ListenableFuture<ResponseEntity<String>> lf1 = rt.getForEntity(URL1, String.class, "Hello" + idx);
//
//			lf1.addCallback(s-> {
//				ListenableFuture<ResponseEntity<String>> lf2 = rt.getForEntity(URL2, String.class, "Hello" + s.getBody());
//				lf2.addCallback(s2-> {
//					ListenableFuture<String> lf3 = myService.work(s2.getBody());
//					lf3.addCallback(s3-> {
//						dr.setResult(s3);
//					}, e->dr.setErrorResult(e.getMessage()));
//				}, e-> dr.setErrorResult(e.getMessage()));
//			}, e-> dr.setErrorResult(e.getMessage()));
			return dr;
		}
	}

	public static class ApplyCompletion<Param, Result> extends Completion<Param, Result> {
        private Function<Param, ListenableFuture<Result>> fn;
        public ApplyCompletion(Function<Param, ListenableFuture<Result>> fn) {
            this.fn = fn;
        }

        @Override
        public void run(Param value) {
            ListenableFuture<Result> lf = fn.apply(value);
            lf.addCallback(this::complete, this::error);
        }
    }

    public static class AcceptCompletion<Param> extends Completion <Param, Void>{
        private Consumer<Param> con;
        public AcceptCompletion(Consumer<Param> con) {
            this.con = con;
        }

        @Override
        public void run(Param value) {
            con.accept(value);
        }
    }

    public static class ErrorCompletion<Result> extends Completion<Result, Result> {
        private Consumer<Throwable> econ;
        public ErrorCompletion(Consumer<Throwable> econ) {
            this.econ = econ;
        }

        @Override
        public void run(Result value) {
            if(next != null) next.run(value);
        }

        @Override
        protected void error(Throwable e) {
            econ.accept(e);
        }
    }

	@NoArgsConstructor
	public static class Completion <Param, Result>{
		protected Completion next;


        public static <Param, Result> Completion<Param, Result > from(ListenableFuture<Result> lf) {
			Completion<Param, Result> c = new Completion<>();
			lf.addCallback(c::complete, c::error);
			return c;
		}

		public Completion<Result, Result> andError(Consumer<Throwable> econ) {
            this.next = new ErrorCompletion<>(econ);
            return this.next;
        }

		public void andAccept(Consumer<Result> con) {
			this.next = new AcceptCompletion<>(con);
		}

		public <New> Completion<Result, New> andApply(Function<Result, ListenableFuture<New>> fn) {
			Completion<Result, New> c = new ApplyCompletion<>(fn);
			this.next = c;
			return c;
		}

        protected void error(Throwable e) {
            if(next != null) next.error(e);
		}

        protected void complete(Result s) {
			if(next != null) next.run(s);
		}

        protected void run(Param value) {}
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
		SpringApplication.run(E10Application.class, args);
	}
}
