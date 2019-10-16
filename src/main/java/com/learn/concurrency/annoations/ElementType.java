package com.learn.concurrency.annoations;

/**
 * @author Darling
 */

public enum ElementType {
    /** Class, interface (including annotation type), or enum declaration */
   /** 声明注解作用在类，接口，枚举上*/
    TYPE,

    /** Field declaration (includes enum constants) */
   /** 声明注解作用在属性上*/
    FIELD,

    /** Method declaration */
   /** 声明注解作用在方法上*/
    METHOD,

    /** Formal parameter declaration */
   /** 声明注解作用在参数上*/
    PARAMETER,

    /** Constructor declaration */
   /** 声明注解作用在构造函数上*/
    CONSTRUCTOR,

    /** Local variable declaration */
   /** 声明注解作用在本地变量上*/
    LOCAL_VARIABLE,

    /** Annotation type declaration */
   /** 声明注解作用在注解上*/
    ANNOTATION_TYPE,

    /** Package declaration */
   /** 声明注解作用在包上*/
    PACKAGE,

    /**
     * Type parameter declaration
     *
     * @since 1.8
     */
   /** 声明注解可以应用在TYPE声明上*/
    TYPE_PARAMETER,

    /**
     * Use of a type
     * Type.TYPE_USE 表示这个 Annotation 可以用在所有使用 Type 的地方（如：泛型，类型转换等）
     * @since 1.8
     */
    TYPE_USE
}
