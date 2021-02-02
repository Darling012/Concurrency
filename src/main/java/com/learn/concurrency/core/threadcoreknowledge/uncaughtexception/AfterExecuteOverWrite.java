package com.learn.concurrency.core.threadcoreknowledge.uncaughtexception;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @program: concurrency
 * @version:
 * @description:
 * @author: ling
 * @create: 2021-02-02 10:58
 **/
public class AfterExecuteOverWrite {
    public static void main(String[] args) {
        //1.创建线程池
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                if (r instanceof Thread) {
                    if (t != null) {
                        //处理捕获的异常
                        System.out.println("捕获异常");
                    }
                } else if (r instanceof FutureTask) {
                    FutureTask futureTask = (FutureTask) r;
                    try {
                        futureTask.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        //处理捕获的异常
                         System.out.println("捕获FutureTask异常");
                    }
                }

            }
        };


        Thread t1 = new Thread(() -> {
            int c = 1 / 0;
        });
        threadPoolExecutor.execute(t1);

        Callable<Integer> callable = () -> 2 / 0;
        threadPoolExecutor.submit(callable);
    }
}
