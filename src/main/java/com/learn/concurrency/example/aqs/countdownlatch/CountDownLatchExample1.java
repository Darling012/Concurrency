package com.learn.concurrency.example.aqs.countdownlatch;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Darling
 * 当线程调用CountDownLatch 对象的await 方法后， 当前线程会被阻塞， 直到下面的情况之一发生才会返回：
 * 1 当所有线程都调用了CountDownLatch 对象的countDown 方法后，也就是计数器的值为0 时；
 * 2 其他线程调用了当前线程的interrupt（）方法中断了当前线程，当前线程就会抛出InterruptedException 异常， 然后返回。
 */

@Slf4j
public class CountDownLatchExample1 {

    private final static int THREAD_COUNT = 5;

    public static void main(String[] args) throws Exception {

        ExecutorService exec = Executors.newCachedThreadPool();

        final CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadNum = i;
            exec.execute(() -> {
                try {
                    test(threadNum);
                } catch (Exception e) {
                    log.error("exception", e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        log.info("finish");
        exec.shutdown();
    }

    private static void test(int threadNum) throws Exception {
        Thread.sleep(100);
        log.info("{}", threadNum);
        Thread.sleep(100);
    }
}
