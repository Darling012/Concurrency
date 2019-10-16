package com.learn.concurrency.example.singleton;

import com.learn.concurrency.annoations.NotThreadSafe;

/**
 * 懒汉模式
 * 单例实例在第一次使用时进行创建
 *
 * @author Darling
 */
@NotThreadSafe
public class LazySingleton {

    // 私有构造函数
    private LazySingleton() {
    }


// 定义静态变量时，未初始化实例
    private static LazySingleton instance = null;

    // 静态的工厂方法
    public static LazySingleton getInstance() {
        // 使用时，先判断实例是否为空，如果实例为空，则实例化对象
        if (instance == null) {
            instance = new LazySingleton();
        }
        return instance;
    }
}
