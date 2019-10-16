[网上博客参考链接](https://zhuanlan.zhihu.com/p/28769523)

- Semaphore管理着一组许可（permit）,许可的初始数量可以通过构造函数设定，操作时首先要获取到许可，才能进行操作，操作完成后需要释放许可。如果没有获取许可，则阻塞到有许可被释放。如果初始化了一个许可为1的Semaphore，那么就相当于一个不可重入的互斥锁（Mutex）。
- Semaphore 有两个构造函数，参数为许可的个数 permits 和是否公平竞争 fair。通过 acquire 方法能够获得的许可个数为 permits，如果超过了这个个数，就需要等待。当一个线程 release 释放了一个许可后，fair 决定了正在等待的线程该由谁获取许可，如果是公平竞争则等待时间最长的线程获取，如果是非公平竞争则随机选择一个线程获取许可。不传 fair 的构造函数默认采用非公开竞争。
 ```
  Semaphore(int permits)
  Semaphore(int permits, boolean fair)
```
- 一个线程可以一次获取一个许可，也可以一次获取多个。 在 acquire 等待的过程中，如果线程被中断，acquire 会抛出中断异常，如果希望忽略中断继续等待可以调用 acquireUninterruptibly 方法。同时提供了 tryAcquire 方法尝试获取，获取失败返回 false，获取成功返回 true。tryAcquire 方法可以在获取不到时立即返回，也可以等待一段时间。需要注意的是，没有参数的 tryAcquire 方法在有许可可以获取的情况下，无论有没有线程在等待都能立即获取许可，即便是公平竞争也能立即获取。

```
public void acquire()
public void acquireUninterruptibly()
public boolean tryAcquire()
public boolean tryAcquire(long timeout, TimeUnit unit)
public void release()

public void acquire(int permits)
public void acquireUninterruptibly(int permits)
public boolean tryAcquire(int permits)
public boolean tryAcquire(int permits, long timeout, TimeUnit unit)
public void release(int permits)
```

