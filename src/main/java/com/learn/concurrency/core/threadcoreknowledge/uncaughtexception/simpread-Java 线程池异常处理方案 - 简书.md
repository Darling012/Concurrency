> 本文由 [简悦 SimpRead](http://ksria.com/simpread/) 转码， 原文地址 [www.jianshu.com](https://www.jianshu.com/p/30e488f4e021)

> 执行多线程并发任务的时候，如果任务类型相同，一般会考虑使用线程池，一方面利用了并发的优势，一方面避免创建大量线程得不偿失。使用线程池执行的任务一般是我们自己的代码，或者第三方的代码，有没有想过，如果这些代码抛出异常时，线程池会怎么处理呢？如果不处理又会有什么影响？

异常的影响
-----

[Java 理论与实践: 嗨，我的线程到哪里去了？](https://link.jianshu.com?t=https://www.ibm.com/developerworks/cn/java/j-jtp0924/)这篇文章列举了一个由于 RuntimeException 引发的线程泄漏问题：

> 考虑这样一个假设的中间件服务器应用程序，它聚合来自各种输入源的消息，然后将它们提交到外部服务器应用程序，从外部应用程序接收响应并将响应路由回适当的输入源。对于每个输入源，都有一个以其自己的方式接受其输入消息的插件（通过扫描文件目录、等待套接字连接、轮询数据库表等）。插件可以由第三方编写，即使它们是在服务器 JVM 上运行的。这个应用程序拥有（至少）两个内部工作队列 ― 从插件处接收的正在等待被发送到服务器的消息（“出站消息” 队列），以及从服务器接收的正在等待被传递到适当插件的响应（“入站响应” 队列）。通过调用插件对象上的服务例程 incomingResponse() ，消息被路由到最初发出请求的插件。

> 从插件接收消息后，就被排列到出站消息队列中。由一个或多个从队列读取消息的线程处理出站消息队列中的消息、记录其来源并将它提交给远程服务器应用程序（假定通过 Web 服务接口）。远程应用程序最终通过 Web 服务接口返回响应，然后我们的服务器将接收的响应排列到入站响应队列中。一个或多个响应线程从入站响应队列读取消息并将其路由到适当的插件，从而完成往返 “旅程”。  
> 在这个应用程序中，有两个消息队列，分别用于出站请求和入站响应，不同的插件内可能也有另外的队列。我们还有几种服务线程，一个从出站消息队列读取请求并将其提交给外部服务器，一个从入站响应队列读取响应并将其路由到插件，在用于向套接字或其它外部请求源提供服务的插件中可能也有一些线程。

> 如果这些线程中的一个（如响应分派线程）消失了，将会发生什么？因为插件仍能够提交新消息，所以它们可能不会立即注意到某些方面出错了。消息仍将通过各种输入源到达，并通过我们的应用程序提交到外部服务。因为插件并不期待立即获得其响应，因此它仍没有意识到出了问题。最后，接收的响应将排满队列。如果它们存储在内存中，那么最终将耗尽内存。即使不耗尽内存，也会有人在某个时刻发现响应得不到传递 ― 但这可能需要一些时间，因为系统的其它方面仍能正常发挥作用。

> 当主要的任务处理方面由线程池而不是单个线程来处理时，对于偶然的线程泄漏的后果有一定程度的保护，因为一个执行得很好的八线程的线程池，用七个线程完成其工作的效率可能仍可以接受。起初，可能没有任何显著的差异。但是，系统性能最终将下降，虽然这种下降的方式不易被察觉。

> 服务器应用程序中的线程泄漏问题在于不是总是容易从外部检测它。因为大多数线程只处理服务器的部分工作负载，或可能仅处理特定类型的后台任务，所以当程序实际上遭遇严重故障时，在用户看来它仍在正常工作。这一点，再加上引起线程泄漏的因素并不总是留下明显痕迹，就会引起令人惊讶甚或使人迷惑的应用程序行为。

我们在使用线程池处理并行任务时，在线程池的生命周期当中，将通过某种抽象机制 (Runnable) 调用许多未知的代码，这些代码有可能是我们自己写的，也有可能来自第三方。任何代码都有可能抛出一个 RuntimeException，如果这些提交的 Runnable 抛出了 RuntimeException，线程池可以捕获他，线程池有可能会创建一个新的线程来代替这个因为抛出异常而结束的线程，也有可能什么也不做(这要看线程池的策略)。即使不会造成线程泄漏，我们也会丢失这个任务的执行情况，无法感知任务执行出现了异常。

所以，有必要处理提交到线程池运行的代码抛出的异常。

如何处理异常
------

### 简单了解线程池

![](http://upload-images.jianshu.io/upload_images/5555632-a27ab816ddeb073b.png)

1. 在自己的代码里try-catch捕获所有的异常
2. 使用ExecutorService.submit执行，得到返回的Future，使用Future.get接收抛出的异常
3. 重写ThreadPoolExcutor.afterExecute方法，处理传递到方法中的异常引用
4. 实例化线程池时传入自己实现的ThreadFactory,设置Thread.UncaughtExcetionHander处理异常

上面是我画的思维导图

先介绍一下 jdk 中线程池的实现：

![](http://upload-images.jianshu.io/upload_images/5555632-b0c8186776290568.png)

Executor 定义了一个通用的并发任务框架，即通过 execute 方法执行一个任务。

ExecutorService 定义了并发框架 (线程池) 的生命周期。

AbstractExecutorService、ThreadPoolExecutor、ScheduledThreadPoolExecutor 实现了并发任务框架 (线程池)。其中 ScheduledThreadPoolExecutor 支持定时及周期性任务的执行。

Executors 相当于一个线程池工厂类，返回了不同执行策略的线程池对象。

我们一般使用 Executors.new... 方法来得到某种线程池：

```
newCachedThreadPool
创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程。

newFixedThreadPool
创建一个定长线程池，可控制线程最大并发数，超出的线程会在队列中等待。

newSingleThreadExecutor
创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行。

newScheduledThreadPool
创建一个定长线程池，支持定时及周期性任务执行。


```

其中，前三者返回 ExecutorService 实例，他们的实现为 ThreadPoolExecutor 或其包装类；newScheduledThreadPool 返回的是 ScheduledExecutorService 实例，他的实现为 ScheduledThreadPoolExecutor 或其包装类。

```
ExecutorService exec = Executors.newFixedThreadPool(8);
```

以上述代码为例，得到 ExecutorService 实例后，我们可以通过两种方式提交任务 (Runnable):

*   `exec.execute(runnable)`
    
*   `exec.submit(runnable)`

对于这两种不同的任务提交方式，我们有不同的异常处理办法。

### exec.submit(runnable)

![](http://upload-images.jianshu.io/upload_images/5555632-df31dda3b42cb30d.png)

使用`exec.submit(runnable)`这种方式提交任务时，submit 方法会将我们的 Runnable 包装为一个 RunnableFuture 对象，这个对象实际上是 FutureTask 实例，然后将这个 FutureTask 交给 execute 方法执行。

![](http://upload-images.jianshu.io/upload_images/5555632-ac3692574c433082.png) 

Future 用来管理任务的生命周期，将 Future 实例提交给异步线程执行后，可以调用 Future.get 方法获取任务执行的结果。我们知道 Runnable 执行是没有返回结果的，那么这个结果是怎么来的？

![](http://upload-images.jianshu.io/upload_images/5555632-8fe7f887b8dabaf4.png)

可以看到，在 FutureTask 的构造方法中，将 Runnable 包装成了一个 Callable 类型的对象。

![](http://upload-images.jianshu.io/upload_images/5555632-c8ffbc122b795708.png)

FutureTask 的 run 方法中，调用了 callable 对象的 call 方法，也就调用了我们传入的 Runnable 对象的 run 方法。可以看到，如果代码 (Runnable) 抛出异常，会被捕获并且把这个异常保存下来。

![](http://upload-images.jianshu.io/upload_images/5555632-c98d76c0faeef35b.png)

![](http://upload-images.jianshu.io/upload_images/5555632-19af15fdc9fa0500.png)

可以看到，在调用 get 方法时，会将保存的异常重新抛出。所以，我们在使用 submit 方法提交任务的时候，利用返回的 Future 对象，通过他的 get 方法可以得到任务运行中抛出的异常，然后针对异常做一些处理。

由于我们在调用 submit 时并没有给 Runnable 指定返回结果，所以在将 Runnable 包装为 Callable 的时候，会传入一个 null，故 get 方法返回一个 null.

当然，我们也可以直接传入 Callable 类型的任务，这样就可以获取任务执行返回结果，并且得到任务执行抛出的异常。

这就是使用线程池时处理任务中抛出异常的**第一种方法**：**使用 ExecutorService.submit 执行任务，利用返回的 Future 对象的 get 方法接收抛出的异常，然后进行处理**

### exec.execute(runnable)

利用 Future.get 得到任务抛出的异常的**缺点在于**，我们需要显式的遍历 Future，调用 get 方法获取每个任务执行抛出的异常，然后处理。

很多时候我们仅仅是使用`exec.execute(runnable)`这种方法来提交我们的任务。这种情况下任务抛出的异常如何处理呢？

在使用`exec.execute(runnable)`提交任务的时候 (submit 其实也是调用 execute 方法执行)，我们的任务最终会被一个 Worker 对象执行。这个 Worker 内部封装了一个 Thread 对象，这个 Thread 就是线程池的工作者线程。工作者线程会调用 runWorker 方法来执行我们提交的任务：(代码比较长，就直接粘过来了)

```java
final void runWorker(Worker w) {
        Thread wt = Thread.currentThread();
        Runnable task = w.firstTask;
        w.firstTask = null;
        w.unlock(); // allow interrupts
        boolean completedAbruptly = true;
        try {
            while (task != null || (task = getTask()) != null) {
                w.lock();
                // If pool is stopping, ensure thread is interrupted;
                // if not, ensure thread is not interrupted.  This
                // requires a recheck in second case to deal with
                // shutdownNow race while clearing interrupt
                if ((runStateAtLeast(ctl.get(), STOP) ||
                     (Thread.interrupted() &&
                      runStateAtLeast(ctl.get(), STOP))) &&
                    !wt.isInterrupted())
                    wt.interrupt();
                try {
                    beforeExecute(wt, task);
                    Throwable thrown = null;
                    try {
                        task.run();
                    } catch (RuntimeException x) {
                        thrown = x; throw x;
                    } catch (Error x) {
                        thrown = x; throw x;
                    } catch (Throwable x) {
                        thrown = x; throw new Error(x);
                    } finally {
                        afterExecute(task, thrown);
                    }
                } finally {
                    task = null;
                    w.completedTasks++;
                    w.unlock();
                }
            }
            completedAbruptly = false;
        } finally {
            processWorkerExit(w, completedAbruptly);
        }
}


```

上面代码的基本意思就是不停的从任务队列中取出任务执行，如果任务代码 (task.run) 抛出异常，会被最内层的`try--catch`块捕获，然后重新抛出。注意到最里面的 finally 块，在重新抛出异常之前，**要先执行`afterExecute`方法，这个方法的默认实现为空**，即什么也不做。我们可以在这个方法上做点文章，这就是我们的**第二种方法**，重写`ThreadPoolExecutor.afterExecute`方法，处理传递到`afterExecute`方法中的异常**：

```java
class ExtendedExecutor extends ThreadPoolExecutor {
   // ...
   protected void afterExecute(Runnable r, Throwable t) {
     super.afterExecute(r, t);
     if (t == null && r instanceof Future<?>) {
       try {
         Object result = ((Future<?>) r).get();
       } catch (CancellationException ce) {
           t = ce;
       } catch (ExecutionException ee) {
           t = ee.getCause();
       } catch (InterruptedException ie) {
           Thread.currentThread().interrupt(); // ignore/reset
       }
     }
     if (t != null)
       System.out.println(t);
   }
 }


```

> When actions are enclosed in tasks (such as FutureTask) either explicitly or via methods such as submit, these task objects catch and maintain computational exceptions, and so they do not cause abrupt termination, and the internal exceptions are not passed to this method. If you would like to trap both kinds of failures in this method, you can further probe for such cases, as in this sample subclass that prints either the direct cause or the underlying exception if a task has been aborted:

上面是 java doc 给出的建议。可以看到，代码中还处理了`task`是`FutureTask`的情况。回想一下`submit`方式提交任务的情况：

*   在 submit 方法中，我们传入的 Runnable/Callable(要执行的任务) 被封装为 FutureTask 对象，交给`execute`方法执行
    
*   经过一系列操作，提交的 FutureTask 对象被 Worker 对象中的工作者线程所执行，也就是 runWorker 方法
    
    此时的代码运行情况：runWorker->submit 方法封装的 FutureTask 的 run 方法 -> 我们提交的 Runnable 的 run 方法
    
*   此时从`我们提交的Runnable的run方法`中抛出了一个未检测异RunnableException，被 FutureTask 的 run 方法捕获
    
*   FutureTask 的 run 方法捕获异常后保存，不再重新抛出。同时意味着 run 方法执行结束。
    
*   runWorker 方法没有检测到异常，`task.run`当作正常运行结束。但是还是会执行 afterExecute 方法。
    

经过这样的梳理，上面的代码为什么这么写就一目了然了。

上面已经提到了两种解决任务代码抛出未检测异常的方案。接下来是**第三种**：

当一个线程因为未捕获的异常而退出时，JVM 会把这个事件报告给应用提供的`UncaughtExceptionHandler`异常处理器，如果没有提供任何的异常处理器，那么默认的行为就是将堆栈信息输送到 System.err。

看一下上面的`runWorker`方法，`如果task.run(任务代码) 抛出了异常，异常会层层抛出，最终导致这个线程退出。此时这个抛出的异常就会传递到UncaughtExceptionHandler实例当中，由uncaughtException(Thread t,Throwable e)这个方法处理。`

于是就有了第三种解决任务代码抛出异常的方案：**为工作者线程设置`UncaughtExceptionHandler`，在`uncaughtException`方法中处理异常**

**注意，这个方案不适用与使用`submit`方式提交任务的情况，原因上面也提到了，FutureTask 的 run 方法捕获异常后保存，不再重新抛出，意味着`runWorker`方法并不会捕获到抛出的异常，线程也就不会退出，也不会执行我们设置的`UncaughtExceptionHandler`。**

如何为工作者线程设置`UncaughtExceptionHandler`呢？`ThreadPoolExecutor`的构造函数提供一个`ThreadFactory`，可以在其中设置我们自定义的`UncaughtExceptionHandler`，这里不再赘述。

至于**第四种方案**，就很简单了：**在我们提供的 Runnable 的 run 方法中捕获任务代码可能抛出的所有异常，包括未检测异常**。这种方法比较简单，也有他的局限性，不够灵活，我们的处理被局限在了线程代码边界之内。

总结
--

通过上面的分析我们得到了四种解决任务代码抛异常的方案：

*   **在我们提供的 Runnable 的 run 方法中捕获任务代码可能抛出的所有异常，包括未检测异常**
    
*   **使用 ExecutorService.submit 执行任务，利用返回的 Future 对象的 get 方法接收抛出的异常，然后进行处理**
    
*   **重写`ThreadPoolExecutor.afterExecute`方法，处理传递到`afterExecute`方法中的异常**
    
*   **为工作者线程设置`UncaughtExceptionHandler`，在`uncaughtException`方法中处理异常，无法处理以`submit`的方式提交的任务。**
