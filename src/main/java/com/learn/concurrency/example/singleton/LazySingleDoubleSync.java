package com.learn.concurrency.example.singleton;

import com.learn.concurrency.annoations.NotThreadSafe;

/**
 * 懒汉模式 -》 双重同步锁单例模式
 * 单例实例在第一次使用时进行创建
 *
 * @author Darling
 */
@NotThreadSafe
public class LazySingleDoubleSync {

    // 私有构造函数
    private LazySingleDoubleSync() {

    }

    // 1、memory = allocate() 分配对象的内存空间
    // 2、ctorInstance() 初始化对象
    // 3、instance = memory 设置instance指向刚分配的内存

    // JVM和cpu优化，发生了指令重排

    // 1、memory = allocate() 分配对象的内存空间
    // 3、instance = memory 设置instance指向刚分配的内存
    // 2、ctorInstance() 初始化对象

    // 单例对象
    private static LazySingleDoubleSync instance = null;

    // 静态的工厂方法
    public static LazySingleDoubleSync getInstance() {

        // 第一次判断，如果这里为空，不进入抢锁阶段，直接返回实例
        if (instance == null) { // 双重检测机制        // B
            synchronized (LazySingleDoubleSync.class) { // 同步锁
                // 抢到锁之后再次判断是否为空
                if (instance == null) {
                    instance = new LazySingleDoubleSync(); // A - 3
                }
            }
        }
        return instance;
    }
}
