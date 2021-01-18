package com.learn.concurrency.example.threadPool;

import java.util.concurrent.*;

public class MyExecutorCompletionService<V> implements CompletionService<V> {
    // 任务的执行委托给了executor
    private final Executor executor;
    // 如果executor继承自AbstractExecutorService，aes和executor指向同一个对象，否则aes为空
    private final AbstractExecutorService aes;
    // 保存完成任务的Future
    private final BlockingQueue<Future<V>> completionQueue;

    /**
     * FutureTask extension to enqueue upon completion
     * 继承自FutureTask，重写done()方法以扩展保存结果的功能
     * 会在任务结束后将任务的Future加到completionQueue中
     */
    private class QueueingFuture extends FutureTask<Void> {
        QueueingFuture(RunnableFuture<V> task) {
            super(task, null);
            this.task = task;
        }

        // done()是一个钩子方法，会在任务结束时被调用
        protected void done() {
            // 任务结束后将Future加到队列中
            completionQueue.add(task);
        }
        private final Future<V> task;
    }

    // 将任务包装成FutureTask
    private RunnableFuture<V> newTaskFor(Callable<V> task) {
        if (aes == null)
            return new FutureTask<V>(task);
        else
            return aes.newTaskFor(task);
    }

    // 将任务包装成FutureTask
    private RunnableFuture<V> newTaskFor(Runnable task, V result) {
        if (aes == null)
            return new FutureTask<V>(task, result);
        else
            return aes.newTaskFor(task, result);
    }

    /**
     * Creates an ExecutorCompletionService using the supplied
     * executor for base task execution and a
     * {@link LinkedBlockingQueue} as a completion queue.
     *
     * 构造方法
     *
     * @param executor the executor to use 被使用的线程池
     * @throws NullPointerException if executor is {@code null}
     */
    public MyExecutorCompletionService(Executor executor) {
        // 检查传入的参数
        if (executor == null)
            throw new NullPointerException();
        // 记录线程池
        this.executor = executor;
        // 如果线程池是AbstractExecutorService会将其用aes进行记录
        this.aes = (executor instanceof AbstractExecutorService) ?
                (AbstractExecutorService) executor : null;
        // 初始化装载完成任务的阻塞队列
        this.completionQueue = new LinkedBlockingQueue<Future<V>>();
    }

    /**
     * Creates an ExecutorCompletionService using the supplied
     * executor for base task execution and the supplied queue as its
     * completion queue.
     *
     * 构造方法，可指定装载任务结果的队列
     *
     * @param executor the executor to use
     * @param completionQueue the queue to use as the completion queue
     *        normally one dedicated for use by this service. This
     *        queue is treated as unbounded -- failed attempted
     *        {@code Queue.add} operations for completed taskes cause
     *        them not to be retrievable.
     * @throws NullPointerException if executor or completionQueue are {@code null}
     */
    public MyExecutorCompletionService(Executor executor,
                                       BlockingQueue<Future<V>> completionQueue) {
        // 检查参数
        if (executor == null || completionQueue == null)
            throw new NullPointerException();
        // 记录线程池
        this.executor = executor;
        // 如果线程池是AbstractExecutorService会将其用aes进行记录
        this.aes = (executor instanceof AbstractExecutorService) ?
                (AbstractExecutorService) executor : null;
        // 记录装载任务结果的队列
        this.completionQueue = completionQueue;
    }

    // 提交任务
    public Future<V> submit(Callable<V> task) {
        if (task == null) throw new NullPointerException();
        // 对任务进行包装
        RunnableFuture<V> f = newTaskFor(task);
        // 使用线程池提交任务，提交的任务被QueueingFuture包装了
        executor.execute(new QueueingFuture(f));
        return f;
    }

    // 提交任务
    public Future<V> submit(Runnable task, V result) {
        if (task == null) throw new NullPointerException();
        // 对任务进行包装
        RunnableFuture<V> f = newTaskFor(task, result);
        // 使用线程池提交任务，提交的任务被QueueingFuture包装了
        executor.execute(new QueueingFuture(f));
        return f;
    }

    // 获取任务结果，如果队列为空则阻塞
    public Future<V> take() throws InterruptedException {
        // 从任务结果队列中获取已完成的任务
        return completionQueue.take();
    }

    // 获取任务结果，如果队列为空返回null
    public Future<V> poll() {
        // 从任务结果队列中获取已完成的任务
        return completionQueue.poll();
    }

    // 获取任务结果，如果超时，返回空
    public Future<V> poll(long timeout, TimeUnit unit)
            throws InterruptedException {
        // 从任务结果队列中获取已完成的任务
        return completionQueue.poll(timeout, unit);
    }
}
