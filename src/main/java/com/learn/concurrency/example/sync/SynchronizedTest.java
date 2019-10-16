package com.learn.concurrency.example.sync;

import java.util.HashMap;

/**
 * 当两个并发线程访问同一个对象Object中的这个synchronized同步代码块时，一个时间内只能有一个线程得到执行。另一个线程必须等待当前线程执行完这个代码块后才能执行该代码块。
 然而，当一个线程访问Object的一个synchronized同步代码块时，另一个线程仍然可以访问该Object中的非synchronized同步代码块。
 尤其关键的是，当一个线程访问Object的一个同步代码块时，其他线程对Object中所有其他同步代码块的访问将被阻塞。也就是说，当一个线程访问Object的一个同步代码块时，他就获得了这个Object的对象锁。结果，其他线程对该Object对象所有同步代码部分的访问都被暂时阻塞。
 *
 */
public class SynchronizedTest{

    private static  volatile  Object object = new Object();

    public static void main (String[] args){

        Thread threadA = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (object) {
                    System.out.println("A");
                    try {
                        System.out.println("AA");
                        object.wait();
                        System.out.println("AAA");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        Thread threadB = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("b");
                synchronized (object) {
                    try {
                        object.wait();
                        System.out.println("bb");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        threadA.start();
        threadB.start();
        try {
            threadA.join();
            threadB.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
