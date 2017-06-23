package vw.rxstudy.e10;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LoadTest {
    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        loadTest("http://localhost:7070/rest?idx={idx}");

    }

    private static void loadTest(String url) throws InterruptedException, BrokenBarrierException {
        AtomicInteger counter = new AtomicInteger(0);
        ExecutorService es = Executors.newFixedThreadPool(100);
        CyclicBarrier barrier = new CyclicBarrier(101);
        RestTemplate rt = new RestTemplate();

        for (int i = 0; i < 100; i++) {
            es.submit(() -> {
                int idx = counter.addAndGet( 1);
                barrier.await();

                log.info("Thredad {}", idx);

                StopWatch sw = new StopWatch();
                sw.start();

                String res = rt.getForObject(url, String.class, idx);

                sw.stop();
                log.info("Elapsed : {}, {}, {}", idx, sw.getTotalTimeSeconds(), res);
                return null;
            });
        }

        barrier.await();
        StopWatch main = new StopWatch();
        main.start();

        es.shutdown();
        es.awaitTermination(100, TimeUnit.SECONDS);

        main.stop();
        log.info("Total : {}", main.getTotalTimeSeconds());
    }


}
