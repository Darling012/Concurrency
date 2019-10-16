package com.learn.concurrency.example.singleton;

import com.learn.concurrency.annoations.NotRecommend;
import com.learn.concurrency.annoations.ThreadSafe;

/**
 * 懒汉模式
 * 单例实例在第一次使用时进行创建
 */
@ThreadSafe
@NotRecommend
public class LazySingleSync {

    // 私有构造函数
    private LazySingleSync() {

    }

    // 单例对象
    private static LazySingleSync instance = null;

    // 静态的工厂方法

    /**
     * 添加class类锁，影响了性能，加锁之后将代码进行了串行化，
     * 我们的代码块绝大部分是读操作，在读操作的情况下，代码线程是安全的
     */
    public static synchronized LazySingleSync getInstance() {
        if (instance == null) {
            instance = new LazySingleSync();
        }
        return instance;
    }
}
