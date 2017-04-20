package com.voyager.rxstudy.e07;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Slf4j
public class DefaultLogSubscriber implements Subscriber<Integer> {
    @Override
    public void onSubscribe(Subscription subscription) {
        log.debug("onSubscribe");
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(Integer integer) {
        log.debug("onNext: {}", integer);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("onError:{}", throwable);
    }

    @Override
    public void onComplete() {
        log.debug("onComplete");

    }
}
