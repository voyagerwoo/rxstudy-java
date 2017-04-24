package com.voyager.rxstudy.e07;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FluxSchedulerEx {
    public static void main(String[] args) throws InterruptedException {
        /* publishOn & subscribeOn Test */
        Flux<Integer> pub = Flux.range(1, 10)
                .publishOn(Schedulers.newSingle("pub")) //컨슈머가 느릴 경우
//                .log()
                .subscribeOn(Schedulers.newParallel("sub"));//퍼블리셔가 느릴 경우
        pub.subscribe(System.out::println); //sout subscriber
        pub.subscribe(s -> log.debug(s.toString())); //slf4j subscriber
        System.out.println("exit");


        /* Flux interval Test */
//        Flux.interval(Duration.ofMillis(200))
//                .take(10)
//                .subscribe(s -> log.debug("onNext: {}", s));
//        log.debug("exit");
//        TimeUnit.SECONDS.sleep(5);


        /* User Thread Test */
        /*Executors.newSingleThreadExecutor().execute(() -> {
            try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException ignored) {}
            log.debug("Hello");
        });
        log.debug("exit");*/
    }
}
