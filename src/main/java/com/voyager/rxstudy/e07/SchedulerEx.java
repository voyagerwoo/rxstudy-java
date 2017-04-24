package com.voyager.rxstudy.e07;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SchedulerEx {
    public static void wait1Second() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Publisher<Integer> pub = sub -> {
            sub.onSubscribe(new Subscription() {
                @Override
                public void request(long l) {
                    log.debug("request");
                    sub.onNext(1);
                    wait1Second();
                    sub.onNext(2);
                    wait1Second();
                    sub.onNext(3);
                    wait1Second();
                    sub.onNext(4);
                    wait1Second();
                    sub.onNext(5);
                    wait1Second();
                    sub.onComplete();
                }

                @Override
                public void cancel() {

                }
            });

        };

        Publisher<Integer> subOnPub = sub -> {
            ExecutorService es = Executors.newSingleThreadExecutor(new CustomizableThreadFactory(){
                @Override
                public String getThreadNamePrefix() {
                    return "subOn-";
                }
            });
            es.execute(()-> pub.subscribe(new Subscriber<Integer>() {
                @Override
                public void onSubscribe(Subscription subscription) {
                    sub.onSubscribe(subscription);
                }

                @Override
                public void onNext(Integer integer) {
                    sub.onNext(integer);
                }

                @Override
                public void onError(Throwable throwable) {
                    sub.onError(throwable);
                    es.shutdown();
                }

                @Override
                public void onComplete() {
                    sub.onComplete();
                    es.shutdown();
                }
            }));
        };

        Publisher<Integer> pubOnPub = sub -> {
            subOnPub.subscribe(new Subscriber<Integer>() {
                ExecutorService es = Executors.newSingleThreadExecutor(new CustomizableThreadFactory(){
                    @Override
                    public String getThreadNamePrefix() {
                        return "pubOn-";
                    }
                });
                @Override
                public void onSubscribe(Subscription s) {
                    sub.onSubscribe(s);
                }

                @Override
                public void onNext(Integer integer) {
                    es.execute(()->sub.onNext(integer));
                }

                @Override
                public void onError(Throwable throwable) {
                    es.execute(()->sub.onError(throwable));
                    es.shutdown();
                }

                @Override
                public void onComplete() {
                    es.execute(sub::onComplete);
                    es.shutdown();
                }
            });
        };



//        subOnPub.subscribe(new Subscriber<Integer>() {
        subOnPub.subscribe(new DefaultLogSubscriber());
        subOnPub.subscribe(new DefaultLogSubscriber());
        log.debug("main thread done");
    }
}
