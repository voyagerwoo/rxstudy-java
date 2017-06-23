package vw.rxstudy.e05;

import java.util.Iterator;

public class IterableTest {

    public static void main(String[] args) {
        Iterable<Integer> iter = () -> new Iterator<Integer>() {
            int i = 0;
            final static int MAX = 10;

            @Override
            public boolean hasNext() {
                return i < MAX;
            }

            @Override
            public Integer next() {
                return ++i;
            }
        };

        // for-each
        for(Integer i : iter) {
            System.out.println("For-Each " + i);
        }

        // old style
        for (Iterator<Integer> it = iter.iterator(); it.hasNext();) {
            System.out.println("Old-For " + it.next());
        }
    }
}
