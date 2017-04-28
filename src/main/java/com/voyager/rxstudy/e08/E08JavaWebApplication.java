package com.voyager.rxstudy.e08;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootApplication
@EnableAsync
public class E08JavaWebApplication {

    @RestController
    public static class MyController {
        @RequestMapping("/callable")
        public Callable<String> callable() throws Exception {
            log.info("callable");
            return () -> {
                //MvcAsync1 스프링이 실행시켜줌
                log.info("async");
                TimeUnit.SECONDS.sleep(2);
                return "Hello";
            };
        }

        @RequestMapping("/noncallable")
        public String notcallable() throws Exception {
            log.info("noncallable");
            TimeUnit.SECONDS.sleep(2);
            return "Hello";
        }
    }

    @RestController
    public static class DRController {
        Queue<DeferredResult<String>> queue = new ConcurrentLinkedDeque<>();

        @GetMapping("/dr")
        public DeferredResult<String> dr() {
            log.info("dr");
            DeferredResult<String> dr = new DeferredResult<>(6000000L);
            queue.add(dr);
            return dr;
        }

        @GetMapping("/dr/count")
        public String drCount() {
            log.info("dr count");
            return String.valueOf(queue.size());
        }

        @GetMapping("/dr/event")
        public String drEvent(String msg) {
            log.info("dr event");
            for (DeferredResult<String> dr : queue) {
                dr.setResult("Hello " + msg);
                queue.remove(dr);
            }
            return "OK";
        }
    }

    @RestController
    public static class EmitterController {
        @GetMapping("/emitter")
        public ResponseBodyEmitter emitter() {
            ResponseBodyEmitter emitter = new ResponseBodyEmitter();

            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    for (int i = 1; i < 50; i++) {
                        emitter.send("<p>Stream " + i + "</p>");
                        TimeUnit.MILLISECONDS.sleep(100);
                    }
                } catch (Exception e) {
                    log.error("error", e);
                }
            });

            return emitter;
        }
    }


    public static void main(String[] args) {
        SpringApplication.run(E08JavaWebApplication.class, args);
    }
}
