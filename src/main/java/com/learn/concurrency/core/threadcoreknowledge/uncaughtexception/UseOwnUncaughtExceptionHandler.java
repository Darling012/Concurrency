package com.learn.concurrency.core.threadcoreknowledge.uncaughtexception;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;

/**
 * 描述：     使用刚才自己写的UncaughtExceptionHandler
 */
public class UseOwnUncaughtExceptionHandler implements Runnable {

    public static void main(String[] args) throws InterruptedException {

        Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler("捕获器1"));

        new Thread(new UseOwnUncaughtExceptionHandler(), "MyThread-1").start();
        Thread.sleep(300);
        new Thread(new UseOwnUncaughtExceptionHandler(), "MyThread-2").start();
        Thread.sleep(300);
        new Thread(new UseOwnUncaughtExceptionHandler(), "MyThread-3").start();
        Thread.sleep(300);
        new Thread(new UseOwnUncaughtExceptionHandler(), "MyThread-4").start();

        ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setUncaughtExceptionHandler(new MyUncaughtExceptionHandler("捕获器2"));
                return thread;
            }
        });
        // 捕获不到 FutureTask 的 run 方法捕获异常后保存，不再重新抛出。在调用 get 方法时，会将保存的异常重新抛出
        Future<Integer> future = executorService.submit(() -> 1 / 0);
        executorService.execute(new UseOwnUncaughtExceptionHandler());

    }


    @Override
    public void run() {
        throw new RuntimeException();
    }
}
