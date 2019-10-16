package com.learn.concurrency.annoations;


/**
 * @author Darling
 * Retention 保留  Policy 政策
 */
public enum RetentionPolicy {
    /**
     * Annotations are to be discarded by the compiler.
     * 在编译的时候会被取消，只用于声明，理解，或者测试
     */
    SOURCE,

    /**
     * Annotations are to be recorded in the class file by the compiler
     * but need not be retained by the VM at run time.  This is the default
     * behavior.
     * 注解将被编译器记录在类文件中，但在运行时不需要由VM保留，（默认的选项）
     */
    CLASS,

    /**
     * Annotations are to be recorded in the class file by the compiler and
     * retained by the VM at run time, so they may be read reflectively.
     * 注解将被编译器记录在类文件中，但在运行时由VM保留，这样他们可以被反射获取（当你需要获取注解中字段的属性值的时候，需要用这个，比如AOP）
     * @see java.lang.reflect.AnnotatedElement
     */
    RUNTIME
}
