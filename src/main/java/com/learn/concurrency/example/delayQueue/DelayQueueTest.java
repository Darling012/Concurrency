package com.learn.concurrency.example.delayQueue;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author daling
 */
public class DelayQueueTest {
    static class DelayedStr implements Delayed {
        /**
         * 延迟时间
         */
        private final long delayTime;
        /**
         * 到期时间
         */
        private final long expire;
        /**
         * 任务名称
         */
        private final String taskName;

        DelayedStr(long delay, String taskName) {
            delayTime = delay;
            this.taskName = taskName;
            expire = System.currentTimeMillis() + delay;
        }

        @Override
        public long getDelay(TimeUnit unit) {

            return unit.convert(this.expire-System.currentTimeMillis(),TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        DelayQueue<DelayedStr> delayQueue = new DelayQueue<>();
        DelayedStr a = new DelayedStr(500,"任务1");
        DelayedStr b = new DelayedStr(1500,"任务2");
        delayQueue.offer(a);
        delayQueue.offer(b);
        DelayedStr now = null;
        for(;;){
            while ((now=delayQueue.take())!= null){
                System.out.println(now.taskName);
            }
        }
    }
}
