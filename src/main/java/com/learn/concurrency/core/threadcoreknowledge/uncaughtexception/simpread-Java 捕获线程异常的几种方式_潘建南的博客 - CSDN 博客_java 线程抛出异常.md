> 本文由 [简悦 SimpRead](http://ksria.com/simpread/) 转码， 原文地址 [blog.csdn.net](https://blog.csdn.net/pange1991/article/details/82115437)

首先，我们要知道，在 Java 中，线程中的异常是不能抛出到调用该线程的外部方法中捕获的。

**为什么不能抛出到外部线程捕获？**

因为线程是独立执行的代码片断，线程的问题应该由线程自己来解决，而不要委托到外部。基于这样的设计理念，在 Java 中，线程方法的异常都应该在线程代码边界之内（run 方法内）进行 try catch 并处理掉。换句话说，我们不能捕获从线程中逃逸的异常。

**怎么进行的限制？**

通过 java.lang.Runnable.run()方法声明 (因为此方法声明上没有 throw exception 部分) 进行了约束。

**如果在线程中抛出了异常会怎么样？**

线程会立即终结。

**现在我们可以怎样捕获线程中的异常？**

Java 中在处理异常的时候，通常的做法是使用 try-catch-finally 来包含代码块，但是 Java 自身还有一种方式可以处理——使用 UncaughtExceptionHandler。它能检测出某个线程由于未捕获的异常而终结的情况。当一个线程由于未捕获异常而退出时，JVM 会把这个事件报告给应用程序提供的 UncaughtExceptionHandler 异常处理器（这是 Thread 类中的接口）：

```java
//Thread类中的接口
public interface UncaughtExceptionHanlder {
	void uncaughtException(Thread t, Throwable e);
}

```

JDK5 之后允许我们在每一个 Thread 对象上添加一个异常处理器 UncaughtExceptionHandler 。Thread.UncaughtExceptionHandler.uncaughtException() 方法会在线程因未捕获的异常而面临死亡时被调用。

首先要先定义一个异常捕获器：

```java
public class MyUncheckedExceptionHandler implements UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.out.println("捕获异常处理方法：" + e);
    }
}

```

方法 1. 创建线程时设置异常处理 Handler
-------------------------

```java
Thread t = new Thread(new ExceptionThread());
t.setUncaughtExceptionHandler(new MyUnchecckedExceptionhandler());
t.start();

```

方法 2. 使用 Executors 创建线程时，还可以在 ThreadFactory 中设置
-----------------------------------------------

```java
ExecutorService exec = Executors.newCachedThreadPool(new ThreadFactory(){
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setUncaughtExceptionHandler(new MyUnchecckedExceptionhandler());
                return thread;
            }
});
exec.execute(new ExceptionThread());

```

不过，上面的结果能证明：通过 **execute** 方式提交的任务，能将它抛出的异常交给异常处理器。如果改成 **submit** 方式提交任务，则**异常不能被异常处理器捕获**，这是为什么呢？查看源码后可以发现，如果一个由 submit 提交的任务由于抛出了异常而结束，那么这个异常将被 Future.get 封装在 ExecutionException 中重新抛出。所以，通过 submit 提交到线程池的任务，无论是抛出的未检查异常还是已检查异常，都将被认为是任务返回状态的一部分，因此不会交由异常处理器来处理。

```java
java.util.concurrent.FutureTask 源码

public V get() throws InterruptedException, ExecutionException {
    int s = state;
    if (s <= COMPLETING)//如果任务没有结束，则等待结束
        s = awaitDone(false, 0L);
    return report(s);//如果执行结束，则报告执行结果
}

@SuppressWarnings("unchecked")
private V report(int s) throws ExecutionException {
    Object x = outcome;
    if (s == NORMAL)//如果执行正常，则返回结果
        return (V)x;
    if (s >= CANCELLED)//如果任务被取消，调用get则报CancellationException
        throw new CancellationException();
    throw new ExecutionException((Throwable)x);//执行异常，则抛出ExecutionException
}

```

方法 3. 使用线程组 ThreadGroup
-----------------------

```java
//1.创建线程组
ThreadGroup threadGroup =
        // 这是匿名类写法
        new ThreadGroup("group") {
            // 继承ThreadGroup并重新定义以下方法
            // 在线程成员抛出unchecked exception 会执行此方法
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                //4.处理捕获的线程异常
            }
        };
//2.创建Thread
Thread thread = new Thread(threadGroup, new Runnable() {
    @Override
    public void run() {
        System.out.println(1 / 0);

    }
}, "my_thread");
//3.启动线程
thread.start();

```

方法 4. 默认的线程异常捕获器
----------------

如果我们只需要一个线程异常处理器处理线程的异常，那么我们可以设置一个默认的线程异常处理器，当线程出现异常时，如果我们没有指定线程的异常处理器，而且线程组也没有设置，那么就会使用默认的线程异常处理器

```java
// 设置默认的线程异常捕获处理器
Thread.setDefaultUncaughtExceptionHandler(new MyUnchecckedExceptionhandler());

```

> 上面说的 4 种方法都是基于线程异常处理器实现的，接下来将的几种方法则不需要依赖异常处理器。

方法 5. 使用 FetureTask 来捕获异常
-------------------------

```java
//1.创建FeatureTask
FutureTask<Integer> futureTask = new FutureTask<>(new Callable<Integer>() {
    @Override
    public Integer call() throws Exception {
        return 1/0;
    }
});
//2.创建Thread
Thread thread = new Thread(futureTask);
//3.启动线程
thread.start();
try {
    Integer result = futureTask.get();
} catch (InterruptedException e) {
    e.printStackTrace();
} catch (ExecutionException e) {
    //4.处理捕获的线程异常
}

```

方法 6. 利用线程池提交线程时返回的 Feature 引用
------------------------------

```java
//1.创建线程池
ExecutorService executorService = Executors.newFixedThreadPool(10);
//2.创建Callable，有返回值的，你也可以创建一个线程实现Callable接口。
//  如果你不需要返回值，这里也可以创建一个Thread即可，在第3步时submit这个thread。
Callable<Integer> callable = new Callable<Integer>() {
    @Override
    public Integer call() throws Exception {
        return 1/0;
    }
};
//3.提交待执行的线程
Future<Integer> future = executorService.submit(callable);
try {
     Integer result = future.get();
} catch (InterruptedException e) {
    e.printStackTrace();
} catch (ExecutionException e) {
    //4.处理捕获的线程异常
}

```

实现原理可以看一下方法 2 的说明。方法 6 本质上和方法 5 一样是基于 FutureTask 实现的。

方法 7. 重写 ThreadPoolExecutor afterExecute方法
---------------------------------------------

```java
//1.创建线程池
ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>()) {
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        if (r instanceof Thread) {
            if (t != null) {
                //处理捕获的异常
            }
        } else if (r instanceof FutureTask) {
            FutureTask futureTask = (FutureTask) r;
            try {
                futureTask.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                //处理捕获的异常
            }
        }

    }
};


Thread t1 = new Thread(() -> {
    int c = 1 / 0;
});
threadPoolExecutor.execute(t1);

Callable<Integer> callable = () -> 2 / 0;
threadPoolExecutor.submit(callable);

```

总结
--

*   线程最好交由线程池进行管理。
*   线程池中如果你的线程不需要返回值则可以使用方法 2，利用 ThreadFactory 为线程指定统一的异常处理器。记得一定要用 execute 方式提交任务，否则异常处理器捕获不到异常。
*   线程池中如果你的线程需要返回值，你又想捕获线程异常，则需要借助 FutureTask，即使用方法 6。使用 submit 方法提交线程。当然了，不需要返回值的情况也可以使用方法 6。
*   方法 7 同时支持 execute 和 submit 提交任务时异常的捕获，适合相同类型任务的统一异常处理，但是异常处理粒度较粗，而方法 2 和方法 6 可以针对每个任务进行自定义的异常处理。
