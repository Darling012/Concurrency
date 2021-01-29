package com.learn.concurrency.example.completableFuture;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @program: concurrency
 * @version:
 * @description:
 * @author: ling
 * @create: 2021-01-29 14:07
 **/

public class CompletableFutureTest {
    @Test
    public void noComputation() throws ExecutionException, InterruptedException {
        CompletableFuture<String> completableFuture
                = CompletableFuture.completedFuture("hello world");

        System.out.println("result is " + completableFuture.get());
    }

    @Test
    public void supplyAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<String> completableFuture
                = CompletableFuture.supplyAsync(() -> "挖坑完成");

        System.out.println("result is " + completableFuture.get());
    }

    @Test
    public void runAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> completableFuture
                = CompletableFuture.runAsync(() -> System.out.println("挖坑完成"));
        completableFuture.get();
    }

    @Test
    public void thenApply() throws ExecutionException, InterruptedException {
        CompletableFuture<String> completableFuture
                = CompletableFuture.supplyAsync(() -> "挖坑完成")
                                   .thenApply(s -> s + ",并且归还铁锹")
                                   .thenApply(s -> s + "，全部完成。");

        System.out.println("result is " + completableFuture.get());
    }

    @Test
    public void thenAccept() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> completableFuture
                = CompletableFuture.supplyAsync(() -> "挖坑完成")
                                   .thenAccept(s -> System.out.println(s + ",并且归还铁锹"));
        completableFuture.get();
    }
}
