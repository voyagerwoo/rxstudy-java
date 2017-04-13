package com.voyager.rxstudy.e06;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public  class DelegateSub  <T, R> implements Subscriber<T> {
    private Subscriber sub;

    public DelegateSub(Subscriber<? super R> sub) {
        this.sub = sub;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
         sub.onSubscribe(subscription);
    }

    @Override
    public void onNext(T i) {
          sub.onNext(i);
    }

    @Override
    public void onError(Throwable throwable) {
        sub.onError(throwable  );
    }

    @Override
    public void onComplete() {
        sub.onComplete();
    }
}
