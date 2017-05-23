package com.voyager.rxstudy.e08;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LoadTest {

    public static void main(String[] args) throws InterruptedException {
        loadTest("http://localhost:7070/callable");
        //loadTest("http://localhost:7070/noncallable");
        //loadTest("http://localhost:7070/dr");

    }

    private static void loadTest(String url) throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        ExecutorService es = Executors.newFixedThreadPool(1000);

        RestTemplate rt = new RestTemplate();

        StopWatch main = new StopWatch();
        main.start();

        for (int i = 0; i < 1000; i++) {
            es.execute(() -> {
                int idx = counter.addAndGet(1);
                StopWatch sw = new StopWatch();
                sw.start();

                rt.getForObject(url, String.class);

                sw.stop();
                log.info("Elapsed : {}, {}", idx, sw.getTotalTimeSeconds());

            });
        }

        es.shutdown();
        es.awaitTermination(100, TimeUnit.SECONDS);
        main.stop();
        log.info("Total : {}", main.getTotalTimeSeconds());
    }


}
