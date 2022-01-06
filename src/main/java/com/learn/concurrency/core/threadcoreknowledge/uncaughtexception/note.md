#### 缘由：

任务执行代码为ThreadPoolExecutor#runWorker，其中的task执行被try catch 起来，异常传递到afterExecute(task,ex)，但其并没有实现。所以我们无法感知，这样做保证了提交的任务出错不影响其他任务及当前线程。

#### 如何避免：

1. 提交任务时捕获，不抛给线程池
2. 抛给线程池，我们处理

##### 1. 直接catch

​     所有任务都要catch，不存在受检异常也要catch。

##### 2. 线程池处理

  1. 重写ThreadPoolExecutor#afterExecute(Runnable r, Throwable t)方法。
  2. submit
       1. FutureTask的run方法中，调用了callable对象的call方法,如果代码(Runnable)抛出异常，会被捕获并且把这个异常保存下来。在调用get方法时，会将保存的异常重新抛出。
       2. 2.1
  3. execute 

2. 实现Thread.UncaughtExceptionHandler接口，重写uncaughtException方法，在使用线程时将此handler置入。针对于execute()方式。
3. 继承ThreadGroup,重写重写uncaughtException方法，同2.针对于execute()方式。
4. 针对于submit（），异常会被放入Future,catch起future.get()处理。