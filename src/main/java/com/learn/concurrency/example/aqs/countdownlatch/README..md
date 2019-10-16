- CountDownLatc h 与join 方法的区别。一个区别是，调用一个子线程的join ()
方法后，该线程会一直被阻塞直到子线程运行完毕，而CountDownLatch 则使用计数器来
允许子线程运行完毕或者在运行中递减计数，也就是CountDownLatch 可以在子线程运行
的任何时候让await 方法返回而不一定必须等到线程结束。另外， 使用线程池来管理线程
时一般都是直接添加Runable 到线程池，这时候就没有办法再调用线程的join 方法了，就
是说countDownLatch 相比join 方法让我们对线程同步有更灵活的控制
- CountDownLatch 是使用AQS 实现的。通过构造函数发现，实际上是把计数器的值赋给了AQS 的状态变量state。

