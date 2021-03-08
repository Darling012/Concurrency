package com.learn.concurrency.example.delayQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayQueueDemo {
    public static void main(String[] args) {
        DelayQueue<DelayTask> queue = new DelayQueue<>();
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        queue.add(new DelayTask(5000, () -> System.out.println("5000 执行了")));
        queue.add(new DelayTask(8000, () -> System.out.println("8000 执行了")));
        while (true) {
            try {
                // 如果没有到时间需要执行的task,take方法会阻塞
                DelayTask task = queue.take();
                task.run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class DelayTask implements Delayed, Runnable {

        // 执行任务的绝对时间
        private long delayTime;
        private Runnable run;

        public DelayTask(long delay, Runnable run) {
            this.delayTime = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(delay);
            this.run = run;
        }

        @Override
        public void run() {
            if (run != null) {
                run.run();
            }
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return  unit.toNanos(delayTime) - System.nanoTime();
        }

        @Override
        public int compareTo(Delayed o) {
            if (getDelay(TimeUnit.NANOSECONDS) < o.getDelay(TimeUnit.NANOSECONDS)) {
                return -1;
            } else if (getDelay(TimeUnit.NANOSECONDS) > o.getDelay(TimeUnit.NANOSECONDS)) {
                return 1;
            }
            return 0;
        }
    }
}
