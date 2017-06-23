package vw.rxstudy.e05;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ObservableTest {
    /*
    * Observable = Subject = Publisher
    * Observer = Subscriber
    * */

    static class IntObservable extends Observable implements Runnable {
        @Override
        public void run() {
            for (int i=1; i<=10; i++) {
                setChanged();
                notifyObservers(i);     // push : 데이터의 방향 ->
                // int i = it.next();   // pull : 데이터의 방향 <-
            }
        }
    }

    public static void main(String[] args) {
        Observer ob = (o, arg) -> System.out.println(Thread.currentThread().getName() + " " + arg);

        IntObservable io = new IntObservable();
        io.addObserver(ob);

        ExecutorService es = Executors.newSingleThreadExecutor();
        es.execute(io);

        System.out.println(Thread.currentThread().getName() + " EXIT");
        es.shutdown();
        // push 방식으로 Observer 패턴을 이용하면 별개의 쓰레드에서 동작하는 코드를 손쉽게 만들 수 있다
    }
}
