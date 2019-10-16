package com.learn.concurrency.example.singleton;

import com.learn.concurrency.annoations.ThreadSafe;

/**
 * 饿汉模式
 * //在类加载时就完成了初始化，所以类加载较慢，但获取对象的速度快
 * @author Darling
 */
@ThreadSafe
public class HungrySingleton {
    // 私有构造函数
    private HungrySingleton() {

    }

    // 单例对象
    // 利用静态变量来存储唯一实例
    private static HungrySingleton instance = new HungrySingleton();

    // 提供公开获取实例接口
    public static HungrySingleton getInstance() {
        return instance;
    }
}
