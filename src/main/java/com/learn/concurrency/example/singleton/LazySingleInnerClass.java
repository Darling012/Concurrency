package com.learn.concurrency.example.singleton;

public class LazySingleInnerClass {
    private LazySingleInnerClass() {
    }

    // 单例持有者
    private static class InstanceHolder {
        private final static LazySingleInnerClass instance = new LazySingleInnerClass();
    }

    //
    public static LazySingleInnerClass getInstance() {
        // 调用内部类属性
        return InstanceHolder.instance;
    }
}
