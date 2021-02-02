package com.learn.concurrency.example.threadPool;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class ThreadPoolExample2 {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        for (int i = 0; i < 10; i++) {
            final int index = i;
            // executorService.execute(new Runnable() {
            //     @Override
            //     public void run() {
            //         log.info("task:{}", index);
            //     }
            // });
         Future<Integer> future = executorService.submit(()->{
                log.info("task:{}", index);
                return index;
            });
         log.info(String.valueOf(future.get()));
        }
        executorService.shutdown();
    }
}
