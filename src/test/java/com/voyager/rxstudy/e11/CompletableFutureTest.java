package com.voyager.rxstudy.e11;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

@Slf4j
public class CompletableFutureTest {
    @Test
    public void complete_2() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> integerCF = new CompletableFuture<>();

        integerCF.complete(2);

        log.info(integerCF.get().toString());
        assertThat(2, is(integerCF.get()));
    }

    @Test(expected = ExecutionException.class)
    public void complete_RuntimeException() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> integerCF = new CompletableFuture<>();
        integerCF.completeExceptionally(new RuntimeException());
        integerCF.get();
    }

    @Test
    public void runAsyncAndThenRun() throws Exception {
        CompletableFuture.runAsync(() -> log.info("runAsync"))
                .thenRun(() -> log.info("thenRun1"))
                .thenRun(() -> log.info("thenRun2"));
        log.info("exit");

        ForkJoinPool.commonPool().shutdown();
        ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    public void supplyAsyncAndThenApplyAndThenAccept() throws Exception {
        CompletableFuture
                .supplyAsync(() -> {
                    log.info("supplyAsync");
                    return 1;
                })
                .thenApply(i -> {
                    log.info("thenApply1 {}", i);
                    return i + 1;
                })
                .thenApply(i -> {
                    log.info("thenApply2 {}", i);
                    return i * 3;
                })
                .thenAccept((i) -> log.info("thenAccept {}", i));
        log.info("exit");

        ForkJoinPool.commonPool().shutdown();
        ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    public void supplyAsyncAndThenComposeAndThenAccept() throws Exception {
        CompletableFuture
                .supplyAsync(() -> {
                    log.info("supplyAsync");
                    return 1;
                })
                .thenApply(i -> {
                    log.info("thenApply1 {}", i);
                    return i + 1;
                })
                .thenCompose(i -> {
                    log.info("thenApply2 {}", i);
                    return CompletableFuture.completedFuture(i * 3);
                })
                .thenAccept((i) -> log.info("thenAccept {}", i));
        log.info("exit");

        ForkJoinPool.commonPool().shutdown();
        ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    public void supplyAsyncAndThenComposeAndExceptionallyAndThenAccept() throws Exception {
        CompletableFuture
                .supplyAsync(() -> {
                    if(1==1) throw new RuntimeException();
                    log.info("supplyAsync");
                    return 1;
                })
                .thenApply(i -> {
                    log.info("thenApply1 {}", i);
                    return i + 1;
                })
                .thenCompose(i -> {
                    log.info("thenApply2 {}", i);
                    return CompletableFuture.completedFuture(i * 3);
                })
                .exceptionally(e -> -10)
                .thenAccept((i) -> log.info("thenAccept {}", i));
        log.info("exit");

        ForkJoinPool.commonPool().shutdown();
        ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);
    }


    @Test
    public void supplyAsyncAndThenApplyAsyncWithCustomThreads() throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(10);

        CompletableFuture
                .supplyAsync(() -> {
                    log.info("supplyAsync");
                    return 1;
                }, es)
                .thenCompose(i -> {
                    log.info("thenApply2 {}", i);
                    return CompletableFuture.completedFuture(i * 3);
                })
                .thenApplyAsync(i -> {
                    log.info("thenApply1 {}", i);
                    return i + 1;
                }, es)
                .exceptionally(e -> -10)
                .thenAcceptAsync((i) -> log.info("thenAccept {}", i), es);
        log.info("exit");

        es.awaitTermination(1, TimeUnit.SECONDS);
        es.shutdown();
    }
}