package com.learn.concurrency.example.atomic;

import com.learn.concurrency.annoations.ThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ABA问题：在CAS操作的时候，其他线程将变量的值A改成了B由改成了A，本线程使用期望值A与当前变量进行比较的时候，发现A变量没有变，于是CAS就将A值进行了交换操作，这个时候实际上A值已经被其他线程改变过，这与设计思想是不符合的
 解决思路：每次变量更新的时候，把变量的版本号加一，这样只要变量被某一个线程修改过，该变量版本号就会发生递增操作，从而解决了ABA变化
 * @author Darling
 */
@Slf4j
@ThreadSafe
public class AtomicExample1 {

    // 请求总数
    public static int clientTotal = 5000;

    // 同时并发执行的线程数
    public static int threadTotal = 200;

    public static AtomicInteger count = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(threadTotal);
        final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);
        for (int i = 0; i < clientTotal ; i++) {
            executorService.execute(() -> {
                try {
                    semaphore.acquire();
                    add();
                    semaphore.release();
                } catch (Exception e) {
                    log.error("exception", e);
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        log.info("count:{}", count.get());
    }

    private static void add() {
        count.incrementAndGet();
        // count.getAndIncrement();
    }
}
