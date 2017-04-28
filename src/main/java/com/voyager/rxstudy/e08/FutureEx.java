package com.voyager.rxstudy.e08;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.*;

@Slf4j
public class FutureEx {
    interface SuccessCallback<T> {
        void onSuccess(T result);
    }

    interface ExceptionCallback {
        void onError(Throwable T);
    }

    public static class CallbackFutureTask<T> extends FutureTask<T> {
        SuccessCallback<T> sc;
        ExceptionCallback ec;
        public CallbackFutureTask(Callable<T> callable, SuccessCallback<T> sc, ExceptionCallback ec) {
            super(callable);
            this.sc = Objects.requireNonNull(sc);
            this.ec = Objects.requireNonNull(ec);
        }

        @Override
        protected void done() {
            try {
                sc.onSuccess(get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                ec.onError(e.getCause());
            }
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newCachedThreadPool();
//
//        Future<String> f = es.submit(() -> {
//            Thread.sleep(2000);
//            log.info("Async");
//            return "Hello";
//        });
//
//        log.info("is f done? " + f.isDone()); // non-blocking
//        log.info(f.get()); // blocking
//
//        log.info("Exit");

//        FutureTask<String> f = new FutureTask<String>(() -> {
//            Thread.sleep(2000);
//            log.info("Async");
//            return "Hello";
//        }) {
//            @Override
//            protected void done() {
//                try {
//                    log.info(get());
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
//        };

        CallbackFutureTask<String> f = new CallbackFutureTask<>(() -> {
            Thread.sleep(2000);
            log.info("Async");
            if (1==1) throw new RuntimeException("Async Error!!!");
            return "Hello";
        }, log::info, t -> log.error("error", t));

        es.execute(f);
        es.shutdown();

    }
}